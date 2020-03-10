package com.demotxt.droidsrce.homedashboard.io;

import java.util.Queue;

public class DataSynchronizer {
    private Queue<String> locationQueue, obdDataQueue;

    public DataSynchronizer(Queue<String> locationList, Queue<String> obdDataList) {
        this.locationQueue = locationList;
        this.obdDataQueue = obdDataList;
    }

    public void appendLocation(String locationData) {
        if (locationQueue.size() >= 10) {
            locationQueue.clear();
        }

        locationQueue.add(locationData);
    }

    public void appendObdData(String obdData) {
        if (obdDataQueue.size() >= 10) {
            obdDataQueue.clear();
        }

        obdDataQueue.add(obdData);
    }

    //rpm speed coolant load latitude longitude altitude timestamp
    public String getSynchronizedData() {
        StringBuilder builder = null;
        int obdLen;
        int locationLen;
        long obdTimestamp;
        long locationTimestamp;

        for (String obdLine = obdDataQueue.peek(); !obdDataQueue.isEmpty(); obdLine = obdDataQueue.poll()) {
            obdLen = obdLine.split(" ").length;
            obdTimestamp = Long.parseLong(obdLine.split(" ")[obdLen - 1]);
            if (locationQueue.isEmpty()) {
                builder = new StringBuilder();
                builder.append(obdLine.split(" ")[0] + " ");
                builder.append(obdLine.split(" ")[1] + " ");
                builder.append(obdLine.split(" ")[2] + " ");
                builder.append(obdLine.split(" ")[3] + " ");
                builder.append("N/A N/A N/A ");
                builder.append(obdLine.split(" ")[4] + " ");

            } else {
                for (String locationLine = locationQueue.peek(); !locationQueue.isEmpty(); locationLine = locationQueue.poll()) {
                    locationLen = locationLine.split(" ").length;
                    locationTimestamp = Long.parseLong(locationLine.split(" ")[locationLen - 1]);

                    if (Math.abs(obdTimestamp - locationTimestamp) < 700) {
                        builder = new StringBuilder();
                        builder.append(obdLine.split(" ")[0] + " ");
                        builder.append(obdLine.split(" ")[1] + " ");
                        builder.append(obdLine.split(" ")[2] + " ");
                        builder.append(obdLine.split(" ")[3] + " ");
                        builder.append(locationLine);
                    }
                }
            }
        }

        return builder.toString();
    }
}
