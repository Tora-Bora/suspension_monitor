package com.example.max.suspensionmonitor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void buttonStopClick(View view) {
    }

    public void buttonStartClick(View view) {

        Intent i = new Intent(MainActivity.this, DeviceListActivity.class);
        startActivity(i);
    }
}
