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

    private static final String TAG = "Preferences";
    public static final String BLUETOOTH_LIST_KEY = "bluetooth_list_preference";
    public static final String BLUETOOTH_ENABLE = "enable_bluetooth_preference";

    private Preference bt;
    private BluetoothAdapter mBtAdapter;
    private final Activity thisActivity = this;
    private Set<BluetoothDevice> pairedDevices;
    private ListPreference listBtDevices;
    private Preference mPreferenceCheckBt;
    private ArrayList<CharSequence> pairedDeviceStrings;
    private ArrayList<CharSequence> vals;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        mPreferenceCheckBt = findPreference(BLUETOOTH_ENABLE);

        /*
         * Read preferences resources available at res/xml/preferences.xml
         */
         pairedDeviceStrings = new ArrayList<>();
         vals = new ArrayList<>();
         listBtDevices= (ListPreference) getPreferenceScreen().findPreference(BLUETOOTH_LIST_KEY);
         listBtDevices.setEntries(pairedDeviceStrings.toArray(new CharSequence[0]));
         listBtDevices.setEntryValues(vals.toArray(new CharSequence[0]));


        /*
         * Select device to connect
         */
        if (mBtAdapter == null) {
            listBtDevices.setEntries(pairedDeviceStrings.toArray(new CharSequence[0]));
            listBtDevices.setEntryValues(vals.toArray(new CharSequence[0]));
            // we shouldn't get here, still warn user
            makeToast("This device does not support Bluetooth.");
            return;
        }
//        /*
//         * Listen for preferences click.
//         *
//         * TODO there are so many repeated validations :-/
//         */
//        listBtDevices.setEntries(new CharSequence[1]);
//        listBtDevices.setEntryValues(new CharSequence[1]);
//        listBtDevices.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            public boolean onPreferenceClick(Preference preference) {
//                // see what I mean in the previous comment?
//                if (mBtAdapter == null || !mBtAdapter.isEnabled()) {
//                    makeToast("This device does not support Bluetooth or it is disabled.");
//                    return false;
//                }
//                return true;
//            }
//        });

        /*
         * Get paired devices and populate preference list.
         * TODO: Double click problem
         */
        listBtDevices.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(mBtAdapter.isEnabled() && mBtAdapter != null){
                    //Clear old devices and setting it clear
                    if(vals != null && pairedDeviceStrings != null) {
                        vals.clear();
                        pairedDeviceStrings.clear();
                        listBtDevices.setEntries(pairedDeviceStrings.toArray(new CharSequence[0]));
                        listBtDevices.setEntryValues(vals.toArray(new CharSequence[0]));
                    }

                    pairedDevices = mBtAdapter.getBondedDevices();
                    if (pairedDevices.size() > 0) {
                        for (BluetoothDevice device : pairedDevices) {
                            pairedDeviceStrings.add(device.getName() + "\n" + device.getAddress());
                            vals.add(device.getAddress());
                        }
                    }
                    listBtDevices.setEntries(pairedDeviceStrings.toArray(new CharSequence[0]));
                    listBtDevices.setEntryValues(vals.toArray(new CharSequence[0]));
                }else{
                    //Clear old devices and setting it clear
                    if(vals != null && pairedDeviceStrings != null) {
                        vals.clear();
                        pairedDeviceStrings.clear();
                        listBtDevices.setEntries(pairedDeviceStrings.toArray(new CharSequence[0]));
                        listBtDevices.setEntryValues(vals.toArray(new CharSequence[0]));
                    }
                }
                return false;
            }
        });

        /*
         * Enable/Disable bt realtime
         */
        if(mBtAdapter != null){
            mPreferenceCheckBt.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if(preference.getKey().equals(BLUETOOTH_ENABLE)){
                        if(!mBtAdapter.isEnabled()){
                            if(mBtAdapter.enable()) {
                                Log.i(TAG, "onPreferenceClick: enable");
                                makeToast("Bluetooth is enabled!");
                            }else{
                                makeToast("There was an error.");
                            }

                        }else{
                            if(mBtAdapter.disable()) {
                                Log.i(TAG, "onPreferenceClick: disable");
                                makeToast("Bluetooth is disabled!");
                            }else{
                                makeToast("There was an error.");
                            }
                        }
                    }
                    return false;
                }
            });
        }

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        return false;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
    public void makeToast(String txt){
        Toast.makeText(thisActivity, txt, Toast.LENGTH_SHORT).show();
    }
}
