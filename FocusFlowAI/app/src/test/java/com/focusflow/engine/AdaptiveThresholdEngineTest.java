package com.focusflow.engine;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AdaptiveThresholdEngineTest {

    private final AdaptiveThresholdEngine engine = new AdaptiveThresholdEngine();

    @Test
    public void nextThreshold_lowersWhenHelpful() {
        float next = engine.nextThreshold(0.72f, true);
        assertEquals(0.70f, next, 0.001f);
    }

    @Test
    public void nextThreshold_raisesWhenNotHelpful() {
        float next = engine.nextThreshold(0.72f, false);
        assertEquals(0.75f, next, 0.0001f);
    }

    @Test
    public void nextThreshold_clampsToRange() {
        assertTrue(engine.nextThreshold(0.45f, true) >= AdaptiveThresholdEngine.MIN_THRESHOLD);
        assertTrue(engine.nextThreshold(0.92f, false) <= AdaptiveThresholdEngine.MAX_THRESHOLD);
    }
}
