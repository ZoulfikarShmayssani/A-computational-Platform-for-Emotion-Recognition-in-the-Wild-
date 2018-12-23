package com.example.halac.keyloggers_notify;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
/*
The class is used to go from activity to another when you click on( Enter current emotion, Send data checklist, and mind wave buttons)
 */

public class SendDataActivity extends AppCompatActivity {


    Button checkList;
    Button emtionSelection;
    Button mindWave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_data);
        checkList = (Button) findViewById(R.id.SendDataChecklist);
        //Send data checklist button
        checkList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent confirm = new Intent(SendDataActivity.this, CheckListActivity.class);
                startActivity(confirm);

            }
        });
        //Enter your current emotion button
        emtionSelection = (Button) findViewById(R.id.button2);
        emtionSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent confirm = new Intent(SendDataActivity.this, MoodPopUp.class);
                startActivity(confirm);

            }
        });
        //mind wave data button
        mindWave = (Button) findViewById(R.id.MindwaveData);
        mindWave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent confirm = new Intent(SendDataActivity.this, MindWaveSensor.class);
                startActivity(confirm);

            }
        });
    }
}
