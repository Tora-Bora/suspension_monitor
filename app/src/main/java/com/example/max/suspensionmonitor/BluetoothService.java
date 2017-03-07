package com.example.max.suspensionmonitor;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.max.suspensionmonitor.Communications.BluetoothThread;
import com.example.max.suspensionmonitor.Communications.ISampleReceiver;
import com.example.max.suspensionmonitor.Concrete.ISampleReader;
import com.example.max.suspensionmonitor.Concrete.JYSampleReader;
import com.example.max.suspensionmonitor.Concrete.V1SampleReader;
import com.example.max.suspensionmonitor.Domain.Sample;
import com.example.max.suspensionmonitor.Domain.SampleJY;
import com.example.max.suspensionmonitor.Domain.SampleV1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.UUID;

public class BluetoothService extends Service {
    public static boolean IS_SERVICE_RUNNING = false;
    // Binder given to clients
    private final IBinder binder = new LocalBinder();
    // Registered callbacks
    private ISampleReceiver sampleReceiver;

    private static final String TAG = "myLogs";
    private BluetoothAdapter btAdapter = null;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    LinkedList<BluetoothThread> mBluetoothThreads = new LinkedList<BluetoothThread>();

    public BluetoothService() {
    }

    // Class used for the client Binder.
    public class LocalBinder extends Binder {
        BluetoothService getService() {
            // Return this instance of MyService so clients can call public methods
            return BluetoothService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "SERVICE BINDED");
        // TODO: Return the communication channel to the service.
        return binder;
    }

    public void setSampleReceiver(ISampleReceiver callbacks) {
        sampleReceiver = callbacks;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "SERVICE CREATED");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "SERVICE STARTED");


        String address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        Handler jySample1Handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                //TODO: Write data to collector
                if (sampleReceiver != null) {
                    sampleReceiver.ReceiveJYSampleA1((SampleJY) msg.obj);
                }
            }
        };

        Handler v1Sample1Handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                //TODO: Write data to collector
                if (sampleReceiver != null) {
                    sampleReceiver.ReceiveV1Sample((SampleV1) msg.obj);
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter

        //StartThread(new V1SampleReader(), v1Sample1Handler, address);
        StartThread(new JYSampleReader(), jySample1Handler, address);

        showNotification();

        return START_STICKY;
        //return super.onStartCommand(intent, flags, startId);
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

    private void showNotification() {
        Intent notificationIntent = new Intent(this, MonitoringActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);



        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_notifications_black_24dp);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Suspension monitor")
                .setTicker("Suspension monitor")
                .setContentText("Writing samples...")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                //.setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                notification);

    }

}
