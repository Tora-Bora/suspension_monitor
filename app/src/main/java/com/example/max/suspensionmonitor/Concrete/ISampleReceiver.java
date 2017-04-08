package com.example.max.suspensionmonitor.Concrete;

/**
 * Created by Max on 26.03.2017.
 */

public interface ISampleReceiver {
    void ReceiveSample(double position, double velocity, double sag, double dynamicSag, double time);
}
