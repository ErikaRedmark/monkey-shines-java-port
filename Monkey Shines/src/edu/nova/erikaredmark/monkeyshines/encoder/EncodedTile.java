package edu.nova.erikaredmark.monkeyshines.encoder;

import edu.nova.erikaredmark.monkeyshines.ImmutablePoint2D;
import edu.nova.erikaredmark.monkeyshines.Tile;
import edu.nova.erikaredmark.monkeyshines.Tile.TileType;

/**
 * A serialisable class that maintains the static state data of everything required to recreate a tile. This includes
 * the following things:
 * <br/>
 * <ul>
 * <li> Id of the tile, indicating the graphic to use </li>
 * <li> Type of the tile, indicating both which sprite sheet to draw from as well as the property of the tile</li>
 * <li> row position of the tile</li>
 * <li> col position of the tile</li>
 * </ul>
 * <strong> Only these proxy classes are serialised.</strong>. The regular classes are free to evolve as long as no
 * changes are made to the static contents of the level.
 * <p/>
 * Instances of this class are immutable.
 * 
 * @author Erika Redmark
 */
public final class EncodedTile {
	
	private final int id;
	/* Enums are OKAY to serialize.	*/
	private final TileType type;
	private final ImmutablePoint2D location;
	
	private EncodedTile(final int id, final TileType type, final ImmutablePoint2D location) {
		this.id = id; this.type = type; this.location = location;
	}
	
	/**
	 * Creates a new encoded tile from the given tile instance. 
	 * 
	 * @param t
	 * @return
	 * 		new encoded tile
	 */
	public static EncodedTile from(final Tile t) {
		final int _id = t.getTileId();
		final TileType _type = t.getType();
		final ImmutablePoint2D _location = ImmutablePoint2D.from(t.getLocation() );
		
		return new EncodedTile(_id, _type, _location);
	}
	
}
