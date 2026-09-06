package com.focusflow.telemetry.collectors;

/**
 * Collects consented screen unlock and interactive events.
 */
public interface ScreenEventCollector {

    void start();

    void stop();
}
