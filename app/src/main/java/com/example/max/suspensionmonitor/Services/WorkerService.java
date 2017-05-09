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
import com.example.max.suspensionmonitor.Concrete.ISampleReader;
import com.example.max.suspensionmonitor.Concrete.ISampleReceiver;
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

public class WorkerService extends Service {
    public WorkerService() {
    }

    public static boolean IS_SERVICE_RUNNING = false;
    // Binder given to clients
    protected final IBinder binder = new WorkerService.LocalBinder();
    // Registered callbacks
    protected ISampleReceiver sampleReceiver;

    protected V1SampleAnalizer sampleAnalizer = new V1SampleAnalizer();
    protected boolean analisisStarted = false;

    protected static final String TAG = "myLogs";

    protected boolean isConnected = false;




    public void ToggleAnalizing() {
        if (IsAnalisisStarted()) {
            FinishAnalizing();
        } else {
            StartAnalizing();
        }
    }

    public void StartAnalizing() {
        sampleAnalizer.ResetAnalisis();
        analisisStarted = true;
    }

    public AnalisisData FinishAnalizing() {
        AnalisisData data = sampleAnalizer.GetAnalisis();
        data.stopDate = new Date(System.currentTimeMillis());
        data.histogramData.Normalize();
        sampleAnalizer.ResetAnalisis();

        analisisStarted = false;

        return data;
    }

    public void Calibrate() {
        sampleAnalizer.Calibrate();
    }

    public boolean IsAnalisisStarted() {
        return analisisStarted;
    }

    // Class used for the client Binder.
    public class LocalBinder extends Binder {
        public WorkerService getService() {
            // Return this instance of MyService so clients can call public methods
            return WorkerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "SERVICE BINDED");
        // TODO: Return the communication channel to the service.

        if (!isConnected) {
            String address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
            Connect(address);
            isConnected = true;
        }

        showNotification();
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

    protected void Connect(String address) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "SERVICE STARTED");


        if (!isConnected) {
            String address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
            Connect(address);
            isConnected = true;
        }

        showNotification();

        return START_STICKY;
        //return super.onStartCommand(intent, flags, startId);
    }



    private void showNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
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
