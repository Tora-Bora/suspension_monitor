package com.example.max.suspensionmonitor.Concrete;

import com.example.max.suspensionmonitor.Domain.SensorsSampleV1;

/**
 * Created by Max on 13.02.2017.
 */

public interface ITelemetryDataCollector {
    void AppendSample(SensorsSampleV1 sample);
}
