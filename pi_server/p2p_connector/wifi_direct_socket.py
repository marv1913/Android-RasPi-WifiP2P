"""
This module provides an abstract class for implementing a TCP server socket, which can be used to implement an
application which is communicating with an android phone using WIFI-direct.
"""
import socket
import threading
from abc import ABC, abstractmethod
from _socket import timeout


class WifiDirectSocket(ABC):
    """
    This abstract class can be used as parent class for implementing an application which can communicate with an
    Android phone using WIFI-direct.
    """
    connection: socket
    receive_messages_thread: threading.Thread
    run_receiving_thread: bool = False

    def __init__(self, host: str, port: int):
        """
        constructor
        @param host: IP-address/hostname for the server socket
        @param port: port of the server socket
        """
        self.host: str = host
        self.port: int = port

    def start_server_socket(self):
        """
        Initializes the server socket and waits for an incoming connection. After connection to a client was
        established the private method '__start_receive_thread' is called. This method starts a new thread which
        listens for incoming messages. If message was received the abstract method 'on_receive_message' will be called.
        """
        server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server_socket.bind((self.host, self.port))
        server_socket.listen()
        self.connection, addr = server_socket.accept()
        self.connection.settimeout(1.0)
        print(f'client connected: {addr}')
        self.__start_receive_thread()

    def __start_receive_thread(self):
        """
        Starts a new thread which is waiting for incoming messages. The method is called by 'start_server_socket'.
        """
        self.run_receiving_thread = True
        self.__check_connection()
        self.on_client_connected()

        def receive_loop():
            while self.run_receiving_thread:
                try:
                    data: bytes = self.connection.recv(1024)
                    if not data:
                        self.on_client_disconnected()
                        return
                    else:
                        print(data)
                        self.on_receive_message(data)
                except timeout:
                    continue

        self.receive_messages_thread = threading.Thread(target=receive_loop)
        self.receive_messages_thread.start()

    def send_message_to_client(self, message: bytes):
        """
        Sends a message to the client which is connected to the server-socket provided by this application.
        @param message: message which should be sent to the client (bytes)
        """
        self.__check_connection()
        self.connection.send(message)

    def __check_connection(self):
        """
        Checks whether a client is already connected to the server-socket
        @raise ConnectionError: raises a ConnectionError if no client is connected
        """

    def stop_receive_thread(self):
        """
        Stops the thread which is receiving incoming messages.
        """
        self.run_receiving_thread = False
        self.receive_messages_thread.join()

    @abstractmethod
    def on_receive_message(self, message: bytes):
        """
        Is called when a new message was received.
        @param message: received message (bytes)
        """

    @abstractmethod
    def on_client_connected(self):
        """
        Is called when a client has connected to the server socket.
        """

    @abstractmethod
    def on_client_disconnected(self):
        """
        is called if the client has been disconnected from the server socket.
        """
