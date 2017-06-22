package client;
public enum Status{
	ONLINE, IDLE, UNAVAILABLE, OFFLINE;
	
	@Override
	  public String toString() {
	    switch(this) {
	    	case ONLINE: return "online";
	    	case IDLE: return "idle";
	    	case UNAVAILABLE: return "unavailable";
	    	case OFFLINE: return "offline";
	    	default: throw new IllegalArgumentException();
	    }
	}
}