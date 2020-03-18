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
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "rec ok");
            String action = intent.getAction();
            long stopTimestamp;
            switch (action) {
                case "click":
                    stopTimestamp = intent.getLongExtra("clickTime", 0);
                    timeChecker(stopTimestamp, Constants.STARTING_TIME_INTERVAL_NIGHT_DRIVE);
                    Log.i(TAG, "onReceive: night mode");
                    break;
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public NightModeSleepDetector() {
        super(NightModeSleepDetector.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i(TAG, NightModeSleepDetector.class.getSimpleName() + " started...");
        Log.i(TAG, "Thread id: " + Thread.currentThread().getId());
        registerReceiver(receiver, new IntentFilter("click"));
        long startTime = System.currentTimeMillis();
        int timeInterval = Constants.STARTING_TIME_INTERVAL_NIGHT_DRIVE;

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        Log.i(TAG, NightModeSleepDetector.class.getSimpleName() + " stopped...");
    }

    private void startSleepPrevention() {
        Intent intent = new Intent(Constants.NIGHT_SLEEP_PREVENTION);
        sendBroadcast(intent);
    }

    private void manageSleepPreventionInterval() {

    }
}
