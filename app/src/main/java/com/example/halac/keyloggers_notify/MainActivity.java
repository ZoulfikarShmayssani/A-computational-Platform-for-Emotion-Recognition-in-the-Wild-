package com.example.halac.keyloggers_notify;

import android.Manifest;
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


public class MainActivity extends AppCompatActivity {
    static public final int REQUEST_LOCATION = 1;
    static public final int REQUEST_EXTERNAL_STORAGE_RW = 2;

    EditText fname, lname, gender, age;
    Button add,checkList;
    Boolean flag1 = false;
    Boolean flag2 = false;
    Boolean flag3 = false;
    Boolean flag4 = false;

    DatabaseHelper db = new DatabaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fname = (EditText) findViewById(R.id.fname);
        lname = (EditText) findViewById(R.id.lname);
        gender = (EditText) findViewById(R.id.gender);
        age = (EditText) findViewById(R.id.age);
        add = (Button) findViewById(R.id.add);
        checkList=(Button)findViewById(R.id.SendDataChecklist);

        Intent intentService = new Intent(this, MyAccessibilityService.class);
        this.startService(intentService);


        add.setEnabled(false);

        check(); // cannot press the button if not filled

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUser();
                Intent redirect = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivityForResult(redirect, 1);
            }
        });

        checkList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent confirm= new Intent(MainActivity.this,CheckListActivity.class);
                startActivity(confirm);

            }
        });



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.RECORD_AUDIO}, REQUEST_LOCATION);
        } else {
            runService();
        }
    }

    private void runService() {
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) || ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, REQUEST_EXTERNAL_STORAGE_RW);
        } else {
            startService(new Intent(this, RegistrableSensorManager.class));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                runService();
                break;
            case REQUEST_EXTERNAL_STORAGE_RW:
                startService(new Intent(this, RegistrableSensorManager.class));
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    private void addUser() {
        String fname1 = fname.getText().toString().trim();
        String lname1 = lname.getText().toString().trim();
        String age1 = age.getText().toString().trim();
        String gender1 = gender.getText().toString().trim();


        fname.setText("");
        lname.setText("");
        gender.setText(gender1);
        age.setText(age1);

        db.deleteUser();
        db.insertUser(fname1, lname1, age1, gender1);
    }

    public void check() {
        fname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    flag1 = true;
                    if (flag1 == true && flag2 == true && flag3 == true && flag4 == true) {
                        add.setEnabled(true);
                    }
                } else {
                    flag1 = false;
                    add.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        lname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    flag2 = true;
                    if (flag1 == true && flag2 == true && flag3 == true && flag4 == true ) {
                        add.setEnabled(true);
                    }
                } else {
                    flag2 = false;
                    add.setEnabled(false);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        gender.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    flag3 = true;
                    if (flag1 == true && flag2 == true && flag3 == true && flag4 == true ) {
                        add.setEnabled(true);
                    }
                } else {
                    flag3 = false;
                    add.setEnabled(false);

                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        age.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    flag4 = true;
                    if (flag1 == true && flag2 == true && flag3 == true && flag4 == true ) {
                        add.setEnabled(true);
                    }
                } else {
                    flag4 = false;
                    add.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
}
