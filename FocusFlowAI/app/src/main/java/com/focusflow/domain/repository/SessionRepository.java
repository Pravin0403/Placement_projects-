package com.focusflow.domain.repository;

import androidx.lifecycle.LiveData;

import com.focusflow.domain.model.FocusSession;

import java.util.List;

public interface SessionRepository {

    LiveData<List<FocusSession>> observeSessions();

    LiveData<FocusSession> observeActive();

    long startSession();

    void endSession();
}
