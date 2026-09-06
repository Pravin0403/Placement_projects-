package com.focusflow.data.repository;

import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.focusflow.core.common.AppExecutors;
import com.focusflow.data.local.dao.FocusSessionDao;
import com.focusflow.data.local.dao.TelemetryEventDao;
import com.focusflow.data.local.entity.FocusSessionEntity;
import com.focusflow.data.local.entity.TelemetryEventEntity;
import com.focusflow.domain.model.AppUsageStats;
import com.focusflow.domain.repository.AppUsageRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of AppUsageRepository using Android's UsageStatsManager.
 */
public class AppUsageRepositoryImpl implements AppUsageRepository {

    private final Context context;
    private final AppExecutors executors;
    private final FocusSessionDao focusSessionDao;
    private final TelemetryEventDao telemetryEventDao;
    private final PackageManager packageManager;
    private final UsageStatsManager usageStatsManager;
    private final AppOpsManager appOpsManager;

    public AppUsageRepositoryImpl(
            Context context,
            AppExecutors executors,
            FocusSessionDao focusSessionDao,
            TelemetryEventDao telemetryEventDao
    ) {
        this.context = context.getApplicationContext();
        this.executors = executors;
        this.focusSessionDao = focusSessionDao;
        this.telemetryEventDao = telemetryEventDao;
        this.packageManager = context.getPackageManager();
        this.usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        this.appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
    }

    @Override
    @NonNull
    public List<AppUsageStats> getDailyUsageStats() {
        return getUsageStatsForRange(getTodayStart(), System.currentTimeMillis());
    }

