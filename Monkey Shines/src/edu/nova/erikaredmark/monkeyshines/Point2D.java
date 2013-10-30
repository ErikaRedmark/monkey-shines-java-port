package edu.nova.erikaredmark.monkeyshines;

import edu.nova.erikaredmark.monkeyshines.bounds.IPoint2D;

/**
 * 
 * Represents a single point on the 2D world.
 * <p/>
 * To prevent heavy amounts of object insantatiation, instances of this class are mutable. Every unique object in the world
 * must have its own reference to a unique Point2D copy.
 * <p/>
 * Point data is internally stored as a double-precision floating point to allow fluid physics calculations.
 * 
 * @author Erika Redmark
 *
 */
public class Point2D implements IPoint2D {
	private double x;
	private double y;
	
	private Point2D(final double x, final double y) { this.x = x; this.y = y; }
	
	public double precisionX() { return x; }
	public double precisionY() { return y; }
	
	public int x() { return (int) x; }
	public int y() { return (int) y; }
	
	public void setX(final double x) { this.x = x; }
	public void setY(final double y) { this.y = y; }
	
	/** Translates point one full unit to the left, regardless of precision.											*/
	public void left() { this.x -= 1; }
	/** Translates point one full unit to the right, regardless of precision.											*/
	public void right() { this.x += 1; }
	/** Translates point one full unit down, regardless of precision.											        */
	public void down() { this.y -= 1; }
	/** Translates point one full unit up, regardless of precision.											            */
	public void up() { this.y += 1; }
	
	/**
	 * Translates the given point along the horizontal. 
	 * 
	 * @param amt positive values go right, negative values go left
	 */
	public void translateX(int amt) { this.x += amt; }
	
	/**
	 * Translates the given point along the verticle.
	 * 
	 * @param amt positive values go down, negative values go up.
	 */
	public void translateY(int amt) { this.y += amt; }
	
	/**
	 * Translates with extra precision; the translation may not affect the actual location returned by {@link #x() }
	 * 
	 * @param amt positive values go right, negative values go left
	 */
	public void translateXFine(double amt) { this.x -= amt; }
	
	/**
	 * Translates with extra precision; the translation may not affect the actual location returned by {@link #y() }
	 * 
	 * @param amt positive values go down, negative values go up
	 */
	public void translateYFine(double amt) { this.y -= amt; }
	
	/** Reverses the sign of the x-cordinate, effectively flipping it to the other side of however the y-axis is defined*/
	public void reverseX() {this.x = -x; }
	/** Reverses the sign of the y-cordinate, effectively flipping it to the other side of however the x-axis is defined*/
	public void reverseY() {this.y = -y; }
	
	/**
	 * 
	 * Returns a new copy of a Point2D with the same logical position as the passed one. The returned object is completely
	 * distinct from the original.
	 * 
	 * @param other
	 * 		the original point 2d to copy from
	 * 
	 * @return
	 * 		a unqiue copy
	 * 
	 */
	public static Point2D of(Point2D other) { return new Point2D(other.precisionX(), other.precisionY() ); }
	
	public static Point2D of(final int x, final int y) { return new Point2D((double)x, (double)y); }
	public static Point2D of(final double x, final double y) { return new Point2D(x, y); }
	
	/**
	 * 
	 * Copies the values of a valid point object to create a new instance of this object (a mutable point). The resulting
	 * object can not be used to modify the immutable object.
	 * <p/>
	 * Due to the immutable point storing values as integers, a point transformed into an immutable point, and then back
	 * again, will lose precision.
	 * 
	 * @param point
	 * 		the other point, possibly an immutable point
	 * 
	 * @return
	 * 		a new, unique instance of this object initialised to the values in the corresponding other point.
	 * 
	 */
	public static Point2D from(final IPoint2D point) {
		return of(point.x(), point.y() );
	}

	/**
	 * 
	 * Adds to this point another point, where the second point's x and y values represent a velocity. This point
	 * instance is mutated by this function.
	 * <p/>
	 * The x component represented by velocity is added to the point, and the y component is subtracted from this point.
	 * This is because positive velocities intuitively should go up, and negative ones should go down.
	 * 
	 * @param velocity
	 * 		the 'velocity' to add to this point. 
	 * 
	 */
	public void applyVelocity(Point2D velocity) {
		this.translateXFine(velocity.precisionX() );
		//using minus, because otherwise positive numbers go down and that makes no sense.
		this.translateYFine(-velocity.precisionY() );
	}
	
	@Override public String toString() {
		return x + ", " + y;
	}


}