package com.focusflow.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * Represents a focus session violation.
 */
public class Violation {

    private final long id;
    private final long sessionId;
    @NonNull
    private final String violationType;
    @NonNull
    private final String description;
    private final long timestampEpochMs;
    @Nullable
    private final String packageName;
    private final boolean wasBlocked;

    public Violation(
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

    public long id() {
        return id;
    }

    public long sessionId() {
        return sessionId;
    }

    @NonNull
    public String violationType() {
        return violationType;
    }

    @NonNull
    public String description() {
        return description;
    }

    public long timestampEpochMs() {
        return timestampEpochMs;
    }

    @Nullable
    public String packageName() {
        return packageName;
    }

    public boolean wasBlocked() {
        return wasBlocked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Violation violation = (Violation) o;
        return id == violation.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}