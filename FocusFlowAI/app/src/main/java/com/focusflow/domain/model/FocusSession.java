package com.focusflow.domain.model;

import androidx.annotation.Nullable;

public final class FocusSession {

    private final long id;
    private final long startedAtEpochMs;
    @Nullable
    private final Long endedAtEpochMs;

    public FocusSession(long id, long startedAtEpochMs, @Nullable Long endedAtEpochMs) {
        this.id = id;
        this.startedAtEpochMs = startedAtEpochMs;
        this.endedAtEpochMs = endedAtEpochMs;
    }

    public long id() {
        return id;
    }

    public long startedAtEpochMs() {
        return startedAtEpochMs;
    }

    @Nullable
    public Long endedAtEpochMs() {
        return endedAtEpochMs;
    }
}
