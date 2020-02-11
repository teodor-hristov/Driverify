/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.demotxt.droidsrce.homedashboard;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.demotxt.droidsrce.homedashboard.Utils.Constants;
import com.demotxt.droidsrce.homedashboard.Utils.Methods;
import com.demotxt.droidsrce.homedashboard.io.CSVWriter;
import com.demotxt.droidsrce.homedashboard.io.LocationIO;
import com.demotxt.droidsrce.homedashboard.io.ObdReaderData;
import com.demotxt.droidsrce.homedashboard.services.LocationServiceProvider;
import com.demotxt.droidsrce.homedashboard.services.ObdConnection;
import com.demotxt.droidsrce.homedashboard.settings.Preferences;
import com.demotxt.droidsrce.homedashboard.ui.camera.CameraSourcePreview;
import com.demotxt.droidsrce.homedashboard.ui.camera.GraphicOverlay;
import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.control.TroubleCodesCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;

public final class Drive extends AppCompatActivity {
    private static final String TAG = "Drive";

    private static final int RC_HANDLE_GMS = 9001;
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private static boolean bluetoothDefaultIsEnable = false;

    //region CAMERA vars
    private static Context appContext;
    private CameraSource mCameraSource = null;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    //endregion

    //region OBD vars
    private TextView rpmText;
    private TextView speedText;
    private TextView engineLoad;
    private TextView oilTemp;
    private TextView coolantText;
    private ArrayList<TextView> driveItems;
    //endregion

    //region Location vars
    private LocationIO location;
    //endregion

    private boolean isRegistered = false;
    private boolean preRequisites = true;
    private BluetoothAdapter btAdapter;
    private BluetoothDevice mBtDevice;
    private SharedPreferences prefs;
    private IntentFilter filter;
    private Intent obdConnection;

    private String[] actions = {
            Constants.connected,
            Constants.disconnected,
            Constants.receiveData,
            Constants.extra,
            Constants.GPSDisabled,
            Constants.GPSEnabled,
            Constants.GPSLiveData,
            Constants.GPSPutExtra
    };

    private CSVWriter writer = null;
    private StringBuilder sb;

    private int rc;


