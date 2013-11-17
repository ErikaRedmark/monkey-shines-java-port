package edu.nova.erikaredmark.monkeyshines.tiles;

import edu.nova.erikaredmark.monkeyshines.Hazard;

/**
 * 
 * Indicates a hazard on the map, along with drawing and other state information. Whilst {@code Hazard} represents the 
 * information describing a type of hazard, this object represents one such type somewhere on the world
 * 
 * @author Erika Redmark
 *
 */
public class HazardTile implements TileType {
	private static final long serialVersionUID = 6837954726294938232L;

	// The actual hazard that this tile represents (is this a lava hazard/bomb? Some other type for this world?)
	// This is also the only thing serialized; everything else is state information for during gameplay
	private final Hazard hazard;
	
	// State information
	// Determines if the hazard should be in exploding animation or not. Ignored for hazards that don't explode and 
	// ignored for a hazard that already exploded.
	private transient boolean exploding = false;

	// Cycles between 0-1 for basic drawing, and then 0-9 for explosion rendering.
	// When this value is -1, the hazard is not drawn. Additionally, a value of -1
	// indicates the hazard is 'dead' and can't hurt bonzo anymore.
	private transient int animationPoint = 0;
	private static final int maxExplodingFrames = 9;
	
	private HazardTile(final Hazard hazard) {
		this.hazard = hazard;
	}
	
	/**
	 * 
	 * Creates a hazard on a tile for the given hazard.
	 * 
	 * @param h
	 * 		the hazard type this map hazard represents
	 * 
	 * @return
	 * 		an instance of this object
	 * 
	 */
	public static final HazardTile forHazard(Hazard h) {
		return new HazardTile(h);
	}
	
	/**
	 * 
	 * Returns the hazard type that this tile is representing
	 * 
	 * @return
	 * 		the hazard
	 * 
	 */
	public Hazard getHazard() {
		return hazard;
	}
	
	/**
	 * 
	 * updates animation tick for drawing
	 * 
	 */
	public void update() {
		if (isDead() )  return;
		
		if (!exploding) {
			// Cycle 0 to 1
			animationPoint =   animationPoint == 0
							 ? 1
							 : 0;
		} else {
			++animationPoint;
			if (animationPoint > maxExplodingFrames)  die();
		}
	}
	
	/**
	 * 
	 * Kills the hazard, preventing it from harming bonzo anymore.
	 * 
	 */
	private void die() {
		animationPoint = -1;
	}
	
	/**
	 * 
	 * For hazard tiles that use a hazard that explodes, this represents whether the hazard is gone (it should no longer
	 * affect bonzo), or is still alive. Hazards that don't explode never die.
	 * 
	 * @return
	 * 		{@code true} if the hazard is gone and should not be used in bonzo death considerations, {@code false} if otherwise
	 * 
	 */
	public boolean isDead() {
		return animationPoint == -1;
	}
	
	/**
	 * 
	 * Called when bonzo hits the hazard. This does not kill or affect bonzo; it affects the state of the hazard (exploding
	 * hazards must now begin explodin). Does nothing if the hazard is dead.
	 * 
	 */
	public void hazardHit() {
		if (isDead() )  return;
		
		if (hazard.explodes() ) {
			exploding = true;
			animationPoint = 0;
		}
	}
}
