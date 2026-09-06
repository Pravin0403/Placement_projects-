package com.focusflow.domain.model;

public final class OnboardingDraft {

    private final String displayName;
    private final boolean privacyAcknowledged;
    private final boolean telemetryConsented;

    public OnboardingDraft(String displayName, boolean privacyAcknowledged, boolean telemetryConsented) {
        this.displayName = displayName;
        this.privacyAcknowledged = privacyAcknowledged;
        this.telemetryConsented = telemetryConsented;
    }

    public String displayName() {
        return displayName;
    }

    public boolean privacyAcknowledged() {
        return privacyAcknowledged;
    }

    public boolean telemetryConsented() {
        return telemetryConsented;
    }

    public boolean canContinue() {
        return privacyAcknowledged;
    }
}
