package client;

public class StatusTimer {

	private long startCheckTime;

	//constructor
	StatusTimer() {
		reset();
	}

	void reset() {
		startCheckTime = System.currentTimeMillis();
	}

	boolean isOverTime() {
		return isOverTime(300000);
	}

	boolean isOverTime(long idleTime) {
		return startCheckTime + idleTime < System.currentTimeMillis();
	}
}
