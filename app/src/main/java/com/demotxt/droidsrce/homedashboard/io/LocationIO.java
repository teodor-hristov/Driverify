package com.demotxt.droidsrce.homedashboard.io;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.demotxt.droidsrce.homedashboard.Drive;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationIO {
    private final String TAG = getClass().getName();
    LocationManager locationManager;
    Location location;
    Context context;

    public LocationIO(Context context) {
        this.context = context;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.location = new Location("GPS");
    }

    private Location getLocation() {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return location;
        } else {
            enableGPS(context);
        }
        return null;
    }

    private void enableGPS(final Context context) {
        new AlertDialog.Builder(context)
                .setTitle("GPS not enabled.")  // GPS not found
                .setMessage("This application need GPS for Black box records.") // Want to enable?
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", null)
                .show();

    }
}
