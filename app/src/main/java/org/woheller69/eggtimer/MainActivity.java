package org.woheller69.eggtimer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.view.View;

import android.os.CountDownTimer;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


    private TextView timerTextView;
    private Button controllerButton;
    private Boolean counterIsActive = false;
    private Boolean ringtoneIsActive = false;
    private CountDownTimer countDownTimer;
    private static final String[] eggSize = {"S", "M", "L", "XL"};
    private static final String[] fridgeTemperature = {"6°C", "8°C", "10°C", "12°C", "20°C"};
    private static final String[] target = {"weich", "mittel", "hart"};
    private float Diameter;
    private int altitude=0;
    private int tFridge;
    private int tTarget =65;
    private Ringtone ringtone;

    public void resetTimer() {

        timerTextView.setText("-:--");
        countDownTimer.cancel();
        controllerButton.setText("Start");
        timerTextView.setEnabled(true);
        counterIsActive = false;

    }

    @SuppressLint("SetTextI18n")
    public void updateTimer(int secondsLeft) {

        int minutes = (int) secondsLeft / 60;
        int seconds = secondsLeft - minutes * 60;

        String secondString = Integer.toString(seconds);

        if (seconds <= 9) {

            secondString = "0" + secondString;

        }

        timerTextView.setText(minutes + ":" + secondString);

    }

    public void controlTimer(View view) {

        if (ringtoneIsActive) {
            ringtone.stop();
            ringtoneIsActive=false;
            controllerButton.setText("Start");
        } else if (!counterIsActive) {

            counterIsActive = true;
            controllerButton.setText("Stop");
            double timeInMillis;
            requestLocation();

            timeInMillis= (0.15*Diameter*Diameter*Math.log(2*(100-altitude*0.003354-tFridge)/(100-altitude*0.003354- tTarget)))*60*1000;

            countDownTimer = new CountDownTimer((long) timeInMillis, 1000) {

                @Override
                public void onTick(long l) {
                    updateTimer((int) l / 1000);
                }

                @Override
                public void onFinish() {
                    resetTimer();
                    ringtone();
                }
            }.start();

        } else {

            resetTimer();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        setContentView(R.layout.activity_main);

        requestLocation();

        timerTextView = (TextView) findViewById(R.id.timerTextView);
        controllerButton = (Button) findViewById(R.id.controllerButton);

        Spinner spinnerEggSize = (Spinner) findViewById(R.id.spinnerSize);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_spinner_item, eggSize);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEggSize.setAdapter(adapter);
        spinnerEggSize.setSelection(1);
        spinnerEggSize.setOnItemSelectedListener(this);

        Spinner spinnerFridgeTemp = (Spinner) findViewById(R.id.spinnerFridge);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_spinner_item, fridgeTemperature);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFridgeTemp.setAdapter(adapter2);
        spinnerFridgeTemp.setSelection(2);
        spinnerFridgeTemp.setOnItemSelectedListener(this);


        Spinner spinnerTarget = (Spinner) findViewById(R.id.spinnerTarget);
        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_spinner_item, target);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTarget.setAdapter(adapter3);
        spinnerTarget.setSelection(0);
        spinnerTarget.setOnItemSelectedListener(this);

    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationGPS != null) {
                altitude= (int) locationGPS.getAltitude();
                TextView altitudeTextView = findViewById(R.id.altitude);
                altitudeTextView.setText(Integer.toString(altitude)+"m");
            }else Toast.makeText(this.getApplicationContext(),"Keine Position verfügbar",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent==findViewById(R.id.spinnerSize)) {
            switch (position) {
                case 0:
                    Diameter = 4.1f;
                    break;
                case 1:
                    Diameter = 4.225f;
                    break;
                case 2:
                    Diameter = 4.46f;
                    break;
                case 3:
                    Diameter = 4.83f;
                    break;
            }
        }else if (parent==findViewById(R.id.spinnerFridge)) {
            switch (position) {
                case 0:
                    tFridge=6;
                    break;
                case 1:
                    tFridge=8;
                    break;
                case 2:
                    tFridge=10;
                    break;
                case 3:
                    tFridge=12;
                    break;
                case 4:
                    tFridge=20;
                    break;
            }
        }else if (parent==findViewById(R.id.spinnerTarget)) {
            switch (position) {
                case 0:
                    tTarget =65;
                    break;
                case 1:
                    tTarget =71;
                    break;
                case 2:
                    tTarget =80;
                    break;
            }
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void ringtone(){
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
            ringtoneIsActive=true;
            controllerButton.setText("Stop Alarm");
            ringtone.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}



