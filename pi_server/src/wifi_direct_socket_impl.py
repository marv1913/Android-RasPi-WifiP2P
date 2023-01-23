import socket
from abc import ABC, abstractmethod

from pi_server.src.wifi_direct_socket import WifiDirectSocket


class SimpleMessenger(WifiDirectSocket):

    def on_receive(self, message: bytes):
        print(f'received message: {message.decode()}')

    def send_text_message(self, message: str):
        self.send_message_to_client(message.encode())

    def on_connected(self):
        self.send_text_message('hello world')