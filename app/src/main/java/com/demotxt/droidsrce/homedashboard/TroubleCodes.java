package com.demotxt.droidsrce.homedashboard;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demotxt.droidsrce.homedashboard.io.BluetoothConnectionIO;
import com.demotxt.droidsrce.homedashboard.services.ObdConnection;
import com.demotxt.droidsrce.homedashboard.settings.Preferences;
import com.github.pires.obd.commands.control.TroubleCodesCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ResetTroubleCodesCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;
import com.sohrab.obd.reader.service.ObdReaderService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import static com.sohrab.obd.reader.constants.DefineObdReader.ACTION_OBD_CONNECTED;

public class TroubleCodes extends AppCompatActivity {
    private static final String TAG = "Trouble codes: ";
    private static final int CODE_LEN = 4;

    private IntentFilter intentFilter;
    private TextView codes;
    private ArrayList<String> mAllCodes;
    private Intent ObdService;
    private ClipboardManager mClipManager;

    private boolean isRegistered = false;

    BluetoothConnectionIO btConnection = new BluetoothConnectionIO();
    BluetoothAdapter mbtAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothDevice dev = null;
    SharedPreferences prefs;
    BluetoothSocket sock = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trouble_codes);
        //TODO add trouble codes api

        codes = findViewById(R.id.troubleCodesId);
        ObdService = new Intent(this, ObdReaderService.class);
        mAllCodes = new ArrayList<>();
        mClipManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        intentFilter = new IntentFilter();
        intentFilter.addAction(ObdConnection.DTC);
        intentFilter.addAction(ACTION_OBD_CONNECTED);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (mbtAdapter != null) {
            for (BluetoothDevice lvdev : mbtAdapter.getBondedDevices()) {
                if (lvdev.getAddress().equals(prefs.getString(Preferences.BLUETOOTH_LIST_KEY, "-1"))) {
                    dev = lvdev;
                }
            }
        }
        btConnection.setmBtDevice(dev);
        try {
            sock = btConnection.connect();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        //registerReceiver(mObdReaderReceiver, intentFilter);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.trouble_codes_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.getCodes:
                try {
                    getCodes(codes, sock);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                } catch (TimeoutException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
                break;
            case R.id.clearCodes:
                if(codes.getText().length() > CODE_LEN || codes.getText() != null)
                {
                    try{
                        if(isServiceRunning(ObdConnection.class) && isRegistered) {
                            new ResetTroubleCodesCommand();
                            makeToast(getString(R.string.dtc_cleared));
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.copyCodes:
                //Copy to clipboard
                if(codes.getText() != null || codes.getText().length() < 3)
                    copyToClipboard(codes.getText().toString());

                break;
            case R.id.saveCodes:
                //TODO: Save codes
                break;
            case R.id.settings:
                startActivity(new Intent(TroubleCodes.this, Preferences.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void makeToast(String  msg){
        Toast.makeText(TroubleCodes.this, msg, Toast.LENGTH_SHORT).show();
    }

    public boolean isServiceRunning(Class serviceClass){
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void copyToClipboard(String text){
        ClipData clipData = ClipData.newPlainText(TAG, text);
        mClipManager.setPrimaryClip(clipData);
        makeToast(getString(R.string.dtc_copied));
    }

    private void getCodes(TextView tv, BluetoothSocket sock) throws IOException, TimeoutException, InterruptedException {
        if(sock == null) {
            sock = btConnection.connect();
            sock.connect();
        }else if(sock.isConnected() && mbtAdapter != null){
            new EchoOffCommand().run(sock.getInputStream(), sock.getOutputStream());
            new LineFeedOffCommand().run(sock.getInputStream(), sock.getOutputStream());
            new TimeoutCommand(125).run(sock.getInputStream(), sock.getOutputStream());
            new SelectProtocolCommand(ObdProtocols.AUTO).run(sock.getInputStream(), sock.getOutputStream());

            TroubleCodesCommand dtc = new TroubleCodesCommand();
            dtc.run(sock.getInputStream(), sock.getOutputStream());

            tv.setText("" + dtc.getFormattedResult());
            Log.i(TAG, "" + dtc.getFormattedResult());
        }else{
            sock.connect();
            getCodes(tv, sock);
        }
    }
}
