package com.focusflow.engine;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.focusflow.core.common.Constants;
import com.focusflow.data.local.AppSettings;
import com.focusflow.data.local.dao.FocusSessionDao;
import com.focusflow.data.local.dao.FocusSnapshotDao;
import com.focusflow.data.local.dao.TelemetryEventDao;
import com.focusflow.data.local.entity.FocusSessionEntity;
import com.focusflow.data.local.entity.FocusSnapshotEntity;
import com.focusflow.data.local.entity.TelemetryEventEntity;
import com.focusflow.domain.model.FocusZone;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of FocusZoneEngine using historical session data.
 */
public class FocusZoneEngineImpl implements FocusZoneEngine {

    private final Context context;
    private final FocusSessionDao sessionDao;
    private final FocusSnapshotDao snapshotDao;
    private final TelemetryEventDao eventDao;
    private final AppSettings settings;

    public FocusZoneEngineImpl(
            Context context,
            FocusSessionDao sessionDao,
            FocusSnapshotDao snapshotDao,
            TelemetryEventDao eventDao,
            AppSettings settings
    ) {
        this.context = context.getApplicationContext();
        this.sessionDao = sessionDao;
        this.snapshotDao = snapshotDao;
        this.eventDao = eventDao;
        this.settings = settings;
    }

