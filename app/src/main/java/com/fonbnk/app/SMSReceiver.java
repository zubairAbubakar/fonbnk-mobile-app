package com.fonbnk.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SMSReceiver extends BroadcastReceiver {

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String TAG = "SmsBroadCastReceiver";
    String msg, telcoPhoneNo, senderNumber, amount, displayOriginatingAddress = "";
    Transaction transaction = new Transaction();

    String baseApiUrl = "http://zubairabubakar.co:8080/fonbnk/api/";

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
                    telcoPhoneNo = message[i].getOriginatingAddress();

                }

                //RegExt Pattern for MTN's airtime receipt confirmation sms
                Pattern pattern = Pattern.compile("^Congrats!\\s+You\\s+have\\s+received\\s+N(\\d+)\\s+airtime\\s+from\\s+(\\d+)\\s+via\\s+MTN\\s+Share");
                Matcher matcher = pattern.matcher(msg);

                if (matcher.matches()){
                    amount = matcher.group(1);
                    senderNumber = matcher.group(2);

                }

                //get the latest airtime transfer done by the sender and the amount
                AndroidNetworking.get(baseApiUrl+ "transactions/search/"+
                        "findByAmountContainingAndSenderNumberContainingAndStatusContaining" +
                        "?amount="+amount+"&senderNumber="+senderNumber+"&status=initiated&sort=dateInitiated,desc")
                        .addHeaders("Content-Type", "application/json")
                        .setPriority(Priority.LOW)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // do anything with response

                                try {
                                    JSONObject embedded = response.getJSONObject("_embedded");
                                    JSONArray jsonTransactions = embedded.getJSONArray("transactions");

                                    Log.d("API response", jsonTransactions.get(0).toString());
                                    Log.d("Sender Number:", jsonTransactions.getJSONObject(0).getString("senderNumber"));
                                    Log.d("amount:", jsonTransactions.getJSONObject(0).getString("amount"));

                                    transaction.setId(jsonTransactions.getJSONObject(0).getString("id"));
                                    transaction.setDateInitiated(jsonTransactions.getJSONObject(0).getString("dateInitiated"));
                                    transaction.setSenderNumber(jsonTransactions.getJSONObject(0).getString("senderNumber"));
                                    transaction.setAmount(jsonTransactions.getJSONObject(0).getString("amount"));
                                    transaction.setRecipientNumber(jsonTransactions.getJSONObject(0).getString("recipientNumber"));
                                    transaction.setTransactionType(jsonTransactions.getJSONObject(0).getString("transactionType"));
                                    transaction.setStatus("RECEIVED");
                                    transaction.setDateReceived(jsonTransactions.getJSONObject(0).getString("dateReceived"));

                                }
                                catch (Exception e){
                                    e.printStackTrace();
                                }

                            }
                            @Override
                            public void onError(ANError error) {
                                // handle error
                                Log.d("API", "response : " + error);
                            }
                        });

                //Now that confirmation message is received, update the transaction status
                //send transaction details to transaction endpoint
                AndroidNetworking.put("http://zubairabubakar.co:8080/fonbnk/api/transactions/{id}")
                        .addPathParameter("id", transaction.getId())
                        .addApplicationJsonBody(transaction)
                        .addHeaders("Content-Type", "application/json")
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // do anything with response
                                Toast.makeText(context, "Transaction details updated successfully!",
                                        Toast.LENGTH_LONG).show();
                            }
                            @Override
                            public void onError(ANError error) {
                                // handle error
                                Log.d("API Error", "response : " + error);
                            }
                        });

//                Toast.makeText(context, "Message: "+msg +"\nNumber: "+telcoPhoneNo +"\nDisplay: "+displayOriginatingAddress,
//                        Toast.LENGTH_SHORT).show();
            }

        }
    }
}