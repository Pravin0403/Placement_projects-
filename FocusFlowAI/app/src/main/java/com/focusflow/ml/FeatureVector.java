package com.focusflow.ml;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Calendar;

/**
 * Ordered feature vector matching the training pipeline.
 * 0 app_switch_count
 * 1 screen_unlock_count
 * 2 focus_session_duration_min
 * 3 session_elapsed_minutes
 * 4 interaction_frequency
 * 5 previous_break_duration_min
 * 6 time_of_day
 * 7 day_of_week
 * 8 notification_interruption_count
 * 9 risk_score_previous_window
 */
public final class FeatureVector {

    public static final int SIZE = 10;

    private final float[] values;

    public FeatureVector(@NonNull float[] values) {
        if (values.length != SIZE) {
            throw new IllegalArgumentException("Expected " + SIZE + " features");
        }
        this.values = Arrays.copyOf(values, SIZE);
    }

    public static FeatureVector fromWindow(
            int appSwitches,
            int unlocks,
            float sessionDurationMin,
            float elapsedMin,
            float interactionFrequency,
            float previousBreakMin,
            long nowEpochMs,
            int notificationInterruptions,
            float previousRisk
    ) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(nowEpochMs);
        float timeOfDay = calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE) / 60f;
        float dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return new FeatureVector(new float[]{
                appSwitches,
                unlocks,
                sessionDurationMin,
                elapsedMin,
                interactionFrequency,
                previousBreakMin,
                timeOfDay,
                dayOfWeek,
                notificationInterruptions,
                previousRisk
        });
    }

    @NonNull
    public float[] values() {
        return Arrays.copyOf(values, SIZE);
    }

    public float get(int index) {
        return values[index];
    }
}
