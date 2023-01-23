import os
import subprocess
import time

BLE_INIT_FAILED = ord("0")
BLE_INIT_SUCCESS = ord("1")
CENTRAL_CONNECTED = ord("2")
DEVICE_NAME = ord("3")
START_CONNECT = ord("4")
CENTRAL_DISCONNECTED = ord("5")
DISCONNECT_WFD = ord("7")
START_SOCKET = ord("8")
WPA_CLI_LOCATION = "/usr/sbin/wpa_cli"
P2P_FIND_COMMAND = f"{WPA_CLI_LOCATION} -i p2p-dev-wlan0 p2p_find"
GET_P2P_PEERS_COMMAND = f'for i in $( {WPA_CLI_LOCATION} -i p2p-dev-wlan0 p2p_peers ); do echo -n \"$i \n\"; ' \
                        f'{WPA_CLI_LOCATION} -i p2p-dev-wlan0 p2p_peer $i | grep -E \"device_name=\"; done'
P2P_GROUP_REMOVE_COMMAND = f"{WPA_CLI_LOCATION} -ip2p-dev-wlan0 p2p_group_remove  $(ip -br link | grep -Po " \
                           f"'p2p-wlan0-\\d+')"


class WifiDirectConnector:
    WPA_CLI_HELP = f"{WPA_CLI_LOCATION} -h"

    def __init__(self):
        self.device_name = ""
        self.connected = False
        self.check_wpa_cli_available()

    def check_wpa_cli_available(self):
        """
        check whether the application 'wpa_cli`is installed
        :raises ValueError: raise if not installed or status-code is not equal to 0
        """
        child = subprocess.Popen(self.WPA_CLI_HELP.split(' '), stdout=subprocess.DEVNULL,
                                 stderr=subprocess.STDOUT)
        child.wait()
        if child.returncode != 0:
            raise ValueError('wpa_cli is not available')

    def disconnect(self):
        """
        close p2p connection
        """
        self.connected = False
        print("disconnecting")
        if not self.p2p_group_remove():
            print("Disconnect failed")
        else:
            print("Disconnected")

    def start_connect(self):
        """
        connect to p2p device
        """
        if self.device_name == "":
            return
        print("Starting connection with " + str(self.device_name))
        if not self.p2p_find():
            print("p2p_find failed. stopping")
            return

        mac_addr = self.get_mac_address()

        print("got mac-address: " + str(mac_addr))

        if self.p2p_connect(mac_addr):
            print("successfully connected to " + str(self.device_name))
            self.connected = True

        else:
            print("Connection failed. Something went wrong")

    def central_connected(self):
        """
        search for p2p device
        """
        self.connected = False
        if not self.p2p_find():
            print("p2p_find failed. stopping")
            return

    def central_disconnected(self):
        """
        unset device_name
        """
        self.device_name = ""

    def set_device_name(self, peer_device_name: str):
        """
        setter for device_name attribute
        :param peer_device_name:
        """
        self.device_name = peer_device_name

    def p2p_find(self):
        """
        search for p2p device
        :return: True if device was found; else False
        """
        to_return = False
        p2p_find_file = os.popen(P2P_FIND_COMMAND, 'r')
        cmd_output = p2p_find_file.readline()

        if cmd_output == "OK\n":
            to_return = True

        p2p_find_file.close()

        return to_return

    def get_mac_address(self) -> str:
        """
        get mac address of p2p device
        :return: mac address of p2p device
        """
        to_return = ""

        get_peers_file = os.popen(GET_P2P_PEERS_COMMAND, 'r')
        while True:
            macd = get_peers_file.readline()[:-1]
            device_name = get_peers_file.readline()[12:-1]

            if macd == "" or device_name == "":
                break

            if device_name == self.device_name:
                to_return = macd
                break

        get_peers_file.close()
        if len(to_return) == 0:
            raise ValueError(f"couldn't get mac address of '{self.device_name}'")
        return to_return

    def p2p_connect(self, macd) -> bool:
        """
        connect to device
        :param macd: mac address of target device
        :return: True if connection was successfully established; else False
        """
        to_return = False

        p2p_connect_file = os.popen(f"{WPA_CLI_LOCATION} -i p2p-dev-wlan0 p2p_connect {macd} pbc", 'r')
        cmd_output = p2p_connect_file.readline()

        if cmd_output == "OK\n":
            to_return = True
        else:
            print(cmd_output)

        return to_return

    def p2p_group_remove(self):
        """

        :return:
        """
        to_return = False

        p2p_group_remove_file = os.popen(P2P_GROUP_REMOVE_COMMAND, "r")
        cmd_output = p2p_group_remove_file.readline()

        if cmd_output == "OK\n":
            to_return = True
        else:
            print(cmd_output)

        return to_return

    def start_connect_loop(self):
        """
        waits for connection request
        """
        connected = False
        while not connected:
            try:
                mac_addr = wifi_direct_conn.get_mac_address()
                print(f'mac address: {mac_addr}')
                connected = wifi_direct_conn.p2p_connect(mac_addr)
            except ValueError:
                time.sleep(1)


if __name__ == "__main__":
    wifi_direct_conn = WifiDirectConnector()
    wifi_direct_conn.set_device_name('Android_dq8l')
    wifi_direct_conn.p2p_group_remove()
    wifi_direct_conn.central_connected()

    wifi_direct_conn.start_connect_loop()
