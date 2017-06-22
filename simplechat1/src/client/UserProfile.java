/**
 *
 */
package client;

import ocsf.server.ConnectionToClient;

import java.util.ArrayList;
import java.util.List;

/**
 * @author williamhinz
 */
public class UserProfile {

	//PRIVATE VARIABLES
	private String username;
	private List<UserProfile> usersIBlock;
	private List<String> channelList;
	private List<UserProfile> forwardTo;
	private List<UserProfile> forwardToMe;
	private boolean isLoggedIn;
	private Status status;
	private ConnectionToClient ctc;
	private String password;
	private StatusTimer timer;


	public static List<String> globalChannelList = new ArrayList<>();

	//CONSTRUCTORS
	public UserProfile(String username, boolean isLoggedIn, ConnectionToClient ctc, String password) {
		this.username = username;
		this.usersIBlock = new ArrayList<>();
		this.channelList = new ArrayList<>();
		this.forwardTo = new ArrayList<>();
		this.forwardToMe = new ArrayList<>();
		this.isLoggedIn = isLoggedIn;
		this.status = Status.OFFLINE;
		this.ctc = ctc;
		this.password = password;

		//initialize timer, it won't be started until the user has logged in
		//the timer thread will also be stopped once the user has logged out
		this.timer = new StatusTimer();
	}

	public UserProfile(String username, ArrayList<UserProfile> usersIBlock, boolean isLoggedIn, ConnectionToClient ctc, String password) {
		this.username = username;
		this.usersIBlock = usersIBlock;
		this.channelList = new ArrayList<>();
		this.forwardTo = new ArrayList<>();
		this.forwardToMe = new ArrayList<>();
		this.isLoggedIn = isLoggedIn;
		this.status = Status.OFFLINE;
		this.ctc = ctc;
		this.password = password;

		//initialize timer, it won't be started until the user has logged in
		//the timer thread will also be stopped once the user has logged out
		this.timer = new StatusTimer();
	}

	//GETTERS
	public String getUserName() {
		return username;
	}

	public List<UserProfile> getBlockingList() {
		return usersIBlock;
	}

	public boolean doISubscribeToThisChannel(String channelInQuestion) {
		return channelList.contains(channelInQuestion);
	}

	public boolean doIBlockThisUser(UserProfile userinquestion) {
		return usersIBlock.contains(userinquestion);
	}

	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	public ConnectionToClient getCTC() {
		return ctc;
	}

	public String getPassword() {
		return password;
	}

	public synchronized Status getStatus() {
		if (timer.isOverTime())
			status = Status.IDLE;
		return status;
	}

	public List<UserProfile> getUsersForwardTo() {
		return forwardTo;
	}

	public List<UserProfile> getUsersForwardToMe() {
		return forwardToMe;
	}

	public boolean isForwarder() {
		return !forwardTo.isEmpty();
	}

	public boolean isForwardee() {
		return !forwardToMe.isEmpty();
	}

	//SETTERS
	private void setBlockingList(ArrayList<UserProfile> newBlockingList) {
		usersIBlock = newBlockingList;
	}

	/*private void setChannelList(ArrayList<String> newChannelList) { // currently unused
		channelList = newChannelList;
	}*/

	public void block(UserProfile blockUser) {
		if (!doIBlockThisUser(blockUser))
			usersIBlock.add(blockUser);
	}

	public void unblock(UserProfile unblockUser) {
		usersIBlock.remove(unblockUser);
	}

	public synchronized void setStatus(Status newStatus) {
		status = newStatus;
	}

	public void setLoginState(Boolean loginState) {
		if (isLoggedIn && !loginState) {
			this.resetBlockingList();
		}
		isLoggedIn = loginState;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	private void resetBlockingList() {
		this.setBlockingList(new ArrayList<>());
	}

	public void setCTC(ConnectionToClient ctc) {
		this.ctc = ctc;
	}

	public void startCountdown() {
		this.timer.reset();
	}

	public void resetCountdown() {
		this.timer.reset();
	}

	public void handleLogoff() {
		this.status = Status.OFFLINE;
	}

	public void forwardTo(UserProfile user) {
		this.forwardTo.add(user);
		user.forwardToMe.add(this);
	}

	public void cancelForwardTo(UserProfile user) {
		this.forwardTo.remove(user);
		user.forwardToMe.remove(this);
	}

	@Override
	public String toString() {
		return username;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserProfile other = (UserProfile) obj;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equalsIgnoreCase(other.username))
			return false;
		return true;
	}

	public void subscribeToChannel(String channelName) {
		if (!doISubscribeToThisChannel(channelName)) {
			this.channelList.add(channelName);
		}
	}

	public void leaveChannel(String channelName) {
		if (doISubscribeToThisChannel(channelName)) {
			this.channelList.remove(channelName);
		}
	}
}
