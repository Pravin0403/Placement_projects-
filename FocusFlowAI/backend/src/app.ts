import bcrypt from "bcryptjs";
import cors from "cors";
import express, { NextFunction, Request, Response } from "express";
import helmet from "helmet";
import pinoHttp from "pino-http";
import { z } from "zod";
import { hashToken, issueTokens, requestHash, verifyAccessToken } from "./auth";
import { logger } from "./logger";
import { JobQueues } from "./queues";
import { DataStore } from "./store/types";

declare global {
  namespace Express {
    interface Request {
      userId?: string;
    }
  }
}

const authBody = z.object({
  email: z.string().email(),
  password: z.string().min(8),
});

const telemetryBody = z.object({
  events: z.array(
    z.object({
      eventId: z.string().min(1),
      sessionId: z.string().min(1),
      type: z.string().min(1),
      packageName: z.string().nullable().optional(),
      timestamp: z.number(),
    })
  ),
});

const predictionBody = z.object({
  predictions: z.array(
    z.object({
      predictionId: z.string().min(1),
      sessionId: z.string().min(1),
      riskScore: z.number(),
      modelVersion: z.string().min(1),
      createdAt: z.number(),
    })
  ),
});

const feedbackBody = z.object({
  predictionId: z.string().min(1),
  helpful: z.boolean(),
  actionTaken: z.string().nullable().optional(),
  createdAt: z.number(),
});

const refreshBody = z.object({
  refreshToken: z.string().min(16),
});

function refreshExpiry(): Date {
  return new Date(Date.now() + 30 * 24 * 60 * 60 * 1000);
}

function authRateLimit() {
  const hits = new Map<string, { count: number; resetAt: number }>();
  return (req: Request, res: Response, next: NextFunction) => {
    const ip = req.ip ?? "unknown";
    const now = Date.now();
    const windowMs = 15 * 60 * 1000;
    const existing = hits.get(ip);
    if (!existing || existing.resetAt < now) {
      hits.set(ip, { count: 1, resetAt: now + windowMs });
      next();
      return;
    }
    existing.count += 1;
    if (existing.count > 40) {
      res.status(429).json({ error: "Too many auth attempts" });
      return;
    }
    next();
  };
}

export function createApp(store: DataStore, queues: JobQueues) {
  const app = express();
  app.use(helmet());
  app.use(cors());
  app.use(express.json({ limit: "1mb" }));
  app.use(pinoHttp({ logger }));
  const limitAuth = authRateLimit();

  app.get("/health", (_req, res) => {
    res.json({ ok: true });
  });

  app.post("/auth/register", limitAuth, async (req, res, next) => {
    try {
      const body = authBody.parse(req.body);
      const existing = await store.findUserByEmail(body.email);
      if (existing) {
        res.status(409).json({ error: "Email already registered" });
        return;
      }
      const passwordHash = await bcrypt.hash(body.password, 10);
      const user = await store.createUser(body.email, passwordHash);
      const tokens = issueTokens(user.id, user.email);
      await store.saveRefreshToken(user.id, hashToken(tokens.refreshToken), refreshExpiry());
      res.status(201).json({ accessToken: tokens.accessToken, refreshToken: tokens.refreshToken });
    } catch (error) {
      next(error);
    }
  });

  app.post("/auth/login", limitAuth, async (req, res, next) => {
    try {
      const body = authBody.parse(req.body);
      const user = await store.findUserByEmail(body.email);
      if (!user || !(await bcrypt.compare(body.password, user.passwordHash))) {
        res.status(401).json({ error: "Invalid credentials" });
        return;
      }
      const tokens = issueTokens(user.id, user.email);
      await store.saveRefreshToken(user.id, hashToken(tokens.refreshToken), refreshExpiry());
      res.json({ accessToken: tokens.accessToken, refreshToken: tokens.refreshToken });
    } catch (error) {
      next(error);
    }
  });

  app.post("/auth/refresh", limitAuth, async (req, res, next) => {
    try {
      const body = refreshBody.parse(req.body);
      const user = await store.consumeRefreshToken(hashToken(body.refreshToken));
      if (!user) {
        res.status(401).json({ error: "Invalid refresh token" });
        return;
      }
      const tokens = issueTokens(user.id, user.email);
      await store.saveRefreshToken(user.id, hashToken(tokens.refreshToken), refreshExpiry());
      res.json({ accessToken: tokens.accessToken, refreshToken: tokens.refreshToken });
    } catch (error) {
      next(error);
    }
  });

  const requireAuth = (req: Request, res: Response, next: NextFunction) => {
    const header = req.header("authorization") ?? "";
    const token = header.startsWith("Bearer ") ? header.slice(7) : "";
    if (!token) {
      res.status(401).json({ error: "Missing token" });
      return;
    }
    try {
      req.userId = verifyAccessToken(token).sub;
      next();
    } catch {
      res.status(401).json({ error: "Invalid token" });
    }
  };

  async function enforceIdempotency(
    userId: string,
    key: string | undefined,
    body: unknown,
    res: Response
  ): Promise<boolean> {
    if (!key) {
      return true;
    }
    const hash = requestHash(body);
    const existing = await store.getIdempotency(userId, key);
    if (!existing) {
      return true;
    }
    if (existing.requestHash !== hash) {
      res.status(409).json({ error: "Idempotency key reused with a different payload" });
      return false;
    }
    res.status(existing.statusCode).end();
    return false;
  }

  app.post("/telemetry/batch", requireAuth, async (req, res, next) => {
    try {
      const userId = req.userId!;
      const body = telemetryBody.parse(req.body);
      const key = req.header("idempotency-key") ?? undefined;
      if (!(await enforceIdempotency(userId, key, body, res))) {
        return;
      }
      await queues.enqueueTelemetry(userId, body.events);
      if (key) {
        await store.saveIdempotency(userId, {
          key,
          requestHash: requestHash(body),
          statusCode: 202,
        });
      }
      res.status(202).end();
    } catch (error) {
      next(error);
    }
  });

  app.post("/predictions/batch", requireAuth, async (req, res, next) => {
    try {
      const userId = req.userId!;
      const body = predictionBody.parse(req.body);
      const key = req.header("idempotency-key") ?? undefined;
      if (!(await enforceIdempotency(userId, key, body, res))) {
        return;
      }
      await queues.enqueuePredictions(userId, body.predictions);
      if (key) {
        await store.saveIdempotency(userId, {
          key,
          requestHash: requestHash(body),
          statusCode: 202,
        });
      }
      res.status(202).end();
    } catch (error) {
      next(error);
    }
  });

  app.post("/feedback", requireAuth, async (req, res, next) => {
    try {
      const body = feedbackBody.parse(req.body);
      await store.upsertFeedback(req.userId!, body);
      res.status(204).end();
    } catch (error) {
      next(error);
    }
  });

  app.get("/models/active", requireAuth, async (_req, res, next) => {
    try {
      const model = await store.getActiveModel();
      res.json({
        version: model.version,
        artifactUrl: model.artifactUrl,
        f1Score: model.f1Score,
      });
    } catch (error) {
      next(error);
    }
  });

  app.use((error: unknown, _req: Request, res: Response, _next: NextFunction) => {
    if (error instanceof z.ZodError) {
      res.status(400).json({ error: "Invalid request", details: error.flatten() });
      return;
    }
    logger.error({ err: error }, "unhandled error");
    res.status(500).json({ error: "Internal error" });
  });

  return app;
}
