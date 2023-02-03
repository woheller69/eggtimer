package org.woheller69.eggtimer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    //SOFT_EGG = 66; Core temperature
    //MEDIUM_EGG = 72;
    //HARD_EGG = 86;
    //S_EGG = 48;  //<53g
    //M_EGG = 58;  //53...63g
    //L_EGG = 68;  //63...73g
    //XL_EGG = 76; //>73g
    private static final String[] eggSize = {"XS","S", "M", "L", "XL", "45g", "50g", "55g", "60g", "65g", "70g", "75g", "80g"};
    private static final int[] eggWeight = {42,48,58,68,76,45,50,55,60,65,70,75,80};
    private static final String[] fridgeTemperature = {"4°C","6°C", "8°C", "10°C", "12°C", "15°C", "20°C", "25°C", "30°C"};
    private static final int[] fridgeTemperatureVal = {4,6,8,10,12,15,20,25,30};
    private static final int[] coreTemperature = {62,64,66,68,70,72,74,76,78,80,82,84,86,88};

    private TextView timerTextView;
    private TextView altitudeTextView;
    private Button controllerButton;
    private Boolean counterIsActive = false;
    private Boolean countUpTimerIsActive = false;

    private CountDownTimer countDownTimer;
    private Timer countUpTimer;
    private MediaPlayer mediaPlayer;

    private int weight;
    private int tFridge;
    private int tTarget;
    private final Context context = this;

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        initViews();
    }

    private void initViews() {
        String[] consistency = new String[coreTemperature.length];
        setContentView(R.layout.activity_main);
        timerTextView = (TextView) findViewById(R.id.timerTextView);
        altitudeTextView = findViewById(R.id.altitude);
        controllerButton = (Button) findViewById(R.id.controllerButton);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        eggWeight[0]=sp.getInt("xs_weight",42);
        eggWeight[1]=sp.getInt("s_weight",48);
        eggWeight[2]=sp.getInt("m_weight",58);
        eggWeight[3]=sp.getInt("l_weight",68);
        eggWeight[4]=sp.getInt("xl_weight",76);
        eggSize[0]=sp.getString("xs_name","XS");
        eggSize[1]=sp.getString("s_name","S (EU)");
        eggSize[2]=sp.getString("m_name","M (EU)");
        eggSize[3]=sp.getString("l_name","L (EU)");
        eggSize[4]=sp.getString("xl_name","XL (EU)");

        for (int i = 0; i < coreTemperature.length; i++) {
            consistency[i]=coreTemperature[i]+"°C";
            if (Integer.parseInt(sp.getString("soft","66"))==coreTemperature[i]) consistency[i]=consistency[i]+" "+getString(R.string.soft);
            if (Integer.parseInt(sp.getString("medium","72"))==coreTemperature[i]) consistency[i]=consistency[i]+" "+getString(R.string.medium);
            if (Integer.parseInt(sp.getString("hard","86"))==coreTemperature[i]) consistency[i]=consistency[i]+" "+getString(R.string.hard);
        }

        Spinner spinnerEggSize = (Spinner) findViewById(R.id.spinnerSize);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, eggSize);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEggSize.setAdapter(adapter);
        spinnerEggSize.setSelection(sp.getInt("eggsize",1));
        spinnerEggSize.setOnItemSelectedListener(this);

        Spinner spinnerFridgeTemp = (Spinner) findViewById(R.id.spinnerFridge);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, fridgeTemperature);

        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFridgeTemp.setAdapter(adapter2);
        spinnerFridgeTemp.setSelection(sp.getInt("fridgetemp",3));
        spinnerFridgeTemp.setOnItemSelectedListener(this);


        Spinner spinnerTarget = (Spinner) findViewById(R.id.spinnerConsistency);
        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, consistency);

        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTarget.setAdapter(adapter3);
        spinnerTarget.setSelection(sp.getInt("consistency",2));
        spinnerTarget.setOnItemSelectedListener(this);

        if (Barometer.hasSensor(context)) {
            Barometer.requestPressure(context,altitudeTextView);
        } else {
            Location.requestLocation(context,altitudeTextView);
        }

        if (!counterIsActive && !countUpTimerIsActive) controllerButton.setText(getString(R.string.start));
        else if (countUpTimerIsActive)controllerButton.setText(getString(R.string.stopAlarm));
        else controllerButton.setText(getString(R.string.stop));

    }

    @Override
    public void onBackPressed(){
        if(!counterIsActive) super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Barometer.hasSensor(context)){
            Location.checkLocationProvider(this);
        }
        initViews();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Location.stopLocation(context);
        Barometer.stopPressureSensor(context);
    }

    @Override
    protected void onDestroy(){
        cancelAlarm();
        if (countDownTimer!=null) countDownTimer.cancel();
        Notification.cancelNotification(this);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Barometer.hasSensor(context)){
            Location.checkLocationPermission(this);
        }

        checkNotificationsPermission();
    }

    public void resetTimer() {
        timerTextView.setText("--:--");
        if (countUpTimer!=null) countUpTimer.cancel();
        countDownTimer.cancel();
        cancelAlarm();
        controllerButton.setText(getString(R.string.start));
        timerTextView.setTextColor(ContextCompat.getColor(context,R.color.teal_700));
        counterIsActive = false;
        countUpTimerIsActive = false;
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public void updateTimer(int secondsLeft) {
        int minutes = Math.abs(secondsLeft) / 60;
        int seconds = Math.abs(secondsLeft) - minutes * 60;

        String timeRemaining = String.format("%02d", minutes) + ":" + String.format("%02d", seconds);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // This code will always run on the UI thread, therefore is safe to modify UI elements.
                if (secondsLeft>=0){
                    timerTextView.setText(timeRemaining);
                    timerTextView.setTextColor(ContextCompat.getColor(context,R.color.teal_700));
                }else{
                    timerTextView.setText("-"+timeRemaining);
                    timerTextView.setTextColor(ContextCompat.getColor(context,R.color.red));
                }

            }
        });
        if (secondsLeft>=0) Notification.showNotification(context,timeRemaining);
    }

    public void controlTimer(View view) {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if  ((AlarmReceiver.isRingtoneActive()) || (!AlarmReceiver.isRingtoneActive() && controllerButton.getText()==getString(R.string.stopAlarm))) {
            resetTimer();
            AlarmReceiver.stopAlarmSound();
            Notification.cancelNotification(context);
            GithubStar.starDialog(context);
        } else if (!counterIsActive) {
            AlarmReceiver.stopAlarmSound(); //just to be sure: stop old Mediaplayer
            double timeInMillis;
            double tBoil;
            if (Barometer.hasSensor(context)){
                tBoil = Barometer.getBoilingTemp();
            } else{
                Location.requestLocation(context,altitudeTextView);
                tBoil = Location.getBoilingTemp();
            }

            if ((tBoil - tTarget) < 1) {   //check if boiling temperature is too low (should be at least 1K higher than target temperature)
                mediaPlayer=MediaPlayer.create(context,R.raw.buzzer);
                Toast.makeText(this,getString(R.string.error_boiling_temperature), Toast.LENGTH_LONG).show();
                mediaPlayer.setVolume(1, 1);
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(mp -> {
                    mp.reset();
                    mp.release();
                });
            } else {
                counterIsActive = true;
                controllerButton.setText(getString(R.string.stop));
                timeInMillis = (0.451 * Math.pow(weight, 2.0f / 3.0f) * Math.log(0.76 * (tBoil - tFridge) / (tBoil - tTarget))) * 60 * 1000;
                setAlarm((long) timeInMillis);

                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                countDownTimer = new CountDownTimer((long) timeInMillis, 1000) {

                    @Override
                    public void onTick(long l) {
                        updateTimer((int) l / 1000);

                        mediaPlayer = MediaPlayer.create(context, R.raw.click);
                        if (l > 11000) {
                            mediaPlayer.setVolume(0.05f, 0.05f);
                        } else {
                            mediaPlayer.setVolume(1, 1);
                        }
                        mediaPlayer.start();
                        mediaPlayer.setOnCompletionListener(mp -> {
                            mp.reset();
                            mp.release();
                        });
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //reactivate SCREEN_ON while timer is running, when e.g. switching back from other app
                    }

                    @Override
                    public void onFinish() {
                        Notification.showNotification(context, "00:00");
                        counterIsActive = false;
                        countUpTimerIsActive = true;
                        AlarmReceiver.playAlarmSound(context);
                        controllerButton.setText(getString(R.string.stopAlarm));

                        TimerTask timerTask;
                        countUpTimer = new Timer();
                        timerTask = new TimerTask() {
                            int num = 0;

                            @Override
                            public void run() {
                                num--;
                                updateTimer(num);
                            }
                        };
                        countUpTimer.schedule(timerTask, 0, 1000);
                    }
                }.start();
            }
        } else {
            resetTimer();
            Notification.cancelNotification(context);
        }
    }

    private void setAlarm(long timeInMillis) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(context,0,intent,0);
        }

        //SCHEDULE_EXACT_ALARM: on Android 12 this permission is automatically granted by the Android system but on Android 13 we need to check if the user has granted this permission.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent2 = new Intent();
                intent2.setAction(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent2);
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M){
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeInMillis, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeInMillis, pendingIntent);
        }

    }

    private void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(context,0,intent,0);
        }
        alarmManager.cancel(pendingIntent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        if (parent==findViewById(R.id.spinnerSize)) {
            editor.putInt("eggsize",position);
            editor.apply();
            weight = eggWeight[position];
        }else if (parent==findViewById(R.id.spinnerFridge)) {
            editor.putInt("fridgetemp",position);
            editor.apply();
            tFridge = fridgeTemperatureVal[position];
        }else if (parent==findViewById(R.id.spinnerConsistency)) {
            editor.putInt("consistency",position);
            editor.apply();
            tTarget=coreTemperature[position];
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void showTutorial(View view) {
        Intent intent = new Intent(this, Tutorial.class);
        startActivity(intent);
    }

    public void startSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void setAltitude(View view) {
        if (!Barometer.hasSensor(context)) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(context.getString(R.string.dialog_Enter_altitude));
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setRawInputType(Configuration.KEYBOARD_12KEY);
            alert.setView(input);
            alert.setPositiveButton(context.getString(R.string.dialog_OK_button), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    int altitude = Integer.parseInt(input.getText().toString());
                    Location.setAltitude(context, altitude);
                    altitudeTextView.setText(altitude + "\u2009m");
                }
            });
            alert.setNegativeButton(context.getString(R.string.dialog_Cancel_button), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //Put actions for CANCEL button here, or leave in blank
                }
            });
            alert.show();
        }
    }

    private void checkNotificationsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (Notification.isPermissionGranted(this)) {
                initNotifications();
            } else {
                Notification.requestPermission(this);
            }
        } else {
            initNotifications();
        }
    }

    private void initNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.initNotification(context);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Notification.PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initNotifications();
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
