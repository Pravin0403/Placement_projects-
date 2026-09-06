# Deploy FocusFlow AI on free tiers

This configuration deploys the API to Render and the PostgreSQL database to Supabase. It is suitable for a portfolio or personal demo, not a guaranteed always-on production service.

## 1. Push this project to GitHub

Create a private GitHub repository and push the complete `FocusFlowAI` project. Do not commit `.env` files, database URLs, or tokens.

## 2. Create the free PostgreSQL database

1. Create a project at [Supabase](https://supabase.com).
2. In **Connect**, copy the PostgreSQL connection string for server-side use.
3. Keep this value private; it becomes Render's `DATABASE_URL`.

## 3. Deploy the API on Render

1. Create a Render account and choose **New > Blueprint**.
2. Connect the GitHub repository and select this project. Render reads `render.yaml` from the repository root.
3. When prompted, enter the Supabase URL as `DATABASE_URL`.
4. Deploy. The Docker image runs Prisma migrations before the API starts.
5. Open `https://<your-render-service>.onrender.com/health`. A successful deployment responds with `{ "ok": true }`.

Render supplies HTTPS automatically. Its free web services sleep after inactivity, so the first request after a pause can take about a minute.

## 4. Point Android at the public API

In `app/build.gradle.kts`, replace the temporary local URL in `API_BASE_URL` with the Render URL, including the final slash:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://<your-render-service>.onrender.com/\"")
```

Rebuild and install the app on the phone. The public HTTPS address works on mobile data as well as Wi-Fi; the laptop no longer needs to be on.

## Free-tier limits

- Render's free web service sleeps after 15 minutes of inactivity.
- Supabase free projects can pause after a week of inactivity and have limited storage.
- Use paid hosting, backups, monitoring, and a real deployed model before relying on the app for real users.
