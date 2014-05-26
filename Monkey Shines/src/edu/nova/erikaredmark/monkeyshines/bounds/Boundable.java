package edu.nova.erikaredmark.monkeyshines.bounds;

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
public abstract class Boundable {
	
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
	 * Determines if the given regions intersect. Intersections are symmetric: If region A intersects with region B, then
	 * region B intersects with region A
	 * 
	 * @param that
	 * 		the other boundable to check for intersection
	 * 	
	 * @return
	 * 		{@code true} if the two boundables intersect, {@code false} if otherwise
	 * 
	 */
	public boolean intersect(Boundable that) {
		// Prove that the don't intersect
		// If we orders the X values of both the left and right points of this and that, if this appears two times 
		// in succession, then that, or vice-versa, they are NOT intersecting.
		int thisX1 = location.x();
		int thisX2 = location.x() + size.x();
		
		int thatX1 = that.location.x();
		int thatX2 = that.location.x() + that.size.x();
		
		// Check for the only two cases of non-intersecting X bounds:
		// thisX1 < thisX2 < thatX1 < thatX2 turns into thisX2 < thatX1
		// thatX1 < thatX2 < thisX1 < thisX2 turns into thatX2 < thisX1
		if (thisX2 < thatX1 || thatX2 < thisX1)  return false;
		
		// Same thing for y
		
		int thisY1 = location.y();
		int thisY2 = location.y() + size.y();
		
		int thatY1 = that.location.y();
		int thatY2 = that.location.y() + that.size.y();
		
		if (thisY2 < thatY1 || thatY2 < thisY1)  return false;
		
		return true;
	}

}
