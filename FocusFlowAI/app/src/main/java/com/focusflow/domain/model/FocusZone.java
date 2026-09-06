package com.focusflow.domain.model;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * Represents an hourly focus zone with score and prediction data.
 */
public class FocusZone {

    private final int hourOfDay;
    private final int focusScore;
    private final float distractionRisk;
    private final float confidence;
    private final String recommendation;
    private final boolean isRecommended;

    public FocusZone(
            int hourOfDay,
            int focusScore,
            float distractionRisk,
            float confidence,
            @NonNull String recommendation,
            boolean isRecommended
    ) {
        this.hourOfDay = hourOfDay;
        this.focusScore = focusScore;
        this.distractionRisk = distractionRisk;
        this.confidence = confidence;
        this.recommendation = recommendation;
        this.isRecommended = isRecommended;
    }

    public int hourOfDay() {
        return hourOfDay;
    }

    public int focusScore() {
        return focusScore;
    }

    public float distractionRisk() {
        return distractionRisk;
    }

    public float confidence() {
        return confidence;
    }

    @NonNull
    public String recommendation() {
        return recommendation;
    }

    public boolean isRecommended() {
        return isRecommended;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FocusZone focusZone = (FocusZone) o;
        return hourOfDay == focusZone.hourOfDay;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hourOfDay);
    }
}