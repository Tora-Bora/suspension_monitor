package com.example.max.suspensionmonitor.Communications;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import com.example.max.suspensionmonitor.Concrete.ISampleReader;
import com.example.max.suspensionmonitor.Domain.Sample;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Max on 21.02.2017.
 */

public class BluetoothThread extends Thread {
    private final String TAG = "BluetoothTrehad";
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private final ISampleReader mmSampleReader;
    private InputStream mmInStream;
    private OutputStream mmOutStream;
    private Handler mmSampleHandler;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public BluetoothThread(BluetoothDevice device, ISampleReader reader, Handler sampleHandler) {
        Log.d(TAG, "IN CONNECTING THREAD");
        mmDevice = device;
        BluetoothSocket temp = null;
        mmSampleReader = reader;
        mmSampleHandler = sampleHandler;
        Log.d(TAG, "BT UUID : " + BTMODULEUUID);
        try {
            temp = mmDevice.createRfcommSocketToServiceRecord(BTMODULEUUID);
            Log.d(TAG, "SOCKET CREATED : " + temp.toString());
        } catch (IOException e) {
            Log.d(TAG, "SOCKET CREATION FAILED :" + e.toString());
        }
        mmSocket = temp;
    }

    @Override
    public void run() {
        super.run();
        Log.d(TAG, "IN CONNECTING THREAD RUN");
        // Establish the Bluetooth socket connection.
        try {
            mmSocket.connect();
            Log.d(TAG, "BT SOCKET CONNECTED");

            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                //Create I/O streams for connection
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                Log.d(TAG, e.toString());
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;

            //I send a character when resuming.beginning transmission to check device is connected
            //If it is not an exception will be thrown in the write method and finish() will be called
            this.write("x");
            this.read();
        } catch (IOException e) {
            Log.d(TAG, "SOCKET CONNECTION FAILED : " + e.toString());
        } catch (IllegalStateException e) {
            Log.d(TAG, "CONNECTED THREAD START FAILED : " + e.toString());
        }

        this.closeStreams();
        this.closeSocket();
    }


    public void read()
    {
        // Keep looping to listen for received messages
        while (true && !this.isInterrupted() ) {
            try {
                Sample sample = mmSampleReader.ReadSample(mmInStream);
                if (sample != null) {
                    mmSampleHandler.obtainMessage(0, 0, -1, sample).sendToTarget();
                }
            } catch (IOException e) {
                Log.d(TAG, e.toString());
                break;
            }
        }
    }

    public void write(String input) {
        byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
        try {
            mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
        } catch (IOException e) {
            //if you cannot write, close the application
            Log.d(TAG, "UNABLE TO READ/WRITE " + e.toString());
        }
    }

    public void closeStreams() {
        try {
            //Don't leave Bluetooth sockets open when leaving activity
            mmInStream.close();
            mmOutStream.close();
        } catch (IOException e2) {
            //insert code to deal with this
            Log.d(TAG, e2.toString());
        }
    }

    public void closeSocket() {
        try {
            //Don't leave Bluetooth sockets open when leaving activity
            mmSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
            Log.d(TAG, e2.toString());
        }
    }
}
