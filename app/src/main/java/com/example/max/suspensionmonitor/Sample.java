package com.example.max.suspensionmonitor;

/**
 * Created by Max on 18.02.2017.
 */

public class Sample {
    public double ax;
    public double ay;
    public double az;
    public double dt;
    public double vz;

    public double wx;
    public double wy;
    public double wz;

    public double anx;
    public double any;
    public double anz;



    @Override
    public String toString() {
        return String.format("ax:%.2f, ay:%.2f, az:%.2f, vz:%.2f, dt:%.3f", ax, ay,az,vz, dt);
    }
}
