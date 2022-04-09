package com.fgroupindonesia.fgiparentremote;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import bean.Entry;
import bean.Reply;
import helper.Keys;
import helper.ShowDialog;
import helper.UserData;

public class MainActivity extends AppCompatActivity {

    Button buttonConnect;
    TextView textViewMessage, textViewMute, textViewInfo;
    ImageView imageViewMessage;
    EditText editTextRemoteIP;

    public String SERVER_IP = null;
    public final int SERVER_PORT = 2004;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        needHide();

        // for later usage
        UserData.setPreference(this);

        buttonConnect = (Button) findViewById(R.id.buttonConnect);
        tableLayout = (TableLayout) findViewById(R.id.tableLayout);
        hideAccess(true);

        imageViewMessage = (ImageView) findViewById(R.id.imageViewMessage);

        editTextRemoteIP = (EditText) findViewById(R.id.editTextRemoteIP);

        linearLoading = (LinearLayout) findViewById(R.id.linearLoading);
        linearError = (LinearLayout) findViewById(R.id.linearError);
        linearMenu = (LinearLayout) findViewById(R.id.linearMenu);

        textViewInfo = (TextView) findViewById(R.id.textViewInfo);
        textViewMute = (TextView) findViewById(R.id.textViewMute);
        // this is the text below the picture (message menu)
        textViewMessage = (TextView) findViewById(R.id.textViewMessage);

        imageViewAudio = (ImageView) findViewById(R.id.imageViewAudio);

        showLoading();

        checkPermission();

    }

    public void startConnecting(View v) {

        if(SERVER_IP==null){
            ShowDialog.message(this, "start connecting...");
        }else{
            ShowDialog.message(this, "resuming..." + SERVER_IP);
        }

        if (editTextRemoteIP.getText().toString().length() > 0) {
            SERVER_IP = editTextRemoteIP.getText().toString();
            // save locally
            UserData.savePreference(Keys.IP_ADDRESS, SERVER_IP);

            new Thread(new ThreadConnector()).start();
        } else {
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
        ShowDialog.message(this, "FGI Parent Control v1.0 -Android-");
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
            } else {
                String x = createEntryAsJSONString("unmute_audio", null);
                new Thread(new ThreadSender(x)).start();
                v.setTag("mute");
                imageViewAudio.setImageResource(R.drawable.mute);
                textViewMute.setText("Mute Audio");
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

        if(SERVER_IP!=null){
             editTextRemoteIP.setText(SERVER_IP);
        }else{
            editTextRemoteIP.setVisibility(View.VISIBLE);
        }

        if (socket == null) {
            startConnecting(null);
        } else if (socket != null) {
            if (socket.isClosed())
                startConnecting(null);
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

    private void sendMessage(View vLinear, String pesan) {
        String x = createEntryAsJSONString("message_add", pesan);
        vLinear.setTag("delete");
        imageViewMessage.setImageResource(R.drawable.message_delete);
        textViewMessage.setText(R.string.label_menu_clear_message);
        new Thread(new ThreadSender(x)).start();
    }

    private void deleteMessage(View v) {
        String x = createEntryAsJSONString("message_delete", null);
        v.setTag("add");
        imageViewMessage.setImageResource(R.drawable.message_add);
        textViewMessage.setText(R.string.label_menu_message);
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
                socket = new Socket(SERVER_IP, SERVER_PORT);
                output = new PrintWriter(socket.getOutputStream(), true);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textViewInfo.setText("Connecting...");
                        buttonConnect.setVisibility(View.INVISIBLE);
                        editTextRemoteIP.setVisibility(View.INVISIBLE);
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

    String msgReceived;
    boolean access = false;

    class ThreadReceiver implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    msgReceived = input.readLine();
                    final Reply dataCome = new Gson().fromJson(msgReceived, Reply.class);
                    if (msgReceived != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (dataCome.getStatus().equalsIgnoreCase("success") ||
                                        dataCome.getStatus().equalsIgnoreCase("okay")) {
                                    showMessage("server: " + msgReceived);

                                } else {

                                    //ShowDialog.message(MainActivity.this, dataCome.getData());
                                    extractAppNameData(dataCome);
                                }

                                hideAccess(false);
                                access = true;

                            }
                        });
                    } else {
                        break;
                        //new Thread(new ThreadConnector()).start();
                        //return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // session closed
                    buttonConnect.setVisibility(View.VISIBLE);
                    editTextRemoteIP.setVisibility(View.VISIBLE);
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

    private void showMenu() {
        linearError.setVisibility(View.GONE);
        linearMenu.setVisibility(View.VISIBLE);
        linearLoading.setVisibility(View.GONE);
    }


    private void showError() {
        linearError.setVisibility(View.VISIBLE);
        linearMenu.setVisibility(View.GONE);
        linearLoading.setVisibility(View.GONE);
    }

    private void showLoading() {
        linearError.setVisibility(View.GONE);
        linearMenu.setVisibility(View.GONE);
        linearLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 111:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showMenu();
                    ShowDialog.message(this, "permission is okay!");
                } else {
                    showError();
                }
                break;

        }
    }

    private void checkPermission() {
        int MyVersion = Build.VERSION.SDK_INT;
        //  if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
        //if (!checkIfAlreadyhavePermission()) {
        requestForSpecificPermission();
        //}else{

        //}
        //  }
    }


    String data[] = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE};

    final int REQUEST_STATE = 111;

    private void requestForSpecificPermission() {
        /*String data [] = new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        */
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.INTERNET);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.INTERNET)) {
                ShowDialog.message(this, "Permission Needed");
            } else {
                requestPermission(Manifest.permission.INTERNET, REQUEST_STATE);
            }
        } else {
            showMenu();
        }
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
    }

}