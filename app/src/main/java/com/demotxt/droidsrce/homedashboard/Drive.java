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
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.demotxt.droidsrce.homedashboard.Utils.Constants;
import com.demotxt.droidsrce.homedashboard.Utils.Methods;
import com.demotxt.droidsrce.homedashboard.io.LocationIO;
import com.demotxt.droidsrce.homedashboard.io.ObdReaderData;
import com.demotxt.droidsrce.homedashboard.services.DataController;
import com.demotxt.droidsrce.homedashboard.services.LocationServiceProvider;
import com.demotxt.droidsrce.homedashboard.services.ObdConnection;
import com.demotxt.droidsrce.homedashboard.settings.Preferences;
import com.demotxt.droidsrce.homedashboard.ui.camera.CameraSourcePreview;
import com.demotxt.droidsrce.homedashboard.ui.camera.GraphicOverlay;
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
import java.util.Arrays;

public final class Drive extends AppCompatActivity {
    private final String TAG = getClass().getName();

    private static final int RC_HANDLE_GMS = 9001;
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private static boolean bluetoothDefaultIsEnable = false;

    //region CAMERA vars
    private static Context appContext;
    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    //endregion

    private ArrayList<TextView> driveItems;

    private boolean isRegistered = false;
    private boolean preRequisites = true;
    private BluetoothAdapter btAdapter;
    private SharedPreferences prefs;
    private IntentFilter filter;

    private String[] actions = {
            Constants.CONNECTED,
            Constants.DISCONNECTED,
            Constants.RECEIVE_DATA,
            Constants.EXTRA,
            Constants.GPS_DISABLED,
            Constants.GPS_ENABLED,
            Constants.GPS_LIVE_DATA,
            Constants.GPS_PUT_EXTRA
    };
    private Menu actionBarMenu;

    private int rc;


    //==============================================================================================
    // Activity Methods
    //==============================================================================================

    private BroadcastReceiver liveDataReceiever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isRegistered = true;
            String action = intent.getAction();
            String stringExtra = intent.getStringExtra(Constants.EXTRA);
            ObdReaderData data = intent.getParcelableExtra(Constants.RECEIVE_DATA);

            assert action != null;
            switch (action) {
                case Constants.CONNECTED:
                    connectedBluetooth(stringExtra);
                    break;
                case Constants.DISCONNECTED:
                    connectionLost(stringExtra);
                    break;
                case Constants.GPS_ENABLED:
                    locationEnabled();
                    break;
                case Constants.GPS_DISABLED:
                    locationDisabled();
                    break;
                case Constants.GPS_LIVE_DATA:
                    handleLocationLiveData();
                    break;
                case Constants.RECEIVE_DATA:
                    handleBluetoothLiveData(data);
                    break;
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_drive);
        Log.i(TAG, "Thread id: " + Thread.currentThread().getId());

        appContext = getApplicationContext();
        preview = findViewById(R.id.preview);
        graphicOverlay = findViewById(R.id.faceOverlay);

        TextView rpmText = findViewById(R.id.rpmValue);
        TextView speedText = findViewById(R.id.speedometerValue);
        TextView engineLoad = findViewById(R.id.engineLoadValue);
        TextView oilTemp = findViewById(R.id.oilTemp);
        TextView coolantText = findViewById(R.id.coolantValue);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        filter = new IntentFilter();
        driveItems = new ArrayList<>(Arrays.asList(rpmText, speedText, engineLoad, coolantText, oilTemp));

        if (btAdapter != null)
            bluetoothDefaultIsEnable = btAdapter.isEnabled();

        preRequisites = btAdapter != null && btAdapter.isEnabled();
        if (!preRequisites && prefs.getBoolean(Preferences.BLUETOOTH_ENABLE, false)) {
            preRequisites = btAdapter != null && btAdapter.enable();
        }

