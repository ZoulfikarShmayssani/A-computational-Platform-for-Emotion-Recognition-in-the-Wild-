package com.example.halac.keyloggers_notify;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;

public class RegistrableSensorManager extends Service {
    public static RegistrableSensorManager Instance;
    private static SensorManager sensorManager;
    private static LocationManager locationManager;
    private static MyLocationListener locationListener;
    private RegistrableSensorEventListener[] sensors ;
    private boolean[] registered;
    private Timer timer;
    FileOutputStream fileOutputStream;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Instance = this;
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        RegistrableSensorEventListener[] sensors = {
                new RegistrableSensorEventListener(RegistrableSensorType.humidity, 20),
                new RegistrableSensorEventListener(RegistrableSensorType.accelerometer, 20),
                new RegistrableSensorEventListener(RegistrableSensorType.stepCounter, 20),
                new RegistrableSensorEventListener(RegistrableSensorType.temperature, 20)
        };
        this.sensors = sensors;
        registered = new boolean[sensors.length];
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();
        timer = new Timer();
        File csv = new File(getFilesDir(), "sensorData.csv");

        try
        {
            if (!csv.exists() || !csv.isFile())
            {
                csv.createNewFile();
                PrintStream stream = new PrintStream(csv);
                stream.print("Time,GPS");

                for (RegistrableSensorEventListener sensor: sensors)
                {
                    stream.print("," + sensor.type);
                }

                stream.print('\n');
                stream.close();
            }

            fileOutputStream = new FileOutputStream(csv, true);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        registerAll();
        writePeriodicMeasurements();
    }

    @Override
    public void onDestroy()
    {
        try
        {
            fileOutputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        timer.cancel();
        unregisterAll();

        try
        {
            Log.d("sensorData", convertStreamToString(openFileInput("sensorData.csv")));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static SensorManager getSensorManager() {
        if(sensorManager == null)
        {
            throw new RuntimeException("RegistrableSensorManager service hasn't start yet!!");
        }

        return sensorManager;
    }

    public boolean registerSensor(RegistrableSensorType type)
    {
        if(!registered[type.index])
        {
            registered[type.index] = sensors[type.index].register();
        }

        return registered[type.index];
    }

    public void unregisterSensor(RegistrableSensorType type)
    {
        if(registered[type.index])
        {
            registered[type.index] = false;
            sensors[type.index].unregister();
        }
    }

    public void registerGPS()
    {
        try
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MyLocationListener.duration * 1000, 0, locationListener, Looper.myLooper());
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }
    }

    public void unregisterGPS()
    {
        locationManager.removeUpdates(locationListener);
    }

    public boolean registerAll()
    {
        // TODO: fix issues with gps
        //registerGPS();
        boolean allRegistered = true;

        for(RegistrableSensorEventListener sensor: sensors)
        {
            allRegistered &= registerSensor(sensor.type);
        }

        return allRegistered;
    }

    public void unregisterAll()
    {
        unregisterGPS();

        for(RegistrableSensorEventListener sensor: sensors)
        {
            unregisterSensor(sensor.type);
        }
    }

    public void writePeriodicMeasurements()
    {
        int gcd = MyLocationListener.duration;

        for(int i = 0; i < sensors.length; i++)
        {
            if(registered[i])
            {
                gcd = gcd(sensors[i].duration, gcd);
            }
        }

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try
                {
                    fileOutputStream.write((System.currentTimeMillis() + "," + join(locationListener.getLastMeasuredValues(), " | ")).getBytes());

                    for(RegistrableSensorEventListener sensor: sensors)
                    {
                        fileOutputStream.write(("," + join(sensor.getLastMeasuredValues() , " | ")).getBytes());
                    }

                    fileOutputStream.write("\n".getBytes());
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }, 0, gcd * 1000);

    }

    private int gcd(int a, int b)
    {
        return (b == 0) ? a : gcd(b, a % b);
    }

    private static String join(double[] values, String delimeter)
    {
        StringBuilder sb = new StringBuilder();

        if(values != null && values.length != 0)
        {
            sb.append(values[0]);

            for(int i = 1; i < values.length; i++)
            {
                sb.append(delimeter);
                sb.append(values[i]);
            }
        }

        return sb.toString();
    }

    private static String join(float[] values, String delimeter)
    {
        StringBuilder sb = new StringBuilder();

        if(values != null && values.length != 0)
        {
            sb.append(values[0]);

            for(int i = 1; i < values.length; i++)
            {
                sb.append(delimeter);
                sb.append(values[i]);
            }
        }

        return sb.toString();
    }

    public String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
