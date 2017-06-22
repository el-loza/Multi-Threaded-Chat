import Console.Print;
import common.IChat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import Console.Print;


/**
 * This class constructs the UI for a chat server. It implements the chat
 * interface in order to activate the display() method.
 *
 * @author Ethan Loza
 * @author Trevor Se
 * @author Will Hinz
 * @version October 2016
 */

public class ServerConsole implements IChat {

	private final static int DEFAULT_PORT = 5555;
	private EchoServer server;

	ServerConsole(int port) {
		try {
			server = new EchoServer(port, this);
		} catch (IOException ioexception) {
			Print.error("Error: Can't setup connection!" + " Terminating server.");
			System.exit(1);
		}
		try {
			server.listen();
		} catch (Exception e) {
			Print.error("Error: - Could not listen for clients!");
		}
	}

	void accept() {
		try {
			BufferedReader fromConsole = new BufferedReader(new InputStreamReader(System.in));
			String message;

			//noinspection InfiniteLoopStatement
			while (true) {
				message = fromConsole.readLine();
				server.handleMessageFromServerUI(message);
			}
		} catch (Exception exception) {
			Print.error("Unexpected error while reading from console!");
		}
	}

	public void display(String message) {
		Print.line("> " + message);
	}

	public static void main(String[] args) {
		ServerConsole chat = new ServerConsole(DEFAULT_PORT);
		chat.accept(); // Wait for console data
	}

}
