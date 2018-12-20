package com.example.halac.keyloggers_notify;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class SendDataActivity extends AppCompatActivity {


    Button checkList;
    Button emtionSelection;
    Button mindWave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_data);
        checkList = (Button) findViewById(R.id.SendDataChecklist);
        checkList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent confirm = new Intent(SendDataActivity.this, CheckListActivity.class);
                startActivity(confirm);

            }
        });
        emtionSelection = (Button) findViewById(R.id.button2);
        emtionSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent confirm = new Intent(SendDataActivity.this, MoodPopUp.class);
                startActivity(confirm);

            }
        });
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
