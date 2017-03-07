package com.example.max.suspensionmonitor.Communications;

import com.example.max.suspensionmonitor.Concrete.SpeedHistogramData;
import com.example.max.suspensionmonitor.Domain.SampleJY;
import com.example.max.suspensionmonitor.Domain.SampleV1;

/**
 * Created by Max on 16.02.2017.
 */

public interface ISampleReceiver {
    void ReceiveV1Sample(SampleV1 sample);
    void ReceiveJYSampleA1(SampleJY sample);
    void ReceiveJYSampleA2(SampleJY sample);
}