        if (btAdapter != null) {
            for (BluetoothDevice dev : btAdapter.getBondedDevices()) {
                if (dev.getAddress().equals(prefs.getString(Preferences.BLUETOOTH_LIST_KEY, "-1"))) {
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
    }

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
        if (Methods.isServiceRunning(getAppContext(), LocationServiceProvider.class)) {
            stopService(new Intent(getApplicationContext(), DataController.class));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        actionBarMenu = menu;
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.driveSettings:
                startActivity(new Intent(this.getApplicationContext(), Preferences.class));
                break;
            case R.id.start_live_data:
                startLiveData(filter);
                break;
            case R.id.stop_live_data:
                stopLiveData();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
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
        if (!Methods.isServiceRunning(getAppContext(), DataController.class)) {
            startService(new Intent(getApplicationContext(), DataController.class));
        }
        if (!Methods.isServiceRunning(getAppContext(), LocationServiceProvider.class)) {
            startService(new Intent(getApplicationContext(), LocationServiceProvider.class));
        }

    }

    public static Context getAppContext() {
        return appContext;
    }

    public void makeToast(String msg) {
        Toast.makeText(Drive.this, msg, Toast.LENGTH_SHORT).show();
    }

    private void connectedBluetooth(String connectionStatusMsg) {
        //makeToast(connectionStatusMsg);
        if (connectionStatusMsg.equals(getString(R.string.obd_connected))) {
            makeToast(getString(R.string.obd_connected));
        }
    }

    private void connectionLost(String connectionStatusMsg) {
        if (connectionStatusMsg.equals(getString(R.string.connect_lost))) {
            makeToast("Connection lost.");
            actionBarMenu.getItem(R.id.bluetoothStatus).setIcon(R.drawable.ic_bluetooth_searching_black_24dp);
        }
    }

    private void handleBluetoothLiveData(ObdReaderData data) {
        ArrayList commands;
        actionBarMenu.findItem(R.id.bluetoothStatus).setIcon(R.drawable.ic_bluetooth_connected_black_24dp);
        if (data == null) {
            return;
        }

        commands = data.getCommands();
        for (int i = 0; i < commands.size(); i++) {
            if (commands.get(i) == null) {
                driveItems.get(i).setText("NaN");
            } else {
                driveItems.get(i).setText(commands.get(i).toString());
            }
        }
    }

    private void handleLocationLiveData() {
        Log.i(TAG, "Getting location data...");
        actionBarMenu.findItem(R.id.locationStatus).setIcon(R.drawable.location_ok);
    }

    private void locationDisabled() {
        actionBarMenu.findItem(R.id.locationStatus).setIcon(R.drawable.location_off);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LocationIO(getAppContext());
            }
        };
        Snackbar.make(graphicOverlay,
                "You turned off GPS, for better usage of this application you have to turn it on.",
                Snackbar.LENGTH_INDEFINITE)
                .setAction("Turn on", listener)
                .show();
    }

    private void locationEnabled() {
        actionBarMenu.findItem(R.id.locationStatus).setIcon(R.drawable.location_ok);
        makeSnackbar("GPS Enabled!");
    }

    public void makeSnackbar(String string) {
        Snackbar.make(graphicOverlay, string,
                Snackbar.LENGTH_SHORT)
                .show();
    }

    private void filterAddActions(IntentFilter filter, String[] actions) {
        for (String item : actions)
            filter.addAction(item);
    }

    private void startLiveData(IntentFilter filter) {
        startService(new Intent(Drive.this, ObdConnection.class));
        startService(new Intent(Drive.this, DataController.class));
        startService(new Intent(Drive.this, LocationServiceProvider.class));
        if (!isRegistered) {
            registerReceiver(liveDataReceiever, filter);
        }
        makeSnackbar("Live data started.");
    }

    public void stopLiveData() {
        if (isRegistered) {
            unregisterReceiver(liveDataReceiever);
            isRegistered = false;
        }
        if (Methods.isServiceRunning(getAppContext(), ObdConnection.class)) {
            stopService(new Intent(getApplicationContext(), ObdConnection.class));
        }
        if (Methods.isServiceRunning(getAppContext(), ObdConnection.class)) {
            stopService(new Intent(getApplicationContext(), DataController.class));
        }
        if (Methods.isServiceRunning(getAppContext(), LocationServiceProvider.class)) {
            stopService(new Intent(getApplicationContext(), LocationServiceProvider.class));
        }
        for (TextView v : driveItems)
            v.setText("0");
        makeSnackbar("Live data stopped.");
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

        Snackbar.make(graphicOverlay, R.string.permission_camera_rationale,
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

        cameraSource = new CameraSource.Builder(context, detector)
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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

        if (cameraSource != null) {
            try {
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
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
            return new GraphicFaceTracker(graphicOverlay);
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
