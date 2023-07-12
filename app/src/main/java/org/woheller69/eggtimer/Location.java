package org.woheller69.eggtimer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

public class Location {

    private static LocationListener locationListenerGPS;
    private static int altitude=0;

    public static int getAltitude() {
        return altitude;
    }

    public static double getBoilingTemp() {
        return 100 - getAltitude() * 0.003354;
    }
    static void stopLocation(Context context){
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationListenerGPS!=null) locationManager.removeUpdates(locationListenerGPS);
        locationListenerGPS=null;
    }

    static void requestLocation(Context context, TextView altitudeTextView) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        altitude=sp.getInt("altitude",0);
        if (sp.getBoolean("useGPS",true) && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            if (locationListenerGPS==null) locationListenerGPS = new LocationListener() {
                @Override
                public void onLocationChanged(android.location.Location location) {
                    altitude = (int) location.getAltitude();
                    sp.edit().putInt("altitude",altitude).apply();
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
            android.location.Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationGPS != null) {
                altitude = (int) locationGPS.getAltitude();
                sp.edit().putInt("altitude",altitude).apply();
                altitudeTextView.setText(altitude + "\u2009" + context.getString(R.string.unit_m));
                altitudeTextView.setTextColor(MainActivity.getThemeColor(context,R.attr.colorOnPrimaryContainer));
            }else {
                Toast.makeText(context.getApplicationContext(),context.getString(R.string.noAltitude),Toast.LENGTH_LONG).show();
                altitudeTextView.setText(altitude + "\u2009" + context.getString(R.string.unit_m));
                altitudeTextView.setTextColor(ContextCompat.getColor(context,R.color.grey));
            }
        }else {
            if (sp.getBoolean("useGPS",true)) Toast.makeText(context.getApplicationContext(),context.getString(R.string.noAltitude)+"\n"+context.getString(R.string.noGPS),Toast.LENGTH_LONG).show();
            altitudeTextView.setText(altitude + "\u2009" + context.getString(R.string.unit_m));
            altitudeTextView.setTextColor(ContextCompat.getColor(context,R.color.grey));
        }
    }

    public static void setAltitude(Context context, int altitude) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Location.altitude = altitude;
        sp.edit().putInt("altitude", Location.altitude).apply();
    }

    public static void checkLocationProvider(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (sp.getBoolean("useGPS",true)){
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                Toast.makeText(context, context.getString(R.string.error_no_gps), Toast.LENGTH_SHORT).show();
            }

        }
    }

    public static void checkLocationPermission(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (sp.getBoolean("useGPS",true) && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

}
