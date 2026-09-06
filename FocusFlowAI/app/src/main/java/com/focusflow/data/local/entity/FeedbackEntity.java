package com.focusflow.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "feedback")
public class FeedbackEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String predictionId;
    private boolean helpful;
    @Nullable
    private String actionTaken;
    private long createdAtEpochMs;
    private boolean synced;

    public FeedbackEntity(
            long id,
            @NonNull String predictionId,
            boolean helpful,
            @Nullable String actionTaken,
            long createdAtEpochMs,
            boolean synced
    ) {
        this.id = id;
        this.predictionId = predictionId;
        this.helpful = helpful;
        this.actionTaken = actionTaken;
        this.createdAtEpochMs = createdAtEpochMs;
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

    public boolean isHelpful() {
        return helpful;
    }

    @Nullable
    public String getActionTaken() {
        return actionTaken;
    }

    public long getCreatedAtEpochMs() {
        return createdAtEpochMs;
    }

    public boolean isSynced() {
        return synced;
    }
}
