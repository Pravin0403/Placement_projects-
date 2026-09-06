package com.focusflow.engine;

import com.focusflow.domain.model.EmergencyContact;

import java.util.List;

/**
 * Engine for managing emergency access and multi-step confirmation.
 */
public interface EmergencyGatewayEngine {

    /**
     * Start the emergency access flow.
     * @param sessionId Current focus session ID
     * @return Emergency access state
     */
    EmergencyAccessState startEmergencyFlow(long sessionId);

    /**
     * Select emergency reason.
     * @param state Current emergency state
     * @param reason Selected reason
     * @return Updated emergency state
     */
    EmergencyAccessState selectReason(EmergencyAccessState state, String reason);

    /**
     * Confirm emergency access.
     * @param state Current emergency state
     * @param confirmation User confirmation text
     * @return Updated emergency state
     */
    EmergencyAccessState confirmAccess(EmergencyAccessState state, String confirmation);

    /**
     * Cancel emergency flow.
     * @param state Current emergency state
     * @return Canceled state
     */
    EmergencyAccessState cancelFlow(EmergencyAccessState state);

    /**
     * Get emergency contacts.
     * @return List of emergency contacts
     */
    List<EmergencyContact> getEmergencyContacts();

    /**
     * Add emergency contact.
     * @param contact Emergency contact to add
     */
    void addEmergencyContact(EmergencyContact contact);

    /**
     * Record emergency override.
     * @param sessionId Session ID
     * @param reason Emergency reason
     * @param grantedAccess Whether access was granted
     */
    void recordEmergencyOverride(long sessionId, String reason, boolean grantedAccess);

    /**
     * Check if user is abusing emergency access.
     * @return true if abuse detected
     */
    boolean isEmergencyAbuseDetected();
}

/**
 * Represents the state of the emergency access flow.
 */
class EmergencyAccessState {
    private final long sessionId;
    private final int step; // 1=reason selection, 2=confirmation, 3=cooldown, 4=granted
    private final String selectedReason;
    private final String confirmationCode;
    private final long cooldownRemainingMs;
    private final boolean isGranted;
    private final boolean isCancelled;

    public EmergencyAccessState(long sessionId, int step, String selectedReason, 
                                 String confirmationCode, long cooldownRemainingMs, 
                                 boolean isGranted, boolean isCancelled) {
        this.sessionId = sessionId;
        this.step = step;
        this.selectedReason = selectedReason;
        this.confirmationCode = confirmationCode;
        this.cooldownRemainingMs = cooldownRemainingMs;
        this.isGranted = isGranted;
        this.isCancelled = isCancelled;
    }

    public long sessionId() { return sessionId; }
    public int step() { return step; }
    public String selectedReason() { return selectedReason; }
    public String confirmationCode() { return confirmationCode; }
    public long cooldownRemainingMs() { return cooldownRemainingMs; }
    public boolean isGranted() { return isGranted; }
    public boolean isCancelled() { return isCancelled; }

    public static EmergencyAccessState initial(long sessionId) {
        return new EmergencyAccessState(sessionId, 1, null, null, 0, false, false);
    }

    public static EmergencyAccessState cancelled(long sessionId) {
        return new EmergencyAccessState(sessionId, 0, null, null, 0, false, true);
    }

    public static EmergencyAccessState granted(long sessionId) {
        return new EmergencyAccessState(sessionId, 4, null, null, 0, true, false);
    }
}