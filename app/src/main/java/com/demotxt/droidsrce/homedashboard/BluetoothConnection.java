package com.demotxt.droidsrce.homedashboard;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class BluetoothConnection extends Service {
    private final String TAG = "BluetoothConection";
    private BluetoothAdapter bluetoothAdapter;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        enableDisableBT();
        return START_STICKY;
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(bluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, bluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };



    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called");
        unregisterReceiver(mReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void enableDisableBT(){
        if(bluetoothAdapter == null){
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
        }
        if(!bluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: enabling BT.");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mReceiver, BTIntent);
        }
        if(bluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: disabling BT.");
            Toast.makeText(this, "BT is now disabled!", Toast.LENGTH_SHORT).show();
            bluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mReceiver, BTIntent);
        }

    }
}