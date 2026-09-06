package com.focusflow.ml;

/**
 * Applies training-time normalization to extracted features.
 */
public interface FeatureScaler {

    float[] scale(float[] rawFeatures);
}
