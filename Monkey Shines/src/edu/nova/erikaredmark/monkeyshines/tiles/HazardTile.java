package edu.nova.erikaredmark.monkeyshines.tiles;

import java.awt.Graphics2D;

import edu.nova.erikaredmark.monkeyshines.Hazard;
import edu.nova.erikaredmark.monkeyshines.resource.WorldResource;

/**
 * 
 * Indicates a hazard on the map, along with drawing and other state information. Whilst {@code Hazard} represents the 
 * information describing a type of hazard, this object represents one such type somewhere on the world
 * <p/>
 * As with other tiles, the actual location in the level is maintained in a tilemap, not in this object.
 * 
 * @author Erika Redmark
 *
 */
public class HazardTile implements TileType {
	// The actual hazard that this tile represents (is this a lava hazard/bomb? Some other type for this world?)
	// This is also the only thing serialized; everything else is state information for during gameplay
	private final Hazard hazard;
	
	// State information
	// Determines if the hazard should be in exploding animation or not. Ignored for hazards that don't explode and 
	// ignored for a hazard that already exploded.
	private boolean exploding = false;

	// Cycles between 0-1 for basic drawing, and then 0-9 for explosion rendering.
	// When this value is -1, the hazard is not drawn. Additionally, a value of -1
	// indicates the hazard is 'dead' and can't hurt bonzo anymore.
	private int animationPoint = 0;
	
	// When this reaches TICKS_BETWEEN_ANIMATIONS, it goes back to zero and the animationPoint is updated.
	// This prevents quick, rapid animation.
	private int timeToNextFrame = 0;
	
	private static final int TICKS_BETWEEN_ANIMATIONS = 5;
	private static final int MAX_EXPLODING_FRAMES = 9;
	
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
	
	public boolean isExploding() {
		return exploding;
	}
	
	/**
	 * 
	 * Returns the frame of animation that should be used in the current sprite sheet to draw this hazard.
	 * <p/>
	 * Note that if this hazard is exploding, the exploding sprite sheet should be used instead of the standard
	 * one. The animation step is always 0 based; it is up to caller to determine which actual sprite sheet
	 * needs to be used for drawing.
	 * <p/>
	 * It is an error to call this method if the hazard is destroyed (it shouldn't be drawn at all). If assertions
	 * are disabled calling the method in this state returns -1 which has undefined behaviour.
	 * 
	 * @return
	 * 		animation step for the current sprite sheet, dependent on whether this hazard is exploding or not.
	 * 
	 */
	public int getAnimationStep() {
		assert !(isDead() );
		
		if (exploding)  return animationPoint - 2;
		else			return animationPoint;
	}

	@Override public int getId() { return hazard.getId(); }
	
	/**
	 * 
	 * updates animation tick for drawing
	 * 
	 */
	@Override public void update() {
		if (isDead() )  return;
		
		if (readyToAnimate() ) {
			
			if (!exploding) {
				animationPoint =   animationPoint == 0
								 ? 1
								 : 0;
			} else {
				++animationPoint;
				if (animationPoint > MAX_EXPLODING_FRAMES)  die();
			}
		
		}
	}

	@Override public boolean isThru() { return false; }
	
	@Override public boolean isSolid() { return false; }
	
	@Override public void paint(Graphics2D g2d, int drawToX, int drawToY, WorldResource rsrc) {
		// Nothing to paint if dead.
		if (isDead() )  return;
		
		hazard.paint(g2d, drawToX, drawToY, rsrc, getAnimationStep() );
	}
	/**
	 * 
	 * Checks if the hazard is ready to switch to the next frame of animation. Calling this
	 * method changes state; it checks if ready, and if not increments the ticker. As defined
	 * in the class static final variables, a certain number of 'ticks' have to pass before 
	 * being allowed to change the animation state.
	 * 
	 * @return
	 * 		{@code true} if ready, {@code false} if otherwise. Merely calling this method
	 * 		increments the ticker, so this should only be called in the main update method
	 * 		on each tick.
	 * 
	 */
	private boolean readyToAnimate() {
		if (timeToNextFrame >= TICKS_BETWEEN_ANIMATIONS) {
			timeToNextFrame = 0;
			return true;
		} else {
			++timeToNextFrame;
			return false;
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
	 * Resets the state of the hazard. Normally called whenever a level screen is reloaded.
	 * 
	 */
	@Override public void reset() {
		exploding = false;
		animationPoint = 0;
		timeToNextFrame = 0;
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
