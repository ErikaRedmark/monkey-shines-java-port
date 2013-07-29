package edu.nova.erikaredmark.monkeyshines;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;

import edu.nova.erikaredmark.monkeyshines.Tile.TileType;
import edu.nova.erikaredmark.monkeyshines.encoder.EncodedLevelScreen;
import edu.nova.erikaredmark.monkeyshines.encoder.EncodedSprite;
import edu.nova.erikaredmark.monkeyshines.encoder.EncodedTile;
import edu.nova.erikaredmark.monkeyshines.graphics.WorldResource;

/**
 * Improving the screen-by-screen architecture is NOT something I will be doing.
 * This provides the classic Monkey Shines feel: Each screen is one part of a level. Going off to the side of a screen
 * sends you to the next screen
 * <p/>
 * The initial screen is screen 1000. The Bonus screen can be determined by a constant (To allow more flexibility; the 
 * original used 10000.)
 * <p/>
 * This contains a lot of elements similar to World, but World is what contains the objects. These are only pointers.
 * The large number of pointers allow the LevelScreen to draw itself and handle it's own collisions.
 * <p/>
 * Changes to a level screen are not persisted to a level. The level editor must use {@link LevelScreenEditor} objects
 * that wrap level screens and store a list of changes to them to write out level data. This object, by itself, only
 * represents the level as it is in one-instance of the game.
 * 
 */
public class LevelScreen {
	
	// Initialisation datas for a level screen
	private final int screenId;
	private final int backgroundId;
	private final Tile screenTiles[][]; // 20 rows, 32 cols
	private final ImmutablePoint2D bonzoStart;
	private final List<Sprite> spritesOnScreen;
	
	// state information for the screen
	private       Point2D bonzoCameFrom;

	// Graphics
	// These are all pointers to what is stored in world.
	private WorldResource rsrc;
	private boolean isSkinned = false;
	
	// Background to get

	
	/**
	 * 
	 * Creates an instance of this object from its encoded for.
	 * 
	 * @param value
	 * 
	 * @return
	 * 
	 */
	public static LevelScreen inflateFrom(EncodedLevelScreen screen) {
		final int screenId = screen.getId();
		final int backgroundId = screen.getBackgroundId();
		final ImmutablePoint2D bonzoStart = screen.getBonzoLocation();
		
		final Tile[][] screenTiles = new Tile[20][32];
		final EncodedTile[][] encodedTiles = screen.getTiles();
		for (int i = 0; i < screenTiles.length; i++) {
			for (int j = 0; j < screenTiles[i].length; j++) {
				screenTiles[i][j] = Tile.inflateFrom(encodedTiles[i][j]);
			}
		}
		
		final List<EncodedSprite> encodedSprites = screen.getSprites();
		final List<Sprite> spritesOnScreen = new ArrayList<>();
		for (EncodedSprite encSprite : encodedSprites) {
			spritesOnScreen.add(Sprite.inflateFrom(encSprite) );
		}
		
		return new LevelScreen(screenId, backgroundId, screenTiles, bonzoStart, spritesOnScreen);
		
	}
	
	/**
	 * 
	 * Creates an empty level screen initialised to no tiles or sprites with a background id of 0.
	 * 
	 * @param screenId
	 * 		the id of the screen, which must match with the id of the key that maps to this screen value in the world
	 * 		hash map
	 * 
	 * @param rsrc
	 * 		a graphics resource to skin this level
	 * 
	 * @return
	 * 		a new level screen
	 * 
	 */
	public static final LevelScreen newScreen(int screenId, WorldResource rsrc) {
		LevelScreen screen = new LevelScreen(screenId,
							                 0,
							                 Tile.createBlankTileMap(),
							                 ImmutablePoint2D.of(0, 0),
							                 new ArrayList<Sprite>() );
		screen.skin(rsrc);
		return screen;
	}
	

	
	private LevelScreen(final int screenId, 
						final int backgroundId,
						final Tile[][] screenTiles, 
						final ImmutablePoint2D bonzoStart, 
						final List<Sprite> spritesOnScreen) {
		
		this.screenId = screenId;
		this.backgroundId = backgroundId;
		this.screenTiles = screenTiles;
		this.bonzoStart = bonzoStart;
		this.spritesOnScreen = spritesOnScreen;
	}
	
