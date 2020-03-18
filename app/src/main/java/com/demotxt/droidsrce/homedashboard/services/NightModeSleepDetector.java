package com.demotxt.droidsrce.homedashboard.services;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.demotxt.droidsrce.homedashboard.Utils.Constants;
import com.demotxt.droidsrce.homedashboard.Utils.Methods;

public class NightModeSleepDetector extends IntentService {
    private static String TAG = "Test";
    private double timeInterval = Constants.STARTING_TIME_INTERVAL_NIGHT_DRIVE;
    private long startTime = 0;
    private boolean status = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public NightModeSleepDetector() {
        super(NightModeSleepDetector.class.getName());
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            long stopTimestamp;
            switch (action) {
                case "click":
                    stopTimestamp = intent.getLongExtra("clickTime", 0);
                    manageSleepPreventionInterval(stopTimestamp);
                    Log.i(TAG, "caknah beliq ekran");
                    break;
            }
        }
    };

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i(TAG, NightModeSleepDetector.class.getSimpleName() + " started...");
        Log.i(TAG, "Thread id: " + Thread.currentThread().getId());
        registerReceiver(receiver, new IntentFilter("click"));

        timeChecker(startTime, timeInterval);
    }

    private void timeChecker(long startTime, int timeInterval) {
        long currentTime;

        while (true) {
            currentTime = System.currentTimeMillis();
            if (Math.abs(startTime - currentTime) >= Methods.secondsToMillis(timeInterval)) {
                startSleepPrevention();
            }
        }
    }

    private void startSleepPrevention() {
        Intent intent = new Intent(Constants.NIGHT_SLEEP_PREVENTION);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        Log.i(TAG, NightModeSleepDetector.class.getSimpleName() + " stopped...");
    }

    private void manageSleepPreventionInterval(long stopTimestamp) {
        int difference = Methods.millisToSeconds(stopTimestamp - startTime);
        if (difference >= 0 && difference <= 10) {
            timeInterval += 0.3 * timeInterval; // 1/10
            Log.i(TAG, "+= (1/10)*timeInterval;");
        } else if (difference > 10 && difference <= 30) {
            timeInterval = 0.25 * timeInterval; // 1/4
            Log.i(TAG, "= (1/4)*timeInterval;");
        } else {
            timeInterval = 1;
            Log.i(TAG, "= 1;");
        }
        Log.i(TAG, "Time interval: " + timeInterval);
    }
}
