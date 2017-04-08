package com.example.max.suspensionmonitor.Concrete;

import android.util.Log;

import com.example.max.suspensionmonitor.Domain.Sample;
import com.example.max.suspensionmonitor.Domain.SampleJY;
import com.example.max.suspensionmonitor.Domain.SampleV1;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Max on 19.02.2017.
 */

public class V1SampleReader implements ISampleReader {

    private StringBuilder recDataString = new StringBuilder();
    private byte[] buffer = new byte[256];
    private int bytes;

    public Sample ReadSample(InputStream inStream) throws IOException {
        SampleV1 sample = null;

        int endOfLineIndex = recDataString.indexOf("\r\n");                    // determine the end-of-line

        //Читаеем из буфера только если в строковом букфере пусто
        if (endOfLineIndex < 0) {
            bytes = inStream.read(buffer);            //read bytes from input buffer
            String readMessage = new String(buffer, 0, bytes);

            recDataString.append(readMessage);//
        }

        endOfLineIndex = recDataString.indexOf("\r\n");                    // determine the end-of-line
        if(endOfLineIndex > 0) {
            String data = recDataString.substring(0, endOfLineIndex);
            try {
                sample = new SampleV1(data);
            }
            catch (Exception ex) {
                Log.d("V1SampleReader", String.format("ex:%s, parsing line:%s", ex.getMessage(), data));
            }
            recDataString.delete(0, endOfLineIndex + 2);
        }


        return sample;
    }
}
