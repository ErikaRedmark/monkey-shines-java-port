package org.erikaredmark.monkeyshines.tiles;

import java.awt.Graphics2D;

import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.resource.WorldResource;

/**
 * 
 * Represents a specific collapsible tile in the level. There is no analog 'Collapsible' class to
 * store immutable state like the other tiles; the only thing immutable about this is the id for
 * drawing. Everything else is state dependent.
 * <p/>
 * Tiles collapse a bit when {@code collapse} is called. Collapsible tiles have 10 frames of
 * animation and 20 ticks of collapsibility before they are gone.
 * <p/>
 * When not collapsed, they act like thru tiles. When collapsed, they act like scene tiles.
 * 
 * @author Erika Redmark
 *
 */
public final class CollapsibleTile implements TileType {
	// Only immutable state; id of which collapsing tile this in graphics resource
	private final int id;

	// the level of damage the tile has currently taken. Once it hits 20 that effectively 
	// makes it 'dead'. Lower values mean LESS damage.
	int damage;
	
	public CollapsibleTile(final int id) {
		this.id = id;
		this.damage = 0;
	}

	@Override public int getId() { return id; }
	
	/**
	 * 
	 * {@inheritDoc}
	 * <p/>
	 * Collapsible tiles take on the characterstics of thrus when not destroyed, but lose that power
	 * when destroyed.
	 * 
	 */
	@Override public boolean isThru() { return damage < 20; }
	
	@Override public boolean isSolid() { return false; }
	
	@Override public boolean isLandable() { return isSolid() || isThru(); } 

	@Override public void reset() { 
		damage = 0;
	}	
	
	@Override public void update() { 
		/* Damaging a tile is done per tick IF bonzo is standing on it. We don't have that context here so
		 * update does nothing. Collision logic must check for collapsing tile and call specific methods.
		 */
	}
	
	/**
	 * 
	 * Performs 1 tick of collapsing. This is typically called if bonzo is standing on a collapsible tile.
	 * This method can safely be called if the collapsible tile is gone; it just won't do anything.
	 * 
	 */
	public void collapse() {
		if (damage < 20)  ++damage;
	}
	
	/**
	 * 
	 * Paints the given collapsible tile onto the given graphics context, at the given position.
	 * The appearence of this tile if fully controlled by its own state.
	 * 
	 */
	@Override public void paint(Graphics2D g2d, int drawToX, int drawToY, WorldResource rsrc) {
		// 10 frames of animation, 20 points of damage.
		int drawFromX = (damage / 2) * GameConstants.TILE_SIZE_X;
		// y position is controlled 100% by immutable id
		int drawFromY = id * GameConstants.TILE_SIZE_Y;
		
		g2d.drawImage(rsrc.getCollapsingSheet(), drawToX , drawToY, 							    // Destination 1 (top left)
					  drawToX + GameConstants.TILE_SIZE_X, drawToY + GameConstants.TILE_SIZE_Y,     // Destination 2 (bottom right)
					  drawFromX, drawFromY, 													    // Source 1 (top Left)
					  drawFromX + GameConstants.TILE_SIZE_X, drawFromY + GameConstants.TILE_SIZE_Y, // Source 2 (bottom right)
					  null);
	}
}
