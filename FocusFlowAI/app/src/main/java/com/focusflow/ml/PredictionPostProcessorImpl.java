package com.focusflow.ml;

import com.focusflow.core.common.Constants;

public class PredictionPostProcessorImpl implements PredictionPostProcessor {

    @Override
    public PredictionResult process(float rawOutput, String modelVersion, long inferenceTimeMs) {
        float bounded = Math.max(0f, Math.min(1f, rawOutput));
        String version = modelVersion == null || modelVersion.isEmpty()
                ? Constants.MODEL_VERSION
                : modelVersion;
        return new PredictionResult(bounded, version, inferenceTimeMs);
    }

    public int focusScore(float risk) {
        return Math.round((1f - Math.max(0f, Math.min(1f, risk))) * 100f);
    }
}
