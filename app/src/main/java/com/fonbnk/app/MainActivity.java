package com.fonbnk.app;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.ANResponse;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_PHONE_NUMBERS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.READ_SMS;
import static android.Manifest.permission.RECEIVE_SMS;

public class MainActivity extends AppCompatActivity {

    //ask user for permissions
    private static final int PERMISSION_REQUEST_TO_RECEIVE_SMS = 0;
    private static final int PERMISSION_REQUEST_TO_MAKE_PHONE_CALL = 0;
    private static final int PERMISSION_REQUESTS_CODE = 100;

    private EditText recipientNumber;
    private EditText airtimeAmount;
    private EditText pin;
    private Button shareAirtime;
    private String sendNumber;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        //check if permission is not granted
//        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) !=
//                PackageManager.PERMISSION_GRANTED) {
//
//            //if permission not granted, check if user has denied the permission
//            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.RECEIVE_SMS)){
//
//                //since user denied do nothing
//            }
//            else {
//
//                //show pop-up to ask user for permission
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.RECEIVE_SMS}, PERMISSION_REQUEST_TO_RECEIVE_SMS);
//            }
//        }
//
//        //check if permission is not granted
//        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) !=
//                PackageManager.PERMISSION_GRANTED) {
//
//            //if permission not granted, check if user has denied the permission
//            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.CALL_PHONE)){
//
//                //since user denied do nothing
//            }
//            else {
//
//                //show pop-up to ask user for permission
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_REQUEST_TO_MAKE_PHONE_CALL);
//            }
//        }
//
//        //check if permission is not granted
//        if(ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) !=
//                PackageManager.PERMISSION_GRANTED) {
//
//            //if permission not granted, check if user has denied the permission
//            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.INTERNET)){
//
//                //since user denied do nothing
//            }
//            else {
//
//                //show pop-up to ask user for permission
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.INTERNET}, PERMISSION_REQUEST_TO_MAKE_PHONE_CALL);
//            }
//        }

        if (ActivityCompat.checkSelfPermission(this, READ_SMS) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, CALL_PHONE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, INTERNET) == PackageManager.PERMISSION_GRANTED
        ) {

            TelephonyManager telephonyManager = (TelephonyManager)   this.getSystemService(Context.TELEPHONY_SERVICE);
            sendNumber = telephonyManager.getLine1Number();


        } else {
            requestPermission();
        }


        recipientNumber = (EditText)findViewById(R.id.editTextRecipientNumber);
        airtimeAmount = (EditText)findViewById(R.id.editTextAirtimeAmount);
        pin = (EditText)findViewById(R.id.editTextPin);
        shareAirtime = (Button)findViewById(R.id.buttonShareAirtime);

        //add listener to the shareAirtime button
        shareAirtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                callUSSDToShareAirtime(recipientNumber.getText().toString(),
                        airtimeAmount.getText().toString(), pin.getText().toString(), sendNumber);
            }
        });
    }


    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{READ_SMS, READ_PHONE_NUMBERS, READ_PHONE_STATE,
                    CALL_PHONE, RECEIVE_SMS, INTERNET}, 100);
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


    private void callUSSDToShareAirtime(String recipientNumber, String airtimeAmount, String pin,
                                        String sendNumber){

        Transaction transaction = new Transaction();
        transaction.setAmount(airtimeAmount);
        transaction.setSenderNumber(sendNumber);
        transaction.setRecipientNumber(recipientNumber);
        transaction.setTransactionType("Airtime Transfer");
        transaction.setStatus("INITIATED");


        //send transaction details to transaction endpoint
        AndroidNetworking.post("http://zubairabubakar.co:8080/fonbnk/api/transactions/")
                .addApplicationJsonBody(transaction)
                .addHeaders("Content-Type", "application/json")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Toast.makeText(getApplicationContext(), "Transaction details submitted!", Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Log.d("API Error", "response : " + error);
                    }
                });


        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse(Uri.parse("tel:" + "*777*"+recipientNumber+"*"+airtimeAmount+"*"
                                    +pin)+Uri.encode("#")));{
            startActivity(intent);
        }

    }
}