package com.focusflow.core.common;

public final class Constants {

    public static final String DATABASE_NAME = "focusflow.db";
    public static final int USER_PREFERENCES_ROW_ID = 1;
    public static final int DEFAULT_WINDOW_MINUTES = 5;
    public static final int DEFAULT_HORIZON_MINUTES = 15;
    public static final long WINDOW_DURATION_MS = DEFAULT_WINDOW_MINUTES * 60_000L;
    public static final long PREDICTION_INTERVAL_MS = 60_000L;
    public static final long ALERT_COOLDOWN_MS = 10 * 60_000L;
    public static final long MIN_SESSION_BEFORE_ALERT_MS = 3 * 60_000L;
    public static final float DEFAULT_RISK_THRESHOLD = 0.72f;
    public static final String MODEL_VERSION = "logistic-baseline-1.0.0";
    public static final String NOTIFICATION_CHANNEL_ID = "focus_session";
    public static final int SESSION_NOTIFICATION_ID = 42;

    public static final String EVENT_APP_SWITCH = "APP_SWITCH";
    public static final String EVENT_SCREEN_UNLOCK = "SCREEN_UNLOCK";
    public static final String EVENT_SCREEN_OFF = "SCREEN_OFF";
    public static final String EVENT_SESSION_START = "SESSION_START";
    public static final String EVENT_SESSION_END = "SESSION_END";

    private Constants() {
    }
}
