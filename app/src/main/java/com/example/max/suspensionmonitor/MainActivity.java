package com.example.max.suspensionmonitor;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.max.suspensionmonitor.Concrete.ISampleReceiver;
import com.example.max.suspensionmonitor.Domain.AnalisisData;
import com.example.max.suspensionmonitor.Services.BluetoothService;
import com.example.max.suspensionmonitor.Services.EmulatorService;
import com.example.max.suspensionmonitor.Services.WorkerService;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import static com.example.max.suspensionmonitor.AnalysisActivity.EXTRA_ANALISIS_DATA;

public class MainActivity extends AppCompatActivity implements ISampleReceiver {

    private static final int MY_PERMISSIONS_REQUEST_CREATE_FILE = 1;
    final String LOG_TAG = "myLogs";

    private LineGraphSeries<DataPoint> mSeriesPos;
    private LineGraphSeries<DataPoint> mSeriesVel;
    //private GraphView mGraph;

    WorkerService monitoringService;
    boolean bound = false;

    private Intent serviceIntent = null;
    private BluetoothAdapter btAdapter = null;
    // String for MAC address
    private static String address;

    private void ShowSessionListActivity() {
        Intent intent = new Intent(this, SessionListActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bottom_nav, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_measurements:
                ShowSessionListActivity();
                return true;
            case R.id.action_monitor:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        BottomNavigationView bottomNavigationView = (BottomNavigationView)findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_measurements:
                        ShowSessionListActivity();
                        return true;
                    case R.id.action_monitor:
                        return true;
                    default:
                        return false;
                }
            }
        });


        GraphView graph = (GraphView) findViewById(R.id.graph);
        mSeriesPos = new LineGraphSeries<>();
        mSeriesPos.setColor(Color.BLUE);
        graph.addSeries(mSeriesPos);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(5);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(200);

        graph.getViewport().setScalable(true);
        //graph.getViewport().setScalableY(true);
        graph.getViewport().setScrollable(true);
        //graph.getViewport().setScrollableY(true);

        mSeriesVel = new LineGraphSeries<>();
        mSeriesVel.setColor(Color.RED);
        graph.addSeries(mSeriesVel);

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        if (address.startsWith("emulator")) {
            serviceIntent = new Intent(this, EmulatorService.class);
        } else {
            serviceIntent = new Intent(this, BluetoothService.class);
        }

        serviceIntent.putExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS, address);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        bound = true;


    }


    public void actualiszeToggleButtonText() {

        Button toggleButton = (Button) findViewById(R.id.toggleButton);

        if (monitoringService.IsAnalisisStarted()) {
            toggleButton.setText("Stop");
        } else {
            toggleButton.setText("Start");
        }

    }

    private void RequestPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                Toast.makeText(this, "shouldShowRequestPermissionRationale", Toast.LENGTH_SHORT).show();

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_CREATE_FILE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        else {
            monitoringService.StartAnalizing();
        }
    }

    public void ResetGraph()
    {
        GraphView graph = (GraphView) findViewById(R.id.graph);
        graph.removeAllSeries();

        mSeriesPos = new LineGraphSeries<>();
        mSeriesPos.setColor(Color.BLUE);
        graph.addSeries(mSeriesPos);

        mSeriesVel = new LineGraphSeries<>();
        mSeriesVel.setColor(Color.RED);
        graph.addSeries(mSeriesVel);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CREATE_FILE: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    monitoringService.StartAnalizing();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
                    monitoringService.StartAnalizing();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void ToggleAnalizing() {

        if (monitoringService.IsAnalisisStarted()) {
            AnalisisData data = monitoringService.FinishAnalizing();

            // TODO: Show AnalysisActivity
            Intent intent = new Intent(this, AnalysisActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.putExtra(EXTRA_ANALISIS_DATA, data);
            startActivity(intent);

        }else {
            //monitoringService.StartAnalizing();
            this.RequestPermissions();
        }
    }

    public void buttonToggleClick(View view) {
        ToggleAnalizing();
        actualiszeToggleButtonText();
    }

    public void buttonCalibrateClick(View view) {
        monitoringService.Calibrate();
        actualiszeToggleButtonText();
    }

    /** Callbacks for service binding, passed to bindService() */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // cast the IBinder and get MyService instance
            WorkerService.LocalBinder binder = (WorkerService.LocalBinder) service;
            monitoringService = binder.getService();
            actualiszeToggleButtonText();
            bound = true;
            monitoringService.setSampleReceiver(MainActivity.this); // register
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    @Override
    public void ReceiveSample(double position, double velocity, double sag, double dynamicSag, double time) {
        if ( (time - mSeriesPos.getHighestValueX()) > 0.001) {
            mSeriesPos.appendData(new DataPoint(time, position), true, 5000);
            //mSeriesVel.appendData(new DataPoint(time, velocity), true, 5000);

            TextView sagView = (TextView) findViewById(R.id.sag);
            sagView.setText(String.format("%.2f%%", sag));

            TextView dsagView = (TextView) findViewById(R.id.dsag);
            sagView.setText(String.format("%.2f%%", dynamicSag));
        }
    }

    @Override
    protected void onStop() {
        Log.d(LOG_TAG, "onStop");
        super.onStop();
        // Unbind from service
        if (bound) {

            monitoringService.setSampleReceiver(null); // unregister
            unbindService(serviceConnection);
            bound = false;

            if (!monitoringService.IsAnalisisStarted()) {
                monitoringService.stopForeground(true);
            }
        }
    }

    @Override
    public void onResume() {
        Log.d(LOG_TAG, "onResume");
        super.onResume();
        if (!bound) {
            ResetGraph();
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            bound = true;
        }
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {
        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }
}
