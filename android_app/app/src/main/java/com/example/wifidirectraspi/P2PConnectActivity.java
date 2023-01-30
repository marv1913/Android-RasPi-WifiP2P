package com.example.wifidirectraspi;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class P2PConnectActivity extends AppCompatActivity implements WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener {

    WifiP2pManager manager;
    WifiP2pManager.Channel channel;
    private final int REQUEST_LOCATION_PERMISSION = 1;

    ListView listView;
    ProgressBar progressBar;
    Button discoverButton;
    Button connectButton;
    WifiP2pDevice device;
    IntentFilter intentFilter;
    WifiDirectBroadcastReceiver wifiDirectBroadcastReceiver;
    boolean connectPeer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_p2p);
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        wifiDirectBroadcastReceiver = new WifiDirectBroadcastReceiver(manager, channel, this);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        requestLocationPermission();

        listView = findViewById(R.id.listView);
        progressBar = findViewById(R.id.progressBar);
        discoverButton = findViewById(R.id.discoverPeersButton);
        connectButton = findViewById(R.id.connectButton);
        discoverButton.setOnClickListener(view -> discoverPeers());
        connectButton.setOnClickListener(view -> connectPeer());
    }

    public void discoverPeers() {
        listView.clearChoices();
        listView.requestLayout();
        showDiscoverButton(true);
        wifiDirectBroadcastReceiver.discoverPeers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        connectPeer = false;
        registerReceiver(wifiDirectBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiDirectBroadcastReceiver);
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        fillPeerListView(new ArrayList<>(wifiP2pDeviceList.getDeviceList()));
    }

    public void fillPeerListView(List<WifiP2pDevice> p2pDevices) {
        if (0 == listView.getCheckedItemCount()) {
            List<String> deviceNames = new ArrayList<>();
            for (WifiP2pDevice device : p2pDevices) {
                deviceNames.add(device.deviceName);
            }
            String[] deviceNameArr = deviceNames.toArray(new String[deviceNames.size()]);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.activity_listview, R.id.textView, deviceNameArr);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener((adapterView, view, i, l) -> {
                device = p2pDevices.get(i);
                showDiscoverButton(false);
            });
        }
    }

    public void showDiscoverButton(boolean discovering) {
        if (discovering) {
            progressBar.setVisibility(View.VISIBLE);
            discoverButton.setVisibility(View.INVISIBLE);
            connectButton.setVisibility(View.INVISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            discoverButton.setVisibility(View.VISIBLE);
            connectButton.setVisibility(View.VISIBLE);
        }
    }

    public void connectPeer() {
        connectPeer = true;
        wifiDirectBroadcastReceiver.connectPeer(device);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    public void requestLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
        } else {
            EasyPermissions.requestPermissions(this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);
        }
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        Log.d("MY_DEBUG", "onConnectionInfoAvailable");
        if (null != wifiP2pInfo.groupOwnerAddress && connectPeer) {
            Intent myIntent = new Intent(P2PConnectActivity.this, DataExchangeActivity.class);
            myIntent.putExtra("P2P_DEVICE", device);
            myIntent.putExtra("P2P_INFO", wifiP2pInfo);
            P2PConnectActivity.this.startActivity(myIntent);
        }

    }
}
