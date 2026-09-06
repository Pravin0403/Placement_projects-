package com.focusflow.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "violations")
public class ViolationEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long sessionId;
    @NonNull
    private String violationType;
    @NonNull
    private String description;
    private long timestampEpochMs;
    @Nullable
    private String packageName;
    private boolean wasBlocked;

    public ViolationEntity(
            long id,
            long sessionId,
            @NonNull String violationType,
            @NonNull String description,
            long timestampEpochMs,
            @Nullable String packageName,
            boolean wasBlocked
    ) {
        this.id = id;
        this.sessionId = sessionId;
        this.violationType = violationType;
        this.description = description;
        this.timestampEpochMs = timestampEpochMs;
        this.packageName = packageName;
        this.wasBlocked = wasBlocked;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSessionId() {
        return sessionId;
    }

    @NonNull
    public String getViolationType() {
        return violationType;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    public long getTimestampEpochMs() {
        return timestampEpochMs;
    }

    @Nullable
    public String getPackageName() {
        return packageName;
    }

    public boolean isWasBlocked() {
        return wasBlocked;
    }
}