package com.example.max.suspensionmonitor.Math;

import android.support.v7.widget.VectorEnabledTintResources;

/**
 * Created by Max on 27.02.2017.
 */

public class Vector3{
    public Double x;
    public Double y;
    public Double z;

    public Vector3() {
        x = 0d;
        y = 0d;
        z = 0d;
    }

    public Vector3(Double _x, Double _y, Double _z) {
        x = _x;
        y = _y;
        z = _z;
    }

    public Vector3 Add(Vector3 v) {
        return new Vector3(x + v.x, y + v.y, z + v.z);
    }

    public Double Length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public void Normalize() {
        Double length = this.Length();
        x /= length;
        y /= length;
        z /= length;
    }
}
