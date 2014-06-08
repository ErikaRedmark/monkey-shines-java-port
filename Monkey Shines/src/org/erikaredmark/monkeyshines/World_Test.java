package org.erikaredmark.monkeyshines;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.erikaredmark.monkeyshines.Conveyer.Rotation;
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
		ImmutablePoint2D[] four = World.effectiveTilesCollision(box);
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
		ImmutablePoint2D[] four = World.effectiveTilesCollision(box);
		assertEquals(1, four[0].x() );
		assertEquals(1, four[0].y() );
		
		assertEquals(2, four[1].x() );
		assertEquals(1, four[1].y() );

		assertEquals(1, four[2].x() );
		assertEquals(2, four[2].y() );
		
		assertEquals(2, four[3].x() );
		assertEquals(2, four[3].y() );
	}
	
	/**
	 * 
	 * Generate two sets of conveyers from an empty list
	 * 
	 */
	@Test public void generateConveyersEmpty() {
		List<Conveyer> conveyers = new ArrayList<>();
		World.generateConveyers(conveyers, 2);
		assertEquals(4, conveyers.size() );
		assertEquals(Rotation.CLOCKWISE, conveyers.get(0).getRotation() );
		assertEquals(0, conveyers.get(0).getId() );
		assertEquals(Rotation.ANTI_CLOCKWISE, conveyers.get(1).getRotation() );
		assertEquals(0, conveyers.get(1).getId() );
		assertEquals(Rotation.CLOCKWISE, conveyers.get(2).getRotation() );
		assertEquals(1, conveyers.get(2).getId() );
		assertEquals(Rotation.ANTI_CLOCKWISE, conveyers.get(3).getRotation() );
		assertEquals(1, conveyers.get(3).getId() );
	}
	
	/**
	 * 
	 * Generate two sets of conveyers from a list already containing a set
	 * 
	 */
	@Test public void generateConveyersPartial() {
		List<Conveyer> conveyers = new ArrayList<>();
		conveyers.add(new Conveyer(0, Rotation.CLOCKWISE) );
		conveyers.add(new Conveyer(0, Rotation.ANTI_CLOCKWISE) );
		
		World.generateConveyers(conveyers, 2);
		
		assertEquals(6, conveyers.size() );
		assertEquals(Rotation.CLOCKWISE, conveyers.get(0).getRotation() );
		assertEquals(0, conveyers.get(0).getId() );
		assertEquals(Rotation.ANTI_CLOCKWISE, conveyers.get(1).getRotation() );
		assertEquals(0, conveyers.get(1).getId() );
		assertEquals(Rotation.CLOCKWISE, conveyers.get(2).getRotation() );
		assertEquals(1, conveyers.get(2).getId() );
		assertEquals(Rotation.ANTI_CLOCKWISE, conveyers.get(3).getRotation() );
		assertEquals(1, conveyers.get(3).getId() );
		assertEquals(Rotation.CLOCKWISE, conveyers.get(4).getRotation() );
		assertEquals(2, conveyers.get(4).getId() );
		assertEquals(Rotation.ANTI_CLOCKWISE, conveyers.get(5).getRotation() );
		assertEquals(2, conveyers.get(5).getId() );
	}
	
}
