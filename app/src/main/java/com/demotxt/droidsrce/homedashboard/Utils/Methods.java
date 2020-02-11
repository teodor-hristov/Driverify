package com.demotxt.droidsrce.homedashboard.Utils;

import android.app.ActivityManager;
import android.content.Context;

import static androidx.core.content.ContextCompat.getSystemService;

public class Methods {
    public static boolean isServiceRunning(Context context, Class serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
