package com.focusflow.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Represents detailed statistics for a completed focus session.
 */
public class SessionStatistics {

    private final long sessionId;
    private final long startedAtEpochMs;
    private final long endedAtEpochMs;
    private final long plannedDurationMs;
    private final long actualDurationMs;
    private final float completionPercentage;
    private final int distractionAttempts;
    private final int blockedAppAttempts;
    private final int notificationCount;
    private final float averageRisk;
    private final float peakRisk;
    private final int interruptions;
    private final int focusScore;
    private final List<String> distractingApps;

    public SessionStatistics(
            long sessionId,
            long startedAtEpochMs,
            long endedAtEpochMs,
            long plannedDurationMs,
            long actualDurationMs,
            float completionPercentage,
            int distractionAttempts,
            int blockedAppAttempts,
            int notificationCount,
            float averageRisk,
            float peakRisk,
            int interruptions,
            int focusScore,
            @Nullable List<String> distractingApps
    ) {
        this.sessionId = sessionId;
        this.startedAtEpochMs = startedAtEpochMs;
        this.endedAtEpochMs = endedAtEpochMs;
        this.plannedDurationMs = plannedDurationMs;
        this.actualDurationMs = actualDurationMs;
        this.completionPercentage = completionPercentage;
        this.distractionAttempts = distractionAttempts;
        this.blockedAppAttempts = blockedAppAttempts;
        this.notificationCount = notificationCount;
        this.averageRisk = averageRisk;
        this.peakRisk = peakRisk;
        this.interruptions = interruptions;
        this.focusScore = focusScore;
        this.distractingApps = distractingApps;
    }

    public long sessionId() {
        return sessionId;
    }

    public long startedAtEpochMs() {
        return startedAtEpochMs;
    }

    public long endedAtEpochMs() {
        return endedAtEpochMs;
    }

    public long plannedDurationMs() {
        return plannedDurationMs;
    }

    public long actualDurationMs() {
        return actualDurationMs;
    }

    public float completionPercentage() {
        return completionPercentage;
    }

    public int distractionAttempts() {
        return distractionAttempts;
    }

    public int blockedAppAttempts() {
        return blockedAppAttempts;
    }

    public int notificationCount() {
        return notificationCount;
    }

    public float averageRisk() {
        return averageRisk;
    }

    public float peakRisk() {
        return peakRisk;
    }

    public int interruptions() {
        return interruptions;
    }

    public int focusScore() {
        return focusScore;
    }

    @Nullable
    public List<String> distractingApps() {
        return distractingApps;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionStatistics that = (SessionStatistics) o;
        return sessionId == that.sessionId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }
}