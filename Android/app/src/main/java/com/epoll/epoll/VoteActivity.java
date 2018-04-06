package com.epoll.epoll;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class VoteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);

//        RadioButton mButton1 = (RadioButton)findViewById(R.id.radioButton);
//        RadioButton mButton2 = (RadioButton)findViewById(R.id.radioButton2);
        Button mBlindButton = (Button)findViewById(R.id.button_blind);
        final RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        final AlertDialog dialog = new AlertDialog.Builder(VoteActivity.this).create();
        mBlindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = radioGroup.getCheckedRadioButtonId();

                String msg = "";
                int flag = 0;
                if(id == R.id.radioButton){
                    msg = "The blind message for Trump";
                    flag = 1;
                    //Toast.makeText(VoteActivity.this, "You voted for Duck", Toast.LENGTH_SHORT).show();
                }else if(id == R.id.radioButton2){
                    msg = "The blind message for Clinton";
                    flag = 1;
                    //Toast.makeText(VoteActivity.this, "You voted for Clinton", Toast.LENGTH_SHORT).show();
                }

                if(flag == 0){
                    Toast.makeText(VoteActivity.this, "Please select an option first.", Toast.LENGTH_SHORT).show();
                }else{
                    dialog.setTitle("Blind message");
                    dialog.setMessage(msg);
                    dialog.setButton(AlertDialog.BUTTON_POSITIVE, "SEND",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Toast.makeText(VoteActivity.this, "SEND", Toast.LENGTH_SHORT).show();
                                }
                            });
                    dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DO NOT SEND",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //Toast.makeText(VoteActivity.this, "SEND", Toast.LENGTH_SHORT).show();
                                }
                            });
                    dialog.show();
                   //
                    // builder.setMessage(msg).setTitle("Blind message");
//                    dialog = builder.create();
//                    builder.setPositiveButton("SEND", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            // User clicked OK button
//                        }
//                    });
//                    builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            // User cancelled the dialog
//                        }
//                    });
                    flag = 0;
                    dialog.show();
                }

            }
        });


    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radioButton:
                if (checked)
                    // Pirates are the best
                    break;
            case R.id.radioButton2:
                if (checked)
                    // Ninjas rule
                    break;
        }
    }
}
