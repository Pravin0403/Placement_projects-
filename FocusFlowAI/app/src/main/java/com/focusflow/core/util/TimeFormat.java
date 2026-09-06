package com.focusflow.core.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class TimeFormat {

    private TimeFormat() {
    }

    public static String sessionWindow(long startedAtEpochMs, long endedAtEpochMs) {
        SimpleDateFormat format = new SimpleDateFormat("h:mm a", Locale.getDefault());
        String start = format.format(new Date(startedAtEpochMs));
        String end = format.format(new Date(endedAtEpochMs));
        return start + " – " + end;
    }

    public static String elapsed(long startedAtEpochMs) {
        long totalSeconds = Math.max(0L, (System.currentTimeMillis() - startedAtEpochMs) / 1000L);
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    public static String formatDuration(long durationMs) {
        long totalSeconds = durationMs / 1000L;
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%dh %dm", hours, minutes);
        } else if (minutes > 0) {
            return String.format(Locale.getDefault(), "%dm", minutes);
        } else {
            return "0m";
        }
    }
}
