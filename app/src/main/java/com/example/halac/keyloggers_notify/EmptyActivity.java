package com.example.halac.keyloggers_notify;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
/*Used for entering the registration activity once, and for Location and Audio recording permissions
 */
public class EmptyActivity extends AppCompatActivity {
    static public final int REQUEST_LOCATION_AND_AUDIO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Requesting location and audio recording permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WAKE_LOCK }, REQUEST_LOCATION_AND_AUDIO);
        } else {
            runService();
        }
    }
    //Remembers the entering of the user
    private void runService() {
        Intent i = new Intent(this, RegistrableSensorManager.class);
        i.setAction("com.example.halac.keyloggers_notify.action.startforeground");
        startService(i);

        if (new DatabaseHelper(this).getUser() != null) {
            Intent aa = new Intent(this, SendDataActivity.class);
            aa.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(aa);

        } else {
            Intent bb = new Intent(this, launchActivity.class);
            bb.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(bb);

        }
    }

    @Override
    //What happens when the permission is granted or denied
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_AND_AUDIO:
                runService();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}