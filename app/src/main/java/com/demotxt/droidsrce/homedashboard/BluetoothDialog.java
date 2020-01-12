package com.demotxt.droidsrce.homedashboard;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.versionedparcelable.ParcelUtils;

import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

public class BluetoothDialog extends AppCompatDialogFragment {
    private static final String TAG = "BluetoothDialog";
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private Set<BluetoothDevice> pairedDev;
    private String[] devices;
    private AlertDialog.Builder builder;

    private BluetoothDevice selectedDevice;
    private UUID deviceAdress;
    BluetoothSocket socket;

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
                                selectedDevice = (pairedDev.toArray(new BluetoothDevice[pairedDev.size()]))[which];
//                                try {
//                                    deviceAdress = getUUID(bluetoothAdapter, which);
//                                    if(deviceAdress == null)
//                                        throw new Exception("Null");
//                                } catch (NoSuchMethodException e) {
//                                    Log.e(TAG, e.toString());
//                                } catch (InvocationTargetException e) {
//                                    Log.e(TAG, e.toString());
//                                } catch (IllegalAccessException e) {
//                                    Log.e(TAG, e.toString());
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                                bluetoothAdapter.getRemoteDevice(selectedDevice.getAddress());
//                                try {
//                                    socket = selectedDevice.createInsecureRfcommSocketToServiceRecord(deviceAdress);
//                                } catch (IOException e) {
//                                    Log.e(TAG, e.toString());
//                                }
//
//                                try {
//                                    socket.connect();
//                                } catch (IOException e) {
//                                    Log.e(TAG, e.toString());
//                                }
//                                // execute commands
//                                try {
//                                    new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());
//                                    new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());
//                                    new TimeoutCommand(125).run(socket.getInputStream(), socket.getOutputStream());
//                                    new SelectProtocolCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());
//                                    new AmbientAirTemperatureCommand().run(socket.getInputStream(), socket.getOutputStream());
//                                } catch (Exception e) {
//                                    // handle errors
//                                }
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
    public Set<BluetoothDevice> pairedDevices(BluetoothAdapter bluetoothAdapter){
        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();

            if (devices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                return devices;
            }
        return null;
    }
    private UUID getUUID(BluetoothAdapter adapter, int index) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);
        int i = 0;
        UUID uuid = null;
        ParcelUuid[] uuids = (ParcelUuid[]) getUuidsMethod.invoke(adapter, null);

        for (ParcelUuid item: uuids) {
            if(i == index) {
                uuid = item.getUuid();
                break;
            }
            i++;
        }
        return uuid;
    }


}
