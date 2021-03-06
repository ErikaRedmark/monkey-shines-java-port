package org.erikaredmark.monkeyshines.tiles;

import java.awt.image.BufferedImage;

import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.resource.AwtWorldGraphics;
import org.erikaredmark.monkeyshines.resource.SlickWorldGraphics;
import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.newdawn.slick.Image;

/**
 * 
 * How the tile reacts to the game world. These tile types are stateless; their nature represents all that needs to be known
 * about a tile at a given point. Stateless tile types only ever differ in their id and underlying type. However, each instance
 * IS unique. This is deliberate: Many collision detectiong algorithms can be simplified by not sharing instances (to determine if
 * two points map to the same tile)
 * 
 * <strong> Stateless</strong>
 * <ul>
 * <li> Solid: Can not be passed. Bonzo may stand on it, but can not otherwise go through it. There is one exception:
 * 		if bonzo starts a screen inside of a solid, he may move out of it (and thus through it) but not back into it
 * 		again.</li>
 * <li> Thru: Bonzo may stand on it, but if he is walking through it on the side it will not impede his movement.
 * 		He may not, however, go down through it (it is standable only)</li>
 * <li> Scene: Has no effect on Bonzo. Has no effect on anything. Merely a stand-in for graphics. </li>
 * <li> None: No tile. Acts as a null-safe way of simply saying "no tile" </li>
 * </ul>
 * 
 * @author Erika Redmark
 *
 */
public class CommonTile implements TileType {
	
	private final int id;
	private final StatelessTileType underlyingType;
	
	// Private state variables for drawing. Only recomputed when paint is called with a new resource,
	// which is very rare.
	private int tileDrawRow;
	private int tileDrawCol;
	
	public static final CommonTile NONE = new CommonTile(0, StatelessTileType.NONE);
	
	private CommonTile(int id, StatelessTileType type) {
		this.id = id;
		this.underlyingType = type;
	}
	
	/** source Tile drawing row for rendering operations */
	public int getTileDrawRow() { return tileDrawRow; }
	/** source tile drawing column for rendering operations */
	public int getTileDrawCol() { return tileDrawCol; }
	
	/**
	 * 
	 * Static factory to return instances of common tiles. If it is decided to share instances
	 * and, going through this point will make it easier.
	 * <p/>
	 * Creates a new common tile instance. Common tiles have no state and draw most
	 * behaviour from a set of common enumerated types. This class is merely the container
	 * for the unique id and to give a unique constructed object per tile on a level.
	 * 
	 * @param id
	 * 
	 * @param type
	 * 
	 * @param rsrc
	 * 		the initial expected world resource this tile will be drawn with. This causes
	 * 		drawing data to be computed before the paint method. This prevents slowdown
	 * 		for tiles never drawn yet when changing screens in the game
	 * 
	 * @return
	 * 		new instance of this object
	 * 
	 */
	public static CommonTile of(int id, StatelessTileType type, WorldResource rsrc) {
		CommonTile tile =  new CommonTile(id, type);
		tile.recomputeDrawState(rsrc);
		return tile;
	}
	
	/**
	 * 
	 * Static factory to return instances of common tiles. If it is decided to share instances
	 * and, going through this point will make it easier.
	 * <p/>
	 * Creates a new common tile instance. Common tiles have no state and draw most
	 * behaviour from a set of common enumerated types. This class is merely the container
	 * for the unique id and to give a unique constructed object per tile on a level.
	 * 
	 * @param id
	 * 
	 * @param type
	 * 
	 * @return
	 * 		new instance of this object
	 * 
	 */
	public static CommonTile of(int id, StatelessTileType type) {
		return new CommonTile(id, type);
	}
	
	public StatelessTileType getUnderlyingType() { return underlyingType; }

	@Override public int getId() { return id; }

	@Override public boolean isThru() { return underlyingType == StatelessTileType.THRU; }
	
	@Override public boolean isSolid() { return underlyingType == StatelessTileType.SOLID; }
	
