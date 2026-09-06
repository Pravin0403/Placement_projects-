package com.focusflow.engine;

import com.focusflow.domain.model.FocusZone;

import java.util.List;

/**
 * Engine for calculating hourly focus zones and scores.
 */
public interface FocusZoneEngine {

    /**
     * Calculate focus zones for the current day.
     * @return List of 24 hourly focus zones
     */
    List<FocusZone> calculateDailyFocusZones();

    /**
     * Calculate focus zones for a specific day of week.
     * @param dayOfWeek Day of week (1-7, where 1 is Sunday)
     * @return List of 24 hourly focus zones
     */
    List<FocusZone> calculateFocusZonesForDayOfWeek(int dayOfWeek);

    /**
     * Get the best recommended focus hour for the current day.
     * @return Best focus zone, or null if no data available
     */
    FocusZone getBestFocusHour();

    /**
     * Get ranked recommended focus hours for the current day.
     * @param limit Maximum number of hours to return
     * @return List of recommended focus zones, sorted by focus score (descending)
     */
    List<FocusZone> getRecommendedFocusHours(int limit);
}