/// Created by trevor on 10/29/16.

import Console.Print;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

public class Print_Test {

	public Print_Test() {
		Print.testing = true;
	}

	@Before
	public void setUp() {
		Print.HasError = false;
		Print.consoleLines = new ArrayList<>();
		Print.errors = new ArrayList<>();
		Print.testing = true;
	}

	@Test
	public void No_Errors() {
		Print.line("print");
		assertTrue(!Print.HasError);
	}

	@Test
	public void One_line() {
		Print.line("print");
		assertTrue(Print.consoleLines.size() == 1);
	}

	@Test
	public void Has_Error() {
		Print.error("there is an error");
		assertTrue(Print.HasError);
		Print.HasError = false;
	}

	@Test
	public void HasOne_Error() {
		Print.error("there is an error");
		assertTrue(Print.errors.size() == 1);
	}

}
