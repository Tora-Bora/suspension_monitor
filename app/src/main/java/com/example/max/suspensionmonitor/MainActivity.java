package com.example.max.suspensionmonitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import com.example.max.suspensionmonitor.Domain.SampleJY;
import com.example.max.suspensionmonitor.Domain.SampleV1;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements ISampleReceiver {

    final String LOG_TAG = "myLogs";

    BluetoothTelemetryService myService;
    boolean bound = false;

    Button btnOn, btnOff;
    TextView txtString;
    Handler bluetoothIn;

    final int handlerState = 0;        				 //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address;

    private LineGraphSeries<DataPoint> mSeries;

    private LineGraphSeries<DataPoint> mSeriesAX;
    private LineGraphSeries<DataPoint> mSeriesAY;
    private LineGraphSeries<DataPoint> mSeriesAZ;

    private double graphLastXValue = 0d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GraphView graph = (GraphView) findViewById(R.id.graph);
        mSeries = new LineGraphSeries<>();
        graph.addSeries(mSeries);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(40);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(1);


        GraphView graphAcl = (GraphView) findViewById(R.id.graphacl);
        mSeriesAX = new LineGraphSeries<>();
        mSeriesAX.setColor(Color.BLUE);
        mSeriesAY = new LineGraphSeries<>();
        mSeriesAY.setColor(Color.RED);
        mSeriesAZ = new LineGraphSeries<>();
        mSeriesAZ.setColor(Color.YELLOW);
        graphAcl.addSeries(mSeriesAX);
        graphAcl.addSeries(mSeriesAY);
        graphAcl.addSeries(mSeriesAZ);
        graphAcl.getViewport().setXAxisBoundsManual(true);
        graphAcl.getViewport().setMinX(0);
        graphAcl.getViewport().setMaxX(40);
        graphAcl.getViewport().setYAxisBoundsManual(true);
        graphAcl.getViewport().setMinY(-30);
        graphAcl.getViewport().setMaxY(30);



        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {										//if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread

                    recDataString.append(readMessage);      								//keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("\r\n");                    // determine the end-of-line

                    int count = 0;
                    while(endOfLineIndex > 0){
                        String data = recDataString.substring(0, endOfLineIndex);

                        String[] packet = data.split(";");
                        if(packet.length >= 5) {
                            double xValue = Integer.parseInt(packet[0]);
                            double pot = Double.parseDouble(packet[1]);
                            mSeries.appendData(new DataPoint(xValue / 10.0, pot), true, 400);

                            double ax = Double.parseDouble(packet[2]);
                            mSeriesAX.appendData(new DataPoint(xValue / 10.0, ax), true, 400);

                            double ay = Double.parseDouble(packet[3]);
                            mSeriesAY.appendData(new DataPoint(xValue / 10.0, ay), true, 400);

                            double az = Double.parseDouble(packet[4]);
                            mSeriesAZ.appendData(new DataPoint(xValue / 10.0, az), true, 400);
                        }
                        else {
                            Log.d(LOG_TAG, "Unexpected line: " + data);
                        }

                        recDataString.delete(0, endOfLineIndex + 2);
                        endOfLineIndex = recDataString.indexOf("\r\n");
                        count++;
                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();


    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }


    /** Callbacks for service binding, passed to bindService() */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // cast the IBinder and get MyService instance
            BluetoothTelemetryService.LocalBinder binder = (BluetoothTelemetryService.LocalBinder) service;
            myService = binder.getService();
            bound = true;
            myService.setSampleReceiver(MainActivity.this); // register
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    @Override
    public void ReceiveV1Sample(SampleV1 sample) {

        mSeries.appendData(new DataPoint(sample.mDTime / 10.0, sample.mPotentiometer), true, 400);
        mSeriesAX.appendData(new DataPoint(sample.mDTime / 10.0, sample.mAccelX), true, 400);
        mSeriesAY.appendData(new DataPoint(sample.mDTime / 10.0, sample.mAccelY), true, 400);
        mSeriesAZ.appendData(new DataPoint(sample.mDTime / 10.0, sample.mAccelZ), true, 400);

    }

    @Override
    public void ReceiveJYSample(SampleJY sample) {

    }

    @Override
    protected void onStop() {
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
        super.onResume();

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        Intent serviceIntent = new Intent(this, BluetoothTelemetryService.class);
        serviceIntent.putExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS, address);

        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        //create device and set the MAC address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                //insert code to deal with this
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
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

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {

//                    BufferedReader in = new BufferedReader(new InputStreamReader(mmInStream));
//                    String line = in.readLine();
//                    if (line != null) {
//                        bluetoothIn.obtainMessage(handlerState, line.length(), -1, line).sendToTarget();
//                    }


                    bytes = mmInStream.read(buffer);        	//read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();

                } catch (IOException e) {
                    Log.e(LOG_TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }

}
