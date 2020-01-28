package com.demotxt.droidsrce.homedashboard.io;

import android.os.Parcel;
import android.os.Parcelable;

import com.github.pires.obd.commands.ObdCommand;

import java.util.ArrayList;

public class ObdReaderData implements Parcelable{
    private ArrayList<String> commands;

    protected ObdReaderData(Parcel in) {
        setCommands(in.readArrayList(String.class.getClassLoader()));
    }


    public static final Creator<ObdReaderData> CREATOR = new Creator<ObdReaderData>() {
        @Override
        public ObdReaderData createFromParcel(Parcel in) {
            return new ObdReaderData(in);
        }

        @Override
        public ObdReaderData[] newArray(int size) {
            return new ObdReaderData[size];
        }
    };

    public ArrayList<String> getCommands() {
        return commands;
    }

    public void setCommands(ArrayList<String> commands) {
        if(commands != null)
        this.commands = commands;
    }

    public ObdReaderData(ArrayList<String> cmds){
        setCommands(cmds);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeList(getCommands());
    }
}
