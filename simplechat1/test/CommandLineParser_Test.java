/// Created by trevor on 10/29/16.

import org.junit.Test;
import parsers.CommandLineParser;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CommandLineParser_Test {

	public CommandLineParser_Test() {

	}

	@Test
	public void SpaceArgNoLimit_OneTest() {
		// one args test
		List<String> args = CommandLineParser.SpaceDelimitedArguments("test");
		assertTrue(args.size() == 1);
		assertTrue(args.get(0).equals("test"));
	}

	@Test
	public void SpaceArgNoLimit_TwoTest() {
		// one args test
		List<String> args = CommandLineParser.SpaceDelimitedArguments("test test1");
		assertTrue(args.size() == 2);
		assertTrue(args.get(0).equals("test"));
		assertTrue(args.get(1).equals("test1"));
	}

	@Test
	public void SpaceArg_OneTest() {
		// one args test
		List<String> args = CommandLineParser.SpaceDelimitedArguments("test", 2);
		assertTrue(args.size() == 1);
		assertTrue(args.get(0).equals("test"));
	}

	@Test
	public void SpaceArg_TwoTest() {
		// two args test
		List<String> args = CommandLineParser.SpaceDelimitedArguments("test tester", 2);
		assertTrue(args.size() == 2);
		assertTrue(args.get(0).equals("test"));
		assertTrue(args.get(1).equals("tester"));
	}

	@Test
	public void SpaceArg_OneLimitTwoTest() {
		// two args test
		List<String> args = CommandLineParser.SpaceDelimitedArguments("test tester tester3", 2);
		assertTrue(args.size() == 2);
		assertTrue(args.get(0).equals("test"));
		assertTrue(args.get(1).equals("tester tester3"));
	}

	@Test
	public void SpaceArg_WithLessThanGreaterThan() {
		// one args test
		List<String> args = CommandLineParser.SpaceDelimitedArguments("<test>", 1);
		assertTrue(args.size() == 1);
		assertEquals(args.get(0), "<test>");
	}

	@Test
	public void TokenArgs_OneArg() {
		// one args test
		List<String> args = CommandLineParser.TokenizedArguments("<test>");
		assertTrue(args.size() == 1);
		assertTrue(args.get(0).equals("test"));
	}

	@Test
	public void TokenArgs_TwoArg() {
		// one args test
		List<String> args = CommandLineParser.TokenizedArguments("<test> <test2>");
		assertTrue(args.size() == 2);
		assertTrue(args.get(0).equals("test"));
		assertTrue(args.get(1).equals("test2"));
	}

	@Test
	public void TokenArgs_TwoNoTokenArg() {
		// one args test
		List<String> args = CommandLineParser.TokenizedArguments("test test2");
		assertTrue(args.size() == 1);
		assertTrue(args.get(0).equals("test test2"));
	}

	@Test
	public void TokenArgs_ThreeBetweenTokenArg() {
		// one args test
		List<String> args = CommandLineParser.TokenizedArguments("test <test2> test3");
		assertTrue(args.size() == 3);
		assertTrue(args.get(0).equals("test"));
		assertTrue(args.get(1).equals("test2"));
		assertTrue(args.get(2).equals("test3"));
	}

	@Test
	public void TokenArgs_ThreeOddNumberOfToken_TokenArg() {
		// one args test
		List<String> args = CommandLineParser.TokenizedArguments("test test2> test3");
		assertTrue(args.size() == 2);
		assertTrue(args.get(0).equals("test test2"));
		assertTrue(args.get(1).equals("test3"));
	}

	@Test
	public void TokenArgs_ThreeSpaces_TokenArg() {
		// one args test
		List<String> args = CommandLineParser.TokenizedArguments("test <test 2> test3");
		assertTrue(args.size() == 3);
		assertTrue(args.get(0).equals("test"));
		assertTrue(args.get(1).equals("test 2"));
		assertTrue(args.get(2).equals("test3"));
	}
}
