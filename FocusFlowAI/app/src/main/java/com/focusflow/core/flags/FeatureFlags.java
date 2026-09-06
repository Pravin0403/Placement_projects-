package com.focusflow.core.flags;

import android.content.SharedPreferences;

/**
 * Local feature flags. Remote config is not required; defaults are production-safe.
 */
public final class FeatureFlags {

    public static final String KEY_INCREMENTAL_LEARNING = "flag_incremental_learning";
    public static final String KEY_NOTIFICATION_INTERRUPTS = "flag_notification_interrupts";
    public static final String KEY_BATCH_SYNC = "flag_batch_sync";

    private final SharedPreferences preferences;

    public FeatureFlags(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public boolean incrementalLearning() {
        return preferences.getBoolean(KEY_INCREMENTAL_LEARNING, true);
    }

    public void setIncrementalLearning(boolean enabled) {
        preferences.edit().putBoolean(KEY_INCREMENTAL_LEARNING, enabled).apply();
    }

    public boolean notificationInterrupts() {
        return preferences.getBoolean(KEY_NOTIFICATION_INTERRUPTS, true);
    }

    public void setNotificationInterrupts(boolean enabled) {
        preferences.edit().putBoolean(KEY_NOTIFICATION_INTERRUPTS, enabled).apply();
    }

    public boolean batchSync() {
        return preferences.getBoolean(KEY_BATCH_SYNC, true);
    }

    public void setBatchSync(boolean enabled) {
        preferences.edit().putBoolean(KEY_BATCH_SYNC, enabled).apply();
    }
}
