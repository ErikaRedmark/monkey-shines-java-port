package org.erikaredmark.monkeyshines.tiles;

import java.awt.Graphics2D;

import org.erikaredmark.monkeyshines.resource.WorldResource;

/**
 * 
 * Interface for indicating classes operate as a 'tile' in the game world. Some tile types are stateless; these
 * are implemented as enumerations. Other types may carry with them some intrinsic state, that is either supplied on
 * creation and never changed or can be changed during course of gameplay.
 * 
 * @author Erika Redmark
 *
 */
public interface TileType {
	
	/**
	 * 
	 * Returns the id of the tile. All tiles are backed by some sprite sheet, which contains
	 * them, and/or other frames of animation of them. Each sheet can contain many different
	 * looking versions of the same basic tile. The id determines which unique version it is,
	 * which is important for drawing routines.'
	 * <p/>
	 * Note that a given sprite sheet may not have enough graphics to support a given tile
	 * of a given id. This can happen when loading a world with a cut-down world resource
	 * folder. It is the responsibility of other classes to decide what happens in these
	 * cases.
	 * 
	 * @return
	 * 		the id of the tile, indicating which version to draw from the sprite sheet.
	 * 
	 */
	int getId();
	
	/**
	 * 
	 * Determines if a given tile is a 'thru' tile, meaning Bonzo can jump through it but
	 * he can still land on it and stand. The dedicated THRU tiles are an example of this
	 * but other tile types may have the same properties, such as conveyers.
	 * 
	 * @return
	 * 		{@code true} if the given tile type can be jumped through and stood on,
	 * 		{@code false} if otherwise. Note that {@code false} does NOT imply solid
	 * 		or non-solid, simply that it isn't a thru tile
	 * 
	 */
	boolean isThru();
	
	/**
	 * 
	 * Determines if a given tile is completely solid. Bonzo cannot in any way move
	 * through this tile.
	 * 
	 * @return
	 * 		{@code true} if solid, {@code false} if otherwise.
	 * 
	 */
	boolean isSolid();
	
	/**
	 * 
	 * Determines if a given tile can be landed on.
	 * {@code return isSolid() || isThru(); }
	 * TODO If codebase is upgraded to Java 8 this SHOULD be a default method. isLandable is ALWAYS
	 * an either/or against isSolid or isThru.
	 * 
	 * @return
	 */
	boolean isLandable();
	
	/**
	 * 
	 * Updates the given tile type IF it has state. This method will do nothing on stateless tile
	 * types
	 * 
	 */
	void update();
	
	/**
	 * 
	 * Some tile types have a concept of reset. Resetting is done whenver a level is reloaded, such
	 * as from death or just moving back onto the screen.
	 * 
	 */
	void reset();
	
	/**
	 * 
	 * Paints the given tile on the world. Container classes store location, and supply it to paint
	 * method in pixel position. The tile will draw according to its internal id, its state, and
	 * the current world resource.
	 * <p/>
	 * If the tile cannot draw, due to the passed resource not containing the appropriate graphics,
	 * this method will should NOT throw an exception. implementations should supply a placeholder graphic 
	 * or not draw at all (placeholder preferred)
	 * 
	 * @param g2d
	 * 		graphics context to draw to
	 * 
	 * @param drawToX
	 * 		x position in pixels to draw to
	 * 
	 * @param drawToY
	 * 		y position in pixels to draw to
	 * 
	 * @param rsrc
	 * 		graphics resources where the source graphics will be taken from
	 * 
	 */
	void paint(Graphics2D g2d, int drawToX, int drawToY, WorldResource rsrc);
	
}
