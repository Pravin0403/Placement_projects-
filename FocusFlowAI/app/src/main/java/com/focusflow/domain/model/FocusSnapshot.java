package com.focusflow.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FocusSnapshot {

    @Nullable
    private final Integer focusScore;
    @Nullable
    private final Integer distractionRiskPercent;
    @Nullable
    private final Long sessionStartedAtEpochMs;
    @Nullable
    private final Long sessionEndedAtEpochMs;
    @NonNull
    private final List<String> topSignals;
    @Nullable
    private final String recommendation;

    public FocusSnapshot(
            @Nullable Integer focusScore,
            @Nullable Integer distractionRiskPercent,
            @Nullable Long sessionStartedAtEpochMs,
            @Nullable Long sessionEndedAtEpochMs,
            @NonNull List<String> topSignals,
            @Nullable String recommendation
    ) {
        this.focusScore = focusScore;
        this.distractionRiskPercent = distractionRiskPercent;
        this.sessionStartedAtEpochMs = sessionStartedAtEpochMs;
        this.sessionEndedAtEpochMs = sessionEndedAtEpochMs;
        this.topSignals = Collections.unmodifiableList(new ArrayList<>(topSignals));
        this.recommendation = recommendation;
    }

    public static FocusSnapshot empty() {
        return new FocusSnapshot(null, null, null, null, Collections.emptyList(), null);
    }

    @Nullable
    public Integer focusScore() {
        return focusScore;
    }

    @Nullable
    public Integer distractionRiskPercent() {
        return distractionRiskPercent;
    }

    @Nullable
    public Long sessionStartedAtEpochMs() {
        return sessionStartedAtEpochMs;
    }

    @Nullable
    public Long sessionEndedAtEpochMs() {
        return sessionEndedAtEpochMs;
    }

    @NonNull
    public List<String> topSignals() {
        return topSignals;
    }

    @Nullable
    public String recommendation() {
        return recommendation;
    }

    public boolean hasPrediction() {
        return focusScore != null && distractionRiskPercent != null;
    }
}
