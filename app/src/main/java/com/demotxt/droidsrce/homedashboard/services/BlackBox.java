package com.demotxt.droidsrce.homedashboard.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.demotxt.droidsrce.homedashboard.Utils.Constants;
import com.demotxt.droidsrce.homedashboard.io.ZipManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BlackBox extends IntentService {
    private final String TAG = "BlackBox";

    public BlackBox() {
        super("BlackBox");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, BlackBox.class.getSimpleName() + " started...");
        Log.i(TAG, "Thread id: " + Thread.currentThread().getId());

        ZipManager zipManager = new ZipManager();
        File logs = new File(Constants.DATA_LOG_PATH);
        File blackBoxDir = new File(Constants.BLACK_BOX_DIRECTORY);
        String[] logFiles;

        logFiles = logs.list();
        int len = logFiles.length;
        for (int i = 0; i < len; i++) {
            logFiles[i] = Constants.DATA_LOG_PATH + logFiles[i];
        }

        if (!blackBoxDir.exists()) {
            blackBoxDir.mkdirs();
        }

        zipManager.zip(logFiles, Constants.BLACK_BOX_DIRECTORY + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()) + ".zip");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
