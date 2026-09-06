package com.focusflow.domain.usecase;

import androidx.lifecycle.LiveData;

import com.focusflow.domain.model.DashboardState;
import com.focusflow.domain.repository.FocusRepository;

public class ObserveDashboardUseCase {

    private final FocusRepository focusRepository;

    public ObserveDashboardUseCase(FocusRepository focusRepository) {
        this.focusRepository = focusRepository;
    }

    public LiveData<DashboardState> execute() {
        return focusRepository.observeDashboard();
    }
}
