package com.focusflow.engine;

/**
 * Adjusts the personal risk threshold from suggestion feedback.
 * Helpful suggestions slightly lower the bar; unhelpful ones raise it to cut spam.
 */
public class AdaptiveThresholdEngine {

    public static final float MIN_THRESHOLD = 0.45f;
    public static final float MAX_THRESHOLD = 0.92f;
    public static final float HELPFUL_DELTA = 0.02f;
    public static final float UNHELPFUL_DELTA = 0.03f;

    public float nextThreshold(float current, boolean suggestionHelpful) {
        float next = suggestionHelpful ? current - HELPFUL_DELTA : current + UNHELPFUL_DELTA;
        return clamp(next);
    }

    public float clamp(float value) {
        return Math.max(MIN_THRESHOLD, Math.min(MAX_THRESHOLD, value));
    }
}
