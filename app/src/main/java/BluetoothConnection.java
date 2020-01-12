import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.demotxt.droidsrce.homedashboard.Drive;

public class BluetoothConnection extends Activity {
    private final String TAG = "BluetoothConection";
    private BluetoothAdapter bluetoothAdapter;
    private final BroadcastReceiver mReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action.equals(bluetoothAdapter.ACTION_STATE_CHANGED)){
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, bluetoothAdapter.ERROR);

                    switch (state){
                        case BluetoothAdapter.STATE_OFF:
                            Log.d(TAG, "onRecieve: STATE OFF");
                            break;
                        case BluetoothAdapter.STATE_ON:
                            Log.d(TAG, "onRecieve: STATE ON");
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            Log.d(TAG, "onRecieve: STATE TURNING OFF");
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            Log.d(TAG, "onRecieve: STATE TURNING ON");
                            break;
                    }
                }
        }
    };


    public BluetoothConnection(BluetoothAdapter bluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter;
    }

    public void enableBluetooth(){
        if(bluetoothAdapter == null){
            Log.d(TAG, "BluetoothConnection: Does not have BT capabilities.");
        }
        if(bluetoothAdapter.isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            //registerReciever(broadcastReciever, BTIntent);
        }
        if(bluetoothAdapter.isEnabled()){
            bluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            //registerReciever(broadcastReciever, BTIntent);
        }
    }
}