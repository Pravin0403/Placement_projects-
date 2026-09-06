package com.focusflow.ml;

/**
 * Typed inference output used by the decision engine.
 */
public final class PredictionResult {

    private final float riskScore;
    private final String modelVersion;
    private final long inferenceTimeMs;

    public PredictionResult(float riskScore, String modelVersion, long inferenceTimeMs) {
        this.riskScore = riskScore;
        this.modelVersion = modelVersion;
        this.inferenceTimeMs = inferenceTimeMs;
    }

    public float riskScore() {
        return riskScore;
    }

    public String modelVersion() {
        return modelVersion;
    }

    public long inferenceTimeMs() {
        return inferenceTimeMs;
    }
}