	public void skin(final WorldResource rsrc) {
		this.rsrc = rsrc;
		for (Sprite s : spritesOnScreen) {
			s.skin(rsrc);
		}
		
		for (Tile[] tileArray : screenTiles) {
			for (Tile t : tileArray) {
				t.skin(rsrc);
			}
		}
		
		isSkinned = true;
	}
	
	public boolean isSkinned() { return isSkinned; }
	
	/** Returns the screen id of this screen																			*/
	public int getId() { return this.screenId; }
	
	/** Returns the background id. This indicates which background to display for the screen.							*/
	public int getBackgroundId() { return backgroundId; }
	
	/**
	 * 
	 * Gets the starting position of Bonzo in the level. The returned point is immutable: Use {@code Point2D#from(ImmutablePoint2D)}
	 * to get a mutable version
	 * 
	 * @return
	 * 		the location bonzo starts on this level. Never {@code null}
	 */
	public ImmutablePoint2D getBonzoStartingLocation() {
		return this.bonzoStart;
	}
	
	/**
	 * 
	 * Returns a new point representing a location on the screen that bonzo entered from. The returned point may be freely
	 * modified and assigned as-is to any object.
	 * 
	 * @return
	 * 		a new instance of a point that represents the location bonzo entered this screen. This initially is set to his
	 * 		starting location on the screen but is changed as he moves through the screens. Value never {@code null}
	 * 
	 */
	public Point2D newPointFromWhereBonzoCame() {
		return Point2D.of(bonzoCameFrom);
	}
	
	/**
	 * Called when Bonzo enters the screen from another Screen. Sets the location he came from so if he dies on this screen, 
	 * he can return to that position. A copy of the passed point is made, so that object may be re-used.
	 * 
	 * @param bonzoCameFrom 
	 * 		the location Bonzo entered the screen from
	 */
	public void setBonzoCameFrom(final Point2D bonzoCameFrom) {
		this.bonzoCameFrom = Point2D.of(bonzoCameFrom);
	}
	
	/**
	 * If, for some reason, the location Bonzo Came from becomes invalid, this resets it.
	 */
	public void resetBonzoCameFrom() {
		this.bonzoCameFrom = Point2D.from(bonzoStart);
	}
	
	// Sprites
	/**
	 * Sprites move around the screen. This makes them return to where they spawn when the screen is first entered. 
	 * Typically called alongside resetBonzo when Bonzo dies.
	 */
	public void resetSprites() {
		if (spritesOnScreen != null) {
			for (Sprite nextSprite : spritesOnScreen) {
				nextSprite.resetSpritePosition();
			}
		}
	}

	
	// TODO Refactor these functions into one. They do basically the same thing.
	// The actual location on screen for bonzoX and Y, NOT the tile position
	/**
	 * 
	 * checks
	 * 
	 * @param bonzoX
	 * @param bonzoY
	 * @return
	 */
	public boolean checkForTile(int bonzoX, int bonzoY) {
		int tileX = bonzoX / GameConstants.TILE_SIZE_X;
		int tileY = bonzoY / GameConstants.TILE_SIZE_Y;
		// If out of bounds, allow to slip by
		if (tileX < 0 || tileX >= GameConstants.TILES_IN_ROW || tileY < 0 || tileY >= GameConstants.TILES_IN_COL)
			return false;
		// if there is no tile, well, false
		if (screenTiles[tileY][tileX].isEmpty() ) {
			return false;
		}
		if (screenTiles[tileY][tileX].getType() == TileType.SOLID )
			return true;
		return false;
	}
	
	// Careful! This is return by reference
	public List<Sprite> getSpritesOnScreen() {
		return spritesOnScreen;
	}
	
