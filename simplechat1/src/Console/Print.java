package Console;

import java.util.ArrayList;
import java.util.List;

/// Created by trevor on 11/2/16.
public class Print {
	public static boolean testing = false;

	public static boolean HasError = false;
	public static List<String> consoleLines = new ArrayList<>();
	public static List<String> errors = new ArrayList<>();

	public static void line(String line) {
		if (testing) {
			consoleLines.add(line);
		} else {
			System.out.println(line);
		}
	}

	public static void error(String line) {
		HasError = true;
		if (testing) {
			errors.add(line);
		} else {
			System.out.println(line);
		}
	}

	public static void initTesting() {
		testing = true;
		HasError = false;
		consoleLines.clear();
		errors.clear();
	}
}
