package edu.nova.erikaredmark.monkeyshines;

import edu.nova.erikaredmark.monkeyshines.bounds.Boundable;

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

	
}