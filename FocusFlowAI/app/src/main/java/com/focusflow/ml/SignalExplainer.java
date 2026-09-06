package com.focusflow.ml;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class SignalExplainer {

    private SignalExplainer() {
    }

    @NonNull
    public static List<String> explain(FeatureVector vector) {
        List<String> signals = new ArrayList<>();
        if (vector.get(0) >= 4f) {
            signals.add("App switching increased");
        }
        if (vector.get(1) >= 2f) {
            signals.add("Frequent screen unlocks");
        }
        if (vector.get(8) >= 2f) {
            signals.add("Session interruptions increased");
        }
        if (vector.get(4) >= 0.6f) {
            signals.add("Interaction frequency is high");
        }
        if (vector.get(3) >= 45f) {
            signals.add("Long session without a break");
        }
        if (signals.isEmpty()) {
            signals.add("Stable on-device activity");
        }
        if (signals.size() > 3) {
            return signals.subList(0, 3);
        }
        return signals;
    }
}
