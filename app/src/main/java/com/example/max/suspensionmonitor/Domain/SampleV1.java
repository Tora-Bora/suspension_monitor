package com.example.max.suspensionmonitor.Domain;

import com.example.max.suspensionmonitor.Math.Vector3;

/**
 * Created by Max on 13.02.2017.
 */

public class SampleV1 extends Sample {
    public double mPos = 0;
    public double mV = 0;

    public SampleV1() {
    }

    public SampleV1(String rawData){
        this.ProcessRawData(rawData);
    }


    public void ProcessRawData(String rawData)
    {
        String[] packet = rawData.split(";");
        if (packet.length < 3 ){
            throw new ArrayIndexOutOfBoundsException("Unexpected data:" + rawData);
        }
        mTime = Double.parseDouble(packet[0]);
        mPos = Double.parseDouble(packet[1]);
        mV = Double.parseDouble(packet[2]);
    }
}
