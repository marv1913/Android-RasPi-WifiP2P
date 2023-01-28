![pylint badge](https://github.com/marv1913/Android-RasPi-WifiP2P/blob/badges/pylint.svg) [![Build APK](https://github.com/marv1913/Android-RasPi-WifiP2P/actions/workflows/android.yml/badge.svg)](https://github.com/marv1913/Android-RasPi-WifiP2P/actions/workflows/android.yml)

# Android-RasPi-WifiP2P-Communication
This project provides two applications for setting up a Wifi Direct connection between a Raspberry Pi 3b and an android 
phone. 
Contains config scripts as well as (WIP) code for setting up a connection, setting up a TCP socket, and exchanging data 
on both the android and the pi

links: 

https://raspberrypi.stackexchange.com/questions/117150/issue-connecting-raspberry-pi-to-android-via-wifi-p2p
https://raspberrypi.stackexchange.com/questions/117718/how-to-connect-an-android-phone-and-a-raspberry-pi-through-wifi-direct-programma

## Raspberry Pi P2P Server

The Application to start the P2P Server for the Raspberry Pi is located at `pi_server/src/wifi_direct_connector.py`.
Before you can run the application it is necessary to install some additional packages and change a few network configurations.

The `rasperry-config.sh` script can be used to install these packages. After running this script the Raspberry Pi 
will lose the current Wi-Fi connection. It's still possible to communicate with the Raspberry Pi using a wired connection.

### Usage

#### Start the P2P-Connector application:

`python3 pi_server/src/wifi_direct_connetor.py --target [target_device_name]`

The Raspberry Pi is now waiting for an incoming connection from the device with the device name 'target_device_name'.
After receiving a connection request from the android phone, the Raspberry Pi will establish a connection automatically.

#### Remove P2P Group (disconnect from target device):

`python3 pi_server/src/wifi_direct_connetor.py --remove-group`


### display usage text

`python3 pi_server/src/wifi_direct_connetor.py -h`

## Android App
