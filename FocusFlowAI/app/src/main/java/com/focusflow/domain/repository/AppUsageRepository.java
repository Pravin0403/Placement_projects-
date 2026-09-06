package com.focusflow.domain.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.focusflow.domain.model.AppUsageStats;

import java.util.List;

/**
 * Repository interface for app usage analytics and statistics.
 */
public interface AppUsageRepository {

    /**
     * Get aggregated app usage statistics for the current day.
     * @return List of app usage statistics sorted by total usage duration (descending)
     */
    @NonNull
    List<AppUsageStats> getDailyUsageStats();

    /**
     * Get aggregated app usage statistics for a specific date range.
     * @param startTimeMs Start time in milliseconds since epoch
     * @param endTimeMs End time in milliseconds since epoch
     * @return List of app usage statistics sorted by total usage duration (descending)
     */
    @NonNull
    List<AppUsageStats> getUsageStatsForRange(long startTimeMs, long endTimeMs);

    /**
     * Get hourly usage statistics for a specific app.
     * @param packageName The package name of the app
     * @return Array of 24 hourly usage durations in milliseconds
     */
    @NonNull
    long[] getHourlyUsageStats(@NonNull String packageName);

    /**
     * Get usage statistics specifically during focus sessions.
     * @return List of app usage statistics during focus sessions
     */
    @NonNull
    List<AppUsageStats> getFocusSessionUsageStats();

    /**
     * Get the most distracting apps based on usage patterns during focus sessions.
     * @param limit Maximum number of apps to return
     * @return List of most distracting apps
     */
    @NonNull
    List<AppUsageStats> getMostDistractingApps(int limit);

    /**
     * Check if the app has permission to access usage statistics.
     * @return true if permission is granted, false otherwise
     */
    boolean hasUsageStatsPermission();

    /**
     * Get total phone usage time for the current day.
     * @return Total usage time in milliseconds
     */
    long getTotalDailyUsageMs();
}