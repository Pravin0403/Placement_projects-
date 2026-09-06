package com.focusflow.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "blocked_apps")
public class BlockedAppEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String packageName;
    private String appName;
    private String distractionLevel; // "always", "usually", "neutral", "productive", "allowed"
    private long addedAtEpochMs;

    public BlockedAppEntity(
            long id,
            @NonNull String packageName,
            String appName,
            String distractionLevel,
            long addedAtEpochMs
    ) {
        this.id = id;
        this.packageName = packageName;
        this.appName = appName;
        this.distractionLevel = distractionLevel;
        this.addedAtEpochMs = addedAtEpochMs;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getPackageName() {
        return packageName;
    }

    public String getAppName() {
        return appName;
    }

    public String getDistractionLevel() {
        return distractionLevel;
    }

    public long getAddedAtEpochMs() {
        return addedAtEpochMs;
    }
}