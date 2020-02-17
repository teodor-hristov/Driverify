/*
This class is for saving data
 */

package com.demotxt.droidsrce.homedashboard.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.demotxt.droidsrce.homedashboard.R;
import com.demotxt.droidsrce.homedashboard.Utils.Constants;
import com.demotxt.droidsrce.homedashboard.io.CSVWriter;
import com.demotxt.droidsrce.homedashboard.io.ObdReaderData;

import java.io.IOException;
import java.sql.Timestamp;

public class DataController extends Service {
    private final String TAG = "DataController";
    private Timestamp timestamp;
    private CSVWriter bluetoothWriter;
    private CSVWriter locationWriter;
    private IntentFilter filter;
    private String[] actions = {
            Constants.connected,
            Constants.disconnected,
            Constants.receiveData,
            Constants.extra,
            Constants.GPSDisabled,
            Constants.GPSEnabled,
            Constants.GPSLiveData,
            Constants.GPSPutExtra
    };

    private int valueCounter = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private BroadcastReceiver liveDataReceiever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            ObdReaderData data;

            if (action.equals(Constants.disconnected)) {
                connectionLost(intent);
            }
            if (action.equals(Constants.GPSEnabled)) {
                locationEnabled();
            }
            if (action.equals(Constants.GPSDisabled)) {
                locationDisabled();
            }
            if (action.equals(Constants.receiveData)) {
                data = intent.getParcelableExtra(Constants.receiveData);
                handleBluetoothLiveData(data);
            }
            if (action.equals(Constants.GPSLiveData)) {
                if (intent.hasExtra(Constants.GPSPutExtra)) {
                    handleLocationLiveData(intent);
                }
            }

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        /**
         * TODO: Create files, write in files...
         */
        filter = new IntentFilter();
        filterAddActions(filter, actions);
        registerReceiver(liveDataReceiever, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /**
         * TODO: Close all writers, save all states, break connections
         */
        unregisterReceiver(liveDataReceiever);
        try {
            bluetoothWriter.close();
            //locationWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connectionLost(Intent intent) {
        String connectionStatusMsg = intent.getStringExtra(Constants.extra);
        Log.i(TAG, "Connection lost.");
        if (connectionStatusMsg.equals(getString(R.string.connect_lost))) {
            try {
                if (bluetoothWriter != null)
                    bluetoothWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "Problem with file close");
            }
        }
    }

    private void handleBluetoothLiveData(ObdReaderData data) {
        Log.i(TAG, "Handle bt data.");
        timestamp = new Timestamp(System.currentTimeMillis());
        StringBuilder sb;
        try {
            if (bluetoothWriter == null) {
                bluetoothWriter = new CSVWriter(Constants.DataLogPath);
                bluetoothWriter.append(Constants.obdDataCSVHeader);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "Writer initialization problem.");
            Log.i(TAG, e.getMessage());
        }
        if (data != null) {
            sb = new StringBuilder();
            if (bluetoothWriter != null) {
                try {
                    for (String str : data.getCommands()) {
                        sb.append(str);
                        sb.append(" ");
                    }
                    sb.append(timestamp.getTime());
                    bluetoothWriter.append(sb.toString());
                    autoSave(bluetoothWriter);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i(TAG, "Could not write to file");
                }
            }
        }
    }

    private void handleLocationLiveData(Intent intent) {
        StringBuilder sb;
        try {
            if (locationWriter == null) {
                locationWriter = new CSVWriter(Constants.DataLogPath + "/Location/");
                locationWriter.append("latitude longitude");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "Problem with writing location.");
        }
        if (intent.getStringExtra(Constants.GPSPutExtra) != null) {
            Log.i(TAG, "" + intent.getStringExtra(Constants.GPSPutExtra));
            sb = new StringBuilder();
            try {
                for (String str : intent.getStringExtra(Constants.GPSPutExtra).split(" ")) {
                    sb.append(str);
                    sb.append(" ");
                }
                if (sb != null) {
                    locationWriter.append(sb.toString());
                }
                autoSave(locationWriter);
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "Could not write to file");
            }
        }
    }

    private void locationDisabled() {
        Log.i(TAG, "Location disabled");
        try {
            if (locationWriter != null)
                locationWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void locationEnabled() {
        try {
            locationWriter = new CSVWriter(Constants.DataLogPath + "Locations/");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void filterAddActions(IntentFilter filter, String[] actions) {
        for (String item : actions)
            filter.addAction(item);
    }

    private void autoSave(CSVWriter fileWriter) throws IOException {
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        if (ts.getSeconds() % Constants.saveSeconds == 0) {
            fileWriter.flush();
        }
    }
}
