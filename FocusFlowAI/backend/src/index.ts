import "dotenv/config";
import { PrismaClient } from "@prisma/client";
import { createApp } from "./app";
import { logger } from "./logger";
import { createBullQueues, createInlineQueues } from "./queues";
import { PrismaStore } from "./store/prismaStore";

async function main() {
  if (!process.env.JWT_SECRET) {
    throw new Error("JWT_SECRET must be set");
  }
  const prisma = new PrismaClient();
  const store = new PrismaStore(prisma);
  const redisUrl = process.env.REDIS_URL;
  const queues = redisUrl ? createBullQueues(store, redisUrl) : createInlineQueues(store);
  const app = createApp(store, queues);
  const port = Number(process.env.PORT ?? 8080);
  const server = app.listen(port, "0.0.0.0", () => {
    logger.info({ port, redis: Boolean(redisUrl) }, "FocusFlow API listening");
  });

  const shutdown = async () => {
    server.close();
    await queues.close();
    await prisma.$disconnect();
    process.exit(0);
  };
  process.on("SIGINT", shutdown);
  process.on("SIGTERM", shutdown);
}

main().catch((error) => {
  logger.error({ err: error }, "failed to start");
  process.exit(1);
});
