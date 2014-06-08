package org.erikaredmark.monkeyshines.bounds;

import static org.junit.Assert.*;

import org.erikaredmark.monkeyshines.ImmutableRectangle;
import org.junit.Test;

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
		
		Boundable intersection = a.intersect(b);
		Boundable intersection2 = b.intersect(a);
		
		assertNotNull(b.intersect(a) );
		assertNotNull(a.intersect(b) );
		assertEquals(intersection, intersection2);
		
		assertEquals(20, intersection.getLocation().x() );
		assertEquals(20, intersection.getLocation().y() );
		assertEquals(10, intersection.getSize().x() );
		assertEquals(10, intersection.getSize().y() );
	}
	
	/**
	 * 
	 * Rectangle A is partially within Rectangle B on the left side
	 * 
	 */
	@Test public void testIntersectLeftSide() {
		ImmutableRectangle a = ImmutableRectangle.of(10, 10, 10, 10); // 10,10 to 20,20
		ImmutableRectangle b = ImmutableRectangle.of(15, 10, 10, 10); // 15,10 to 25,20
		
		Boundable intersection = a.intersect(b);
		Boundable intersection2 = b.intersect(a);
		
		assertNotNull(intersection);
		assertNotNull(intersection);
		
		assertEquals(intersection, intersection2);
		
		assertEquals(15, intersection.getLocation().x() );
		assertEquals(10, intersection.getLocation().y() );
		assertEquals(5, intersection.getSize().x() );
		assertEquals(10, intersection.getSize().y() );
	}

	@Test public void testIntersectTopSide() {
		ImmutableRectangle a = ImmutableRectangle.of(10, 10, 10, 10);
		ImmutableRectangle b = ImmutableRectangle.of(10, 15, 10, 10); // 10, 15 to 20, 25
		
		Boundable intersection = a.intersect(b);
		Boundable intersection2 = b.intersect(a);
		
		assertNotNull(intersection);
		assertNotNull(intersection);
		
		assertEquals(intersection, intersection2);
		
		assertEquals(10, intersection.getLocation().x() );
		assertEquals(15, intersection.getLocation().y() );
		assertEquals(10, intersection.getSize().x() );
		assertEquals(5, intersection.getSize().y() );
	}
	
	@Test public void testIntersectRightSide() {
		ImmutableRectangle a = ImmutableRectangle.of(10, 10, 10, 10);
		ImmutableRectangle b = ImmutableRectangle.of(5, 10, 10, 10);
		
		Boundable intersection = a.intersect(b);
		Boundable intersection2 = b.intersect(a);
		
		assertNotNull(intersection);
		assertNotNull(intersection);
		
		assertEquals(intersection, intersection2);
		
		assertEquals(10, intersection.getLocation().x() );
		assertEquals(10, intersection.getLocation().y() );
		assertEquals(5, intersection.getSize().x() );
		assertEquals(10, intersection.getSize().y() );
	}
	
	@Test public void testIntersectBottomSide() {
		ImmutableRectangle a = ImmutableRectangle.of(10, 10, 10, 10);
		ImmutableRectangle b = ImmutableRectangle.of(10, 5, 10, 10);
		
		Boundable intersection = a.intersect(b);
		Boundable intersection2 = b.intersect(a);
		
		assertNotNull(intersection);
		assertNotNull(intersection);
		
		assertEquals(intersection, intersection2);
		
		assertEquals(10, intersection.getLocation().x() );
		assertEquals(10, intersection.getLocation().y() );
		assertEquals(10, intersection.getSize().x() );
		assertEquals(5, intersection.getSize().y() );
	}
	
	@Test public void testNotIntersect() {
		ImmutableRectangle a = ImmutableRectangle.of(10, 10, 10, 10);
		ImmutableRectangle b = ImmutableRectangle.of(30, 30, 10, 10);
		
		assertNull(a.intersect(b) );
		assertNull(b.intersect(a) );
	}

}
