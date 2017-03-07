package com.example.max.suspensionmonitor.Concrete;

import android.view.Surface;

import com.example.max.suspensionmonitor.Domain.SampleV1;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Max on 15.02.2017.
 */

public class SpeedHistogramData {
    public class VInterval {
        public double mTo;
        public double mTotalTime;
    }

    private ArrayList<VInterval> mHistogramArray;
    private final int mPositiveCapacity;
    private final int mSpeedInterval;

    public List<VInterval> GetHistogram() {
        return mHistogramArray;
    }

    public VInterval GetInterval(int signedIndex) throws ArrayIndexOutOfBoundsException {
        if (Math.abs(signedIndex) > mPositiveCapacity || signedIndex == 0)
            throw new ArrayIndexOutOfBoundsException("SignedIndex: " + signedIndex);

        if (signedIndex > 0)
            return mHistogramArray.get(signedIndex + mPositiveCapacity - 1);
        else
            return mHistogramArray.get(signedIndex + mPositiveCapacity);

    }

    public SpeedHistogramData(int speedInterval, int positiveCapacity) {
        mHistogramArray = new ArrayList<>(positiveCapacity * 2);
        mPositiveCapacity = positiveCapacity;
        mSpeedInterval = speedInterval;
        for(int i = -positiveCapacity; i <= positiveCapacity; i++)
        {
            if (i == 0) continue;
            VInterval iv = new VInterval();
            iv.mTo = speedInterval * i;
            iv.mTotalTime = 0;
            mHistogramArray.add(iv);
        }
    }

    public void AddSpeedSample(double speed, double dt) {
        if (Math.abs(speed) < 0.1)
            return;

        int positiveIndex = (int)Math.ceil(Math.abs(speed / mSpeedInterval));
        positiveIndex = Math.min(mPositiveCapacity, positiveIndex);
        int signedIndex = (int)(positiveIndex * Math.signum(speed));
        GetInterval(signedIndex).mTotalTime += dt;
    }

    public void AddV1Sample(SampleV1 sample) {

        double speedmm = sample.mV;

        if (Math.abs(speedmm) < 0.1)
            return;

        int positiveIndex = (int)Math.ceil(Math.abs(speedmm / mSpeedInterval));
        positiveIndex = Math.min(mPositiveCapacity, positiveIndex);
        int signedIndex = (int)(positiveIndex * Math.signum(speedmm));
        GetInterval(signedIndex).mTotalTime += sample.mDt;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for(VInterval interval : mHistogramArray) {
            sb.append(String.format("%.1f:%.3f ", interval.mTo, interval.mTotalTime));
        }

        return sb.toString();
    }
}
