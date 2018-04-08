package com.epoll.epoll;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Button mRegisterButton = (Button)findViewById(R.id.btn_register);
        final EditText mEditEmail = (EditText)findViewById(R.id.edit_email);
        final EditText mEditPass = (EditText)findViewById(R.id.edit_password);
        final EditText mEditFirst = (EditText)findViewById(R.id.edit_firstname);
        final EditText mEditLast = (EditText)findViewById(R.id.edit_lastname);
        Button mBtnRegister = (Button)findViewById(R.id.btn_register);

        final Editable mEmailStr = mEditEmail.getText();
        final Editable mPassStr = mEditPass.getText();
        final Editable mFirstStr = mEditFirst.getText();
        final Editable mLastStr = mEditLast.getText();


        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(mEmailStr) || TextUtils.isEmpty(mPassStr)|| TextUtils.isEmpty(mFirstStr)
                        || TextUtils.isEmpty(mLastStr)){
                    Toast.makeText(SignUpActivity.this, "Please provide all details to register.", Toast.LENGTH_SHORT).show();
                }else{
//                    OkHttpClient client = new OkHttpClient();
//                    try {
//                        jsonObject.put("username", mEmailStr);
//                        jsonObject.put("password", mPassStr);
//                        jsonObject.put("first_name", mFirstStr);
//                        jsonObject.put("last_name", mLastStr);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
                    new OkHttpClientTask().execute(mEmailStr.toString(), mPassStr.toString(), mFirstStr.toString(), mLastStr.toString());

//                    RequestBody body = RequestBody.create(JSON, jsonObject.toString());
//                    Request request = new Request.Builder()
//                            .url(webLink)
//                            .build();
//
//                    try {
//                        Response response = client.newCall(request).execute();
//                        Log.d("Response", response.body().string());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

//                    try {
//                        jsonObject.put("username", mEmailStr);
//                        jsonObject.put("password", mPassStr);
//                        jsonObject.put("first_name", mFirstStr);
//                        jsonObject.put("last_name", mLastStr);
//                        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
//                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, webLink, jsonObject, new Response.Listener<JSONObject>() {
//
//                            @Override
//                            public void onResponse(JSONObject response) {
//
//                                try {
//
//                                    Intent intent = new Intent(SignUpActivity.this, SignMessageActivity.class);
//
//                                    Context context = getApplicationContext();
//
//                                    SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.loginpass), Context.MODE_PRIVATE);
//                                    SharedPreferences.Editor editor = sharedPref.edit();
//                                    JSONObject jsonObject1 = new JSONObject();
//                                    jsonObject1.put("userName", mEmailStr);
//                                    jsonObject1.put("password", mPassStr);
//                                    editor.putString(getString(R.string.loginpass), jsonObject1.toString());
//                                    //String string = "sdafdafd";
//
//                                    editor.commit();
//                                    Log.d("OK", "OK");
//                                    Toast.makeText(SignUpActivity.this, "Thanks for registering" + mFirstStr + " " + mLastStr, Toast.LENGTH_SHORT).show();
//                                    startActivity(intent);
//                                    SignUpActivity.this.finish();
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                                //  Log.i("Response", "Response: " + response.toString());
//                            }
//                        }, new Response.ErrorListener() {
//
//                            @Override
//                            public void onErrorResponse(VolleyError error) {
//                                // TODO: Handle error
//                                Log.e("Error", error.toString());
//
//                                Toast.makeText(SignUpActivity.this, "Error, Response not found!", Toast.LENGTH_SHORT).show();
//
//                            }
//                        }){
//
//                            @Override
//                            public Map<String, String> getHeaders() throws AuthFailureError {
//                                HashMap<String, String> headers = new HashMap<String,String>();
//                                // headers.put("Content-Type", "application/json; charset=utf-8");
//                                return headers;
//                            }
//                            @Override
//                            public String getBodyContentType() {
//                                return "application/json";
//                            }
//                            @Override
//                            public String getBodyContentType() {
//                                return "application/json; charset=utf-8";
//                            }
//                            @Override
//                            public Map<String, String> getHeaders() throws AuthFailureError {
//                                Map<String,String> params = new HashMap<String, String>();
//                                params.put("Content-Type","application/x-www-form-urlencoded");
//                                return params;
//                            }
                        //};
                        //queue.add(jsonObjectRequest);

                }
            }
        });




    }

    public class OkHttpClientTask extends AsyncTask<String, Integer, String>{
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
                //Toast.makeText(SignUpActivity.this, "Done", Toast.LENGTH_SHORT).show();
                return response.body().string();
            }catch (Exception e){
                e.printStackTrace();
                //Toast.makeText(SignUpActivity.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("Response body", s);
        }
    }
}
