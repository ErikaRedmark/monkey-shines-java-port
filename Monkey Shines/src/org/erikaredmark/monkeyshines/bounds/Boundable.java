package org.erikaredmark.monkeyshines.bounds;

import org.erikaredmark.monkeyshines.ImmutableRectangle;

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
	 * 
	 * Determines if the given regions intersect. Intersections are symmetric: If region A intersects with region B, then
	 * region B intersects with region A
	 * <p/>
	 * If two regions are intersecting, a boundable representing the intersection region is returned. Otherwise, {@code null}
	 * is returned to signify no intersection
	 * 
	 * @param that
	 * 		the other boundable to check for intersection
	 * 	
	 * @return
	 * 		a boundable object representing the intersection region, or {@code null} if the two boundables
	 * 		do not intersect.
	 * 
	 */
	public Boundable intersect(Boundable that) {
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
		if (thisX2 < thatX1 || thatX2 < thisX1)  return null;
		
		// Same thing for y
		
		int thisY1 = location.y();
		int thisY2 = location.y() + size.y();
		
		int thatY1 = that.location.y();
		int thatY2 = that.location.y() + that.size.y();
		
		if (thisY2 < thatY1 || thatY2 < thisY1)  return null;
		
		// We have an intersection. Calculate offsets.
		// If my starting position is less than theirs, theirs must be the intersection
		// otherwise it is mine. This is like drawing a line on the axis where both points
		// are and finding the intersection point
		int intersectX =   thisX1 < thatX1
						 ? thatX1
						 : thisX1;
		
		int intersectY =   thisY1 < thatY1
						 ? thatY1
						 : thisY1;
		
		// Now, find sizes. Easiest way is to simply use the same process as above, but with
		// the other corners of the rectangle. however, logic is reversed. Whoever is smaller wins.
		int intersectBottomX =   thisX2 < thatX2
							   ? thisX2
							   : thatX2;
	
		int intersectBottomY =   thisY2 < thatY2
							   ? thisY2
							   : thatY2;
		
		// Make rectangle to return. Constructor expects width and heights, not positions.
		return ImmutableRectangle.of(intersectX, intersectY, intersectBottomX - intersectX, intersectBottomY - intersectY);
	}
	
	/**
	 * 
	 * Boundables are equal to each other if they have the same location and size, regardless
	 * of underlying type.
	 * 
	 */
	@Override public boolean equals(Object o) {
		if (this == o)  return true;
		if (!(o instanceof Boundable) )  return false;
		
		Boundable other = (Boundable) o;
		
		return    this.location.x() == other.location.x()
			   && this.location.y() == other.location.y()
			   && this.size.x() == other.size.x()
			   && this.size.y() == other.size.y();
	}
	
	/**
	 * 
	 * Boundables with equal location and size have equal hashcode
	 * 
	 */
	@Override public int hashCode() {
		int result = 17;
		result += result * 31 + location.x();
		result += result * 31 + location.y();
		result += result * 31 + size.x();
		result += result * 31 + size.y();
		return result;
	}

}
