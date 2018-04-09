package com.epoll.epoll;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;


import android.os.Bundle;

import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;



/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    final String webLink = "http://ec2-35-174-137-131.compute-1.amazonaws.com:8000/api/";
    Button mVoteBtn;
    JSONObject jsonObject = new JSONObject();
    final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    Editable  mEmailStr, mPassStr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText mEditEmail = (EditText) findViewById(R.id.email);
        final EditText mEditPass = (EditText) findViewById(R.id.password);
        mEmailStr = mEditEmail.getText();
        mPassStr = mEditPass.getText();

        Button mSignInBtn = (Button) findViewById(R.id.btn_sign_in);
        Button mSignUpBtn = (Button) findViewById(R.id.btn_signup);
        mVoteBtn = (Button) findViewById(R.id.btn_anon_vote);
        Button mVerifyVoteBtn = (Button) findViewById(R.id.btn_anon_verify);
        Button mViewResultBtn = findViewById(R.id.btn_view_results);
        final String webLink = "http://ec2-35-174-137-131.compute-1.amazonaws.com:8000/api";


        mSignUpBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
                LoginActivity.this.finish();
            }
        });


        mSignInBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(mEmailStr) || TextUtils.isEmpty(mPassStr)){
                    Toast.makeText(LoginActivity.this, "Please provide all details to sign in.", Toast.LENGTH_SHORT).show();
                }else{
                    new SignInCheckTask().execute(mEmailStr.toString(), mPassStr.toString());
                }
            }
        });

        mVoteBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject candidateSignJSON = null;
                Context context = getApplicationContext();
                String candidateString, signString;
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                //String candidateSignJSONStr = preferences.getString(getString(R.string.candidate), "DEFAULT");
                candidateString = preferences.getString(getString(R.string.candidate), "DEFAULT");
                signString = preferences.getString(getString(R.string.sign), "DEFAULT");
              //  SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                //String userPassJson = preferences.getString(getString(R.string.candidate), "DEFAULT");

                Log.d("candidateString", candidateString);
                Log.d("signString", signString);

                if(candidateString.equals("DEFAULT") || signString.equals("DEFAULT")){
                    Toast.makeText(LoginActivity.this, "Could not vote. Try signing vote before casting it.", Toast.LENGTH_LONG).show();
                }else{
                new VoteTask().execute(candidateString, signString);}
            }
        });

        mVerifyVoteBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                Toast.makeText(LoginActivity.this, "Your transaction ID: " + preferences.getString(getString(R.string.txnID), "DEFAULT"),
                        Toast.LENGTH_LONG).show();
            }
        });


        mViewResultBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                new ResultTask().execute();
            }
        });

    }

    public class ResultTask extends AsyncTask<Void, Integer, String>{
        int flag = 0;
        @Override
        protected String doInBackground(Void... voids) {
            String endPoint = "results/";
            OkHttpClient client = new OkHttpClient();
            okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
            //RequestBody body = RequestBody.create(JSON, voteObject.toString());
            builder.url(webLink + endPoint);
            okhttp3.Request request = builder.build();

            try {
                okhttp3.Response response = client.newCall(request).execute();
                String responseString = response.body().string();

                if(response.code() == 200){
                    Log.d("Results received", Integer.toString(response.code()));

                    //Log.d("Transaction String", responseString);
                    flag= 1;
                    return responseString;


                }else{
                    Log.e("Res not recd", "Results not received. Code: " + response.code() + " " + responseString);
                }

            }catch (IOException e){
                e.printStackTrace();
                Log.e("Response not recd", "No response");
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            String dtCount = null, hcCount = null;
            if(flag == 1){
            try {
                JSONObject jsonObject = new JSONObject(s);
                dtCount = jsonObject.getString("dt_count");
                hcCount = jsonObject.getString("hc_count");
                jsonObject.getString("hc_count");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Toast.makeText(LoginActivity.this, "Donald Trump: " + dtCount + " Hillary Clinton: "  + hcCount, Toast.LENGTH_LONG).show();
        }else{
                Toast.makeText(LoginActivity.this, "Try again.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public class VoteTask extends AsyncTask<String, Integer, String>{
        AlertDialog dialog;
        int flag = 0;
        @Override
        protected String doInBackground(String... strings) {
            String candidate = strings[0];
            String sign = strings[1];
            String endPoint = "vote/";
            JSONObject voteObject = new JSONObject();

            try {
                voteObject.put("message", candidate);
                voteObject.put("signature", sign);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            OkHttpClient client = new OkHttpClient();
            okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
            RequestBody body = RequestBody.create(JSON, voteObject.toString());
            builder.url(webLink + endPoint);
            builder.post(body);
            //builder.addHeader("Authorization", Credentials.basic(username, password));
            okhttp3.Request request = builder.build();

            try {
                okhttp3.Response response = client.newCall(request).execute();
                String responseString = response.body().string();
                //Toast.makeText(SignUpActivity.this, "Done", Toast.LENGTH_SHORT).show();
                if(response.code() == 200){
                    //Toast.makeText(LoginActivity.this, "Your vote has been cast!", Toast.LENGTH_SHORT).show();
                    flag  = 1;
                    Log.d("Vote cast!", Integer.toString(response.code()));

                    Log.d("Transaction String", responseString);
                    return responseString;


                }else{
                    Log.e("Vote not cast", "Could not vote. Code: " + response.code() + " " + responseString);

                 //   flag = 2;
                }

            }catch (IOException e){
                e.printStackTrace();
               // flag = 1;

                Log.e("Response not recd", "No response");

                //Toast.makeText(SignUpActivity.this, "ERROR: IOException", Toast.LENGTH_SHORT).show();
            }



            return null;
        }

        @Override
        protected void onPostExecute(final String s) {
            super.onPostExecute(s);
            dialog = new AlertDialog.Builder(LoginActivity.this).create();
            if(flag == 1){

            dialog.setTitle("Vote cast!");
            dialog.setMessage("Congratulations! Your vote has been cast! Your transaction ID is \n" + s);
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialog.dismiss();
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    preferences.edit().clear().commit();
                   // SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(getString(R.string.txnID),s);

                    editor.commit();
                    Log.d("After removing, TXID:",preferences.getString(getString(R.string.txnID), "DEFAULT"));
                    //Log.d("Unblinded sign",preferences.getString(getString(R.string.sign), "ANDROID"));
                    mVoteBtn.setVisibility(View.INVISIBLE);


                }
            });
            dialog.show();}
            else{
                dialog.setTitle("Error in vote casting!");
                dialog.setMessage("Sorry! Your vote could not be cast! Try signing your vote first.");
                dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                    }
                });
            }
        }
    }



    public class SignInCheckTask extends AsyncTask<String, Integer, String>{
        int flag = 1;
        AlertDialog dialog;
        String endPoint = "get-public-key";
        String username, password;
        @Override
        protected String doInBackground(String... strings) {
            username = strings[0];
            password = strings[1];
            Log.d("username", username);
            Log.d("password", password);

            try {
                jsonObject.put("username", username);
                jsonObject.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            OkHttpClient client = new OkHttpClient();
            okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
            RequestBody body = RequestBody.create(JSON, jsonObject.toString());
            builder.url(webLink + endPoint);
            builder.addHeader("Authorization", Credentials.basic(username, password));
            okhttp3.Request request = builder.build();

            try {
                okhttp3.Response response = client.newCall(request).execute();
                String responseString = response.body().string();
                //Toast.makeText(SignUpActivity.this, "Done", Toast.LENGTH_SHORT).show();
                if(response.code() == 200){

                    Log.d("Login successful", response.code() + " " + responseString);
                    return responseString;
                }else{
                    Log.e("Login unsuccessful", response.code() + " " + responseString);
                    flag = 0;
                }

            }catch (IOException e){
                e.printStackTrace();
                flag = 2;
                Log.e("Response not recd", "No response");

                //Toast.makeText(SignUpActivity.this, "ERROR: IOException", Toast.LENGTH_SHORT).show();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // Log.d("Response body", s);

            if(flag == 1) {
                Context context = getApplicationContext();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = preferences.edit();
                try {
                    editor.putString(getString(R.string.loginpass), jsonObject.toString());
                    editor.commit();
                    //sharedPref = context.getSharedPreferences(getString(R.string.loginpass), Context.MODE_PRIVATE);
                    Log.d("After setting", preferences.getString(getString(R.string.loginpass), "ANDROID"));
                    Log.d("OK", "OK");
                    Intent intent = new Intent(LoginActivity.this, SignMessageActivity.class);
                    startActivity(intent);
                    LoginActivity.this.finish();

                    //Toast.makeText(SignUpActivity.this, "Thanks for registering" + mFirstStr + " " + mLastStr, Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(flag == 2){
                dialog = new AlertDialog.Builder(LoginActivity.this).create();
                dialog.setTitle("Login UnSuccessful!");
                dialog.setMessage("Some technical issue. Please try again!");
                dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                flag = 1;
            }
            else{
                dialog = new AlertDialog.Builder(LoginActivity.this).create();
                dialog.setTitle("Login UnSuccessful!");
                dialog.setMessage("Wrong username/password. Please try again!");
                dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                flag = 1;
            }
        }
    }
}

