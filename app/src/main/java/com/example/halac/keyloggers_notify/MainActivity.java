package com.example.halac.keyloggers_notify;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    static public final int REQUEST_LOCATION = 1;
    EditText fname;
    EditText lname;
    EditText gender;
    EditText age;
    EditText mood;
    EditText comments;
    Button add;
    Boolean flag1=false;
    Boolean flag2=false;
    Boolean flag3=false;
    Boolean flag4=false;
    Boolean flag5=false;
    Boolean flag6=false;

    DatabaseHelper db = new DatabaseHelper(this);
    DatabaseReference database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = FirebaseDatabase.getInstance().getReference("Users");

        fname = (EditText) findViewById(R.id.fname);
        lname = (EditText) findViewById(R.id.lname);
        gender = (EditText) findViewById(R.id.gender);
        age = (EditText) findViewById(R.id.age);
        mood = (EditText) findViewById(R.id.mood);
        comments = (EditText) findViewById(R.id.comment);
        add = (Button) findViewById(R.id.add);

        Intent intentService = new Intent(this, MyAccessibilityService.class);
        this.startService(intentService);


        add.setEnabled (false);

        check(); // cannot press the button if not filled

        add.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                addUser();
                Intent redirect = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivityForResult(redirect, 1);
                /*
                fname.setText("");
                lname.setText("");
                gender.setText("");
                age.setText("");
                mood.setText("");
                comments.setText("");*/
            }
        });

        startService(new Intent(this, RegistrableSensorManager.class));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    RegistrableSensorManager.Instance.registerGPS();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void addUser(){
        String fname1 = fname.getText().toString().trim();
        String lname1 = lname.getText().toString().trim();
        String age1 = age.getText().toString().trim();
        String mood1 = mood.getText().toString().trim();
        String gender1 = gender.getText().toString().trim();
        String comments1 = comments.getText().toString().trim();

        fname.setText("");
        lname.setText("");
        gender.setText(gender1);
        age.setText(age1);
        mood.setText(mood1);
        comments.setText("");

        db.deleteUser();
        db.insertUser(fname1,lname1, age1, mood1, gender1, comments1);
    }

    public void check(){
        fname.addTextChangedListener(new TextWatcher () {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()>0){
                    flag1 = true;
                    if(flag1==true&&flag2==true&&flag3==true&&flag4==true&&flag5==true&&flag6==true){
                        add.setEnabled (true);
                    }
                }
                else {
                    flag1 = false;
                    add.setEnabled (false);
                }

            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        lname.addTextChangedListener(new TextWatcher () {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()>0){
                    flag2 = true;
                    if(flag1==true&&flag2==true&&flag3==true&&flag4==true&&flag5==true&&flag6==true) {
                        add.setEnabled (true);
                    }
                }
                else {
                    flag2 = false;
                    add.setEnabled (false);
                }

            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        gender.addTextChangedListener(new TextWatcher () {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()>0){
                    flag3 = true;
                    if(flag1==true&&flag2==true&&flag3==true&&flag4==true&&flag5==true&&flag6==true) {
                        add.setEnabled (true);
                    }
                }
                else {
                    flag3 = false;
                    add.setEnabled (false);

                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        age.addTextChangedListener(new TextWatcher () {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()>0) {
                    flag4 = true;
                    if(flag1==true&&flag2==true&&flag3==true&&flag4==true&&flag5==true&&flag6==true){
                        add.setEnabled (true);
                    }
                }
                else {
                    flag4 = false;
                    add.setEnabled (false);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        comments.addTextChangedListener(new TextWatcher () {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()>0) {
                    flag5 = true;
                    if(flag1==true&&flag2==true&&flag3==true&&flag4==true&&flag5==true&&flag6==true){
                        add.setEnabled (true);
                    }
                }
                else {
                    flag5 = false;
                    add.setEnabled (false);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mood.addTextChangedListener(new TextWatcher () {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()>0) {
                    flag6 = true;

                    if(flag1==true&&flag2==true&&flag3==true&&flag4==true&&flag5==true&&flag6==true){
                        add.setEnabled (true);
                    }
                }
                else {
                    flag6 = false;
                    add.setEnabled (false);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
}
