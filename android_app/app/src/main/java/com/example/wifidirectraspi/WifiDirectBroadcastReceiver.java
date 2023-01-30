package com.example.wifidirectraspi;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;


public class WifiDirectBroadcastReceiver extends BroadcastReceiver implements WifiP2pManager.ActionListener {
    WifiP2pManager manager;
    WifiP2pManager.Channel channel;
    P2PConnectActivity p2PConnectActivity;

    public WifiDirectBroadcastReceiver(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, P2PConnectActivity p2PConnectActivity) {
        this.manager = wifiP2pManager;
        this.channel = channel;
        this.p2PConnectActivity = p2PConnectActivity;

    }

    @SuppressLint("MissingPermission")
    public void connectPeer(WifiP2pDevice p2pDevice) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = p2pDevice.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("MY_DEBUG", String.format("connected to %s", p2pDevice.deviceName));
            }

            @Override
            public void onFailure(int reason) {
                Log.d("MY_DEBUG", String.format("could not connect to %s", p2pDevice.deviceName));
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            Log.d("MY_DEBUG", "WIFI_P2P_PEERS_CHANGED_ACTION");
            manager.requestPeers(channel, p2PConnectActivity);
            manager.requestConnectionInfo(channel, p2PConnectActivity);
        }else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.d("MY_DEBUG", "WIFI_P2P_CONNECTION_CHANGED_ACTION");
            manager.requestConnectionInfo(channel, p2PConnectActivity);
        }
    }


    @SuppressLint("MissingPermission")
    public void discoverPeers() {
        Log.d("MY_DEBUG", "discoverPeers called");
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("MY_DEBUG", "discover peers successful");
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.d("MY_DEBUG", "discover peers failed");
            }
        });
    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onFailure(int i) {

    }
}
