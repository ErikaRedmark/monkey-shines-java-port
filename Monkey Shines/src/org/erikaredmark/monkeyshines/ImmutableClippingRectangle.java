package org.erikaredmark.monkeyshines;

import java.util.HashMap;
import java.util.Map;

import org.erikaredmark.monkeyshines.bounds.Boundable;


/**
 * 
 * Instance controlled immutable clipping rectangles. Rectangles taken from this class are immutable, and may be
 * shared between multiple objects.
 * <p/>
 * This rectangles are designed for 'drawing Only!' not collision detection. Rectangle involved in collision detection
 * should be mutable with regard to the object they are encompassing. This is used only for drawing. Objects created by the
 * static factories are <strong>NEVER REMOVED FROM MEMORY UNTIL THE PROGRAM EXITS.</strong>. Given the size constraints of
 * objects in the monkey shines world, there are only a small, finite number of possible clipping rectangles the program
 * will ask for.
 * 
 * @author Erika Redmark
 *
 */
public final class ImmutableClippingRectangle extends Boundable {
	
	// Caching: this class will only currently be used for clipping rectangles and drawing contexts. As a result, it will
	// quickly fill up with (0, 0, 40, 40) and (40, 0, 40, 40) and valid rectangles for drawing sprites and fruits, and
	// then no longer increase in size.
	private static final Map<ImmutablePoint2D, Map<ImmutablePoint2D, ImmutableClippingRectangle> > availableInstances =
		new HashMap<ImmutablePoint2D, Map<ImmutablePoint2D, ImmutableClippingRectangle> >();
	
	// location -> size -> rectangle
	private ImmutableClippingRectangle(final int x, final int y, final int width, final int height) {
		this.location = ImmutablePoint2D.of(x, y);
		this.size = ImmutablePoint2D.of(width, height);
	}
	
	/**
	 * 
	 * Returns an instance of this object that is a clipping rectangle (immutable position and size) for the given
	 * co-ordinates. The returned instance may be shared among multiple objects.
	 * 
	 * @param x
	 * 		x location
	 * 
	 * @param y
	 * 		y location
	 * 
	 * @param width
	 * 		width of rectangle
	 * 
	 * @param height
	 * 		height of rectangle
	 * 
	 * @return
	 * 		instance of this object with the given properties
	 * 
	 */
	public static ImmutableClippingRectangle of(final int x, final int y, final int width, final int height) {
		ImmutablePoint2D loc = ImmutablePoint2D.of(x, y); 
		ImmutablePoint2D siz = ImmutablePoint2D.of(width, height);
		Map<ImmutablePoint2D, ImmutableClippingRectangle> validLoc =
			availableInstances.get(loc);
		
		if (validLoc == null) {
			// no mapping for loc.
			ImmutableClippingRectangle r = new ImmutableClippingRectangle(x, y, width, height);
			// Create the inner hashmap size -> rectangle first
			Map<ImmutablePoint2D, ImmutableClippingRectangle> newMap =
				new HashMap<ImmutablePoint2D, ImmutableClippingRectangle>();
			
			newMap.put(siz, r);
			// The outer hashmap is already created. Create new entry for loc -> map[size -> rectangle]
			availableInstances.put(loc, newMap);
			
			return r;
		} else {
			// Mapping for loc -> map[size -> rectangle]. Check for valid size -> rectangle
			ImmutableClippingRectangle r = validLoc.get(siz);
			if (r == null) {
				r = new ImmutableClippingRectangle(x, y, width, height);
				validLoc.put(siz, r);
				return r;
			} else {
				return r;
			}
		}
	}
	
	/**
	 * 
	 * Returns an instance of this object based on the mutable clipping rectangle passed.
	 * 
	 * @param c
	 * 
	 * @return
	 * 
	 */
	public static ImmutableClippingRectangle from(ClippingRectangle c) {
		return of(c.x(), c.y(), c.width(), c.height() );
	}
}
