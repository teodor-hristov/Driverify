package com.demotxt.droidsrce.homedashboard.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class CSVWriter {
    private static String PATH;
    private String path;
    private BufferedWriter writer;

    public String getPath() {
        return path;
    }

    public BufferedWriter getWriter() {
        return writer;
    }

    public CSVWriter(String PATH) throws IOException {
        this.PATH = PATH;
        File lvFile = new File(PATH);
        File lvLogFile = new File(PATH + new Date().toGMTString() + ".csv");
        if(!lvFile.exists()){
            lvFile.mkdirs();
        }
        if(!lvLogFile.exists()){
            lvLogFile.createNewFile();
        }

        this.path = lvLogFile.getAbsolutePath();

        this.writer = new BufferedWriter(new FileWriter(this.path));
    }

    public boolean append(String string) throws IOException {
        if(this.writer != null) {
            writer.append(string);
            this.newLine();
            return true;
        }
        return false;

    }
    public void close() throws IOException {
        writer.close();
    }
    public void newLine() throws IOException {
        writer.newLine();
    }
}
