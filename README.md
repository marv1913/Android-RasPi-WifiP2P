![pylint badge](https://github.com/marv1913/Android-RasPi-WifiP2P/blob/badges/pylint.svg) [![Build APK](https://github.com/marv1913/Android-RasPi-WifiP2P/actions/workflows/android.yml/badge.svg)](https://github.com/marv1913/Android-RasPi-WifiP2P/actions/workflows/android.yml)  
  
# Android-RasPi-WifiP2P-Communication  
This project provides two applications for setting up a Wifi Direct connection between a Raspberry Pi 3b and an android   
phone.   
Contains config scripts as well code for setting up a connection, setting up a TCP socket, and exchanging data   
on both the android and the pi.  
  


## Establish a P2P connection  
  
To establish a P2P connection between the raspberry pi and an android phone two steps are required:  
1. Setup the Raspberry Pi and start the P2P server on the Raspberry Pi  
2. connect to the raspberry pi using Wi-Fi direct from the raspberry pi  

## Raspberry Pi P2P Server  
  
  
### Setting up the Raspberry Pi  
  
To be able to establish a P2P connection between a Raspberry Pi and an android phone a few packages need to be installed  
some configurations must be changed. For that reason the repository contains the installation script `raspberry-config.sh`.  
After running this script the Raspberry Pi will lose the current Wi-Fi connection. It's still possible to communicate   
with the Raspberry Pi using a wired connection. 
  
#### Start the P2P Server  
  
To start the P2P connector run this command:  
  
`python3 pi_server/p2p_connector/wifi_direct_connetor.py [target_device_name]`  
  
The Raspberry Pi is now waiting for an incoming connection from the device with the device name 'target_device_name'.  
After receiving a connection request from the android phone, the Raspberry Pi will establish a connection automatically.
Now you can continue with the step [Connect to the Raspberry Pi](#connect-to-the-raspberry-pi-using-wi-fi-direct).
  
##### Remove P2P Group (disconnect from target device):  
  
`python3 pi_server/src/wifi_direct_connetor.py --remove-group`  
  
##### display usage text  
  
`python3 pi_server/src/wifi_direct_connetor.py -h`  

#### documentation
The documentation of the python code is hosted as [Github Page.](https://marv1913.github.io/Android-RasPi-WifiP2P/docs/python_p2p_connector/p2p_connector.html)
  
## Connect to the Raspberry Pi using Wi-Fi direct  
  
After the P2P server on the Raspberry Pi was started it's possible to connect to it using Wi-Fi direct. There are two options for  establishing a connection:  
  
#### 1. Use the android system settings  
-> settings -> Wi-Fi -> open the dropdown menu (three vertical dots icon) -> Wi-Fi Direct -> choose the device name 
which was set in the `raspberry-config.sh` file
#### 2. Use the P2P-Connector Android App which is provided by this project  
  
  
## Android App  
  
### Exchange data between the Raspberry Pi and the android phone  
  
After the android phone is connected to the Raspberry Pi it is possible to send data using TCP sockets. On the Raspberry Pi a server socket should to be started. After that it's possible to connect to this socket from an android app. The Android-RasPi-WifiP2P App which is provided by this project can has already implemented a TCPSocket class and an example Activity which shows how a TCP connection could be established. The App is a good entrypoint for developing a Android P2P app using Wi-Fi direct.

links:   
  
https://raspberrypi.stackexchange.com/questions/117150/issue-connecting-raspberry-pi-to-android-via-wifi-p2p  
https://raspberrypi.stackexchange.com/questions/117718/how-to-connect-an-android-phone-and-a-raspberry-pi-through-wifi-direct-programma  
  
https://github.com/CatalinPrata/funcodetuts/blob/master/AndroidTCPClient  