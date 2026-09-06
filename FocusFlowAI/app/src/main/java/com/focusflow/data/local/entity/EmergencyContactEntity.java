package com.focusflow.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "emergency_contacts")
public class EmergencyContactEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String name;
    @NonNull
    private String contactType;
    @NonNull
    private String contactValue;
    private boolean isPriority;
    private long createdAtEpochMs;

    public EmergencyContactEntity(
            long id,
            @NonNull String name,
            @NonNull String contactType,
            @NonNull String contactValue,
            boolean isPriority,
            long createdAtEpochMs
    ) {
        this.id = id;
        this.name = name;
        this.contactType = contactType;
        this.contactValue = contactValue;
        this.isPriority = isPriority;
        this.createdAtEpochMs = createdAtEpochMs;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getContactType() {
        return contactType;
    }

    @NonNull
    public String getContactValue() {
        return contactValue;
    }

    public boolean isPriority() {
        return isPriority;
    }

    public long getCreatedAtEpochMs() {
        return createdAtEpochMs;
    }
}