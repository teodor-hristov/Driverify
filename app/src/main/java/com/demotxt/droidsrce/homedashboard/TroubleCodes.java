package com.demotxt.droidsrce.homedashboard;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.pires.obd.commands.protocol.ResetTroubleCodesCommand;
import com.sohrab.obd.reader.obdCommand.control.TroubleCodesCommand;
import com.sohrab.obd.reader.service.ObdReaderService;

import java.util.ArrayList;

import static com.sohrab.obd.reader.constants.DefineObdReader.ACTION_OBD_CONNECTED;
import static com.sohrab.obd.reader.constants.DefineObdReader.ACTION_READ_OBD_REAL_TIME_DATA;

public class TroubleCodes extends AppCompatActivity {
    private static final String TAG = "Trouble codes: ";
    private static final int CODE_LEN = 4;

    private IntentFilter intentFilter;
    private TextView codes;
    private ArrayList<String> mAllCodes;
    private Intent ObdService;
    private ClipboardManager mClipManager;

    private boolean isRegistered = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trouble_codes);
        //TODO add trouble codes api

        codes = findViewById(R.id.troubleCodesId);
        ObdService = new Intent(this, ObdReaderService.class);
        mAllCodes = new ArrayList<>();
        mClipManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        //Obd command array
        //ObdConfiguration.setmObdCommands(this, null); //for executing all commands
//        ArrayList<ObdCommand> obdCommands = new ArrayList<>();
//        obdCommands.add(new TroubleCodesCommand());
//
//        //Set configuration
//        ObdConfiguration.setmObdCommands(this, obdCommands);

        //Register receiver with some action related to OBD connection status and read PID values
        intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_READ_OBD_REAL_TIME_DATA);
        intentFilter.addAction(ACTION_OBD_CONNECTED);
        registerReceiver(mObdReaderReceiver, intentFilter);
//
        //start service that keep running in background for connecting and execute command until you stop
        startService(ObdService);

    }

    @Override
    protected void onResume() {
        super.onResume();
        startService(ObdService);
        registerReceiver(mObdReaderReceiver, intentFilter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isRegistered) {
            unregisterReceiver(mObdReaderReceiver);
            isRegistered = false;
        }
        if(isServiceRunning(ObdReaderService.class)){
            stopService(ObdService);
            Log.i(TAG, "onPause: " + isServiceRunning(ObdReaderService.class));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.trouble_codes_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.clearCodes:
                //TODO: Test if clear codes is working
                if(codes.getText().length() > CODE_LEN || codes.getText() != null)
                {
                    try{
                        new ResetTroubleCodesCommand();
                        makeToast(getString(R.string.dtc_cleared));
                        codes.setText("" + getDTC());
                    }catch (Exception e){

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
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Broadcast Receiver to receive OBD connection status and real time data
     */
    private final BroadcastReceiver mObdReaderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //findViewById(R.id.progress_bar).setVisibility(View.GONE);
            //mObdInfoTextView.setVisibility(View.VISIBLE);
            isRegistered = true;
            String action = intent.getAction();

            if (action.equals(ACTION_OBD_CONNECTED)) {
                String connectionStatusMsg = intent.getStringExtra(ObdReaderService.INTENT_EXTRA_DATA);
                makeToast(connectionStatusMsg);

                if (connectionStatusMsg.equals(getString(R.string.obd_connected))) {//OBD connected  do what want after OBD connection
                    makeToast(getString(R.string.obd_connected));
                    codes.setText("" + getDTC());

                } else if (connectionStatusMsg.equals(getString(R.string.connect_lost))) {//OBD disconnected  do what want after OBD disconnection
                    makeToast(getString(R.string.connect_lost));

                } else {// here you could check OBD connection and pairing status

                }

            } else if (action.equals(ACTION_READ_OBD_REAL_TIME_DATA)) {

            }

        }
    };
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
    public String getDTC(){
        String troubleCodes = new TroubleCodesCommand().getFormattedResult();
       if(troubleCodes == null || troubleCodes.length() < CODE_LEN){
           return getString(R.string.no_dtc);
       }else{
           return troubleCodes;
       }
    }
}
