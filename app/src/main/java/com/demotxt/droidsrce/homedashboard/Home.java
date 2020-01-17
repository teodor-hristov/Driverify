package com.demotxt.droidsrce.homedashboard;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import android.view.View;
import android.widget.LinearLayout;


public class Home extends AppCompatActivity {
    CardView troubleView, driveView ;
    Intent drive, trubleCodes ;
    LinearLayout ll;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ll = findViewById(R.id.ll);
        driveView = findViewById(R.id.driveId);
        troubleView = findViewById(R.id.troubleCodes);

        drive = new Intent(this, Drive.class);
        driveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(drive);
            }
        });

        trubleCodes = new Intent(this, TroubleCodes.class);
        troubleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(trubleCodes);
            }
        });
    }
}
