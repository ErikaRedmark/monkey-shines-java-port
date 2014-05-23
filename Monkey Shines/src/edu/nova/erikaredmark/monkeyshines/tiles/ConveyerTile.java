package edu.nova.erikaredmark.monkeyshines.tiles;

import java.awt.Graphics2D;

import edu.nova.erikaredmark.monkeyshines.Conveyer;

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
	public void paint(Graphics2D g2d, int drawToX, int drawToY) {
		conveyer.paint(g2d, drawToX, drawToY, animationStep);
	}
	
	/**
	 * 
	 * Updates the state of this conveyer belt. Should be called every tick.
	 * 
	 */
	public void update() {
		if (animationStep > 4)  animationStep = 0;
		else					++animationStep;
	}
	
	@Override public boolean isThru() { return true; }

}