	/**
	 * Returns either nothing if there is no tile (or a non-solid tile). Otherwise, returns the TileType of the solid tile
	 * 
	 * @param bonzoX
	 * 		position of bonzo to look for tile.
	 * 
	 * @param bonzoY
	 *
	 * @return
	 * 		a tile type if the tile exists AND is solid, or nothing if there is no solid tile as ground
	 */
	public Optional<TileType> checkForGroundTile(int bonzoX, int bonzoY) {
		int tileX = bonzoX / GameConstants.TILE_SIZE_X;
		int tileY = bonzoY / GameConstants.TILE_SIZE_Y;
		// If out of bounds, allow to slip by
		if (tileX < 0 || tileX >= GameConstants.TILES_IN_ROW || tileY < 0 || tileY >= GameConstants.TILES_IN_COL)
			return Optional.absent();
		// if there is no tile, well, false
		if (screenTiles[tileY][tileX].isEmpty() ) {
			return Optional.absent();
		}
		
		TileType type = screenTiles[tileY][tileX].getType();
		
		if (type == TileType.SOLID || type == TileType.THRU) return Optional.of(type);
		else return Optional.absent();
	}
	
	/*
	 * Editor functions. None of these effects are saved until the level is saved.
	 * Save often!
	 * @param g2d
	 *
	 */
	
	/**
	 * Sets the tile at tileX, tileY, to the indicated tile.
	 * 
	 * @param tileX x location, terms of grid, not pixels.
	 * 
	 * @param tileY y location, terms of grid, not pixels.
	 * 
	 * @param tileType Whether this is a Solid, Thru, or scenery tile.
	 * 
	 * @param tileId  the ID of the tile, which is basically the graphic to use when rendering.
	 * 
	 * @throws 
	 * 		IllegalArgumentException
	 * 			if the given x or y coordinate is outside of the range {@code (32[x] by 20[y]) }
	 * 		IllegalStateException
	 * 			if the screen has not been skinned yet. Tiles can not be added until there is a graphics
	 * 			resource ready
	 */
	public void setTile(int tileX, int tileY, TileType tileType, int tileId) {
		if (tileX > 31 || tileX < 0) throw new IllegalArgumentException(tileX + " outside of X range [0, 31]");
		if (tileY > 20 || tileY < 0) throw new IllegalArgumentException(tileY + " outside of Y range [0, 19]");
		if (isSkinned == false) throw new IllegalStateException("LevelScreen " + this + " not skinned yet");
		
		screenTiles[tileY][tileX] = Tile.newTile(ImmutablePoint2D.of(tileX, tileY), tileId, tileType, rsrc);
	}
	
	/**
	 * Removes the tile at position tileX, tileY
	 * @param tileX x location, terms of grid, not pixels.
	 * @param tileY y location, terms of grid, not pixels.
	 */
	
	public void eraseTile(int tileX, int tileY) {
		screenTiles[tileY][tileX] = Tile.emptyTile();
	}
	
	/**
	 * Draw background, tiles, and sprites in one swoop.
	 * TODO draws entire screen. May require a more intelligent algorithm to run on slower
	 * machines
	 * @param g2d
	 */
	public void paint(Graphics2D g2d) {
		g2d.drawImage(rsrc.getBackground(this.backgroundId), 0, 0, null);
		for (int i = 0; i < GameConstants.TILES_IN_COL; i++) { // for every tile in the row
			for (int j = 0; j < GameConstants.TILES_IN_ROW; j++) {
				if (screenTiles[i][j] != null)
					screenTiles[i][j].paint(g2d);
			}
		}
		for (Sprite s : spritesOnScreen) {
			s.update();
			s.paint(g2d);
		}
	}

	/**
	 * <strong> NOT PUBLIC API</strong>
	 * This method returns the backing 2 dimensional array of tiles in the level. This method is reserved only for
	 * encoders that need access to internal information to save the object.
	 * 
	 * @return
	 * 		2d array of tiles. Changes to the array <strong> will cause issues. Do not modify</strong>
	 */
	public Tile[][] internalGetTiles() { return this.screenTiles; }


}
