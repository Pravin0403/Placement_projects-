package com.focusflow.engine;

import com.focusflow.domain.model.FocusZone;

import java.util.List;

/**
 * Engine for calculating hourly distraction predictions throughout the day.
 */
public interface HourlyPredictionEngine {

    /**
     * Generate hourly distraction predictions for the current day.
     * @return List of 24 hourly predictions (risk scores and confidence)
     */
    List<HourlyPrediction> generateDailyPredictions();

    /**
     * Get prediction for a specific hour.
     * @param hour Hour of day (0-23)
     * @return Hourly prediction for the specified hour
     */
    HourlyPrediction getPredictionForHour(int hour);

    /**
     * Generate distraction risk for the next 15 minutes during a session.
     * @param currentRisk Current distraction risk
     * @param sessionElapsedMinutes Minutes elapsed in current session
     * @return Risk prediction for next 15 minutes
     */
    float predictNearTermRisk(float currentRisk, int sessionElapsedMinutes);
}

/**
 * Represents an hourly distraction prediction.
 */
class HourlyPrediction {
    private final int hour;
    private final float distractionProbability;
    private final float confidence;
    private final String recommendation;

    public HourlyPrediction(int hour, float distractionProbability, float confidence, String recommendation) {
        this.hour = hour;
        this.distractionProbability = distractionProbability;
        this.confidence = confidence;
        this.recommendation = recommendation;
    }

    public int hour() {
        return hour;
    }

    public float distractionProbability() {
        return distractionProbability;
    }

    public float confidence() {
        return confidence;
    }

    public String recommendation() {
        return recommendation;
    }
}