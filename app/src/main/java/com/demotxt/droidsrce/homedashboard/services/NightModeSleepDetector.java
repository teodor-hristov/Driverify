package com.demotxt.droidsrce.homedashboard.services;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.demotxt.droidsrce.homedashboard.Utils.Alarm;
import com.demotxt.droidsrce.homedashboard.Utils.Constants;
import com.demotxt.droidsrce.homedashboard.Utils.Methods;

public class NightModeSleepDetector extends IntentService {
    private static String TAG = "Test";
    private double timeInterval = Constants.STARTING_TIME_INTERVAL_NIGHT_DRIVE;
    private long startTime = 0;
    private boolean status = false;
    private Alarm alarm;
    private MediaPlayer mediaPlayer;

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
                    alarm.pause();
                    Log.i(TAG, "caknah beliq ekran");
                    break;
            }
        }
    };

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i(TAG, NightModeSleepDetector.class.getSimpleName() + " started...");
        Log.i(TAG, "Thread id: " + Thread.currentThread().getId());
        alarm = new Alarm(mediaPlayer);
        registerReceiver(receiver, new IntentFilter("click"));

        timeChecker();
    }

    private void timeChecker() {
        long currentTime;
        startTime = System.currentTimeMillis();
        while (true) {
            if (status)
                break;

            currentTime = System.currentTimeMillis();
            if (Math.abs(startTime - currentTime) >= Methods.secondsToMillis(timeInterval)) {
                startSleepPrevention();
                startTime = System.currentTimeMillis();
                Log.i(TAG, "puskam beliq ekran");
            }
        }
    }

    private void startSleepPrevention() {
        Intent intent = new Intent(Constants.NIGHT_SLEEP_PREVENTION);
        sendBroadcast(intent);
        try {
            Thread.sleep(2800);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        alarm.play();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        status = true;
        Log.i(TAG, NightModeSleepDetector.class.getSimpleName() + " stopped...");
    }

    private void manageSleepPreventionInterval(long stopTimestamp) {
        int difference = Methods.millisToSeconds(stopTimestamp - startTime);
        if (difference >= 0 && difference <= 4) {
            timeInterval += 0.5 * timeInterval;
            Log.i(TAG, "+= 0.5 * timeInterval;");
        } else if (difference > 4 && difference <= 7) {
            timeInterval += 0.3 * timeInterval;
            Log.i(TAG, "+= 0.3 * timeInterval;");
        } else if (difference > 7 && difference <= 10) {
            timeInterval -= 0.8 * timeInterval;
            Log.i(TAG, "-= 0.8 * timeInterval;");
        } else {
            timeInterval = 1;
            Log.i(TAG, "= 1;");
        }
        Log.i(TAG, "Time interval: " + timeInterval);
    }
}
