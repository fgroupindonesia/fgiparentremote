package com.fgroupindonesia.fgiparentremote;

import android.os.Bundle;
import android.os.StrictMode;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

import helper.SocketScanner;

public class ScanLocallyActivity  extends AppCompatActivity {

    TextView textViewTextLoading;
    LinearLayout linearLoading, linearChooseTarget;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_locally);

        textViewTextLoading = (TextView) findViewById(R.id.textViewTextLoading);
        textViewTextLoading.setSingleLine(false);
        textViewTextLoading.setInputType(textViewTextLoading.getInputType()| InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        linearChooseTarget = (LinearLayout) findViewById(R.id.linearChooseTarget);
        linearLoading = (LinearLayout) findViewById(R.id.linearLoading);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // scan by 192.168.0.0 to
        // 192.168.0.255
        showLoading(true);
        startScanning();

    }

    private void showLoading (boolean b){
        if(b){
            linearLoading.setVisibility(View.VISIBLE);
            linearChooseTarget.setVisibility(View.GONE);
        }else{
            linearLoading.setVisibility(View.GONE);
            linearChooseTarget.setVisibility(View.VISIBLE);
        }
    }
    SocketScanner scn;
    private  void startScanning(){
       scn = new SocketScanner(this);
        // n is a automatic value later by java from 0 to 255
        scn.setStartingIP("192.168.0.n");
        scn.setLabel(textViewTextLoading);

        scn.start();
    }
}
