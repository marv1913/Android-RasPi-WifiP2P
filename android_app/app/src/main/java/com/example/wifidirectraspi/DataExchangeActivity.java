package com.example.wifidirectraspi;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class DataExchangeActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView receivedMessagesTextView;
    private TextView connectedPeerTextView;
    private Button connectSocketButton;

    private TCPClient tcpClient;
    private boolean connectedToSocket;
    WifiP2pDevice p2pDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_data_exchange);


        Bundle extras = getIntent().getExtras();
        p2pDevice = (WifiP2pDevice) extras.get("P2P_DEVICE");


        receivedMessagesTextView = findViewById(R.id.receivedMessagesTextView);
        connectedPeerTextView = findViewById(R.id.connectedPeerTextView);
        connectSocketButton = findViewById(R.id.connectSocketButton);

        connectedPeerTextView.setText(String.format("connected to peer: %s", p2pDevice.deviceName));

        connectSocketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectSocketButton.setText("Connecting");
                connectSocketButton.setEnabled(false);
                new ConnectTask().execute("");
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (tcpClient != null) {
            // disconnect
            new DisconnectTask().execute();
        }

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case (R.id.connectSocketButton):
                if(!tcpClient.isConnected()){
                    connectSocketButton.setText("Connecting");
                    connectSocketButton.setEnabled(false);
                    new ConnectTask().execute("");
                }else {
                    // disconnect
                }

                break;
//            case R.id.button2:
//                //DO something
//                break;
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
        boolean connectionFailed;

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
            }, new TCPClient.OnConnectionFailed() {
                @Override
                public void connectionFailed(String message) {
                    Log.d("MY_DEBUG", "on connection failed called");
                    connectionFailed = true;
                }
            }, new TCPClient.OnConnectionEstablished() {
                @Override
                public void onConnected() {
                    publishProgress();
                }
            });
            tcpClient.run();
            return null;
        }
        @Override
        protected void onPostExecute(TCPClient tcpClient) {
            if(connectionFailed){
                Log.d("MY_DEBUG", "connection failed");

                connectSocketButton.setText("Failed");

            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            connectSocketButton.setText("Disconnect");
            connectSocketButton.setEnabled(true);
            connectedToSocket = true;

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





