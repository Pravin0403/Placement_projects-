package com.focusflow.engine;

import android.content.Context;

import com.focusflow.data.local.AppSettings;
import com.focusflow.data.local.dao.EmergencyContactDao;
import com.focusflow.data.local.dao.ViolationDao;
import com.focusflow.data.local.entity.EmergencyContactEntity;
import com.focusflow.data.local.entity.ViolationEntity;
import com.focusflow.domain.model.EmergencyContact;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of EmergencyGatewayEngine.
 */
public class EmergencyGatewayEngineImpl implements EmergencyGatewayEngine {

    private static final String REQUIRED_CONFIRMATION = "FOCUS EXIT";
    private static final long COOLDOWN_MS = 10_000; // 10 seconds
    private static final long EMERGENCY_ACCESS_DURATION_MS = 2 * 60 * 1000; // 2 minutes

    private final Context context;
    private final AppSettings settings;
    private final EmergencyContactDao emergencyContactDao;
    private final ViolationDao violationDao;

    public EmergencyGatewayEngineImpl(
            Context context,
            AppSettings settings,
            EmergencyContactDao emergencyContactDao,
            ViolationDao violationDao
    ) {
        this.context = context.getApplicationContext();
        this.settings = settings;
        this.emergencyContactDao = emergencyContactDao;
        this.violationDao = violationDao;
    }

    @Override
    public EmergencyAccessState startEmergencyFlow(long sessionId) {
        return EmergencyAccessState.initial(sessionId);
    }

    @Override
    public EmergencyAccessState selectReason(EmergencyAccessState state, String reason) {
        if (state.step() != 1) {
            return state;
        }

        // Check if reason requires cooldown
        if (requiresCooldown(reason)) {
            return new EmergencyAccessState(
                    state.sessionId(),
                    3, // Cooldown step
                    reason,
                    null,
                    COOLDOWN_MS,
                    false,
                    false
            );
        }

        // Move to confirmation step
        return new EmergencyAccessState(
                state.sessionId(),
                2, // Confirmation step
                reason,
                null,
                0,
                false,
                false
        );
    }

    @Override
    public EmergencyAccessState confirmAccess(EmergencyAccessState state, String confirmation) {
        if (state.step() != 2) {
            return state;
        }

        // Validate confirmation code
        if (REQUIRED_CONFIRMATION.equals(confirmation.toUpperCase())) {
            return EmergencyAccessState.granted(state.sessionId());
        }

        // Wrong confirmation - return to start
        return EmergencyAccessState.initial(state.sessionId());
    }

    @Override
    public EmergencyAccessState cancelFlow(EmergencyAccessState state) {
        return EmergencyAccessState.cancelled(state.sessionId());
    }

    @Override
    public List<EmergencyContact> getEmergencyContacts() {
        List<EmergencyContactEntity> entities = emergencyContactDao.observeAll().getValue();
        List<EmergencyContact> contacts = new ArrayList<>();

        if (entities != null) {
            for (EmergencyContactEntity entity : entities) {
                contacts.add(new EmergencyContact(
                        entity.getId(),
                        entity.getName(),
                        entity.getContactType(),
                        entity.getContactValue(),
                        entity.isPriority()
                ));
            }
        }

        return contacts;
    }

    @Override
    public void addEmergencyContact(EmergencyContact contact) {
        EmergencyContactEntity entity = new EmergencyContactEntity(
                0,
                contact.name(),
                contact.contactType(),
                contact.contactValue(),
                contact.isPriority(),
                System.currentTimeMillis()
        );
        emergencyContactDao.insert(entity);
    }

    @Override
    public void recordEmergencyOverride(long sessionId, String reason, boolean grantedAccess) {
        ViolationEntity violation = new ViolationEntity(
                0,
                sessionId,
                "EMERGENCY_OVERRIDE",
                "Emergency access requested: " + reason,
                System.currentTimeMillis(),
                null,
                false
        );
        violationDao.insert(violation);
    }

    @Override
    public boolean isEmergencyAbuseDetected() {
        // Check recent emergency overrides
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -24); // Last 24 hours
        long cutoffTime = calendar.getTimeInMillis();

        // Count emergency overrides in last 24 hours
        int overrideCount = 0; // Would need proper DAO query

        // Consider abuse if:
        // - More than 5 overrides in 24 hours
        // - High frequency of overrides
        // - Pattern of overrides followed by long distraction periods

        return overrideCount > 5;
    }

    private boolean requiresCooldown(String reason) {
        // Require cooldown for non-emergency reasons
        List<String> emergencyReasons = List.of(
                "Emergency call",
                "Family",
                "College / work",
                "Important message"
        );

        return !emergencyReasons.contains(reason);
    }
}