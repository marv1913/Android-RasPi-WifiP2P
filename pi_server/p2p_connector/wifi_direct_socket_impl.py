"""
This module provides an example application to show how the abstract class WifiDirectSocket could be used to implement
application which communicates with an android phone using a P2P connection.
"""
import click
from click_shell import shell

from p2p_connector.wifi_direct_socket import WifiDirectSocket


class SimpleMessenger(WifiDirectSocket):
    """
    This class inherits from the abstract parent class WifiDirectSocket. This is a simple example how the class
    WifiDirectSocket can be used to establish a P2P connection.
    """

    def on_receive_message(self, message: bytes):
        print(f'received message: {message.decode()}')

    def on_client_connected(self):
        print('client connected')

    def on_client_disconnected(self):
        print('client has disconnected')

    def send_text_message(self, message: str):
        """
        Sends a message to the client.
        @param message: message as str
        """
        self.send_message_to_client(f'{message}\n'.encode())


messenger = SimpleMessenger('192.168.4.1', 4444)


@shell(prompt='my-app > ', intro='Starting my app...')
def main():
    messenger.start_server_socket()


@main.command()
@click.option("--message", prompt=" Enter the message", type=str)
def send(message):
    click.echo(f"sending message '{message}'")
    messenger.send_text_message(message)


@main.command()
def stop():
    click.echo("exiting")
    messenger.stop_receive_thread()


if __name__ == "__main__":
    main()
