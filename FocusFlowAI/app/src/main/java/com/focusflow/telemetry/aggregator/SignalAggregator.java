package com.focusflow.telemetry.aggregator;

/**
 * Sliding-window aggregation over stored raw events.
 */
public interface SignalAggregator {

    void aggregateCurrentWindow();
}
