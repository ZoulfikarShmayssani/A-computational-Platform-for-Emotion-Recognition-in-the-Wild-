package com.example.halac.keyloggers_notify;

import android.hardware.Sensor;

public enum RegistrableSensorType {
    humidity(0, Sensor.TYPE_RELATIVE_HUMIDITY),
    accelerometer(1, Sensor.TYPE_ACCELEROMETER),
    stepCounter(2, Sensor.TYPE_STEP_COUNTER),
    temperature(3, Sensor.TYPE_AMBIENT_TEMPERATURE),
    light(4, Sensor.TYPE_LIGHT),
    linearAcceleration(5, Sensor.TYPE_LINEAR_ACCELERATION),
    gyroscope(6, Sensor.TYPE_GYROSCOPE),
    proximity(7, Sensor.TYPE_PROXIMITY);

    public final int index;
    public final int sensorId;

    RegistrableSensorType(int index, int sensorId) {
        this.index = index;
        this.sensorId = sensorId;
    }
}
