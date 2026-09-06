export type TelemetryEventInput = {
  eventId: string;
  sessionId: string;
  type: string;
  packageName?: string | null;
  timestamp: number;
};

export type PredictionInput = {
  predictionId: string;
  sessionId: string;
  riskScore: number;
  modelVersion: string;
  createdAt: number;
};

export type FeedbackInput = {
  predictionId: string;
  helpful: boolean;
  actionTaken?: string | null;
  createdAt: number;
};

export type UserRecord = {
  id: string;
  email: string;
  passwordHash: string;
};

export type IdempotencyRecord = {
  key: string;
  requestHash: string;
  statusCode: number;
};

export type ActiveModel = {
  version: string;
  artifactUrl: string;
  f1Score: number;
};

export interface DataStore {
  createUser(email: string, passwordHash: string): Promise<UserRecord>;
  findUserByEmail(email: string): Promise<UserRecord | null>;
  findUserById(id: string): Promise<UserRecord | null>;
  saveRefreshToken(userId: string, tokenHash: string, expiresAt: Date): Promise<void>;
  consumeRefreshToken(tokenHash: string): Promise<UserRecord | null>;
  upsertTelemetry(userId: string, events: TelemetryEventInput[]): Promise<number>;
  upsertPredictions(userId: string, predictions: PredictionInput[]): Promise<number>;
  upsertFeedback(userId: string, feedback: FeedbackInput): Promise<void>;
  getIdempotency(userId: string, key: string): Promise<IdempotencyRecord | null>;
  saveIdempotency(userId: string, record: IdempotencyRecord): Promise<void>;
  getActiveModel(): Promise<ActiveModel>;
}

export const DEFAULT_MODEL: ActiveModel = {
  version: "logistic-baseline-1.0.0",
  // The Android client uses its bundled logistic fallback until a real signed-off TFLite
  // artifact is published. An empty URL explicitly means "no remote artifact available".
  artifactUrl: "",
  f1Score: 0.81,
};
