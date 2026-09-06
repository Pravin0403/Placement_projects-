import { createHmac, randomBytes } from "crypto";
import jwt from "jsonwebtoken";

export type TokenPair = {
  accessToken: string;
  refreshToken: string;
};

export function requireJwtSecret(): string {
  const secret = process.env.JWT_SECRET;
  if (!secret) {
    throw new Error("JWT_SECRET is required");
  }
  return secret;
}

export function hashToken(token: string): string {
  return createHmac("sha256", requireJwtSecret()).update(token).digest("hex");
}

export function issueTokens(userId: string, email: string): TokenPair {
  const secret = requireJwtSecret();
  const accessToken = jwt.sign({ sub: userId, email }, secret, { expiresIn: "15m" });
  const refreshToken = randomBytes(32).toString("hex");
  return { accessToken, refreshToken };
}

export function verifyAccessToken(token: string): { sub: string; email: string } {
  const payload = jwt.verify(token, requireJwtSecret()) as { sub: string; email: string };
  if (!payload.sub) {
    throw new Error("Invalid token");
  }
  return payload;
}

export function requestHash(body: unknown): string {
  return createHmac("sha256", "idempotency").update(JSON.stringify(body)).digest("hex");
}
