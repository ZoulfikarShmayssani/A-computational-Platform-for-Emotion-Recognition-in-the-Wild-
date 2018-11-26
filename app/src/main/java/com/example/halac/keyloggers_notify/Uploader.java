package com.example.halac.keyloggers_notify;

import android.content.Intent;
import android.provider.Settings;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Uploader {
    private static final String ACCESS_TOKEN = "PQ1-gA3ADaYAAAAAAAAASRr_mmSTWeOMIJpu71uUbRVoaJMZebTH-H1mNyFhiQxA";

    public static void upload(boolean audio, boolean keyLogger, boolean gps, boolean times, boolean sensors) throws IOException {
        RegistrableSensorManager rsm = RegistrableSensorManager.Instance;
        Intent service = new Intent(rsm.getApplicationContext(), RegistrableSensorManager.class);
        service.setAction("com.example.halac.keyloggers_notify.action.startforegroundagain");
        rsm.stopService(service);
        String parent = rsm.getFilesDir().toString() + "/";
        String fileName = rsm.sdf.format(Calendar.getInstance().getTime()).replace(":", "-") + " " + Settings.Secure.getString(rsm.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID) + ".zip";
        String filePath = parent + fileName;
        List<String> filesToUpload = new ArrayList<>();
        File sensorData = new File(parent + "sensorData.csv");
        File eventCounts = new File(parent + "eventCounts.csv");

        if(audio)
        {
            filesToUpload.add(parent + "AudioRecord");
        }

        if(gps && sensors)
        {
            filesToUpload.add(parent + "sensorData.csv");
        }
        else if(gps ^ sensors)
        {
            filesToUpload.add(parent + "sensorData.csv");
            Scanner in = new Scanner(sensorData);
            List<String> lines = new ArrayList<>();

            while (in.hasNextLine())
            {
                lines.add(in.nextLine());
            }

            in.close();
            sensorData.delete();
            PrintStream ps = new PrintStream(sensorData);
            int splitIndex = 2;

            if(gps)
            {
                for (int i = 0; i < lines.size(); i++)
                {
                    String line = lines.get(i);
                    ps.println(line.substring(0, ithIndexOf(line, ",", splitIndex)));
                }
            }
            else
            {
                for (int i = 0; i < lines.size(); i++)
                {
                    String line = lines.get(i);
                    ps.println(line.substring(0, line.indexOf(',')) + line.substring(ithIndexOf(line, ",", splitIndex)));
                }
            }

            ps.close();
        }

        if(times && keyLogger)
        {
            filesToUpload.add(parent + "eventCounts.csv");
        }
        else if(times ^ keyLogger)
        {
            filesToUpload.add(parent + "eventCounts.csv");
            Scanner in = new Scanner(eventCounts);
            List<String> lines = new ArrayList<>();

            while (in.hasNextLine())
            {
                lines.add(in.nextLine());
            }

            in.close();
            eventCounts.delete();
            PrintStream ps = new PrintStream(eventCounts);
            int splitIndex = 22;

            if(times)
            {
                for (int i = 0; i < lines.size(); i++)
                {
                    String line = lines.get(i);
                    ps.println(line.substring(0, ithIndexOf(line, ",", splitIndex)));
                }
            }
            else
            {
                for (int i = 0; i < lines.size(); i++)
                {
                    String line = lines.get(i);
                    ps.println(line.substring(0, line.indexOf(',')) + line.substring(ithIndexOf(line, ",", splitIndex)));
                }
            }

            ps.close();
        }

        Users u = rsm.db.getUser();
        File userInfo = new File(parent + "userInfo.txt");
        PrintStream ps = new PrintStream(userInfo);
        ps.println(u.fname + " " + u.lname);
        ps.println(u.age);
        ps.println(u.gender);
        ps.close();
        filesToUpload.add(parent + "userInfo.txt");
        String[] filesList = new String[filesToUpload.size()];
        filesToUpload.toArray(filesList);
        zipFiles(filesList, filePath);

        DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
        DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
        try {
            InputStream in = new FileInputStream(filePath);
            client.files().uploadBuilder("/emotions/" + fileName).uploadAndFinish(in);
        }
        catch (DbxException e)
        {
            e.printStackTrace();
        }

        userInfo.delete();
        sensorData.delete();
        eventCounts.delete();
        new File(filePath).delete();

        for (File f: new File(parent + "AudioRecord").listFiles()){
            f.delete();
        }

        rsm.startService(service);
    }

    private static int ithIndexOf(String str, String substr, int i)
    {
        int index = str.indexOf(substr);

        for(int currentIndexOrder = 1; currentIndexOrder < i; currentIndexOrder++)
        {
            if(index == -1)
            {
                break;
            }

            index = str.indexOf(substr, index + 1);
        }

        return index;
    }

    private static final int BUFFER_SIZE = 2048;

    public static boolean zipFiles(String[] filesList, String toLocation)
    {
        FileOutputStream dest;

        try {
            dest = new FileOutputStream(toLocation);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
        boolean result = zipFiles(out, filesList);
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static boolean zipFiles(ZipOutputStream out, String[] filesList) {
        try {
            for(String file: filesList) {
                File sourceFile = new File(file);

                if (sourceFile.isDirectory()) {
                    List<String> filesPaths = new ArrayList<>();

                    for (File f: sourceFile.listFiles())
                    {
                        filesPaths.add(f.getAbsolutePath());
                    }

                    String[] filesAbsolutePaths = new String[filesPaths.size()];
                    filesPaths.toArray(filesAbsolutePaths);

                    zipFiles(out, filesAbsolutePaths);
                } else {
                    byte data[] = new byte[BUFFER_SIZE];
                    FileInputStream fi = new FileInputStream(file);
                    BufferedInputStream origin = new BufferedInputStream(fi, BUFFER_SIZE);
                    ZipEntry entry = new ZipEntry(getLastPathComponent(file));
                    entry.setTime(sourceFile.lastModified()); // to keep modification time after unzipping
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                        out.write(data, 0, count);
                    }
                    origin.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static String getLastPathComponent(String filePath) {
        String[] segments = filePath.split("/");
        if (segments.length == 0)
            return "";

        return segments[segments.length - 1];
    }
}
