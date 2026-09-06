package com.focusflow.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(
        tableName = "telemetry_events",
        indices = {@Index("sessionId"), @Index("eventId"), @Index("timestampEpochMs")}
)
public class TelemetryEventEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String eventId;

    private long sessionId;

    @NonNull
    private String type;

    @Nullable
    private String packageName;

    private long timestampEpochMs;

    private boolean synced;

    public TelemetryEventEntity(
            long id,
            @NonNull String eventId,
            long sessionId,
            @NonNull String type,
            @Nullable String packageName,
            long timestampEpochMs,
            boolean synced
    ) {
        this.id = id;
        this.eventId = eventId;
        this.sessionId = sessionId;
        this.type = type;
        this.packageName = packageName;
        this.timestampEpochMs = timestampEpochMs;
        this.synced = synced;
    }

    @NonNull
    public static TelemetryEventEntity create(
            long sessionId,
            @NonNull String type,
            @Nullable String packageName
    ) {
        return new TelemetryEventEntity(
                0,
                UUID.randomUUID().toString(),
                sessionId,
                type,
                packageName,
                System.currentTimeMillis(),
                false
        );
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getEventId() {
        return eventId;
    }

    public long getSessionId() {
        return sessionId;
    }

    @NonNull
    public String getType() {
        return type;
    }

    @Nullable
    public String getPackageName() {
        return packageName;
    }

    public long getTimestampEpochMs() {
        return timestampEpochMs;
    }

    public boolean isSynced() {
        return synced;
    }
}
