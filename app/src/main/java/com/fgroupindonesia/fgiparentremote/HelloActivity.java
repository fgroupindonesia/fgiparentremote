package com.fgroupindonesia.fgiparentremote;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.util.UUID;

import bean.User;
import helper.Keys;
import helper.Navigator;
import helper.RespondHelper;
import helper.URLReference;
import helper.UserData;
import helper.WebRequest;
import pl.droidsonroids.gif.GifImageView;

public class HelloActivity extends AppCompatActivity implements Navigator {

    LinearLayout linearFirst, linearSecond, linearThird;
    EditText editTextFullname, editTextEmail, editTextWhatsapp;

    GifImageView gifAnimatedWaitingFinal, gifAnimatedWaiting;

    Button buttonSave;
    TextView textViewMessage, textviewUUID, textViewLogin;
    int wrongLoginCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello);

        // for later usage
        UserData.setPreference(this);

        textviewUUID = (TextView) findViewById(R.id.textviewUUID);
        textViewMessage = (TextView) findViewById(R.id.textViewMessage);
        textViewLogin = (TextView) findViewById(R.id.textViewLogin);

        generateUUID();

        // underlining text
        textViewLogin.setPaintFlags(textViewLogin.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);

        buttonSave = (Button) findViewById(R.id.buttonSave);

        gifAnimatedWaitingFinal = (GifImageView) findViewById(R.id.gifAnimatedWaitingFinal);
        gifAnimatedWaiting = (GifImageView) findViewById(R.id.gifAnimatedWaiting);

        linearFirst = (LinearLayout) findViewById(R.id.linearFirst);
        linearSecond = (LinearLayout) findViewById(R.id.linearSecond);
        linearThird = (LinearLayout) findViewById(R.id.linearThird);

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
        textviewUUID.setText("UUID : " + id);

        UserData.savePreference(Keys.DEVICE_UUID, id);
    }

    public void closeApp(View v){
        nextActivity();
        // finishAffinity();
    }

    public void readyProceed(View v){
        linearFirst.setVisibility(View.GONE);
        linearSecond.setVisibility(View.VISIBLE);
    }

    public String getText(EditText ed){
        return  ed.getText().toString();
    }

    public String getText(Button ed){
        return  ed.getText().toString();
    }

    public void saveForm(View v){
        if(!getText(buttonSave).equalsIgnoreCase("login")) {
            UserData.savePreference(Keys.EMAIL, getText(editTextEmail));
            UserData.savePreference(Keys.FULLNAME, getText(editTextFullname));
            UserData.savePreference(Keys.WHATSAPP, getText(editTextWhatsapp));

            hideDataForm();

            UserData.savePreference(Keys.FORM_FILLED, true);

            successMessage();
        }else{
            // if it is for login
            if(getText(editTextWhatsapp).isEmpty() || getText(editTextEmail).isEmpty()){
                textViewMessage.setText("Please fill the login cridential here...");
                buzzEffect();
            }else {
                lockTemporarily(true);
                loginUser();
            }

        }

    }

    private void buzzEffect(){
        ObjectAnimator rotate = ObjectAnimator.ofFloat(gifAnimatedWaiting, "rotation", 0f, 20f, 0f, -20f, 0f); // rotate o degree then 20 degree and so on for one loop of rotation.

        rotate.setRepeatCount(10); // repeat the loop 10 times
        rotate.setDuration(75); // animation play time 75 ms
        rotate.start();
    }

    private void loginUser() {


        WebRequest httpCall = new WebRequest(HelloActivity.this, HelloActivity.this);

        httpCall.addData("email", getText(editTextEmail));
        httpCall.addData("whatsapp", getText(editTextWhatsapp));

        // we need to wait for the response
        httpCall.setWaitState(true);
        httpCall.setRequestMethod(WebRequest.POST_METHOD);
        httpCall.setTargetURL(URLReference.RemoteLogin);
        httpCall.execute();

    }

    public void showLoginForm(View v){
        textViewMessage.setText("Okay, let's try logging in...");
        linearThird.setVisibility(View.GONE);
        linearSecond.setVisibility(View.VISIBLE);

        buttonSave.setText("Login");

        editTextFullname.setVisibility(View.GONE);
        textViewLogin.setVisibility(View.GONE);
    }

    private void hideDataForm(){
        gifAnimatedWaiting.setVisibility(View.GONE);
        gifAnimatedWaitingFinal.setVisibility(View.VISIBLE);

        editTextFullname.setVisibility(View.INVISIBLE);
        editTextEmail.setVisibility(View.INVISIBLE);
        editTextWhatsapp.setVisibility(View.INVISIBLE);

        buttonSave.setVisibility(View.INVISIBLE);
    }

    private void successMessage(){
        int TIME_OUT = 3000;

        textViewMessage.setText("waiting...");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                linearThird.setVisibility(View.VISIBLE);
                linearSecond.setVisibility(View.GONE);
            }
        }, TIME_OUT);
    }

    private void lockTemporarily(boolean b){
        editTextWhatsapp.setEnabled(!b);
        editTextEmail.setEnabled(!b);
        editTextFullname.setEnabled(!b);
        
        buttonSave.setEnabled(!b);
    }

    @Override
    public void nextActivity(){
        int TIME_OUT = 3000;

        linearThird.setVisibility(View.GONE);
        linearSecond.setVisibility(View.VISIBLE);

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



    @Override
    public void onSuccess(String urlTarget, String respond) {

        try {
            // ShowDialog.message(this, "dapt " + respond);

            Gson objectG = new Gson();

            if (RespondHelper.isValidRespond(respond)) {

                if (urlTarget.contains(URLReference.RemoteLogin)) {
                    JSONObject jsons = RespondHelper.getObject(respond, "multi_data");

                    User objectUser = objectG.fromJson(jsons.toString(), User.class);

                    UserData.savePreference(Keys.EMAIL, objectUser.getEmail());
                    UserData.savePreference(Keys.FULLNAME, objectUser.getFullname());
                    UserData.savePreference(Keys.WHATSAPP, objectUser.getWhatsapp());

                    UserData.savePreference(Keys.FORM_FILLED, true);

                    nextActivity();
                }


            }else{

                // when the login fails
                if (urlTarget.contains(URLReference.RemoteLogin)) {
                    textViewMessage.setText("Oops, Login failed...!");
                    lockTemporarily(false);
                    wrongLoginCount++;
                    buzzEffect();

                    if(wrongLoginCount==2){
                        textViewMessage.setText("Are you sure already registered?");
                    }else if(wrongLoginCount>2){
                        textViewMessage.setText("Please contact admin for more help!");
                    }
                }
            }


        } catch (Exception ex) {

        }

    }
}