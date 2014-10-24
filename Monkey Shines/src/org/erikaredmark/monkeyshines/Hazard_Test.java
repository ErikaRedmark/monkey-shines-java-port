package org.erikaredmark.monkeyshines;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public final class Hazard_Test {

	private List<Hazard> starterHazards;
	
	@Before public void setUp() {
		starterHazards = new ArrayList<>();
		// Custom hazards will be checked that they were added properly by simply checking for the one default BURN
		// in the collection of hazards
		starterHazards.add(new Hazard(0, true, DeathAnimation.ELECTRIC, false) );
		starterHazards.add(new Hazard(1, true, DeathAnimation.NORMAL, false) );
		starterHazards.add(new Hazard(2, true, DeathAnimation.BEE, false) );
	}
	
	// Test replacing hazards
	@Test public void testReplaceStart() {
		final Hazard toReplace = new Hazard(0, true, DeathAnimation.BURN, false);
		
		assertEquals(DeathAnimation.ELECTRIC, starterHazards.get(0).getDeathAnimation() );
		Hazard.replaceHazard(starterHazards, toReplace);
		assertEquals(DeathAnimation.BURN, starterHazards.get(0).getDeathAnimation() );
	}
	
	@Test public void testReplaceMiddle() {
		final Hazard toReplace = new Hazard(1, true, DeathAnimation.BURN, false);
		
		assertEquals(DeathAnimation.NORMAL, starterHazards.get(1).getDeathAnimation() );
		Hazard.replaceHazard(starterHazards, toReplace);
		assertEquals(DeathAnimation.BURN, starterHazards.get(1).getDeathAnimation() );
	}
	
	@Test public void testReplaceEnd() {
		final Hazard toReplace = new Hazard(2, true, DeathAnimation.BURN, false);
		
		assertEquals(DeathAnimation.BEE, starterHazards.get(2).getDeathAnimation() );
		Hazard.replaceHazard(starterHazards, toReplace);
		assertEquals(DeathAnimation.BURN, starterHazards.get(2).getDeathAnimation() );
	}
	
	@Test(expected=IllegalArgumentException.class) public void testReplaceCant() {
		final Hazard toReplace = new Hazard(3, true, DeathAnimation.BURN, false);
		Hazard.replaceHazard(starterHazards, toReplace);
	}
}

