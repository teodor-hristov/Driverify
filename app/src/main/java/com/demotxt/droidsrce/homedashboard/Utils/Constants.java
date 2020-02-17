package com.demotxt.droidsrce.homedashboard.Utils;

public class Constants {
    public static final String connected = "BLUETOOTH_CONNECTED";
    public static final String disconnected = "BLUETOOTH_DISCONNECTED";
    public static final String receiveData = "OBD_DATA_RECEIVE";
    public static final String extra = "INTENT_EXTRA_DATA";
    public static final String DTC = "INTENT_TROUBLE_CODES";
    public static final String GPSEnabled = "GPS_ENABLE";
    public static final String GPSDisabled = "GPS_DISABLE";
    public static final String GPSLiveData = "GPS_LIVE_DATA";
    public static final String GPSPutExtra = "coordinates";
    public static final String DataLogPath = "storage/emulated/0/Driverify/Logs/";
    public static final String timeIntervalKey = "obd_update_period_preference";

    public static final int saveSeconds = 10;
    public static final String obdDataCSVHeader = "rpm speed coolant load fuel_level timestamp";
}
