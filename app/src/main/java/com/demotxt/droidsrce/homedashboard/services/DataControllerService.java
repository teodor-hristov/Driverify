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

import com.demotxt.droidsrce.homedashboard.Utils.Constants;
import com.demotxt.droidsrce.homedashboard.io.CSVWriter;
import com.demotxt.droidsrce.homedashboard.io.ObdReaderData;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class DataControllerService extends Service {
    private static final int ASSERTED_COMMANDS_COUNT = 4;
    private final String TAG = "DataControllerService";
    private CSVWriter bluetoothWriter, locationWriter, faceDataWriter;
    private String[] actions = {
            Constants.CONNECTED,
            Constants.DISCONNECTED,
            Constants.RECEIVE_DATA,
            Constants.EXTRA,
            Constants.GPS_DISABLED,
            Constants.GPS_ENABLED,
            Constants.GPS_LIVE_DATA,
            Constants.GPS_PUT_EXTRA,
            Constants.FACE_DATA
    };
    private SimpleDateFormat formatter;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private BroadcastReceiver liveDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String stringExtra = intent.getStringExtra(Constants.EXTRA);
            ObdReaderData data = intent.getParcelableExtra(Constants.RECEIVE_DATA);

            assert action != null;
            switch (action) {
                case Constants.DISCONNECTED:
                    connectionLost();
                    break;
                case Constants.GPS_ENABLED:
                    //locationEnabled();
                    break;
                case Constants.GPS_DISABLED:
                    locationDisabled();
                    break;
                case Constants.GPS_LIVE_DATA:
                    handleLocationLiveData(stringExtra);
                    break;
                case Constants.RECEIVE_DATA:
                    handleBluetoothLiveData(data);
                    break;
                case Constants.FACE_DATA:
                    handleFaceLiveData(stringExtra);
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filterAddActions(filter, actions);
        registerReceiver(liveDataReceiver, filter);
        formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "DataControllerService is shutting down..");
        unregisterReceiver(liveDataReceiver);
        stopLocation();
        try {
            closeWriters(bluetoothWriter, locationWriter, faceDataWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connectionLost() {
        Log.i(TAG, "Connection lost.");
            try {
                if (bluetoothWriter != null)
                    bluetoothWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "Problem with file close");
        }
    }

    private void handleBluetoothLiveData(ObdReaderData data) {
        StringBuilder sb = new StringBuilder();
        if (bluetoothWriter == null) {
            try {
                bluetoothWriter = new CSVWriter(Constants.DATA_LOG_PATH);
                bluetoothWriter.append(Constants.OBD_DATA_HEADER_CSV);
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "Writer initialization problem.");
                Log.i(TAG, e.getMessage());
            }
        } else {
            if (data == null || data.getCommands().size() < ASSERTED_COMMANDS_COUNT) {
                return;
            }
            Log.i(TAG, "Handle data");
            try {
                for (String str : data.getCommands()) {
                    sb.append(str);
                    sb.append(" ");
                }
                sb.append(System.currentTimeMillis());
                bluetoothWriter.append(sb.toString());
                autoSave(bluetoothWriter);
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "Could not write to file");
            }
        }
    }

    private void handleFaceLiveData(String data) {
        StringBuilder sb = new StringBuilder();
        if (data == null || data.split(" ").length != 2) {
            return;
        }

        if (faceDataWriter == null) {
            try {
                faceDataWriter = new CSVWriter(Constants.FACE_DATA_PATH);
                faceDataWriter.append(Constants.FACE_DATA_HEADER);
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "Problem with writing face data.");
            }
        } else {
            try {
                for (String str : data.split(" ")) {
                    sb.append(str);
                    sb.append(" ");
                }
                sb.append(System.currentTimeMillis());
                faceDataWriter.append(sb.toString());
                autoSave(faceDataWriter);
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "Could not write to file");
            }
        }
    }

    private void handleLocationLiveData(String gpsExtraString) {
        StringBuilder sb = new StringBuilder();
        if (gpsExtraString == null || gpsExtraString.split(" ").length < 2) {
            return;
        }

        if (locationWriter == null) {
            try {
                locationWriter = new CSVWriter(Constants.DATA_LOG_PATH + "/Location/");
                locationWriter.append("latitude longitude timestamp");
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "Problem with writing location.");
            }
        } else {
            try {
                for (String str : gpsExtraString.split(" ")) {
                    sb.append(str);
                    sb.append(" ");
                }

                sb.append(System.currentTimeMillis());
                locationWriter.append(sb.toString());
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

    private void closeWriters(CSVWriter... writers) throws IOException {
        for (CSVWriter writer : writers) {
            if (writer != null)
                writer.close();
        }
    }
}
