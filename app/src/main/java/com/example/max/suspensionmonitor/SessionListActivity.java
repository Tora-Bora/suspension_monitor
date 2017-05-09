package com.example.max.suspensionmonitor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;

import static com.example.max.suspensionmonitor.R.string.connecting;

public class SessionListActivity extends AppCompatActivity {

    private ArrayAdapter<String> mSessionArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_list);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Find and set up the ListView for paired devices
        ListView pairedListView = (ListView) findViewById(R.id.sessions);
        pairedListView.setAdapter(mSessionArrayAdapter);
        pairedListView.setOnItemClickListener(mClickListener);

        File dir = getFilesDir();
        File[] subFiles = dir.listFiles();
        if (subFiles != null)
        {
            for (File file : subFiles)
            {
                // Here is each file
                mSessionArrayAdapter.add(file.getName());
            }
        }

    }

    // Set up on-click listener for the list (nicked this - unsure)
    private AdapterView.OnItemClickListener mClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            //textView1.setText(getResources().getText(connecting).toString());
            // Get the device MAC address, which is the last 17 chars in the View
            //String info = ((TextView) v).getText().toString();
            //String address = info.substring(info.length() - 17);

            // Make an intent to start next activity while taking an extra which is the MAC address.
            //Intent i = new Intent(DeviceListActivity.this, MainActivity.class);
            //i.putExtra(EXTRA_DEVICE_ADDRESS, address);
            //startActivity(i);
        }
    };
}
