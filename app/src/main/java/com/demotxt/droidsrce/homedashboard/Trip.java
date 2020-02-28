package com.demotxt.droidsrce.homedashboard;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.demotxt.droidsrce.homedashboard.Utils.Constants;
import com.demotxt.droidsrce.homedashboard.display.ListViewTripDisplayAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Trip extends AppCompatActivity {

    private ListViewTripDisplayAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);

        setFileDataAdapter();
        setupRecyclerView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setFileDataAdapter() {
        File directory = new File(Constants.DATA_LOG_PATH);
        List<File> files = new ArrayList<>();
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                files.add(file);
            }
        }
        Collections.sort(files, Collections.<File>reverseOrder());
        mAdapter = new ListViewTripDisplayAdapter(getApplicationContext(), files);
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mAdapter);
    }
}
