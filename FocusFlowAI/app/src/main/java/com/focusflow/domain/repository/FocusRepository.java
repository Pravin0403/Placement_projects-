package com.focusflow.domain.repository;

import androidx.lifecycle.LiveData;

import com.focusflow.domain.model.DashboardState;

public interface FocusRepository {

    LiveData<DashboardState> observeDashboard();
}
