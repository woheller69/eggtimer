package org.woheller69.eggtimer;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.google.android.material.color.DynamicColors;

public class Eggtimer extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp.getBoolean("useDynamicColors", false)) {
            DynamicColors.applyToActivitiesIfAvailable(this);
        }
    }
}
