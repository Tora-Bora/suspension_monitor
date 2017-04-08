package com.example.max.suspensionmonitor;
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
import com.example.max.suspensionmonitor.Domain.AnalisisData;
import com.example.max.suspensionmonitor.Domain.SampleV1;

import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;

import static com.example.max.suspensionmonitor.AnalysisActivity.EXTRA_ANALISIS_DATA;

public class BluetoothService extends Service {
    public static boolean IS_SERVICE_RUNNING = false;
    // Binder given to clients
    private final IBinder binder = new LocalBinder();
    // Registered callbacks
    private ISampleReceiver sampleReceiver;

    private V1SampleAnalizer sampleAnalizer = new V1SampleAnalizer();
    private boolean analisisStarted = false;

    private static final String TAG = "myLogs";
    private BluetoothAdapter btAdapter = null;

    private boolean isConnected = false;


    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    LinkedList<BluetoothThread> mBluetoothThreads = new LinkedList<BluetoothThread>();

    public BluetoothService() {
    }

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
        BluetoothService getService() {
            // Return this instance of MyService so clients can call public methods
            return BluetoothService.this;
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

    private void Connect(String address) {
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "SERVICE STARTED");


        String address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        Handler v1Sample1Handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                //TODO: Write data to collector

                SampleV1 sample = (SampleV1) msg.obj;
                sampleAnalizer.ReceiveV1Sample(sample);


                if (sampleReceiver != null) {
                    //sampleReceiver.ReceiveV1Sample((SampleV1) msg.obj);
                    AnalisisData andata = sampleAnalizer.GetAnalisis();
                    sampleReceiver.ReceiveSample(sample.mPos, sample.mV, andata.sag, andata.dynamicSag, sample.mTime);
                }

            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter

        StartThread(new V1SampleReader(), v1Sample1Handler, address);
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
