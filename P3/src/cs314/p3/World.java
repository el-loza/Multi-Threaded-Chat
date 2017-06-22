package cs314.p3;

import java.util.ArrayList;

//Author: William Hinz III
//Version: 1.2;

public class World {

	/*
	 * Class Variables
	 */
	private ArrayList<ArrayList<Object>> worldArray;
	private int n;
	private int m;

	/*
	 * Constructors
	 */
	public World(int n, int m) {
		this.n = n;
		this.m = m;
		worldArray = new ArrayList<ArrayList<Object>>(m);

		for (int j = 0; j < m; j++) {
			ArrayList<Object> temp = new ArrayList<Object>(n);
			for(int i = 0; i < n; i++){
				temp.add(null);
			}
			worldArray.add(temp);
		}
	}

	public World(int n) {
		this(n, n);
	}

	/*
	 * Getters
	 */
	protected Object get(int n, int m) {
		return worldArray.get(m).get(n);
	}

	protected int getWidthN() {
		return n;
	}

	protected int getHeightM() {
		return m;
	}

	public Object get(Coordinate c){
		return worldArray.get(c.getY()).get(c.getX());
	}
	
	/*
	 * Setters
	 */
	protected void put(int n, int m, Object putMe) {
		worldArray.get(m).set(n, putMe);
	}

	public void put(Coordinate coordinate, Object object) {
		put(coordinate.getX(), coordinate.getY(), object);
	}

	@Override
	public String toString() {
		return "World(" + n + ", " + m + ")";
	}
	
}



