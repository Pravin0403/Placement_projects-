package com.focusflow.engine;

public class RecommendationEngineImpl implements RecommendationEngine {

    @Override
    public String recommendationFor(Decision decision) {
        switch (decision) {
            case SUGGEST_BREAK:
                return "Take a 3-minute break before attention drops.";
            case FOCUS_ALERT:
                return "Distraction risk is high. Silence extra apps and reset on one task.";
            case FEEDBACK_REQUEST:
                return "Risk is rising. Stay with the current task if you still feel focused.";
            case NO_ACTION:
            default:
                return "You are in a stable focus window. Keep going.";
        }
    }
}
