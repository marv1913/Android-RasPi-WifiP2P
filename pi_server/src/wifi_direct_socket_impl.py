import click
from click_shell import shell, make_click_shell

from pi_server.src.wifi_direct_socket import WifiDirectSocket


class SimpleMessenger(WifiDirectSocket):

    def on_receive(self, message: bytes):
        print(f'received message: {message.decode()}')

    def send_text_message(self, message: str):
        self.send_message_to_client(message.encode())

    def on_connected(self):
        print('start receive thread')
        self.start_receive_thread()


messenger = SimpleMessenger()
messenger.start_server_socket()


@shell(prompt='my-app > ', intro='Starting my app...')
def main():
    pass


@main.command()
@click.option("--message", prompt=" Enter the message", type=str)
def send(message):
    click.echo(f"sending message '{message}'")
    messenger.send_text_message(message)


if __name__ == "__main__":
    main()
