package com.focusflow.telemetry.collectors;

/**
 * Records explicit focus session start and end.
 */
public interface SessionCollector {

    void startSession();

    void endSession();
}
