package com.example.halac.keyloggers_notify;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/*
This class contains all the services that are running in the app( Sensors, events, and audio services)
 */

public class RegistrableSensorManager extends Service {
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static String mood = null;
    public static RegistrableSensorManager Instance;
    private static SensorManager sensorManager;
    private static LocationManager locationManager;
    private static MyLocationListener locationListener;
    private static PowerManager.WakeLock wl;
    private final int eventsPeriod = 20; //   in seconds
    private final int audioPeriod = 60; //  in seconds
    private final int audioLength = 10; //  in seconds
    private static final int popupPeriod = 2 * 60 * 60; // in seconds (The pop-up appears after 2 hours)
    public DatabaseHelper db = new DatabaseHelper(this);
    private MediaRecorder recorder;
    private File audioRecordFolder;
    private Handler handler1 = new Handler();
    private CSVPrinter eventCounts;
    private CSVPrinter sensorData;
    private CSVPrinter mindWave;
    private RegistrableSensorEventListener[] sensors;
    private boolean[] registered;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (recorder != null) {
                try {
                    recorder.stop();
                } catch (RuntimeException ex) {
                }
            }
            recorder.reset();
            recorder.release();
        }
    };
    //pop-up handling
    private Runnable popupRunnable = new Runnable() {
        @Override
        public void run() {
            Intent bb = new Intent(RegistrableSensorManager.Instance.getApplicationContext(), MoodPopUp.class);
            bb.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            RegistrableSensorManager.Instance.startActivity(bb);
            handler1.postDelayed(this, popupPeriod * 1000);
        }
    };

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

    public static SensorManager getSensorManager() {
        if (sensorManager == null) {
            throw new RuntimeException("RegistrableSensorManager service hasn't start yet!!");
        }

        return sensorManager;
    }

    @Override
    //writing data on the csv files
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction().startsWith("com.example.halac.keyloggers_notify.action.startforeground")) {

            Notification notification = new NotificationCompat.Builder(this).setOngoing(true).build();
            startForeground(101, notification);
            if(!intent.getAction().endsWith("again"))
            {
                handler1.postDelayed(popupRunnable, popupPeriod * 1000);
            }

            String parent = getFilesDir().toString();
            try {
                File eventCountsFile = new File(parent + "/eventCounts.csv");
                CSVFormat eventCountsFileFormat = CSVFormat.DEFAULT;
                File sensorDataFile = new File(parent + "/sensorData.csv");
                CSVFormat sensorDataFileFormat = CSVFormat.DEFAULT;
                File mindWaveFile = new File(parent + "/mindWave.csv");
                CSVFormat mindWaveFileFormat = CSVFormat.DEFAULT;

                if(!eventCountsFile.exists())
                {
                    eventCountsFileFormat = CSVFormat.DEFAULT
                            .withHeader("time", "click #", "long click #", "scrolls #", "text #", "focused #", "window changed #", "logs #",
                                    "time facebook", "time whatsapp", "time instagram", "time camera", "time gallery", "time email",
                                    "time youtube", "time games", "camera #", "phone #", "calls #", "words #", "search #", "youtube vid #", "key logs");//what is number of phone(phone #)??!?!?
                }

                eventCounts = new CSVPrinter(new BufferedWriter(new FileWriter(eventCountsFile, true)), eventCountsFileFormat);
                List<String> sensorFileHeaders = new ArrayList<>();
                sensorFileHeaders.add("Time");
                sensorFileHeaders.add("GPS");

                for (RegistrableSensorEventListener sensor : sensors) {
                    sensorFileHeaders.add(sensor.type.toString());
                }

                String[] headers = new String[sensorFileHeaders.size()];
                sensorFileHeaders.toArray(headers);

                if(!sensorDataFile.exists())
                {
                    sensorDataFileFormat = CSVFormat.DEFAULT.withHeader(headers);
                }

                sensorData = new CSVPrinter(new BufferedWriter(new FileWriter(sensorDataFile, true)), sensorDataFileFormat);

                if(!mindWaveFile.exists())
                {
                    mindWaveFileFormat = CSVFormat.DEFAULT.withHeader("Time", "Type", "value");
                }

                mindWave = new CSVPrinter(new BufferedWriter(new FileWriter(mindWaveFile, true)), mindWaveFileFormat);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            registerAll();
            writePeriodicSensorMeasurements();
            writePeriodicEventsCounts();
            recordPeriodicClips();
        }
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        Instance = this;
        wl = ((PowerManager)getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wake lock");
        wl.acquire();
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
        String parent = getFilesDir().toString();
        audioRecordFolder = new File(parent, "AudioRecord");

        if (audioRecordFolder.exists() || !audioRecordFolder.isDirectory()) {
            audioRecordFolder.mkdirs();
        }
    }

    @Override
    public void onDestroy() {
        handler1.removeCallbacks(audioRunnable);
        handler1.removeCallbacks(sensorsRunnable);
        handler1.removeCallbacks(eventsRunnable);

        try {
            sensorData.close();
            eventCounts.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        unregisterAll();
        wl.release();
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
//                criteria.setHorizontalAccuracy(Criteria.ACCURACY_MEDIUM);
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

    //Audio recording handling
    private void recordAudio(String fileName) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            try {
                recorder = new MediaRecorder();
                String path = audioRecordFolder.getAbsolutePath() + "/" + fileName + ".3gp";
                // make sure the directory we plan to store the recording in exists
                //Sets the audio source to be used for recording.
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                //setting 3gp as output format
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                recorder.setMaxDuration(audioLength * 1000);
                //path of the audio recording to be stored
                recorder.setOutputFile(path);
                //Prepares the recorder to begin
                recorder.prepare();
                //begins capturing data
                recorder.start();
                handler1.postDelayed(runnable, audioLength * 1000);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    Runnable audioRunnable = new Runnable() {
        @Override
        public void run() {
            recordAudio(sdf.format(Calendar.getInstance().getTime()).replace(":", "-"));
            handler1.postDelayed(this, audioPeriod * 1000);
        }
    };

    private void recordPeriodicClips() {
        handler1.post(audioRunnable);
    }

    int gcd;
    Runnable sensorsRunnable = new Runnable() {
        @Override
        public void run() {
            writeSensorsData();
            handler1.postDelayed(this, gcd * 1000);
        }
    };

    private void writePeriodicSensorMeasurements() {
        gcd = MyLocationListener.duration;

        for (int i = 0; i < sensors.length; i++) {
            if (registered[i]) {
                gcd = gcd(sensors[i].duration, gcd);
            }
        }

        handler1.post(sensorsRunnable);
    }

    Runnable eventsRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                writeCounts();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            handler1.postDelayed(this, eventsPeriod * 1000);
        }
    };

    private void writePeriodicEventsCounts() {
        handler1.post(eventsRunnable);
    }

    private void writeSensorsData() {
        List<String> sensorsData = new ArrayList<>();
        sensorsData.add(sdf.format(Calendar.getInstance().getTime()));
        sensorsData.add(join(locationListener.getLastMeasuredValues(), " | "));

        for (RegistrableSensorEventListener sensor : sensors) {
            sensorsData.add(join(sensor.getLastMeasuredValues(), " | "));
        }

        if (mood != null) {
            sensorsData.add(mood);
            mood = null;
        }

        try {
            sensorData.printRecord(sensorsData);
            sensorData.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int gcd(int a, int b) {
        return (b == 0) ? a : gcd(b, a % b);
    }
    //Write counts for the phone activity events (scrolls, clicks..)
    private void writeCounts() throws ParseException {
        Calendar cal = Calendar.getInstance();
        long currentTime = cal.getTime().getTime();
        String time = sdf.format(cal.getTime());
        String where = DatabaseHelper.ECOL_3 + " <= '" + currentTime + "' AND " + DatabaseHelper.ECOL_3 + " >= '" + (currentTime - eventsPeriod * 1000) + "'";
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

        for (FeatureExtractor fe : f.getFreatures()) {
            input.add(fe.toString());
        }

        StringBuilder sb = new StringBuilder();

        for (Log log : listLogs) {
            if (log.getType().equals("TEXT")) {
                sb.append(log.getContext() + "\n");
            }
        }
        input.add(sb.toString());

        try {
            eventCounts.printRecord(input);
            eventCounts.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeMindWaveEvent(String type, int value)
    {
        try {
            mindWave.printRecord(sdf.format(Calendar.getInstance().getTime()), type, value);
            mindWave.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
