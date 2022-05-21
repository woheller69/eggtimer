package org.woheller69.eggtimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;

public class AlarmReceiver extends BroadcastReceiver {

    private static final MediaPlayer player = new MediaPlayer();

    @Override
    public void onReceive(Context context, Intent intent) {
        playAlarmSound(context);
    }

    public static void playAlarmSound(Context context){
        try {
            Uri notification = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM);  //play through alarm channel
            player.reset();
            player.setLooping(true);
            player.setDataSource(context,notification);
            player.setAudioStreamType(AudioManager.STREAM_ALARM);
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
}
