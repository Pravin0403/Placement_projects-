package com.focusflow.engine;

import com.focusflow.ml.PredictionResult;

public interface DecisionEngine {

    Decision decide(PredictionResult prediction);
}
