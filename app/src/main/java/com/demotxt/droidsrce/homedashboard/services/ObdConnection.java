package com.demotxt.droidsrce.homedashboard.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
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

public class ObdConnection extends Service {
    private static final String TAG = ObdConnection.class.getName();
    private ArrayList<ObdCommand> cmds;
    private BluetoothAdapter btAdapter;
    private BluetoothDevice mBtDevice;
    private BluetoothSocket sock;

    private SharedPreferences prefs;

    public ArrayList<ObdCommand> getCmds() {
        return cmds;
    }

    public void setCmds(ArrayList<ObdCommand> cmds) {
        if(cmds != null && cmds.size() > 0)
        this.cmds = cmds;
    }

    @Override
    public void onCreate() {
        ArrayList<ObdCommand> commands = new ArrayList<>();
        commands.add(new RPMCommand());
        commands.add(new SpeedCommand());
        commands.add(new TroubleCodesCommand());
        setCmds(commands);
        Log.i(TAG, "ObdConnection service started");
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sock = null;

        if(btAdapter != null) {
            for (BluetoothDevice dev : btAdapter.getBondedDevices()) {
                if (dev.getAddress().equals(prefs.getString(Preferences.BLUETOOTH_LIST_KEY, "-1"))) {
                    mBtDevice = dev;
                    break;
                }
            }
        }

        try {
            if(mBtDevice != null) {
                Log.i(TAG, "Entering communication test...");
                sock = new BluetoothConnectionIO(mBtDevice).connect();
                sock.connect();
                if(sock.isConnected()) {
                    Log.i(TAG, "Connection is now created.");
                    Log.i(TAG, "Testing OBD commands...");
                    new EchoOffCommand().run(sock.getInputStream(), sock.getOutputStream());
                    new LineFeedOffCommand().run(sock.getInputStream(), sock.getOutputStream());
                    new TimeoutCommand(125).run(sock.getInputStream(), sock.getOutputStream());
                    new SelectProtocolCommand(ObdProtocols.AUTO).run(sock.getInputStream(), sock.getOutputStream());
                    new AmbientAirTemperatureCommand().run(sock.getInputStream(), sock.getOutputStream());
                    if(cmds.size() > 0){
                        for(ObdCommand var : cmds){
                            var.run(sock.getInputStream(), sock.getOutputStream());
                        }
                    }
                }

            }else{
                Log.i(TAG, "mbtdevice null");
            }

        } catch (IOException e) {
            Log.i(TAG, "Nep.");
            makeToast("There is an error with connecting to device.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
            Log.e(TAG, "Not responding");
        }

        if(sock.isConnected()) {
            Log.i(TAG, "Commands are working and getting data...");
            if (sock.isConnected()) {

                //print commands
                if(cmds.size() > 0){
                    for(ObdCommand var : cmds){
                        Log.i(TAG, "" + var.getName() + ": " + var.getFormattedResult());
                        makeToast("" + var.getName() + ": " + var.getFormattedResult());
                    }
                }
                try {
                    sock.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.i(TAG, "No connection");
                makeToast("There is an error with connecting to device.");
            }
        }

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

    public void makeToast(String txt){
        Toast.makeText(Drive.getAppContext(), txt, Toast.LENGTH_SHORT).show();
    }
}
