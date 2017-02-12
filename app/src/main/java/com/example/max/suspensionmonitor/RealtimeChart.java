package com.example.max.suspensionmonitor;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Random;

public class RealtimeChart extends AppCompatActivity {

    private final Handler mHandler = new Handler();
    private LineGraphSeries<DataPoint> mSeries;
    private Runnable mTimer;
    private double graph2LastXValue = 5d;
    final String LOG_TAG = "myLogs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(LOG_TAG, "RealtimeChart::OnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_chart);

        GraphView graph = (GraphView)findViewById(R.id.graph);
        mSeries = new LineGraphSeries<>();
        graph.addSeries(mSeries);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(40);
    }

    @Override
    protected void onResume() {
        Log.d(LOG_TAG, "RealtimeChart::onResume");
        super.onResume();

        mTimer = new Runnable() {
            @Override
            public void run() {
                graph2LastXValue += 1d;
                mSeries.appendData(new DataPoint(graph2LastXValue, getRandom()), true, 40);
                mHandler.postDelayed(this, 200);
            }
        };
        mHandler.postDelayed(mTimer, 1000);

    }

    public void OnDataChanged(DataPoint dataPoint){
        mSeries.appendData(dataPoint, true, 40);
    }

    double mLastRandom = 2;
    Random mRand = new Random();
    private double getRandom() {
        return mLastRandom += mRand.nextDouble()*0.5 - 0.25;
    }
}
