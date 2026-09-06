package com.focusflow.feature.focuszone;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.focusflow.core.common.AppExecutors;
import com.focusflow.domain.model.FocusZone;
import com.focusflow.engine.FocusZoneEngine;

import java.util.List;

/**
 * ViewModel for focus zone screen.
 */
public class FocusZoneViewModel extends ViewModel {

    private final FocusZoneEngine focusZoneEngine;
    private final AppExecutors executors;
    
    private final MutableLiveData<List<FocusZone>> focusZones = new MutableLiveData<>();
    private final MutableLiveData<FocusZone> bestFocusHour = new MutableLiveData<>();
    private final MutableLiveData<List<FocusZone>> recommendedHours = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public FocusZoneViewModel(
            FocusZoneEngine focusZoneEngine,
            AppExecutors executors
    ) {
        this.focusZoneEngine = focusZoneEngine;
        this.executors = executors;
    }

    public LiveData<List<FocusZone>> focusZones() {
        return focusZones;
    }

    public LiveData<FocusZone> bestFocusHour() {
        return bestFocusHour;
    }

    public LiveData<List<FocusZone>> recommendedHours() {
        return recommendedHours;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public void loadFocusZones() {
        isLoading.setValue(true);
        executors.diskIo().execute(() -> {
            List<FocusZone> zones = focusZoneEngine.calculateDailyFocusZones();
            FocusZone best = focusZoneEngine.getBestFocusHour();
            List<FocusZone> recommended = focusZoneEngine.getRecommendedFocusHours(5);
            
            executors.mainThread().execute(() -> {
                focusZones.setValue(zones);
                bestFocusHour.setValue(best);
                recommendedHours.setValue(recommended);
                isLoading.setValue(false);
            });
        });
    }
}