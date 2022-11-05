package org.woheller69.eggtimer;

import static java.lang.Boolean.TRUE;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).registerOnSharedPreferenceChangeListener(this);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            Preference reset = getPreferenceManager().findPreference("reset");
            if (reset != null) reset.setOnPreferenceClickListener(preference -> {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

                sharedPreferences.edit().remove("soft").apply();
                sharedPreferences.edit().remove("medium").apply();
                sharedPreferences.edit().remove("hard").apply();
                sharedPreferences.edit().remove("xs_name").apply();
                sharedPreferences.edit().remove("xs_weight").apply();
                sharedPreferences.edit().remove("s_name").apply();
                sharedPreferences.edit().remove("s_weight").apply();
                sharedPreferences.edit().remove("m_name").apply();
                sharedPreferences.edit().remove("m_weight").apply();
                sharedPreferences.edit().remove("l_name").apply();
                sharedPreferences.edit().remove("l_weight").apply();
                sharedPreferences.edit().remove("xl_name").apply();
                sharedPreferences.edit().remove("xl_weight").apply();

                onCreatePreferences(savedInstanceState,rootKey);
                return false;
            });
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals("useGPS")) {
            if (sharedPreferences.getBoolean(s, true) == TRUE) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,}, 1);
                }
            }
        }
    }

}