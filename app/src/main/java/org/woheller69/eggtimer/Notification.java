package org.woheller69.eggtimer;

import static android.os.Build.VERSION.SDK_INT;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class Notification {

    static void initNotification(Context context) {
        Intent intent = new Intent(context, NotificationService.class);
        if (SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    static void cancelNotification(Context context) {
        context.startService(new Intent(context, NotificationService.class).setAction("EggTimer.STOP_SERVICE"));

    }

    static void showNotification(Context context, String timeRemaining) {
        Intent intent = new Intent(context, NotificationService.class);
        intent.putExtra("Remaining", timeRemaining);
        if (SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

}
