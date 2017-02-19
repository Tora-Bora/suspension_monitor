package com.example.max.suspensionmonitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
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

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    final String LOG_TAG = "myLogs";

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
    private double meanDz = 0d;
    private int meanCount = 0;
    boolean finished = false;


    ArrayList<DataPoint> points = new ArrayList<DataPoint>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        GraphView graphAcl = (GraphView) findViewById(R.id.graphacl);
        mSeriesAX = new LineGraphSeries<>();
        mSeriesAX.setColor(Color.BLUE);
        mSeriesAY = new LineGraphSeries<>();
        mSeriesAY.setColor(Color.RED);
        mSeriesAZ = new LineGraphSeries<>();
        mSeriesAZ.setColor(Color.YELLOW);
        //graphAcl.addSeries(mSeriesAX);
        //graphAcl.addSeries(mSeriesAY);
        //graphAcl.addSeries(mSeriesAZ);
        graphAcl.getViewport().setXAxisBoundsManual(true);
        graphAcl.getViewport().setMinX(0);
        graphAcl.getViewport().setMaxX(5);
        graphAcl.getViewport().setYAxisBoundsManual(true);
        graphAcl.getViewport().setMinY(-15);
        graphAcl.getViewport().setMaxY(15);
        graphAcl.getViewport().setScalable(true);
        //graphAcl.getViewport().setScalableY(true);
        graphAcl.getViewport().setScrollable(true);
        //graphAcl.getViewport().setScrollableY(true);
        //GraphView graphAcl = (GraphView) findViewById(R.id.graphacl);
        graphAcl.addSeries(mSeriesAZ);





        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {										//if message is what we want
                    Sample sample = (Sample) msg.obj;                                                                // msg.arg1 = bytes from connect thread


                    //Log.d(LOG_TAG, "Sample: " + sample.toString());
                    if ( (sample.dt - graphLastXValue) >= 0.001) {

                        points.add(new DataPoint(sample.dt, sample.az));
                        mSeriesAZ.appendData(new DataPoint(sample.dt, sample.az), false, 5000);
                        graphLastXValue = sample.dt;
                        meanCount++;
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


    @Override
    public void onResume() {
        super.onResume();

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

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

        private JYBufferReader bufferReader = new JYBufferReader();

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

            // Keep looping to listen for received messages
            while (true) {
                Sample sample = bufferReader.ReadSample(mmInStream);
                if (sample != null) {
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, 0, -1, sample).sendToTarget();
                }
                //String readMessage = new String(buffer, 0, bytes);
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
