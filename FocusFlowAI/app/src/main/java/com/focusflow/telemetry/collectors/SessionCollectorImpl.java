package com.focusflow.telemetry.collectors;

import com.focusflow.domain.repository.SessionRepository;

public class SessionCollectorImpl implements SessionCollector {

    private final SessionRepository sessionRepository;

    public SessionCollectorImpl(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public void startSession() {
        sessionRepository.startSession();
    }

    @Override
    public void endSession() {
        sessionRepository.endSession();
    }
}
