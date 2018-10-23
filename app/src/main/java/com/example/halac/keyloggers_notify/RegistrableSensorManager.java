package com.example.halac.keyloggers_notify;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RegistrableSensorManager extends Service {
    public static RegistrableSensorManager Instance;
    private static SensorManager sensorManager;
    private static LocationManager locationManager;
    private static MyLocationListener locationListener;
    FileOutputStream fileOutputStream;
    MediaRecorder recorder;
    File audioRecordFolder;
    Handler handler1 = new Handler();
    DatabaseHelper db = new DatabaseHelper(this);
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    CSVPrinter csvPrinter;

    Runnable runnable4 = new Runnable() {
        @Override
        public void run() {
            recorder.stop();
            recorder.reset();
            recorder.release();
        }

    };
    private RegistrableSensorEventListener[] sensors;
    private boolean[] registered;
    private Timer timer;
    private Timer audioTimer;

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
        audioTimer = new Timer();
        String parent;
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || !Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            parent = getFilesDir().toString();
        }
        else
        {
            parent = Environment.getExternalStorageDirectory().toString();
        }
        File csv = new File(parent, "sensorData.csv");
        audioRecordFolder = new File(parent, "AudioRecord");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(parent + "/eventCounts.csv"));
            csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                    .withHeader("time", "click #", "long click #", "scrolls #", "text #", "focused #", "window changed #", "logs #",
                    "time facebook", "time whatsapp", "time instagram", "time camera", "time gallery", "time email",
                    "time youtube", "time games", "camera #", "phone #", "calls #", "words #", "search #", "youtube vid #", "key logs"));//what is number of phone(phone #)??!?!?
        } catch (IOException e) {
            e.printStackTrace();
        }
        // TODO: take into consideration that the user might revoke permissions later
        try {
            if(audioRecordFolder.exists() || !audioRecordFolder.isDirectory())
            {
                audioRecordFolder.mkdirs();
            }
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
        recordPeriodicClips();

    }

    @Override
    public void onDestroy() {
        try {
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        timer.cancel();
        audioTimer.cancel();
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

    public void recordAudio(String fileName) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            try {
                recorder = new MediaRecorder();
                String path = audioRecordFolder.getAbsolutePath() + "/" + fileName + ".3gp";
                String state = android.os.Environment.getExternalStorageState();
                if (!state.equals(android.os.Environment.MEDIA_MOUNTED)) {
                }
                // make sure the directory we plan to store the recording in exists
                //Sets the audio source to be used for recording.
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                //setting 3gp as output format
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                recorder.setMaxDuration(10000);
                //path of the audio recording to be stored
                recorder.setOutputFile(path);
                //Prepares the recorder to begin
                recorder.prepare();
                //begins capturing data
                recorder.start();
                handler1.postDelayed(runnable4, 10000);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void recordPeriodicClips() {
        audioTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                recordAudio(sdf.format(Calendar.getInstance().getTime()).replace(":", "-"));
            }
        }, 0, 60000);
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
                    writeCounts();
                    fileOutputStream.write((sdf.format(Calendar.getInstance().getTime()) + "," + join(locationListener.getLastMeasuredValues(), " | ")).getBytes());

                    for (RegistrableSensorEventListener sensor : sensors) {
                        fileOutputStream.write(("," + join(sensor.getLastMeasuredValues(), " | ")).getBytes());
                    }

                    fileOutputStream.write("\n".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }, 0, gcd * 1000);

    }

    private int gcd(int a, int b) {
        return (b == 0) ? a : gcd(b, a % b);
    }

    public void writeCounts() throws ParseException {
        Calendar cal = Calendar.getInstance();
        long currentTime = cal.getTime().getTime();
        String time = sdf.format(cal.getTime());
        //TODO: 20000 stands for 20 sec, so make this a variable. also make own timer !!!
        String where = DatabaseHelper.ECOL_3 + " <= '" + currentTime + "' AND " + DatabaseHelper.ECOL_3 + " >= '" + (currentTime - 20000) + "'";
        List<Log> listLogs = db.getLogs(where);

        FeaturesExtraction f = new FeaturesExtraction();

        //get click count (1)
        f.addExtractor(new ExtractNbEvent(listLogs, "CLICKED"));

        //get long click count (2)
        f.addExtractor(new ExtractNbEvent(listLogs, "LONG CLICKED"));

        //get scrolls count (3)
        f.addExtractor(new ExtractNbEvent(listLogs, "SCROLLED"));

        //get text count (4)
        f.addExtractor(new ExtractNbEvent(listLogs, "TEXT"));

        //get focused count (5)
        f.addExtractor(new ExtractNbEvent(listLogs, "FOCUSED"));

        //get number of change window (6)
        f.addExtractor(new ExtractNbEvent(listLogs, "CHANGE"));

        //get logs count (7)
        f.addExtractor(new ExtractNbLogs(listLogs));

        //get time in Facebook (8)
        f.addExtractor(new ExtractTimeSpent(listLogs, ".*(Facebook).*"));

        //get time in Whatsapp (9)
        f.addExtractor(new ExtractTimeSpent(listLogs, ".*(WhatsApp).*"));

        //get time in Instagram (10)
        f.addExtractor(new ExtractTimeSpent(listLogs, ".*(Instagram).*"));

        //get time in Camera (11)
        f.addExtractor(new ExtractTimeSpent(listLogs, ".*(Camera).*"));

        //get time in Gallery (12)
        f.addExtractor(new ExtractTimeSpent(listLogs, ".*(Gallery).*"));

        //get time in Email (13)
        f.addExtractor(new ExtractTimeSpent(listLogs, ".*(Email|Gmail|Outlook).*"));

        //get time in Youtube (14)
        f.addExtractor(new ExtractTimeSpent(listLogs, ".*(YouTube).*"));

        //time spent in games (15)
        f.addExtractor(new ExtractTimeSpent(listLogs, ".*(PrincessSalon|Candy Crush Saga|Six Guns|8 Ball Pool|Subway Surfers|Clash of Clans|Clash Royale|Hay Day|Pou|PUBG MOBILE).*"));

        //get number of  Camera (16)
        f.addExtractor(new ExtractAppCount(listLogs, "[Camera]"));

        //get number of  Phone (17)
        f.addExtractor(new ExtractAppCount(listLogs, "[Phone]"));

        // count calls (18)
        f.addExtractor(new ExtractAppCount(listLogs, "[Dialling"));

        //add social media ??

        //get word count (19)
        f.addExtractor(new ExtractNumWords(listLogs));

        //get search count (20)
        f.addExtractor(new ExtractNbSearchCount(listLogs));

        //number of Youtube videos (21)
        f.addExtractor(new ExtractnNbYoutubeVideo(listLogs));

        // print all the features for all users
        List<String> input = new ArrayList<>();
        input.add(time);

        for(FeatureExtractor fe: f.getFreatures())
        {
            input.add(fe.toString());
        }

        StringBuilder sb = new StringBuilder();

        for (Log log: listLogs)
        {
            if(log.getType().equals("TEXT")) {
                sb.append(log.getContext() + "\n");
            }
        }
        input.add(sb.toString());

        try
        {
            csvPrinter.printRecord(input);
            csvPrinter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
