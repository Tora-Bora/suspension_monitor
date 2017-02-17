package com.example.max.suspensionmonitor.Concrete;

import com.example.max.suspensionmonitor.Domain.SampleV1;

/**
 * Created by Max on 13.02.2017.
 */

public interface IV1DataCollector {
    void AppendSample(SampleV1 sample);
}
