package com.fgroupindonesia.fgipc.parentremote;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.maps.GoogleMap;

import helper.GpsTracker;

public class TrackerMapActivity extends AppCompatActivity {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView wb = new WebView(this);
        //setContentView(R.layout.activity_tracker_map);


        wb.getSettings().setJavaScriptEnabled(true);
        wb.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        //wb.getSettings().setGeolocationEnabled(true);//
        wb.getSettings().setAllowFileAccess(true);
        wb.getSettings().setDomStorageEnabled(true);//
       // wb.getSettings().setDatabaseEnabled(true);//
        wb.getSettings().setPluginState(WebSettings.PluginState.ON);
        wb.getSettings().setBuiltInZoomControls(false);
        wb.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        wb.getSettings().setBlockNetworkImage(false);
        wb.getSettings().setBlockNetworkLoads(false);

        wb.loadUrl("file:///android_res/raw/sample.html");

        setContentView(wb);

        wb.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String loc) {
               getCurrentLocation(view);

            }
        });

    }

    private GpsTracker gpsTracker;
    public void  getCurrentLocation(WebView wv){

        gpsTracker = new GpsTracker(this);
        if(gpsTracker.canGetLocation()){
            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();
            x = latitude;
            y = longitude;

            wv.loadUrl("javascript:getFromAndroid("+x+","+y+")");
           // tvLatitude.setText(String.valueOf(latitude));
            //tvLongitude.setText(String.valueOf(longitude));
        }else{
            gpsTracker.showSettingsAlert();
        }

    }

    double x = 999;
    double y = 212;
}