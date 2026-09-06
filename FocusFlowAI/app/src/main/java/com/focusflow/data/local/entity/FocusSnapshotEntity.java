package com.focusflow.data.local.entity;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "focus_snapshots")
public class FocusSnapshotEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @Nullable
    private Integer focusScore;

    @Nullable
    private Integer distractionRiskPercent;

    @Nullable
    private Long sessionStartedAtEpochMs;

    @Nullable
    private Long sessionEndedAtEpochMs;

    @Nullable
    private String topSignalsCsv;

    @Nullable
    private String recommendation;

    private long createdAtEpochMs;

    public FocusSnapshotEntity(
            long id,
            @Nullable Integer focusScore,
            @Nullable Integer distractionRiskPercent,
            @Nullable Long sessionStartedAtEpochMs,
            @Nullable Long sessionEndedAtEpochMs,
            @Nullable String topSignalsCsv,
            @Nullable String recommendation,
            long createdAtEpochMs
    ) {
        this.id = id;
        this.focusScore = focusScore;
        this.distractionRiskPercent = distractionRiskPercent;
        this.sessionStartedAtEpochMs = sessionStartedAtEpochMs;
        this.sessionEndedAtEpochMs = sessionEndedAtEpochMs;
        this.topSignalsCsv = topSignalsCsv;
        this.recommendation = recommendation;
        this.createdAtEpochMs = createdAtEpochMs;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Nullable
    public Integer getFocusScore() {
        return focusScore;
    }

    @Nullable
    public Integer getDistractionRiskPercent() {
        return distractionRiskPercent;
    }

    @Nullable
    public Long getSessionStartedAtEpochMs() {
        return sessionStartedAtEpochMs;
    }

    @Nullable
    public Long getSessionEndedAtEpochMs() {
        return sessionEndedAtEpochMs;
    }

    @Nullable
    public String getTopSignalsCsv() {
        return topSignalsCsv;
    }

    @Nullable
    public String getRecommendation() {
        return recommendation;
    }

    public long getCreatedAtEpochMs() {
        return createdAtEpochMs;
    }
}
