package com.example.max.suspensionmonitor.Domain;

import com.example.max.suspensionmonitor.Math.Vector3;

/**
 * Created by Max on 13.02.2017.
 */

public class SampleV1 extends Sample {
    public double mPotentiometer = 0;
    public double mV = 0;
    public double mDt = 0;

    Vector3 mAccel = new Vector3();

    public SampleV1() {

    }

    public SampleV1(String rawData){
        this.ProcessRawData(rawData);
    }


    public void ProcessRawData(String rawData)
    {
        String[] packet = rawData.split(";");
        if (packet.length < 5 ){
            throw new ArrayIndexOutOfBoundsException("Unexpected data:" + rawData);
        }
        mTime = Double.parseDouble(packet[0]);
        mPotentiometer = Double.parseDouble(packet[1]);
        mAccel.x = Double.parseDouble(packet[2]);
        mAccel.y = Double.parseDouble(packet[3]);
        mAccel.z = Double.parseDouble(packet[4]);
    }


}
