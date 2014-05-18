package edu.nova.erikaredmark.monkeyshines;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import edu.nova.erikaredmark.monkeyshines.resource.WorldResource;

public final class Hazard_Test {

	private static final WorldResource dummy = WorldResource.empty();
	private List<Hazard> starterHazards;
	
	@Before public void setUp() {
		starterHazards = new ArrayList<>();
		// Custom hazards will be checked that they were added properly by simply checking for the one default BURN
		// in the collection of hazards
		starterHazards.add(new Hazard(0, true, DeathAnimation.ELECTRIC, dummy) );
		starterHazards.add(new Hazard(1, true, DeathAnimation.NORMAL, dummy) );
		starterHazards.add(new Hazard(2, true, DeathAnimation.BEE, dummy) );
	}
	
	
	/**
	 * 
	 * Proper execution for when the hazard list doesn't contain anything
	 * 
	 */
	@Test public void testAddNewHazardEmtpy() {
		final List<Hazard> emptyHazards = new ArrayList<>();
		Hazard.newHazardTo(emptyHazards, 0, dummy);
		assertEquals(DeathAnimation.BURN, emptyHazards.get(0).getDeathAnimation() );
	}
	/**
	 * 
	 * Proper execution when a new hazard is added to the beginning of an existing hazard list
	 * 
	 */
	@Test public void testAddNewHazardBeginning() {
		Hazard.newHazardTo(starterHazards, 0, dummy);
		assertEquals(DeathAnimation.BURN, starterHazards.get(0).getDeathAnimation() );
		assertEquals(DeathAnimation.ELECTRIC, starterHazards.get(1).getDeathAnimation() );
		assertEquals(DeathAnimation.NORMAL, starterHazards.get(2).getDeathAnimation() );
		assertEquals(DeathAnimation.BEE, starterHazards.get(3).getDeathAnimation() );
		
		checkIds(starterHazards);
	}
	
	// ensure ids are sane
	private void checkIds(List<Hazard> hazards) {
		for (int i = 0; i < hazards.size(); i++) {
			assertEquals(i, hazards.get(i).getId() );
		}
	}
	
	/**
	 * 
	 * Proper execution when a new hazard is added to the middle
	 * 
	 */
	@Test public void testAddHazardMiddle() {
		Hazard.newHazardTo(starterHazards, 1, dummy);
		assertEquals(DeathAnimation.ELECTRIC, starterHazards.get(0).getDeathAnimation() );
		assertEquals(DeathAnimation.BURN, starterHazards.get(1).getDeathAnimation() );
		assertEquals(DeathAnimation.NORMAL, starterHazards.get(2).getDeathAnimation() );
		assertEquals(DeathAnimation.BEE, starterHazards.get(3).getDeathAnimation() );
		
		checkIds(starterHazards);
	}
	
	/**
	 * 
	 * Proper execution when a new hazard added to end
	 * 
	 */
	@Test public void testAddHazardEnd() {
		Hazard.newHazardTo(starterHazards, 3, dummy);
		assertEquals(DeathAnimation.ELECTRIC, starterHazards.get(0).getDeathAnimation() );
		assertEquals(DeathAnimation.NORMAL, starterHazards.get(1).getDeathAnimation() );
		assertEquals(DeathAnimation.BEE, starterHazards.get(2).getDeathAnimation() );
		assertEquals(DeathAnimation.BURN, starterHazards.get(3).getDeathAnimation() );
		
		checkIds(starterHazards);
	}
	
	/**
	 * 
	 * Fail fast behaviour when a hazard is attempted to be added with a negative number
	 * 
	 */
	@Test(expected=IllegalArgumentException.class) public void testAddHazardNegative() {
		Hazard.newHazardTo(starterHazards, -1, dummy);
	}
	
	/**
	 * 
	 * Fail fast behaviour when a hazard is added, where its id would otherwise caause gaps in the id list. For
	 * example, if a list has ids 0 up to 4, adding id 6 is wrong because there is no intervening id 5 and a
	 * requirement of lists of hazards internally is that they are always referenceable by an id number.
	 * 
	 */
	@Test(expected=IllegalArgumentException.class) public void testAddHazardGaps() {
		Hazard.newHazardTo(starterHazards, 4, dummy);
	}
	
	@Test public void testRemoveHazardFirst() {
		final Hazard hazardToRemove = starterHazards.get(0);
		Hazard.removeHazard(starterHazards, hazardToRemove);
		assertEquals(DeathAnimation.NORMAL, starterHazards.get(0).getDeathAnimation() );
		assertEquals(DeathAnimation.BEE, starterHazards.get(1).getDeathAnimation() );
		
		checkIds(starterHazards);
	}
	
	@Test public void testRemoveHazardMiddle() {
		final Hazard hazardToRemove = starterHazards.get(1);
		Hazard.removeHazard(starterHazards, hazardToRemove);
		assertEquals(DeathAnimation.ELECTRIC, starterHazards.get(0).getDeathAnimation() );
		assertEquals(DeathAnimation.BEE, starterHazards.get(1).getDeathAnimation() );
		
		checkIds(starterHazards);
	}
	
	@Test public void testRemoveHazardEnd() {
		final Hazard hazardToRemove = starterHazards.get(2);
		Hazard.removeHazard(starterHazards, hazardToRemove);
		assertEquals(DeathAnimation.ELECTRIC, starterHazards.get(0).getDeathAnimation() );
		assertEquals(DeathAnimation.NORMAL, starterHazards.get(1).getDeathAnimation() );
		
		checkIds(starterHazards);
	}
	
	@Test(expected=IllegalArgumentException.class) public void testRemoveHazardNotExists() {
		final Hazard elWhato = new Hazard(12, true, DeathAnimation.BURN, dummy);
		Hazard.removeHazard(starterHazards, elWhato);
	}
	
	// Test replacing hazards
	@Test public void testReplaceStart() {
		final Hazard toReplace = new Hazard(0, true, DeathAnimation.BURN, dummy);
		
		assertEquals(DeathAnimation.ELECTRIC, starterHazards.get(0).getDeathAnimation() );
		Hazard.replaceHazard(starterHazards, toReplace);
		assertEquals(DeathAnimation.BURN, starterHazards.get(0).getDeathAnimation() );
	}
	
	@Test public void testReplaceMiddle() {
		final Hazard toReplace = new Hazard(1, true, DeathAnimation.BURN, dummy);
		
		assertEquals(DeathAnimation.NORMAL, starterHazards.get(1).getDeathAnimation() );
		Hazard.replaceHazard(starterHazards, toReplace);
		assertEquals(DeathAnimation.BURN, starterHazards.get(1).getDeathAnimation() );
	}
	
	@Test public void testReplaceEnd() {
		final Hazard toReplace = new Hazard(2, true, DeathAnimation.BURN, dummy);
		
		assertEquals(DeathAnimation.BEE, starterHazards.get(2).getDeathAnimation() );
		Hazard.replaceHazard(starterHazards, toReplace);
		assertEquals(DeathAnimation.BURN, starterHazards.get(2).getDeathAnimation() );
	}
	
	@Test(expected=IllegalArgumentException.class) public void testReplaceCant() {
		final Hazard toReplace = new Hazard(3, true, DeathAnimation.BURN, dummy);
		Hazard.replaceHazard(starterHazards, toReplace);
	}
}

