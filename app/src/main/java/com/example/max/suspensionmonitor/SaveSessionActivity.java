package com.example.max.suspensionmonitor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.max.suspensionmonitor.Domain.AnalisisData;
import com.example.max.suspensionmonitor.Domain.RideSession;

import java.text.SimpleDateFormat;

public class SaveSessionActivity extends AppCompatActivity {

    private RideSession mRideSession = new RideSession();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_session);

        Intent intent = getIntent();
        mRideSession.analisisData = (AnalisisData)intent.getSerializableExtra(AnalysisActivity.EXTRA_ANALISIS_DATA);

    }

    public void buttonSaveClick(View view) {

        mRideSession.name = (String) ((EditText) findViewById(R.id.nameTxt)).getText().toString();
        mRideSession.ammortName = (String) ((EditText) findViewById(R.id.forkTxt)).getText().toString();
        mRideSession.highSpeedCompression = Double.parseDouble ((String)((EditText) findViewById(R.id.hcTxt)).getText().toString());
        mRideSession.lowSpeedCompression = Double.parseDouble ((String)((EditText) findViewById(R.id.lcTxt)).getText().toString());
        mRideSession.highSpeedRebound = Double.parseDouble ((String)((EditText) findViewById(R.id.hrTxt)).getText().toString());
        mRideSession.lowSpeedRebound = Double.parseDouble ((String)((EditText) findViewById(R.id.lrTxt)).getText().toString());
        mRideSession.pressure = Double.parseDouble ((String)((EditText) findViewById(R.id.lrTxt)).getText().toString());

        SimpleDateFormat format = new SimpleDateFormat("dd_mm_yyyy_hh_mm_ss");
        String fileName = format.format(mRideSession.analisisData.startDate) + ".sa";
        mRideSession.Serialize(this, fileName);


        Intent openMainActivity= new Intent(SaveSessionActivity.this, MainActivity.class);
        openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityIfNeeded(openMainActivity, 0);

    }

}
