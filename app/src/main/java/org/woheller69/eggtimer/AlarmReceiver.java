package org.woheller69.eggtimer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;

import androidx.preference.PreferenceManager;

public class AlarmReceiver extends BroadcastReceiver {

    private static final MediaPlayer player = new MediaPlayer();

    @Override
    public void onReceive(Context context, Intent intent) {
        playAlarmSound(context);
    }

    public static void playAlarmSound(Context context){
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            Uri notification;
            if (sp.getBoolean("internalAlarm",false)){
                notification = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.rooster);
            } else {
                notification = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM);
            }

            player.reset();
            player.setLooping(true);
            player.setDataSource(context,notification);
            player.setAudioStreamType(AudioManager.STREAM_ALARM);   //play through alarm channel
            /* setAudioStreamType is deprecated. Can be replaced as below but requires MinSDK 21
            player.setAudioAttributes(
                    new AudioAttributes
                            .Builder()
                            .setLegacyStreamType(AudioManager.STREAM_ALARM)
                            .build());*/
            player.prepare();
            player.start();
            CountDownTimer alarmduration = new CountDownTimer((long) 10000, 1000) {
                @Override
                public void onTick(long l) {
                }

                @Override
                public void onFinish() {
                    stopAlarmSound();
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static  void stopAlarmSound(){
            player.stop();
    }

    public static boolean isRingtoneActive(){
        return player.isPlaying();
    }

    public static void cancelAlarm(Context context){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(context,0,intent,0);
        }
        alarmManager.cancel(pendingIntent);
    }
}
