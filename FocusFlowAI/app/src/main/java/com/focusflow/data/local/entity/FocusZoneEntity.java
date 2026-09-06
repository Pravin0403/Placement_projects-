package com.focusflow.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "focus_zones")
public class FocusZoneEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private int hourOfDay;
    private int dayOfWeek;
    private int focusScore;
    private float distractionRisk;
    private float confidence;
    private String recommendation;
    private boolean isRecommended;
    private long calculatedAtEpochMs;

    public FocusZoneEntity(
            long id,
            int hourOfDay,
            int dayOfWeek,
            int focusScore,
            float distractionRisk,
            float confidence,
            String recommendation,
            boolean isRecommended,
            long calculatedAtEpochMs
    ) {
        this.id = id;
        this.hourOfDay = hourOfDay;
        this.dayOfWeek = dayOfWeek;
        this.focusScore = focusScore;
        this.distractionRisk = distractionRisk;
        this.confidence = confidence;
        this.recommendation = recommendation;
        this.isRecommended = isRecommended;
        this.calculatedAtEpochMs = calculatedAtEpochMs;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getHourOfDay() {
        return hourOfDay;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public int getFocusScore() {
        return focusScore;
    }

    public float getDistractionRisk() {
        return distractionRisk;
    }

    public float getConfidence() {
        return confidence;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public boolean isRecommended() {
        return isRecommended;
    }

    public long getCalculatedAtEpochMs() {
        return calculatedAtEpochMs;
    }
}