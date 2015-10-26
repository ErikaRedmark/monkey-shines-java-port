package org.erikaredmark.monkeyshines;

/**
 * Represents a location, direction. The direction is effectively a velocity. 
 * @author Erika Redmark
 *
 */
public class ImmutableVector {
	public final int x, y, velX, velY;
	
	public ImmutableVector(final int x, final int y, final int velX, final int velY) {
		this.x = x; this.y = y; this.velX = velX; this.velY = velY;
	}
	
	public static ImmutableVector of(final int x, final int y, final int velX, final int velY) {
		return new ImmutableVector(x, y, velX, velY);
	}
	
	/** Initialises a vector with some velocity using a pre-existing point */
	public static ImmutableVector fromPoint(ImmutablePoint2D point, int velX, int velY) {
		return new ImmutableVector(point.x(), point.y(), velX, velY);
	}
	
	int x() { return x; }
	int y() { return y; }
	int velX() { return velX; }
	int velY() { return velY; }
}
