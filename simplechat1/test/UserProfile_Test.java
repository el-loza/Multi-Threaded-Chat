
import client.UserProfile;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class UserProfile_Test {

	public UserProfile_Test() {
	}

	@Test
	public void constructorTest() {
		//constructor test
		//public UserProfile(String username, ArrayList<UserProfile> usersIBlock, boolean isLoggedIn, ConnectionToClient ctc, String password) {
		UserProfile user = new UserProfile("will", new ArrayList<>(), true, null, "1234");
		assertEquals(user.getUserName(), "will");
		assertEquals(user.getBlockingList(), new ArrayList<UserProfile>());
		assertEquals(user.getCTC(), null);
		assertEquals(user.getPassword(), "1234");
		assertTrue(user.isLoggedIn());
	}

	@Test
	public void constructorTest2() {
		UserProfile user = new UserProfile("will", true, null, "1234");
		assertEquals(user.getUserName(), "will");
		assertEquals(user.getBlockingList(), new ArrayList<UserProfile>());
		assertEquals(user.getCTC(), null);
		assertEquals(user.getPassword(), "1234");
		assertTrue(user.isLoggedIn());
	}

	@Test
	public void usernameTest() {
		UserProfile user = new UserProfile("will", new ArrayList<>(), true, null, "1234");
		assertEquals(user.getUserName(), "will");
	}

	@Test
	public void blockingListTest() {
		UserProfile user = new UserProfile("will", new ArrayList<>(), true, null, "1234");
		UserProfile user2 = new UserProfile("will", new ArrayList<>(), true, null, "1234");
		user.block(user2);
		assertEquals(user.getBlockingList().get(0), user2);
	}

	@Test
	public void channelTest() {
		UserProfile user = new UserProfile("will", new ArrayList<>(), true, null, "1234");
		String channel = "bludeChanel";
		user.subscribeToChannel(channel);
		assertTrue(user.doISubscribeToThisChannel(channel));
		user.leaveChannel(channel);
		assertTrue(!user.doISubscribeToThisChannel(channel));
	}

	@Test
	public void userBlockingTest() {
		UserProfile user = new UserProfile("will", new ArrayList<>(), true, null, "1234");
		UserProfile user2 = new UserProfile("hinz", new ArrayList<>(), true, null, "1234");
		user.block(user2);
		assertTrue(user.doIBlockThisUser(user2));
		user.unblock(user2);
		assertTrue(!user.doIBlockThisUser(user2));
	}

	@Test
	public void loggedInTest() {
		UserProfile user = new UserProfile("will", new ArrayList<>(), true, null, "1234");
		assertTrue(user.isLoggedIn());
		user.setLoginState(false);
		assertTrue(!user.isLoggedIn());
	}

	@Test
	public void connectionToClientTest() {
		UserProfile user = new UserProfile("will", new ArrayList<>(), true, null, "1234");
		assertEquals(user.getCTC(), null);
		user.setCTC(null);
		assertEquals(user.getCTC(), null);
	}

	@Test
	public void passwordTest() {
		UserProfile user = new UserProfile("will", new ArrayList<>(), true, null, "1234");
		assertEquals(user.getPassword(), "1234");
		user.setPassword("5678");
		assertEquals(user.getPassword(), "5678");
	}

	@Test
	public void statusTest() {
		UserProfile user = new UserProfile("will", new ArrayList<>(), true, null, "1234");
		assertEquals(user.getStatus(), client.Status.OFFLINE);
		user.setStatus(client.Status.ONLINE);
		assertEquals(user.getStatus(), client.Status.ONLINE);
	}

	@Test
	public void forwardingTest() {
		UserProfile user = new UserProfile("will", new ArrayList<>(), true, null, "1234");
		UserProfile user2 = new UserProfile("hinz", new ArrayList<>(), true, null, "1234");
		ArrayList<UserProfile> temp = new ArrayList<>();
		assertEquals(user.getUsersForwardTo(), temp);
		assertEquals(user2.getUsersForwardToMe(), temp);
		user.forwardTo(user2);
		temp.add(user2);
		assertEquals(user.getUsersForwardTo(), temp);
		temp.remove(0);
		temp.add(user);
		assertEquals(user2.getUsersForwardToMe(), temp);
		assertTrue(user.isForwarder());
		assertTrue(user2.isForwardee());
		assertTrue(!user.isForwardee());
		assertTrue(!user2.isForwarder());
		user.cancelForwardTo(user2);
		temp.remove(0);
		assertEquals(user.getUsersForwardTo(), temp);
	}

	@Test
	public void equalsTest() {
		UserProfile user = new UserProfile("will", new ArrayList<>(), true, null, "1234");
		UserProfile user2 = new UserProfile("will", new ArrayList<>(), true, null, "1234");
		UserProfile user3 = new UserProfile("hinz", new ArrayList<>(), true, null, "1234");
		assertTrue(user.equals(user2));
		assertTrue(!user.equals(user3));
		assertNotNull(user);
		UserProfile user5 = new UserProfile(null, new ArrayList<>(), true, null, "1234");
		assertTrue(!user5.equals(user));
	}

	@Test
	public void timerTest() {
		UserProfile user = new UserProfile("will", new ArrayList<>(), true, null, "1234");
		user.startCountdown();
	}

	@Test
	public void toStringTest() {
		UserProfile user = new UserProfile("will", new ArrayList<>(), true, null, "1234");
		assertEquals(user.toString(), "will");
	}

}



