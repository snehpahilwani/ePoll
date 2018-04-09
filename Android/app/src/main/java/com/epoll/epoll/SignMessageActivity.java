package com.epoll.epoll;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;


import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;


public class SignMessageActivity extends AppCompatActivity {

    BigInteger m,r;
    RadioGroup radioGroup;
    final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    int flagChecked = 0;
    final String webLink = "http://ec2-35-174-137-131.compute-1.amazonaws.com:8000/api/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);
        radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        Button mBlindButton = (Button)findViewById(R.id.button_blind);

        mBlindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int id = radioGroup.getCheckedRadioButtonId();

                String msg = "";
                //int flag = 0;
                if (id == R.id.radioButton) {
                    msg = "Donald Trump";
                    flagChecked = 1;
                } else if (id == R.id.radioButton2) {
                    msg = "Hillary Clinton";
                    flagChecked = 1;
                }

                if(flagChecked == 0){
                    Toast.makeText(SignMessageActivity.this, "Please select an option.", Toast.LENGTH_SHORT).show();
                    //flagChecked = 0;
                }else{
                    Context context = getApplicationContext();
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                    String userPassJson = preferences.getString(getString(R.string.loginpass), "DEFAULT");
                    SharedPreferences.Editor editor = preferences.edit();
                    //preferences.edit();
                    editor.putString(getString(R.string.candidate), msg);
                    editor.commit();
                    //sharedPref = context.getSharedPreferences(getString(R.string.loginpass), Context.MODE_PRIVATE);
                    Log.d("After setting", preferences.getString(getString(R.string.candidate), "ANDROID"));
                    if(userPassJson.equals("DEFAULT")){
                        Toast.makeText(SignMessageActivity.this, "Technical Difficulties. Please sign in again.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignMessageActivity.this, LoginActivity.class);
                        startActivity(intent);
                        SignMessageActivity.this.finish();
                    }else
                    {
                        new DownloadPublicKey().execute(userPassJson, msg);
                    }
                    flagChecked = 0;
                }
            }
        });


    }

    public BigInteger calculateMPrime(String message, BigInteger e, BigInteger N){
        try {
            String hashedMessage = new String(Hex.encodeHex(DigestUtils.sha1(message)));
            //String hashedMessage = DigestUtils.sha1Hex(message);
            byte[] msgByteArr = hashedMessage.getBytes("UTF-8");
            m = new BigInteger(msgByteArr);
            SecureRandom rand = new SecureRandom();
                    //SecureRandom.getInstance("SHA1PRNG", "SUN");
            byte[] randomBytes = new byte[10];
            BigInteger one = new BigInteger("1");
            BigInteger gcd = null;
                do
                {
                    rand.nextBytes(randomBytes);

                    r = new BigInteger(randomBytes);

                    gcd = r.gcd(N);

                }
                while (!gcd.equals(one) || r.compareTo(N) >= 0 || r.compareTo(one) <= 0);
                BigInteger mu = ((r.modPow(e, N)).multiply(m)).mod(N);
                Log.d("While calculation", mu.toString());
                return mu;
        } catch (Exception e1){
            e1.printStackTrace();
            Log.d("While calculation", "Something is wrong with mu calculation");
            return null;
        }


    }

    public String signCalculation(BigInteger sPrime, BigInteger N){
        try
        {
            BigInteger s = r.modInverse(N).multiply(sPrime).mod(N);
            //byte[] bytes = new Base64().encode(s.toByteArray());
            //String signature = (new String(bytes));
            Log.d("INFO","Signature produced with Blind RSA procedure for message (hashed with SHA1): " + new String(m.toByteArray()) + " is: ");
            Log.d("Signature",s.toString());
            return s.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public class DownloadPublicKey extends AsyncTask<String, Integer, BigInteger> {
        String eValue = null, nValue = null;
        String username = null, password = null;
        AlertDialog dialog;
        int flag = 0;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new AlertDialog.Builder(SignMessageActivity.this).create();
        }

        @Override
        protected BigInteger doInBackground(String... strings) {
            //RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            BigInteger mPrime = null;
            String endPoint = "get-public-key";

            try {
                JSONObject userPassJson = new JSONObject(strings[0]);

                username = userPassJson.getString("username");
                password = userPassJson.getString("password");
                Log.d("User", username);
                Log.d("Password", password);

            } catch (JSONException e) {
                e.printStackTrace();
            }


            OkHttpClient client = new OkHttpClient();
            okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
            //RequestBody body = RequestBody.create(JSON, jsonObject.toString());
            builder.url(webLink + endPoint);
            builder.addHeader("Authorization", Credentials.basic(username, password));
            okhttp3.Request request = builder.build();

            try {
                okhttp3.Response response = client.newCall(request).execute();
                String responseString = response.body().string();
                //Toast.makeText(SignUpActivity.this, "Done", Toast.LENGTH_SHORT).show();
                if(response.code() == 200){
                    JSONObject ENValue = new JSONObject(responseString);
                    eValue = ENValue.getString("e");
                    nValue = ENValue.getString("n");
                    Log.d("E", eValue);
                    Log.d("N", nValue);
                    Log.d("Received e,n", Integer.toString(response.code()));

                }else{
                    Log.e("Not received", "Could not receive public key. Code: " + response.code() + " " + responseString);
                    flag = 2;
                }

            }catch (IOException e){
                e.printStackTrace();
                flag = 1;
                Log.e("Response not recd", "No response");

                //Toast.makeText(SignUpActivity.this, "ERROR: IOException", Toast.LENGTH_SHORT).show();
            }catch (JSONException e){
                e.printStackTrace();
            }
           // return "";


            String msg = strings[1];
            if ((eValue != null || nValue != null) && flag == 0) {
                //Toast.makeText(SignMessageActivity.this, "You voted for " + msg, Toast.LENGTH_SHORT).show();
                mPrime = calculateMPrime(msg, new BigInteger(eValue), new BigInteger(nValue));
                Log.d("mPrime", mPrime.toString());
            }else{
               // Toast.makeText(SignMessageActivity.this, "Values N and E not found", Toast.LENGTH_SHORT).show();
            }

            return mPrime;
        }


        //String endPoint = "/api/signature";
        //endpoString endPoint = "/api/signature";
        @Override
        protected void onPostExecute(final BigInteger mPrime) {
            super.onPostExecute(mPrime);

            if(mPrime != null){
                dialog.setTitle("Blind Message");
                dialog.setMessage(mPrime.toString());
                dialog.setButton(AlertDialog.BUTTON_POSITIVE, "SEND",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(SignMessageActivity.this, "SEND", Toast.LENGTH_SHORT).show();
                                new DownloadSignatureTask(username, password).execute(mPrime, new BigInteger(nValue));
                            }
                        });
                dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DO NOT SEND",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(SignMessageActivity.this, "DID NOT SEND", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });
                dialog.show();
            }else{
                Toast.makeText(SignMessageActivity.this, "MPrime value not found!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class DownloadSignatureTask extends AsyncTask<BigInteger, Integer, String>{
        BigInteger mPrime,nValue;
        AlertDialog dialog1 = null;
        String sPrime;
        String username = null, password = null;
        public DownloadSignatureTask(String username, String password){
            this.username = username;
            this.password = password;
        }

        @Override
        protected String doInBackground(BigInteger... urls) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("message", urls[0].toString());
                Log.d("message", urls[0].toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            nValue = urls[1];
            OkHttpClient client = new OkHttpClient();
            okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
            RequestBody body = RequestBody.create(JSON, jsonObject.toString());
            String endPoint = "signature/";
            builder.url(webLink + endPoint);
            builder.post(body);
            Log.d("User Pass check",username + " " + password);
            //Log.d("Request body", builder.)
            builder.addHeader("Authorization", Credentials.basic(username, password));
            okhttp3.Request request = builder.build();

            try {
                okhttp3.Response response = client.newCall(request).execute();
                String responseString = response.body().string();
                //Toast.makeText(SignUpActivity.this, "Done", Toast.LENGTH_SHORT).show();
                if(response.code() == 200){

                    Log.d("Signature received", response.code() + " " + responseString);
                    return responseString;
                }else{
                    Log.e("Sign not recd", response.code() + " " + responseString);
                    //flag = 0;
                }

            }catch (IOException e){
                e.printStackTrace();
                //flag = 2;
                Log.e("Response not recd", "No response");

                //Toast.makeText(SignUpActivity.this, "ERROR: IOException", Toast.LENGTH_SHORT).show();
            }
            return null;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {

                JSONObject sPrimeJSON = new JSONObject(s);
                String sPrime = sPrimeJSON.getString("sign");
                Log.d("sPrime", sPrime);
                Log.d("nValue", nValue.toString());
                String sign = signCalculation(new BigInteger(sPrime), nValue);
                //return sign;
                Log.d("Sign after unblind" ,sign);
                Context context = getApplicationContext();

                //Context context = getApplicationContext();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                //String userPassJson = preferences.getString(getString(R.string.candidate), "DEFAULT");
                SharedPreferences.Editor editor = preferences.edit();
                //preferences.edit();
                editor.putString(getString(R.string.sign),sign);
                editor.commit();



                editor.commit();
                Log.d("Unblinded sign",preferences.getString(getString(R.string.sign), "ANDROID"));
                Log.d("Candidate", preferences.getString(getString(R.string.candidate), "DEFAULT"));



                dialog1 = new AlertDialog.Builder(SignMessageActivity.this).create();
                //dialog1 = new AlertDialog();
                dialog1.setTitle("Signature recorded");
                dialog1.setMessage("Your signature has been recorded. Please cast your vote on vote day now! Logging out.");
                dialog1.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(SignMessageActivity.this, LoginActivity.class);
                                startActivity(intent);
                                SignMessageActivity.this.finish();
                            }
                        });

                dialog1.show();
                //Toast.makeText(SignMessageActivity.this, "Signature has been saved.", Toast.LENGTH_SHORT).show();
            }catch (Exception e){
                //Toast.makeText(SignMessageActivity.this, "Signature could not be saved.", Toast.LENGTH_SHORT).show();
            }
        }
    }


}


