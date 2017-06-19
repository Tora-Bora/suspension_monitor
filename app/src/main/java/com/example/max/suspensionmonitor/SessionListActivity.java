package com.example.max.suspensionmonitor;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.max.suspensionmonitor.Domain.RideSession;

import java.io.File;
import java.io.FilenameFilter;

import static com.example.max.suspensionmonitor.AnalysisActivity.EXTRA_ANALISIS_DATA;
import static com.example.max.suspensionmonitor.R.string.connecting;

public class SessionListActivity extends AppCompatActivity {

    private ArrayAdapter<String> mSessionArrayAdapter;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_monitor:
                //ShowSessionListActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_list);


//        BottomNavigationView bottomNavigationView = (BottomNavigationView)findViewById(R.id.navigation);
//        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                switch (item.getItemId()) {
//                    case R.id.action_monitor:
//                        //ShowSessionListActivity();
//                        return true;
//                    default:
//                        return false;
//                }
//            }
//        });

    }


    @Override
    protected void onResume() {
        super.onResume();


        mSessionArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        // Find and set up the ListView for paired devices
        ListView pairedListView = (ListView) findViewById(R.id.sessions);
        pairedListView.setAdapter(mSessionArrayAdapter);
        pairedListView.setOnItemClickListener(mClickListener);

        File dir = getFilesDir();
        File[] subFiles = dir.listFiles(new FilenameFilter() {
                                            public boolean accept(File dir, String name) {
                                                return name.toLowerCase().endsWith(".sa");
                                            }});
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
            String info = ((TextView) v).getText().toString();

            RideSession data = RideSession.Desrialize(SessionListActivity.this, info);

            Intent intent = new Intent(SessionListActivity.this, AnalysisActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.putExtra(AnalysisActivity.ANALISIS_TYPE, AnalysisActivity.ANALISIS_LOAD);
            intent.putExtra(EXTRA_ANALISIS_DATA, data.analisisData);
            startActivity(intent);

            // Make an intent to start next activity while taking an extra which is the MAC address.
            //Intent i = new Intent(DeviceListActivity.this, MainActivity.class);
            //i.putExtra(EXTRA_DEVICE_ADDRESS, address);
            //startActivity(i);
        }
    };
}
