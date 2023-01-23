import socket
import sys
from abc import ABC, abstractmethod


class WifiDirectSocket(ABC):
    HOST = "192.168.4.1"  # Standard loopback interface address (localhost)
    PORT = 4444  # Port to listen on (non-privileged ports are > 1023)
    connection: socket

    def start_server_socket(self):
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.bind((self.HOST, self.PORT))
        s.listen()
        self.connection, addr = s.accept()
        print(f'client connected: {addr}')
        self.on_connected()

    def start_receive_thread(self):
        self.check_connection()
        while True:
            data: bytes = self.connection.recv(1024)
            if not data:
                sys.exit(0)
            self.on_receive(data)

    def send_message_to_client(self, message: bytes):
        self.check_connection()
        self.connection.send(message)

    def check_connection(self):
        if self.connection is None:
            raise ConnectionError("client not connected")

    def stop_receive_thread(self):
        self.connection.shutdown(socket.SHUT_WR)

    @abstractmethod
    def on_receive(self, message: bytes):
        pass

    @abstractmethod
    def on_connected(self):
        pass
