package com.example.max.suspensionmonitor.Concrete;

import com.example.max.suspensionmonitor.Domain.Sample;
import com.example.max.suspensionmonitor.Domain.SampleJY;
import com.example.max.suspensionmonitor.Math.Vector3;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Vector;

/**
 * Created by Max on 18.02.2017.
 */

public class JYSampleReader implements ISampleReader {
    private final String LOG_TAG = "myLogs";
    private final int bufferSize = 256;
    private final int packetSize = 33;
    private final double g = 9.81;
    private byte[] buffer = new byte[bufferSize];
    private int offset = 0;
    private int bytes = -1;
    private int start = 0;

    private long startTime = System.currentTimeMillis();
    private long lastTime = -1;
    private double lastV = 0;
    private double lastVTime = -1;
    private double lastAccelZ = -1;

    private long calibrateSampleCount = 0;
    private boolean calibrateComplete = false;
    private double calibrateZ = 0;
    private LinkedList<Vector3> accFilterList = new LinkedList<>();

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


    public Sample ReadSample(InputStream inStream) throws IOException {

        SampleJY sample = null;

        bytes = inStream.read(buffer, offset, bufferSize - offset);            //read bytes from input buffer
        if (bytes > 0) {
            offset += bytes;

            while (true) {
                int i;
                for (i = start; i < offset; i++) {
                    //Начало пакета
                    if (buffer[i] == 0x51 && i > start && buffer[i - 1] == 0x55) {
                        //Acceleration;
                        start = i - 1;
                        //Достаточно данных для чтения пакета...
                        if (offset - i >= 33) {

                            sample = new SampleJY();

                            sample.mAccel.x = ((short) (buffer[i + 2] << 8 | buffer[i + 1] & 0xff)) / 32768.0d * 16 * g;
                            sample.mAccel.y = ((short) (buffer[i + 4] << 8 | buffer[i + 3] & 0xff)) / 32768.0d * 16 * g;
                            sample.mAccel.z = ((short) (buffer[i + 6] << 8 | buffer[i + 5] & 0xff)) / 32768.0d * 16 * g;

                            int j = i + 11;

                            //Angular velocity
                            if (buffer[j] == 0x52) {
                                sample.mAngularVelocity.x = ((short) (buffer[j + 2] << 8 | buffer[j + 1] & 0xff)) / 32768.0d * 2000;
                                sample.mAngularVelocity.y = ((short) (buffer[j + 4] << 8 | buffer[j + 3] & 0xff)) / 32768.0d * 2000;
                                sample.mAngularVelocity.z = ((short) (buffer[j + 6] << 8 | buffer[j + 5] & 0xff)) / 32768.0d * 2000;
                            }

                            j = j + 11;

                            if (buffer[j] == 0x53) {
                                sample.mAngle.x = ((short) (buffer[j + 2] << 8 | buffer[j + 1] & 0xff)) / 32768.0d * 180;
                                sample.mAngle.y = ((short) (buffer[j + 4] << 8 | buffer[j + 3] & 0xff)) / 32768.0d * 180;
                                sample.mAngle.z = ((short) (buffer[j + 6] << 8 | buffer[j + 5] & 0xff)) / 32768.0d * 180;
                            }



                            if (!calibrateComplete)
                            {
                                calibrateZ += (sample.mAccel.z - g);
                                if (calibrateSampleCount > 100)
                                {
                                    calibrateComplete = true;
                                    calibrateZ /= calibrateSampleCount;
                                    lastV = 0;
                                }
                                calibrateSampleCount++;
                            }

                            if (calibrateComplete)
                            {
                                sample.mAccel.z -= calibrateZ;
                                accFilterList.add(sample.mAccel);

                                double filteredZ = 0;

                                for (Vector3 v : accFilterList) {
                                    filteredZ += v.z;
                                }

                                //sample.mAccel.z = filteredZ / accFilterList.size();

                                while (accFilterList.size() >= 3) {
                                    accFilterList.remove();
                                }
                            }

                            sample.mDTime = (System.currentTimeMillis() - startTime) / 1000.0d;


                            if (lastVTime != -1) {
                                lastV = lastV + ((sample.mAccel.z - g) + (lastAccelZ - g)) * (sample.mDTime - lastVTime) / 2.0;
                                sample.mVZ = lastV;
                            }
                            lastVTime = sample.mDTime;
                            lastAccelZ = sample.mAccel.z;

                            byte[] temp = Arrays.copyOfRange(buffer, start, start + 33);
                            lastTime = System.currentTimeMillis();


                            //Прочитали
                            start = i + packetSize - 1;
                            break;
                        } else {
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


        return sample;
    }
}
