package com.example.max.suspensionmonitor.Mocks;

import com.example.max.suspensionmonitor.Concrete.SpeedHistogramData;
import com.example.max.suspensionmonitor.Domain.AnalisisData;

import java.util.Date;

/**
 * Created by Max on 07.04.2017.
 */

public class AnalisisDataMock extends AnalisisData {
    public AnalisisDataMock() {
        startDate = new Date(System.currentTimeMillis());
        stopDate = new Date(System.currentTimeMillis() + 1000 * 60 * 10); // + 10 sec
        bottomingIncidents = 3;
        dynamicSag = 0;
        sag = 0;
        histogramData = new SpeedHistogramData(300,10);

        histogramData.AddSpeedSample(150, 10);
        histogramData.AddSpeedSample(450, 9);
        histogramData.AddSpeedSample(750, 8);
        histogramData.AddSpeedSample(1050, 7);
        histogramData.AddSpeedSample(1350, 6);
        histogramData.AddSpeedSample(1650, 5);
        histogramData.AddSpeedSample(1950, 4);
        histogramData.AddSpeedSample(2250, 3);
        histogramData.AddSpeedSample(2550, 2);
        histogramData.AddSpeedSample(2850, 1);
        histogramData.AddSpeedSample(-2850, 1);
        histogramData.AddSpeedSample(-2550, 2);
        histogramData.AddSpeedSample(-2250, 3);
        histogramData.AddSpeedSample(-1950, 4);
        histogramData.AddSpeedSample(-1650, 5);
        histogramData.AddSpeedSample(-1350, 6);
        histogramData.AddSpeedSample(-1050, 7);
        histogramData.AddSpeedSample(-750, 8);
        histogramData.AddSpeedSample(-450, 9);
        histogramData.AddSpeedSample(-150, 10);
        histogramData.Normalize();
    }
}
