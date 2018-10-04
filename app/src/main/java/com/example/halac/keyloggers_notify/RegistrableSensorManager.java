package com.example.halac.keyloggers_notify;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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
    FileOutputStream fileOutputStream;
    private RegistrableSensorEventListener[] sensors;
    private boolean[] registered;
    private Timer timer;

    public static SensorManager getSensorManager() {
        if (sensorManager == null) {
            throw new RuntimeException("RegistrableSensorManager service hasn't start yet!!");
        }

        return sensorManager;
    }

    private static String join(double[] values, String delimeter) {
        StringBuilder sb = new StringBuilder();

        if (values != null && values.length != 0) {
            sb.append(values[0]);

            for (int i = 1; i < values.length; i++) {
                sb.append(delimeter);
                sb.append(values[i]);
            }
        }

        return sb.toString();
    }

    private static String join(float[] values, String delimeter) {
        StringBuilder sb = new StringBuilder();

        if (values != null && values.length != 0) {
            sb.append(values[0]);

            for (int i = 1; i < values.length; i++) {
                sb.append(delimeter);
                sb.append(values[i]);
            }
        }

        return sb.toString();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        Instance = this;
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        RegistrableSensorEventListener[] sensors = {
                new RegistrableSensorEventListener(RegistrableSensorType.humidity, 20),
                new RegistrableSensorEventListener(RegistrableSensorType.accelerometer, 20),
                new RegistrableSensorEventListener(RegistrableSensorType.stepCounter, 20),
                new RegistrableSensorEventListener(RegistrableSensorType.temperature, 20),
                new RegistrableSensorEventListener(RegistrableSensorType.light, 20),
                new RegistrableSensorEventListener(RegistrableSensorType.linearAcceleration, 20),
                new RegistrableSensorEventListener(RegistrableSensorType.gyroscope, 20),
                new RegistrableSensorEventListener(RegistrableSensorType.proximity, 20)
        };
        this.sensors = sensors;
        registered = new boolean[sensors.length];
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();
        timer = new Timer();
        File csv = new File((ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || !Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) ? getFilesDir() : Environment.getExternalStorageDirectory(), "sensorData.csv");
        // TODO: take into consideration that the user might revoke permissions later
        try {
            if (!csv.exists() || !csv.isFile()) {
                csv.createNewFile();
                PrintStream stream = new PrintStream(csv);
                stream.print("Time,GPS");

                for (RegistrableSensorEventListener sensor : sensors) {
                    stream.print("," + sensor.type);
                }

                stream.print('\n');
                stream.close();
            }

            fileOutputStream = new FileOutputStream(csv, true);

            //Uploading file to firebase
            FirebaseStorage storage = FirebaseStorage.getInstance();
            // Create a storage reference from our app
            StorageReference storageRef = storage.getReference();
            File path = Environment.getExternalStorageDirectory();
            Uri file = Uri.fromFile(new File(csv.getAbsolutePath()));
            StorageReference riversRef = storageRef.child("Sensors/" + file.getLastPathSegment());
            UploadTask uploadTask = riversRef.putFile(file);

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(new OnFailureListener() {

                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                }
            });
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        registerAll();
        writePeriodicMeasurements();
    }

    @Override
    public void onDestroy() {
        try {


            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        timer.cancel();
        unregisterAll();
    }

    public boolean registerSensor(RegistrableSensorType type) {
        if (!registered[type.index]) {
            registered[type.index] = sensors[type.index].register();
        }

        return registered[type.index];
    }

    public void unregisterSensor(RegistrableSensorType type) {
        if (registered[type.index]) {
            registered[type.index] = false;
            sensors[type.index].unregister();
        }
    }

    @SuppressLint("MissingPermission")
    public void registerGPS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
//                Criteria criteria = new Criteria();
//                criteria.setAccuracy(Criteria.ACCURACY_FINE);
//                criteria.setPowerRequirement(Criteria.POWER_LOW);
//                criteria.setVerticalAccuracy(Criteria.NO_REQUIREMENT);
//                criteria.setHorizontalAccuracy(Criteria.ACCURAx`CY_MEDIUM);
//                criteria.setBearingRequired(false);
//                criteria.setSpeedRequired(false);
//                criteria.setCostAllowed(false);
                locationManager.requestLocationUpdates(/*locationManager.getBestProvider(criteria, true)*/LocationManager.GPS_PROVIDER, MyLocationListener.duration * 1000, 0, locationListener, Looper.myLooper());
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public void unregisterGPS() {
        locationManager.removeUpdates(locationListener);
    }

    public boolean registerAll() {
        registerGPS();
        boolean allRegistered = true;

        for (RegistrableSensorEventListener sensor : sensors) {
            allRegistered &= registerSensor(sensor.type);
        }

        return allRegistered;
    }

    public void unregisterAll() {
        unregisterGPS();

        for (RegistrableSensorEventListener sensor : sensors) {
            unregisterSensor(sensor.type);
        }
    }

    public void writePeriodicMeasurements() {
        int gcd = MyLocationListener.duration;

        for (int i = 0; i < sensors.length; i++) {
            if (registered[i]) {
                gcd = gcd(sensors[i].duration, gcd);
            }
        }

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    fileOutputStream.write((System.currentTimeMillis() + "," + join(locationListener.getLastMeasuredValues(), " | ")).getBytes());

                    for (RegistrableSensorEventListener sensor : sensors) {
                        fileOutputStream.write(("," + join(sensor.getLastMeasuredValues(), " | ")).getBytes());
                    }

                    fileOutputStream.write("\n".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, gcd * 1000);

    }

    private int gcd(int a, int b) {
        return (b == 0) ? a : gcd(b, a % b);
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
