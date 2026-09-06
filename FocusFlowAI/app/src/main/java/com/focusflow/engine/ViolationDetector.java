package com.focusflow.engine;

import com.focusflow.domain.model.Violation;

import java.util.List;

/**
 * Detector for focus session violations.
 */
public interface ViolationDetector {

    /**
     * Detect violations for a specific session.
     * @param sessionId Session ID
     * @return List of detected violations
     */
    List<Violation> detectViolations(long sessionId);

    /**
     * Detect violation in real-time.
     * @param sessionId Current session ID
     * @param eventType Type of event (APP_SWITCH, etc.)
     * @param packageName Package name involved
     * @return Detected violation, or null if no violation
     */
    Violation detectRealTimeViolation(long sessionId, String eventType, String packageName);

    /**
     * Check if an event constitutes a violation.
     * @param eventType Type of event
     * @param packageName Package name involved
     * @param currentProtectionLevel Current protection level
     * @return true if this is a violation
     */
    boolean isViolation(String eventType, String packageName, String currentProtectionLevel);
}