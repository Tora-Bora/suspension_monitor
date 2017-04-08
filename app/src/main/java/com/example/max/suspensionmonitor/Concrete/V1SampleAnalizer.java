package com.example.max.suspensionmonitor.Concrete;

import android.util.Log;

import com.example.max.suspensionmonitor.Domain.AnalisisData;
import com.example.max.suspensionmonitor.Domain.SampleV1;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Max on 25.03.2017.
 */

public class V1SampleAnalizer implements IV1SampleReceiver {

    private Lock mAnalisisLock = new ReentrantLock();

    private AnalisisData mAnalisis = null;

    private final long mCalibrateSamples = 100;
    private final double mBottomOutDiv = 5.0;

    private boolean mCalibrated = false;
    private long mCalibrateCounter = 0;
    private double mFloor = 0.0;
    private double mLength = 160.0;

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
            }
            finally {
                mAnalisisLock.unlock();
            }
        }

        mPrevSample = sample;
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

    public void ResetAnalisis() {
        mAnalisisLock.lock();
        try {
            mAnalisis = new AnalisisData();
            mAnalisis.histogramData = new SpeedHistogramData(300, 10);
        }
        finally {
            mAnalisisLock.unlock();
        }
    }


    public AnalisisData GetAnalisis() { return mAnalisis; }
}
