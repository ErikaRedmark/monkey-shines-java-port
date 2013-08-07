package edu.nova.erikaredmark.monkeyshines.bounds;

import java.io.Serializable;

import edu.nova.erikaredmark.monkeyshines.ImmutablePoint2D;

/**
 * 
 * Note: Had this been Scala, this would be a trait.
 * <p/>
 * This 'trait' represents that an object contains bounds. Subclasses must set the protected members for the location and
 * size to some implementation of {@code IPoint2D}. From there, the bounds checking methods in this trait will be able to
 * allow clients to do collision detection
 * <p/>
 * Implementors MUST initialise all instance variables of this trait.
 * <p/>
 * It is <strong> suggested </strong> that implementors override {@code getLocation() } and {@code getSize() } if the
 * implementation of the IPoint2D for either member is mutable. Default implementation returns a reference to the object,
 * which may not be desirable if it is mutable.
 * 
 * @author Erika Redmark
 *
 */
public abstract class Boundable implements Serializable {
	/* Safe to use default serialized form. The idea of a 'rectangle' isn't going to change.
	 */
	private static final long serialVersionUID = 157L;
	
	/** The location of the boundable. May be any implementation of a point.	*/
	protected IPoint2D location;
	/** The size of the boundable. May be any implementation of a point.		*/
	protected IPoint2D size;
	
	/**
	 * 
	 * Returns the location (upper-left corner) of this boundable. Default implementation returns a direct reference to
	 * the object.
	 * 
	 * @return
	 * 
	 */
	public IPoint2D getLocation() { return location; }
	
	/**
	 * 
	 * Returns the size of this boundable. x() is width and y() is height Default implementation returns a direct reference to
	 * the object.
	 * 
	 * @return
	 * 
	 */
	public IPoint2D getSize() { return size; }
	
	/**
	 * 
	 * Tests if a given points lies within this boundable. Lying within is inclusive of the bounds, any point within
	 * {@code [x, x + width], [y, y + width] } is considered within the region.
	 * 
	 * @param point
	 * 
	 * @return {@code true} if the given point lies within the rectangle given be this object, {@code false} if otherwise
	 * 
	 */
	public boolean inBounds(IPoint2D point) {
		return (inBoundsX(point) && inBoundsY(point) );
	}
	
	/**
	 * Does bounds checking only for X co-ordinate
	 * 
	 * @param point
	 * @return {@code true} if the point's x value lies within the x bounds of this region, {@code false} if otherwise
	 */
	public boolean inBoundsX(IPoint2D point) {
		// Point right of left bound, and left of right bound?
		return (point.x() >= this.location.x() && 
				point.x() <= ( this.location.x() + this.size.x() ) );
	}
	
	/**
	 * Does bounds checking only for Y co-ordinate
	 * 
	 * @param point
	 * @return {@code true} if the point's y value lies within the x bounds of this region, {@code false} if otherwise
	 */
	public boolean inBoundsY(IPoint2D point) {
		// Point down from upper bound, and above lower bound?
		return (point.y() >= this.location.y() && 
				point.y() <= ( this.location.y() + this.size.y() ) );
	}
	
	/**
	 * Determines if the given regions intersect.
	 * 
	 * @param other
	 * @return
	 */
	public boolean intersect(Boundable other) {
		// Check if any of the four corners are within this bounds
		IPoint2D upLeft = other.location;
		IPoint2D upRight = ImmutablePoint2D.of(other.location.x() + other.size.x(), other.location.y() );
		IPoint2D downLeft = ImmutablePoint2D.of(other.location.x(), other.location.y() + other.size.y() );
		IPoint2D downRight = ImmutablePoint2D.of(other.location.x() + other.size.x(), other.location.y() + other.size.y() );
		return   inBounds(upLeft)
			  || inBounds(upRight)
			  || inBounds(downLeft)
			  || inBounds(downRight);
	}

}
