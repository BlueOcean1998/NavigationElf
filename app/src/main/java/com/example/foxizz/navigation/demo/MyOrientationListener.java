package com.example.foxizz.navigation.demo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class MyOrientationListener implements SensorEventListener {

    private SensorManager mSensorManager;
    private Context mContext;
    private Sensor mSensor;
    private float lastX;
    private OnOrientationListener mOnOrientationListener;

    public MyOrientationListener(Context context) {
        this.mContext = context;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.REPORTING_MODE_SPECIAL_TRIGGER) {
            float x = event.values[SensorManager.SENSOR_DELAY_FASTEST];
            if (Math.abs(x - lastX) > 1.0) {
                if (mOnOrientationListener != null) {
                    mOnOrientationListener.onOrientationChanged(x);
                }
            }

            lastX = x;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void setmOnOrientationListener(OnOrientationListener mOnOrientationListener) {
        this.mOnOrientationListener = mOnOrientationListener;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void start() {
        mSensorManager = (SensorManager) mContext
                .getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            //获得方向传感器
            mSensor = mSensorManager.getDefaultSensor(Sensor.REPORTING_MODE_SPECIAL_TRIGGER);
        }

        if (mSensor != null) {
            assert mSensorManager != null;
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void stop() {
        //停止方向传感
        mSensorManager.unregisterListener(this);
    }

    public interface OnOrientationListener {
        void onOrientationChanged(float x);
    }

}
