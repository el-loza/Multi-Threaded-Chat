package cs314.p3;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class CoordinateTest {

	//Coordinate Constructor Tests
	//Scenarios:
	//			x > n, x < 0, 0 <= x <n
	//			y > m, y < 0, 0 <= y <m
	//			w is a world, w is not a world

	@Test
	public void ENLCoordinateConstructXYReg() {

		World w = new World(5, 5);
		Coordinate c = new Coordinate(w, 2, 3);
		assertEquals(c.getX(), 2);
		assertEquals(c.getY(), 3);
	}

	@Test
	public void ENLCoordinateConstructXYLarger() {

		World w = new World(5,5);
		Coordinate c = new Coordinate(w,7,8);
		assertEquals(c.getX(),2);
		assertEquals(c.getY(),3);
	}

	@Test
	public void ENLCoordinateConstructXSmaller() {

		World w = new World(5, 5);
		Coordinate c = new Coordinate(w, -3, -4);
		assertEquals(c.getX(), 2);
		assertEquals(c.getY(), 1);

	}
	
	@Test
	public void ENLCoordinateGetEmpty() {

		World w = new World(5, 5);
		Coordinate c = new Coordinate(w, 2, 4);
		assertEquals(c.get(), null);
	}
	
	@Test
	public void ENLCoordinateGetFull() {

		World w = new World(5, 5);
		Coordinate c = new Coordinate(w, 2, 4);
		c.put(5);
		assertEquals(c.get(), 5);
	}
	
	@Test
	public void ENLCoordinatePutEmpty() {

		World w = new World(5, 5);
		Coordinate c = new Coordinate(w, 2, 4);
		c.put(5);
		assertEquals(c.get(), 5);
	}
	
	@Test
	public void ENLCoordinatePutFull() {

		World w = new World(5, 5);
		Coordinate c = new Coordinate(w, 2, 4);
		c.put(5);
		c.put(7);
		assertEquals(c.get(), 7);
	}
	
	@Test
	public void ENLCoordinateEquals() {

		World w = new World(5, 5);
		Coordinate c = new Coordinate(w, 2, 4);
		Coordinate c2 = new Coordinate(w, 2, 4);
		assertTrue(c.equals(c2));
	}
	
	@Test
	public void ENLCoordinateEquals2() {

		World w = new World(5, 5);
		World w2 = new World(5, 5);
		Coordinate c = new Coordinate(w, 2, 4);
		Coordinate c2 = new Coordinate(w2, 2, 4);
		assertEquals(c.equals(c2), false);
	}
	
	@Test
	public void ENLCoordinateEquals3() {

		World w = new World(5, 5);
		Coordinate c = new Coordinate(w, 4, 4);
		Coordinate c2 = new Coordinate(w, 2, 4);
		assertEquals(c.equals(c2), false);
	}
	
	@Test
	public void ENLCoordinateEquals4() {

		World w = new World(5, 5);
		Coordinate c = new Coordinate(w, 2, 4);
		Coordinate c2 = new Coordinate(w, 2, 2);
		assertEquals(c.equals(c2), false);
	}
	
	@Test
	public void ENLCoordinateFalse() {

		World w = new World(5, 5);
		Coordinate c = new Coordinate(w, 2, 4);
		Coordinate c2 = new Coordinate(w, 2, 4);
		assertTrue(c.equals(c2));
	}
	
	@Test
	public void ENLCoordinateHashCode() {

		World w = new World(5, 5);
		Coordinate c = new Coordinate(w, 2, 4);
		String x = "HelloWorld";
		c.put(x);
		int xHash = w.hashCode();
		int hashTest = 2 + 4 + xHash;
		assertEquals(c.hashCode(), hashTest);
	}
	

	@Test
	public void TMVSCoordinateToString() {
		int n = 10, m = 5;
		World world = new World(n, m);
		Coordinate coordinate = new Coordinate(world, 6, 4);
		assertTrue(coordinate.toString().equals("Coordinate(6,4) in World(10,5)"));
	}


	@Test
	public void TMVSCoordinateNorth() {
		World world = new World(10, 10);
		Coordinate coordinate = new Coordinate(world, 5, 5);
		assertEquals(coordinate.north().getX(), coordinate.getX());
		assertEquals(coordinate.north().getY(), coordinate.getY() + 1);
	}

	@Test
	public void TMVSCoordinateSouth() {
		World world = new World(10, 10);
		Coordinate coordinate = new Coordinate(world, 5, 5);
		assertEquals(coordinate.south().getX(), coordinate.getX());
		assertEquals(coordinate.south().getY(), coordinate.getY() - 1);
	}

	@Test
	public void TMVSCoordinateEast() {
		World world = new World(10, 10);
		Coordinate coordinate = new Coordinate(world, 5, 5);
		assertEquals(coordinate.east().getX(), coordinate.getX() - 1);
		assertEquals(coordinate.east().getY(), coordinate.getY());
	}

	@Test
	public void TMVSCoordinateWest() {
		World world = new World(10, 10);
		Coordinate coordinate = new Coordinate(world, 5, 5);
		assertEquals(coordinate.west().getX(), coordinate.getX() + 1);
		assertEquals(coordinate.west().getY(), coordinate.getY());
	}

}
