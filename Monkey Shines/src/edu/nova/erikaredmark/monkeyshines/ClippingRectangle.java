package edu.nova.erikaredmark.monkeyshines;

import edu.nova.erikaredmark.monkeyshines.bounds.Boundable;
import edu.nova.erikaredmark.monkeyshines.bounds.IPoint2D;

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
public class ClippingRectangle extends Boundable {
	private static final long serialVersionUID = 5968393238329112512L;
	
	private ClippingRectangle(final int x, final int y, final int width, final int height) {
		super.location = Point2D.of(x, y);
		super.size = ImmutablePoint2D.of(width, height);

	}
	
	private ClippingRectangle(ClippingRectangle cpy) {
		super.size = ImmutablePoint2D.of(cpy.width(), cpy.height() );
		super.size = ImmutablePoint2D.of(cpy.x(), cpy.y() );
	}
	
	
	/**
	 * 
	 * Returns a copy of this clipping rectangle's location so it may not be modified by clients. The copy may be
	 * modified without affecting the original.
	 * 
	 */
	@Override public IPoint2D getLocation() {
		return Point2D.from(super.location);
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
	public static ClippingRectangle of(final int x, final int y, final int width, final int height) { return new ClippingRectangle(width, height, x, y); }
	
	/**
	 * Returns a clipping rectangle whose location is 0,0 (mutable) and whose width and height is as specified (immutable)
	 * 
	 * @param width
	 * @param height
	 * 
	 * @return
	 * 		a new instance of a clipping rectangle with the given features
	 */
	public static ClippingRectangle of(final int width, final int height) { return new ClippingRectangle(0, 0, width, height); }
	
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
	
	
	public int x() { return super.location.x(); }
	public int y() { return super.location.y(); }
	public int width() { return super.size.x(); }
	public int height() { return super.size.y(); }
	
	/**
	 * Moves the clipping region to a new location, keeping the same size.
	 * 
	 * @param X
	 * 		new x location for upper-left point
	 * 
	 * @param Y
	 * 		new y location for upper-left point
	 */
	public void move(final int x, final int y) { ((Point2D)super.location).setX(x); ((Point2D)super.location).setY(y); }
	public void setX(final int x) { ((Point2D)super.location).setX(x); }
	public void setY(final int y) { ((Point2D)super.location).setY(y); }


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
	public void translate(final int unitsX, final int unitsY) { ((Point2D)super.location).translateX(unitsX); ((Point2D)super.location).translateY(unitsY); }
	public void translateX(final int units) { ((Point2D)super.location).translateX(units); }
	public void translateY(final int units) { ((Point2D)super.location).translateY(units); }
	
	
	

}
