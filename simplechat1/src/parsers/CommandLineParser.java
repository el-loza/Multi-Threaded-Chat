/// Created by trevor on 10/29/16.
package parsers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class CommandLineParser {

	private static List<String> ListFromArray(String[] array) {
		List<String> result = new ArrayList<>();
		Collections.addAll(result, array);
		return result;
	}

	public static List<String> TokenizedArguments(String input) {
		List<String> list;
		list = ListFromArray(input.split("<"));
		List<String> tempList = new ArrayList<>();
		for (String aList : list) {
			String[] otherResult = aList.split(">");
			Collections.addAll(tempList, otherResult);
		}
		return tempList.stream().filter(aTempList -> !aTempList.trim().isEmpty()).map(String::trim).collect(Collectors.toList());
	}

	public static List<String> SpaceDelimitedArguments(String input) {
		return ListFromArray(input.split(" "));
	}

	public static List<String> SpaceDelimitedArguments(String input, int argsExpected) {
		if (argsExpected == 1 || argsExpected == 0) {
			List<String> result = new ArrayList<>();
			result.add(input);
			return result;
		}
		if (!input.contains("<") && !input.contains(">")) {
			if (argsExpected == -1)
				return ListFromArray(input.split(" "));
			return ListFromArray(input.split(" ", argsExpected));
		}
		return new ArrayList<>();
	}
}
