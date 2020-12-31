package com.fonbnk.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver {

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String TAG = "SmsBroadCastReceiver";
    String msg, phoneNo, displayOriginatingAddress = "";

    @Override
    public void onReceive(Context context, Intent intent) {

        // Retrieves the general action to be performed and displayed on the log
        Log.i(TAG, "Intent Received: " +intent.getAction());
        if (intent.getAction() == SMS_RECEIVED){

            //retrieves a map of extended data from the intent
            Bundle dataBundle = intent.getExtras();
            if(dataBundle != null){

                //create PDU (Protocol Data Unit) object for transferring message
                Object[] pdu = (Object[])dataBundle.get("pdus");
                final SmsMessage[] message = new SmsMessage[pdu.length];

                for (int i = 0; i < pdu.length; i++ ){

                    //for build versions > API Level 23
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

                        String format = dataBundle.getString("format");

                        //from PDU get all SMS message object
                        message[i] = SmsMessage.createFromPdu((byte[])pdu[i], format);
                    }
                    else {

                        //Build versions < API Level 23
                        message[i] = SmsMessage.createFromPdu((byte[])pdu[i]);
                    }

                    msg = message[i].getMessageBody();
                    phoneNo = message[i].getOriginatingAddress();
                    displayOriginatingAddress = message[i].getDisplayOriginatingAddress();
                }

                Toast.makeText(context, "Message: "+msg +"\nNumber: "+phoneNo +"\nDisplay: "+displayOriginatingAddress,
                        Toast.LENGTH_SHORT).show();
            }

        }
    }
}