package com.focusflow.ml;

import java.util.Map;

/**
 * Builds the 5-minute window feature vector for inference.
 */
public interface FeatureExtractor {

    Map<String, Float> extract();
}
