package edu.nova.erikaredmark.monkeyshines.tiles;

/**
 * 
 * Interface for indicating classes operate as a 'tile' in the game world. Some tile types are stateless; these
 * are implemented as enumerations. Other types may carry with them some intrinsic state, that is either supplied on
 * creation and never changed or can be changed during course of gameplay.
 * <p/>
 * This is intended mostly as a marker interface, but a few helpful methods may be added.
 * 
 * @author Erika Redmark
 *
 */
public interface TileType {
	
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
	
}
