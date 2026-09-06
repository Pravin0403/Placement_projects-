package com.focusflow.feature.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.focusflow.core.common.AppExecutors;
import com.focusflow.domain.model.DashboardState;
import com.focusflow.domain.usecase.ObserveDashboardUseCase;
import com.focusflow.domain.usecase.SubmitFeedbackUseCase;

public class DashboardViewModel extends ViewModel {

    private final LiveData<DashboardState> dashboardState;
    private final SubmitFeedbackUseCase submitFeedbackUseCase;
    private final AppExecutors executors;

    public DashboardViewModel(
            ObserveDashboardUseCase observeDashboardUseCase,
            SubmitFeedbackUseCase submitFeedbackUseCase,
            AppExecutors executors
    ) {
        dashboardState = observeDashboardUseCase.execute();
        this.submitFeedbackUseCase = submitFeedbackUseCase;
        this.executors = executors;
    }

    public LiveData<DashboardState> dashboardState() {
        return dashboardState;
    }

    public void submitFeedback(boolean helpful) {
        executors.diskIo().execute(() -> submitFeedbackUseCase.execute(helpful, helpful ? "kept_working" : "ignored"));
    }
}
