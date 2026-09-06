package com.focusflow.engine;

import com.focusflow.domain.model.ProtectionLevel;

/**
 * Engine for managing focus protection states and levels.
 */
public interface FocusProtectionEngine {

    /**
     * Activate focus protection with specified level.
     * @param level Protection level to activate
     */
    void activateProtection(ProtectionLevel level);

    /**
     * Deactivate focus protection.
     */
    void deactivateProtection();

    /**
     * Get current protection level.
     * @return Current protection level, or null if not active
     */
    ProtectionLevel getCurrentProtectionLevel();

    /**
     * Check if protection is currently active.
     * @return true if protection is active
     */
    boolean isProtectionActive();

    /**
     * Get recommended protection level based on predicted risk.
     * @param riskScore Predicted distraction risk (0-1)
     * @return Recommended protection level
     */
    ProtectionLevel getRecommendedProtectionLevel(float riskScore);

    /**
     * Record a protection violation.
     * @param violationType Type of violation
     * @param packageName Package name of violating app (if applicable)
     */
    void recordViolation(String violationType, String packageName);

    /**
     * Get violation count for current session.
     * @return Number of violations in current session
     */
    int getViolationCount();
}