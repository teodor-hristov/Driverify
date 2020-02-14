package com.demotxt.droidsrce.homedashboard.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class DataController extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        /**
         * TODO: Create files, write in files...
         */
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /**
         * TODO: Close all writers, save all states, break connections
         */
    }
}
