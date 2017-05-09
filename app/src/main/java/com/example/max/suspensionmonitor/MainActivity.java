package com.example.max.suspensionmonitor;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_measurements:
                ShowSessionListActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        //startService(serviceIntent);
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

    private void ToggleAnalizing() {

        if (monitoringService.IsAnalisisStarted()) {
            AnalisisData data = monitoringService.FinishAnalizing();

            // TODO: Show AnalysisActivity
            Intent intent = new Intent(this, AnalysisActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.putExtra(EXTRA_ANALISIS_DATA, data);
            startActivity(intent);

        }else {
            monitoringService.StartAnalizing();
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
        }
    }

    @Override
    public void onResume() {
        Log.d(LOG_TAG, "onResume");
        super.onResume();
        if (!bound) {
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
