package com.demotxt.droidsrce.homedashboard.io;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class LocationIO {
    private final String TAG = getClass().getName();
    private LocationManager locationManager;
    private Location location;
    private final Context context;

    public LocationIO(Context context) {
        this.context = context;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.location = getLocation();
    }

    public Location getLocation() {
        Location localLocation = new Location("GPS");
        if (this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            localLocation = this.locationManager.getLastKnownLocation("GPS");
        } else {
            enableGPS();
        }
        return localLocation;
    }

    public void enableGPS() {
        new AlertDialog.Builder(context)
                .setTitle("GPS not enabled.")
                .setMessage("This application need GPS for Black box records.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", null)
                .show();

    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Latitude: ");
        stringBuilder.append(location.getLatitude() + "\n");
        stringBuilder.append("Longitude: \n");
        stringBuilder.append(location.getLongitude() + "\n");
        return stringBuilder.toString();
    }
}
