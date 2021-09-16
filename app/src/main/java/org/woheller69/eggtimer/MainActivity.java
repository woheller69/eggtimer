package org.woheller69.eggtimer;

import android.Manifest;
import android.annotation.SuppressLint;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;

import android.content.Intent;
import android.content.pm.PackageManager;

import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.view.View;

import android.os.CountDownTimer;

import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private final int SOFT_EGG = 66;
    private final int MEDIUM_EGG = 71;
    private final int HARD_EGG = 85;
    private final int S_EGG = 48;  //<53g
    private final int M_EGG = 58;  //53...63g
    private final int L_EGG = 68;  //63...73g
    private final int XL_EGG = 76; //>73g
    private static final String[] eggSize = {"S", "M", "L", "XL"};
    private static final String[] fridgeTemperature = {"6°C", "8°C", "10°C", "12°C", "20°C"};


    private TextView timerTextView;
    private TextView altitudeTextView;
    private Button controllerButton;
    private Boolean counterIsActive = false;

    private CountDownTimer countDownTimer;

    private int weight;
    private int tFridge=10;
    private int tTarget=66;
    private final Context context = this;

    @Override
    protected void onResume() {
        super.onResume();
        Location.requestLocation(context,altitudeTextView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Location.stopLocation(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] consistency = getResources().getStringArray(R.array.consistency);

        checkLocationPermission();

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) Notification.initNotification(context);

        setContentView(R.layout.activity_main);

        timerTextView = (TextView) findViewById(R.id.timerTextView);
        altitudeTextView = findViewById(R.id.altitude);
        controllerButton = (Button) findViewById(R.id.controllerButton);

        Spinner spinnerEggSize = (Spinner) findViewById(R.id.spinnerSize);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, eggSize);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEggSize.setAdapter(adapter);
        spinnerEggSize.setSelection(1);
        spinnerEggSize.setOnItemSelectedListener(this);

        Spinner spinnerFridgeTemp = (Spinner) findViewById(R.id.spinnerFridge);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, fridgeTemperature);

        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFridgeTemp.setAdapter(adapter2);
        spinnerFridgeTemp.setSelection(2);
        spinnerFridgeTemp.setOnItemSelectedListener(this);


        Spinner spinnerTarget = (Spinner) findViewById(R.id.spinnerConsistency);
        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, consistency);

        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTarget.setAdapter(adapter3);
        spinnerTarget.setSelection(0);
        spinnerTarget.setOnItemSelectedListener(this);

    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    public void resetTimer() {
        timerTextView.setText("--:--");
        countDownTimer.cancel();
        cancelAlarm();
        controllerButton.setText(getString(R.string.start));
        timerTextView.setEnabled(true);
        counterIsActive = false;
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public void updateTimer(int secondsLeft) {
        int minutes = (int) secondsLeft / 60;
        int seconds = secondsLeft - minutes * 60;

        String timeRemaining = String.format("%02d", minutes)
                + ":" + String.format("%02d", seconds);

        timerTextView.setText(timeRemaining);
        Notification.showNotification(context,timeRemaining);
    }

    public void controlTimer(View view) {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (!AlarmReceiver.isRingtoneActive() && controllerButton.getText()==getString(R.string.stopAlarm)) {
            Notification.cancelNotification(context);
            GithubStar.starDialog(context);
            controllerButton.setText(getString(R.string.start));
        }else if (AlarmReceiver.isRingtoneActive()) {
            AlarmReceiver.stopAlarmSound();
            Notification.cancelNotification(context);
            GithubStar.starDialog(context);
            controllerButton.setText(getString(R.string.start));
        } else if (!counterIsActive) {
            AlarmReceiver.stopAlarmSound(); //just to be sure: stop old Mediaplayer
            counterIsActive = true;
            controllerButton.setText(getString(R.string.stop));
            double timeInMillis;
            Location.requestLocation(context,altitudeTextView);

            timeInMillis = (0.451*Math.pow(weight,2.0f/3.0f)*Math.log(0.76*(100- org.woheller69.eggtimer.Location.getAltitude() *0.003354-tFridge)/(100- org.woheller69.eggtimer.Location.getAltitude()*0.003354-tTarget)))*60*1000;

            setAlarm((long) timeInMillis);


            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            countDownTimer = new CountDownTimer((long) timeInMillis, 1000) {

                @Override
                public void onTick(long l) {
                    updateTimer((int) l / 1000);
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //reactivate SCREEN_ON while timer is running, when e.g. switching back from other app
                }

                @Override
                public void onFinish() {
                    Notification.showNotification(context,"00:00");
                    resetTimer();
                    AlarmReceiver.playAlarmSound(context);
                    controllerButton.setText(getString(R.string.stopAlarm));

                }
            }.start();
        } else {
            resetTimer();
            Notification.cancelNotification(context);
        }
    }

    private void setAlarm(long timeInMillis) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0,intent,0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeInMillis, pendingIntent);
    }

    private void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0,intent,0);
        alarmManager.cancel(pendingIntent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent==findViewById(R.id.spinnerSize)) {
            switch (position) {
                case 0:
                    weight=S_EGG;
                    break;
                case 1:
                    weight=M_EGG;
                    break;
                case 2:
                    weight=L_EGG;
                    break;
                case 3:
                    weight=XL_EGG;
                    break;
            }
        }else if (parent==findViewById(R.id.spinnerFridge)) {
            switch (position) {
                case 0:
                    tFridge = 6;
                    break;
                case 1:
                    tFridge = 8;
                    break;
                case 2:
                    tFridge = 10;
                    break;
                case 3:
                    tFridge = 12;
                    break;
                case 4:
                    tFridge = 20;
                    break;
            }
        }else if (parent==findViewById(R.id.spinnerConsistency)) {
            switch (position) {
                case 0:
                    tTarget=SOFT_EGG;
                    break;
                case 1:
                    tTarget=MEDIUM_EGG;
                    break;
                case 2:
                    tTarget=HARD_EGG;
                    break;
            }
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}



