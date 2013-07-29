package edu.nova.erikaredmark.monkeyshines.encoder;

import java.io.Serializable;

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
public final class EncodedTile implements Serializable {
	private static final long serialVersionUID = -8382685431682699547L;
	
	private final int id;
	private final TileType type;
	private final ImmutablePoint2D location;
	
	private static final EncodedTile NO_TILE = new EncodedTile(0, TileType.NONE, ImmutablePoint2D.of(0, 0) );
	
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
	
	/**
	 * 
	 * Creates an empty encoded tile array, indicating no tiles present on the entire screen. All entries are initialised
	 * to empty tiles
	 * 
	 * @return
	 * 
	 */
	public static EncodedTile[][] freshTiles() {
		EncodedTile[][] tiles = new EncodedTile[20][32];
		for (int i = 0; i < 20; i++) {
			for (int j = 0; j < 32; j++) {
				tiles[i][j] = NO_TILE;
			}
		}
		return tiles;
	}

	public int getId() { return id; }
	public TileType getType() { return type; }
	public ImmutablePoint2D getLocation() {	return location; }
	
}
