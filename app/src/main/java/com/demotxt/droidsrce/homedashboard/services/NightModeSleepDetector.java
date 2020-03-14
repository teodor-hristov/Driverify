package com.demotxt.droidsrce.homedashboard.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.demotxt.droidsrce.homedashboard.Utils.Constants;
import com.demotxt.droidsrce.homedashboard.Utils.Methods;

public class NightModeSleepDetector extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long currentTime;
        long startTime = System.currentTimeMillis();
        int timeInterval = Constants.STARTING_TIME_INTERVAL_NIGHT_DRIVE;

        while (true) {
            currentTime = System.currentTimeMillis();
            if (startTime - currentTime == Methods.secondsToMillis(timeInterval)) {
                //TODO: OnClick event. Increase/Decrease timeInterval and set visibitlity for sleepPrevention view
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
