package com.focusflow.domain.repository;

import androidx.annotation.NonNull;

import com.focusflow.domain.model.SessionStatistics;

import java.util.List;

/**
 * Repository interface for session statistics and analytics.
 */
public interface SessionStatisticsRepository {

    /**
     * Get statistics for a specific session.
     * @param sessionId The session ID
     * @return Session statistics, or null if not found
     */
    SessionStatistics getSessionStatistics(long sessionId);

    /**
     * Get statistics for recent sessions.
     * @param limit Maximum number of sessions to return
     * @return List of session statistics, most recent first
     */
    @NonNull
    List<SessionStatistics> getRecentSessionStatistics(int limit);

    /**
     * Get aggregated daily statistics.
     * @param dateMs Date in milliseconds since epoch
     * @return Aggregated statistics for the day
     */
    SessionStatistics getDailyStatistics(long dateMs);

    /**
     * Get aggregated weekly statistics.
     * @return List of daily statistics for the past week
     */
    @NonNull
    List<SessionStatistics> getWeeklyStatistics();

    /**
     * Calculate session completion statistics.
     * @return Average completion percentage across all sessions
     */
    float getAverageCompletionPercentage();

    /**
     * Get most distracting apps across all sessions.
     * @param limit Maximum number of apps to return
     * @return List of package names
     */
    @NonNull
    List<String> getMostDistractingApps(int limit);
}