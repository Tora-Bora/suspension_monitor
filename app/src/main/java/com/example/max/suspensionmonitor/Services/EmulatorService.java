package com.example.max.suspensionmonitor.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.example.max.suspensionmonitor.Domain.AnalisisData;
import com.example.max.suspensionmonitor.Domain.SampleV1;

public class EmulatorService extends WorkerService {

    private EmulatorThread thread = null;
    public EmulatorService() {
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (thread.isAlive())
            thread.interrupt();
        Log.d(TAG, "SERVICE DESTROYED");
    }

    @Override
    protected void Connect(String address){

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

        thread = new EmulatorThread(v1Sample1Handler);
        thread.start();
    }

    private class EmulatorThread extends Thread {
        private Handler mmSampleHandler;
        EmulatorThread(Handler sampleHandler) {
            mmSampleHandler = sampleHandler;
        }

        @Override
        public void run() {
            super.run();

            long msCounter = 0;
            double mPrevPos = -1;

            long samples = 0;

            try {
                while (true && !this.isInterrupted()) {

                    Thread.sleep(100);
                    msCounter += 100;


                    SampleV1 sample = new SampleV1();
                    sample.mTime = msCounter / 1000.0;

                    if (samples > 100) {
                        double sin = Math.sin(sample.mTime * 3);
                        sample.mPos = 100 + 60 * sin;
                    } else {
                        sample.mPos = 160;
                    }

                    samples++;


                    if (mPrevPos > 0) {
                        sample.mV = (sample.mPos - mPrevPos) / 0.1;
                    }
                    mPrevPos = sample.mPos;

                    mmSampleHandler.obtainMessage(0, 0, -1, sample).sendToTarget();
                }
            }
            catch (InterruptedException ex) {

            }

        }
    }

}
