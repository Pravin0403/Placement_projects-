package com.focusflow.engine;

import com.focusflow.data.local.dao.BlockedAppDao;
import com.focusflow.data.local.dao.TelemetryEventDao;
import com.focusflow.data.local.dao.ViolationDao;
import com.focusflow.data.local.entity.TelemetryEventEntity;
import com.focusflow.data.local.entity.ViolationEntity;
import com.focusflow.domain.model.ProtectionLevel;
import com.focusflow.domain.model.Violation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of ViolationDetector.
 */
public class ViolationDetectorImpl implements ViolationDetector {

    private final BlockedAppDao blockedAppDao;
    private final TelemetryEventDao eventDao;
    private final ViolationDao violationDao;

    public ViolationDetectorImpl(
            BlockedAppDao blockedAppDao,
            TelemetryEventDao eventDao,
            ViolationDao violationDao
    ) {
        this.blockedAppDao = blockedAppDao;
        this.eventDao = eventDao;
        this.violationDao = violationDao;
    }

    @Override
    public List<Violation> detectViolations(long sessionId) {
        List<TelemetryEventEntity> events = Collections.emptyList(); // Would need proper DAO query
        List<Violation> violations = new ArrayList<>();

        for (TelemetryEventEntity event : events) {
            Violation violation = detectRealTimeViolation(
                    sessionId,
                    event.getType(),
                    event.getPackageName()
            );
            if (violation != null) {
                violations.add(violation);
            }
        }

        return violations;
    }

    @Override
    public Violation detectRealTimeViolation(long sessionId, String eventType, String packageName) {
        // Determine if this event constitutes a violation
        if (isViolation(eventType, packageName, "STRONG")) {
            return new Violation(
                    0,
                    sessionId,
                    determineViolationType(eventType, packageName),
                    generateViolationDescription(eventType, packageName),
                    System.currentTimeMillis(),
                    packageName,
                    wasBlocked(eventType, packageName)
            );
        }
        return null;
    }

    @Override
    public boolean isViolation(String eventType, String packageName, String currentProtectionLevel) {
        // Check if app is blocked
        if (packageName != null && blockedAppDao.isBlocked(packageName)) {
            return true;
        }

        // Check event type severity based on protection level
        switch (currentProtectionLevel) {
            case "DEEP":
                // DEEP: Most events are violations
                return !"SESSION_START".equals(eventType) && !"SESSION_END".equals(eventType);
            case "STRONG":
                // STRONG: Blocked apps and frequent switching are violations
                if (packageName != null && blockedAppDao.isBlocked(packageName)) {
                    return true;
                }
                return "APP_SWITCH".equals(eventType);
            case "LIGHT":
                // LIGHT: Only blocked apps are violations
                return packageName != null && blockedAppDao.isBlocked(packageName);
            default:
                return false;
        }
    }

    private String determineViolationType(String eventType, String packageName) {
        if (packageName != null && blockedAppDao.isBlocked(packageName)) {
            return "BLOCKED_APP_ATTEMPT";
        }
        switch (eventType) {
            case "APP_SWITCH":
                return "DISTRACTING_APP_SWITCH";
            case "SCREEN_UNLOCK":
                return "EXCESSIVE_UNLOCK";
            default:
                return "FOCUS_VIOLATION";
        }
    }

    private String generateViolationDescription(String eventType, String packageName) {
        if (packageName != null && blockedAppDao.isBlocked(packageName)) {
            return "Attempted to access blocked app: " + packageName;
        }
        switch (eventType) {
            case "APP_SWITCH":
                return "Switched to distracting application during focus session";
            case "SCREEN_UNLOCK":
                return "Excessive phone unlocking during focus session";
            default:
                return "Focus session violation detected";
        }
    }

    private boolean wasBlocked(String eventType, String packageName) {
        // This would depend on the protection level and actual blocking mechanism
        // For now, assume it's blocked if it's a blocked app
        return packageName != null && blockedAppDao.isBlocked(packageName);
    }
}