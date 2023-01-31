import socket
import threading
from _socket import timeout
from abc import ABC, abstractmethod


class WifiDirectSocket(ABC):
    connection: socket
    receive_messages_thread: threading.Thread
    run_receiving_thread: bool = False

    def __init__(self, host: str, port: int):
        self.host: str = host
        self.port: int = port

    def start_server_socket(self):
        server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server_socket.bind((self.host, self.port))
        server_socket.listen()
        self.connection, addr = server_socket.accept()
        self.connection.settimeout(1.0)
        print(f'client connected: {addr}')
        self.__start_receive_thread()

    def __start_receive_thread(self):
        self.run_receiving_thread = True
        self.check_connection()
        self.on_client_connected()

        def receive_loop():
            while self.run_receiving_thread:
                try:
                    data: bytes = self.connection.recv(1024)
                    if not data:
                        self.on_client_disconnected()
                    else:
                        print(data)
                        self.on_receive_message(data)
                except timeout:
                    continue

        self.receive_messages_thread = threading.Thread(target=receive_loop)
        self.receive_messages_thread.start()

    def send_message_to_client(self, message: bytes):
        self.check_connection()
        self.connection.send(message)

    def check_connection(self):
        if self.connection is None:
            raise ConnectionError("client not connected")

    def stop_receive_thread(self):
        self.run_receiving_thread = False
        self.receive_messages_thread.join()

    @abstractmethod
    def on_receive_message(self, message: bytes):
        pass

    @abstractmethod
    def on_client_connected(self):
        pass

    @abstractmethod
    def on_client_disconnected(self):
        pass
