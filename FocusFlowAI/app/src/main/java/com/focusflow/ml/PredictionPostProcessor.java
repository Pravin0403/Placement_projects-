package com.focusflow.ml;

/**
 * Maps raw model output into a bounded risk score.
 */
public interface PredictionPostProcessor {

    PredictionResult process(float rawOutput, String modelVersion, long inferenceTimeMs);
}
