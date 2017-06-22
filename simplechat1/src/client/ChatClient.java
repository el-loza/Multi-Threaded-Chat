package client;

import common.IChat;
import ocsf.client.AbstractClient;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

// I accidentally did this on the master at first. This comment is created to show that

public class ChatClient extends AbstractClient {

	// It allows the implementation of the display method in the client.
	private IChat clientChatInterface;
	private UserProfile user;

	// Constructs an instance of the chat client.
	/* @param hostAddress         The server to connect to.
	 * @param hostPort            The port number to connect on.
	 * @param clientChatInterface The interface type variable.
	 */
	public ChatClient(String hostAddress, int hostPort, IChat clientChatInterface) throws IOException {
		super(hostAddress, hostPort); //Call the superclass constructor

		this.clientChatInterface = clientChatInterface;
	}

	// This method handles all data that comes in from the server.
	// @param message The message from the server.
	public void handleMessageFromServer(Object message) {
		String msg = message.toString();
		switch (msg) {
			case "#loginerror":
				clientChatInterface.display("Error logging in, user is already logged in.");
				try {
					super.closeConnection();
				} catch (Exception ignore) {
				}
				break;
			case "#serverClosed":
				clientChatInterface.display("The server has stopped listening for connection. Please login later...");
				try {
					super.closeConnection();
				} catch (Exception ignore) {
				}
				break;
			default:
				clientChatInterface.display(message.toString());
				break;
		}
	}

	// This method handles all data coming from the UI
	// @param message The message from the UI.
	public void handleMessageFromClientUI(String message) {
		try {
			if (message.isEmpty()) {
				System.out.println("Please type something to be sent to the server.");
			} else {
				// is a command that it should interpret
				if (message.charAt(0) == '#') {
					if (!clientCommands(message))
						commandUnrecognized();
				} else {
					// is a messages
					if (!super.isConnected()) { // is not connected
						System.out.println("No connection to server. Please login using \"#login <user_id>\" in order to continue posting.");
					} else {
						/// Format <userID> <> <message>
						sendToServer(String.format("<%s> <sendToAll> <%s>", this.user.getUserName(), message));
					}
				}
			}
		} catch (IOException exception) {
			clientChatInterface.display("Could not send message to server. Terminating client.");
			quit();
		}
	}

