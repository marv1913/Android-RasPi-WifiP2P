package com.example.wifidirectraspi;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class DataExchangeActivity extends AppCompatActivity {

    TextView receivedMessagesTextView;
    TextView connectedPeerTextView;

    private TCPClient tcpClient;
    WifiP2pDevice p2pDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_data_exchange);
        receivedMessagesTextView = findViewById(R.id.receivedMessagesTextView);
        connectedPeerTextView = findViewById(R.id.connectedPeerTextView);

        Bundle extras = getIntent().getExtras();
        p2pDevice = (WifiP2pDevice) extras.get("P2P_DEVICE");

        connectedPeerTextView.setText(String.format("connected to peer: %s", p2pDevice.deviceName));
//        new ConnectTask().execute("");
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (tcpClient != null) {
            // disconnect
            new DisconnectTask().execute();
        }

    }

    public class SendMessageTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            // send the message
            tcpClient.sendMessage(params[0]);

            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            super.onPostExecute(nothing);
            // clear the data set
//            arrayList.clear();
//            // notify the adapter that the data set has changed.
//            mAdapter.notifyDataSetChanged();
        }
    }

    public class ConnectTask extends AsyncTask<String, String, TCPClient> {

        @Override
        protected TCPClient doInBackground(String... message) {

            //we create a TCPClient object and
            tcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            tcpClient.run();

            return null;
        }


    }
    public class DisconnectTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            // disconnect
            tcpClient.stopClient();
            tcpClient = null;
            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            super.onPostExecute(nothing);
            // clear the data set
//            arrayList.clear();
//            // notify the adapter that the data set has changed.
//            mAdapter.notifyDataSetChanged();
        }
    }

}





