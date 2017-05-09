package com.example.max.suspensionmonitor.Services;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.max.suspensionmonitor.Communications.BluetoothThread;
import com.example.max.suspensionmonitor.Concrete.ISampleReceiver;
import com.example.max.suspensionmonitor.Concrete.ISampleReader;
import com.example.max.suspensionmonitor.Concrete.V1SampleAnalizer;
import com.example.max.suspensionmonitor.Concrete.V1SampleReader;
import com.example.max.suspensionmonitor.Constants;
import com.example.max.suspensionmonitor.DeviceListActivity;
import com.example.max.suspensionmonitor.Domain.AnalisisData;
import com.example.max.suspensionmonitor.Domain.SampleV1;
import com.example.max.suspensionmonitor.MainActivity;
import com.example.max.suspensionmonitor.R;

import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;

public class BluetoothService extends WorkerService {
    private BluetoothAdapter btAdapter = null;


    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    LinkedList<BluetoothThread> mBluetoothThreads = new LinkedList<BluetoothThread>();

    public BluetoothService() {
    }


    @Override
    protected void Connect(String address) {
        Handler v1Sample1Handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                //TODO: Write data to collector

                SampleV1 sample = (SampleV1) msg.obj;
                sampleAnalizer.ReceiveV1Sample(sample);


                if (sampleReceiver != null) {
                    //sampleReceiver.ReceiveV1Sample((SampleV1) msg.obj);
                    AnalisisData andata = sampleAnalizer.GetAnalisis();
                    sampleReceiver.ReceiveSample(sampleAnalizer.ConvertPos(sample.mPos), sample.mV, andata.sag, andata.dynamicSag, sample.mTime);
                }

            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        StartThread(new V1SampleReader(), v1Sample1Handler, address);
    }


    private void StartThread(ISampleReader reader, Handler handler, String address)
    {
        BluetoothThread bt = new BluetoothThread(this.ConnectRemoteDevice(address), reader, handler);
        bt.start();
        mBluetoothThreads.add(bt);
    }

    public void StopAllThreads()
    {
        for (BluetoothThread thread : mBluetoothThreads)
        {
            if (thread.isAlive())
                thread.interrupt();
        }
        mBluetoothThreads.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        StopAllThreads();
        Log.d(TAG, "SERVICE DESTROYED");
    }

    private BluetoothDevice ConnectRemoteDevice(String address) {
        return btAdapter.getRemoteDevice(address);
    }



}