	public boolean tryToConnectTimeout() {
		AtomicBoolean finished = new AtomicBoolean(false);
		AtomicBoolean failed = new AtomicBoolean(false);
		Thread thread = new Thread(() -> {
			try {
				openConnection();
				finished.set(true);
			} catch (Exception e) {
				failed.set(true);
			}
		});
		thread.start();
		long startTime = System.currentTimeMillis();
		//noinspection StatementWithEmptyBody
		while (System.currentTimeMillis() - startTime < 1000) {
			// wait in the body
			if (finished.get()) {
				try {
					thread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return true;
			}
			if (failed.get())
				break;
		}
		try {
			closeConnection();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		thread.interrupt();
		this.user = null;
		return false;
	}

	/// Client commands
	private boolean clientCommands(String line) {
		line = line.substring(1); // chops off the '#'

		// checks for empty command field
		if (line.equals(""))
			return false;

		List<String> tokenArguments = parsers.CommandLineParser.TokenizedArguments(line);
		List<String> spaceArguments = parsers.CommandLineParser.SpaceDelimitedArguments(line);
		String command;
		if (spaceArguments.size() > 0)
			command = spaceArguments.get(0);
		else
			return false;

		switch (command) {
			case "sethost":
				commandSetHost(tokenArguments);
				break;
			case "channel":
				commandCreateChannel(tokenArguments);
				break;
			case "send":
				commandSendChannel(tokenArguments);
				break;
			case "join":
				commandJoinChannel(tokenArguments);
				break;
			case "leave":
				commandLeaveChannel(tokenArguments);
				break;
			case "setport":
				commandSetPort(tokenArguments);
				break;
			case "logoff":
			case "logout":
				commandLogoff(spaceArguments);
				break;
			case "gethost":
				commandGetHost(tokenArguments);
				break;
			case "getport":
				commandGetPort(tokenArguments);
				break;
			case "login":
				commandLogin(tokenArguments);
				break;
			case "quit":
				commandQuit(spaceArguments);
				break;
			case "block":
				commandBlock(tokenArguments);
				break;
			case "unblock":
				commandUnblock(tokenArguments);
				break;
			case "whoiblock":
				commandWhoIBlock(spaceArguments);
				break;
			case "whoblocksme":
				commandWhoBlocksMe(spaceArguments);
				break;
			case "available":
				commandAvailable(spaceArguments);
				break;
			case "unavailable":
				commandUnavailable(spaceArguments);
				break;
			case "status":
				commandStatus(tokenArguments);
				break;
			case "private":
				commandPrivate(tokenArguments);
				break;
			case "startforwarding":
				commandStartForwarding(tokenArguments);
				break;
			case "cancelforwarding":
				commandCancelForwarding(tokenArguments);
				break;
			case "whoforwardtome":
				commandWhoForwardToMe(tokenArguments);
				break;
			case "whoforwardto":
				commandWhoForwardTo(tokenArguments);
				break;
			default:
				return false;
		}
		return true;
	}

	/// Format: #setHost <localhost>
	private boolean commandSetHost(List<String> arguments) {
		if (super.isConnected()) {
			System.out.println("Error: You need to logoff before you change the host.");
			return false;
		}
		if (arguments.size() != 2) {
			System.out.println("Error: Bad format. Should be #setHost <host name>");
			return false;
		}
		String host = arguments.get(1);
		System.out.println("Host successfully set to: " + host);
		super.setHost(host); // set the host
		return true;
	}

	/// Format: #setPort <port>
	private boolean commandSetPort(List<String> arguments) {
		if (super.isConnected()) {
			System.out.println("Error, you need to logoff before you change the port.");
			return false;
		}
		if (arguments.size() != 2) {
			System.out.println("Error: Bad format. Should be #setPort <port number>");
			return false;
		}

		int port;
		try {
			port = Integer.parseInt(arguments.get(1));
		} catch (NumberFormatException e) {
			System.out.println("Given port was not a number, please specify a number.");
			return false;
		}
		System.out.println("Port successfully set to: " + port);
		super.setPort(port); // set the port
		return true;
	}

	/// Format: #getHost
	private boolean commandGetHost(List<String> arguments) {
		if (!noArgs(arguments)) {
			return false;
		}
		System.out.println("Current Host: " + super.getHost());
		return true;
	}

	/// Format: #getPort
	private boolean commandGetPort(List<String> arguments) {
		if (!noArgs(arguments)) {
			return false;
		}
		System.out.println("Current Port: " + super.getPort());
		return true;
	}


	/// Server communication commands

	/// Format: #login <userID> <password>
	private boolean commandLogin(List<String> arguments) {
		if (arguments.size() != 3) {
			System.out.println("Bad format. Should be like... #login <user_id> <password>");
			return false;
		}

		if (!super.isConnected()) { // if the client is NOT connected
			this.user = new UserProfile(arguments.get(1), null, true, null, "");
			if (tryToConnectTimeout()) {
				// send the login command to the server.
				/// Format: <userID> <login> <password>
				try {
					sendToServer(String.format("<%s> <login> <%s>", arguments.get(1), arguments.get(2)));
				} catch (IOException e) {
					System.out.printf("Error logging into the server");
				}
			} else {
				System.out.println("Cannot connect to server. Please try again.");
			}
			//System.out.println("Login successful. Welcome " + arguments + "!"); // server needs to say this.
		} else {
			System.out.println("You are already logged in. Please log out using \"#logoff\" before logging in again.");
		}

		return true;
	}

	/// Format: #logoff
	private boolean commandLogoff(List<String> arguments) {
		if (!noArgs(arguments))
			return false;

		if (super.isConnected()) { // if the client is connected
			try {
				/// Format: <userID> <logoff>
				sendToServer(String.format("<%s> <logoff>", this.user.getUserName()));
				super.closeConnection();
				System.out.println("Logoff successful. Please login to a server using \"#login <user_id>\" to continue to post.");
				return true;
			} catch (IOException ioe) {
				System.out.println("Close connection failed! Terminating Abnormally");
				System.exit(-1);
				return false; // not needed, except maybe for unit tests if we get to it...
			}
		} else { // if the client is disconnected
			System.out.println("You are already logged out! Please login to a server using \"#login <user_id>\" to continue to post.");
			return true;
		}
	}

	/// Format: #quit
	private boolean commandQuit(List<String> arguments) {
		if (!noArgs(arguments))
			return false;

		try {
			super.closeConnection();
			quit();
		} catch (IOException e) {
			System.out.println("Close connection failed! Terminating Abnormally");
			System.exit(-1);
		}
		return false;
	}

	/// Format: #block <userIDToBlock>
	private boolean commandBlock(List<String> arguments) {
		try {
			/// Format: <userID> <block> <userIDToBlock>
			if(arguments.size() != 2){
				System.out.println("Argument number mismatch. Please chech the arguments.");
				return false;
			}
			sendToServer(String.format("<%s> <block> <%s>", this.user.getUserName(), arguments.get(1)));
		} catch (IOException e) {
			System.out.println("Error when blocking.");
			return false;
		}
		return true;
	}

	/// Format: #unblock <userIDToUnblock>
	private boolean commandUnblock(List<String> arguments) {
		try {
			/// Format; <userID> <unblock> <userIDToUnblock>
			if(arguments.size() != 2){
				System.out.println("Argument number mismatch. Please chech the arguments.");
				return false;
			}
			sendToServer(String.format("<%s> <unblock> <%s>", this.user.getUserName(), arguments.get(1)));
		} catch (IOException e) {
			System.out.println("Error when unblocking.");
			return false;
		}
		return true;

	}

	/// Format: #whoIBlock
	private boolean commandWhoIBlock(List<String> arguments) {
		if (!noArgs(arguments))
			return false;
		try {
			sendToServer(String.format("<%s> <whoiblock>", this.user.getUserName()));
		} catch (IOException e) {
			System.out.println("Error when asking whoiblock.");
			return false;
		}
		return true;
	}

	/// Format: #whoBlocksMe
	private boolean commandWhoBlocksMe(List<String> arguments) {
		if (!noArgs(arguments))
			return false;
		try {
			sendToServer(String.format("<%s> <whoblocksme>", this.user.getUserName()));
		} catch (IOException e) {
			System.out.println("Error when whoblocksme.");
			return false;
		}
		return true;
	}

	/// Format: #available
	private boolean commandAvailable(List<String> arguments) {
		if (!noArgs(arguments))
			return false;

		try {
			sendToServer(String.format("<%s> <available>", this.user.getUserName()));
		} catch (IOException e) {
			System.out.println("Error when #available.");
			return false;
		}
		return true;
	}

	/// Format: #unavailable
	private boolean commandUnavailable(List<String> arguments) {
		if (!noArgs(arguments))
			return false;

		try {
			sendToServer(String.format("<%s> <unavailable>", this.user.getUserName()));
		} catch (IOException e) {
			System.out.println("Error when #unavailable.");
			return false;
		}
		return true;
	}

	/// Format: #status <userID/Channel to check>
	private boolean commandStatus(List<String> arguments) {
		try {
			/// Format: <userID> <status> <userID/Channel Status>
			if(arguments.size() != 2){
				System.out.println("Argument number mismatch. Please chech the arguments.");
				return false;
			}
			sendToServer(String.format("<%s> <status> <%s>", this.user.getUserName(), arguments.get(1)));
		} catch (IOException e) {
			System.out.println("Error when checking status.");
			return false;
		}
		return true;
	}

	/// Format: #private <user_id> message
	private boolean commandPrivate(List<String> arguments) {
		if (arguments.size() != 3) {
			System.out.println("Error: private command needs to follow the format:  #private <user_id> message");
			return false;
		}

		try {
			/// Format: <userID> <privateMessage> <userIDToWho> <message>
			sendToServer(String.format("<%s> <private> <%s> <%s>",
					this.user.getUserName(), arguments.get(1), arguments.get(2)));
		} catch (IOException e) {
			System.out.println("Error when sending a private message");
			return false;
		}
		return true;
	}

	/// Format: #channel <channel name>
	private boolean commandCreateChannel(List<String> arguments) {
		if (arguments.size() != 2) {
			System.out.println("Error: create channel command needs to follow the format #send <channel_name> message");
			return false;
		}

		try {
			/// Format: <userID> <createChannel> <channel name>
			sendToServer(String.format("<%s> <createChannel> <%s>",
					this.user.getUserName(), arguments.get(1)));
		} catch (IOException e) {
			System.out.println("Error when creating a channel");
			return false;
		}
		return true;
	}

	/// Format: #join <channel name>
	private boolean commandJoinChannel(List<String> arguments) {
		if (arguments.size() != 2) {
			System.out.println("Error: join channel command needs to follow the format #send <channel_name> message");
			return false;
		}

		try {
			/// Format: <userID> <joinChannel> <channel name>
			sendToServer(String.format("<%s> <joinChannel> <%s>", this.user.getUserName(), arguments.get(1)));
		} catch (IOException e) {
			System.out.println("Error when joining a channel");
			return false;
		}
		return true;
	}

	/// Format: #leave <channel name>
	private boolean commandLeaveChannel(List<String> arguments) {
		if (arguments.size() != 2) {
			System.out.println("Error: leave channel command needs to follow the format #send <channel_name> message");
			return false;
		}

		try {
			/// Format: <userID> <leaveChannel> <channel name>
			sendToServer(String.format("<%s> <leaveChannel> <%s>", this.user.getUserName(), arguments.get(1)));
		} catch (IOException e) {
			System.out.println("Error when leaving a channel");
			return false;
		}
		return true;
	}

	/// Format: #send <channel name> message
	private boolean commandSendChannel(List<String> arguments) {
		if (arguments.size() != 3) {
			System.out.println("Error: send to channel command needs to follow the format #send <channel_name> message");
			return false;
		}

		try {
			/// Format: <userID> <sendtochannel> <channel name> <message>
			sendToServer(String.format("<%s> <sendtochannel> <%s> <%s>",
					this.user.getUserName(), arguments.get(1), arguments.get(2)));
		} catch (IOException e) {
			System.out.println("Error when sending a channel message");
			return false;
		}
		return true;
	}
	
	//// Format: #startforwarding <userID>
	private boolean commandStartForwarding(List<String> arguments){
		if (arguments.size() != 2){
			System.out.println("Error: startforwarding needs to follow the format #startforwarding <userID>");
			return false;
		} 
		
		try {
			sendToServer (String.format("<%s> <startforwarding> <%s>", this.user.getUserName(), arguments.get(1)));
		} catch (IOException e){
			System.out.println("Error when starting to forward messages");
			return false;
		}
		return true;
	}
	
	//// Format: #cancelforwarding <userID>
	private boolean commandCancelForwarding(List<String> arguments){
		if (arguments.size() != 2){
			System.out.println("Error: cancelforwarding needs to follow the format #cancelforwarding <userID>");
			return false;
		} 
		
		try {
			sendToServer (String.format("<%s> <cancelforwarding> <%s>", this.user.getUserName(), arguments.get(1)));
		} catch (IOException e){
			System.out.println("Error when cancelling forward messages");
			return false;
		}
		return true;
	}
	
	//// Format: #whoforwardtome
	private boolean commandWhoForwardToMe(List<String> arguments){
		if (arguments.size() != 1){
			System.out.println("Error: whoforwardtome needs to follow the format #whoforwardtome");
			return false;
		} 
		
		try {
			sendToServer (String.format("<%s> <whoforwardtome>", this.user.getUserName()));
		} catch (IOException e){
			System.out.println("Error when asking whoforwardtome");
			return false;
		}
		return true;
	}
	
	//// Format: #whoforwardto
	private boolean commandWhoForwardTo(List<String> arguments){
		if (arguments.size() != 1){
			System.out.println("Error: whoforwardto needs to follow the format #whoforwardto");
			return false;
		} 
		
		try {
			sendToServer (String.format("<%s> <whoforwardto>", this.user.getUserName()));
		} catch (IOException e){
			System.out.println("Error when asking whoforwardto");
			return false;
		}
		return true;
	}

	private boolean commandUnrecognized() {
		System.out.println("Command not recognized. Please check spelling and try again.");
		return false;
	}

	private boolean noArgs(List<String> arguments) {
		if (arguments.size() > 1) {
			String command = arguments.get(0);
			System.out.println("There are no arguments needed for " + command + ". EX: #" + command + ", not #" + command + " " + arguments.get(1));
			return false;
		}
		return true;
	}

	// Terminate the client.
	private void quit() {
		try {
			closeConnection();
		} catch (IOException ignored) {
		}
		System.exit(0);
	}
}
