package com.demotxt.droidsrce.homedashboard.io;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

public class CSVWriter {
    private static final String PATH = "storage/emulated/0/Driverify/Logs";
    private String path;
    private PrintWriter writer;

    public String getPath() {
        return path;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public CSVWriter() throws IOException {
        File lvFile = new File(PATH);
        File lvLogFile = new File(lvFile.getAbsolutePath() + new Date().toGMTString() + ".csv");
        if(!lvFile.exists()){
            lvFile.mkdirs();
        }
        if(!lvLogFile.exists()){
            lvLogFile.createNewFile();
        }

        this.path = lvLogFile.getAbsolutePath();

        this.writer = new PrintWriter(new FileWriter(this.path));
    }

    public boolean write(String string) throws IOException {
        if(this.path != null) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(this.path));
            writer.write(string);

            writer.close();
            return true;
        }
        return false;

    }
}
