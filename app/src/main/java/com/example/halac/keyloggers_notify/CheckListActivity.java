package com.example.halac.keyloggers_notify;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CheckListActivity extends AppCompatActivity implements Button.OnClickListener {
    private ListView listView;
    private Button SubmitData, Cancel;
    private TextView fileSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_main);

        findViewsById();

        RegistrableSensorManager rsm = RegistrableSensorManager.Instance;
        String parent = rsm.getFilesDir().toString() + "/";
        String zipName = parent + "testArchive.zip";
        String[] fileList = { parent + "eventCounts.csv", parent + "sensorData.csv", parent + "AudioRecord"};
        Uploader.zipFiles(fileList, zipName);
        File file = new File(zipName);
        fileSize.setText("Approximate maximum file size: " + Formatter.formatShortFileSize(rsm, file.length()));
        file.delete();
        String[] elements = getResources().getStringArray(R.array.elements);
        CustomAdapter adapter = new CustomAdapter(this, elements);

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(adapter);

        SubmitData.setOnClickListener(this);
        Cancel.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.SubmitData:
                final List<String> selected = getSelectedItems();
                StringBuilder builder = new StringBuilder();
                for (String value : selected) {
                    builder.append(value+" ,");
                }
                String text = builder.toString();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Uploader.upload(selected.contains("Audio Files"), selected.contains("Key Logger"), selected.contains("GPS"), selected.contains("Time Spent"), selected.contains("Sensors"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                finish();
                Toast.makeText(getApplicationContext(), text+" are successfully sent",Toast.LENGTH_LONG).show();
                break;

            case R.id.cancel:
                finish();
                break;
        }
    }

    private void findViewsById() {
        listView = (ListView) findViewById(R.id.list);
        SubmitData = (Button) findViewById(R.id.SubmitData);
        Cancel=(Button)findViewById(R.id.cancel);
        fileSize=(TextView) findViewById(R.id.fileSize);
    }

    private List<String> getSelectedItems() {
        List<String> result = new ArrayList<>();
        SparseBooleanArray checkedItems = listView.getCheckedItemPositions();

        for (int i = 0; i < listView.getCount(); ++i) {
            if (checkedItems.valueAt(i)) {
                result.add((String) listView.getItemAtPosition(checkedItems.keyAt(i)));
            }
        }

        return result;
    }
}