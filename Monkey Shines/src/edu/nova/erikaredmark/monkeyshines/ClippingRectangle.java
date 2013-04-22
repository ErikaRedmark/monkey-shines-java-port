package edu.nova.erikaredmark.monkeyshines;

import java.awt.Dimension;

/**
 * 
 * Represents a clipping region in the game. A clipping region is nothing but a rectangle that indicates the bounds of some 'thing', whether
 * it be graphics or collision.
 * <p/>
 * Objects of this type are mutable to a point. The width and height of a clipping rectangle may not be changed, but the x, y top-left
 * corner may be changed easily. The position of the clipping region MAY be out of drawable bounds; it is up to other code to ignore out-
 * of-bounds positions.
 * 
 * @author Erika Redmark
 *
 */
public class ClippingRectangle {
	
	/* Wrap the dimension instance so it becomes effectively immutable to clients.										*/
	private final Dimension dim;
	
	private int X;
	private int Y;

	private ClippingRectangle(final int width, final int height, final int X, final int Y) {
		dim = new Dimension(width, height);
		this.X = X;
		this.Y = Y;
	}
	
	// Package visibility to allow UnmodifiableClippingRectangle only!
	ClippingRectangle(ClippingRectangle cpy) {
		dim = new Dimension(cpy.width(), cpy.height() );
		this.X = cpy.X;
		this.Y = cpy.Y;
	}
	
	/**
	 * Returns a clipping rectangle whose location and size are all specified. Note that location is mutable, but size is immutable
	 * 
	 * @param width
	 * @param height
	 * @param x
	 * @param y
	 * 
	 * @return
	 * 		a new instance of a clipping rectangle
	 */
	public static ClippingRectangle of(final int width, final int height, final int x, final int y) { return new ClippingRectangle(width, height, x, y); }
	
	/**
	 * Returns a clipping rectangle whose location is 0,0 (mutable) and whose width and height is as specified (immutable)
	 * 
	 * @param width
	 * @param height
	 * 
	 * @return
	 * 		a new instance of a clipping rectangle with the given features
	 */
	public static ClippingRectangle of(final int width, final int height) { return new ClippingRectangle(width, height, 0, 0); }
	
	/**
	 * Returns a new clipping rectangle whose properties are a copy of the passed clipping rectangle.
	 * 
	 * @param other
	 * 		Clipping rectangle to copy from
	 * 
	 * @return
	 * 		new instance of a clipping rectangle initialised to the fields of the passed one. This is a deep-copy.
	 */
	public static ClippingRectangle of(final ClippingRectangle other) { return new ClippingRectangle(other); }
	
	
	public int x() { return X; }
	public int y() { return Y; }
	public int width() { return dim.width; }
	public int height() { return dim.height; }
	
	/**
	 * Moves the clipping region to a new location, keeping the same size.
	 * 
	 * @param X
	 * 		new x location for upper-left point
	 * 
	 * @param Y
	 * 		new y location for upper-left point
	 */
	public void move(final int X, final int Y) { this.X = X; this.Y = Y; }
	public void setX(final int X) { this.X = X; }
	public void setY(final int Y) { this.Y = Y; }


	/**
	 * 
	 * Translates the region the number of units left and down. Use negative values to indicate a reverse direction (right and up)
	 * 
	 * @param unitsX
	 * 		positive for n units right, negative for n units left
	 * 
	 * @param unitsDown
	 * 		positive for n units down, negative for n units up
	 * 
	 */
	public void translate(final int unitsX, final int unitsY) { this.X += unitsX; this.Y += unitsY; }
	public void translateX(final int units) { this.X += units; }
	public void translateY(final int units) { this.Y += units; }
	
	
	/**
	 * 
	 * Tests if a given points lies within this copping region. Lying within is inclusive of the bounds, any point within
	 * {@code [x, x + width], [y, y + width] } is considered within the region.
	 * 
	 * @param point
	 * 
	 * @return {@code true} if the given point lies within the rectangle given be this object, {@code false} if otherwise
	 * 
	 */
	public boolean inBounds(Point2D point) {
		return (inBoundsX(point) && inBoundsY(point) );
	}
	
	/**
	 * Does bounds checking only for X co-ordinate
	 * 
	 * @param point
	 * @return {@code true} if the point's x value lies within the x bounds of this region, {@code false} if otherwise
	 */
	public boolean inBoundsX(Point2D point) {
		return (point.x() >= this.x() && point.x() <= ( this.x() + this.width() ) );
	}
	
	/**
	 * Does bounds checking only for Y co-ordinate
	 * 
	 * @param point
	 * @return {@code true} if the point's y value lies within the x bounds of this region, {@code false} if otherwise
	 */
	public boolean inBoundsY(Point2D point) {
		return (point.y() >= this.y() && point.y() <= ( this.y() + this.height()) );
	}
	
	/**
	 * Determines if the given regions intersect.
	 * 
	 * @param other
	 * @return
	 */
	public boolean intersect(ClippingRectangle other) {
		// TODO method stub
		return false;
	}


}
