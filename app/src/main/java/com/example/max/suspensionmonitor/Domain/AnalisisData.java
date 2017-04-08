package com.example.max.suspensionmonitor.Domain;

import android.util.Xml;

import com.example.max.suspensionmonitor.Concrete.SpeedHistogramData;

import org.xmlpull.v1.XmlSerializer;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.Date;
import java.util.concurrent.Exchanger;

/**
 * Created by Max on 31.03.2017.
 */

public class AnalisisData implements Serializable {

    public Date startDate = new Date(System.currentTimeMillis());
    public Date stopDate = null;
    public long bottomingIncidents = 0;
    public double dynamicSag = 0;
    public double sag = 0;
    public SpeedHistogramData histogramData = null;
}
