package com.focusflow.telemetry.processor;

/**
 * Lifecycle-safe coordinator for consented collectors.
 */
public interface TelemetryProcessor {

    void startIfConsented();

    void stop();
}
