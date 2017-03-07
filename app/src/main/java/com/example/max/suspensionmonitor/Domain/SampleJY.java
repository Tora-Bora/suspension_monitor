package com.example.max.suspensionmonitor.Domain;

import com.example.max.suspensionmonitor.Math.Vector3;

/**
 * Created by Max on 15.02.2017.
 */

public class SampleJY extends Sample {

    public double mDTime = 0;

    public Vector3 mAccel = new Vector3();

    public Vector3 mAngularVelocity = new Vector3();

    public Vector3 mAngle = new Vector3();

    public double mVZ = 0;

    @Override
    public String toString() {
        return String.format("ax:%+.4f, ay:%+.4f, az:%+.4f, v:%+.4f, dt:%.3f", mAccel.x, mAccel.y,mAccel.z, mVZ, mDTime);
    }


}
