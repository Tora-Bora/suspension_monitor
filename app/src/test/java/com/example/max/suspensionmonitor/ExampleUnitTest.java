package com.example.max.suspensionmonitor;

import com.example.max.suspensionmonitor.Concrete.JYSampleReader;
import com.example.max.suspensionmonitor.Concrete.SpeedHistogramData;
import com.example.max.suspensionmonitor.Concrete.V1SampleReader;
import com.example.max.suspensionmonitor.Domain.SampleJY;
import com.example.max.suspensionmonitor.Domain.SampleV1;
import com.example.max.suspensionmonitor.Math.Matrix3x3;
import com.example.max.suspensionmonitor.Math.Vector3;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    private byte[] ReadByteArray(String s)
    {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    @Test
    public void TestMatrix() {


                //90 11 44 0 1 0
                //0 -89 -159 1 0 0
                //-90 0 -101 0 -1 0
                //70 88 128 -1 0 0


        Vector3 vec = new Vector3(0d,0d,9.8d);

        Matrix3x3 matrix = Matrix3x3.fromEuler(Math.toRadians(70.0d), Math.toRadians(88.0d), Math.toRadians(128d));

        Vector3 result = matrix.multiply(vec);


        assertEquals(-1.0, result.y, 0.0001);
    }

    @Test
    public void ReadJYSample() throws IOException {

        String a = "01C80616FEED5551BEFF3200CB0716FE7B555201000000000016FEBC5553";
        String b = "01016001C80616FEED5551BEFF3300C80716FE7955520000FFFF000016FEB9555301";

        JYSampleReader bufferReader = new JYSampleReader();
        ByteArrayInputStream bis = new ByteArrayInputStream(ReadByteArray(a));

        SampleJY smpl = (SampleJY)bufferReader.ReadSample(bis);
        assertNull(smpl);

        bis = new ByteArrayInputStream(ReadByteArray(b));
        smpl = (SampleJY)bufferReader.ReadSample(bis);
        assertNotNull(smpl);
    }

    @Test
    public void ReadV1Sample() throws Exception {

        V1SampleReader sampleReader = new V1SampleReader();

        String prea = ".30;9.85\r\n";
        String a = "1.0;1;2;3;4\r\n1.01;5;";
        String b = "6;7;8\r\n1.02;9;10;11;12\r\n1.03;13";

        ByteArrayInputStream bis = new ByteArrayInputStream(prea.getBytes());
        SampleV1 sample1 = (SampleV1)sampleReader.ReadSample(bis);
        assertNull(sample1);

        bis = new ByteArrayInputStream(a.getBytes());
        sample1 = (SampleV1)sampleReader.ReadSample(bis);

        assertNotNull(sample1);
        assertEquals(1.0, sample1.mTime, 0.0001);

        bis = new ByteArrayInputStream(b.getBytes());
        sample1 = (SampleV1)sampleReader.ReadSample(bis);
        assertNotNull(sample1);
        assertEquals(1.01, sample1.mTime, 0.0001);

        sample1 = (SampleV1)sampleReader.ReadSample(bis);
        assertNotNull(sample1);
        assertEquals(1.02, sample1.mTime, 0.0001);
    }

    @Test
    public void AddToSpeedHistoram() throws ArrayIndexOutOfBoundsException {

        SpeedHistogramData shd = new SpeedHistogramData(10, 2);

        SampleV1 sample = new SampleV1();
        sample.mV = -9;
        sample.mDt = 1;
        shd.AddV1Sample(sample);
        sample.mV = 5;
        shd.AddV1Sample(sample);
        sample.mV = -30;
        shd.AddV1Sample(sample);

        assertEquals(1.0, shd.GetInterval(-1).mTotalTime, 0.001);
        assertEquals(1.0, shd.GetInterval(1).mTotalTime, 0.001);
        assertEquals(0.0, shd.GetInterval(2).mTotalTime, 0.001);
        assertEquals(1.0, shd.GetInterval(-2).mTotalTime, 0.001);
    }
}