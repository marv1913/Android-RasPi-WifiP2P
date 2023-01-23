package com.example.wifidirectraspi;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class WifiDirectBroadcastReceiver extends BroadcastReceiver implements WifiP2pManager.ConnectionInfoListener {
    WifiP2pManager manager;
    WifiP2pManager.Channel channel;
    MainActivity activity;
    WifiP2pManager.PeerListListener myPeerListListener;
    WifiP2pConfig config;
    WifiP2pDevice device;

    public WifiDirectBroadcastReceiver(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, MainActivity activity) {
        this.manager = wifiP2pManager;
        this.channel = channel;
        this.activity = activity;
    }
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        String action = intent.getAction();
//        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
//            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
//            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
//                Toast.makeText(activity, "Wifi on", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(activity, "Wifi off", Toast.LENGTH_SHORT).show();
//            }
//        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
//        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
//        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
//        }
//    }

    @Override
    @SuppressLint("MissingPermission")
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wi-Fi Direct mode is enabled or not, alert
            // the Activity.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
//            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
//                Toast.makeText(activity, "Wifi on", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(activity, "Wifi off", Toast.LENGTH_SHORT).show();
//            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            if (manager != null) {
                manager.requestPeers(channel, myPeerListListener);
                manager.requestPeers(channel, peers -> {
                    Log.d("MY_DEBUG",String.format("PeerListListener: %d peers available, updating device list", peers.getDeviceList().size()));
                    List<String> deviceNames = new ArrayList<>();
                    List<WifiP2pDevice> p2pDevices = new ArrayList<>(peers.getDeviceList());
                    for(WifiP2pDevice device: p2pDevices){
                        deviceNames.add(device.deviceName);
                    }
                    String[] deviceNameArr = deviceNames.toArray(new String[deviceNames.size()]);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, R.layout.activity_listview, R.id.textView, deviceNameArr);
                    activity.listView.setAdapter(adapter);
                    activity.listView.setOnItemClickListener((adapterView, view, i, l) -> {
                            device = p2pDevices.get(i);
                        config = new WifiP2pConfig();
                        config.deviceAddress = device.deviceAddress;
                        config.wps.setup = WpsInfo.PBC;
                            connectPeer();
                    });
                });
            }

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if (manager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                Toast.makeText(activity, "connected to peer", Toast.LENGTH_SHORT).show();
                // we are connected with the other device, request connection
                // info to find group owner IP
                manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
                        activity.textView.setText("Group Owner IP - " + wifiP2pInfo.groupOwnerAddress.getHostAddress());
                        new DataExchangeAsyncTask().execute();
                    }
                });
            } else {
                // It's a disconnect
                Toast.makeText(activity, "disconnected from peer", Toast.LENGTH_SHORT).show();

            }


        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
  /*          DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
                    .findFragmentById(R.id.frag_list);
            fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));*/

        }
    }

    @SuppressLint("MissingPermission")
    public void connectPeer(){
        Toast.makeText(activity, "Ask for connection to: " + device.deviceName, Toast.LENGTH_SHORT).show();

        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(activity, "Could not connect to: " + device.deviceName, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        // String from WifiP2pInfo struct
        String groupOwnerAddress = info.groupOwnerAddress.getHostAddress();
        Toast.makeText(activity, "Owner address: " + groupOwnerAddress, Toast.LENGTH_SHORT).show();


        // After the group negotiation, we can determine the group owner
        // (server).
        if (info.groupFormed && info.isGroupOwner) {
            // Do whatever tasks are specific to the group owner.
            // One common case is creating a group owner thread and accepting
            // incoming connections.
        } else if (info.groupFormed) {
            // The other device acts as the peer (client). In this case,
            // you'll want to create a peer thread that connects
            // to the group owner.
        }
    }

    public static class DataExchangeAsyncTask extends AsyncTask<Void, Void, Void> {

        private Context context;
        private TextView statusText;

//        public DataExchangeAsyncTask(Context context, TextView statusText) {
//            this.context = context;
//            this.statusText = statusText;
//        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Socket socket = new Socket("192.168.4.1", 4444);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("hello world!");
            }catch (IOException e){
                e.printStackTrace();
            }


            return null;
        }
    }
}
