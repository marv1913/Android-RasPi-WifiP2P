package com.example.wifidirectraspi;

import android.net.wifi.p2p.WifiP2pDevice;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.ArrayList;
import java.util.List;

public class DataExchangeActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView connectedPeerTextView;
    private ListView receivedMessagesListView;
    private TextView socketInfoTextView;
    private EditText editTextMessage;
    private Button connectSocketButton;
    private Button sendButton;
    private TCPClient tcpClient;
    private WifiP2pDevice p2pDevice;
    private List<String> receivedMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_data_exchange);

        Bundle extras = getIntent().getExtras();
        p2pDevice = (WifiP2pDevice) extras.get("P2P_DEVICE");
        receivedMessages = new ArrayList<>();

        receivedMessagesListView = findViewById(R.id.receivedMessagesListView);
        receivedMessagesListView.setAdapter(new ArrayAdapter<String>(this, R.layout.activity_listview, R.id.textView, receivedMessages));
        socketInfoTextView = findViewById(R.id.socketInfoTextView);
        connectedPeerTextView = findViewById(R.id.connectedPeerTextView);
        editTextMessage = findViewById(R.id.editTextMessage);
        connectSocketButton = findViewById(R.id.connectSocketButton);
        sendButton = findViewById(R.id.sendMessageButton);

        connectedPeerTextView.setText(String.format("connected to peer: %s", p2pDevice.deviceName));
        connectSocketButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);
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
                }
                break;
            case (R.id.sendMessageButton):
                Log.d("MY_DEBUG", "send button clicked");
                new SendMessageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        editTextMessage.getText().toString());
                break;
        }
    }

    public class SendMessageTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
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
            //here the messageReceived method is implemented
            //this method calls the onProgressUpdate
            tcpClient = new TCPClient(this::publishProgress, message1 -> {
                Log.d("MY_DEBUG", "on connection failed called");
                connectionFailed = true;
            }, this::publishProgress);
            tcpClient.run();
            return null;
        }

        @Override
        protected void onPostExecute(TCPClient localTcpClient) {
            if (connectionFailed) {
                socketInfoTextView.setText("connection failed");
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
                socketInfoTextView.setText("connected to ...");
            }else{
                receivedMessages.add(String.format(">> %s", values[0]));
                ((BaseAdapter) receivedMessagesListView.getAdapter()).notifyDataSetChanged();
            }
        }
    }
}





