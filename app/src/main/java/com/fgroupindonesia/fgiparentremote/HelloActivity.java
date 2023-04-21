package com.fgroupindonesia.fgiparentremote;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.UUID;

import helper.Keys;
import helper.UserData;
import pl.droidsonroids.gif.GifImageView;

public class HelloActivity extends AppCompatActivity {

    LinearLayout linearFirst, linearSecond;
    EditText editTextFullname, editTextEmail, editTextWhatsapp;

    GifImageView gifAnimatedWaitingFinal, gifAnimatedWaiting;

    Button buttonSave;
    TextView textViewMessage, textviewUUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello);

        // for later usage
        UserData.setPreference(this);

        textviewUUID = (TextView) findViewById(R.id.textviewUUID);
        textViewMessage = (TextView) findViewById(R.id.textViewMessage);

        generateUUID();

        buttonSave = (Button) findViewById(R.id.buttonSave);

        gifAnimatedWaitingFinal = (GifImageView) findViewById(R.id.gifAnimatedWaitingFinal);
        gifAnimatedWaiting = (GifImageView) findViewById(R.id.gifAnimatedWaiting);

        linearFirst = (LinearLayout) findViewById(R.id.linearFirst);
        linearSecond = (LinearLayout) findViewById(R.id.linearSecond);

        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextFullname = (EditText) findViewById(R.id.editTextFullname);
        editTextWhatsapp = (EditText) findViewById(R.id.editTextWhatsapp);

        // first time
        boolean formFilled = UserData.getPreferenceBoolean(Keys.FORM_FILLED);
        if(!formFilled) {
            linearFirst.setVisibility(View.VISIBLE);
            linearSecond.setVisibility(View.GONE);
        } else{
            hideDataForm();
            nextActivity();
        }

    }

    private void generateUUID(){
        String id = UUID.randomUUID().toString();
        textviewUUID.setText(id);
    }

    public void readyProceed(View v){
        linearFirst.setVisibility(View.GONE);
        linearSecond.setVisibility(View.VISIBLE);
    }

    public String getText(EditText ed){
        return  ed.getText().toString();
    }

    public void saveForm(View v){
        UserData.savePreference(Keys.EMAIL, getText(editTextEmail));
        UserData.savePreference(Keys.FULLNAME, getText(editTextFullname));
        UserData.savePreference(Keys.WHATSAPP, getText(editTextWhatsapp));

        hideDataForm();

        UserData.savePreference(Keys.FORM_FILLED, true);

        nextActivity();

    }

    private void hideDataForm(){
        gifAnimatedWaiting.setVisibility(View.GONE);
        gifAnimatedWaitingFinal.setVisibility(View.VISIBLE);

        editTextFullname.setVisibility(View.INVISIBLE);
        editTextEmail.setVisibility(View.INVISIBLE);
        editTextWhatsapp.setVisibility(View.INVISIBLE);

        buttonSave.setVisibility(View.INVISIBLE);
    }

    private void nextActivity(){
        int TIME_OUT = 3000;

        textViewMessage.setText("waiting...");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(HelloActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }, TIME_OUT);
    }
}