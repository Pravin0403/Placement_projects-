package com.focusflow.engine;

import com.focusflow.domain.model.FocusZone;

import java.util.List;

/**
 * Engine for recommending optimal one-hour focus sessions.
 */
public interface SessionRecommendationEngine {

    /**
     * Get the best recommended one-hour focus session for today.
     * @return Best focus zone for a one-hour session, or null if no data available
     */
    FocusZone getBestOneHourSession();

    /**
     * Get ranked list of recommended one-hour sessions for today.
     * @param limit Maximum number of sessions to return
     * @return List of recommended focus zones, sorted by focus score (descending)
     */
    List<FocusZone> getRankedOneHourSessions(int limit);

    /**
     * Get explanation for why a specific hour is recommended.
     * @param hour Hour of day
     * @return Explanation text
     */
    String getRecommendationExplanation(int hour);
}