package com.focusflow.feature.analytics;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.focusflow.data.local.dao.PredictionDao;
import com.focusflow.data.local.entity.PredictionEntity;
import com.focusflow.domain.repository.ModelRepository;

import java.util.List;

public class AnalyticsViewModel extends ViewModel {

    private final LiveData<List<PredictionEntity>> predictions;
    private final String modelVersion;

    public AnalyticsViewModel(PredictionDao predictionDao, ModelRepository modelRepository) {
        predictions = predictionDao.observeRecent(20);
        modelVersion = modelRepository.activeModelVersion();
    }

    public LiveData<List<PredictionEntity>> predictions() {
        return predictions;
    }

    public String modelVersion() {
        return modelVersion;
    }
}
