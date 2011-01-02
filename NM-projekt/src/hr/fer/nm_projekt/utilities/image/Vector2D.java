package hr.fer.nm_projekt.utilities.image;

import java.awt.Point;

public class Vector2D extends Point {

	/**
	 * 
	 */
	private static final long serialVersionUID = 659373565076797515L;

	public Vector2D(int x, int y) {
		super(x, y);
	}

	public Vector2D(Point p) {
		super(p);
	}

	public Vector2D add(Point p) {
		this.x += p.x;
		this.y += p.y;
		return this;
	}

	public Vector2D add(int x, int y) {
		this.x += x;
		this.y += y;
		return this;
	}

	public int dot(int x, int y) {
		return this.x * x + this.y * y;
	}

	public double getAngle()
	{
		return Math.atan2(y, x);
	}
	
	public boolean isNull() {
		return x == 0 && y == 0;
	}

	public Vector2D multiply(int factor) {
		this.x *= factor;
		this.y *= factor;
		return this;
	}

	public Vector2D multiply(double factor) {
		this.x = (int) Math.round(this.x * factor);
		this.y = (int) Math.round(this.y * factor);
		return this;
	}
	
	public Vector2D substract(Point p) {
		this.x -= p.x;
		this.y -= p.y;
		return this;
	}
}
