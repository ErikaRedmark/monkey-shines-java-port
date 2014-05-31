package edu.nova.erikaredmark.monkeyshines;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import edu.nova.erikaredmark.monkeyshines.resource.WorldResource;
import edu.nova.erikaredmark.monkeyshines.tiles.CollapsibleTile;
import edu.nova.erikaredmark.monkeyshines.tiles.ConveyerTile;
import edu.nova.erikaredmark.monkeyshines.tiles.HazardTile;
import edu.nova.erikaredmark.monkeyshines.tiles.StatelessTileType;
import edu.nova.erikaredmark.monkeyshines.tiles.TileType;

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
	
	private static final Tile NO_TILE = new Tile(ImmutablePoint2D.of(0, 0), 0, StatelessTileType.NONE);
	
	/* Note: Stateful tile types, such as hazards, use their own ids for the global tile id. 
	 * only stateless tile types use tileId, since they are effectively storing their 'state'
	 * outside of their actual type. 
	 */
	private Tile(final ImmutablePoint2D point, final int tileId, final TileType type) {
		this.tileX = point.x() * GameConstants.TILE_SIZE_X;
		this.tileY = point.y() * GameConstants.TILE_SIZE_X;
		// To be honest, this code makes me angry. But its the price for sometimes storing the tileId
		// in Tile for stateless tiles, and sometimes storing it in the actual TileType object for
		// stateful tiles. TODO make TileType objects ALWAYS store id instead.
		assert type != null : "Null tile type passed to tile at " + tileX + ", " + tileY;
		this.tileId =   type instanceof StatelessTileType
					  ? tileId
					  : 	type instanceof HazardTile
					  	  ? ((HazardTile)type).getHazard().getId()
					  	  :		type instanceof ConveyerTile
					  	  	  ? ((ConveyerTile)type).getConveyer().getId()
					  	  	  : 	type instanceof CollapsibleTile
					  	  	  	  ? ((CollapsibleTile)type).getId()
					  	  	  	  : -1;
		if (this.tileId == -1)  throw new RuntimeException("Unknown tile type " + type + " for tile " + tileX + ", " + tileY);
		
		this.type = type;	
	}
	
	/**
	 * 
	 * Sets the graphics resources for this tile. If the object has not been skinned yet, this method will set the object
	 * as ready for drawing.
	 * <p/>
	 * <strong>This method must be called if there are any changes to the graphics of the resource!</strong>. Computations
	 * based on blitting locations are done once and cached for speed. If the dimensions of any images changes without an
	 * explicit reskin there WILL be non-determisitic graphical glitches.
	 * <p/>
	 * Does nothing if the tile in question is not drawable (is of type {@code TileType.NONE} )
	 * 
	 * @param rsrc
	 * 		graphics resource to reskin this tile with
	 * 
	 */
	public void skin(final WorldResource rsrc) {
		if (this.type == StatelessTileType.NONE) return;
		if (rsrc == null) throw new IllegalArgumentException("Passed Resource was Null!");
		
		if (this.type instanceof StatelessTileType) {
			this.rsrc = rsrc;
			BufferedImage tileSpriteSheet = rsrc.getTilesheetFor(this.type);
			sheetCols = tileSpriteSheet.getWidth() / GameConstants.TILE_SIZE_X;
			
			// Assume Integer division.
			// No need to store id after we computed the bounds for the graphics.
			tileDrawCol = (tileId % sheetCols) * GameConstants.TILE_SIZE_X;
			tileDrawRow = (tileId / sheetCols) * GameConstants.TILE_SIZE_Y;
			
			// Sanity check: If the tileId goes out of bounds of the tile sheet, there is an issue. Print out that there
			// is a rouge invisible tile.
			// TODO Note: Document somehow that this DOESN'T prevent invisible tiles from accidentally being inserted by the
			// editor. If the sheet has a fully transparent tile within the rectangle, that is technically valid. Perhaps
			// have tilesheets fully pink everywhere else to communicate a bad-tile so this check always works?
			int sheetRows = tileSpriteSheet.getHeight() / GameConstants.TILE_SIZE_Y;
			if (tileId > sheetCols * sheetRows) {
				System.out.println("" + this + ": Out of graphics range (Given sprite sheet only permits ids up to " + sheetCols * sheetRows);
			}
			
			// TODO change concept of skinning so it applies to these tiles.
		} else if (this.type instanceof HazardTile) {
			// Hazards are not skinned. Tiletype contains all graphics information
			// Intentionally Empty
		} else if (this.type instanceof ConveyerTile) {
			// Conveyers are not skinned. Tiletype contains all graphics information
			// Intentionally Empty
		} else if (this.type instanceof CollapsibleTile) {
			// Collapsibles not skinned either.
		} else {
			throw new RuntimeException("No drawing handling for this tile: " + toString() );
		}
		
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
		// No drawing for empty tiles
		if (this.type == StatelessTileType.NONE) return;
		if (!(isSkinned) ) {
			System.err.println("Cant draw " + toString() + ": not skinned!");
		}
		
		// TODO not sure about polymorphism but CAN put all this code into HazardTile instead
		// of here.
		if (this.type instanceof HazardTile) {
			// This only handles basic animation: animations steps 0 and 1. Above need to delegate to
			// the explosion sprite sheet.
			final HazardTile hazard = (HazardTile)this.type;
			if (hazard.isDead() )  return;
			
			if (!(hazard.isExploding() ) ) {
				// tileDrawCol and row are not relevant: hazards know how to draw themselves (because they are stateful)
				hazard.getHazard().paint(g2d, tileX, tileY, hazard.getAnimationStep() );
			} else {
				// TODO draw explosions
			}
		} else if (this.type instanceof ConveyerTile) {
			((ConveyerTile)this.type).paint(g2d, tileX, tileY);
		} else if (this.type instanceof CollapsibleTile) {
			((CollapsibleTile)this.type).paint(g2d, tileX, tileY);
		} else {
			g2d.drawImage(rsrc.getTilesheetFor(this.type), tileX, tileY, tileX + GameConstants.TILE_SIZE_X, tileY + GameConstants.TILE_SIZE_Y, //DEST
					tileDrawCol, tileDrawRow, tileDrawCol + GameConstants.TILE_SIZE_X, tileDrawRow + GameConstants.TILE_SIZE_Y, // SOURCE
					null); // OBS
		}
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
	 * Returns the id of this tile, which indicates what graphic is used when drawn.
	 * 
	 * @return
	 * 		the tile id
	 */
	public int getTileId() { return this.tileId; }

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
		for (int i = 0; i < 20; i++) {
			for (int j = 0; j < 32; j++) {
				tiles[i][j] = NO_TILE;
			}
		}
		return tiles;
	}

	/**
	 * 
	 * Determines if this tile space is simply empty space. 
	 * TODO this may need to include destroyed hazards.
	 * 
	 * @return
	 * 		{@code true} if there is no tile here, {@code false} if otherwise
	 * 
	 */
	public boolean isEmpty() {
		return this.type == StatelessTileType.NONE;
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
	public static Tile emptyTile() {
		return NO_TILE;
	}
	
	
	/**
	 * 
	 * String representation of the Tile. This is intended for debugging purposes, and should never be displayed to the
	 * user in any other circumstances.
	 * 
	 */
	@Override public String toString() {
		return "Tile Id [" + tileId + "] " 
				+ "at row " + tileX / GameConstants.TILE_SIZE_X
				+ " col " + tileY / GameConstants.TILE_SIZE_Y 
				+ " of type " + type
				+ (isSkinned ? "(Skinned)" : "");
	}

}
