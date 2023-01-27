"""
This module provides functions to be able to connect to a target p2p device using the application 'wpa_cli'
"""
import re
import subprocess
import time


class P2PPeer:
    """
    This class represents a p2p peer.
    """

    def __init__(self, device_name: str, mac_address: str, status: int):
        self.device_name = device_name
        self.mac_address = mac_address
        self.status = status

    def __str__(self):
        return f"{self.device_name} - {self.mac_address}"

    def __repr__(self):
        return self.__str__()


class WifiDirectConnector:
    """
    This class provides methods for establishing a WiFi-Direct connection with an Android device.
    """
    WPA_CLI_LOCATION = "/usr/sbin/wpa_cli"
    WPA_CLI_LISTEN = "p2p_listen"
    WPA_CLI_P2P_PEERS = "p2p_peers"
    WPA_CLI_HELP = "-h"
    P2P_FIND_COMMAND = f"{WPA_CLI_LOCATION} -i p2p-dev-wlan0 p2p_find"
    GET_P2P_PEERS_COMMAND = f'for i in $( {WPA_CLI_LOCATION} -i p2p-dev-wlan0 p2p_peers ); do echo -n \"$i \n\"; ' \
                            f'{WPA_CLI_LOCATION} -i p2p-dev-wlan0 p2p_peer $i | grep -E \"device_name=\"; done'
    P2P_GROUP_REMOVE_COMMAND = "-ip2p-dev-wlan0 p2p_group_remove  $(ip -br link | grep -Po 'p2p-wlan0-\\d+')"
    P2P_CONNECT = "p2p_connect {target_device_mac_addr} pbc"

    def __init__(self):
        self.device_name = ""
        self.connected = False
        self.check_wpa_cli_available()

    def execute_wpa_cli_command(self, command: str):
        """
        Executes wpa_cli command
        @param command: argument(s) as str
        """
        subprocess.run(f"{self.WPA_CLI_LOCATION} {command}", check=True)

    def check_wpa_cli_available(self):
        """
        check whether the application 'wpa_cli`is installed
        @raise ValueError: raises if not installed or status-code is not equal to 0
        """
        self.execute_wpa_cli_command(self.WPA_CLI_HELP)

    def remove_p2p_group(self):
        """
        removes all p2p groups
        """
        self.execute_wpa_cli_command(self.P2P_GROUP_REMOVE_COMMAND)

    def make_device_visible(self):
        """
        executes 'wpa_cli listen' to make device visible
        """
        self.execute_wpa_cli_command(self.WPA_CLI_LISTEN)

    def get_available_devices(self) -> list[P2PPeer]:
        """
        Gets all available target devices.
        @return: list containing all target devices
        """
        mac_addresses: list[str] = []
        result = subprocess.run("/usr/sbin/wpa_cli p2p_peers", shell=True, capture_output=True, check=True)
        for line in result.stdout.decode().splitlines():
            if re.search(r"^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$", line):
                mac_addresses.append(line)

        available_peers: list[P2PPeer] = []
        for mac_address in mac_addresses:
            result = subprocess.run(f"/usr/sbin/wpa_cli p2p_peer {mac_address}", shell=True, capture_output=True,
                                    check=True)
            stdout = result.stdout.decode()
            device_name = re.search(r'(?<=device_name=).*', stdout).group()
            device_status = re.search(r'(?<=status=).*', stdout).group()
            available_peers.append(P2PPeer(device_name, mac_address, int(device_status)))
        return available_peers

    def connect_to_device_if_available(self, device_name, attempts: int = 30, time_to_sleep: int = 2):
        """
        Connects to a target device and waits for an incoming connection request.
        @param device_name: target device name
        @param attempts: how many attempts should be used to wait for the connection request
        @param time_to_sleep: time in seconds how long should be waited between each attempt
        """
        for _ in range(attempts):
            status_text = f'\rwaiting for connection request from {device_name}. '
            target_device: P2PPeer
            for target_device in self.get_available_devices():
                if target_device.device_name == device_name:
                    # found device
                    if target_device.status == 1:
                        # got connection request
                        self.connect_to_target_device(target_device)
                        return True
                    status_text = status_text + 'Found device, but waiting for connection_request'
            print(status_text, end='')
            time.sleep(time_to_sleep)
        return False

    def connect_to_target_device(self, device: P2PPeer):
        """
        Connects to a target device.
        @param device: target device as P2PPeer object
        """
        self.execute_wpa_cli_command(self.P2P_CONNECT.format(target_device_mac_addr=device.mac_address))


if __name__ == "__main__":
    wifi_direct_conn = WifiDirectConnector()
    wifi_direct_conn.make_device_visible()

    wifi_direct_conn.connect_to_device_if_available('pixel6')
    # wifi_direct_conn.remove_p2p_group()
