package org.woheller69.eggtimer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import androidx.preference.PreferenceManager;
import android.view.View;

import android.os.CountDownTimer;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private final int SOFT_EGG = 65;
    private final int MEDIUM_EGG = 71;
    private final int HARD_EGG = 85;
    private final int S_EGG = 50;  //<53g
    private final int M_EGG = 58;  //53...63g
    private final int L_EGG = 68;  //63...73g
    private final int XL_EGG = 76; //>73g
    private static final String[] eggSize = {"S", "M", "L", "XL"};
    private static final String[] fridgeTemperature = {"6°C", "8°C", "10°C", "12°C", "20°C"};


    private TextView timerTextView;
    private Button controllerButton;
    private Boolean counterIsActive = false;
    private Boolean ringtoneIsActive = false;
    private CountDownTimer countDownTimer;

    private int weight;
    private int altitude=0;
    private int tFridge=10;
    private int tTarget=65;
    private MediaPlayer player;
    private LocationListener locationListenerGPS;

    @Override
    protected void onResume() {
        super.onResume();
        requestLocation();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] consistency = getResources().getStringArray(R.array.consistency);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        initNotification();

        setContentView(R.layout.activity_main);

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

        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFridgeTemp.setAdapter(adapter2);
        spinnerFridgeTemp.setSelection(2);
        spinnerFridgeTemp.setOnItemSelectedListener(this);


        Spinner spinnerTarget = (Spinner) findViewById(R.id.spinnerConsistency);
        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_spinner_item, consistency);

        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTarget.setAdapter(adapter3);
        spinnerTarget.setSelection(0);
        spinnerTarget.setOnItemSelectedListener(this);

    }

    public void resetTimer() {
        timerTextView.setText("--:--");
        countDownTimer.cancel();
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
        showNotification(timeRemaining);
    }

    public void controlTimer(View view) {
        if (ringtoneIsActive) {
            player.stop();
            cancelNotification();
            starDialog();
            ringtoneIsActive=false;
            controllerButton.setText(getString(R.string.start));
        } else if (!counterIsActive) {

            counterIsActive = true;
            controllerButton.setText(getString(R.string.stop));
            double timeInMillis;
            requestLocation();

            timeInMillis = (0.451*Math.pow(weight,2.0f/3.0f)*Math.log(0.76*(100-altitude*0.003354-tFridge)/(100-altitude*0.003354-tTarget)))*60*1000;

            countDownTimer = new CountDownTimer((long) timeInMillis, 1000) {

                @Override
                public void onTick(long l) {
                    updateTimer((int) l / 1000);
                }

                @Override
                public void onFinish() {
                    showNotification("00:00");
                    resetTimer();
                    ringtone();
                }
            }.start();
        } else {
            resetTimer();
            cancelNotification();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "EggTimer";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "EggTimer", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void cancelNotification() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(1);
    }

    private void showNotification(String timeRemaining) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pIntent = PendingIntent.getActivity(this,0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"EggTimer")
                .setSmallIcon(R.drawable.egg_timer_transparent)
                .setContentTitle(getString(R.string.cookingtime)).setContentText(timeRemaining).setSilent(true).setContentIntent(pIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1,builder.build());
    }


    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            locationListenerGPS = new LocationListener() {
                @Override
                public void onLocationChanged(android.location.Location location) {
                    altitude= (int) location.getAltitude();
                }

                @Deprecated
                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            };

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, locationListenerGPS);
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationGPS != null) {
                altitude = (int) locationGPS.getAltitude();
                TextView altitudeTextView = findViewById(R.id.altitude);
                altitudeTextView.setText(altitude +"m");
            }else Toast.makeText(this.getApplicationContext(),getString(R.string.noPosition),Toast.LENGTH_SHORT).show();
        }
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

    public void ringtone(){
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            ringtoneIsActive = true;
            controllerButton.setText(getString(R.string.stopAlarm));
            player = MediaPlayer.create(this, notification);
            player.setLooping(true);
            player.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void starDialog(){
        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefManager.getBoolean("askForStar",true))    {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage(R.string.dialog_StarOnGitHub);
            alertDialogBuilder.setPositiveButton(getString(R.string.dialog_OK_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/woheller69/eggtimer")));
                    setAskForStar(false);
                }
            });
            alertDialogBuilder.setNegativeButton(getString(R.string.dialog_NO_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setAskForStar(false);
                }
            });
            alertDialogBuilder.setNeutralButton(getString(R.string.dialog_Later_button), null);

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    public void setAskForStar(boolean askForStar){
        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefManager.edit();
        editor.putBoolean("askForStar", askForStar);
        editor.apply();
    }

}



