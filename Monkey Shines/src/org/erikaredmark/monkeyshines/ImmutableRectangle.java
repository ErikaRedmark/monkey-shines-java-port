package org.erikaredmark.monkeyshines;

import org.erikaredmark.monkeyshines.bounds.Boundable;

/**
 * 
 * Represents a rectangle, starting from the upper-left of some point, and extending by width and height in the 
 * right and down directions.
 * <p/>
 * Instances of this class are immutable but not under as heavy instance control as {@code ImmutableClippingRectangle}.
 * Newly created immutables are not cached, so it is safe to use this class to create many rectangles without trashing
 * memory.
 * 
 * @author Erika Redmark
 *
 */
public final class ImmutableRectangle extends Boundable {

	private ImmutableRectangle(final int x, final int y, final int width, final int height) {
		super.location = ImmutablePoint2D.of(x, y);
		super.size = ImmutablePoint2D.of(width, height);
	}

	/**
	 * 
	 * Returns an instance of an immutable rectangle with the given parameters.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 * 
	 */
	public static ImmutableRectangle of(final int x, final int y, final int width, final int height) {
		return new ImmutableRectangle(x, y, width, height);
	}

	/** 
	 * 
	 * Returns a new rectangle that is the same as this rectangle but with the new given top left co-ordinate 
	 *
	 * @param point
	 * 		new point
	 * 
	 * @return
	 * 		new rectangle
	 *
	 */
	public ImmutableRectangle newTopLeft(ImmutablePoint2D point) {
		return ImmutableRectangle.of(point.x(), point.y(), this.size.x(), this.size.y() );
	}
	
	/**
	 * 
	 * Returns a new rectangle that is the same as this rectangle but with the new given lower right co-oridnate
	 * 
	 * @param point
	 * 		new point
	 * 
	 * @return
	 * 		new rectangle
	 * 
	 */
	public ImmutableRectangle newBottomRight(ImmutablePoint2D point) {
		return ImmutableRectangle.of(this.location.x(), this.location.y(), this.location.x() - point.x(), this.location.y() - point.y() );
	}
	
	/**
	 * 
	 * Returns a new rectangle that is the same as this rectangle but with the new given size
	 * 
	 * @param size
	 * 		new size
	 * 
	 * @return
	 * 		new rectangle
	 * 
	 */
	public ImmutableRectangle newSize(ImmutablePoint2D size) {
		return ImmutableRectangle.of(this.location.x(), this.location.y(), size.x(), size.y() );
	}

	
}