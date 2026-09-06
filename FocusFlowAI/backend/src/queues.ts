import { Queue, Worker, JobsOptions } from "bullmq";
import IORedis from "ioredis";
import { logger } from "./logger";
import { DataStore, PredictionInput, TelemetryEventInput } from "./store/types";

const jobOptions: JobsOptions = {
  attempts: 5,
  backoff: { type: "exponential", delay: 1000 },
  removeOnComplete: 1000,
  removeOnFail: 5000,
};

export type JobQueues = {
  enqueueTelemetry(userId: string, events: TelemetryEventInput[]): Promise<void>;
  enqueuePredictions(userId: string, predictions: PredictionInput[]): Promise<void>;
  close(): Promise<void>;
};

export function createInlineQueues(store: DataStore): JobQueues {
  return {
    async enqueueTelemetry(userId, events) {
      await store.upsertTelemetry(userId, events);
    },
    async enqueuePredictions(userId, predictions) {
      await store.upsertPredictions(userId, predictions);
    },
    async close() {
      return;
    },
  };
}

export function createBullQueues(store: DataStore, redisUrl: string): JobQueues {
  const connection = new IORedis(redisUrl, { maxRetriesPerRequest: null });
  const telemetry = new Queue("ingest-telemetry", { connection });
  const predictions = new Queue("ingest-predictions", { connection });

  const telemetryWorker = new Worker(
    "ingest-telemetry",
    async (job) => {
      await store.upsertTelemetry(job.data.userId, job.data.events);
    },
    { connection }
  );
  const predictionWorker = new Worker(
    "ingest-predictions",
    async (job) => {
      await store.upsertPredictions(job.data.userId, job.data.predictions);
    },
    { connection }
  );

  telemetryWorker.on("failed", (job, error) => {
    logger.error({ jobId: job?.id, err: error }, "telemetry ingest failed");
  });
  predictionWorker.on("failed", (job, error) => {
    logger.error({ jobId: job?.id, err: error }, "prediction ingest failed");
  });

  return {
    async enqueueTelemetry(userId, events) {
      await telemetry.add("batch", { userId, events }, jobOptions);
    },
    async enqueuePredictions(userId, predictionsBatch) {
      await predictions.add("batch", { userId, predictions: predictionsBatch }, jobOptions);
    },
    async close() {
      await telemetryWorker.close();
      await predictionWorker.close();
      await telemetry.close();
      await predictions.close();
      await connection.quit();
    },
  };
}
