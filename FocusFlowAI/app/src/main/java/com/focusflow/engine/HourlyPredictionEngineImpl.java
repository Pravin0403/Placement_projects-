package com.focusflow.engine;

import com.focusflow.data.local.AppSettings;
import com.focusflow.data.local.dao.FocusSessionDao;
import com.focusflow.data.local.dao.FocusSnapshotDao;
import com.focusflow.data.local.entity.FocusSessionEntity;
import com.focusflow.data.local.entity.FocusSnapshotEntity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of HourlyPredictionEngine using historical session data.
 */
public class HourlyPredictionEngineImpl implements HourlyPredictionEngine {

    private final FocusSessionDao sessionDao;
    private final FocusSnapshotDao snapshotDao;
    private final AppSettings settings;

    public HourlyPredictionEngineImpl(
            FocusSessionDao sessionDao,
            FocusSnapshotDao snapshotDao,
            AppSettings settings
    ) {
        this.sessionDao = sessionDao;
        this.snapshotDao = snapshotDao;
        this.settings = settings;
    }

    @Override
    public List<HourlyPrediction> generateDailyPredictions() {
        List<HourlyPrediction> predictions = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        
        // Get sleep settings
        int sleepStartHour = settings.sleepStartHour();
        int sleepEndHour = settings.sleepEndHour();

        for (int hour = 0; hour < 24; hour++) {
            // Skip sleep hours
            if (isSleepHour(hour, sleepStartHour, sleepEndHour)) {
                predictions.add(new HourlyPrediction(
                        hour,
                        1.0f, // High risk during sleep (irrelevant)
                        0.0f,
                        "Sleep time - not applicable"
                ));
                continue;
            }

            HourlyPrediction prediction = calculateHourlyPrediction(hour, currentDayOfWeek);
            predictions.add(prediction);
        }

        return predictions;
    }

    @Override
    public HourlyPrediction getPredictionForHour(int hour) {
        Calendar calendar = Calendar.getInstance();
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return calculateHourlyPrediction(hour, currentDayOfWeek);
    }

    @Override
    public float predictNearTermRisk(float currentRisk, int sessionElapsedMinutes) {
        // Risk typically increases as session progresses due to fatigue
        // Simple model: risk increases by 2% every 5 minutes after 30 minutes
        if (sessionElapsedMinutes < 30) {
            return currentRisk;
        }

        int fatigueIntervals = (sessionElapsedMinutes - 30) / 5;
        float riskIncrease = fatigueIntervals * 0.02f;
        float increasedRisk = currentRisk + riskIncrease;

        return Math.min(1.0f, increasedRisk);
    }

    private HourlyPrediction calculateHourlyPrediction(int hour, int dayOfWeek) {
        // Get historical data for this hour/day
        List<FocusSessionEntity> sessions = sessionDao.recent(50);
        
        if (sessions.isEmpty()) {
            return createDefaultPrediction(hour);
        }

        // Calculate historical risk for this hour
        float historicalRisk = calculateHistoricalRisk(sessions, hour, dayOfWeek);
        float confidence = calculatePredictionConfidence(sessions, hour, dayOfWeek);
        
        String recommendation = generateRiskRecommendation(historicalRisk);

        return new HourlyPrediction(hour, historicalRisk, confidence, recommendation);
    }

    private float calculateHistoricalRisk(List<FocusSessionEntity> sessions, int hour, int dayOfWeek) {
        int totalRisk = 0;
        int count = 0;

        for (FocusSessionEntity session : sessions) {
            Calendar sessionCalendar = Calendar.getInstance();
            sessionCalendar.setTimeInMillis(session.getStartedAtEpochMs());

            int sessionHour = sessionCalendar.get(Calendar.HOUR_OF_DAY);
            int sessionDay = sessionCalendar.get(Calendar.DAY_OF_WEEK);

            if (sessionHour == hour && sessionDay == dayOfWeek) {
                // Get snapshot data for this session
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
            return getDefaultRiskForHour(hour);
        }

        return (totalRisk / count) / 100f; // Convert percentage to probability
    }

    private float calculatePredictionConfidence(List<FocusSessionEntity> sessions, int hour, int dayOfWeek) {
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

        return Math.min(1.0f, matchingSessions / 10.0f);
    }

    private String generateRiskRecommendation(float risk) {
        if (risk < 0.2f) {
            return "Low risk - excellent time for focused work";
        } else if (risk < 0.4f) {
            return "Moderate risk - good time for focused work";
        } else if (risk < 0.6f) {
            return "Elevated risk - use caution";
        } else if (risk < 0.8f) {
            return "High risk - challenging time for focus";
        } else {
            return "Very high risk - not recommended for deep work";
        }
    }

    private HourlyPrediction createDefaultPrediction(int hour) {
        float risk = getDefaultRiskForHour(hour);
        return new HourlyPrediction(
                hour,
                risk,
                0.3f, // Low confidence without historical data
                generateRiskRecommendation(risk)
        );
    }

    private float getDefaultRiskForHour(int hour) {
        // Inverse of typical productivity patterns
        if (hour >= 6 && hour <= 10) return 0.15f; // Morning peak
        if (hour >= 10 && hour <= 12) return 0.25f; // Late morning
        if (hour >= 13 && hour <= 15) return 0.55f; // Afternoon dip
        if (hour >= 15 && hour <= 17) return 0.30f; // Late afternoon recovery
        if (hour >= 17 && hour <= 20) return 0.40f; // Evening
        return 0.50f; // Other times
    }

    private boolean isSleepHour(int hour, int sleepStart, int sleepEnd) {
        if (sleepStart < sleepEnd) {
            return hour >= sleepStart || hour < sleepEnd;
        } else {
            return hour >= sleepStart || hour < sleepEnd;
        }
    }

    private List<FocusSnapshotEntity> getSnapshotsForSession(long sessionId) {
        // This would need proper DAO query
        return Collections.emptyList();
    }
}