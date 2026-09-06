import { randomUUID } from "crypto";
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

export class MemoryStore implements DataStore {
  private users: UserRecord[] = [];
  private refresh: Array<{ userId: string; tokenHash: string; expiresAt: Date }> = [];
  private events: Array<TelemetryEventInput & { userId: string }> = [];
  private predictions: Array<PredictionInput & { userId: string }> = [];
  private feedback: Array<FeedbackInput & { userId: string }> = [];
  private idempotency: Array<IdempotencyRecord & { userId: string }> = [];
  private model: ActiveModel = DEFAULT_MODEL;

  async createUser(email: string, passwordHash: string): Promise<UserRecord> {
    const user = { id: randomUUID(), email: email.toLowerCase(), passwordHash };
    this.users.push(user);
    return user;
  }

  async findUserByEmail(email: string): Promise<UserRecord | null> {
    return this.users.find((user) => user.email === email.toLowerCase()) ?? null;
  }

  async findUserById(id: string): Promise<UserRecord | null> {
    return this.users.find((user) => user.id === id) ?? null;
  }

  async saveRefreshToken(userId: string, tokenHash: string, expiresAt: Date): Promise<void> {
    this.refresh.push({ userId, tokenHash, expiresAt });
  }

  async consumeRefreshToken(tokenHash: string): Promise<UserRecord | null> {
    const index = this.refresh.findIndex(
      (row) => row.tokenHash === tokenHash && row.expiresAt.getTime() > Date.now()
    );
    if (index < 0) {
      return null;
    }
    const [row] = this.refresh.splice(index, 1);
    return this.findUserById(row.userId);
  }

  async upsertTelemetry(userId: string, events: TelemetryEventInput[]): Promise<number> {
    let inserted = 0;
    for (const event of events) {
      const exists = this.events.some((row) => row.userId === userId && row.eventId === event.eventId);
      if (!exists) {
        this.events.push({ ...event, userId });
        inserted += 1;
      }
    }
    return inserted;
  }

  async upsertPredictions(userId: string, predictions: PredictionInput[]): Promise<number> {
    let inserted = 0;
    for (const prediction of predictions) {
      const exists = this.predictions.some(
        (row) => row.userId === userId && row.predictionId === prediction.predictionId
      );
      if (!exists) {
        this.predictions.push({ ...prediction, userId });
        inserted += 1;
      }
    }
    return inserted;
  }

  async upsertFeedback(userId: string, feedback: FeedbackInput): Promise<void> {
    const index = this.feedback.findIndex(
      (row) => row.userId === userId && row.predictionId === feedback.predictionId
    );
    if (index >= 0) {
      this.feedback[index] = { ...feedback, userId };
    } else {
      this.feedback.push({ ...feedback, userId });
    }
  }

  async getIdempotency(userId: string, key: string): Promise<IdempotencyRecord | null> {
    return this.idempotency.find((row) => row.userId === userId && row.key === key) ?? null;
  }

  async saveIdempotency(userId: string, record: IdempotencyRecord): Promise<void> {
    this.idempotency.push({ ...record, userId });
  }

  async getActiveModel(): Promise<ActiveModel> {
    return this.model;
  }
}