    @Override
    public List<FocusZone> calculateDailyFocusZones() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return calculateFocusZonesForDayOfWeek(dayOfWeek);
    }

    @Override
    public List<FocusZone> calculateFocusZonesForDayOfWeek(int dayOfWeek) {
        List<FocusZone> zones = new ArrayList<>();
        
        // Get sleep settings (default: 22:00 - 07:00)
        int sleepStartHour = settings.sleepStartHour();
        int sleepEndHour = settings.sleepEndHour();
        
        // Calculate focus scores for each hour
        for (int hour = 0; hour < 24; hour++) {
            // Skip sleep hours
            if (isSleepHour(hour, sleepStartHour, sleepEndHour)) {
                zones.add(new FocusZone(
                        hour,
                        0, // No focus score during sleep
                        1.0f, // High distraction risk (irrelevant during sleep)
                        0.0f, // No confidence
                        "Sleep time - not recommended",
                        false
                ));
                continue;
            }
            
            FocusZone zone = calculateHourlyFocusZone(hour, dayOfWeek);
            zones.add(zone);
        }
        
        return zones;
    }

    @Override
    @Nullable
    public FocusZone getBestFocusHour() {
        List<FocusZone> zones = calculateDailyFocusZones();
        return zones.stream()
                .filter(zone -> zone.focusScore() > 0)
                .max(Comparator.comparingInt(FocusZone::focusScore))
                .orElse(null);
    }

    @Override
    public List<FocusZone> getRecommendedFocusHours(int limit) {
        List<FocusZone> zones = calculateDailyFocusZones();
        return zones.stream()
                .filter(zone -> zone.focusScore() > 50) // Only recommend hours with decent focus score
                .sorted(Comparator.comparingInt(FocusZone::focusScore).reversed())
                .limit(limit)
                .toList();
    }

    private FocusZone calculateHourlyFocusZone(int hour, int dayOfWeek) {
        // Get historical data for this hour and day of week
        List<FocusSessionEntity> sessions = sessionDao.recent(50);
        
        if (sessions.isEmpty()) {
            // Default to moderate scores if no historical data
            return createDefaultFocusZone(hour, dayOfWeek);
        }
        
        // Calculate focus score based on historical performance
        int focusScore = calculateHistoricalFocusScore(sessions, hour, dayOfWeek);
        float distractionRisk = calculateDistractionRisk(sessions, hour, dayOfWeek);
        float confidence = calculateConfidence(sessions, hour, dayOfWeek);
        
        String recommendation = generateRecommendation(focusScore, distractionRisk);
        boolean isRecommended = focusScore >= 70;
        
        return new FocusZone(
                hour,
                focusScore,
                distractionRisk,
                confidence,
                recommendation,
                isRecommended
        );
    }

    private int calculateHistoricalFocusScore(List<FocusSessionEntity> sessions, int hour, int dayOfWeek) {
        int totalScore = 0;
        int count = 0;
        
        for (FocusSessionEntity session : sessions) {
            Calendar sessionCalendar = Calendar.getInstance();
            sessionCalendar.setTimeInMillis(session.getStartedAtEpochMs());
            
            int sessionHour = sessionCalendar.get(Calendar.HOUR_OF_DAY);
            int sessionDay = sessionCalendar.get(Calendar.DAY_OF_WEEK);
            
            // Check if session matches the target hour/day
            if (sessionHour == hour && sessionDay == dayOfWeek) {
                // Get focus score from snapshots
                List<FocusSnapshotEntity> snapshots = getSnapshotsForSession(session.getId());
                if (!snapshots.isEmpty()) {
                    int avgScore = (int) snapshots.stream()
                            .mapToInt(FocusSnapshotEntity::getFocusScore)
                            .average()
                            .orElse(50);
                    totalScore += avgScore;
                    count++;
                }
            }
        }
        
        if (count == 0) {
            // Use time-of-day heuristic if no matching historical data
            return getDefaultTimeBasedScore(hour);
        }
        
        return totalScore / count;
    }

    private float calculateDistractionRisk(List<FocusSessionEntity> sessions, int hour, int dayOfWeek) {
        int totalRisk = 0;
        int count = 0;
        
        for (FocusSessionEntity session : sessions) {
            Calendar sessionCalendar = Calendar.getInstance();
            sessionCalendar.setTimeInMillis(session.getStartedAtEpochMs());
            
            int sessionHour = sessionCalendar.get(Calendar.HOUR_OF_DAY);
            int sessionDay = sessionCalendar.get(Calendar.DAY_OF_WEEK);
            
            if (sessionHour == hour && sessionDay == dayOfWeek) {
                List<FocusSnapshotEntity> snapshots = getSnapshotsForSession(session.getId());
                if (!snapshots.isEmpty()) {
                    float avgRisk = (float) snapshots.stream()
                            .mapToInt(FocusSnapshotEntity::getDistractionRiskPercent)
                            .average()
                            .orElse(50);
                    totalRisk += avgRisk;
                    count++;
                }
            }
        }
        
        if (count == 0) {
            return getDefaultTimeBasedRisk(hour);
        }
        
        return totalRisk / (float) count;
    }

    private float calculateConfidence(List<FocusSessionEntity> sessions, int hour, int dayOfWeek) {
        int matchingSessions = 0;
        
        for (FocusSessionEntity session : sessions) {
            Calendar sessionCalendar = Calendar.getInstance();
            sessionCalendar.setTimeInMillis(session.getStartedAtEpochMs());
            
            int sessionHour = sessionCalendar.get(Calendar.HOUR_OF_DAY);
            int sessionDay = sessionCalendar.get(Calendar.DAY_OF_WEEK);
            
            if (sessionHour == hour && sessionDay == dayOfWeek) {
                matchingSessions++;
            }
        }
        
        // More matching sessions = higher confidence
        return Math.min(1.0f, matchingSessions / 10.0f);
    }

    private String generateRecommendation(int focusScore, float distractionRisk) {
        if (focusScore >= 85) {
            return "Excellent focus time - highly recommended";
        } else if (focusScore >= 70) {
            return "Good focus time - recommended";
        } else if (focusScore >= 50) {
            return "Moderate focus time - consider with breaks";
        } else {
            return "Challenging time - not recommended for deep work";
        }
    }

    private FocusZone createDefaultFocusZone(int hour, int dayOfWeek) {
        int focusScore = getDefaultTimeBasedScore(hour);
        float distractionRisk = getDefaultTimeBasedRisk(hour);
        
        return new FocusZone(
                hour,
                focusScore,
                distractionRisk,
                0.3f, // Low confidence without historical data
                generateRecommendation(focusScore, distractionRisk),
                focusScore >= 60
        );
    }

    private int getDefaultTimeBasedScore(int hour) {
        // Typical productivity patterns (morning = higher scores)
        if (hour >= 6 && hour <= 10) return 85; // Morning peak
        if (hour >= 10 && hour <= 12) return 75; // Late morning
        if (hour >= 13 && hour <= 15) return 45; // Afternoon dip
        if (hour >= 15 && hour <= 17) return 70; // Late afternoon recovery
        if (hour >= 17 && hour <= 20) return 60; // Evening
        return 50; // Other times
    }

    private float getDefaultTimeBasedRisk(int hour) {
        // Inverse of focus score
        int focusScore = getDefaultTimeBasedScore(hour);
        return (100 - focusScore) / 100.0f;
    }

    private boolean isSleepHour(int hour, int sleepStart, int sleepEnd) {
        if (sleepStart < sleepEnd) {
            // Sleep doesn't cross midnight (e.g., 22:00 - 07:00)
            return hour >= sleepStart || hour < sleepEnd;
        } else {
            // Sleep crosses midnight (e.g., 23:00 - 07:00)
            return hour >= sleepStart || hour < sleepEnd;
        }
    }

    private List<FocusSnapshotEntity> getSnapshotsForSession(long sessionId) {
        // This would ideally query the database, but for now we'll return empty
        // In a full implementation, this would use snapshotDao.observeBySession(sessionId)
        return Collections.emptyList();
    }
}