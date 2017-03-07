package com.example.max.suspensionmonitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.max.suspensionmonitor.Communications.ISampleReceiver;
import com.example.max.suspensionmonitor.Concrete.SpeedHistogramData;
import com.example.max.suspensionmonitor.Domain.SampleJY;
import com.example.max.suspensionmonitor.Domain.SampleV1;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class MonitoringActivity extends AppCompatActivity implements ISampleReceiver {

    final String LOG_TAG = "myLogs";

    BluetoothService myService;
    boolean bound = false;

    Button btnOn, btnOff;
    TextView txtString;
    Handler bluetoothIn;

    final int handlerState = 0;        				 //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private SpeedHistogramData mHistogramData;


    // String for MAC address
    private static String address;

    private BarGraphSeries<DataPoint> mSeries;

    private LineGraphSeries<DataPoint> mSeriesAX;
    private LineGraphSeries<DataPoint> mSeriesAY;
    private LineGraphSeries<DataPoint> mSeriesAZ;

    private double graphLastXValue = 0d;

    Intent serviceIntent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        mHistogramData = new SpeedHistogramData(1, 6);
        GraphView graph = (GraphView) findViewById(R.id.graph);
        graph.getViewport().setMinX(-6);
        graph.getViewport().setMaxX(6);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(10);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);

        mSeries = new BarGraphSeries<>();
        UpdateSpeedHistogram();
        graph.addSeries(mSeries);

        GraphView graphAcl = (GraphView) findViewById(R.id.graphacl);
        mSeriesAX = new LineGraphSeries<>();
        mSeriesAX.setColor(Color.BLUE);
        mSeriesAY = new LineGraphSeries<>();
        mSeriesAY.setColor(Color.RED);
        mSeriesAZ = new LineGraphSeries<>();
        mSeriesAZ.setColor(Color.YELLOW);
        graphAcl.addSeries(mSeriesAX);
        //graphAcl.addSeries(mSeriesAY);
        graphAcl.addSeries(mSeriesAZ);
        graphAcl.getViewport().setXAxisBoundsManual(true);
        graphAcl.getViewport().setMinX(0);
        graphAcl.getViewport().setMaxX(5);
        graphAcl.getViewport().setYAxisBoundsManual(true);
        graphAcl.getViewport().setMinY(-8);
        graphAcl.getViewport().setMaxY(8);

        graphAcl.getViewport().setScalable(true);
        //graphAcl.getViewport().setScalableY(true);
        graphAcl.getViewport().setScrollable(true);
        //graphAcl.getViewport().setScrollableY(true);



        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        serviceIntent = new Intent(this, BluetoothService.class);
        serviceIntent.putExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS, address);
        startService(serviceIntent);
    }


    /** Callbacks for service binding, passed to bindService() */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // cast the IBinder and get MyService instance
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            myService = binder.getService();
            bound = true;
            myService.setSampleReceiver(MonitoringActivity.this); // register
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    @Override
    public void ReceiveV1Sample(SampleV1 sample) {

        mHistogramData.AddV1Sample(sample);
        UpdateSpeedHistogram();

        if ( (sample.mTime - mSeriesAZ.getHighestValueX()) > 0.001) {
            mSeriesAZ.appendData(new DataPoint(sample.mTime, sample.mV), true, 5000);
        }

//        mSeries.appendData(new DataPoint(sample.mTime / 10.0, sample.mPotentiometer), true, 400);
//        mSeriesAX.appendData(new DataPoint(sample.mTime / 10.0, sample.mAccelX), true, 400);
//        mSeriesAY.appendData(new DataPoint(sample.mTime / 10.0, sample.mAccelY), true, 400);
//        mSeriesAZ.appendData(new DataPoint(sample.mTime / 10.0, sample.mAccelZ), true, 400);

    }

    @Override
    public void ReceiveJYSampleA1(SampleJY sample) {

        if ( (sample.mDTime - mSeriesAZ.getHighestValueX()) > 0.001)
        {
            mHistogramData.AddSpeedSample(sample.mVZ * 2, sample.mDTime - mSeriesAZ.getHighestValueX());
            UpdateSpeedHistogram();

            mSeriesAZ.appendData(new DataPoint(sample.mDTime , sample.mAccel.z - 9.81), true, 5000);
            mSeriesAX.appendData(new DataPoint(sample.mDTime , sample.mVZ * 5), true, 5000);
        }
    }

    public void ReceiveJYSampleA2(SampleJY sample) {

    }

    public void UpdateSpeedHistogram()
    {
        DataPoint[] histPoints = new DataPoint[mHistogramData.GetHistogram().size()];
        double timeMax = 0;

        for(int i = 0; i < mHistogramData.GetHistogram().size(); i++)
        {
            SpeedHistogramData.VInterval interval = mHistogramData.GetHistogram().get(i);
            timeMax = Math.max(interval.mTotalTime, timeMax);
        }

        for(int i = 0; i < mHistogramData.GetHistogram().size(); i++)
        {
            SpeedHistogramData.VInterval interval = mHistogramData.GetHistogram().get(i);
            double normalizedYValue = interval.mTotalTime;
            if (timeMax >= 10) {
                normalizedYValue = normalizedYValue / timeMax;
                normalizedYValue = normalizedYValue * 10;
            }
            histPoints[i] = new DataPoint(interval.mTo, normalizedYValue);
        }
        mSeries.resetData(histPoints);

    }

    public void buttonStopClick(View v){
        if (bound) {
            myService.setSampleReceiver(null); // unregister
            unbindService(serviceConnection);
            bound = false;
        }
        stopService(new Intent(getBaseContext(), BluetoothService.class));
    }

    @Override
    protected void onStop() {
        Log.d(LOG_TAG, "onStop");
        super.onStop();
        // Unbind from service
        if (bound) {
            myService.setSampleReceiver(null); // unregister
            unbindService(serviceConnection);
            bound = false;
        }
    }


    @Override
    public void onResume() {
        Log.d(LOG_TAG, "onResume");
        super.onResume();
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onPause()
    {
        Log.d(LOG_TAG, "onPause");
        super.onPause();
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
