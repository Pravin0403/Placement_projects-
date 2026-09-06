process.env.JWT_SECRET = process.env.JWT_SECRET ?? "test-secret";

import request from "supertest";
import { createApp } from "../src/app";
import { createInlineQueues } from "../src/queues";
import { MemoryStore } from "../src/store/memory";

function build() {
  const store = new MemoryStore();
  const app = createApp(store, createInlineQueues(store));
  return app;
}

describe("auth", () => {
  it("registers and logs in a user", async () => {
    const app = build();
    const registered = await request(app)
      .post("/auth/register")
      .send({ email: "alex@example.com", password: "password1" });
    expect(registered.status).toBe(201);
    expect(registered.body.accessToken).toBeTruthy();
    expect(registered.body.refreshToken).toBeTruthy();

    const login = await request(app)
      .post("/auth/login")
      .send({ email: "alex@example.com", password: "password1" });
    expect(login.status).toBe(200);
    expect(login.body.accessToken).toBeTruthy();
  });

  it("rejects duplicate registration", async () => {
    const app = build();
    await request(app).post("/auth/register").send({ email: "alex@example.com", password: "password1" });
    const second = await request(app)
      .post("/auth/register")
      .send({ email: "alex@example.com", password: "password1" });
    expect(second.status).toBe(409);
  });

  it("rotates refresh tokens and rejects a reused refresh token", async () => {
    const app = build();
    const registered = await request(app)
      .post("/auth/register")
      .send({ email: "refresh@example.com", password: "password1" });

    const refreshed = await request(app)
      .post("/auth/refresh")
      .send({ refreshToken: registered.body.refreshToken });
    expect(refreshed.status).toBe(200);
    expect(refreshed.body.accessToken).toBeTruthy();
    expect(refreshed.body.refreshToken).toBeTruthy();
    expect(refreshed.body.refreshToken).not.toBe(registered.body.refreshToken);

    const reused = await request(app)
      .post("/auth/refresh")
      .send({ refreshToken: registered.body.refreshToken });
    expect(reused.status).toBe(401);
  });

  it("applies baseline HTTP security headers", async () => {
    const app = build();
    const response = await request(app).get("/health");
    expect(response.headers["x-content-type-options"]).toBe("nosniff");
  });
});

describe("idempotent batches", () => {
  async function authToken(app: ReturnType<typeof build>) {
    const response = await request(app)
      .post("/auth/register")
      .send({ email: "batch@example.com", password: "password1" });
    return response.body.accessToken as string;
  }

  const payload = {
    events: [
      {
        eventId: "evt-1",
        sessionId: "12",
        type: "APP_SWITCH",
        packageName: "com.example",
        timestamp: 1,
      },
    ],
  };

  it("replays the same idempotency key without duplicating work", async () => {
    const app = build();
    const token = await authToken(app);
    const first = await request(app)
      .post("/telemetry/batch")
      .set("Authorization", `Bearer ${token}`)
      .set("Idempotency-Key", "batch-1")
      .send(payload);
    const second = await request(app)
      .post("/telemetry/batch")
      .set("Authorization", `Bearer ${token}`)
      .set("Idempotency-Key", "batch-1")
      .send(payload);
    expect(first.status).toBe(202);
    expect(second.status).toBe(202);
  });

  it("rejects a reused key with a different payload", async () => {
    const app = build();
    const token = await authToken(app);
    await request(app)
      .post("/telemetry/batch")
      .set("Authorization", `Bearer ${token}`)
      .set("Idempotency-Key", "batch-2")
      .send(payload);
    const conflict = await request(app)
      .post("/telemetry/batch")
      .set("Authorization", `Bearer ${token}`)
      .set("Idempotency-Key", "batch-2")
      .send({
        events: [
          {
            eventId: "evt-2",
            sessionId: "12",
            type: "SCREEN_UNLOCK",
            timestamp: 2,
          },
        ],
      });
    expect(conflict.status).toBe(409);
  });

  it("requires authentication", async () => {
    const app = build();
    const response = await request(app).post("/telemetry/batch").send(payload);
    expect(response.status).toBe(401);
  });
});
