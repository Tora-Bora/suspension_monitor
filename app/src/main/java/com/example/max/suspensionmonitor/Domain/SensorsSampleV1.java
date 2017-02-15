package com.example.max.suspensionmonitor.Domain;

/**
 * Created by Max on 13.02.2017.
 */

public class SensorsSampleV1 {
    public double mPotentiometer = 0;

    public double mAccelX = 0;

    public double mAccelY = 0;

    public double mAccelZ = 0;

    public double mDTime = 0;

    public SensorsSampleV1(String rawData){
        this.Deserialize(rawData);
    }


    public void Deserialize(String rawData)
    {
        String[] packet = rawData.split(";");
        if (packet.length < 5 ){
            throw new ArrayIndexOutOfBoundsException("Unexpected data:" + rawData);
        }
        mDTime = Double.parseDouble(packet[0]);
        mPotentiometer = Double.parseDouble(packet[1]);
        mAccelX = Double.parseDouble(packet[2]);
        mAccelY = Double.parseDouble(packet[3]);
        mAccelZ = Double.parseDouble(packet[4]);
    }


}
