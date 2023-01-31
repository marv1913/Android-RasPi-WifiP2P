import sys

import click
from click_shell import shell, make_click_shell

from p2p_connector.wifi_direct_socket import WifiDirectSocket


class SimpleMessenger(WifiDirectSocket):

    def on_receive_message(self, message: bytes):
        print(f'received message: {message.decode()}')

    def on_client_connected(self):
        print(f'client connected')

    def on_client_disconnected(self):
        print(f'client has disconnected')

    def send_text_message(self, message: str):
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
def exit():
    click.echo(f"exiting")
    messenger.stop_receive_thread()


if __name__ == "__main__":
    main()
