package com.focusflow.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "predictions")
public class PredictionEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String predictionId;

    private long sessionId;
    private float riskScore;
    private int focusScore;
    @NonNull
    private String modelVersion;
    private long inferenceTimeMs;
    private long createdAtEpochMs;
    @Nullable
    private String topSignalsCsv;
    @Nullable
    private String recommendation;
    private boolean synced;

    public PredictionEntity(
            long id,
            @NonNull String predictionId,
            long sessionId,
            float riskScore,
            int focusScore,
            @NonNull String modelVersion,
            long inferenceTimeMs,
            long createdAtEpochMs,
            @Nullable String topSignalsCsv,
            @Nullable String recommendation,
            boolean synced
    ) {
        this.id = id;
        this.predictionId = predictionId;
        this.sessionId = sessionId;
        this.riskScore = riskScore;
        this.focusScore = focusScore;
        this.modelVersion = modelVersion;
        this.inferenceTimeMs = inferenceTimeMs;
        this.createdAtEpochMs = createdAtEpochMs;
        this.topSignalsCsv = topSignalsCsv;
        this.recommendation = recommendation;
        this.synced = synced;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getPredictionId() {
        return predictionId;
    }

    public long getSessionId() {
        return sessionId;
    }

    public float getRiskScore() {
        return riskScore;
    }

    public int getFocusScore() {
        return focusScore;
    }

    @NonNull
    public String getModelVersion() {
        return modelVersion;
    }

    public long getInferenceTimeMs() {
        return inferenceTimeMs;
    }

    public long getCreatedAtEpochMs() {
        return createdAtEpochMs;
    }

    @Nullable
    public String getTopSignalsCsv() {
        return topSignalsCsv;
    }

    @Nullable
    public String getRecommendation() {
        return recommendation;
    }

    public boolean isSynced() {
        return synced;
    }
}
