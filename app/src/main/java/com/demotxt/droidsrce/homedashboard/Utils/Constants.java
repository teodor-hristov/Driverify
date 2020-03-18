package com.demotxt.droidsrce.homedashboard.Utils;

public class Constants {
    public static final String CONNECTED = "BLUETOOTH_CONNECTED";
    public static final String DISCONNECTED = "BLUETOOTH_DISCONNECTED";
    public static final String RECEIVE_DATA = "OBD_DATA_RECEIVE";
    public static final String EXTRA = "INTENT_EXTRA_DATA";
    public static final String DTC = "INTENT_TROUBLE_CODES";
    public static final String GPS_ENABLED = "GPS_ENABLE";
    public static final String GPS_DISABLED = "GPS_DISABLE";
    public static final String GPS_LIVE_DATA = "GPS_LIVE_DATA";
    public static final String GPS_PUT_EXTRA = "coordinates";
    public static final String DATA_LOG_PATH = "storage/emulated/0/Driverify/Logs/";
    public static final String LOCATON_LIVE_DATA_PATH = "storage/emulated/0/Driverify/Logs/Location/";
    public static final String FACE_DATA_PATH = "storage/emulated/0/Driverify/Logs/Face/";
    public static final String TIME_INTERVAL_KEY = "obd_update_period_preference";
    public static final int SAVE_SECONDS = 10;
    public static final String UNITED_DATA_HEADER = "rpm speed coolant load latitude longitude altitude sleep happiness timestamp";
    public static final String LOCATION_HEADER_CSV = "latitude longitude altitude timestamp";
    public static final String CHECKOUT_TRIP = "checkout_trip";
    public static final String FACE_DATA = "FACE_DATA";
    public static final String FACE_DATA_HEADER = "sleep happiness timestamp";
    public static final int MAX_RPM = 7000;
    public static final int MAX_COOLANT = 180;
    public static final int MAX_SPEED = 260;
    public static final int MAX_LOAD = 1000;
    public static final String AMBIENT_LIGHT_DATA = "ambient_light";
    public static final String NIGHT_SLEEP_PREVENTION = "night_sleep";

    public static final String NIGHT_BG_COLOR = "#4d4646";
    public static final String NIGHT_TEXT_COLOR = "#f5eaea";
    public static final String DAY_BG_COLOR = "#f4f4f4";
    public static final String DAY_TEXT_COLOR = "#FF212121";
    public static final float AMBIENT_LIGHT_CONSTANT_FOR_NIGHT = 13;

    public static final int CONSTANT_SPEED_TO_CHECK_IF_DRIVER_IS_SLEEPING = 10;
    public static final double FUEL_ECONOMY_CONSTANT = 88.4;
    public static final int STARTING_TIME_INTERVAL_NIGHT_DRIVE = 3;

}
