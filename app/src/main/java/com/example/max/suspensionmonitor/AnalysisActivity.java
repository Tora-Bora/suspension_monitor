package com.example.max.suspensionmonitor;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.max.suspensionmonitor.Concrete.SpeedHistogramData;
import com.example.max.suspensionmonitor.Domain.AnalisisData;
import com.example.max.suspensionmonitor.Mocks.AnalisisDataMock;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;

public class AnalysisActivity extends AppCompatActivity {

    // EXTRA string to send on to MonitoringActivity
    public static String EXTRA_ANALISIS_DATA = "analisis_data";

    private BarGraphSeries<DataPoint> mSeries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        Intent intent = getIntent();
        AnalisisData data = (AnalisisData)intent.getSerializableExtra(EXTRA_ANALISIS_DATA);
        //AnalisisData data = new AnalisisDataMock();

        double spdinerval = (double)data.histogramData.GetSpeedInterval();

        DataPoint points[] = new DataPoint[data.histogramData.GetHistogram().size()];
        int i = 0;
        double min = 0;
        double max = 0;
        for (SpeedHistogramData.VInterval interval : data.histogramData.GetHistogram()) {
            double x = interval.mTo / spdinerval;
            max = Math.max(max, x);
            min = Math.min(min, x);
            points[i] = new DataPoint(x, interval.mTotalTime);
            i++;
        }

        GraphView graph = (GraphView) findViewById(R.id.graph);
        mSeries = new BarGraphSeries<>(points);
        mSeries.setSpacing(50);
        graph.addSeries(mSeries);

        mSeries.setValueDependentColor(new ValueDependentColor<DataPoint>() {
            @Override
            public int get(DataPoint data) {
                if ( Math.abs(data.getX()) < 3 )
                    return Color.RED;
                else
                    return Color.BLUE;
                //return Color.rgb((int) data.getX()*255/4, (int) Math.abs(data.getY()*255/6), 100);
            }
        });

        graph.getViewport().setMinX(min);
        graph.getViewport().setMaxX(max);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(100);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);
    }

    public void buttonSaveClick(View view) {

    }
}
