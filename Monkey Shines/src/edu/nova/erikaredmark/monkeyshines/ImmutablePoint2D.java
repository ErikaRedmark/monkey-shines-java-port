package edu.nova.erikaredmark.monkeyshines;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import edu.nova.erikaredmark.monkeyshines.bounds.IPoint2D;

/**
 * 
 * Represents the same information as {@code Point2D}, but instances of this class are immutable. These are designed to
 * enumerate fixed points that rarely change, or points that will be saved to a file.
 * <p/>
 * Points that are immutable are internally represented as integers, not floating point. This is because movement calculations
 * that may stack over time do not apply if the point does not change.
 * <p/>
 * This class may not be subclassed.
 * 
 * @author The Doctor
 *
 */
public final class ImmutablePoint2D implements Serializable, IPoint2D {
	private static final long serialVersionUID = 147L;
	
	// Implementation note: Can't be final due to serialization, but class has not public mutator methods.
	// These fields should never change after assignment.
	private transient int x;
	private transient int y;
	
	@Override public int x() {return x;}
	@Override public int y() {return y;}
	
	private ImmutablePoint2D(final int x, final int y) {this.x = x; this.y = y; }
	
	public static ImmutablePoint2D of(final int x, final int y) { return new ImmutablePoint2D(x, y); }
	
	/**
	 * 
	 * Creates an immutable point from a pre-existing mutable point. The newly created immutable point is not affected
	 * by any changes to the passed point after creation.
	 * <p/>
	 * Because the immutable type uses ints and not doubles, double values of the point are truncated (not rounded) and
	 * stored in the new object.
	 * 
	 * @param point
	 * 		the existing mutable point to create an immutable copy from
	 * 
	 * @return
	 * 		an instance of this object with the x and y fields an integer truncated representation of the double fields
	 *		in the corresponding mutable point. This corresponds to the drawing point.
	 *
	 */
	public static ImmutablePoint2D from(final Point2D point) {
		return new ImmutablePoint2D(point.x(), point.y() );
	}
	
	/**
	 * 
	 * Serialize this {@code ImmutablePoint2D} instance.
	 * 
	 * @serialData the x co-ordinate (int) and then the y co-ordinate (int)
	 *
	 */
	private void writeObject(ObjectOutputStream s) throws IOException {
		s.defaultWriteObject();
		s.writeInt(x);
		s.writeInt(y);
	}
	
	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		this.x = s.readInt();
		this.y = s.readInt();
	}
	
	/**
	 * 
	 * Two points are equal if they share the same x/y co-ordinate.
	 * 
	 */
	@Override public boolean equals(Object other) {
		if (other instanceof ImmutablePoint2D == false) return false;
		if (other == this) return true;
		
		return (((ImmutablePoint2D)other).x() == this.x &&
				((ImmutablePoint2D)other).y() == this.y );
	}
	
	@Override public int hashCode() {
		int result = 17;
		result += result * 31 + x;
		result += result * 31 + y;
		return result;
	}
	
}
