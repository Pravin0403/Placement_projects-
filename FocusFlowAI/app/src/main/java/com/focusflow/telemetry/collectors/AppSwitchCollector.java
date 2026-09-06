package com.focusflow.telemetry.collectors;

/**
 * Collects consented app-switch events via UsageStatsManager.
 */
public interface AppSwitchCollector {

    void start();

    void stop();
}
