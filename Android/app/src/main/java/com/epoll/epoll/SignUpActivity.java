package com.epoll.epoll;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignUpActivity extends AppCompatActivity {
    JSONObject jsonObject = new JSONObject();
    final String webLink = "http://ec2-35-174-137-131.compute-1.amazonaws.com:8000/api/signup/";
    final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    Editable mEmailStr;
    Editable mPassStr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        final Button mRegisterButton = (Button)findViewById(R.id.btn_register);
        final EditText mEditEmail = (EditText)findViewById(R.id.edit_email);
        final EditText mEditPass = (EditText)findViewById(R.id.edit_password);
        final EditText mEditFirst = (EditText)findViewById(R.id.edit_firstname);
        final EditText mEditLast = (EditText)findViewById(R.id.edit_lastname);
        Button mBtnRegister = (Button)findViewById(R.id.btn_register);

        mEmailStr = mEditEmail.getText();
        mPassStr = mEditPass.getText();
        final Editable mFirstStr = mEditFirst.getText();
        final Editable mLastStr = mEditLast.getText();


        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(mEmailStr) || TextUtils.isEmpty(mPassStr)|| TextUtils.isEmpty(mFirstStr)
                        || TextUtils.isEmpty(mLastStr)){
                    Toast.makeText(SignUpActivity.this, "Please provide all details to register.", Toast.LENGTH_SHORT).show();
                }else{
                    new OkHttpClientTask().execute(mEmailStr.toString(), mPassStr.toString(), mFirstStr.toString(), mLastStr.toString());



                }
            }
        });




    }

    public class OkHttpClientTask extends AsyncTask<String, Integer, String>{
        int flag = 1;
        AlertDialog dialog;
        @Override
        protected String doInBackground(String... strings) {

            Log.d("username", strings[0]);
            Log.d("password", strings[1]);
            Log.d("first_name", strings[2]);
            Log.d("last_name", strings[3]);
            OkHttpClient client = new OkHttpClient();
            try {
                jsonObject.put("username", strings[0]);
                jsonObject.put("password", strings[1]);
                jsonObject.put("first_name", strings[2]);
                jsonObject.put("last_name", strings[3]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Request.Builder builder = new Request.Builder();
            RequestBody body = RequestBody.create(JSON, jsonObject.toString());
            builder.url(webLink);
            builder.post(body);
            Request request = builder.build();

            try {
                Response response = client.newCall(request).execute();
                String responseString = response.body().string();
                //Toast.makeText(SignUpActivity.this, "Done", Toast.LENGTH_SHORT).show();
                if(response.code() == 201){

                    Log.d("Request made", response.code() + " " + responseString);
                     return responseString;
                }else{
                    Log.e("User exists", response.code() + " " + responseString);
                    flag = 0;


                    //Toast.makeText(SignUpActivity.this, "Registration not successful as user already exists.", Toast.LENGTH_SHORT).show();
                }

            }catch (IOException e){
                e.printStackTrace();
                flag = 0;
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

            JSONObject jsonObject1 = new JSONObject();
            try {
                jsonObject1.put("userName", mEmailStr);
                jsonObject1.put("password", mPassStr);
                editor.putString(getString(R.string.loginpass), jsonObject1.toString());
                editor.commit();
                //sharedPref = context.getSharedPreferences(getString(R.string.loginpass), Context.MODE_PRIVATE);
                Log.d("After setting", preferences.getString(getString(R.string.loginpass), "ANDROID"));
                Log.d("OK", "OK");

                dialog = new AlertDialog.Builder(SignUpActivity.this).create();
                dialog.setTitle("Registration Successful!");
                dialog.setMessage("Thanks for registering! Press OK to proceed.");
                dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                        Intent intent = new Intent(SignUpActivity.this, SignMessageActivity.class);
                        startActivity(intent);
                        SignUpActivity.this.finish();
                    }
                });
                dialog.show();

                //Toast.makeText(SignUpActivity.this, "Thanks for registering" + mFirstStr + " " + mLastStr, Toast.LENGTH_SHORT).show();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            dialog = new AlertDialog.Builder(SignUpActivity.this).create();
            dialog.setTitle("Registration UnSuccessful!");
            dialog.setMessage("User already exists. Cannot register!");
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
