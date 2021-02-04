package com.navigation.foxizz.mybaidumap;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;

import static com.navigation.foxizz.BaseApplication.getApplication;

/**
 * 方向传感器
 */
public class MyOrientationListener implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private float lastX;
    private OnOrientationListener mOnOrientationListener;

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

    /**
     * 监听方向变化
     */
    public void setOnOrientationListener(OnOrientationListener mOnOrientationListener) {
        this.mOnOrientationListener = mOnOrientationListener;
    }

    /**
     * 开始方向传感
     */
    public void start() {
        mSensorManager = (SensorManager) getApplication().getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            //获得方向传感器
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mSensor = mSensorManager.getDefaultSensor(Sensor.REPORTING_MODE_SPECIAL_TRIGGER);
            }
        }

        if (mSensor != null) {
            assert mSensorManager != null;
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    /**
     * 停止方向传感
     */
    public void stop() {
        mSensorManager.unregisterListener(this);
    }

    /**
     * 监听方向变化的接口
     */
    public interface OnOrientationListener {
        void onOrientationChanged(float x);
    }

}
