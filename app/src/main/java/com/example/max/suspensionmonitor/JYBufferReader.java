package com.example.max.suspensionmonitor;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Created by Max on 18.02.2017.
 */

public class JYBufferReader {
    final String LOG_TAG = "myLogs";
    final int bufferSize = 256;
    final int packetSize = 33;
    public byte[] buffer = new byte[bufferSize];
    int offset = 0;
    int bytes = -1;
    int start = 0;

    long startTime = System.currentTimeMillis();
    long lastTime = -1;
    double lastV = 0;

    final protected char[] hexArray = "0123456789ABCDEF".toCharArray();
    public String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    public Sample ReadSample(InputStream inStream) {

        Sample sample = null;

        try {

            bytes = inStream.read(buffer, offset, bufferSize - offset);            //read bytes from input buffer
            if (bytes > 0) {
                offset += bytes;

                while (true) {
                    int i;
                    for (i = start; i < offset; i++) {
                        //Начало пакета
                        if (buffer[i] == 0x51 && i > start && buffer[i - 1] == 0x55) {
                            start = i - 1;
                            //Достаточно данных для чтения пакета...
                            if (offset - i >= 33) {

                                sample = new Sample();
                                double T = 0;
                                sample.ax = ((short) (buffer[i + 2] << 8 | buffer[i + 1] & 0xff)) / 32768.0d * 16 * 0.981;
                                sample.ay = ((short) (buffer[i + 4] << 8 | buffer[i + 3] & 0xff)) / 32768.0d * 16 * 0.981;
                                sample.az = ((short) (buffer[i + 6] << 8 | buffer[i + 5] & 0xff)) / 32768.0d * 16 * 0.981;


                                sample.dt = (System.currentTimeMillis() - startTime) / 1000.0d;

                                byte[] temp = Arrays.copyOfRange(buffer,start, start + 33);


                                T = ((short) (buffer[i + 8] << 8 | buffer[i + 7])) / 340.0 + 36.25;

                                if (lastTime != -1) {
                                    //integrate speed
                                    lastV = lastV + sample.az * (System.currentTimeMillis() - lastTime) / 1000.0;
                                    lastV = lastV * 0.99;
                                    sample.vz = lastV;
                                }
                                lastTime = System.currentTimeMillis();

                                Log.d(LOG_TAG, "Sample:" + sample.toString());


                                //Прочитали
                                start = i + packetSize - 1;
                                break;
                            }
                            else {
                                i = offset;
                                break;
                            }
                        }
                    }

                    if (i == offset) {
                        //Дошли до конца пакета, скопируем остаток буффера в начало.
                        if (start != 0 && start != offset) {
                            byte[] tempBuffer = Arrays.copyOfRange(buffer, start, offset);
                            Arrays.fill(buffer, (byte) 0);
                            System.arraycopy(tempBuffer, 0, buffer, 0, tempBuffer.length);
                            start = 0;
                            offset = tempBuffer.length;
                        }
                        break;
                    }

                }


            }
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "Input stream was disconnected", e);
        }


        return sample;
    }
}
