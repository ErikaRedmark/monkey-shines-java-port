package org.erikaredmark.monkeyshines.tiles;

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
	 * <p/>
	 * The reset function is passed whether the tile is currently in an odd numbered tile location or an
	 * even numbered one. The point of this checkerboarded reset is to stagger some animations so adjacent
	 * hazards don't animate exactly the same. This helps a lot for marquee style animation. Not all tiles
	 * even care about this parameter.
	 * 
	 * @param oddElseEven
	 * 		{@code true} if the tile index is odd, {@code false} if event (or 0) 
	 * 
	 */
	void reset(boolean oddElseEven);
	
	/**
	 * 
	 * Returns a copy of the tile type. Even stateless tile types should return copies, as some collision detection
	 * algorithms require the ability to distinquish between instances. TODO is this really required?
	 * 
	 * @return
	 * 		copy of this tile
	 * 
	 */
	TileType copy();
	
}
