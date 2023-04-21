package helper;

import android.os.Handler;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.util.ArrayList;

public class SocketScanner {
    ArrayList<String> devices;


    private void addDevice(String anIP) {
        if (!devices.contains(anIP)) {
            devices.add(anIP);
            if (devices.size() == 1) {
                txtObject.setText("Found ..." + devices.size() + " device only!");
            } else if (devices.size() > 1) {
                txtObject.setText("Found ..." + devices.size() + " devices!");
            }

        }
    }

    public ArrayList<String> getDeviceFound() {
        return devices;
    }

    TextView txtObject;

    public void setLabel(TextView txt) {
        txtObject = txt;
    }

    String startingIP;

    public void setStartingIP(String ip) {
        startingIP = ip;
    }

    private StringBuffer createIP(int numIndex) {
        StringBuffer stb = new StringBuffer();

        if (numIndex != -1) {

            // split by dot escaped
            String partIP[] = startingIP.split("\\.");

            // making of 192.168.0.x
            if (partIP != null) {
                stb.append(partIP[0]);
                stb.append(".");
                stb.append(partIP[1]);
                stb.append(".");
                stb.append(partIP[2]);
                stb.append(".");
                stb.append(numIndex);
            } else {
                stb.append(startingIP);
            }
        }

        return stb;
    }

    final int OPENING_PORT = 2004;

    public void start() {

        devices = new ArrayList<String>();

        int numA = 0, numZ = 255;

        StringBuffer stb = new StringBuffer();
        for (; numA <= numZ; numA++) {
            stb = createIP(numA);
            //stb.append(startingIP);
            execute(stb.toString(), OPENING_PORT);
        }

    }

    AppCompatActivity activity;
    public SocketScanner(AppCompatActivity act){
        activity = act;
    }

    boolean workDone = false;
    Handler handler = new Handler();

    private void execute(String anIP, int port) {

        final String innerIP = anIP;
        final int innerPort = port;

        Runnable r = new Runnable() {

            public void run() {

                if (available(innerIP, innerPort)) {
                    addDevice(innerIP);
                }
            }
        };

        TIME_OUT_WAIT += 1000;
        handler.postDelayed(r, TIME_OUT_WAIT / 2);

    }

     int TIME_OUT_WAIT = 3000;

    private boolean available(String anIP, int port) {
        boolean work = true;
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(anIP, port), TIME_OUT_WAIT);
            txtObject.setText("Success at " + anIP);
            ShowDialog.message(activity, "success " + anIP);
        } catch (Exception e) {
            return false; // Either timeout or unreachable or failed DNS lookup.
        }

        return work;
    }
}
