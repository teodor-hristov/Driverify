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
import java.text.SimpleDateFormat;

public class DataController extends Service {
    private final String TAG = "DataController";
    private CSVWriter bluetoothWriter;
    private CSVWriter locationWriter;
    private String[] actions = {
            Constants.CONNECTED,
            Constants.DISCONNECTED,
            Constants.RECEIVE_DATA,
            Constants.EXTRA,
            Constants.GPS_DISABLED,
            Constants.GPS_ENABLED,
            Constants.GPS_LIVE_DATA,
            Constants.GPS_PUT_EXTRA
    };
    private SimpleDateFormat formatter;

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
            if (action == null)
                return;

            switch (action) {
                case Constants.DISCONNECTED:
                    connectionLost(intent);
                    break;
                case Constants.GPS_ENABLED:
                    locationEnabled();
                    Log.i(TAG, "Enable");
                    break;
                case Constants.GPS_DISABLED:
                    locationDisabled();
                    Log.i(TAG, "Disable");
                    break;
                case Constants.GPS_LIVE_DATA:
                    if (intent.hasExtra(Constants.GPS_PUT_EXTRA)) {
                        handleLocationLiveData(intent);
                    }
                    break;
                case Constants.RECEIVE_DATA:
                    data = intent.getParcelableExtra(Constants.RECEIVE_DATA);
                    handleBluetoothLiveData(data);
                    break;

            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filterAddActions(filter, actions);
        registerReceiver(liveDataReceiever, filter);
        formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "DataController is shutting down..");
        unregisterReceiver(liveDataReceiever);
        stopLocation();
        try {
            if (bluetoothWriter != null && locationWriter != null) {
                bluetoothWriter.close();
                locationWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connectionLost(Intent intent) {
        String connectionStatusMsg = intent.getStringExtra(Constants.EXTRA);
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
        StringBuilder sb;
        try {
            if (bluetoothWriter == null) {
                bluetoothWriter = new CSVWriter(Constants.DATA_LOG_PATH);
                bluetoothWriter.append(Constants.OBD_DATA_HEADER_CSV);
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
                    sb.append(formatter.format(new java.util.Date().getTime()));
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
                locationWriter = new CSVWriter(Constants.DATA_LOG_PATH + "/Location/");
                locationWriter.append("latitude longitude timestamp");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "Problem with writing location.");
        }
        if (intent.getStringExtra(Constants.GPS_PUT_EXTRA) != null) {
            Log.i(TAG, "" + intent.getStringExtra(Constants.GPS_PUT_EXTRA));
            sb = new StringBuilder();
            try {
                for (String str : intent.getStringExtra(Constants.GPS_PUT_EXTRA).split(" ")) {
                    sb.append(str);
                    sb.append(" ");
                }
                if (sb != null) {
                    sb.append(formatter.format(new java.util.Date().getTime()));
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
            locationWriter = new CSVWriter(Constants.DATA_LOG_PATH + "Location/");
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
        if (ts.getSeconds() % Constants.SAVE_SECONDS == 0) {
            fileWriter.flush();
        }
    }

    private void stopLocation() {

    }
}
