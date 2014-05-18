package edu.nova.erikaredmark.monkeyshines;

import static org.junit.Assert.*;

import org.junit.Test;
// Test class: Not intended to be instantiated or called outside of JUnit
public final class World_Test {
	
	/**
	 * 
	 * Ensures the algorithm works for mapping a bounding box to tile grid points when the bounding box almost entirely
	 * covers properly the grid tiles it starts in (no snapping)
	 * 
	 */
	@Test public void effectiveTilesNoSnap() {
		ImmutableRectangle box = ImmutableRectangle.of(2, 3, 40, 40);
		ImmutablePoint2D[] four = World.effectiveTiles(box);
		assertEquals(0, four[0].x() );
		assertEquals(0, four[0].y() );
		
		assertEquals(1, four[1].x() );
		assertEquals(0, four[1].y() );

		assertEquals(0, four[2].x() );
		assertEquals(1, four[2].y() );
		
		assertEquals(1, four[3].x() );
		assertEquals(1, four[3].y() );
	}
	
	/**
	 * 
	 * Algorithm must snap to the next tiles since the bounding box is closer to the right/bottom than it
	 * is the top/left. 
	 * 
	 */
	@Test public void effectiveTilesSnap() {
		ImmutableRectangle box = ImmutableRectangle.of(12, 13, 40, 40);
		ImmutablePoint2D[] four = World.effectiveTiles(box);
		assertEquals(1, four[0].x() );
		assertEquals(1, four[0].y() );
		
		assertEquals(2, four[1].x() );
		assertEquals(1, four[1].y() );

		assertEquals(1, four[2].x() );
		assertEquals(2, four[2].y() );
		
		assertEquals(2, four[3].x() );
		assertEquals(2, four[3].y() );
	}
	
}
