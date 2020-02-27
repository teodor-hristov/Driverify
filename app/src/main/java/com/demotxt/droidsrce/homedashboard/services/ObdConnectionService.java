package com.demotxt.droidsrce.homedashboard.services;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.demotxt.droidsrce.homedashboard.Drive;
import com.demotxt.droidsrce.homedashboard.R;
import com.demotxt.droidsrce.homedashboard.Utils.Constants;
import com.demotxt.droidsrce.homedashboard.io.BluetoothConnectionIO;
import com.demotxt.droidsrce.homedashboard.io.ObdReaderData;
import com.demotxt.droidsrce.homedashboard.settings.Preferences;
import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.LoadCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.EngineCoolantTemperatureCommand;
import com.github.pires.obd.enums.ObdProtocols;
import com.github.pires.obd.exceptions.UnableToConnectException;
import com.github.pires.obd.exceptions.UnsupportedCommandException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public class ObdConnectionService extends IntentService {
    private static final String TAG = ObdConnectionService.class.getName();

    private ArrayList<ObdCommand> cmds;
    private BluetoothSocket sock;

    public ObdConnectionService() {
        super(ObdConnectionService.class.getName());
    }

    public ArrayList<ObdCommand> getCmds() {
        return cmds;
    }

    public void setCmds(ArrayList<ObdCommand> cmds) {
        if (cmds != null && cmds.size() > 0)
            this.cmds = cmds;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "ObdConnectionService is shutting down..");
        makeToast("Bluetooth connection is broken.");
        if (sock != null) {
            try {
                sock.close();
            } catch (IOException e) {
                e.printStackTrace();
                makeToast("There is an error while destroying the connection.");
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        ArrayList<String> stringCommands = new ArrayList<>();
        ArrayList<String> stringDtc = new ArrayList<>();
        ArrayList<ObdCommand> commands = new ArrayList<>(Arrays.asList(
                new RPMCommand(),
                new SpeedCommand(),
                new EngineCoolantTemperatureCommand(),
                new LoadCommand()));
        setCmds(commands);

        Log.i(TAG, "ObdConnectionService service started");
        Log.i(TAG, "Thread id: " + Thread.currentThread().getId());

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int sleepPrefs = Integer.parseInt(Objects.requireNonNull(prefs.getString(Preferences.UPDATE_PERIOD, "-1")));
        sock = null;
        ObdReaderData data = new ObdReaderData(stringCommands);
        ObdReaderData dtc = new ObdReaderData(stringDtc);
        Intent intentToBroadcastReceiver = new Intent();

        BluetoothDevice bluetoothDevice = updateSelectedDevice(btAdapter, prefs);

        try {
            if (bluetoothDevice != null) {
                Log.i(TAG, "Entering communication test...");
                sock = new BluetoothConnectionIO(bluetoothDevice).connect();
                sock.connect();
                if (sock.isConnected()) {
                    Log.i(TAG, "Connection is now created.");
                    Log.i(TAG, "Testing OBD commands...");
                    new EchoOffCommand().run(sock.getInputStream(), sock.getOutputStream());
                    new LineFeedOffCommand().run(sock.getInputStream(), sock.getOutputStream());
                    new TimeoutCommand(125).run(sock.getInputStream(), sock.getOutputStream());
                    new SelectProtocolCommand(ObdProtocols.AUTO).run(sock.getInputStream(), sock.getOutputStream());
                    Log.i(TAG, "Commands are working and getting data...");
                    intentToBroadcastReceiver.setAction(Constants.CONNECTED);
                    intentToBroadcastReceiver.putExtra(Constants.EXTRA, getString(R.string.connected_ok));
                    sendBroadcast(intentToBroadcastReceiver);
                    makeToast("Data extraction is working.");
                }

            } else {
                Log.i(TAG, "bluetoothDevice null");
                makeToast("The device you selected is not responding to our connection.");
                intentToBroadcastReceiver.setAction(Constants.CONNECTED);
                intentToBroadcastReceiver.putExtra(Constants.EXTRA, getString(R.string.connect_lost));
                sendBroadcast(intentToBroadcastReceiver);
            }

        } catch (IOException e) {
            Log.i(TAG, "There is an error with connecting to device.");
            makeToast("There is an error with connecting to device.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.i(TAG, e.getMessage());
        } catch (TimeoutException e) {
            e.printStackTrace();
            Log.e(TAG, "Not responding");
        } catch (UnableToConnectException e) {
            makeToast("Car ECU is not responding. You need running engine.");
            e.printStackTrace();
        } catch (UnsupportedCommandException e) {
            e.printStackTrace();
            makeToast("Unsupported command.");
        }

        boolean prereq = bluetoothDevice != null && sock != null && sock.isConnected();
        while (prereq) {
            try {
                Thread.sleep(sleepPrefs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (sock.isConnected()) {
                updateData(sock, cmds, stringCommands);
                printToIntent(cmds, stringCommands, data, intentToBroadcastReceiver.setAction(Constants.RECEIVE_DATA), Constants.RECEIVE_DATA);

            } else {
                Log.i(TAG, "No connection");
                intentToBroadcastReceiver.setAction(Constants.CONNECTED);
                intentToBroadcastReceiver.putExtra(Constants.EXTRA, Constants.DISCONNECTED);
                sendBroadcast(intentToBroadcastReceiver);
                try {
                    sock = new BluetoothConnectionIO(bluetoothDevice).connect();
                    sock.connect();
                } catch (IOException e) {
                    e.printStackTrace();
                    makeToast("There is an error with connecting to device.");
                } catch (TimeoutException e) {
                    e.printStackTrace();
                    makeToast("Connection timeout.");
                }
            }
            stringCommands.clear();
            prereq = bluetoothDevice != null && sock != null && sock.isConnected() && btAdapter.isEnabled();
        }
    }

    public void makeToast(String txt) {
        Toast.makeText(Drive.getAppContext(), txt, Toast.LENGTH_SHORT).show();
    }

    public void updateData(BluetoothSocket sock, ArrayList<ObdCommand> cmds, ArrayList<String> stringCommands) {
        if (cmds.size() > 0) {
            for (ObdCommand var : cmds) {
                try {
                    var.run(sock.getInputStream(), sock.getOutputStream());
                    stringCommands.add(var.getCalculatedResult());
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                } catch (UnsupportedCommandException e) {
                    e.printStackTrace();
                    makeToast("Unsupported command" + var.getName());
                } catch (UnableToConnectException e) {
                    e.printStackTrace();
                    makeToast("Unable to connect." + var.getName());
                }
            }
        }
    }

    public BluetoothDevice updateSelectedDevice(BluetoothAdapter btAdapter, SharedPreferences prefs) {
        if (btAdapter != null) {
            for (BluetoothDevice dev : btAdapter.getBondedDevices()) {
                if (dev.getAddress().equals(prefs.getString(Preferences.BLUETOOTH_LIST_KEY, "-1"))) {
                    return dev;
                }
            }
        }
        return null;
    }


    public void printToIntent(ArrayList<ObdCommand> cmds, ArrayList<String> stringCommands, ObdReaderData data, Intent intent, String receiveData) {
        if (cmds.size() > 0) {
            intent.setAction(receiveData);
            data.setCommands(stringCommands);
            intent.putExtra(receiveData, data);

            sendBroadcast(intent);
        }
    }
}
