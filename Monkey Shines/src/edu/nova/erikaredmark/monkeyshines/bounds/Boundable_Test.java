package edu.nova.erikaredmark.monkeyshines.bounds;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.nova.erikaredmark.monkeyshines.ImmutableRectangle;

public class Boundable_Test {
	/**
	 * 
	 * Rectangle A is fully within rectangle B. Therefore, B intersects A and A intersects B
	 * The {@code ImmutableRectangle} class is used for this test
	 * No edges are touching
	 * <pre>
	 * {@code 
	 * 
	 * 	    10   20   30   40   50   60
	 * 	     B
	 *  10   x-------------x
	 *       |    A        |
	 * 	20   |    x---x    |
	 *       |    |   |    |
	 * 	30   |    x---x    |
   	 *       |             | 
	 * 	40   x-------------x
	 * 
	 * 	50
	 * 
	 * 	60
	 * 
	 * }
	 * </pre>
	 * 
	 */
	@Test public void testIntersectFullyWithin() {
		ImmutableRectangle a = ImmutableRectangle.of(20, 20, 10, 10);
		ImmutableRectangle b = ImmutableRectangle.of(10, 10, 30, 30);
		
		assertTrue(b.intersect(a) );
		assertTrue(a.intersect(b) );
	}
	
	/**
	 * 
	 * Rectangle A is partially within Rectangle B on the left side
	 * 
	 */
	@Test public void testIntersectLeftSide() {
		ImmutableRectangle a = ImmutableRectangle.of(10, 10, 10, 10); // 10,10 to 20,20
		ImmutableRectangle b = ImmutableRectangle.of(15, 10, 10, 10); // 15,10 to 25,20
		
		assertTrue(a.intersect(b) );
		assertTrue(b.intersect(a) );
	}

	@Test public void testIntersectTopSide() {
		ImmutableRectangle a = ImmutableRectangle.of(10, 10, 10, 10);
		ImmutableRectangle b = ImmutableRectangle.of(10, 15, 10, 10); // 10, 15 to 20, 25
		
		assertTrue(a.intersect(b) );
		assertTrue(b.intersect(a) );
	}
	
	@Test public void testIntersectRightSide() {
		ImmutableRectangle a = ImmutableRectangle.of(10, 10, 10, 10);
		ImmutableRectangle b = ImmutableRectangle.of(5, 10, 10, 10);
		
		assertTrue(a.intersect(b) );
		assertTrue(b.intersect(a) );
	}
	
	@Test public void testIntersectBottomSide() {
		ImmutableRectangle a = ImmutableRectangle.of(10, 10, 10, 10);
		ImmutableRectangle b = ImmutableRectangle.of(10, 5, 10, 10);
		
		assertTrue(a.intersect(b) );
		assertTrue(b.intersect(a) );
	}
	
	@Test public void testNotIntersect() {
		ImmutableRectangle a = ImmutableRectangle.of(10, 10, 10, 10);
		ImmutableRectangle b = ImmutableRectangle.of(30, 30, 10, 10);
		
		assertFalse(a.intersect(b) );
		assertFalse(b.intersect(a) );
	}

}
