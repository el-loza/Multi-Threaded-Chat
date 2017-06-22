package cs314.p3;

import static org.junit.Assert.*;

import org.junit.Test;

public class WorldTest {

	/*@Test
	public void test() {
		fail("Not yet implemented");
	}*/
	
	@Test
	public void WDH_World_ConstructorTest1() { // tests world(int n, int m)
		World wTest = new World(5,6);
		Object o = wTest.get(4, 5);
		assertEquals(o, null);
	}

	@Test
	public void WDH_World_ConstructorTest2() {
		World wTest = new World(5,6);
		Object o = wTest.get(0, 0);
		assertEquals(o, null);
	}
	
	@Test
	public void WDH_World_ConstructorTest3() {
		World wTest = new World(5,6);
		Object o = wTest.get(2, 2);
		assertEquals(o, null);
	}
	
	@Test
	public void WDH_World_ConstructorTest4() { // tests world(int n)
		World wTest = new World(4); 
		Object o = wTest.get(0, 0);
		assertEquals(o, null);
	}
	
	@Test
	public void WDH_World_ConstructorTest5() {
		World wTest = new World(4);
		Object o = wTest.get(3, 3);
		assertEquals(o, null);
	}
	
	@Test
	public void WDH_World_ConstructorTest6() {
		World wTest = new World(4);
		Object o = wTest.get(2, 2);
		assertEquals(o, null);
	}
	
	@Test
	public void WDH_World_GetTest1() { // tests get
		World wTest = new World(5,6);
		String s = "IM HANDSOM";
		wTest.put(0, 0, s);
		assertEquals(wTest.get(0, 0), s);
	}
	
	@Test
	public void WDH_World_GetTest2() { // tests get
		World wTest = new World(5,6);
		String s = "IM REAAAAAAALLLLY HANDSOM";
		wTest.put(4, 5, s);
		Coordinate c = new Coordinate(wTest, 4, 5);
		assertEquals(wTest.get(c), s);
	}
	
	@Test
	public void WDH_World_PutTest1() { // tests put
		World wTest = new World(5,6);
		String s = "MY GIRLFRIEND IS AFRAID OF HEIGHTS";
		wTest.put(4, 5, s);
		assertEquals(wTest.get(4, 5), s);
	}
	
	@Test
	public void WDH_World_PutTest2() {
		World wTest = new World(5,6);
		Coordinate c = new Coordinate(wTest, 4, 5);
		String s = "ALL YOUR BASE ARE BELONG TO US";
		assertEquals(wTest.get(4, 5), null);
		wTest.put(c, s);
		assertEquals(wTest.get(4, 5), s);
	}
	
	@Test
	public void WDH_World_ToStringTest1() { // tests toString
		World wTest = new World(5,6);
		assertEquals(wTest.toString(), "World(5, 6)");
	}

	@Test
	public void TMVSWorldToString() {
		int n = 0, m = 1;
		World world = new World(n, m);
		assertTrue(world.toString().equals("World(0, 1)"));
	}
}
