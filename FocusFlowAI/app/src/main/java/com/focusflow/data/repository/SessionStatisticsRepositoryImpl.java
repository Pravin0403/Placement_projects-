package com.focusflow.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.focusflow.data.local.dao.FocusSessionDao;
import com.focusflow.data.local.dao.FocusSnapshotDao;
import com.focusflow.data.local.dao.PredictionDao;
import com.focusflow.data.local.dao.TelemetryEventDao;
import com.focusflow.data.local.dao.ViolationDao;
import com.focusflow.data.local.entity.FocusSessionEntity;
import com.focusflow.data.local.entity.FocusSnapshotEntity;
import com.focusflow.data.local.entity.PredictionEntity;
import com.focusflow.data.local.entity.TelemetryEventEntity;
import com.focusflow.data.local.entity.ViolationEntity;
import com.focusflow.domain.model.SessionStatistics;
import com.focusflow.domain.repository.SessionStatisticsRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of SessionStatisticsRepository.
 */
public class SessionStatisticsRepositoryImpl implements SessionStatisticsRepository {

    private static final long PLANNED_SESSION_DURATION_MS = 60 * 60 * 1000L; // 1 hour

    private final FocusSessionDao sessionDao;
    private final FocusSnapshotDao snapshotDao;
    private final PredictionDao predictionDao;
    private final TelemetryEventDao eventDao;
    private final ViolationDao violationDao;

    public SessionStatisticsRepositoryImpl(
            FocusSessionDao sessionDao,
            FocusSnapshotDao snapshotDao,
            PredictionDao predictionDao,
            TelemetryEventDao eventDao,
            ViolationDao violationDao
    ) {
        this.sessionDao = sessionDao;
        this.snapshotDao = snapshotDao;
        this.predictionDao = predictionDao;
        this.eventDao = eventDao;
        this.violationDao = violationDao;
    }

    @Override
    @Nullable
    public SessionStatistics getSessionStatistics(long sessionId) {
        FocusSessionEntity session = sessionDao.recent(1).stream()
                .filter(s -> s.getId() == sessionId)
                .findFirst()
                .orElse(null);

        if (session == null || session.getEndedAtEpochMs() == null) {
            return null;
        }

        return calculateSessionStatistics(session);
    }

    @Override
    @NonNull
    public List<SessionStatistics> getRecentSessionStatistics(int limit) {
        List<FocusSessionEntity> sessions = sessionDao.recent(limit);
        List<SessionStatistics> statistics = new ArrayList<>();

        for (FocusSessionEntity session : sessions) {
            if (session.getEndedAtEpochMs() != null) {
                SessionStatistics stats = calculateSessionStatistics(session);
                if (stats != null) {
                    statistics.add(stats);
                }
            }
        }

        return statistics;
    }