	@Override public boolean isLandable() { return isSolid() || isThru(); } 

	@Override public void update() { /* No state; never updates */ }
	
	@Override public void reset(boolean oddElseEven) { /* No op */ }
	
	@Override public CommonTile copy() {
		return new CommonTile(id, underlyingType);
	}
	
	/**
	 * Recomputes the location in the sprite sheet that this particular tile should draw from. This depends
	 * on two factors: the id of the tile, and the dimensions of the sprite sheet. To prevent slowdown, this
	 * is only recomputed when the world is painted with a different resource. This should only be called, in
	 * game, once per game, and for the level editor once during load, and any time a new tile is created.
	 * <p/>
	 * It is an error to call this with a NONE tile.
	 * <p/>
	 * This computation functions regardless of the underlying rendering type of the resource (AWT or Slick)
	 * 
	 * @param rsrc
	 * 		the new resource to recompute values for
	 */
	public void recomputeDrawState(WorldResource rsrc) {
		if (rsrc.isSlickGraphics()) {
			recomputeDrawStateSlick(rsrc.getSlickGraphics());
		} else {
			recomputeDrawStateAwt(rsrc.getAwtGraphics());
		}
	}
	
	public void recomputeDrawStateAwt(AwtWorldGraphics awtGraphics) {
		BufferedImage tileSpriteSheet = awtGraphics.getStatelessTileTypeSheet(this.underlyingType);
		int sheetCols = tileSpriteSheet.getWidth() / GameConstants.TILE_SIZE_X;
		int sheetRows = tileSpriteSheet.getHeight() / GameConstants.TILE_SIZE_Y;
		recomputeDrawState_internal(sheetCols, sheetRows);
	}
	
	public void recomputeDrawStateSlick(SlickWorldGraphics slickGraphics) {
		Image tileSpriteSheet = slickGraphics.getStatelessTileTypeSheet(this.underlyingType);
		int sheetCols = tileSpriteSheet.getWidth() / GameConstants.TILE_SIZE_X;
		int sheetRows = tileSpriteSheet.getHeight() / GameConstants.TILE_SIZE_Y;
		recomputeDrawState_internal(sheetCols, sheetRows);
	}
	
	private void recomputeDrawState_internal(int sheetCols, int sheetRows) {
		// Assume Integer division.
		// No need to store id after we computed the bounds for the graphics.
		tileDrawCol = (id % sheetCols) * GameConstants.TILE_SIZE_X;
		tileDrawRow = (id / sheetCols) * GameConstants.TILE_SIZE_Y;
		
		// Sanity check: If the tileId goes out of bounds of the tile sheet, there is an issue. Print out that there
		// is a rouge invisible tile.
		// TODO Note: Document somehow that this DOESN'T prevent invisible tiles from accidentally being inserted by the
		// editor. If the sheet has a fully transparent tile within the rectangle, that is technically valid. Perhaps
		// have tilesheets fully pink everywhere else to communicate a bad-tile so this check always works?
		if (id > sheetCols * sheetRows) {
			System.err.println("" + this + ": Out of graphics range (Given sprite sheet only permits ids up to " + sheetCols * sheetRows);
		}
	}
	
	public enum StatelessTileType {
		SOLID,
		THRU,
		SCENE,
		NONE;
	}
	
	@Override public boolean equals(Object o) {
		if (o == this)  return true;
		if (!(o instanceof CommonTile) )  return false;
		
		CommonTile other = (CommonTile) o;
		
		return    this.id == other.id
			   && this.underlyingType.equals(other.underlyingType);
	}
	
	@Override public int hashCode() {
		int result = 17;
		result += result * 31 + underlyingType.hashCode();
		result += result * 31 + id;
		return result;
	}
	
	@Override public String toString() {
		return   this.equals(CommonTile.NONE)
			   ? "No Tile"
			   : "Common Tile of id " + id + " with underlying type " + underlyingType;
	}

}
