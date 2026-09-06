package com.focusflow.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * Represents aggregated usage statistics for a specific application.
 */
public class AppUsageStats {

    private final String packageName;
    private final String appName;
    private final long totalUsageDurationMs;
    private final int launchCount;
    private final int sessionCount;
    private final long averageSessionDurationMs;
    private final long firstOpenedTimeMs;
    private final long lastOpenedTimeMs;
    private final float percentageOfTotalUsage;
    private final long usageDuringFocusSessionsMs;
    private final int interruptionCount;

    public AppUsageStats(
            @NonNull String packageName,
            @Nullable String appName,
            long totalUsageDurationMs,
            int launchCount,
            int sessionCount,
            long averageSessionDurationMs,
            long firstOpenedTimeMs,
            long lastOpenedTimeMs,
            float percentageOfTotalUsage,
            long usageDuringFocusSessionsMs,
            int interruptionCount
    ) {
        this.packageName = packageName;
        this.appName = appName;
        this.totalUsageDurationMs = totalUsageDurationMs;
        this.launchCount = launchCount;
        this.sessionCount = sessionCount;
        this.averageSessionDurationMs = averageSessionDurationMs;
        this.firstOpenedTimeMs = firstOpenedTimeMs;
        this.lastOpenedTimeMs = lastOpenedTimeMs;
        this.percentageOfTotalUsage = percentageOfTotalUsage;
        this.usageDuringFocusSessionsMs = usageDuringFocusSessionsMs;
        this.interruptionCount = interruptionCount;
    }

    @NonNull
    public String packageName() {
        return packageName;
    }

    @Nullable
    public String appName() {
        return appName;
    }

    public long totalUsageDurationMs() {
        return totalUsageDurationMs;
    }

    public int launchCount() {
        return launchCount;
    }

    public int sessionCount() {
        return sessionCount;
    }

    public long averageSessionDurationMs() {
        return averageSessionDurationMs;
    }

    public long firstOpenedTimeMs() {
        return firstOpenedTimeMs;
    }

    public long lastOpenedTimeMs() {
        return lastOpenedTimeMs;
    }

    public float percentageOfTotalUsage() {
        return percentageOfTotalUsage;
    }

    public long usageDuringFocusSessionsMs() {
        return usageDuringFocusSessionsMs;
    }

    public int interruptionCount() {
        return interruptionCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppUsageStats that = (AppUsageStats) o;
        return Objects.equals(packageName, that.packageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageName);
    }
}