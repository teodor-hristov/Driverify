package com.demotxt.droidsrce.homedashboard.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demotxt.droidsrce.homedashboard.R;

public class Preferences extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        return false;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
