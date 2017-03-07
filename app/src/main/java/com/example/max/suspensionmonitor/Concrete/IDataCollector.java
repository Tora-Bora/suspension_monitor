package com.example.max.suspensionmonitor.Concrete;

import com.example.max.suspensionmonitor.Domain.Sample;
import com.example.max.suspensionmonitor.Domain.SampleV1;

/**
 * Created by Max on 13.02.2017.
 */

public interface IDataCollector {
    void AppendSample(Sample sample);
}
