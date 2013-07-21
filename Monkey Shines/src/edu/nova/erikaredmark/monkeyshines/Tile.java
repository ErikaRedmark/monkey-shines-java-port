package edu.nova.erikaredmark.monkeyshines;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import edu.nova.erikaredmark.monkeyshines.encoder.EncodedTile;
import edu.nova.erikaredmark.monkeyshines.graphics.WorldResource;

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
	private final int tileId; // the numerical value for this tiles position in the tile sheet.
	private final TileType type;
	
	// Recomputed ONLY on a re-skin!!!
	private       int tileDrawRow;
	private       int tileDrawCol;
	private       int sheetCols;

	private WorldResource rsrc;
	private boolean isSkinned = false;
	
	/**
	 * 
	 * Creates an instance of this object from its encoded for.
	 * 
	 * @param value
	 * 
	 * @return
	 * 
	 */
	public static Tile inflateFrom(EncodedTile encodedTile) {
		return new Tile(encodedTile.getLocation(), encodedTile.getId(), encodedTile.getType() );
	}
	
	private Tile(final ImmutablePoint2D point, final int tileId, final TileType type) {
		this.tileX = point.x();
		this.tileY = point.y();
		this.tileId = tileId;
		this.type = type;	
	}
	
	/**
	 * 
	 * Sets the graphics resources for this tile. If the object has not been skinned yet, this method will set the object
	 * as ready for drawing.
	 * <p/>
	 * <strong>This method must be called if there are any changes to the graphics of the resource!</strong>. Computations
	 * based on blitting locations are done once and cached for speed. If the dimensions of any images changes without an
	 * explicit reskin there WILL be non-determisitic graphical glitches!
	 * 
	 * @param rsrc
	 * 		graphics resource to reskin this tile with
	 * 
	 */
	public void skin(final WorldResource rsrc) {
		this.rsrc = rsrc;
		BufferedImage tileSpriteSheet = rsrc.getTilesheetFor(this.type);
		sheetCols = tileSpriteSheet.getWidth() / GameConstants.TILE_SIZE_X;
		
		// Assume Integer division.
		// No need to store id after we computed the bounds for the graphics.
		tileDrawCol = (tileId % sheetCols) * GameConstants.TILE_SIZE_X;
		tileDrawRow = (tileId / sheetCols) * GameConstants.TILE_SIZE_Y;
		isSkinned = true;
	}
	
	public boolean isSkinned() { return isSkinned; }
	
	/**
	 * Returns the location of this tile, on a row/col basis, in the level
	 * 
	 * @return
	 * 		row/col point for tile
	 */
	public Point2D getLocation() {
		// Convert drawing co-ordinates to row/col co-ordinates
		return Point2D.of(tileX / GameConstants.TILE_SIZE_X, tileY / GameConstants.TILE_SIZE_Y);
	}
	
	public TileType getType() { return this.type; };

	public void paint(Graphics2D g2d) {
		g2d.drawImage(rsrc.getTilesheetFor(this.type), tileX, tileY, tileX + GameConstants.TILE_SIZE_X, tileY + GameConstants.TILE_SIZE_Y, //DEST
				tileDrawCol, tileDrawRow, tileDrawCol + GameConstants.TILE_SIZE_X, tileDrawRow + GameConstants.TILE_SIZE_Y, // SOURCE
				null); // OBS
	}
	
	/**
	 * Returns the id of this tile, which indicates what graphic is used when drawn.
	 * 
	 * @return
	 * 		the tile id
	 */
	public int getTileId() { return this.tileId; }
	
	/**
	 * 
	 * How the tile reacts to the game world
	 * <p/>
	 * <ul>
	 * <li> Solid: Can not be passed. Bonzo may stand on it, but can not otherwise go through it. There is one exception:
	 * 		if bonzo starts a screen inside of a solid, he may move out of it (and thus through it) but not back into it
	 * 		again.</li>
	 * <li> Thru: Bonzo may stand on it, but if he is walking through it on the side it will not impede his movement.
	 * 		He may not, however, go down through it (it is standable only)</li>
	 * <li> Scene: Has no effect on Bonzo. Has no effect on anything. Merely a stand-in for graphics. </li>
	 * 
	 * @author Erika Redmark
	 *
	 */
	public enum TileType {SOLID(0), THRU(1), SCENE(2); 
		private int id;
		private static Map<Integer, TileType> int2Type=
			new HashMap<Integer, TileType>();
		
		static {
			for (TileType type : TileType.values()) {
				int2Type.put(type.getId(), type);
			}
		}
		
		private TileType(final int id) { this.id = id; }

		
		public int getId() { return id; }
		public static TileType fromId(final int id) { return int2Type.get(id); }
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
	 * @param
	 * 		the world graphics resource pointer to initialise graphics
	 * 
	 * @return
	 * 		a newly created tile with the given properties. The tile is already skinned and ready to be drawn
	 * 
	 */
	public static Tile newTile(ImmutablePoint2D location, int id, TileType tileType, WorldResource rsrc) {
		Tile tile = new Tile(location, id, tileType);
		tile.skin(rsrc);
		return tile;
	}

}
