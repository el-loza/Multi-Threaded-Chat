package Console;

import common.IChat;

///  Created by trevor on 11/2/16.
public class UnitTestChat implements IChat {

	@Override
	public void display(String message) {
		Print.line(message);
	}
}
