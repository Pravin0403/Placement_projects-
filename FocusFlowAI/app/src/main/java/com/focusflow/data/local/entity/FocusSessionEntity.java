package com.focusflow.data.local.entity;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "focus_sessions")
public class FocusSessionEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long startedAtEpochMs;

    @Nullable
    private Long endedAtEpochMs;

    public FocusSessionEntity(long id, long startedAtEpochMs, @Nullable Long endedAtEpochMs) {
        this.id = id;
        this.startedAtEpochMs = startedAtEpochMs;
        this.endedAtEpochMs = endedAtEpochMs;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getStartedAtEpochMs() {
        return startedAtEpochMs;
    }

    @Nullable
    public Long getEndedAtEpochMs() {
        return endedAtEpochMs;
    }

    public void setEndedAtEpochMs(@Nullable Long endedAtEpochMs) {
        this.endedAtEpochMs = endedAtEpochMs;
    }
}
