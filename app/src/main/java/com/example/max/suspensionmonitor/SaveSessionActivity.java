package com.example.max.suspensionmonitor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.max.suspensionmonitor.Domain.AnalisisData;
import com.example.max.suspensionmonitor.Domain.RideSession;

import java.io.File;
import java.text.SimpleDateFormat;

public class SaveSessionActivity extends AppCompatActivity {


    private static final String SESSION_NAME = "sessionName";
    private static final String AMMORT_NAME = "ammortName";
    private static final String HC = "highSpeedCompression";
    private static final String LC = "lowSpeedCompression";
    private static final String HR = "highSpeedRebound";
    private static final String LR = "lowSpeedRebound";
    private static final String PRESSURE = "pressure";

    private RideSession mRideSession = new RideSession();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_session);

        Intent intent = getIntent();
        mRideSession.analisisData = (AnalisisData)intent.getSerializableExtra(AnalysisActivity.EXTRA_ANALISIS_DATA);

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        //int defaultValue = getResources().getInteger(R.string.saved_high_score_default);
        //long highScore = sharedPref.getInt(getString(R.string.saved_high_score), defaultValue);

        ((EditText) findViewById(R.id.nameTxt)).setText(sharedPref.getString(SESSION_NAME, ""));
        ((EditText) findViewById(R.id.forkTxt)).setText(sharedPref.getString(AMMORT_NAME, ""));
        ((EditText) findViewById(R.id.hcTxt)).setText(String.valueOf(sharedPref.getFloat(HC, 0)));
        ((EditText) findViewById(R.id.lcTxt)).setText(String.valueOf(sharedPref.getFloat(LC, 0)));
        ((EditText) findViewById(R.id.hrTxt)).setText(String.valueOf(sharedPref.getFloat(HR, 0)));
        ((EditText) findViewById(R.id.lrTxt)).setText(String.valueOf(sharedPref.getFloat(LR, 0)));
        ((EditText) findViewById(R.id.presTxt)).setText(String.valueOf(sharedPref.getFloat(PRESSURE, 0)));

    }

    public void buttonSaveClick(View view) {

        mRideSession.name = (String) ((EditText) findViewById(R.id.nameTxt)).getText().toString();
        mRideSession.ammortName = (String) ((EditText) findViewById(R.id.forkTxt)).getText().toString();
        mRideSession.highSpeedCompression = Double.parseDouble ((String)((EditText) findViewById(R.id.hcTxt)).getText().toString());
        mRideSession.lowSpeedCompression = Double.parseDouble ((String)((EditText) findViewById(R.id.lcTxt)).getText().toString());
        mRideSession.highSpeedRebound = Double.parseDouble ((String)((EditText) findViewById(R.id.hrTxt)).getText().toString());
        mRideSession.lowSpeedRebound = Double.parseDouble ((String)((EditText) findViewById(R.id.lrTxt)).getText().toString());
        mRideSession.pressure = Double.parseDouble ((String)((EditText) findViewById(R.id.presTxt)).getText().toString());

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(SESSION_NAME, mRideSession.name);
        editor.putString(AMMORT_NAME, mRideSession.ammortName);
        editor.putFloat(HC, (float)mRideSession.highSpeedCompression);
        editor.putFloat(LC, (float)mRideSession.lowSpeedCompression);
        editor.putFloat(HR, (float)mRideSession.highSpeedRebound);
        editor.putFloat(LR, (float)mRideSession.lowSpeedRebound);
        editor.putFloat(PRESSURE, (float)mRideSession.pressure);
        editor.commit();

        SimpleDateFormat format = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
        String fileName = format.format(mRideSession.analisisData.startDate) + ".sa";

        mRideSession.Serialize(this, fileName);


        Intent openMainActivity= new Intent(SaveSessionActivity.this, MainActivity.class);
        openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityIfNeeded(openMainActivity, 0);

    }

}
