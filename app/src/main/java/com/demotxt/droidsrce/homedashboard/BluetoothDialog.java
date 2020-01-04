package com.demotxt.droidsrce.homedashboard;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BluetoothDialog extends AppCompatDialogFragment {
    private static final String TAG = "BluetoothDialog";
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private Set<BluetoothDevice> pairedDev;
    private String[] devices;
    private AlertDialog.Builder builder;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        builder = new AlertDialog.Builder(getActivity());

            if (bluetoothAdapter.isEnabled() && isSupportingBluetooth(bluetoothAdapter)) {
                pairedDev = pairedDevices(bluetoothAdapter);
                devices = new String[pairedDev.size()];

                int i = 0;
                for (BluetoothDevice device : pairedDev) {
                    devices[i] = device.getName();
                    i++;
                }

                builder.setTitle(R.string.dialogHead)
                        .setItems(devices, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
            }else{
                Toast toast = Toast.makeText(getContext(), R.string.error, Toast.LENGTH_LONG);
                toast.show();
            }
        return builder.create();
        }

    public boolean isSupportingBluetooth(BluetoothAdapter bluetoothAdapter){
        if (bluetoothAdapter == null) {
            return false;
        }
        return true;
    }
    public boolean enableBluetooth(BluetoothAdapter bluetoothAdapter){
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            if(bluetoothAdapter.isEnabled()){
                return true;
            }
            else{
                return false;
            }
        }
        return true;
    }
    public Set<BluetoothDevice> pairedDevices(BluetoothAdapter bluetoothAdapter){
        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();

            if (devices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                return devices;
            }
        return null;
    }


}
