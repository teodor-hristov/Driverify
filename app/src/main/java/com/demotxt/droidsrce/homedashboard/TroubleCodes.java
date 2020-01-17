package com.demotxt.droidsrce.homedashboard;

import android.content.BroadcastReceiver;
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

import com.demotxt.droidsrce.homedashboard.settings.Settings;
import com.sohrab.obd.reader.obdCommand.ObdCommand;
import com.sohrab.obd.reader.obdCommand.ObdConfiguration;
import com.sohrab.obd.reader.obdCommand.control.TroubleCodesCommand;
import com.sohrab.obd.reader.service.ObdReaderService;
import com.sohrab.obd.reader.trip.TripRecord;

import java.util.ArrayList;
import java.util.Arrays;

import static com.sohrab.obd.reader.constants.DefineObdReader.ACTION_OBD_CONNECTED;
import static com.sohrab.obd.reader.constants.DefineObdReader.ACTION_READ_OBD_REAL_TIME_DATA;

public class TroubleCodes extends AppCompatActivity {
    private static final String TAG = "Trouble codes: ";

    private IntentFilter intentFilter;
    private TextView codes;
    private ArrayList<String> mAllCodes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trouble_codes);
        //TODO add trouble codes api
        codes = findViewById(R.id.troubleCodesId);
        mAllCodes = new ArrayList<>();

        //Obd command array
        //ObdConfiguration.setmObdCommands(this, null); //for executing all commands
        ArrayList<ObdCommand> obdCommands = new ArrayList<>();
        obdCommands.add(new TroubleCodesCommand());

        //Set configuration
        ObdConfiguration.setmObdCommands(this, obdCommands);

        //Register receiver with some action related to OBD connection status and read PID values
        intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_READ_OBD_REAL_TIME_DATA);
        intentFilter.addAction(ACTION_OBD_CONNECTED);
        registerReceiver(mObdReaderReceiver, intentFilter);


        //start service that keep running in background for connecting and execute command until you stop
        startService(new Intent(this, ObdReaderService.class));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mObdReaderReceiver);
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
                //TODO: Clear codes
                break;
            case R.id.copyCodes:
                //TODO: Copy to clipboard codes
                break;
            case R.id.saveCodes:
                //TODO: Save codes
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private final BroadcastReceiver mObdReaderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //findViewById(R.id.progress_bar).setVisibility(View.GONE);
            //mObdInfoTextView.setVisibility(View.VISIBLE);
            String action = intent.getAction();

            if (action.equals(ACTION_OBD_CONNECTED)) {

                String connectionStatusMsg = intent.getStringExtra(ObdReaderService.INTENT_EXTRA_DATA);
                //mObdInfoTextView.setText(connectionStatusMsg);
                makeToast(connectionStatusMsg);
                //Toast.makeText(Drive.this, connectionStatusMsg, Toast.LENGTH_SHORT).show();

                if (connectionStatusMsg.equals(getString(R.string.obd_connected))) {
                    //OBD connected  do what want after OBD connection
                    //mObdInfoTextView.setText("Connected");
                    makeToast(Integer.toString(R.string.obd_connected));
                } else if (connectionStatusMsg.equals(getString(R.string.connect_lost))) {
                    //OBD disconnected  do what want after OBD disconnection
                    makeToast(Integer.toString(R.string.connect_lost));
                } else {
                    // here you could check OBD connection and pairing status
                }

            } else if (action.equals(ACTION_READ_OBD_REAL_TIME_DATA)) {
                //mObdInfoTextView.setText("Checkpoint 1");
                TripRecord tripRecord = TripRecord.getTripRecode(TroubleCodes.this);
                try {
                    mAllCodes.addAll(Arrays.asList(tripRecord.getmPermanentTroubleCode()
                            .split(" ")));
                }catch (Exception e){
                    Log.e(TAG, "onReceive: " + e.toString());
                }
               // mAllCodes.addAll(Arrays.asList(tripRecord.getmPendingTroubleCode()
                 //       .split(" ")));

                for(String item : mAllCodes){
                    codes.append(item + "\n");
                }

                //mRpmText.setText(tripRecord.getEngineRpm());
                Log.i(TAG, tripRecord.getmPermanentTroubleCode());
                //Log.i(TAG, tripRecord.getmPendingTroubleCode());


            }

        }
    };
    public void makeToast(String  msg){
        Toast.makeText(TroubleCodes.this, msg, Toast.LENGTH_SHORT).show();
    }
}
