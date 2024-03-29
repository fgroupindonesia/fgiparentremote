package com.fgroupindonesia.fgipc.parentremote;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import bean.Entry;
import bean.TargetProfile;
import helper.Keys;
import helper.Navigator;
import helper.RespondHelper;
import helper.ShowDialog;
import helper.URLReference;
import helper.UserData;
import helper.WebRequest;
import helper.recyclerworks.ClickListener;
import helper.recyclerworks.ClientAdapter;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity implements Navigator {


    TextView textViewMessage, textViewMute, textViewInfo,
            textviewProfileLink;
    ImageView imageViewMessage;
    EditText editTextRemoteIP;
    SeekBar seekBarVolume;
    EditText edtMessage;
    Switch switchMode;
    TargetProfile singleTargetProfile;

    List<TargetProfile>listTargetProfile;
    RecyclerView recyclerViewClient;
    ClientAdapter targetProfileClientAdapter;
    LinearLayoutManager linearLayoutManager;

    int currentVol = 0;
    boolean access = false;
    public String SERVER_IP = null;
    public final int SERVER_PORT = 2004;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    String dataMessage = null;
    String temp;
    private String STORED_PROFILE;

    LinearLayout linearAudio, linearMessage, linearLoading, linearError, linearMenu;
    TableLayout tableLayout;
    ImageView imageViewAudio;
    TargetProfile tpList[];
    ListView lstAppName;
    LinearLayout linearButtonKillApp;
    ArrayList<String> listAppNames = new ArrayList<String>();
    Dialog dialog;

    ProgressBar progressBarLoadingApp;
    ClickListener clickTargetProfileListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // centerTitle();
        // needHide();

        // for later usage
        UserData.setPreference(this);

        // for storing profile later
        loadStoredTargetProfile();

        switchMode = (Switch) findViewById(R.id.switchMode);
        textviewProfileLink = (TextView) findViewById(R.id.textviewProfileLink);

        if(UserData.getPreferenceBoolean(Keys.FORM_FILLED)){
            textviewProfileLink.setText(UserData.getPreferenceString(Keys.FULLNAME));
            myUUID = UserData.getPreferenceString(Keys.DEVICE_UUID);
        }

        //buttonConnect = (Button) findViewById(R.id.buttonConnect);
        tableLayout = (TableLayout) findViewById(R.id.tableLayout);

        imageViewMessage = (ImageView) findViewById(R.id.imageViewMessage);

        //editTextRemoteIP = (EditText) findViewById(R.id.editTextRemoteIP);

        linearLoading = (LinearLayout) findViewById(R.id.linearLoading);
        linearError = (LinearLayout) findViewById(R.id.linearError);
        linearMenu = (LinearLayout) findViewById(R.id.linearMenu);
        linearAudio = (LinearLayout) findViewById(R.id.linearAudio);
        linearMessage = (LinearLayout) findViewById(R.id.linearMessage);

        // textViewInfo = (TextView) findViewById(R.id.textViewInfo);
        textViewMute = (TextView) findViewById(R.id.textViewMute);
        // this is the text below the picture (message menu)
        textViewMessage = (TextView) findViewById(R.id.textViewMessage);

        imageViewAudio = (ImageView) findViewById(R.id.imageViewAudio);

        seekBarVolume = (SeekBar) findViewById(R.id.seekBarVolume);
        seekBarVolume.setProgress(0);
        seekBarVolume.setEnabled(false);

        recyclerViewClient = (RecyclerView)findViewById(R.id.recyclerViewClient);
        clickTargetProfileListener = new ClickListener() {
            @Override
            public void click(int index) {
                // do something when clicked
            }
        };

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        targetProfileClientAdapter = new ClientAdapter(getAllTargetProfile(), getApplication(), clickTargetProfileListener );

        recyclerViewClient.setLayoutManager(linearLayoutManager);
        recyclerViewClient.setAdapter(targetProfileClientAdapter);



        // set the onchangelistener
        applyOnChangedListener(seekBarVolume);

        // keep it awake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        hideAccess(true);
        // showLayout(MODE_LAYOUT_LOADING);
        // showLoading();


        checkAndRequestPermissions();
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.contact_admin:
                chatWAAdminForHelp();
                break;
            case R.id.logout:
                loggingOut();
                break;
            case R.id.clear_all_target_profile:
                deleteStoredTargetProfile();
                break;

            default:
        }

        return true;
    }

    private void loggingOut(){
        // clear everything
        UserData.destroyAll();
        finishAffinity();
    }

    private void showAddMessageLayout() {
        linearMessage.setTag("add");
        imageViewMessage.setImageResource(R.drawable.message_add);
        textViewMessage.setText(R.string.label_menu_message);
    }

    private void deleteMessage(View v) {
        String x = createEntryAsJSONString("message_delete", null);
        showAddMessageLayout();
        //new Thread(new ThreadSender(x)).start();
    }

    private void sendMessage(View vLinear, String pesan) {
        String x = createEntryAsJSONString("message_add", pesan);
        showDeleteMessageLayout();
        //new Thread(new ThreadSender(x)).start();
    }

    public void showMessageInput(final View linearLayoutView) {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_message);

        Button btnCancel = dialog.findViewById(R.id.dialog_cancel);
        Button btnOk = dialog.findViewById(R.id.dialog_ok);
        edtMessage = dialog.findViewById(R.id.editTextTextPersonName);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataMessage = edtMessage.getText().toString();
                sendMessage(linearLayoutView, dataMessage);
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setTitle(R.string.label_menu_message);
        dialog.show();
    }

    public void restartPc(View v) {
        if (access) {
            String x = createEntryAsJSONString("restart_pc", null);
            //new Thread(new ThreadSender(x)).start();
            hideAccess(true);
        } else {
            ShowDialog.message(this, "connect first!");
        }

    }

    public void killApp(String filename) {
        if (access) {
            String x = createEntryAsJSONString("kill_app", filename);
            // new Thread(new ThreadSender(x)).start();
            //hideAccess(true);
        } else {
            ShowDialog.message(this, "connect first!");
        }

    }

    public void showListingApp() {
        listAppNames.clear();

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.choose_app);

        Button btnCancel = dialog.findViewById(R.id.buttonCancel);
        Button btnOk = dialog.findViewById(R.id.buttonKill);
        lstAppName = dialog.findViewById(R.id.listViewAppName);
        lstAppName.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        linearButtonKillApp = (LinearLayout) dialog.findViewById(R.id.linearButtonKillApp);

        progressBarLoadingApp = dialog.findViewById(R.id.progressBarLoadingApp);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listAppNames);
        lstAppName.setAdapter(adapter);

        lstAppName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                temp = lstAppName.getItemAtPosition(position).toString();
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowDialog.message(MainActivity.this, "app target killed was " + temp);
                killApp(temp);
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setTitle(R.string.label_menu_message);
        dialog.show();
    }

    public void listApp(View v) {
        if (access) {
            String x = createEntryAsJSONString("list_app", null);
            // new Thread(new ThreadSender(x)).start();
            hideAccess(true);
            showListingApp();
        } else {
            ShowDialog.message(this, "connect first!");
        }
    }

    public void shutdownPc(View v) {
        if (access) {
            String x = createEntryAsJSONString("shutdown_pc", null);
            //new Thread(new ThreadSender(x)).start();
            hideAccess(true);
        } else {
            ShowDialog.message(this, "connect first!");
        }

    }

    public void muteAudio(View v) {
        if (access) {

            if (v.getTag().toString().equalsIgnoreCase("mute")) {
                String x = createEntryAsJSONString("mute_audio", null);
                // new Thread(new ThreadSender(x)).start();
                v.setTag("unmute");
                imageViewAudio.setImageResource(R.drawable.unmute);
                textViewMute.setText("Unmute Audio");

                seekBarVolume.setProgress(0);
            } else {
                String x = createEntryAsJSONString("unmute_audio", null);
                //new Thread(new ThreadSender(x)).start();
                v.setTag("mute");
                imageViewAudio.setImageResource(R.drawable.mute);
                textViewMute.setText("Mute Audio");

                seekBarVolume.setProgress(100);
            }

        } else {
            ShowDialog.message(this, "connect first!");
        }

    }

    private void deleteStoredTargetProfile(){
        UserData.savePreference(Keys.TARGET_PROFILE, null);
        STORED_PROFILE = null;
        tpList = null;
        recyclerViewClient.setAdapter(null);
        targetProfileClientAdapter.clearItems();

        if(!listTargetProfile.isEmpty()){
            listTargetProfile = Collections.emptyList();
        }
    }
    
    private void loadStoredTargetProfile() {
        STORED_PROFILE = UserData.getPreferenceString(Keys.TARGET_PROFILE);

        if(STORED_PROFILE!=null)
        tpList = new Gson().fromJson(STORED_PROFILE, TargetProfile[].class);

        ShowDialog.shortMessage(this, "I dunno " + STORED_PROFILE);

    }

    private List<TargetProfile> getAllTargetProfile(){
        if(listTargetProfile==null)
       listTargetProfile = Collections.emptyList();

        if(tpList!=null){
            listTargetProfile = Arrays.asList(tpList);
        }

        return listTargetProfile;
    }

    private void hideAccess(boolean b) {
        if (b) {
            tableLayout.setAlpha(0.4f);


        } else {
            tableLayout.setAlpha(1f);


        }
    }

    private String createEntryAsJSONString(String command, String data) {
        Entry en = new Entry();
        en.setCommand(command);
        en.setData(data);
        String jsonInString = new Gson().toJson(en);
        return jsonInString;
    }


    final int MODE_LAYOUT_ERROR = 1, MODE_LAYOUT_LOADING = 0, MODE_LAYOUT_MENU = 2;

    private boolean checkAndRequestPermissions() {
        int permissionInternet = ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET);
        int permissionNetwork = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_NETWORK_STATE);
        int locationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int locationPermission2 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (permissionNetwork != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_NETWORK_STATE);
        }
        if (permissionInternet != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.INTERNET);
        }
        if (locationPermission2 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }


        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    private void showDeleteMessageLayout() {
        linearMessage.setTag("delete");
        imageViewMessage.setImageResource(R.drawable.message_delete);
        textViewMessage.setText(R.string.label_menu_clear_message);
    }

    private void applyOnChangedListener(SeekBar skb) {
        skb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentVol = progress;
                textViewInfo.setText("Volume : " + currentVol);

                if (currentVol == 0) {
                    linearAudio.setTag("unmute");
                    imageViewAudio.setImageResource(R.drawable.unmute);
                    textViewMute.setText("Unmute Audio");
                } else if (currentVol >= 10) {
                    linearAudio.setTag("mute");
                    imageViewAudio.setImageResource(R.drawable.mute);
                    textViewMute.setText("Mute Audio");
                }

                String x = createEntryAsJSONString("sound_mix", String.valueOf(currentVol));
                //new Thread(new ThreadSender(x)).start();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void aboutApp(View v) {
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            ShowDialog.message(this, "FGI Parent Control Remote v" + versionName + " -Android-");
        } catch (Exception ex) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        // if the intentResult is null then
        // toast a message as "cancelled"
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                ShowDialog.message(this, "Cancel");
            } else {
                String dataHasilScan = intentResult.getContents();

                String uuidClient = dataHasilScan.split(";")[0];
                String ipClient = dataHasilScan.split(";")[1];
                String typeNa = dataHasilScan.split(";")[2];

                singleTargetProfile = new TargetProfile("", ipClient, uuidClient, typeNa);

                ShowDialog.message(this,  dataHasilScan);

                registerClientFound();


            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    boolean modeGlobal = false;

    public void switchMode(View v) {
        if (switchMode.isChecked()) {
            switchMode.setText("Mode : Global");
            modeGlobal = true;
        } else {
            switchMode.setText("Mode : Local");
            modeGlobal = false;
        }

        workAppropriately();
    }

    private void workAppropriately() {
        if (modeGlobal) {
            // we post to REST API
            initializeDevice();

        } else {
            // we try to connect by Socket
        }
    }

    private void initializeDevice() {
        WebRequest httpCall = new WebRequest(MainActivity.this, MainActivity.this);

        httpCall.addData("uuid", myUUID);

        // we need to wait for the response
        httpCall.setWaitState(true);
        httpCall.setRequestMethod(WebRequest.POST_METHOD);
        httpCall.setTargetURL(URLReference.RemoteInitialize);
        httpCall.execute();
    }

    private void registerClientFound(){
        WebRequest httpCall = new WebRequest(MainActivity.this, MainActivity.this);

        httpCall.addData("uuid_client", singleTargetProfile.getUuid());
        httpCall.addData("uuid_remote", myUUID);
        httpCall.addData("ip_address", singleTargetProfile.getIp_address());

        // we need to wait for the response
        httpCall.setWaitState(true);
        httpCall.setRequestMethod(WebRequest.POST_METHOD);
        httpCall.setTargetURL(URLReference.RemoteRegisterCilent);
        httpCall.execute();
    }

    String myUUID;

    private void chatWAAdminForHelp(){
        String apiLink = URLReference.RemoteAskHelp + "?language=id&uuid=" + myUUID;
        Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(apiLink));
        startActivity(intent);
    }

    public void chatWAAdmin(View v) {

        // change this http://192.168.0.2 to
        // http://api.fgic.fgroupindonesia.com later when ALIVE
        String apiLink = URLReference.RemotePurchaseLicense + "?language=id&uuid=" + myUUID;
        Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(apiLink));
        startActivity(intent);
    }

    public void scanQRCode(View v) {
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setPrompt("Scan a barcode or QR Code");
        intentIntegrator.setOrientationLocked(true);
        intentIntegrator.setBeepEnabled(true);
        intentIntegrator.setCaptureActivity(CaptureActivityPortrait.class);

        intentIntegrator.initiateScan();
    }

    public void showLocation(View v) {

    }

    //MenuItem menuDisconnect;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
        MenuCompat.setGroupDividerEnabled(menu, true);

        // menuDisconnect = menu.findItem(R.id.disconnect);
        //menuDisconnect.setVisible(false);
        //menuSaveProfile = menu.findItem(R.id.save_profile);
        //menuSaveProfile.setVisible(false);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        // when it is connected
        if (SERVER_IP != null) {
            // menuDisconnect.setVisible(true);
            //menuSaveProfile.setVisible(true);
        } else {
            //menuDisconnect.setVisible(false);
            //menuSaveProfile.setVisible(false);
        }
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    public void message(View v) {
        if (access) {
            String x = null;
            if (v.getTag().toString().contains("add")) {
                showMessageInput(v);

            } else {
                deleteMessage(v);
            }


            //hideAccess(true);
        } else {
            ShowDialog.message(this, "connect first!");
        }

    }

    private TargetProfile[] createCopy(TargetProfile[] tpr, TargetProfile tp1) {
        TargetProfile[] tpNew = new TargetProfile[tpr.length + 1];
        int x = 0;

        for (TargetProfile tpSingle : tpr) {
            tpNew[x] = tpSingle;
            x++;
        }

        // last position
        tpNew[x] = tp1;
        return tpNew;
    }

    private void addStoredProfile(TargetProfile tp) {
        Gson n = new Gson();

        // save as JSON temporarily

        if (STORED_PROFILE == null) {
            // creating array
            tpList = new TargetProfile[1];
            tpList[0] = tp;
        } else {
            // if there is a data
            // we grab as array

            // create a new one with additional data
            tpList = createCopy(tpList, tp);

        }

        if (tpList != null) {
            STORED_PROFILE = n.toJson(tpList);
            UserData.savePreference(Keys.TARGET_PROFILE, STORED_PROFILE);
        }

        ShowDialog.message(this, "Profile saved completely! now \n"+STORED_PROFILE);
    }

    private void renderClientRecycler(){

        getAllTargetProfile();
        targetProfileClientAdapter.setNewItems(listTargetProfile);

        if(recyclerViewClient.getAdapter()==null)
        recyclerViewClient.setAdapter(targetProfileClientAdapter);
    }


    @Override
    public void nextActivity() {

    }

    @Override
    public void onSuccess(String urlTarget, String respond) {

        try {
            // ShowDialog.message(this, "dapt " + respond);

            Gson objectG = new Gson();

            if (RespondHelper.isValidRespond(respond)) {

                if (urlTarget.contains(URLReference.RemoteInitialize)) {
                    String yourname = UserData.getPreferenceString(Keys.FULLNAME);
                    textviewProfileLink.setText(yourname);
                }else if(urlTarget.contains(URLReference.RemoteRegisterCilent)){
                    // add data locally on array
                    addStoredProfile(singleTargetProfile);
                    // then call back the recyclerview
                    renderClientRecycler();
                }

            }

            if(urlTarget.contains(URLReference.RemoteRegisterCilent)) {
                ShowDialog.message(this, "ada  " + respond);
            }


        } catch (Exception ex) {
            ShowDialog.message(this, "success error " + ex.getMessage());
        }

    }
}


