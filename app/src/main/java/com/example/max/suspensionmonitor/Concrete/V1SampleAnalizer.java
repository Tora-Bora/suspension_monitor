package com.example.max.suspensionmonitor.Concrete;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.max.suspensionmonitor.Domain.AnalisisData;
import com.example.max.suspensionmonitor.Domain.SampleV1;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Max on 25.03.2017.
 */

public class V1SampleAnalizer implements IV1SampleReceiver {

    private Lock mAnalisisLock = new ReentrantLock();
    private final String TAG = "V1SampleAnalizer";

    private AnalisisData mAnalisis = null;
    private File mOutputFile = null;

    private final long mCalibrateSamples = 100;
    private final double mBottomOutDiv = 5.0;

    private double mExtrem = 0;
    private long packing = 0;

    private boolean mCalibrated = false;
    private long mCalibrateCounter = 0;
    private double mFloor = 0.0;
    private double mLength = 160.0;
    private double mFirstSample = -1;

    private long mLastBottomingIncident = System.currentTimeMillis();

    private SampleV1 mPrevSample = null;
    private ArrayList<Double> mPosListTail = new ArrayList<Double>(11);

    public V1SampleAnalizer() {
        ResetAnalisis();
    }

    @Override
    public void ReceiveV1Sample(SampleV1 sample) {

        if (!mCalibrated) {
            DoCalibrate(sample);
        } else {

            mAnalisisLock.lock();
            try {

                if (mFirstSample < 0) {
                    mFirstSample = sample.mTime;
                }


                mAnalisis.histogramData.AddSpeedSample(sample.mV, sample.mTime - mPrevSample.mTime);

                double realPos = ConvertPos(sample.mPos);

                mPosListTail.add(realPos);
                if (mPosListTail.size() > 10) {
                    UpdateSag();
                    mPosListTail.clear();
                }


                if ((mLength - realPos) < mBottomOutDiv && (System.currentTimeMillis() - mLastBottomingIncident) > 1000) {
                    mAnalisis.bottomingIncidents += 1;
                    mLastBottomingIncident = System.currentTimeMillis();
                }

                if (Math.signum(mPrevSample.mV) != Math.signum(sample.mV)) {
                    // derivative changed sign
                    double dynamicSag = realPos + (mExtrem - realPos) / 2.0;
                    dynamicSag = dynamicSag * 100.0 / mLength;

                    if (dynamicSag > (mAnalisis.dynamicSag + 0.5)) {
                        packing += 1;
                    }
                    else {
                        packing = 0;
                    }

                    if (packing > 3) {
                        mAnalisis.packingIncidents += 1;
                    }

                    mAnalisis.dynamicSag = dynamicSag;
                }

                if (mOutputFile != null) {
                    try {
                        FileOutputStream outputStream = new FileOutputStream(mOutputFile, true);
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
                        try {

                            outputStreamWriter.append(String.format("%.4f\t%.4f\t%.4f\r\n", sample.mTime - mFirstSample, realPos, sample.mV));
                            outputStreamWriter.flush();
                        }
                        finally {
                            if (outputStreamWriter != null){
                                outputStreamWriter.close();
                            }
                            if (outputStream != null) {
                                outputStream.close();
                            }
                        }
                    }
                    catch (IOException io) {
                        mOutputFile = null;
                    }
                }

            }
            finally {
                mAnalisisLock.unlock();
            }
        }

        mPrevSample = sample;
    }

    public double TimeOffset() {
        if (mFirstSample >= 0)
            return mFirstSample;
        else return 0;
    }

    public void Calibrate() {
        mCalibrated = false;
        mCalibrateCounter = 0;
        mFloor = 0.0;
        ResetAnalisis();
    }

    private void DoCalibrate(SampleV1 sample) {

        mFloor += sample.mPos;
        mCalibrateCounter += 1;

        if (mCalibrateCounter > mCalibrateSamples) {
            mFloor /= mCalibrateCounter;
            mCalibrated = true;
            Log.d("V1SampleAnalizer", String.format("Calibrating finished: %.2f", mFloor));
        }
    }

    public double ConvertPos(double pos) {
        return pos - mFloor;
    }

    private void UpdateSag() {
        double medpos = 0;
        for (Double pos :
                mPosListTail) {
            medpos += pos;
        }

        medpos = medpos / mPosListTail.size();
        mAnalisis.sag = (medpos / mLength) * 100;
    }

    public void StartLoging() {
        mAnalisisLock.lock();
        try {

            File direct = new File(Environment.getExternalStorageDirectory()+"/SussLog");

            if(!direct.exists()) {
                if(!direct.mkdirs()) {
                    //directory is created;
                    Log.d(TAG, "Directory creation failed!");
                }
            }
            Date currentDate = new Date(System.currentTimeMillis());
            SimpleDateFormat format = new SimpleDateFormat("dd_mm_yyyy_hh_mm_ss");
            String fileName = format.format(currentDate) + ".csv";
            mOutputFile = new File(direct, fileName);
        }
        finally {
            mAnalisisLock.unlock();
        }
    }

    public void StopLogging() {
        mAnalisisLock.lock();
        try {
            mOutputFile = null;
        }
        finally {
            mAnalisisLock.unlock();
        }
    }

    public void ResetAnalisis() {
        mAnalisisLock.lock();
        try {
            mAnalisis = new AnalisisData();
            mAnalisis.histogramData = new SpeedHistogramData(300, 10);
            if (mPrevSample == null) {
                mFirstSample = -1;
            }
            else {
                mFirstSample = mPrevSample.mTime;
            }

        }
        finally {
            mAnalisisLock.unlock();
        }
    }


    public AnalisisData GetAnalisis() { return mAnalisis; }
}
