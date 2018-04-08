package com.epoll.epoll;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;


public class SignMessageActivity extends AppCompatActivity {

    BigInteger m,r;
    final RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group);

    final String webLink = "http://ec2-35-174-137-131.compute-1.amazonaws.com:8000";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);

//        RadioButton mButton1 = (RadioButton)findViewById(R.id.radioButton);
//        RadioButton mButton2 = (RadioButton)findViewById(R.id.radioButton2);
        Button mBlindButton = (Button)findViewById(R.id.button_blind);

        mBlindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String endPoint = "/api/get-public-key";
                    new DownloadPublicKey().execute(new URL(webLink + endPoint));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    public BigInteger calculateMPrime(String message, BigInteger e, BigInteger N){
        try {
        String hashedMessage = DigestUtils.sha1Hex(message);
        byte[] msgByteArr = hashedMessage.getBytes("UTF-8");
        m = new BigInteger(msgByteArr);
        SecureRandom rand = SecureRandom.getInstance("SHA1PRNG", "SUN");
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

            return mu;
        } catch (Exception e1){
            e1.printStackTrace();
            return null;
        }


    }

    public String signCalculation(BigInteger sPrime, BigInteger N){
        try
        {
            BigInteger s = r.modInverse(N).multiply(sPrime).mod(N);
            byte[] bytes = new Base64().encode(s.toByteArray());
            String signature = (new String(bytes));
            Log.i("INFO","Signature produced with Blind RSA procedure for message (hashed with SHA1): " + new String(m.toByteArray()) + " is: ");
            Log.i("Signature",signature);
            return signature;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public class DownloadPublicKey extends AsyncTask<URL, Integer, BigInteger> {
        String eValue = null, nValue = null;
        AlertDialog dialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new AlertDialog.Builder(SignMessageActivity.this).create();
        }

        @Override
        protected BigInteger doInBackground(URL... urls) {
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            BigInteger mPrime = null;
            String endPoint = "/api/signature";
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, urls[0].toString(), null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {

                    try {
                        eValue = response.getString("e");
                        nValue = response.getString("n");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        eValue = "";
                        nValue = "";
                        //return null;
                    }
                    Log.i("Response", "Response: " + response.toString());
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO: Handle error
                    Log.e("Error", "Response not found!");
                    Toast.makeText(SignMessageActivity.this, "Error, Response not found!", Toast.LENGTH_SHORT).show();

                }
            });
            queue.add(jsonObjectRequest);
            //  Tuple tuple = new Tuple(new BigInteger(eValue), new BigInteger(nValue));


            int id = radioGroup.getCheckedRadioButtonId();

            String msg = "";
            int flag = 0;
            if (id == R.id.radioButton) {
                msg = "Trump";
                flag = 1;
                //Toast.makeText(SignMessageActivity.this, "You voted for Duck", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.radioButton2) {
                msg = "Clinton";
                flag = 1;
                //Toast.makeText(SignMessageActivity.this, "You voted for Clinton", Toast.LENGTH_SHORT).show();
            }


            if (flag == 0) {
                Toast.makeText(SignMessageActivity.this, "Please select an option first.", Toast.LENGTH_SHORT).show();
            } else {
                if (eValue != null || nValue != null) {
                    Toast.makeText(SignMessageActivity.this, "You voted for " + msg, Toast.LENGTH_SHORT).show();
                    mPrime = calculateMPrime(msg,new BigInteger(eValue), new BigInteger(nValue));
                }else{
                    Toast.makeText(SignMessageActivity.this, "Values N and E not found", Toast.LENGTH_SHORT).show();
                }
            }
            return mPrime;
        }


        String endPoint = "/api/signature";
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
                                try {
                                    new DownloadSignatureTask(mPrime, new BigInteger(nValue)).execute(new URL(webLink + endPoint));
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                }
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

    public class DownloadSignatureTask extends AsyncTask<URL, Integer, String>{
        BigInteger mPrime,nValue;
        String sPrime;
        public DownloadSignatureTask(BigInteger mPrime, BigInteger nValue){
            this.mPrime = mPrime;
            this.nValue = nValue;
        }

        @Override
        protected String doInBackground(URL... urls) {
            JSONObject jsonObject = new JSONObject();
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

            try {
                jsonObject.put("mPrime", mPrime.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, urls[0].toString(), jsonObject, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {

                    try {
                        sPrime = response.getString("sign");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.i("Response", "Response: " + response.toString());
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO: Handle error
                    Log.e("Error", "Response not found!");
                    Toast.makeText(SignMessageActivity.this, "Error, Response not found!", Toast.LENGTH_SHORT).show();

                }


            }){


                //Pass Your Parameters here
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
//                    params.put("User", UserName);
//                    params.put("Pass", PassWord);
                    return params;
                }
            };


            queue.add(jsonObjectRequest);
            String sign = signCalculation(new BigInteger(sPrime), nValue);
            return sign;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                Context context = getApplicationContext();

                SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.sign), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.sign), s);
                editor.commit();
                Toast.makeText(SignMessageActivity.this, "Signature has been saved.", Toast.LENGTH_SHORT).show();
            }catch (Exception e){
                Toast.makeText(SignMessageActivity.this, "Signature could not be saved.", Toast.LENGTH_SHORT).show();
            }
        }
    }


}


