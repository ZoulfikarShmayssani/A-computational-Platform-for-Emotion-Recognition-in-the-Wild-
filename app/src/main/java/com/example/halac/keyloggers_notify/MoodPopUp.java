package com.example.halac.keyloggers_notify;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.File;

public class MoodPopUp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_mood_pop_up);
        dialog.setTitle("How do you feel?");
        dialog.setCancelable(true);

        final RadioButton rd1 = (RadioButton) dialog.findViewById(R.id.Happy);
        final RadioButton rd2 = (RadioButton) dialog.findViewById(R.id.Sad);
        final RadioButton rd3 = (RadioButton) dialog.findViewById(R.id.Neutral);
        final RadioButton rd4 = (RadioButton) dialog.findViewById(R.id.Angry);
        Button submit= (Button) dialog.findViewById(R.id.Submit);

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
                if(rd1.isChecked())
                {
                    rsm.mood = "happy";
                }
                else if(rd2.isChecked())
                {
                    rsm.mood = "sad";
                }
                else if(rd3.isChecked())
                {
                    rsm.mood = "neutral";
                }
                else if(rd4.isChecked())
                {
                    rsm.mood = "angry";
                }
            }
        });

        dialog.show();


    }
    /*
    public void setOnetimeTimer(Context context) {
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, MoodPopUp.class);
        intent.putExtra("ONE_TIME", Boolean.TRUE);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (1000 * 60 * 5), pi);
    }
    */

}