    @Override
    @NonNull
    public List<AppUsageStats> getUsageStatsForRange(long startTimeMs, long endTimeMs) {
        if (!hasUsageStatsPermission()) {
            return Collections.emptyList();
        }

        if (usageStatsManager == null) {
            return Collections.emptyList();
        }

        try {
            Map<String, AppUsageData> usageMap = new HashMap<>();
            List<android.app.usage.UsageStats> stats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    startTimeMs,
                    endTimeMs
            );

            long totalUsage = 0;
            for (android.app.usage.UsageStats stat : stats) {
                if (stat.getTotalTimeInForeground() > 0) {
                    String packageName = stat.getPackageName();
                    usageMap.put(packageName, new AppUsageData(packageName, stat.getTotalTimeInForeground()));
                    totalUsage += stat.getTotalTimeInForeground();
                }
            }

            // Convert to domain model
            List<AppUsageStats> result = new ArrayList<>();
            for (AppUsageData data : usageMap.values()) {
                String appName = getAppName(data.packageName);
                float percentage = totalUsage > 0 ? (data.totalUsageMs * 100f / totalUsage) : 0f;
                
                result.add(new AppUsageStats(
                        data.packageName,
                        appName,
                        data.totalUsageMs,
                        0, // launchCount - would need event analysis
                        0, // sessionCount - would need event analysis
                        data.totalUsageMs, // averageSessionDurationMs - simplified
                        startTimeMs, // firstOpenedTimeMs - simplified
                        endTimeMs, // lastOpenedTimeMs - simplified
                        percentage,
                        0, // usageDuringFocusSessionsMs - to be calculated
                        0  // interruptionCount - to be calculated
                ));
            }

            // Sort by total usage duration (descending)
            result.sort(Comparator.comparingLong(AppUsageStats::totalUsageDurationMs).reversed());
            return result;

        } catch (SecurityException e) {
            return Collections.emptyList();
        }
    }

    @Override
    @NonNull
    public long[] getHourlyUsageStats(@NonNull String packageName) {
        if (!hasUsageStatsPermission() || usageStatsManager == null) {
            return new long[24];
        }

        long[] hourlyStats = new long[24];
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();

        try {
            List<android.app.usage.UsageStats> stats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_HOURLY,
                    startOfDay,
                    System.currentTimeMillis()
            );

            for (android.app.usage.UsageStats stat : stats) {
                if (packageName.equals(stat.getPackageName())) {
                    Calendar statCalendar = Calendar.getInstance();
                    statCalendar.setTimeInMillis(stat.getLastTimeUsed());
                    int hour = statCalendar.get(Calendar.HOUR_OF_DAY);
                    if (hour >= 0 && hour < 24) {
                        hourlyStats[hour] += stat.getTotalTimeInForeground();
                    }
                }
            }
        } catch (SecurityException e) {
            // Return empty array
        }

        return hourlyStats;
    }

    @Override
    @NonNull
    public List<AppUsageStats> getFocusSessionUsageStats() {
        List<FocusSessionEntity> sessions = focusSessionDao.recent(10);
        if (sessions.isEmpty()) {
            return Collections.emptyList();
        }

        List<AppUsageStats> result = new ArrayList<>();
        Map<String, Long> usageMap = new HashMap<>();

        for (FocusSessionEntity session : sessions) {
            if (session.getEndedAtEpochMs() == null) {
                continue;
            }

            long startTime = session.getStartedAtEpochMs();
            long endTime = session.getEndedAtEpochMs();

            List<AppUsageStats> sessionStats = getUsageStatsForRange(startTime, endTime);
            for (AppUsageStats stats : sessionStats) {
                String packageName = stats.packageName();
                usageMap.put(packageName, usageMap.getOrDefault(packageName, 0L) + stats.totalUsageDurationMs());
            }
        }

        long totalFocusUsage = usageMap.values().stream().mapToLong(Long::longValue).sum();
        for (Map.Entry<String, Long> entry : usageMap.entrySet()) {
            String appName = getAppName(entry.getKey());
            float percentage = totalFocusUsage > 0 ? (entry.getValue() * 100f / totalFocusUsage) : 0f;

            result.add(new AppUsageStats(
                    entry.getKey(),
                    appName,
                    entry.getValue(),
                    0, // launchCount
                    0, // sessionCount
                    entry.getValue() / Math.max(1, sessions.size()), // average
                    0, // firstOpenedTimeMs
                    0, // lastOpenedTimeMs
                    percentage,
                    entry.getValue(), // usageDuringFocusSessionsMs
                    0  // interruptionCount
            ));
        }

        result.sort(Comparator.comparingLong(AppUsageStats::totalUsageDurationMs).reversed());
        return result;
    }

    @Override
    @NonNull
    public List<AppUsageStats> getMostDistractingApps(int limit) {
        List<AppUsageStats> focusStats = getFocusSessionUsageStats();
        if (focusStats.isEmpty()) {
            return Collections.emptyList();
        }

        // Calculate distraction score based on focus session usage vs total usage
        List<AppUsageStats> dailyStats = getDailyUsageStats();
        Map<String, Long> dailyUsageMap = new HashMap<>();
        for (AppUsageStats stats : dailyStats) {
            dailyUsageMap.put(stats.packageName(), stats.totalUsageDurationMs());
        }

        List<AppUsageStats> distractingApps = new ArrayList<>();
        for (AppUsageStats focusStat : focusStats) {
            long dailyUsage = dailyUsageMap.getOrDefault(focusStat.packageName(), 0L);
            long focusUsage = focusStat.usageDuringFocusSessionsMs();
            
            // Distraction score: higher if app is used significantly during focus sessions
            // compared to overall usage
            float distractionScore = 0f;
            if (dailyUsage > 0) {
                distractionScore = (focusUsage * 100f) / dailyUsage;
            }

            // Add to results with score embedded in a custom way
            // For now, we'll use the usageDuringFocusSessionsMs as a proxy
            distractingApps.add(focusStat);
        }

        // Sort by focus session usage (descending)
        distractingApps.sort(Comparator.comparingLong(AppUsageStats::usageDuringFocusSessionsMs).reversed());

        // Limit results
        if (distractingApps.size() > limit) {
            return distractingApps.subList(0, limit);
        }
        return distractingApps;
    }

    @Override
    public boolean hasUsageStatsPermission() {
        if (appOpsManager == null) {
            return false;
        }
        
        int mode;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mode = appOpsManager.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.getPackageName()
            );
        } else {
            mode = appOpsManager.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.getPackageName()
            );
        }
        
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    @Override
    public long getTotalDailyUsageMs() {
        if (!hasUsageStatsPermission() || usageStatsManager == null) {
            return 0;
        }

        try {
            List<android.app.usage.UsageStats> stats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    getTodayStart(),
                    System.currentTimeMillis()
            );

            long total = 0;
            for (android.app.usage.UsageStats stat : stats) {
                total += stat.getTotalTimeInForeground();
            }
            return total;

        } catch (SecurityException e) {
            return 0;
        }
    }

    private long getTodayStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    @Nullable
    private String getAppName(@NonNull String packageName) {
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            CharSequence label = packageManager.getApplicationLabel(appInfo);
            return label != null ? label.toString() : packageName;
        } catch (PackageManager.NameNotFoundException e) {
            return packageName;
        }
    }

    private static class AppUsageData {
        final String packageName;
        final long totalUsageMs;

        AppUsageData(String packageName, long totalUsageMs) {
            this.packageName = packageName;
            this.totalUsageMs = totalUsageMs;
        }
    }
}