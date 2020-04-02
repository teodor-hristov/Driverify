package com.demotxt.droidsrce.homedashboard;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.demotxt.droidsrce.homedashboard.Utils.Constants;
import com.demotxt.droidsrce.homedashboard.services.BlackBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;


public class Home extends AppCompatActivity {
    public static final int PERMISSION_ALL = 1;
    public static String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.CAMERA,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};
    CardView troubleView, tipsView, driveView, blackBoxView;
    Intent drive, tips, troubleCodes, blackBox;
    LinearLayout ll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        /**
         * Requesting permissions
         */
        requestPermissions();

        ll = findViewById(R.id.ll);
        driveView = findViewById(R.id.driveId);
        tipsView = findViewById(R.id.tipsId);
        troubleView = findViewById(R.id.troubleCodes);
        blackBoxView = findViewById(R.id.blackBoxId);

        drive = new Intent(this, Drive.class);
        driveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(drive);
            }
        });

        tips = new Intent(this, Trip.class);
        tipsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(tips);
            }
        });

        troubleCodes = new Intent(this, TroubleCodes.class);
        troubleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(troubleCodes);
            }
        });

        blackBox = new Intent(this, BlackBox.class);
        blackBoxView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(blackBox);

                new MaterialAlertDialogBuilder(Home.this)
                        .setTitle("Black Box")
                        .setMessage("All log files are now compressed. You can go to file location.\nFile location is: \n" + Constants.BLACK_BOX_DIRECTORY)
                        .setNegativeButton("Close", null)
                        .show();
            }
        });
    }

    public static boolean checkPermission(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void requestPermissions() {
        checkPermission(this, permissions);
        if (!checkPermission(this, permissions)) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_ALL);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exit:
                finishAndRemoveTask();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
