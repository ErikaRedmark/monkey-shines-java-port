package edu.nova.erikaredmark.monkeyshines;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

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
	private final BufferedImage tileSpriteSheet;
	
	private final int tileX; // the location of this tile in the world, drawing wise
	private final int tileY; // the location of this tile in the world, drawing wise
	private final int tileId; // the numerical value for this tiles position in the tile sheet.
	
	private final int tileDrawRow;
	private final int tileDrawCol;
	
//	private final int sheetRows; // rows in the sheet, calculated dynamically
	private final int sheetCols; // cols ' ' ' ' ' ' ' ' ' '
	
	private final TileType type;
	
	/**
	 * 
	 * Constructs a new tile. A tile knows its position, its sprite sheet, and its type. It is not drawn until it is 
	 * loaded into a {@link LevelScreen}.
	 * 
	 * @param tileSpriteSheet
	 * 		The sprite to draw from. Whilst not enforced, this sprite sheet for engine reasons SHOULD ALWAYS match the
	 * 		sprite sheet loaded by the world that conforms to this tile's type. For example: a tile of type Solid should
	 * 		always draw from the 'solids' sprite sheet so as not to surprise the player.
	 * 
	 * @param tileY
	 * 		row		
	 * 
	 * @param tileX
	 * 		col
	 * 
	 * @param tileid
	 * 		the id starting from 0 of this tile within its own sprite sheet. This id is used only for graphics purposes
	 * 		and serves no function to gameplay.		
	 * 
	 * @param type
	 * 		The tiles type. See the enumeration docs for more info.
	 * 
	 */
	// TileY is row, TileX is col
	public Tile(final BufferedImage tileSpriteSheet, 
			 	final int tileY, 
			 	final int tileX, 
			 	final int tileId, 
			 	final TileType type) {
		
		this.tileSpriteSheet = tileSpriteSheet;
		this.type = type;
		this.tileId = tileId;
		
		// Get Drawing locations to Screen
		this.tileX = tileX * GameConstants.TILE_SIZE_X;
		this.tileY = tileY * GameConstants.TILE_SIZE_Y;
		
//		this.tileid = tileid;
		

		
		// check
		// later
//		sheetRows = tileSpriteSheet.getHeight() / GameConstants.TILE_SIZE_Y;
		sheetCols = tileSpriteSheet.getWidth() / GameConstants.TILE_SIZE_X;
		
		// Assume Integer division.
		// No need to store id after we computed the bounds for the graphics.
		tileDrawCol = (tileId % sheetCols) * GameConstants.TILE_SIZE_X;
		tileDrawRow = (tileId / sheetCols) * GameConstants.TILE_SIZE_Y;
	}
	
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
		g2d.drawImage(tileSpriteSheet, tileX, tileY, tileX + GameConstants.TILE_SIZE_X, tileY + GameConstants.TILE_SIZE_Y, //DEST
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
}
