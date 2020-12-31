package com.fonbnk.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    //ask user for permission
    private static final int PERMISSION_REQUEST_TO_RECEIVE_SMS = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check if permission is not granted
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) !=
                PackageManager.PERMISSION_GRANTED) {

            //if permission not granted, check if user has denied the permission
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECEIVE_SMS)){

                //since user denied do nothing
            }
            else {

                //show pop-up to ask user for permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECEIVE_SMS}, PERMISSION_REQUEST_TO_RECEIVE_SMS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){

        //check the requestCode
        switch (requestCode){

            case PERMISSION_REQUEST_TO_RECEIVE_SMS:
            {
                //check if the length of the grantResults is greater than 0 and equal to PERMISSION_GRANTED
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    //Now broadcastreceiver works in the background
                    Toast.makeText(this, "Thank you for permitting !", Toast.LENGTH_LONG).show();
                }
                else {

                    Toast.makeText(this, "Sorry I need you permission to continue", Toast.LENGTH_LONG).show();
                }
            }
        }

    }
}