package com.example.halac.keyloggers_notify;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Looper;

public class RegistrableSensorEventListener implements SensorEventListener {
    public final RegistrableSensorType type;
    public final int duration; // in seconds
    private float[] lastMeasuredValues;
    private Sensor sensor;

    public RegistrableSensorEventListener(RegistrableSensorType type, int duration)
    {
        this.type = type;
        this.duration = duration;
        sensor = RegistrableSensorManager.getSensorManager().getDefaultSensor(type.sensorId);
    }

    @Override
    public final void onSensorChanged(SensorEvent event)
    {
        if (event.sensor.getType() == sensor.getType())
        {
            lastMeasuredValues = event.values;
        }
    }

    public boolean register()
    {
        return RegistrableSensorManager.getSensorManager().registerListener(this, sensor, duration * 1000000); //last parameter is the delay between events in microseconds
    }

    public void unregister()
    {
        RegistrableSensorManager.getSensorManager().unregisterListener(this, sensor);
    }

    public float[] getLastMeasuredValues()
    {
        return lastMeasuredValues;
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // Do something here if sensor accuracy changes.
    }
}
