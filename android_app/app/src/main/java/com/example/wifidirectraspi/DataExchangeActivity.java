package com.example.wifidirectraspi;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.ArrayList;
import java.util.List;

public class DataExchangeActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView connectedPeerTextView;
    private ListView receivedMessagesListView;
    private TextView socketInfoTextView;
    private EditText editTextMessage;
    private EditText portEditText;
    private Button connectSocketButton;
    private Button sendButton;
    private TCPClient tcpClient;
    private WifiP2pDevice p2pDevice;
    private List<String> receivedMessages;
    private String host;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_data_exchange);

        Bundle extras = getIntent().getExtras();
        p2pDevice = (WifiP2pDevice) extras.get("P2P_DEVICE");
        host = ((WifiP2pInfo) extras.get("P2P_INFO")).groupOwnerAddress.getHostAddress();
        receivedMessages = new ArrayList<>();

        receivedMessagesListView = findViewById(R.id.receivedMessagesListView);
        receivedMessagesListView.setAdapter(new ArrayAdapter<String>(this, R.layout.activity_listview, R.id.textView, receivedMessages));
        socketInfoTextView = findViewById(R.id.socketInfoTextView);
        connectedPeerTextView = findViewById(R.id.connectedPeerTextView);
        editTextMessage = findViewById(R.id.editTextMessage);
        connectSocketButton = findViewById(R.id.connectSocketButton);
        sendButton = findViewById(R.id.sendMessageButton);
        portEditText = findViewById(R.id.editTextPort);

        connectedPeerTextView.setText(String.format("connected to peer: %s", p2pDevice.deviceName));
        connectSocketButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);
        socketInfoTextView.setText(String.format("%s", host));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (tcpClient != null) {
            // disconnect
            tcpClient.stopClient();
            tcpClient = null;
        }
    }

    @Override
    public void onClick(View view) {
        if (null == tcpClient) {
            tcpClient = new TCPClient();
        }
        switch (view.getId()) {
            case (R.id.connectSocketButton):
                if (!tcpClient.isConnected()) {
                    connectSocketButton.setText("Connecting");
                    connectSocketButton.setEnabled(false);
                    new ConnectTask().execute("");
                } else {
                    tcpClient.stopClient();
                    sendButton.setEnabled(false);
                    editTextMessage.setEnabled(false);
                }
                break;
            case (R.id.sendMessageButton):
                new SendMessageTask(editTextMessage.getText().toString()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
        }
    }

    public class SendMessageTask extends AsyncTask<Void, Void, Void> {
        private String message;
        public SendMessageTask(String message){
            this.message = message;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            tcpClient.sendMessage(message);
            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            super.onPostExecute(nothing);
            receivedMessages.add(String.format("> %s", message));
            ((BaseAdapter) receivedMessagesListView.getAdapter()).notifyDataSetChanged();
        }
    }

    public class ConnectTask extends AsyncTask<String, String, TCPClient> {
        boolean connectionFailed;

        @Override
        protected TCPClient doInBackground(String... message) {
            //we create a TCPClient object and
            //here the messageReceived method is implemented
            //this method calls the onProgressUpdate
            tcpClient = new TCPClient(this::publishProgress, message1 -> {
                Log.d("MY_DEBUG", "on connection failed called");
                connectionFailed = true;
            }, this::publishProgress);
            tcpClient.setHost(host);
            tcpClient.setPort(Integer.parseInt(portEditText.getText().toString()));
            tcpClient.run();
            return null;
        }

        @Override
        protected void onPostExecute(TCPClient localTcpClient) {
            if (connectionFailed) {
                Toast.makeText(getApplicationContext(), String.format("Failed to connect to %s:%s", host, portEditText.getText().toString()), Toast.LENGTH_LONG).show();
            }
            tcpClient.stopClient();
            tcpClient = null;
            connectSocketButton.setText("Connect");
            connectSocketButton.setEnabled(true);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if(values.length == 0){
                connectSocketButton.setText("Disconnect");
                connectSocketButton.setEnabled(true);
                sendButton.setEnabled(true);
                editTextMessage.setEnabled(true);
            }else{
                receivedMessages.add(String.format(">> %s", values[0]));
                ((BaseAdapter) receivedMessagesListView.getAdapter()).notifyDataSetChanged();
            }
        }
    }
}





