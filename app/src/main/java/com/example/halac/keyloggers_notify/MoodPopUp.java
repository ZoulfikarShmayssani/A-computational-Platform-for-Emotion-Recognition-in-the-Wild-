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
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

public class MoodPopUp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_mood_pop_up);
        dialog.setTitle("How do you feel?");
        dialog.setCancelable(true);

        RadioButton rd1 = (RadioButton) dialog.findViewById(R.id.Happy);
        RadioButton rd2 = (RadioButton) dialog.findViewById(R.id.Sad);
        RadioButton rd3 = (RadioButton) dialog.findViewById(R.id.Neutral);
        RadioButton rd4 = (RadioButton) dialog.findViewById(R.id.Angry);
        Button submit= (Button) dialog.findViewById(R.id.Submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
