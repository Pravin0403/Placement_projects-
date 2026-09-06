package com.focusflow.feature.analytics;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.focusflow.core.common.AppExecutors;
import com.focusflow.domain.model.AppUsageStats;
import com.focusflow.domain.repository.AppUsageRepository;

import java.util.List;

/**
 * ViewModel for app usage analytics screen.
 */
public class AppUsageAnalyticsViewModel extends ViewModel {

    private final AppUsageRepository appUsageRepository;
    private final AppExecutors executors;
    
    private final MutableLiveData<List<AppUsageStats>> dailyUsageStats = new MutableLiveData<>();
    private final MutableLiveData<List<AppUsageStats>> focusSessionStats = new MutableLiveData<>();
    private final MutableLiveData<List<AppUsageStats>> mostDistractingApps = new MutableLiveData<>();
    private final MutableLiveData<Long> totalDailyUsage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasPermission = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public AppUsageAnalyticsViewModel(
            AppUsageRepository appUsageRepository,
            AppExecutors executors
    ) {
        this.appUsageRepository = appUsageRepository;
        this.executors = executors;
        checkPermission();
    }

    public LiveData<List<AppUsageStats>> dailyUsageStats() {
        return dailyUsageStats;
    }

    public LiveData<List<AppUsageStats>> focusSessionStats() {
        return focusSessionStats;
    }

    public LiveData<List<AppUsageStats>> mostDistractingApps() {
        return mostDistractingApps;
    }

    public LiveData<Long> totalDailyUsage() {
        return totalDailyUsage;
    }

    public LiveData<Boolean> hasPermission() {
        return hasPermission;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public void checkPermission() {
        hasPermission.setValue(appUsageRepository.hasUsageStatsPermission());
    }

    public void loadDailyUsageStats() {
        isLoading.setValue(true);
        executors.diskIo().execute(() -> {
            List<AppUsageStats> stats = appUsageRepository.getDailyUsageStats();
            long totalUsage = appUsageRepository.getTotalDailyUsageMs();
            executors.mainThread().execute(() -> {
                dailyUsageStats.setValue(stats);
                totalDailyUsage.setValue(totalUsage);
                isLoading.setValue(false);
            });
        });
    }

    public void loadFocusSessionStats() {
        isLoading.setValue(true);
        executors.diskIo().execute(() -> {
            List<AppUsageStats> stats = appUsageRepository.getFocusSessionUsageStats();
            executors.mainThread().execute(() -> {
                focusSessionStats.setValue(stats);
                isLoading.setValue(false);
            });
        });
    }

    public void loadMostDistractingApps(int limit) {
        isLoading.setValue(true);
        executors.diskIo().execute(() -> {
            List<AppUsageStats> stats = appUsageRepository.getMostDistractingApps(limit);
            executors.mainThread().execute(() -> {
                mostDistractingApps.setValue(stats);
                isLoading.setValue(false);
            });
        });
    }

    public void refreshAllData() {
        checkPermission();
        if (appUsageRepository.hasUsageStatsPermission()) {
            loadDailyUsageStats();
            loadFocusSessionStats();
            loadMostDistractingApps(10);
        }
    }
}