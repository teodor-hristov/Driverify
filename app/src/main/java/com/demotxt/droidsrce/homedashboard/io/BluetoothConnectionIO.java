package com.demotxt.droidsrce.homedashboard.io;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class BluetoothConnectionIO {
    private static final String TAG = BluetoothConnectionIO.class.getName();
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothSocket socket;
    private BluetoothDevice bluetoothDevice;

    public BluetoothConnectionIO(BluetoothDevice device) {
        setBluetoothDevice(device);
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        if (bluetoothDevice != null)
            this.bluetoothDevice = bluetoothDevice;
    }

    public BluetoothSocket connect() throws IOException, TimeoutException {
        if (getBluetoothDevice() != null) {
            socket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            if (socket != null) {
                Log.i(TAG, "State socket not null.");
                return socket;
            } else {
                throw new TimeoutException();
            }
        } else {
            throw new NullPointerException();
        }
    }
}
