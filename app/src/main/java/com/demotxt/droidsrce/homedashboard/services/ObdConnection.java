package com.demotxt.droidsrce.homedashboard.services;

import android.app.IntentService;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.demotxt.droidsrce.homedashboard.Drive;
import com.demotxt.droidsrce.homedashboard.io.BluetoothConnectionIO;
import com.demotxt.droidsrce.homedashboard.settings.Preferences;
import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.control.TroubleCodesCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class ObdConnection extends IntentService {
    private static final String TAG = ObdConnection.class.getName();
    public static final String connected = "BLUETOOTH_CONNECTED";
    public static final String disconnected = "BLUETOOTH_DISCONNECTED";
    public static final String receiveData = "OBD_DATA_RECEIVE";
    public static final String extra = "INTENT_EXTRA_DATA";

    private ArrayList<ObdCommand> cmds;
    private BluetoothAdapter btAdapter;
    private BluetoothDevice mBtDevice;
    private BluetoothSocket sock;

    private SharedPreferences prefs;

    public ObdConnection() {
        super(ObdConnection.class.getName());
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
        Log.i(TAG, "ObdConnection started...");
        sendBroadcast(new Intent(receiveData));
        Log.i(TAG, "ObdConnection service started");
        Log.i(TAG, "Thread id: " + Thread.currentThread().getId());
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sock = null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ok");
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

        ArrayList<ObdCommand> commands = new ArrayList<>();
        commands.add(new RPMCommand());
        commands.add(new SpeedCommand());
        //commands.add(new TroubleCodesCommand());
        setCmds(commands);

        if (btAdapter != null) {
            for (BluetoothDevice dev : btAdapter.getBondedDevices()) {
                if (dev.getAddress().equals(prefs.getString(Preferences.BLUETOOTH_LIST_KEY, "-1"))) {
                    mBtDevice = dev;
                    break;
                }
            }
        }

        try {
            if (mBtDevice != null) {
                Log.i(TAG, "Entering communication test...");
                sock = new BluetoothConnectionIO(mBtDevice).connect();
                sock.connect();
                if (sock.isConnected()) {
                    Log.i(TAG, "Connection is now created.");
                    Log.i(TAG, "Testing OBD commands...");
                    new EchoOffCommand().run(sock.getInputStream(), sock.getOutputStream());
                    new LineFeedOffCommand().run(sock.getInputStream(), sock.getOutputStream());
                    new TimeoutCommand(125).run(sock.getInputStream(), sock.getOutputStream());
                    new SelectProtocolCommand(ObdProtocols.AUTO).run(sock.getInputStream(), sock.getOutputStream());
                    new AmbientAirTemperatureCommand().run(sock.getInputStream(), sock.getOutputStream());
                }

            } else {
                Log.i(TAG, "mbtdevice null");
            }

        } catch (IOException e) {
            Log.i(TAG, "There is an error with connecting to device.");
            makeToast("There is an error with connecting to device.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
            Log.e(TAG, "Not responding");
        }
        Log.i(TAG, "Commands are working and getting data...");

        /**
         * Need to optimize the code, too much dummy implementations
         * need to check if is connected to catch the errors
         * need to sent data to broadcast receiver in Drive.class
         * need to add stop live data in drive
         */
        while (mBtDevice!= null && sock != null && sock.isConnected()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (sock.isConnected()) {

                //update data
                if (cmds.size() > 0) {
                    for (ObdCommand var : cmds) {
                        try {
                            var.run(sock.getInputStream(), sock.getOutputStream());
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                //print commands
                if (cmds.size() > 0) {
                    for (ObdCommand var : cmds) {
                        Log.i(TAG, "" + var.getName() + ": " + var.getFormattedResult());
                    }
                }
            } else {
                Log.i(TAG, "No connection");
                try {
                    sock = new BluetoothConnectionIO(mBtDevice).connect();
                    sock.connect();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    e.printStackTrace();
                }

                makeToast("There is an error with connecting to device.");
            }
        }
    }

    public void makeToast(String txt){
        Toast.makeText(Drive.getAppContext(), txt, Toast.LENGTH_SHORT).show();
    }
}
