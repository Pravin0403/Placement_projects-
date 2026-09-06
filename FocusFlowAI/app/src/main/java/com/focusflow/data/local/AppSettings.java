package com.focusflow.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.focusflow.core.common.Constants;
import com.focusflow.engine.AdaptiveThresholdEngine;

public class AppSettings {

    private static final String FILE_NAME = "focusflow_settings";
    private static final String KEY_THRESHOLD = "personal_risk_threshold";
    private static final String KEY_LAST_ALERT = "last_alert_epoch_ms";
    private static final String KEY_LAST_BREAK_MS = "last_break_duration_ms";
    private static final String KEY_PREV_RISK = "previous_window_risk";
    private static final String KEY_API_BASE = "api_base_url";
    private static final String KEY_LAST_PREDICTION_ID = "last_prediction_id";
    private static final String KEY_CIRCUIT_FAILURES = "circuit_failures";
    private static final String KEY_CIRCUIT_OPEN_UNTIL = "circuit_open_until";
    private static final String KEY_SLEEP_START_HOUR = "sleep_start_hour";
    private static final String KEY_SLEEP_END_HOUR = "sleep_end_hour";
    private static final String KEY_PROTECTION_LEVEL = "protection_level";

    private final SharedPreferences preferences;
    private final EncryptedTokenStore tokenStore;
    private final AdaptiveThresholdEngine thresholdEngine = new AdaptiveThresholdEngine();

    public AppSettings(Context context) {
        preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        tokenStore = new EncryptedTokenStore(context, preferences);
    }

    public float personalRiskThreshold() {
        return preferences.getFloat(KEY_THRESHOLD, Constants.DEFAULT_RISK_THRESHOLD);
    }

    public void setPersonalRiskThreshold(float value) {
        preferences.edit().putFloat(KEY_THRESHOLD, thresholdEngine.clamp(value)).apply();
    }

    public void adaptThreshold(boolean suggestionHelpful) {
        setPersonalRiskThreshold(thresholdEngine.nextThreshold(personalRiskThreshold(), suggestionHelpful));
    }

    public long lastAlertEpochMs() {
        return preferences.getLong(KEY_LAST_ALERT, 0L);
    }

    public void setLastAlertEpochMs(long value) {
        preferences.edit().putLong(KEY_LAST_ALERT, value).apply();
    }

    public long lastBreakDurationMs() {
        return preferences.getLong(KEY_LAST_BREAK_MS, 0L);
    }

    public void setLastBreakDurationMs(long value) {
        preferences.edit().putLong(KEY_LAST_BREAK_MS, value).apply();
    }

    public float previousWindowRisk() {
        return preferences.getFloat(KEY_PREV_RISK, 0f);
    }

    public void setPreviousWindowRisk(float value) {
        preferences.edit().putFloat(KEY_PREV_RISK, value).apply();
    }

    public String accessToken() {
        return tokenStore.accessToken();
    }

    public String refreshToken() {
        return tokenStore.refreshToken();
    }

    public void setTokens(String access, String refresh) {
        tokenStore.setTokens(access, refresh);
    }

    public boolean canStoreTokens() {
        return tokenStore.isAvailable();
    }

    public String apiBaseUrl() {
        return preferences.getString(KEY_API_BASE, "");
    }

    public void setApiBaseUrl(String url) {
        preferences.edit().putString(KEY_API_BASE, url == null ? "" : url).apply();
    }

    public String lastPredictionId() {
        return preferences.getString(KEY_LAST_PREDICTION_ID, "");
    }

    public void setLastPredictionId(String predictionId) {
        preferences.edit().putString(KEY_LAST_PREDICTION_ID, predictionId == null ? "" : predictionId).apply();
    }

    public int circuitFailures() {
        return preferences.getInt(KEY_CIRCUIT_FAILURES, 0);
    }

    public void recordSyncSuccess() {
        preferences.edit()
                .putInt(KEY_CIRCUIT_FAILURES, 0)
                .putLong(KEY_CIRCUIT_OPEN_UNTIL, 0L)
                .apply();
    }

    public void recordSyncFailure() {
        int next = circuitFailures() + 1;
        long openUntil = next >= 5 ? System.currentTimeMillis() + 15 * 60_000L : 0L;
        preferences.edit()
                .putInt(KEY_CIRCUIT_FAILURES, next)
                .putLong(KEY_CIRCUIT_OPEN_UNTIL, openUntil)
                .apply();
    }

    public boolean isCircuitOpen() {
        return System.currentTimeMillis() < preferences.getLong(KEY_CIRCUIT_OPEN_UNTIL, 0L);
    }

    public int sleepStartHour() {
        return preferences.getInt(KEY_SLEEP_START_HOUR, 22);
    }

    public void setSleepStartHour(int hour) {
        preferences.edit().putInt(KEY_SLEEP_START_HOUR, hour).apply();
    }

    public int sleepEndHour() {
        return preferences.getInt(KEY_SLEEP_END_HOUR, 7);
    }

    public void setSleepEndHour(int hour) {
        preferences.edit().putInt(KEY_SLEEP_END_HOUR, hour).apply();
    }

    public String protectionLevel() {
        return preferences.getString(KEY_PROTECTION_LEVEL, "LIGHT");
    }

    public void setProtectionLevel(String level) {
        preferences.edit().putString(KEY_PROTECTION_LEVEL, level).apply();
    }
}
