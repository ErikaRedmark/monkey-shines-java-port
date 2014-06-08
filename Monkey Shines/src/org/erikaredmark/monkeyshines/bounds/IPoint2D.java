package org.erikaredmark.monkeyshines.bounds;

/**
 * 
 * Generic interface trait for objects representing a point. A point has two fields; an x and y.
 * 
 * @author Erika Redmark
 *
 */
public interface IPoint2D {

	/**
	 * 
	 * Returns x-coordinate. This may sometimes be represented as 'width' when used as a size for rectangles.
	 * 
	 * @return
	 * 
	 */
	public int x();
	
	/**
	 * 
	 * Returns y-coordinate. This may sometimes be represented as 'width' when used as a size for rectangles.
	 * 
	 * @return
	 * 
	 */
	public int y();
	
}
