import { PrismaClient } from "@prisma/client";
import {
  ActiveModel,
  DataStore,
  DEFAULT_MODEL,
  FeedbackInput,
  IdempotencyRecord,
  PredictionInput,
  TelemetryEventInput,
  UserRecord,
} from "./types";

export class PrismaStore implements DataStore {
  constructor(private readonly prisma: PrismaClient) {}

  async createUser(email: string, passwordHash: string): Promise<UserRecord> {
    return this.prisma.user.create({
      data: { email: email.toLowerCase(), passwordHash },
      select: { id: true, email: true, passwordHash: true },
    });
  }

  async findUserByEmail(email: string): Promise<UserRecord | null> {
    return this.prisma.user.findUnique({
      where: { email: email.toLowerCase() },
      select: { id: true, email: true, passwordHash: true },
    });
  }

  async findUserById(id: string): Promise<UserRecord | null> {
    return this.prisma.user.findUnique({
      where: { id },
      select: { id: true, email: true, passwordHash: true },
    });
  }

  async saveRefreshToken(userId: string, tokenHash: string, expiresAt: Date): Promise<void> {
    await this.prisma.refreshToken.create({ data: { userId, tokenHash, expiresAt } });
  }

  async consumeRefreshToken(tokenHash: string): Promise<UserRecord | null> {
    const row = await this.prisma.refreshToken.findFirst({
      where: { tokenHash, expiresAt: { gt: new Date() } },
    });
    if (!row) {
      return null;
    }
    await this.prisma.refreshToken.delete({ where: { id: row.id } });
    return this.findUserById(row.userId);
  }

  async upsertTelemetry(userId: string, events: TelemetryEventInput[]): Promise<number> {
    let inserted = 0;
    for (const event of events) {
      const result = await this.prisma.telemetryEvent.upsert({
        where: { userId_eventId: { userId, eventId: event.eventId } },
        update: {},
        create: {
          userId,
          eventId: event.eventId,
          sessionId: event.sessionId,
          type: event.type,
          packageName: event.packageName ?? null,
          timestamp: BigInt(event.timestamp),
        },
      });
      if (result) {
        inserted += 1;
      }
    }
    const earliest = new Map<string, number>();
    for (const event of events) {
      const current = earliest.get(event.sessionId);
      if (current === undefined || event.timestamp < current) {
        earliest.set(event.sessionId, event.timestamp);
      }
    }
    for (const [sessionId, timestamp] of earliest) {
      await this.prisma.focusSession.upsert({
        where: { userId_externalId: { userId, externalId: sessionId } },
        update: {},
        create: {
          userId,
          externalId: sessionId,
          startedAt: new Date(timestamp),
        },
      });
    }
    for (const event of events) {
      if (event.type === "SESSION_END") {
        await this.prisma.focusSession.updateMany({
          where: { userId, externalId: event.sessionId },
          data: { endedAt: new Date(event.timestamp) },
        });
      }
    }
    return inserted;
  }

  async upsertPredictions(userId: string, predictions: PredictionInput[]): Promise<number> {
    let inserted = 0;
    for (const prediction of predictions) {
      await this.prisma.prediction.upsert({
        where: { userId_predictionId: { userId, predictionId: prediction.predictionId } },
        update: {},
        create: {
          userId,
          predictionId: prediction.predictionId,
          sessionId: prediction.sessionId,
          riskScore: prediction.riskScore,
          modelVersion: prediction.modelVersion,
          createdAtMs: BigInt(prediction.createdAt),
        },
      });
      await this.prisma.telemetryWindow.create({
        data: {
          userId,
          sessionId: prediction.sessionId,
          timestamp: BigInt(prediction.createdAt),
          features: {
            predictionId: prediction.predictionId,
            riskScore: prediction.riskScore,
            modelVersion: prediction.modelVersion,
          },
        },
      });
      inserted += 1;
    }
    return inserted;
  }

  async upsertFeedback(userId: string, feedback: FeedbackInput): Promise<void> {
    await this.prisma.feedback.upsert({
      where: { userId_predictionId: { userId, predictionId: feedback.predictionId } },
      update: {
        helpful: feedback.helpful,
        actionTaken: feedback.actionTaken ?? null,
        createdAtMs: BigInt(feedback.createdAt),
      },
      create: {
        userId,
        predictionId: feedback.predictionId,
        helpful: feedback.helpful,
        actionTaken: feedback.actionTaken ?? null,
        createdAtMs: BigInt(feedback.createdAt),
      },
    });
  }

  async getIdempotency(userId: string, key: string): Promise<IdempotencyRecord | null> {
    const row = await this.prisma.idempotencyKey.findUnique({
      where: { userId_key: { userId, key } },
    });
    if (!row) {
      return null;
    }
    return { key: row.key, requestHash: row.requestHash, statusCode: row.statusCode };
  }

  async saveIdempotency(userId: string, record: IdempotencyRecord): Promise<void> {
    await this.prisma.idempotencyKey.create({
      data: {
        userId,
        key: record.key,
        requestHash: record.requestHash,
        statusCode: record.statusCode,
      },
    });
  }

  async getActiveModel(): Promise<ActiveModel> {
    const row = await this.prisma.modelArtifact.findFirst({ where: { active: true } });
    if (!row) {
      await this.prisma.modelArtifact.create({
        data: { ...DEFAULT_MODEL, active: true },
      });
      return DEFAULT_MODEL;
    }
    return { version: row.version, artifactUrl: row.artifactUrl, f1Score: row.f1Score };
  }
}