/*
import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import bean.Entry;
import bean.Reply;
import bean.TargetProfile;
import helper.Keys;
import helper.ShowDialog;
import helper.UserData;
import helper.WhatsappSender;

public class MainActivity extends AppCompatActivity {

    Button buttonConnect;
    TextView textViewMessage, textViewMute, textViewInfo;
    ImageView imageViewMessage;
    EditText editTextRemoteIP;
    SeekBar seekBarVolume;

    int currentVol = 0;

    public String SERVER_IP = null;
    public final int SERVER_PORT = 2004;

    LinearLayout linearAudio, linearMessage;

    TableLayout tableLayout;

    ImageView imageViewAudio;

    private void hideAccess(boolean b) {
        if (b) {
            tableLayout.setAlpha(0.4f);
            buttonConnect.setVisibility(View.VISIBLE);

        } else {
            tableLayout.setAlpha(1f);
            buttonConnect.setVisibility(View.INVISIBLE);

        }
    }

    private void needHide() {
        try {
            this.getSupportActionBar().hide();
        } catch (NullPointerException e) {
        }
    }

    private void loadStoredTargetProfile() {
        STORED_PROFILE = UserData.getPreferenceString(Keys.TARGET_PROFILE);
        tpList = new Gson().fromJson(STORED_PROFILE, TargetProfile[].class);
    }

    private void applyOnChangedListener(SeekBar skb) {
        skb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentVol = progress;
                textViewInfo.setText("Volume : " + currentVol);

                if (currentVol == 0) {
                    linearAudio.setTag("unmute");
                    imageViewAudio.setImageResource(R.drawable.unmute);
                    textViewMute.setText("Unmute Audio");
                } else if (currentVol >= 10) {
                    linearAudio.setTag("mute");
                    imageViewAudio.setImageResource(R.drawable.mute);
                    textViewMute.setText("Mute Audio");
                }

                String x = createEntryAsJSONString("sound_mix", String.valueOf(currentVol));
                new Thread(new ThreadSender(x)).start();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // centerTitle();
        //needHide();


        // for later usage
        UserData.setPreference(this);

        // for storing profile later
        loadStoredTargetProfile();

        buttonConnect = (Button) findViewById(R.id.buttonConnect);
        tableLayout = (TableLayout) findViewById(R.id.tableLayout);

        imageViewMessage = (ImageView) findViewById(R.id.imageViewMessage);

        editTextRemoteIP = (EditText) findViewById(R.id.editTextRemoteIP);

        linearLoading = (LinearLayout) findViewById(R.id.linearLoading);
        linearError = (LinearLayout) findViewById(R.id.linearError);
        linearMenu = (LinearLayout) findViewById(R.id.linearMenu);
        linearAudio = (LinearLayout) findViewById(R.id.linearAudio);
        linearMessage = (LinearLayout) findViewById(R.id.linearMessage);

        textViewInfo = (TextView) findViewById(R.id.textViewInfo);
        textViewMute = (TextView) findViewById(R.id.textViewMute);
        // this is the text below the picture (message menu)
        textViewMessage = (TextView) findViewById(R.id.textViewMessage);

        imageViewAudio = (ImageView) findViewById(R.id.imageViewAudio);

        seekBarVolume = (SeekBar) findViewById(R.id.seekBarVolume);
        seekBarVolume.setProgress(0);
        seekBarVolume.setEnabled(false);

        // set the onchangelistener
        applyOnChangedListener(seekBarVolume);

        // keep it awake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        hideAccess(true);
       // showLayout(MODE_LAYOUT_LOADING);
        // showLoading();

        //UserData.savePreference(Keys.TARGET_PROFILE, null);
        checkAndRequestPermissions();
    }

    public void startConnecting(View v) {

        hideAccess(true);
       // showLayout(MODE_LAYOUT_LOADING);

        if (SERVER_IP != null) {

           // ShowDialog.message(this, "resuming..." + SERVER_IP);
            new Thread(new ThreadConnector()).start();
        } else if (editTextRemoteIP.getText().toString().length() > 0) {
            showLayout(MODE_LAYOUT_MENU);

            SERVER_IP = editTextRemoteIP.getText().toString();
            // save locally
            UserData.savePreference(Keys.IP_ADDRESS, SERVER_IP);

            new Thread(new ThreadConnector()).start();
        } else {

            showLayout(MODE_LAYOUT_MENU);
            ShowDialog.message(this, "Please input the valid IP Address first for connecting remote.");
        }

    }


    private String createEntryAsJSONString(String command, String data) {
        Entry en = new Entry();
        en.setCommand(command);
        en.setData(data);
        String jsonInString = new Gson().toJson(en);
        return jsonInString;
    }

    public void shutdownPc(View v) {
        if (access) {
            String x = createEntryAsJSONString("shutdown_pc", null);
            new Thread(new ThreadSender(x)).start();
            hideAccess(true);
        } else {
            ShowDialog.message(this, "connect first!");
        }

    }

    public void listApp(View v) {
        if (access) {
            String x = createEntryAsJSONString("list_app", null);
            new Thread(new ThreadSender(x)).start();
            hideAccess(true);
            showListingApp();
        } else {
            ShowDialog.message(this, "connect first!");
        }
    }

    public void aboutApp(View v) {
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            ShowDialog.message(this, "FGI Parent Control Remote v" + versionName + " -Android-");
        } catch (Exception ex){

        }
    }

    public void killApp(String filename) {
        if (access) {
            String x = createEntryAsJSONString("kill_app", filename);
            new Thread(new ThreadSender(x)).start();
            //hideAccess(true);
        } else {
            ShowDialog.message(this, "connect first!");
        }

    }

    public void muteAudio(View v) {
        if (access) {

            if (v.getTag().toString().equalsIgnoreCase("mute")) {
                String x = createEntryAsJSONString("mute_audio", null);
                new Thread(new ThreadSender(x)).start();
                v.setTag("unmute");
                imageViewAudio.setImageResource(R.drawable.unmute);
                textViewMute.setText("Unmute Audio");

                seekBarVolume.setProgress(0);
            } else {
                String x = createEntryAsJSONString("unmute_audio", null);
                new Thread(new ThreadSender(x)).start();
                v.setTag("mute");
                imageViewAudio.setImageResource(R.drawable.mute);
                textViewMute.setText("Mute Audio");

                seekBarVolume.setProgress(100);
            }

        } else {
            ShowDialog.message(this, "connect first!");
        }

    }


    String dataMessage = null;
    EditText edtMessage;
    Dialog dialog;

    public void showMessageInput(final View linearLayoutView) {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_message);

        Button btnCancel = dialog.findViewById(R.id.dialog_cancel);
        Button btnOk = dialog.findViewById(R.id.dialog_ok);
        edtMessage = dialog.findViewById(R.id.editTextTextPersonName);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataMessage = edtMessage.getText().toString();
                sendMessage(linearLayoutView, dataMessage);
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setTitle(R.string.label_menu_message);
        dialog.show();
    }

    @Override
    public void onResume() {
        // this will reconnecting back
        SERVER_IP = UserData.getPreferenceString(Keys.IP_ADDRESS);

        if (SERVER_IP != null) {
            editTextRemoteIP.setText(SERVER_IP);
            startConnecting(null);

        } else {
            editTextRemoteIP.setText("");
            editTextRemoteIP.setVisibility(View.VISIBLE);
        }

        super.onResume();
    }

    @Override
    public void onStop() {
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception e) {
                ShowDialog.message(this, "Error when closing socket!");
            }
        }

        super.onStop();
    }

    ListView lstAppName;
    LinearLayout linearButtonKillApp;
    ArrayList<String> listAppNames = new ArrayList<String>();
    ArrayAdapter<String> adapter;

    String temp;

    ProgressBar progressBarLoadingApp;

    public void showListingApp() {
        listAppNames.clear();

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.choose_app);

        Button btnCancel = dialog.findViewById(R.id.buttonCancel);
        Button btnOk = dialog.findViewById(R.id.buttonKill);
        lstAppName = dialog.findViewById(R.id.listViewAppName);
        lstAppName.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        linearButtonKillApp = (LinearLayout) dialog.findViewById(R.id.linearButtonKillApp);

        progressBarLoadingApp = dialog.findViewById(R.id.progressBarLoadingApp);


        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listAppNames);
        lstAppName.setAdapter(adapter);

        lstAppName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                temp = lstAppName.getItemAtPosition(position).toString();
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowDialog.message(MainActivity.this, "app target killed was " + temp);
                killApp(temp);
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setTitle(R.string.label_menu_message);
        dialog.show();
    }

    private void extractAppNameData(Reply dataIn) {
        String overAll[] = dataIn.getData().split(";");
        for (String n : overAll) {
            adapter.add(n);
        }

        progressBarLoadingApp.setVisibility(View.INVISIBLE);
    }

    private void showDeleteMessageLayout() {
        linearMessage.setTag("delete");
        imageViewMessage.setImageResource(R.drawable.message_delete);
        textViewMessage.setText(R.string.label_menu_clear_message);
    }

    private void showAddMessageLayout() {
        linearMessage.setTag("add");
        imageViewMessage.setImageResource(R.drawable.message_add);
        textViewMessage.setText(R.string.label_menu_message);
    }

    private void sendMessage(View vLinear, String pesan) {
        String x = createEntryAsJSONString("message_add", pesan);
        showDeleteMessageLayout();
        new Thread(new ThreadSender(x)).start();
    }

    private void deleteMessage(View v) {
        String x = createEntryAsJSONString("message_delete", null);
        showAddMessageLayout();
        new Thread(new ThreadSender(x)).start();
    }

    public void message(View v) {
        if (access) {
            String x = null;
            if (v.getTag().toString().contains("add")) {
                showMessageInput(v);

            } else {
                deleteMessage(v);
            }


            //hideAccess(true);
        } else {
            ShowDialog.message(this, "connect first!");
        }

    }

    private void showMessage(String msg) {
        textViewInfo.setText(msg);
    }

    public void restartPc(View v) {
        if (access) {
            String x = createEntryAsJSONString("restart_pc", null);
            new Thread(new ThreadSender(x)).start();
            hideAccess(true);
        } else {
            ShowDialog.message(this, "connect first!");
        }

    }

    private PrintWriter output;
    private BufferedReader input;
    Socket socket;

    class ThreadConnector implements Runnable {
        public void run() {

            try {
                if(socket!=null) {
                    input.close();
                    output.close();
                    socket.close();
                }

                socket = new Socket(SERVER_IP, SERVER_PORT);
                output = new PrintWriter(socket.getOutputStream(), true);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showMessage("Connecting...");
                        buttonConnect.setVisibility(View.INVISIBLE);
                        editTextRemoteIP.setVisibility(View.INVISIBLE);

                        if (menuDisconnect != null) {
                            menuDisconnect.setVisible(true);
                            menuSaveProfile.setVisible(true);
                        }
                    }
                });

                new Thread(new ThreadReceiver()).start();
            } catch (Exception e) {
                e.printStackTrace();
                buttonConnect.setVisibility(View.VISIBLE);
                editTextRemoteIP.setVisibility(View.VISIBLE);
                ShowDialog.message(MainActivity.this, "Error at 368 " + e.getMessage());
            }
        }
    }

    class ThreadDisconnector implements Runnable {
        public void run() {
            try {
                if (socket != null)
                    socket.close();
            } catch (Exception e) {

            }
        }
    }

    String msgReceived;
    boolean access = false;
    Reply dataCome;
    boolean working = true;

    class ThreadReceiver implements Runnable {
        String stat;

        @Override
        public void run() {
            while (working) {
                try {
                  //  ShowDialog.message(MainActivity.this, "we tried wait for reading... ");

                    msgReceived = input.readLine();

                  //  ShowDialog.message(MainActivity.this, "we got " + msgReceived);

                    // received a reply from server
                    dataCome = new Gson().fromJson(msgReceived, Reply.class);

                    if (msgReceived != null) {

                        hideAccess(false);
                        showLayout(MODE_LAYOUT_MENU);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                stat = dataCome.getStatus();
                                if (stat.equalsIgnoreCase("success") ||
                                        stat.equalsIgnoreCase("okay")) {
                                    showMessage("server: " + msgReceived);

                                    seekBarVolume.setEnabled(true);

                                } else if (stat.equalsIgnoreCase("private")) {

                                    //ShowDialog.message(MainActivity.this, dataCome.getData());
                                    extractAppNameData(dataCome);
                                } else if (stat.equalsIgnoreCase("pending")) {
                                    // read as array for the status obtained
                                    ShowDialog.message(MainActivity.this, "we got pending one...");
                                    Entry dataList[] = new Gson().fromJson(dataCome.getData(), Entry[].class);
                                    // there is a previous task recorded here
                                    String cmd;
                                    for (Entry e : dataList) {
                                        cmd = e.getCommand();
                                        if (cmd.equalsIgnoreCase("message")) {
                                            // we toggle the message button

                                            showDeleteMessageLayout();

                                        } else if (cmd.equalsIgnoreCase("sound_mix")) {
                                            // we set the volume bar
                                            seekBarVolume.setProgress(Integer.parseInt(e.getData()));
                                        }
                                    }
                                }


                                access = true;

                                if (menuDisconnect != null) {
                                    menuDisconnect.setVisible(true);
                                }

                            }
                        });
                    } else {
                        ShowDialog.message(MainActivity.this, "escaped");
                        break;
                        //new Thread(new ThreadConnector()).start();
                        //return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ShowDialog.message(MainActivity.this, e.getMessage());
                    working = false;
                    // session closed
                   // buttonConnect.setVisibility(View.VISIBLE);
                  //  editTextRemoteIP.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    class ThreadSender implements Runnable {
        private String message;

        // message sent as Entry from Client (JSON format)
        ThreadSender(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            output.println(message);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showMessage("client: " + message);
                }
            });
        }
    }


    LinearLayout linearMenu, linearLoading, linearError;

    private void showLayout(int modeNa) {
        if (modeNa == MODE_LAYOUT_ERROR) {
            linearError.setVisibility(View.VISIBLE);
            linearMenu.setVisibility(View.GONE);
            linearLoading.setVisibility(View.GONE);
        } else if (modeNa == MODE_LAYOUT_LOADING) {
            linearError.setVisibility(View.GONE);
            linearMenu.setVisibility(View.GONE);
            linearLoading.setVisibility(View.VISIBLE);
        }else if(modeNa == MODE_LAYOUT_MENU){
            linearError.setVisibility(View.GONE);
            linearMenu.setVisibility(View.VISIBLE);
            linearLoading.setVisibility(View.GONE);
        }
    }

    final int MODE_LAYOUT_ERROR = 1, MODE_LAYOUT_LOADING = 0, MODE_LAYOUT_MENU = 2;

    private boolean checkAndRequestPermissions() {
        int permissionInternet = ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET);
        int permissionNetwork = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_NETWORK_STATE);
        int locationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int locationPermission2 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (permissionNetwork != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_NETWORK_STATE);
        }
        if (permissionInternet != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.INTERNET);
        }
        if (locationPermission2 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }


        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    MenuItem menuDisconnect;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);

        menuDisconnect = menu.findItem(R.id.disconnect);
        menuDisconnect.setVisible(false);
        menuSaveProfile = menu.findItem(R.id.save_profile);
        menuSaveProfile.setVisible(false);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        // when it is connected
        if (SERVER_IP != null) {
            menuDisconnect.setVisible(true);
            menuSaveProfile.setVisible(true);
        } else {
            menuDisconnect.setVisible(false);
            menuSaveProfile.setVisible(false);
        }
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    private String STORED_PROFILE;

    private void saveProfile(String name, String anIP) {
        Gson n = new Gson();

        // save as JSON temporarily
        TargetProfile tp = new TargetProfile(name, anIP);


        if (STORED_PROFILE == null) {
            // creating array
            tpList = new TargetProfile[1];
            tpList[0] = tp;
        } else {
            // if there is a data
            // we grab as array

            // create a new one with additional data
            tpList = createCopy(tpList, tp);

        }

        if (tpList != null) {
            STORED_PROFILE = n.toJson(tpList);
            UserData.savePreference(Keys.TARGET_PROFILE, STORED_PROFILE);
        }

        ShowDialog.message(this, STORED_PROFILE + "Profile saved completely!");
    }

    private TargetProfile[] createCopy(TargetProfile[] tpr, TargetProfile tp1) {
        TargetProfile[] tpNew = new TargetProfile[tpr.length + 1];
        int x = 0;

        for (TargetProfile tpSingle : tpr) {
            tpNew[x] = tpSingle;
            x++;
        }

        // last position
        tpNew[x] = tp1;
        return tpNew;
    }

    String profileName;
    Spinner spn;
    TargetProfile tpList[];

    private void showDialogChooseProfile() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.choose_profile);

        Button btnCancel = dialog.findViewById(R.id.dialog_cancel);
        Button btnOk = dialog.findViewById(R.id.dialog_ok);

        spn = dialog.findViewById(R.id.spinnerProfile);

        ArrayAdapter<String> adapter;
        List<String> list = new ArrayList<String>();

        Gson g = new Gson();
        tpList = g.fromJson(STORED_PROFILE, TargetProfile[].class);

        for (TargetProfile tp : tpList) {
            list.add(tp.getName());
        }

        adapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn.setAdapter(adapter);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String item = spn.getSelectedItem().toString();
                loadProfile(item);

                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setTitle("Choose Profile");
        dialog.show();

    }

    private void loadProfile(String profileName) {
        for (TargetProfile tp : tpList) {
            if (tp.getName().equalsIgnoreCase(profileName)) {
                editTextRemoteIP.setText(tp.getIp_address());
                break;
            }
        }

        ShowDialog.message(this, "You may connect now!");
    }

    private void showDialogSaveProfile() {


        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_message);

        Button btnCancel = dialog.findViewById(R.id.dialog_cancel);
        Button btnOk = dialog.findViewById(R.id.dialog_ok);

        btnOk.setText("Save");
        edtMessage = dialog.findViewById(R.id.editTextTextPersonName);
        TextView dialog_info = dialog.findViewById(R.id.dialog_info);

        dialog_info.setText("This " + SERVER_IP + " will be saved as...");

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileName = edtMessage.getText().toString();
                saveProfile(profileName, SERVER_IP);
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setTitle("Save Profile");
        dialog.show();

    }

    private boolean alreadyInTheProfile() {
        boolean already = false;

        if (SERVER_IP != null) {
            for (TargetProfile tp : tpList) {
                if (tp.getIp_address().equalsIgnoreCase(SERVER_IP)) {
                    already = true;
                    break;
                }
            }
        }

        return already;
    }

    MenuItem menuSaveProfile;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scanlocal:
                openScanner();
                break;
            case R.id.save_profile:
                // show the dialog message
                // if the ip address hasn't been stored lately
                if (!alreadyInTheProfile()) {
                    showDialogSaveProfile();
                } else {
                    ShowDialog.message(this, "This ip Address is already saved in the profile!");
                }

                break;
            case R.id.contact_admin:
                new WhatsappSender(this).sendMessageToWhatsAppContact(Keys.ADMIN_NUMBER, "*Admin*\nTolong saya mau tanya!");
                break;
            case R.id.disconnect:
                SERVER_IP = null;
                editTextRemoteIP.setVisibility(View.VISIBLE);
                buttonConnect.setVisibility(View.VISIBLE);

                // we store it
                UserData.savePreference(Keys.IP_ADDRESS, SERVER_IP);

                hideAccess(true);
                showMessage("- disconnected -");

                menuDisconnect.setVisible(false);
                new Thread(new ThreadDisconnector()).start();
                break;
            case R.id.choose_profile:
                // load the data from storage locally
                STORED_PROFILE = UserData.getPreferenceString(Keys.TARGET_PROFILE);

                if (STORED_PROFILE != null) {
                    showDialogChooseProfile();
                } else {
                    ShowDialog.message(this, "No profile available. Connect & save first!");
                }

                break;
            case R.id.maps:
                openTracker();
                break;
            default:
                break;
        }

        return true;
    }

    private void openScanner() {
        Intent i = new Intent(this, ScanLocallyActivity.class);
        startActivity(i);
        //finish();
    }

    private void openTracker() {
        Intent i = new Intent(this, TrackerMapActivity.class);
        startActivity(i);
        finish();
    }

    final int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_NETWORK_STATE
    };


    private boolean gotPermission() {
        return hasPermissions(this, PERMISSIONS);
    }

    int WAITING_TIME = 2000;
    Handler handler = new Handler();

    private void proceed() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (gotPermission()) {
                    openTracker();
                } else {
                    finish();
                }
            }
        }, WAITING_TIME);
    }

    private void requestPermission() {

        if (!gotPermission()) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            proceed();
        }


    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


}*/