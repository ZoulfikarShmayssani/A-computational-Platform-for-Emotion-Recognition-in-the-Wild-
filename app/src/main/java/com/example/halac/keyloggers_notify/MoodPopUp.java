package com.example.halac.keyloggers_notify;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import java.util.Timer;
import java.util.TimerTask;

public class MoodPopUp extends AppCompatActivity {
    private static Timer timer;
    private static final int timerPeriod = 2 * 60 * 60; // in seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_mood_pop_up);
        dialog.setTitle("How do you feel?");
        dialog.setCancelable(true);
        final RadioButton rd1 = (RadioButton) dialog.findViewById(R.id.Anxious);
        final RadioButton rd2 = (RadioButton) dialog.findViewById(R.id.Sad);
        final RadioButton rd3 = (RadioButton) dialog.findViewById(R.id.Anxious);
        final RadioButton rd4 = (RadioButton) dialog.findViewById(R.id.Angry);
        Button submit = (Button) dialog.findViewById(R.id.Submit);


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RegistrableSensorManager rsm = RegistrableSensorManager.Instance;
                /*String parent = rsm.getFilesDir().toString() + "/";
                String zipName = parent + "testArchive.zip";
                String[] fileList = { parent + "eventCounts.csv", parent + "sensorData.csv", parent + "AudioRecord"};
                Uploader.zipFiles(fileList, zipName);
                File file = new File(zipName);
                Toast.makeText(rsm.getBaseContext(), Formatter.formatShortFileSize(rsm, file.length()), Toast.LENGTH_LONG).show();
                file.delete();*/
                if (rd1.isChecked()) {
                    rsm.mood = "happy";
                } else if (rd2.isChecked()) {
                    rsm.mood = "sad";
                } else if (rd3.isChecked()) {
                    rsm.mood = "anxious";
                } else if (rd4.isChecked()) {
                    rsm.mood = "angry";
                }
                finish();
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        dialog.show();

    }

    public static void activity() {
        if(timer != null)
        {
            timer.cancel();
            timer = null;
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Intent bb = new Intent(RegistrableSensorManager.Instance.getApplicationContext(), MoodPopUp.class);
                RegistrableSensorManager.Instance.startActivity(bb);
            }
        }, 0, timerPeriod * 1000);
    }
}