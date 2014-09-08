package org.erikaredmark.monkeyshines;

import java.awt.Graphics2D;

import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.erikaredmark.monkeyshines.tiles.CommonTile;
import org.erikaredmark.monkeyshines.tiles.TileType;

/**
 * 
 * Represents a tile on the map. A tile is not part of any screen until a screen stores a reference to a tile in its
 * memory.
 * <p/>
 * Instances of this class are immutable.
 * 
 * @author Erika Redmark
 *
 */
public class Tile {
	private final int tileX; // the location of this tile in the world, drawing wise
	private final int tileY; // the location of this tile in the world, drawing wise
	private final TileType type;

	// Currently final. Future versions may implement re-skinning tiles to change drawing if ever appropriate.
	private final WorldResource rsrc;
	
	// Only tile without a world resource, since nothing is ever drawn.
	private static final Tile NO_TILE = new Tile(ImmutablePoint2D.of(0, 0), CommonTile.NONE, null);
	
	/**
	 * 
	 * Creates a new tile that is told to draw to the given location with the given parameters
	 * Drawing location is ignored for {@code StatelessTileType.NONE}, as they are never drawn
	 * anyway. Hence it is possible to use a singleton for empty tiles.
	 * 
	 */
	private Tile(final ImmutablePoint2D point, final TileType type, final WorldResource rsrc) {
		this.tileX = point.x() * GameConstants.TILE_SIZE_X;
		this.tileY = point.y() * GameConstants.TILE_SIZE_X;

		assert type != null : "Null tile type passed to tile at " + tileX + ", " + tileY;
		
		this.type = type;
		this.rsrc = rsrc;
	}
	
	public TileType getType() { return this.type; };

	public void paint(Graphics2D g2d) {
		type.paint(g2d, tileX, tileY, rsrc);
	}
	
	/**
	 * 
	 * If this tile has any state that changes automatically between frames, this will update that state.
	 * 
	 */
	public void update() {
		this.type.update();
	}

	/**
	 * 
	 * Creates a new tile with the given location and properties.
	 * 
	 * @param location
	 * 		the x (column) and y (row) that this tile is to appear on
	 * 
	 * @param id
	 * 		the id of the tile, for display purposes
	 * 
	 * @param tileType
	 * 		the type of tile for both gameplay and display purposes
	 * 
	 * @param rsrc
	 * 		initial world resource
	 * 
	 * @return
	 * 		a newly created tile with the given properties. The tile is already skinned and ready to be drawn
	 * 
	 */
	public static Tile newTile(ImmutablePoint2D location, TileType tileType, WorldResource rsrc) {
		return new Tile(location, tileType, rsrc);
	}

	/** 
	 * 
	 * Creates an empty map of tiles, all initialised to a tile type of {@code None}. The 2D array is always generated
	 * to the proper size and no entries are null
	 * 
	 * @return
	 * 		a new 2d array of tiles
	 * 
	 */
	public static Tile[][] createBlankTileMap() {
		Tile[][] tiles = new Tile[20][32];
		for (int i = 0; i < 20; ++i) {
			for (int j = 0; j < 32; ++j) {
				tiles[i][j] = NO_TILE;
			}
		}
		return tiles;
	}

	/** 
	 * 
	 * Returns an empty tile instance. This is instance controlled: There is only one immutable singleton representing
	 * an empty tile. Tile maps should NEVER contain null. In the absence of a tile, they should be set to this
	 * null object.
	 * 
	 * @return
	 * 		an empty tile
	 * 
	 */
	public static Tile emptyTile() { return NO_TILE; }
	
	
	/**
	 * 
	 * String representation of the Tile. This is intended for debugging purposes, and should never be displayed to the
	 * user in any other circumstances.
	 * 
	 */
	@Override public String toString() {
		return "Tile type " + type 
				+ "at row " + tileX / GameConstants.TILE_SIZE_X
				+ " col " + tileY / GameConstants.TILE_SIZE_Y 
				+ " of type " + type;
	}

}
