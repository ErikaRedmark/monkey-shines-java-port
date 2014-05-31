package edu.nova.erikaredmark.monkeyshines.tiles;

import java.awt.Graphics2D;

import edu.nova.erikaredmark.monkeyshines.Conveyer;
import edu.nova.erikaredmark.monkeyshines.resource.WorldResource;

/**
 * 
 * Indicates a specific conveyer tile on the map. Just like hazard, this represents the
 * running game, mutable state of a conveyer belt.
 * <p/>
 * As with other tiles, the actual location in the level is maintained in a tilemap, not in this object.
 * 
 * @author Erika Redmark
 *
 */
public class ConveyerTile implements TileType {

	// Immutable state for reference.
	private final Conveyer conveyer;
	
	private int animationStep;
	
	// When this reaches TICKS_BETWEEN_ANIMATIONS, it goes back to zero and the animationPoint is updated.
	// This prevents quick, rapid animation.
	private transient int timeToNextFrame = 0;
	
	private static final int TICKS_BETWEEN_ANIMATIONS = 1;
	
	/**
	 * 
	 * Creates an instance of this tile with the given immutable state.
	 * 
	 * @param conveyer
	 * 		the conveyer object representing the immutable state of this specific tile
	 * 
	 */
	public ConveyerTile(Conveyer conveyer) {
		this.conveyer = conveyer;
	}
	
	public Conveyer getConveyer() {	return conveyer; }
	
	/**
	 * 
	 * Draws the given conveyer belt to the given location in pixels
	 * 
	 * @param g2d
	 * 
	 * @param drawToX
	 * 		x location to draw (pixels)
	 * 
	 * @param drawToY
	 * 		y location to draw (pixels)
	 * 
	 */
	@Override public void paint(Graphics2D g2d, int drawToX, int drawToY, WorldResource rsrc) {
		conveyer.paint(g2d, drawToX, drawToY, animationStep, rsrc);
	}
	
	@Override public int getId() { return conveyer.getId(); }
	
	/**
	 * 
	 * Updates the state of this conveyer belt. Should be called every tick.
	 * 
	 */
	@Override public void update() {
		if (readyToAnimate() ) {
			if (animationStep >= 4)  animationStep = 0;
			else					++animationStep;
		}
	}
	
	// See docs in HazardTile for this method; uses the same principles
	private boolean readyToAnimate() {
		if (timeToNextFrame >= TICKS_BETWEEN_ANIMATIONS) {
			timeToNextFrame = 0;
			return true;
		} else {
			++timeToNextFrame;
			return false;
		}
	}
	
	@Override public boolean isThru() { return true; }
	
	@Override public boolean isSolid() { return false; }
	
	@Override public void reset() { /* No op */ }

}
