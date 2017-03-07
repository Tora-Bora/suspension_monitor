package com.example.max.suspensionmonitor.Math;



/**
 * Created by Max on 01.03.2017.
 */

public class Matrix3x3 {
    public Vector3[] rows = { new Vector3(), new Vector3(), new Vector3() };

    public Vector3 multiply(Vector3 vec)
    {
        Vector3 result = new Vector3();
        result.x = rows[0].x * vec.x + rows[0].y * vec.y + rows[0].z * vec.z;
        result.y = rows[1].x * vec.x + rows[1].y * vec.y + rows[1].z * vec.z;
        result.z = rows[2].x * vec.x + rows[2].y * vec.y + rows[2].z * vec.z;

        return result;
    }

    public static Matrix3x3 fromEuler(double yaw, double pitch, double roll)
    {
        double sinA = Math.sin(yaw);
        double cosA = Math.cos(yaw);
        double sinB = Math.sin(pitch);
        double cosB = Math.cos(pitch);
        double sinY = Math.sin(roll);
        double cosY = Math.cos(roll);

        Matrix3x3 matrix = new Matrix3x3();

        matrix.rows[0].x = cosA * cosB;
        matrix.rows[0].y = cosA * sinB * sinY - sinA*cosY;
        matrix.rows[0].z = cosA * sinB * cosY + sinA*sinY;

        matrix.rows[1].x = sinA * cosB;
        matrix.rows[1].y = sinA * sinB * sinY + cosA * cosY;
        matrix.rows[1].z = sinA * sinB * cosY - cosA * sinY;

        matrix.rows[2].x = -sinB;
        matrix.rows[2].y = cosB * sinY;
        matrix.rows[2].z = cosB * cosY;

        return matrix;
    }
}
