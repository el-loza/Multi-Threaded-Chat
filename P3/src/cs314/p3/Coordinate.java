package cs314.p3;

public class Coordinate {

    private int x;
    private int y;
    private World w;

    public Coordinate(World w, int x, int y) {
        this.w = w;
        this.x = wrapAroundX(x);
        this.y = wrapAroundY(y);
    }
    
    public int wrapAroundX(int xInput){
    	int x; 
    	if (xInput < 0){
    		x = w.getWidthN() + (xInput % w.getWidthN());
    	}else {
    		x = xInput % w.getWidthN();
    	}
    	return x;
    }
    
    public int wrapAroundY(int yInput){
    	int y; 
    	if (yInput < 0){
    		y = w.getHeightM() + (yInput % w.getHeightM());
    	}else {
    		y = yInput % w.getHeightM();
    	}
    	return y;
    }


    public Object get() {
        return w.get(x, y);
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public void put(Object o) {
        w.put(x, y, o);
    }

    public Boolean equals(Coordinate c) {
        return (c.w == w && c.x == x && c.y == y);
    }

    @Override
    public int hashCode() {
        return (w.hashCode() + x + y);
    }
    
    public Coordinate north() {
		return new Coordinate(w, x, wrapAroundY(y + 1));
	}

	public Coordinate south() {
		return new Coordinate(w, x, wrapAroundY(y -1));
	}

	public Coordinate east() {
		return new Coordinate(w, wrapAroundX(x - 1), y);
	}
	
	public Coordinate west() {
		return new Coordinate(w, wrapAroundX(x + 1), y);
	}

	@Override
	public String toString() {
		return "Coordinate(" + x + "," + y + ") in World(" + w.getWidthN() + "," + w.getHeightM() + ")";
	}

}
