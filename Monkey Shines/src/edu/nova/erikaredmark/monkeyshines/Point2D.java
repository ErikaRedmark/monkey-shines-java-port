package edu.nova.erikaredmark.monkeyshines;

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
public class Point2D {
	private double x;
	private double y;
	
	private Point2D(final double x, final double y) { this.x = x; this.y = y; }
	
	public double x() { return x; }
	public double y() { return y; }
	
	public int drawX() { return (int) x; }
	public int drawY() { return (int) y; }
	
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
	 * Translates with extra precision; the translation may not affect the actual location returned by {@link #drawX() }
	 * 
	 * @param amt positive values go right, negative values go left
	 */
	public void translateXFine(double amt) { this.x -= amt; }
	
	/**
	 * Translates with extra precision; the translation may not affect the actual location returned by {@link #drawY() }
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
	public static Point2D of(Point2D other) { return new Point2D(other.x(), other.y() ); }
	
	public static Point2D of(final int x, final int y) { return new Point2D((double)x, (double)y); }
	public static Point2D of(final double x, final double y) { return new Point2D(x, y); }
	
	
}