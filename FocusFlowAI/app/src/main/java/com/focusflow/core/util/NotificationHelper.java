package com.focusflow.core.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.focusflow.core.common.Constants;

public final class NotificationHelper {

    private NotificationHelper() {
    }

    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                "Focus sessions",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Ongoing focus session and break suggestions");
        manager.createNotificationChannel(channel);
    }
}
