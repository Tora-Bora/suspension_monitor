package com.example.max.suspensionmonitor.Concrete;

import com.example.max.suspensionmonitor.Domain.Sample;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Max on 19.02.2017.
 */

public interface ISampleReader {
    Sample ReadSample(InputStream inStream) throws IOException;
}
