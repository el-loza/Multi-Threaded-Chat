import Console.Print;
import client.Status;
import client.UserProfile;
import common.IChat;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EchoServer extends AbstractServer {
	private IChat serverChatInterface;
	List<UserProfile> users;

	private final static int DEFAULT_PORT = 5555;

	/**
	 * Constructs an instance of the echo server.
	 *
	 * @param port                The port number to connect on.
	 * @param clientChatInterface The interface type variable.
	 */
	public EchoServer(int port, IChat clientChatInterface) throws IOException {
		super(port);
		this.serverChatInterface = clientChatInterface;
		this.users = new ArrayList<>();
		users.add(new UserProfile("server", new ArrayList<>(), true, null, ""));
		readProfilesFromFile(); // get profiles from the file
	}


	//~~~User Profile Methods~~~
	void addProfileToList(UserProfile newuser) {
		users.add(newuser);
	}

	void removeProfileFromList(UserProfile deleteuser) {
		users.remove(deleteuser);
	}

	// finds user based on username. Returns null if user is not found.
	UserProfile findUser(String username) {
		UserProfile user = null;
		UserProfile temp;
		for (UserProfile user1 : users) {
			temp = user1;
			if (temp.getUserName().equals(username)) {
				user = temp;
				break;
			}
		}
		return user;
	}

	// check if a user is available to talk
	boolean clientAvailToTalk(UserProfile user) {
		return user.getStatus() == Status.ONLINE || user.getStatus() == Status.IDLE;
	}

	//reads new profiles from a file and puts them into the list in this class.
	private int readProfilesFromFile() {
		String filename = "profiles.txt"; // define file name
		String line;
		String[] temp;
		String username;
		String password;
		UserProfile profile;
		BufferedReader in = null; // new buffered file reader
		try {
			in = new BufferedReader(new FileReader(filename)); // pass it our file
		} catch (FileNotFoundException e) { // error upon failing to find file
			Print.error("Error, the profiles file \"profiles.txt\" does not exist. ");
			e.printStackTrace();
		}
		try {
			while (true) {
				assert in != null;
				line = in.readLine(); // gets username and password line, structured user_id>>password
				if (line == null) { // base case, if there is nothing left in the file
					break; // stop reading file
				}
				temp = line.split(">>");
				username = temp[0];
				password = temp[1];
				profile = new UserProfile(username, new ArrayList<UserProfile>(), false, null, password); // setup the user
				addProfileToList(profile); // add user to the list
			}
		} catch (IOException e) {
			Print.error("Error reading profiles in from file. Make sure each profile name has its password directly underneath it.");
			e.printStackTrace();
		}

		return 0;
	}

	//blocking implemented for client/client
	boolean clientBlockClient(String b1, String b2) {
		UserProfile blocker = findUser(b1);
		UserProfile blockee = findUser(b2);
		if ((blocker == null) || (blockee == null)) { // if either blocker or blockee does not exist, fail.
			Print.error("Either blocker or blockee does not exist. clientBlockUser failed.");
			return false;
		}
		blocker.block(blockee); // block user
		return true;
	}

	private void sendPrivate(Object msg, UserProfile senderProfile, UserProfile receiverProfile) {
		ConnectionToClient connectionToReceiver = receiverProfile.getCTC();
		try {
			// block the client from the user profile
			if (!receiverProfile.doIBlockThisUser(senderProfile)) {
				if (!(receiverProfile.getStatus() == Status.OFFLINE) && !(receiverProfile.getStatus() == Status.UNAVAILABLE)) {
					connectionToReceiver.sendToClient(msg);
				} else {
					forwardAndSend(msg, receiverProfile);
				}
			} else {
				senderProfile.getCTC().sendToClient("Could not deliver message");
			}
		} catch (IOException ignore) {
			senderProfile.setLoginState(false);
		}
	}

	private boolean sendToClient(Object message, UserProfile userToSendTo) {
		try {
			userToSendTo.getCTC().sendToClient(message);
		} catch (IOException e) {
			userToSendTo.setLoginState(false);
			return false;
		}
		return true;
	}

	private void forwardAndSend(Object msg, UserProfile userProfile) throws IOException {
		String newMessage = "Forwarded From: " + userProfile.getUserName() + " > " + msg;
		for (UserProfile forwardee : userProfile.getUsersForwardTo()) {
			if (!(forwardee.getStatus() == Status.OFFLINE) && !(forwardee.getStatus() == Status.UNAVAILABLE)) {
				forwardee.getCTC().sendToClient(newMessage);
			}
		}

	}

	private void sendToAllClients(Object msg, UserProfile userProfile) {
		try {
			if (userProfile.getStatus() == Status.UNAVAILABLE) {
				userProfile.getCTC().sendToClient("[Server]> Set your status to available to send messages.");
			} else {
				for (UserProfile user : this.users) {
					ConnectionToClient connectionToClient = user.getCTC();
					if (connectionToClient == null)
						continue;
					try {
						// block the client from the user profile
						if (!user.doIBlockThisUser(userProfile) && !(user == userProfile)) {
							if (!(user.getStatus() == Status.OFFLINE)
									&& !(user.getStatus() == Status.UNAVAILABLE)) {
								connectionToClient.sendToClient(msg);
							} else {
								forwardAndSend(msg, user);
							}
						}

					} catch (IOException ignore) {
						userProfile.setLoginState(false);
					}
				}
			}
		} catch (IOException ignore) {
			userProfile.setLoginState(false);
		}
	}

	private void serverSendToAllClients(Object msg, UserProfile userProfile) {
		try {
			for (UserProfile userSendTo : this.users) {
				ConnectionToClient connectionToClient = userSendTo.getCTC();
				if (connectionToClient == null)
					continue;
				// block the client from the user profile
				if (!userSendTo.doIBlockThisUser(userProfile)) {
					if (!(userSendTo.getStatus() == Status.OFFLINE)) {
						sendToClient(msg, userSendTo);
					} else {
						forwardAndSend(msg, userProfile);
					}
				}

			}
		} catch (IOException ignore) {
			userProfile.setLoginState(false);
		}
	}

	private boolean sendToChannel(Object msg, UserProfile userSendingMessage, String channel) throws IOException {
		// if the channel exists and the user is subscribed to it.
		if (!userSendingMessage.doISubscribeToThisChannel(channel)) {
			userSendingMessage.getCTC().sendToClient("SERVER > You have not joined this channel.");
			return false;
		}
		if (!UserProfile.globalChannelList.contains(channel)) {
			userSendingMessage.getCTC().sendToClient("SERVER > Channel does not exist.");
			return false;
		}
		for (UserProfile userToSendTo : this.users) {
			ConnectionToClient connectionToClient = userToSendTo.getCTC();
			if (connectionToClient == null) // is not connected any more
				continue;
			// block the client from the user profile
			if (!userToSendTo.doIBlockThisUser(userSendingMessage) && !(userToSendTo == userSendingMessage)) {
				if (userToSendTo.doISubscribeToThisChannel(channel)) {
					// only send if the userToSendTo is apart of the channel you
					// are sending to.
					if (!(userToSendTo.getStatus() == Status.OFFLINE)
							&& !(userToSendTo.getStatus() == Status.UNAVAILABLE)) {
						sendToClient(msg, userToSendTo);
					} else {
						forwardAndSend(msg, userToSendTo);
					}

				}
			}
		}
		return true;
	}

	//~~~/User Profile Methods~~~

	/**
	 * This method handles any messages received from the client.
	 *
	 * @param message The message received from the client.
	 * @param client  The connection from which the message originated.
	 */
	public void handleMessageFromClient(Object message, ConnectionToClient client) {
		Print.line("Message received: " + message + " from " + client);
		// parse the userId from the message.
		String clientUserId;

		/// new standard: Every message needs to follow this format. Question marks around message is option
		/// <userID> <command> <data>
		List<String> arguments = parsers.CommandLineParser.TokenizedArguments(message.toString());

		if (arguments.size() < 2) {
			try {
				client.sendToClient("[Server] > Command does not follow correct format.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		clientUserId = arguments.get(0);

		// message received from userID, reset timer for status
		if (arguments.size() >= 2 && !arguments.get(1).equalsIgnoreCase("login")) {
			UserProfile userProfile = findUser(clientUserId);
			userProfile.resetCountdown();
			userProfile.setCTC(client);
		}

		commandsFromClient(arguments, client);
	}

	/// commands form the client.
	/// Format: <userID> <sendToAll> <message>
	private boolean clientCommand_sendToAll(List<String> arguments) {
		String messageToClients = arguments.get(0) + " > " + arguments.get(2);
		sendToAllClients(messageToClients, findUser(arguments.get(0)));
		return true;
	}

	/// Format: <userID> <sendToChannel> <channelName> <message>
	private boolean clientCommand_channelMessage(List<String> arguments) throws IOException {
		if (arguments.size() != 4)
			return false;
		UserProfile userSending = findUser(arguments.get(0));
		String toMessageClient = "Channel " + arguments.get(2) + ": " + arguments.get(0) + " > " + arguments.get(3);
		sendToChannel(toMessageClient, userSending, arguments.get(2));
		return true;
	}

	/// Format: <userID> <createChannel> <channelName>
	private boolean clientCommand_createChannel(List<String> arguments) throws IOException {
		if (arguments.size() != 3)
			return false;
		String channelName = arguments.get(2);
		UserProfile userSending = findUser(arguments.get(0));
		if (UserProfile.globalChannelList.contains(channelName)) {
			userSending.getCTC().sendToClient("[SERVER] > Channel already exists");
			return false;
		} else {
			UserProfile.globalChannelList.add(channelName);
			userSending.getCTC().sendToClient("[SERVER] > Success, created channel.");
			return true;
		}
	}

	/// Format: <userID> <joinChannel> <channelName>
	private boolean clientCommand_joinChannel(List<String> arguments) throws IOException {
		if (arguments.size() != 3)
			return false;

		String channelName = arguments.get(2);
		UserProfile userSending = findUser(arguments.get(0));

		if (!UserProfile.globalChannelList.contains(channelName)) {
			userSending.getCTC().sendToClient("[SERVER] > Channel does not exist");
			return false;
		} else {
			userSending.subscribeToChannel(channelName);
			userSending.getCTC().sendToClient("[SERVER] > Success, scribed to channel.");
			return true;
		}
	}

	/// Format: <userID> <leaveChannel> <channelName> <message>
	private boolean clientCommand_leaveChannel(List<String> arguments) throws IOException {
		if (arguments.size() != 3)
			return false;
		String channelName = arguments.get(2);
		UserProfile userSending = findUser(arguments.get(0));
		if (!UserProfile.globalChannelList.contains(channelName)) {
			userSending.getCTC().sendToClient("[SERVER] > Channel does not exist");
			return false;
		} else {
			userSending.leaveChannel(channelName);
			userSending.getCTC().sendToClient("[SERVER] > Success, left the channel.");
			return true;
		}
	}

	/// Format: <userID> <login> <password>
	private boolean clientCommand_login(ConnectionToClient client, List<String> arguments) throws IOException {
		String userID = arguments.get(0);
		// command login is at argument 1
		String password = arguments.get(2);
		UserProfile user = new UserProfile(userID, false, null, password);
		if (users.contains(user)) {
			if (findUser(userID).isLoggedIn()) {
				client.sendToClient("[Server]> Someone is already logged in under this profile");
				client.close();
				return false;
			} else if (!findUser(userID).getPassword().equals(password)) {
				client.sendToClient("[Server]> Password Mismatch. Please check your username or password.");
				client.close();
				return false;
			} else {
				this.findUser(userID).setLoginState(true);
				this.findUser(userID).setCTC(client);
				this.findUser(userID).setStatus(Status.ONLINE);
				this.findUser(userID).startCountdown();
				client.sendToClient("[Server]> Connection Successful");
				return true;
			}
		} else {
			client.sendToClient("This username <" + userID + "> is not affiliated with this server.");
			client.close();
			//users.add(user); // removed since user profiles are now read in from the file.
			return false; // return false because the user did not exist
		}
	}

	/// Format: <userID> <logoff>
	private boolean clientCommand_logoff(List<String> arguments, ConnectionToClient ctc) {
		String userID = arguments.get(0);
		UserProfile client = findUser(userID);
		client.setLoginState(false);
		try {
			client.handleLogoff();
			ctc.close();
			client.getCTC().close();
		} catch (IOException e) {
			System.out.println("Error: Could not close client connection");
			return false;
		}
		return true;
	}

	/// Format: <userID> <block> <userIDToBlock>
	private boolean clientCommand_block(ConnectionToClient client, List<String> arguments) throws IOException {
		String blockWho = arguments.get(2);
		String userID = arguments.get(0);

		if (findUser(blockWho) == null) {
			client.sendToClient("[Server]> Blockee: " + blockWho + "does not exist as a Client user");
		} else if (blockWho.equals(userID)) {
			client.sendToClient("[Server]> Can not block yourself");
		} else if (findUser(userID).doIBlockThisUser(findUser(blockWho))) {
			client.sendToClient("[Server]> You are already blocking this user.");
		} else {
			findUser(userID).block(findUser(blockWho));
			client.sendToClient("[Server]> Blockee: " + blockWho + " sucessfully blocked");
		}
		return true;
	}

	/// Format: <userID> <unblock> <userIDToUnblock>
	private boolean clientCommand_unblock(ConnectionToClient client, List<String> arguments) throws IOException {
		String userID = arguments.get(0);
		String unblockWho = arguments.get(2);
		Print.line(unblockWho);
		if (findUser(unblockWho) == null) {
			client.sendToClient("[Server]> unBlockee: " + unblockWho + "does not exist as a Client user");
		} else if (unblockWho.equals(userID)) {
			client.sendToClient("[Server]> Can not unblock yourself");
		} else if (!findUser(userID).doIBlockThisUser(findUser(unblockWho))) {
			client.sendToClient("[Server]> You did not have this user blocked.");
		} else {
			findUser(userID).unblock(findUser(unblockWho));
			client.sendToClient("[Server]> Blockee: " + unblockWho + " sucessfully unblocked");
		}
		return true;
	}

	/// Format: <userID> <whoIBlock>
	private boolean clientCommand_whoIBlock(ConnectionToClient client, List<String> arguments) throws IOException {
		String whoIamBlocking = "";
		List<UserProfile> blockedP = findUser(arguments.get(0)).getBlockingList();
		for (UserProfile aBlockedP : blockedP) {
			whoIamBlocking += aBlockedP.toString() + "\n";
		}
		client.sendToClient(whoIamBlocking);
		return true;
	}

	/// Format: <userID> <whoBlocksMe>
	private boolean clientCommand_whoBlocksMe(ConnectionToClient client, List<String> arguments) throws IOException {
		String whoBlocksMe = "";
		for (UserProfile user : users) {
			if (user.doIBlockThisUser(findUser(arguments.get(0)))) {
				whoBlocksMe += user.toString() + "\n";
			}
		}
		client.sendToClient(whoBlocksMe);
		return true;
	}

	/// Format: <userID> <available>
	private boolean clientCommand_available(ConnectionToClient client, List<String> arguments) throws IOException {
		findUser(arguments.get(0)).setStatus(Status.ONLINE);
		client.sendToClient("Your status has been set to \"Available\"");
		return true;
	}

	/// Format: <userID> <unavailable>
	private boolean clientCommand_unavailable(ConnectionToClient client, List<String> arguments) throws IOException {
		findUser(arguments.get(0)).setStatus(Status.UNAVAILABLE);
		client.sendToClient("Your status has been set to \"Unavailable\"");
		return true;
	}

	/// Format: <userID> <status> <userID/Channel Status>
	private boolean clientCommand_status(ConnectionToClient client, List<String> arguments) throws IOException {
		String target = arguments.get(2);
		UserProfile profile = findUser(target);
		//target is a user
		if (profile != null) {
			client.sendToClient("[Server]> " + profile.getUserName() + " is " + profile.getStatus());
			return true;
		}
		//check if target is a channel
		else if (UserProfile.globalChannelList.contains(target)) {
			//check if user requesting status is a member of the channel
			UserProfile requester = findUser(arguments.get(0));
			if (requester.doISubscribeToThisChannel(target)) {
				for (UserProfile u : users) {
					//user belongs to the channel, send original user status of all channel members
					if (u.doISubscribeToThisChannel(target)) {
						client.sendToClient(u.getUserName() + " is " + u.getStatus() + ".");
					}
				}
				return true;
			}
			return false;
		} else {
			return false;
		}
	}

	/// Format: <userID> <privateMessage> <userIDToWho> <message>
	private boolean clientCommand_private(ConnectionToClient client, List<String> arguments) throws IOException {
		String toUserID = arguments.get(2);
		String toMessage = arguments.get(3);
		UserProfile sender = findUser(arguments.get(0));

		if (findUser(toUserID) == null) {
			String toClientMessage = toUserID + " does not exist as a valid user, please check your user_id and check again";
			client.sendToClient(toClientMessage);
			return false;
		} else {
			UserProfile receiver = findUser(toUserID);
			toMessage = "private from: " + arguments.get(0) + " > " + toMessage;
			sendPrivate(toMessage, sender, receiver);
			return true;
		}

	}

	// Format: <userID> <startforwarding> <userIDForwardingTo>
	private boolean clientCommand_startForwarding(ConnectionToClient client, List<String> arguments) throws IOException {
		String forwarderID = arguments.get(0);
		String forwardeeID = arguments.get(2);
		UserProfile forwarder = findUser(forwarderID);
		UserProfile forwardee = findUser(forwardeeID);
		String messageToClient;
		if(forwardee.equals(forwarder)){
			client.sendToClient("Unfortunately, you cannot forward to yourself.");
			return false;
		}
		if(forwardee == null || !users.contains(forwardee)){
			messageToClient = forwardeeID + " is not a valid userID, please check your input and try again";
			client.sendToClient(messageToClient);
			return false;
		} else if (forwardee.getStatus() == Status.OFFLINE || forwardee.getStatus() == Status.UNAVAILABLE) {
			messageToClient = forwardeeID + " is not online or is unavailable, can not forward messages.";
			client.sendToClient(messageToClient);
			return false;
		} else if (forwarder.getUsersForwardTo().contains(forwardee)) {
			messageToClient = "You are already forwarding messages to " + forwardeeID + ".";
			client.sendToClient(messageToClient);
			return false;
		} else {
			if (forwarder.isForwardee()) {
				messageToClient = "You are currently receiving forwarded messages, cannot forward messages when currently receiving messages.";
				client.sendToClient(messageToClient);
				return false;
			} else if (forwardee.isForwarder()) {
				messageToClient = forwardeeID +
						" is already forwarding messages to other clients. Cannot forward messages to clients already forwarding messages.";
				client.sendToClient(messageToClient);
				return false;
			} else {
				forwarder.forwardTo(forwardee);
				messageToClient = "Forwarding messages to: " + forwardeeID;
				client.sendToClient(messageToClient);
				return true;
			}
		}
	}

	// Format: <userID> <cancelforwarding> <userIDForwardingTo>
	private boolean clientCommand_cancelForwarding(ConnectionToClient client, List<String> arguments) throws IOException {
		String forwarderID = arguments.get(0);
		String forwardeeID = arguments.get(2);
		UserProfile forwarder = findUser(forwarderID);
		UserProfile forwardee = findUser(forwardeeID);
		String messageToClient;
		if (forwardee == null) {
			messageToClient = forwardeeID + " is not a valid userID, please check your input and try again";
			client.sendToClient(messageToClient);
			return false;
		} else if (!forwarder.getUsersForwardTo().contains(forwardee)) {
			messageToClient = "You are not forwarding messages to " + forwardeeID + ", please check and try again.";
			client.sendToClient(messageToClient);
			return false;
		} else {
			forwarder.cancelForwardTo(forwardee);
			messageToClient = "Cancelled forwarding messages to " + forwardeeID + ".";
			client.sendToClient(messageToClient);
			return true;
		}
	}

	// Format: <userID> <whoforwardto>
	private boolean clientCommand_whoForwardTo(ConnectionToClient client, List<String> arguments) throws IOException {
		String forwarderID = arguments.get(0);
		UserProfile forwarder = findUser(forwarderID);
		for (UserProfile user : forwarder.getUsersForwardTo()) {
			client.sendToClient(user.getUserName());
		}
		return true;
	}

	// Format: <userID> <whoforwardtome>
	private boolean clientCommand_whoForwardToMe(ConnectionToClient client, List<String> arguments) throws IOException {
		String forwarderID = arguments.get(0);
		UserProfile forwarder = findUser(forwarderID);
		for (UserProfile user : forwarder.getUsersForwardToMe()) {
			client.sendToClient(user.getUserName());
		}
		return true;
	}


	void commandsFromClient(List<String> arguments, ConnectionToClient client) {
		try {
			String command = arguments.get(1).toLowerCase();
			switch (command) {
				case "sendtoall":
					clientCommand_sendToAll(arguments);
					break;
				case "sendtochannel":
					clientCommand_channelMessage(arguments);
					break;
				case "createchannel":
					clientCommand_createChannel(arguments);
					break;
				case "joinchannel":
					clientCommand_joinChannel(arguments);
					break;
				case "leavechannel":
					clientCommand_leaveChannel(arguments);
					break;
				case "login":
					clientCommand_login(client, arguments);
					break;
				case "logoff":
				case "logout":
					clientCommand_logoff(arguments, client);
					break;
				case "block":
					clientCommand_block(client, arguments);
					break;
				case "unblock":
					clientCommand_unblock(client, arguments);
					break;
				case "whoiblock":
					clientCommand_whoIBlock(client, arguments);
					break;
				case "whoblocksme":
					clientCommand_whoBlocksMe(client, arguments);
					break;
				case "available":
					clientCommand_available(client, arguments);
					break;
				case "unavailable":
					clientCommand_unavailable(client, arguments);
					break;
				case "status":
					clientCommand_status(client, arguments);
					break;
				case "private":
					clientCommand_private(client, arguments);
					break;
				case "startforwarding":
					clientCommand_startForwarding(client, arguments);
					break;
				case "cancelforwarding":
					clientCommand_cancelForwarding(client, arguments);
					break;
				case "whoforwardtome":
					clientCommand_whoForwardToMe(client, arguments);
					break;
				case "whoforwardto":
					clientCommand_whoForwardTo(client, arguments);
					break;

			}

		} catch (Exception ignore) {
		}
	}


	void handleMessageFromServerUI(String message) {
		if (message.charAt(0) == '#') {
			serverCommands(message.substring(1));
		} else {
			serverChatInterface.display(message);
			this.serverSendToAllClients("SERVER MESSAGE> " + message, findUser("server"));
		}
	}

	private int serverCommands(String message) {
		if (message.equals("quit")) {
			try {
				this.close();
			} catch (IOException e) {
				sendToAllClients("#serverClosed");
				Print.error("Could not gracefully close, check port settings after termination");
			}
			System.exit(0);
		} else if (message.equals("stop")) {
			this.stopListening();
		} else if (message.equals("close")) {
			try {
				for (UserProfile userProfile : this.users) {
					userProfile.setLoginState(false);
				}
				sendToAllClients("#serverClosed");
				this.close();
			} catch (IOException e) {
				Print.error("Could not gracefully close, check port settings after termination");
			}
		} else if (message.startsWith("setport")) {
			if (message.length() <= 7) { // checks to make sure there are inputs
				System.out.println("Error, please provide a port that you want to change to.");
				return -1;
			}
			String portNum = message.substring(7).trim();
			if (portNum.charAt(0) == '<' && portNum.charAt(portNum.length() - 1) == '>') {
				portNum = portNum.substring(1, portNum.length() - 1);
			} else {
				serverCommandUsage(message);
			}

			if (getNumberOfClients() == 0 && !isListening()) {
				this.setPort(Integer.parseInt(portNum));
				serverChatInterface.display("Server port changed to " + portNum);
			} else {
				System.out.println("Number of clients connected:" + getNumberOfClients());
				serverChatInterface.display("The server is not closed, can only change port when server is closed.");
			}


		} else if (message.equals("getport")) {
			serverChatInterface.display("Current port is: " + Integer.toString(getPort()));
		} else if (message.equals("start")) {
			if (!this.isListening()) {
				try {
					listen();
				} catch (Exception e) {
					serverChatInterface.display("Error: Could not listen for clients!!");
				}
			} else {
				serverChatInterface.display("Error: Already Listening for clients!!");
			}
		} else {
			serverCommandUsage(message);
		}
		return 0;
	}

	private void serverCommandUsage(String message) {
		serverChatInterface.display(message + " is not a valid command. Use a hashtag to prefix all commands and then use on of the following commands: #quit, #stop,"
				+ " #close, #setport <port>, #getport, #start.");
	}

	protected void serverStarted() {
		Print.line("Server listening for connections on port " + getPort());
	}

	protected void serverStopped() {
		Print.line("Server has stopped listening for connections.");
	}

	public static void main(String[] args) {
		ServerConsole chat = new ServerConsole(DEFAULT_PORT);
		chat.accept(); // Wait for console data
	}

}
//End of EchoServer class
