"""
This module provides functions to be able to connect to a target p2p device using the application 'wpa_cli'
"""
import argparse
import re
import subprocess
import sys
import time
from typing import Optional


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
    WPA_CLI_P2P_PEER_DETAILS = "p2p_peer {mac_address}"
    WPA_CLI_HELP = "-h"
    P2P_FIND_COMMAND = f"{WPA_CLI_LOCATION} -i p2p-dev-wlan0 p2p_find"
    P2P_GROUP_REMOVE_COMMAND = "-ip2p-dev-wlan0 p2p_group_remove {interface_name}"
    GET_WIFI_INTERFACE = "ip -br link | grep -Po 'p2p-wlan0-\\d+'"
    P2P_CONNECT = "p2p_connect {target_device_mac_addr} pbc"

    def __init__(self):
        self.device_name = ""
        self.connected = False
        # self.check_wpa_cli_available()

    def execute_wpa_cli_command(self, command: str, ignore_status_code: Optional[int] = None) -> str:
        """
        Executes wpa_cli command
        @param command: argument(s) as str
        @param ignore_status_code: optional argument - if set no error will be raised if status code is not equal to
        zero but is equal to 'ignore_status_code'
        """
        command = f"{self.WPA_CLI_LOCATION} {command}"
        result = subprocess.run(command, shell=True, capture_output=True)
        if result.returncode != 0 and (ignore_status_code is None or result.returncode != ignore_status_code):
            raise ValueError(f'Error occurred while executing command "{command}": {result.stderr}')
        return result.stdout.decode()

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
        result = subprocess.run(self.GET_WIFI_INTERFACE, shell=True, capture_output=True)
        if len(result.stdout) > 0:
            self.execute_wpa_cli_command(self.P2P_GROUP_REMOVE_COMMAND.format(interface_name=result.stdout.decode()))
            time.sleep(15)

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
        stdout = self.execute_wpa_cli_command(self.WPA_CLI_P2P_PEERS, ignore_status_code=255)
        for line in stdout.splitlines():
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

    def check_already_connected(self, device_name: str) -> bool:
        """
        Checks whether a target device is already connected to this device.
        @param device_name: device name of target device as str
        @return: True if device is already connected, else False
        """
        for target_device in self.get_available_devices():
            if target_device.device_name == device_name:
                stdout = self.execute_wpa_cli_command(
                    self.WPA_CLI_P2P_PEER_DETAILS.format(mac_address=target_device.mac_address))
                return re.search(r'(?<=interface_addr=).*', stdout).group() != '00:00:00:00:00:00'
        return False


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Provides an Interface to set up a P2P Server.')
    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument('--target', help="Set name of target device")
    group.add_argument('--remove-group', action='store_true', help="remove p2p group of this device")
    args = parser.parse_args()

    wifi_direct_conn = WifiDirectConnector()

    target_device_name = args.target
    if args.remove_group:
        print('removing p2p group')
        wifi_direct_conn.remove_p2p_group()
    else:
        if wifi_direct_conn.check_already_connected(target_device_name):
            print(f'device "{target_device_name}" is already connected')
            sys.exit(0)

        wifi_direct_conn.make_device_visible()
        wifi_direct_conn.connect_to_device_if_available(target_device_name)
