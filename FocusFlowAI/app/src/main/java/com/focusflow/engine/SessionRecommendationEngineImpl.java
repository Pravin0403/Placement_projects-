package com.focusflow.engine;

import com.focusflow.domain.model.FocusZone;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Implementation of SessionRecommendationEngine using FocusZoneEngine.
 */
public class SessionRecommendationEngineImpl implements SessionRecommendationEngine {

    private final FocusZoneEngine focusZoneEngine;

    public SessionRecommendationEngineImpl(FocusZoneEngine focusZoneEngine) {
        this.focusZoneEngine = focusZoneEngine;
    }

    @Override
    public FocusZone getBestOneHourSession() {
        return focusZoneEngine.getBestFocusHour();
    }

    @Override
    public List<FocusZone> getRankedOneHourSessions(int limit) {
        List<FocusZone> allZones = focusZoneEngine.calculateDailyFocusZones();
        
        // Filter out sleep hours and low-focus zones
        List<FocusZone> recommendedZones = new ArrayList<>();
        for (FocusZone zone : allZones) {
            if (zone.focusScore() > 50 && !zone.recommendation().contains("Sleep")) {
                recommendedZones.add(zone);
            }
        }

        // Sort by focus score (descending)
        recommendedZones.sort(Comparator.comparingInt(FocusZone::focusScore).reversed());

        // Return top recommendations
        if (recommendedZones.size() > limit) {
            return recommendedZones.subList(0, limit);
        }
        return recommendedZones;
    }

    @Override
    public String getRecommendationExplanation(int hour) {
        FocusZone zone = focusZoneEngine.calculateDailyFocusZones().stream()
                .filter(z -> z.hourOfDay() == hour)
                .findFirst()
                .orElse(null);

        if (zone == null) {
            return "No data available for this hour.";
        }

        StringBuilder explanation = new StringBuilder();
        explanation.append("Focus Score: ").append(zone.focusScore()).append("/100\n");
        explanation.append("Distraction Risk: ").append(String.format("%.0f%%", zone.distractionRisk() * 100)).append("\n");
        explanation.append("Confidence: ").append(String.format("%.0f%%", zone.confidence() * 100)).append("\n");
        explanation.append("\n").append(zone.recommendation());

        if (zone.focusScore() >= 85) {
            explanation.append("\n\nYou historically perform very well during this period with low distraction rates.");
        } else if (zone.focusScore() >= 70) {
            explanation.append("\n\nThis is a good time for focused work based on your historical patterns.");
        } else if (zone.focusScore() >= 50) {
            explanation.append("\n\nThis time is moderately suitable for focus work with proper breaks.");
        } else {
            explanation.append("\n\nThis time is challenging for deep work based on your historical patterns.");
        }

        return explanation.toString();
    }
}