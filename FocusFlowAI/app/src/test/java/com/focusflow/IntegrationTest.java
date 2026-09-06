package com.focusflow;

import com.focusflow.core.di.AppContainer;
import com.focusflow.data.local.AppDatabase;
import com.focusflow.data.local.AppSettings;
import com.focusflow.data.local.dao.FocusZoneDao;
import com.focusflow.domain.model.AppUsageStats;
import com.focusflow.domain.model.FocusZone;
import com.focusflow.domain.repository.AppUsageRepository;
import com.focusflow.engine.FocusZoneEngine;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Integration test to verify basic functionality of new components.
 */
public class IntegrationTest {

    @Test
    public void testAppUsageStatsModel() {
        AppUsageStats stats = new AppUsageStats(
                "com.example.app",
                "Example App",
                3600000L, // 1 hour
                5,
                3,
                1200000L, // 20 min average
                System.currentTimeMillis() - 86400000L,
                System.currentTimeMillis(),
                15.5f,
                600000L, // 10 min during focus
                2
        );

        assertEquals("com.example.app", stats.packageName());
        assertEquals("Example App", stats.appName());
        assertEquals(3600000L, stats.totalUsageDurationMs());
        assertEquals(5, stats.launchCount());
        assertEquals(15.5f, stats.percentageOfTotalUsage(), 0.1f);
        assertEquals(600000L, stats.usageDuringFocusSessionsMs());
        assertEquals(2, stats.interruptionCount());
    }

    @Test
    public void testFocusZoneModel() {
        FocusZone zone = new FocusZone(
                9,
                85,
                0.15f,
                0.87f,
                "Excellent focus time - highly recommended",
                true
        );

        assertEquals(9, zone.hourOfDay());
        assertEquals(85, zone.focusScore());
        assertEquals(0.15f, zone.distractionRisk(), 0.01f);
        assertEquals(0.87f, zone.confidence(), 0.01f);
        assertTrue(zone.isRecommended());
    }

    @Test
    public void testProtectionLevelEnum() {
        com.focusflow.domain.model.ProtectionLevel[] levels = com.focusflow.domain.model.ProtectionLevel.values();
        assertEquals(3, levels.length);
        assertEquals("Light Protection", com.focusflow.domain.model.ProtectionLevel.LIGHT.getDisplayName());
        assertEquals("Strong Protection", com.focusflow.domain.model.ProtectionLevel.STRONG.getDisplayName());
        assertEquals("Deep Protection", com.focusflow.domain.model.ProtectionLevel.DEEP.getDisplayName());
    }

    @Test
    public void testEmergencyContactModel() {
        com.focusflow.domain.model.EmergencyContact contact = new com.focusflow.domain.model.EmergencyContact(
                1L,
                "John Doe",
                "phone",
                "+1234567890",
                true
        );

        assertEquals(1L, contact.id());
        assertEquals("John Doe", contact.name());
        assertEquals("phone", contact.contactType());
        assertEquals("+1234567890", contact.contactValue());
        assertTrue(contact.isPriority());
    }

    @Test
    public void testViolationModel() {
        com.focusflow.domain.model.Violation violation = new com.focusflow.domain.model.Violation(
                1L,
                100L,
                "BLOCKED_APP_ATTEMPT",
                "User attempted to access blocked app",
                System.currentTimeMillis(),
                "com.instagram.android",
                true
        );

        assertEquals(1L, violation.id());
        assertEquals(100L, violation.sessionId());
        assertEquals("BLOCKED_APP_ATTEMPT", violation.violationType());
        assertEquals("com.instagram.android", violation.packageName());
        assertTrue(violation.wasBlocked());
    }

    @Test
    public void testSessionStatisticsModel() {
        com.focusflow.domain.model.SessionStatistics stats = new com.focusflow.domain.model.SessionStatistics(
                100L,
                System.currentTimeMillis() - 3600000L,
                System.currentTimeMillis(),
                3600000L, // 1 hour planned
                3540000L, // 59 min actual
                98.3f,
                4,
                2,
                3,
                0.25f,
                0.78f,
                2,
                92,
                List.of("com.instagram.android", "com.youtube.android")
        );

        assertEquals(100L, stats.sessionId());
        assertEquals(3600000L, stats.plannedDurationMs());
        assertEquals(3540000L, stats.actualDurationMs());
        assertEquals(98.3f, stats.completionPercentage(), 0.1f);
        assertEquals(4, stats.distractionAttempts());
        assertEquals(2, stats.blockedAppAttempts());
        assertEquals(92, stats.focusScore());
        assertEquals(2, stats.distractingApps().size());
    }
}