package com.demotxt.droidsrce.homedashboard.io;

import java.util.ArrayList;
import java.util.Queue;

public class DataSynchronizer {
    private Queue<String> locationQueue, obdDataQueue, faceDataQueue;

    public DataSynchronizer(Queue<String> locationQueue, Queue<String> obdDataQueue, Queue<String> faceDataQueue) {
        this.locationQueue = locationQueue;
        this.obdDataQueue = obdDataQueue;
        this.faceDataQueue = faceDataQueue;
    }

    public int Length() {
        return (locationQueue.size() + obdDataQueue.size() + faceDataQueue.size()) / 3;
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

    public void appendFaceData(String faceData) {
        if (faceDataQueue.size() >= 10) {
            faceDataQueue.clear();
        }

        faceDataQueue.add(faceData);
    }

    //rpm speed coolant load latitude longitude altitude sleep happiness timestamp
    public ArrayList<String> getSynchronizedData() {
        StringBuilder builder = new StringBuilder();
        ArrayList arrayList = new ArrayList<String>();
        String[] obdLine = null;
        String[] locationLine = null;
        String[] faceLine = null;
        String obdPart = null, locPart = null, facePart = null;

        while (true) {
            if (obdDataQueue.isEmpty() && locationQueue.isEmpty() && faceDataQueue.isEmpty()) {
                break;
            }
            if (!obdDataQueue.isEmpty()) {
                obdLine = obdDataQueue.poll().split(" ");
            } else {
                obdLine = new String[]{"N/A", "N/A", "N/A", "N/A", "0"};
            }
            if (!locationQueue.isEmpty()) {
                locationLine = locationQueue.poll().split(" ");
            } else {
                locationLine = new String[]{"N/A", "N/A", "N/A", "0"};
            }
            if (!faceDataQueue.isEmpty()) {
                faceLine = faceDataQueue.poll().split(" ");
            } else {
                faceLine = new String[]{"N/A", "N/A", "0"};
            }

            long obdTime = Long.parseLong(obdLine[4]);
            long locTime = Long.parseLong(locationLine[3]);
            long faceTime = Long.parseLong(faceLine[2]);
            long obdLoc = Math.abs(obdTime - locTime);
            long obdFace = Math.abs(obdTime - faceTime);


            if (obdLoc <= 1000) {
                obdPart = obdLine[0] + " " + obdLine[1] + " " + obdLine[2] + " " + obdLine[3] + " ";
                locPart = locationLine[0] + " " + locationLine[1] + " " + locationLine[2] + " ";
                if (obdFace <= 1000) {
                    facePart = faceLine[0] + " " + faceLine[1] + " " + faceLine[2];
                } else {
                    facePart = "N/A N/A " + obdTime;
                }
            } else {
                if (obdTime == 0) {
                    obdPart = "N/A " + "N/A " + "N/A " + "N/A ";
                } else {
                    obdPart = obdLine[0] + " " + obdLine[1] + " " + obdLine[2] + " " + obdLine[3] + " ";
                }
                if (locTime == 0) {
                    locPart = "N/A " + "N/A " + "N/A ";
                } else {
                    locPart = locationLine[0] + " " + locationLine[1] + " " + locationLine[2] + " ";
                }
                if (faceTime == 0) {
                    facePart = "N/A " + "N/A " + obdTime;
                } else {
                    facePart = faceLine[0] + " " + faceLine[1] + " " + faceLine[2];
                }

            }
            if (!faceLine[2].equals("0")) {
                builder.append(obdPart + locPart + facePart);
                arrayList.add(builder.toString());
                builder = new StringBuilder();
            }

        }

        return arrayList;
    }
}
