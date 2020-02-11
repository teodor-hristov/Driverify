package com.demotxt.droidsrce.homedashboard.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.demotxt.droidsrce.homedashboard.Utils.Constants;

public class LocationServiceProvider extends Service {
    private final String TAG = this.getClass().getName();
    private LocationListener listener;
    private LocationManager locationManager;
    private Intent intentToBroadcastReceiver = new Intent();


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "Service started!");
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i(TAG, "Location changed!");
                intentToBroadcastReceiver.setAction(Constants.GPSLiveData);
                intentToBroadcastReceiver.putExtra(Constants.GPSPutExtra, location.getLongitude() + " " + location.getLatitude());
                sendBroadcast(intentToBroadcastReceiver);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {
                Log.i(TAG, "Provider started!");
                intentToBroadcastReceiver.setAction(Constants.GPSEnabled);
                sendBroadcast(intentToBroadcastReceiver);
            }

            @Override
            public void onProviderDisabled(String s) {
                Log.i(TAG, "Provider stopped!");
                intentToBroadcastReceiver.setAction(Constants.GPSDisabled);
                sendBroadcast(intentToBroadcastReceiver);
            }
        };
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                900,
                1,
                listener);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(listener);
        }
    }
}