    @Override
    public SessionStatistics getDailyStatistics(long dateMs) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateMs);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        long endOfDay = calendar.getTimeInMillis();

        // This would need a proper query in the DAO
        // For now, return a simplified version
        List<FocusSessionEntity> sessions = sessionDao.recent(100);
        List<FocusSessionEntity> daySessions = new ArrayList<>();

        for (FocusSessionEntity session : sessions) {
            if (session.getStartedAtEpochMs() >= startOfDay && 
                session.getStartedAtEpochMs() < endOfDay &&
                session.getEndedAtEpochMs() != null) {
                daySessions.add(session);
            }
        }

        if (daySessions.isEmpty()) {
            return null;
        }

        return aggregateSessions(daySessions);
    }

    @Override
    @NonNull
    public List<SessionStatistics> getWeeklyStatistics() {
        List<SessionStatistics> weeklyStats = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < 7; i++) {
            long dateMs = calendar.getTimeInMillis();
            SessionStatistics dayStats = getDailyStatistics(dateMs);
            if (dayStats != null) {
                weeklyStats.add(dayStats);
            }
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }

        return weeklyStats;
    }

    @Override
    public float getAverageCompletionPercentage() {
        List<SessionStatistics> recentStats = getRecentSessionStatistics(50);
        if (recentStats.isEmpty()) {
            return 0f;
        }

        float totalCompletion = 0f;
        for (SessionStatistics stats : recentStats) {
            totalCompletion += stats.completionPercentage();
        }

        return totalCompletion / recentStats.size();
    }

    @Override
    @NonNull
    public List<String> getMostDistractingApps(int limit) {
        Map<String, Integer> appViolationCount = new HashMap<>();
        List<ViolationEntity> violations = Collections.emptyList(); // Would need proper DAO query

        // Count violations by app
        for (ViolationEntity violation : violations) {
            String packageName = violation.getPackageName();
            if (packageName != null) {
                appViolationCount.put(packageName, appViolationCount.getOrDefault(packageName, 0) + 1);
            }
        }

        // Sort by violation count
        List<String> sortedApps = new ArrayList<>(appViolationCount.keySet());
        sortedApps.sort((a, b) -> appViolationCount.get(b) - appViolationCount.get(a));

        return sortedApps.subList(0, Math.min(limit, sortedApps.size()));
    }

    private SessionStatistics calculateSessionStatistics(FocusSessionEntity session) {
        long actualDuration = session.getEndedAtEpochMs() - session.getStartedAtEpochMs();
        float completionPercentage = (actualDuration * 100f) / PLANNED_SESSION_DURATION_MS;

        // Get session snapshots for risk calculation
        List<FocusSnapshotEntity> snapshots = Collections.emptyList(); // Would need proper DAO query
        float averageRisk = 0f;
        float peakRisk = 0f;
        int focusScore = 0;

        if (!snapshots.isEmpty()) {
            averageRisk = (float) snapshots.stream()
                    .mapToInt(FocusSnapshotEntity::getDistractionRiskPercent)
                    .average()
                    .orElse(0);
            peakRisk = snapshots.stream()
                    .mapToInt(FocusSnapshotEntity::getDistractionRiskPercent)
                    .max()
                    .orElse(0);
            focusScore = (int) snapshots.stream()
                    .mapToInt(FocusSnapshotEntity::getFocusScore)
                    .average()
                    .orElse(50);
        }

        // Count events
        int notificationCount = 0; // Would need proper query
        int distractionAttempts = 0; // Would need proper query
        int blockedAppAttempts = 0; // Would need proper query
        int interruptions = 0; // Would need proper query

        // Get distracting apps
        List<String> distractingApps = new ArrayList<>(); // Would need proper query

        return new SessionStatistics(
                session.getId(),
                session.getStartedAtEpochMs(),
                session.getEndedAtEpochMs(),
                PLANNED_SESSION_DURATION_MS,
                actualDuration,
                completionPercentage,
                distractionAttempts,
                blockedAppAttempts,
                notificationCount,
                averageRisk,
                peakRisk,
                interruptions,
                focusScore,
                distractingApps
        );
    }

    private SessionStatistics aggregateSessions(List<FocusSessionEntity> sessions) {
        if (sessions.isEmpty()) {
            return null;
        }

        long totalActualDuration = 0;
        int totalDistractionAttempts = 0;
        int totalBlockedAttempts = 0;
        int totalNotifications = 0;
        int totalInterruptions = 0;
        float totalRisk = 0f;
        float totalPeakRisk = 0f;
        int totalFocusScore = 0;

        for (FocusSessionEntity session : sessions) {
            if (session.getEndedAtEpochMs() != null) {
                totalActualDuration += (session.getEndedAtEpochMs() - session.getStartedAtEpochMs());
            }
        }

        long plannedDuration = sessions.size() * PLANNED_SESSION_DURATION_MS;
        float completionPercentage = (totalActualDuration * 100f) / plannedDuration;

        return new SessionStatistics(
                0, // Aggregated session
                sessions.get(0).getStartedAtEpochMs(),
                sessions.get(sessions.size() - 1).getEndedAtEpochMs(),
                plannedDuration,
                totalActualDuration,
                completionPercentage,
                totalDistractionAttempts,
                totalBlockedAttempts,
                totalNotifications,
                totalRisk / sessions.size(),
                totalPeakRisk,
                totalInterruptions,
                totalFocusScore / sessions.size(),
                Collections.emptyList()
        );
    }
}