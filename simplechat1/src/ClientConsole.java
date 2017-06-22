import client.ChatClient;
import common.IChat;
import Console.Print;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import static java.lang.System.exit;

public class ClientConsole implements IChat {
	private final static int DEFAULT_PORT = 5555;

	private ChatClient client;

	public ClientConsole(String host, int port) {
		try {
			client = new ChatClient(host, port, this);
		} catch (IOException ioexception) {
			Print.error("Error: Can't setup connection!" + " Terminating client.");
			exit(1);
		}
	}

	private void accept() {
		try {
			BufferedReader fromConsole = new BufferedReader(new InputStreamReader(System.in));
			String message;

			while (true) {
				message = fromConsole.readLine();
				client.handleMessageFromClientUI(message);
			}
		} catch (Exception exception) {
			Print.error("Unexpected error while reading from console!");
		}
	}

	public void display(String message) {
		System.out.println(message);
	}

	public static void main(String[] args) {
		if (args.length > 3) {
			Print.error("Error: Can't setup connection!  Terminating client.");
			exit(-1);
			throw (new IllegalArgumentException("Usage: [username] [?host] [?port]"));
		}
		// get the username
		String username = "";
		if (args.length > 1)
			username = args[0];
		String host = "localhost";
		int port = DEFAULT_PORT;
		if (args.length > 1) {
			host = args[1];
			if (args.length == 3) {
				boolean portParsed = false;
				String portInputString = args[2];
				while (!portParsed) {
					try {
						port = Integer.parseInt(portInputString);
						portParsed = true;
					} catch (Exception ignored) {
						Print.error(String.format("Invalid Port: [%s]", portInputString));
						Print.error("Please enter the a valid port number.");
						try {
							Scanner scan = new Scanner(System.in);
							portInputString = scan.nextLine();
							if (portInputString.isEmpty()) {
								port = DEFAULT_PORT;
								portParsed = true;
							}
						} catch (Exception ignore) {
						}
					}
				}
			}
		}

		ClientConsole chat = new ClientConsole(host, port);
		chat.accept();  //Wait for console data
	}
}
