package org.woheller69.eggtimer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;

public class NotificationService extends Service {

    public static final String ACTION_STOP_SERVICE = "EggTimer.STOP_SERVICE";
    public static boolean mIsRunning = false;
    private NotificationManager mNotificationManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || !ACTION_STOP_SERVICE.equals(intent.getAction())) {  //intent may be null if service is recreated after it got killed
            if (!mIsRunning){
                initNotif();
                mIsRunning = true;
            } else {
                if (intent != null && intent.hasExtra("Remaining")){
                    updateNotif(intent.getStringExtra("Remaining"));
                }
            }
            return START_STICKY;
        } else {
            stop();
            return START_NOT_STICKY;
        }
    }

    private void stop() {
        mIsRunning = false;
        stopForeground(true);
        stopSelf();
    }

    private static final int NOTIF_ID = 123;
    private static final String CHANNEL_ID = "EggTimer";

    private void updateNotif(String remaining){
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pi = PendingIntent.getActivity(this, NOTIF_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new Builder(this, CHANNEL_ID)
                .setSilent(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // For N and below
                .setContentIntent(pi)
                .setSmallIcon(R.drawable.ic_egg)
                .setAutoCancel(false)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentText(remaining)
                .setContentTitle(getString(R.string.cookingtime)).build();

                mNotificationManager.notify(NOTIF_ID,notification);
    }

    private void initNotif() {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            mNotificationManager.createNotificationChannel(new NotificationChannel(CHANNEL_ID, "EggTimer", NotificationManager.IMPORTANCE_DEFAULT));
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pi = PendingIntent.getActivity(this, NOTIF_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // For N and below
        Notification notification = new Builder(this, CHANNEL_ID)
                .setSilent(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // For N and below
                .setContentIntent(pi)
                .setSmallIcon(R.drawable.ic_egg)
                .setAutoCancel(false)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentText("--:--")
                .setContentTitle(getString(R.string.cookingtime)).build();

        if (VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED);
        } else if (VERSION.SDK_INT >= VERSION_CODES.Q) {
            startForeground(NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST);
        } else {
            startForeground(NOTIF_ID, notification);
        }

    }

}