    //==============================================================================================
    // Activity Methods
    //==============================================================================================

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_drive);
        Log.i(TAG, "Thread id: " + Thread.currentThread().getId());

        appContext = getApplicationContext();
        mPreview = findViewById(R.id.preview);
        mGraphicOverlay = findViewById(R.id.faceOverlay);
        rpmText = findViewById(R.id.rpmValue);
        speedText = findViewById(R.id.speedometerValue);
        engineLoad = findViewById(R.id.engineLoadValue);
        oilTemp = findViewById(R.id.oilTemp);
        coolantText = findViewById(R.id.coolantValue);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        filter = new IntentFilter();
        obdConnection = new Intent(getApplicationContext(), ObdConnection.class);
        driveItems = new ArrayList<>();
        driveItems.add(rpmText);
        driveItems.add(speedText);
        driveItems.add(engineLoad);
        driveItems.add(oilTemp);
        driveItems.add(coolantText);

        location = new LocationIO(Drive.this);

        if (btAdapter != null)
            bluetoothDefaultIsEnable = btAdapter.isEnabled();

        preRequisites = btAdapter != null && btAdapter.isEnabled();
        if (!preRequisites && prefs.getBoolean(Preferences.BLUETOOTH_ENABLE, false)) {
            preRequisites = btAdapter != null && btAdapter.enable();
        }

        if (btAdapter != null) {
            for (BluetoothDevice dev : btAdapter.getBondedDevices()) {
                if (dev.getAddress().equals(prefs.getString(Preferences.BLUETOOTH_LIST_KEY, "-1"))) {
                    mBtDevice = dev;
                    break;
                }
            }


        }

        filterAddActions(filter, actions);

        rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }

        if (!isRegistered) {
            registerReceiver(liveDataReceiever, filter);
        }
        startService(new Intent(getApplicationContext(), LocationServiceProvider.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
        /**
         *  Check if bluetooth is enabled and if not bt.enable()
         * else bt.disable.
         */
        if (btAdapter != null)
            bluetoothDefaultIsEnable = btAdapter.isEnabled();

        preRequisites = btAdapter != null && btAdapter.isEnabled();
        if (!preRequisites && prefs.getBoolean(Preferences.BLUETOOTH_ENABLE, false)) {
            preRequisites = btAdapter != null && btAdapter.enable();
        }
        if (preRequisites && !prefs.getBoolean(Preferences.BLUETOOTH_ENABLE, false)) {
            preRequisites = btAdapter != null && btAdapter.disable();
        }
        if (!isRegistered) {
            registerReceiver(liveDataReceiever, filter);
        }
        if (!Methods.isServiceRunning(getAppContext(), ObdConnection.class)) {
            startService(new Intent(getApplicationContext(), ObdConnection.class));
        }

    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
        if (isRegistered) {
            unregisterReceiver(liveDataReceiever);
            isRegistered = false;
        }
        if (Methods.isServiceRunning(getAppContext(), ObdConnection.class)) {
            stopService(new Intent(getApplicationContext(), ObdConnection.class));
        }

    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (btAdapter != null && btAdapter.isEnabled() && !bluetoothDefaultIsEnable)
            btAdapter.disable();

        if (isRegistered) {
            unregisterReceiver(liveDataReceiever);
            isRegistered = false;
        }
        if (Methods.isServiceRunning(getAppContext(), ObdConnection.class)) {
            stopService(new Intent(getApplicationContext(), ObdConnection.class));
        }
    }

    private BroadcastReceiver liveDataReceiever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isRegistered = true;
            String action = intent.getAction();
            ObdReaderData data;

            if (action.equals(Constants.connected)) {
                connectivityBluetooth(intent);
            }
            if (action.equals(Constants.GPSEnabled)) {
                makeSnackbar("GPS Enabled!");
            }
            if (action.equals(Constants.GPSDisabled)) {
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        location.enableGPS();
                    }
                };
                Snackbar.make(mGraphicOverlay,"You turned off GPS, for better usage of this application you have to turn it on." ,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("Turn on", listener)
                        .show();
            }

            if (action.equals(Constants.receiveData)) {
                data = intent.getParcelableExtra(Constants.receiveData);
                handleBluetoothLiveData(data);
            }
            if (action.equals(Constants.GPSLiveData)) {

            }

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.driveSettings:
                startActivity(new Intent(this.getApplicationContext(), Preferences.class));
                break;
            case R.id.start_live_data:
                ArrayList<ObdCommand> commands = new ArrayList<>();
                commands.add(new RPMCommand());
                commands.add(new SpeedCommand());
                commands.add(new TroubleCodesCommand());

                startService(obdConnection);
                if (!isRegistered) {
                    registerReceiver(liveDataReceiever, filter);
                }
                makeToast("Live data started.");
                break;
            case R.id.stop_live_data:
                if (isRegistered) {
                    unregisterReceiver(liveDataReceiever);
                    isRegistered = false;
                }
                if (Methods.isServiceRunning(getAppContext(), ObdConnection.class)) {
                    stopService(new Intent(getApplicationContext(), ObdConnection.class));
                }
                makeToast("Live data stopped.");
                try {
                    if (writer != null)
                        writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i(TAG, "Problem with file close");
                }
                for (TextView v : driveItems)
                    v.setText("0");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static Context getAppContext() {
        return appContext;
    }

    public void makeToast(String msg) {
        Toast.makeText(Drive.this, msg, Toast.LENGTH_SHORT).show();
    }

    private void connectivityBluetooth(Intent intent) {

        String connectionStatusMsg = intent.getStringExtra(Constants.extra);
        makeToast(connectionStatusMsg);

        if (connectionStatusMsg.equals(getString(R.string.obd_connected))) {
            makeToast(getString(R.string.obd_connected));

        } else if (connectionStatusMsg.equals(getString(R.string.connect_lost))) {
            makeToast(getString(R.string.connect_lost));
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "Problem with file close");
            }
        }

    }

    private void handleBluetoothLiveData(ObdReaderData data) {
        makeSnackbar("OBD live data is processing.!");
        try {
            if (writer == null) {
                writer = new CSVWriter(Constants.DataLogPath);
                writer.append(writer.formatCSV("rpm speed coolant load latitude longitude"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "Writer initialization problem.");
            Log.i(TAG, e.getMessage());
        }
        if (data != null) {
            for (int i = 0; i < data.getCommands().size(); i++) {
                driveItems.get(i).setText("" + data.getCommands().get(i));
            }
            if (writer != null) {
                sb = new StringBuilder();
                try {
                    for (String str : data.getCommands()) {
                        sb.append(str);
                        sb.append(",");
                    }
                    writer.append(sb.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i(TAG, "Could not write to file");
                }
            }
        }
    }

    public void makeSnackbar(String string) {
        Snackbar.make(mGraphicOverlay, string,
                Snackbar.LENGTH_INDEFINITE)
                .show();
    }

    private void filterAddActions(IntentFilter filter, String[] actions) {
        for(String item : actions)
            filter.addAction(item);
    }

    //region SpeepDetection with GMS

    /**
     * Broadcast Receiver to receive OBD connection status and real time data
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Face Tracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }
    //endregion


}
