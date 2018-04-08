package com.epoll.epoll;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText mEditEmail = (EditText) findViewById(R.id.edit_email);
        final EditText mEditPass = (EditText) findViewById(R.id.edit_password);

        Button mSignInBtn = (Button) findViewById(R.id.btn_sign_in);
        Button mSignUpBtn = (Button) findViewById(R.id.btn_signup);
        Button mVoteBtn = (Button) findViewById(R.id.btn_anon_vote);
        Button mVerifyVoteBtn = (Button) findViewById(R.id.btn_anon_verify);
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
                if(mEditEmail.getText() != null || mEditPass.getText() != null){
                    String endPoint = "/get-public-key";
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, webLink, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {


                            Log.i("Response", "Response: " + response.toString());
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Handle error
                            Log.e("Error", "Response not found!");
                            //Toast.makeText(SignMessageActivity.this, "Error, Response not found!", Toast.LENGTH_SHORT).show();

                        }
                    });
                    //queue.add(jsonObjectRequest);
                }
            }
        });


    }
}

