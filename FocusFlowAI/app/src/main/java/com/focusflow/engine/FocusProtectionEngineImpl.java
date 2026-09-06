package com.focusflow.engine;

import android.content.Context;

import com.focusflow.data.local.AppSettings;
import com.focusflow.data.local.dao.ViolationDao;
import com.focusflow.data.local.entity.ViolationEntity;
import com.focusflow.domain.model.ProtectionLevel;

/**
 * Implementation of FocusProtectionEngine.
 */
public class FocusProtectionEngineImpl implements FocusProtectionEngine {

    private final Context context;
    private final AppSettings settings;
    private final ViolationDao violationDao;
    
    private ProtectionLevel currentLevel;
    private boolean isActive;
    private int violationCount;

    public FocusProtectionEngineImpl(
            Context context,
            AppSettings settings,
            ViolationDao violationDao
    ) {
        this.context = context.getApplicationContext();
        this.settings = settings;
        this.violationDao = violationDao;
        this.isActive = false;
        this.violationCount = 0;
    }

    @Override
    public void activateProtection(ProtectionLevel level) {
        this.currentLevel = level;
        this.isActive = true;
        this.violationCount = 0;
        settings.setProtectionLevel(level.name());
    }

    @Override
    public void deactivateProtection() {
        this.isActive = false;
        this.currentLevel = null;
        settings.setProtectionLevel(null);
    }

    @Override
    public ProtectionLevel getCurrentProtectionLevel() {
        if (!isActive) {
            return null;
        }
        return currentLevel;
    }

    @Override
    public boolean isProtectionActive() {
        return isActive;
    }

    @Override
    public ProtectionLevel getRecommendedProtectionLevel(float riskScore) {
        if (riskScore >= 0.8f) {
            return ProtectionLevel.DEEP;
        } else if (riskScore >= 0.5f) {
            return ProtectionLevel.STRONG;
        } else {
            return ProtectionLevel.LIGHT;
        }
    }

    @Override
    public void recordViolation(String violationType, String packageName) {
        this.violationCount++;
        
        // Log violation to database
        ViolationEntity violation = new ViolationEntity(
                0,
                0, // session ID - would need to be set properly
                violationType,
                "User violated protection: " + violationType,
                System.currentTimeMillis(),
                packageName,
                wasBlocked(violationType)
        );
        
        violationDao.insert(violation);
    }

    @Override
    public int getViolationCount() {
        return violationCount;
    }

    private boolean wasBlocked(String violationType) {
        // Determine if the violation was blocked based on protection level
        if (currentLevel == null) {
            return false;
        }

        switch (currentLevel) {
            case DEEP:
                return true; // Most violations are blocked at DEEP level
            case STRONG:
                return !violationType.equals("EMERGENCY_OVERRIDE");
            case LIGHT:
                return false; // LIGHT level mostly monitors
            default:
                return false;
        }
    }
}