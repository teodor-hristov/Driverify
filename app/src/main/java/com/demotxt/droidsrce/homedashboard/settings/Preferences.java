package com.demotxt.droidsrce.homedashboard.settings;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.demotxt.droidsrce.homedashboard.R;

import java.util.ArrayList;
import java.util.Set;

public class Preferences extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
    

    public static final String BLUETOOTH_LIST_KEY = "bluetooth_list_preference";
    public static final String BLUETOOTH_ENABLE = "enable_bluetooth_preference";

    private Preference bt;
    private BluetoothAdapter mBtAdapter;
    private final Activity thisActivity = this;
    private Set<BluetoothDevice> pairedDevices;
    private ListPreference listBtDevices;
    private Preference mPreferenceCheckBt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        mPreferenceCheckBt = findPreference(BLUETOOTH_ENABLE);

        /*
         * Read preferences resources available at res/xml/preferences.xml
         */
        ArrayList<CharSequence> pairedDeviceStrings = new ArrayList<>();
        ArrayList<CharSequence> vals = new ArrayList<>();
        listBtDevices= (ListPreference) getPreferenceScreen()
                .findPreference(BLUETOOTH_LIST_KEY);

        /*
         * Select device to connect
         */
        if (mBtAdapter == null) {
            listBtDevices
                    .setEntries(pairedDeviceStrings.toArray(new CharSequence[0]));
            listBtDevices.setEntryValues(vals.toArray(new CharSequence[0]));

            // we shouldn't get here, still warn user
            Toast.makeText(this, "This device does not support Bluetooth.",
                    Toast.LENGTH_LONG).show();

            return;
        }
        /*
         * Listen for preferences click.
         *
         * TODO there are so many repeated validations :-/
         */

        listBtDevices.setEntries(new CharSequence[1]);
        listBtDevices.setEntryValues(new CharSequence[1]);
        listBtDevices.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                // see what I mean in the previous comment?
                if (mBtAdapter == null || !mBtAdapter.isEnabled()) {
                    Toast.makeText(thisActivity,
                            "This device does not support Bluetooth or it is disabled.",
                            Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }
        });

        /*
         * Get paired devices and populate preference list.
         */
        pairedDevices = mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDeviceStrings.add(device.getName() + "\n" + device.getAddress());
                vals.add(device.getAddress());
            }
        }
        listBtDevices.setEntries(pairedDeviceStrings.toArray(new CharSequence[0]));
        listBtDevices.setEntryValues(vals.toArray(new CharSequence[0]));

        /*
         * Check if bt enable/disable state is changed
         */
        if(mBtAdapter != null){
            mPreferenceCheckBt.setOnPreferenceClickListener(clickListener);
        }

    }
        Preference.OnPreferenceClickListener clickListener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(preference.getKey().equals(BLUETOOTH_ENABLE)){
                    if(preference.isEnabled()){
                         mBtAdapter.enable();
                        Log.i(TAG, "onPreferenceClick: ");
                    }else{
                         mBtAdapter.disable();
                    }
                }
                return false;
            }
        };

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        return false;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
