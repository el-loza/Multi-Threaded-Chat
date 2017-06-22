import Console.Print;
import Console.UnitTestChat;
import client.ChatClient;
import client.Status;
import client.UserProfile;
import common.IChat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class EchoServer_Test {

	private EchoServer server;
	private ChatClient client;

	public EchoServer_Test() {
	}

	@Before
	public void InitializeServer() throws InterruptedException {
		Thread.sleep(5); // this is needed.
		Print.initTesting();
		IChat serverDisplay = new UnitTestChat();
		IChat clientDisplay = new UnitTestChat();
		try {
			server = new EchoServer(5555, serverDisplay);
			server.users = new ArrayList<>();
			server.listen();

			client = new ChatClient("localhost", 5555, clientDisplay);
			client.tryToConnectTimeout();

		} catch (IOException e) {
			assertFalse(true); // fail
		}
	}

	@Test
	public void a1OneLine() {
		// a1 because it needs to run first.
		// one line for the server has listened on x port
		try {
			Thread.sleep(25);
			if (Print.consoleLines.size() > 1)
				System.out.print(Print.consoleLines);
			assertEquals(1, Print.consoleLines.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void InitTest() {
		assertTrue(server != null);
		assertTrue(client != null);
	}

	@Test
	public void NoErrors() {
		assertFalse(Print.HasError);
	}

	@Test
	public void AddNewUser() {
		server.addProfileToList(new UserProfile("", false, null, ""));
		assertTrue(server.users.size() == 1);
	}

	@Test
	public void RemoveUser() {
		UserProfile profile = new UserProfile("", false, null, "");
		server.addProfileToList(profile);
		server.removeProfileFromList(profile);
		assertTrue(server.users.size() == 0);
	}

	@Test
	public void FindUser() {
		UserProfile profile = new UserProfile("username", false, null, "");
		server.addProfileToList(profile);
		UserProfile find = server.findUser("username");
		assertNotNull(find);
	}

	@Test
	public void FindNullUser() {
		UserProfile find = server.findUser("username");
		assertNull(find);
	}

	@Test
	public void OfflineUser() {
		UserProfile profile = new UserProfile("username", false, null, "");
		server.addProfileToList(profile);
		assertFalse(server.clientAvailToTalk(profile));
	}

	@Test
	public void UserProfilesNotBlockEachOther() {
		UserProfile profile = new UserProfile("username", false, null, "");
		UserProfile profile2 = new UserProfile("username2", false, null, "");
		server.addProfileToList(profile);
		server.addProfileToList(profile2);
		assertFalse(profile.doIBlockThisUser(profile2));
	}

	@Test
	public void UserProfilesBlockEachOther() {
		UserProfile profile = new UserProfile("username", false, null, "");
		UserProfile profile2 = new UserProfile("username2", false, null, "");
		server.addProfileToList(profile);
		server.addProfileToList(profile2);
		server.clientBlockClient(profile.getUserName(), profile2.getUserName());

		assertTrue(profile.doIBlockThisUser(profile2));
	}

	@Test
	public void CheckErrorUserBlockOther() {
		UserProfile profile = new UserProfile("username", false, null, "");
		server.addProfileToList(profile);
		server.clientBlockClient(profile.getUserName(), null);
		assertTrue(Print.HasError);
	}

	@Test
	public void CheckErrorNotUserBlockOther() {
		UserProfile profile = new UserProfile("username", false, null, "");
		server.addProfileToList(profile);
		server.clientBlockClient(profile.getUserName(), "not a user");
		assertTrue(Print.HasError);
	}

	@Test
	public void CheckNoErrorUserBlockOther() {
		UserProfile profile = new UserProfile("username", false, null, "");
		UserProfile profile2 = new UserProfile("username2", false, null, "");
		server.addProfileToList(profile);
		server.addProfileToList(profile2);
		server.clientBlockClient(profile.getUserName(), profile2.getUserName());
		assertFalse(Print.HasError);
	}

	@Test
	public void CommandSendToAll() throws InterruptedException, IOException {
		UserProfile profile = new UserProfile("username", false, null, "");
		UserProfile profile2 = new UserProfile("username2", false, null, "");
		server.addProfileToList(profile);
		server.addProfileToList(profile2);
		String message = "<userID> <sendToAll> <message>";
		client.sendToServer(message);
		Thread.sleep(25);
		assertEquals(2, Print.consoleLines.size());
	}

	@Test
	public void CommandSendToChannel() throws InterruptedException, IOException {
		UserProfile profile = new UserProfile("username", false, null, "");
		UserProfile profile2 = new UserProfile("username2", false, null, "");
		server.addProfileToList(profile);
		server.addProfileToList(profile2);
		String message = "<username> <sendtochannel> <message>";
		client.sendToServer(message);
		Thread.sleep(25);
		assertEquals(2, Print.consoleLines.size());
	}

	@Test
	public void CommandCreateChannel() throws InterruptedException, IOException {
		UserProfile profile = new UserProfile("trevor", false, null, "cookies");
		server.addProfileToList(profile);
		client.sendToServer("<trevor> <login> <cookies>");
		Thread.sleep(25);
		client.sendToServer("<trevor> <createchannel> <channelName>");
		Thread.sleep(25);
		assertEquals(5, Print.consoleLines.size());
	}

	@Test
	public void CommandStatus() throws InterruptedException, IOException {
		UserProfile profile = new UserProfile("trevor", false, null, "cookies");
		server.addProfileToList(profile);
		client.sendToServer("<trevor> <login> <cookies>");
		Thread.sleep(50);
		assertEquals(profile.getStatus(), Status.ONLINE);
	}

	@Test
	public void ClientsLoginLogoffActiveCount() throws InterruptedException, IOException {
		UserProfile profile = new UserProfile("trevor", false, null, "cookies");
		server.addProfileToList(profile);
		String loginMessage = "<trevor> <login> <cookies>";
		String logoffMessage = "<trevor> <logoff>";

		client.sendToServer(loginMessage);
		Thread.sleep(25);
		client.sendToServer(logoffMessage);
		Thread.sleep(25);

		client.tryToConnectTimeout();
		client.sendToServer(loginMessage);
		Thread.sleep(25);
		client.sendToServer(logoffMessage);
		Thread.sleep(25);

		assertEquals(0, server.getNumberOfClients());
	}

	@After
	public void DestroyServer() {
		try {
			server.close();
			client.closeConnection();
		} catch (IOException e) {
			assertFalse(true); // fail
		}
	}
}
