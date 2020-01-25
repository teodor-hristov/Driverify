package com.demotxt.droidsrce.homedashboard.io;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class BluetoothConnectionIO {
    private static final String TAG = BluetoothConnectionIO.class.getName();
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothSocket mSock;
    private BluetoothDevice mBtDevice;

        public BluetoothConnectionIO() {

        }
        public BluetoothConnectionIO(BluetoothDevice device) {
            setmBtDevice(device);
    }

        public BluetoothDevice getmBtDevice() {
            return mBtDevice;
        }

        public void setmBtDevice(BluetoothDevice mBtDevice) {
            if(mBtDevice != null)
                this.mBtDevice = mBtDevice;
        }

        public BluetoothSocket connect() throws IOException, TimeoutException {
            if(getmBtDevice() != null){
                    mSock = mBtDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                if(mSock != null){
                    Log.i(TAG, "State socket not null.");
                    return mSock;
                }else {
                    throw new TimeoutException();
                }
            }else {
                throw new NullPointerException();
            }
        }
}
