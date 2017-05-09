package com.example.max.suspensionmonitor.Domain;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by Max on 09.04.2017.
 */

public class RideSession implements Serializable {

    public AnalisisData analisisData;
    public String name;
    public String ammortName;
    public double highSpeedRebound;
    public double lowSpeedRebound;
    public double highSpeedCompression;
    public double lowSpeedCompression;
    public double pressure;
    public double preload;

    public void Serialize(Context context, String fileName) {

        try {
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(this);
            os.close();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static RideSession Desrialize(Context context, String fileName) {
        RideSession objectToReturn = null;
        try {
            FileInputStream fileInputStream = context.openFileInput(fileName);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            objectToReturn = (RideSession) objectInputStream.readObject();

            objectInputStream.close();
            fileInputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return objectToReturn;
    }
}
