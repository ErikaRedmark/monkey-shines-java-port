package edu.nova.erikaredmark.monkeyshines;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import edu.nova.erikaredmark.monkeyshines.encoder.EncodedLevelScreen;
import edu.nova.erikaredmark.monkeyshines.encoder.EncodedSprite;
import edu.nova.erikaredmark.monkeyshines.encoder.EncodedTile;
import edu.nova.erikaredmark.monkeyshines.resource.WorldResource;
import edu.nova.erikaredmark.monkeyshines.tiles.StatelessTileType;
import edu.nova.erikaredmark.monkeyshines.tiles.TileType;

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
public final class LevelScreen {
	
	// Initialisation datas for a level screen
	private final int screenId;
	private final int backgroundId;
	private final Tile screenTiles[][]; // 20 rows, 32 cols
	// Whilst this is generally final in gameplay, it is left non-final here so it may be modified by the level editor.
	private       ImmutablePoint2D bonzoStart;
	private final List<Sprite> spritesOnScreen;
	
	// state information for the screen
	private       ImmutablePoint2D bonzoCameFrom;

	// Graphics
	// These are all pointers to what is stored in world.
	private WorldResource rsrc;
	private boolean isSkinned = false;
	
	// Background to get

	
	/**
	 * 
	 * Creates an instance of this object from its encoded for.
	 * 
	 * @param screen
	 * 		the level screen to inflate
	 * 
	 * @param worldHazards
	 * 		list of hazards that are part of the world. Some levels have hazards on them so these must
	 * 		be inflated first. This list will not be modified
	 * 
	 * @param conveyers
	 * 		list of conveyers that are part of the world. As with hazards, some levels have conveyer belts
	 * 		that must reference a valid 'Conveyer' object for their immutable state.
	 * 
	 * @return
	 * 		new instance of this object
	 * 
	 */
	public static LevelScreen inflateFrom(EncodedLevelScreen screen, List<Hazard> worldHazards, List<Conveyer> conveyers) {
		final int screenId = screen.getId();
		final int backgroundId = screen.getBackgroundId();
		final ImmutablePoint2D bonzoStart = screen.getBonzoLocation();
		
		final Tile[][] screenTiles = new Tile[20][32];
		final EncodedTile[][] encodedTiles = screen.getTiles();
		for (int i = 0; i < screenTiles.length; i++) {
			for (int j = 0; j < screenTiles[i].length; j++) {
				screenTiles[i][j] = Tile.inflateFrom(encodedTiles[i][j], worldHazards, conveyers);
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
	 * <p/>
	 * This location is in <strong>Tile Coordinates</strong>, not pixel coordinates!
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
	 * <p/>
	 * The returned point is in pixel coordinates
	 * 
	 * @return
	 * 		a new instance of a point that represents the location bonzo entered this screen. This initially is set to his
	 * 		starting location on the screen but is changed as he moves through the screens. Value never {@code null}
	 * 
	 */
	public Point2D newPointFromWhereBonzoCame() {
		return Point2D.from(bonzoCameFrom);
	}
	
	/**
	 * Called when Bonzo enters the screen from another Screen. Sets the location he came from so if he dies on this screen, 
	 * he can return to that position. 
	 * 
	 * @param bonzoCameFrom 
	 * 		the location Bonzo entered the screen from
	 */
	public void setBonzoCameFrom(final ImmutablePoint2D bonzoCameFrom) {
		this.bonzoCameFrom = bonzoCameFrom;
	}
	
	/**
	 * If, for some reason, the location Bonzo Came from becomes invalid, this resets it.
	 */
	public void resetBonzoCameFrom() {
		this.bonzoCameFrom = bonzoStart;
	}
	
	/**
	 * Sprites move around the screen. This makes them return to where they spawn when the screen is first entered. 
	 * Typically called alongside resetBonzo when Bonzo dies.
	 */
	public void resetSprites() {
		for (Sprite nextSprite : spritesOnScreen) {
			nextSprite.resetSpritePosition();
		}
	}
	
	
	/**
	 * Returns the tile type at the given cordinates. These are PIXEL cordinates in the screen
	 * 
	 * @param x
	 * 		x location in terms of pixels
	 * 
	 * @param y
	 * 		y location in terms of pixels
	 *
	 * @return
	 * 		a tile type at the given position
	 */
	public TileType getTileAt(int x, int y) {
		return getTile(x / GameConstants.TILE_SIZE_X, y / GameConstants.TILE_SIZE_Y);
	}
	
	/**
	 * 
	 * Returns the tile type for the tile at the given GRID location; as in, the values passed to
	 * this method should already be normalised as a grid location and not an absolute location
	 * on the screen
	 * <p/>
	 * If the location is not valid for a tile, then {@code StatelessTileType.NONE} is returned.
	 * 
	 * @param x
	 * 		x location in terms of grid index
	 * 
	 * @param y
	 * 		y location in terms of grid index
	 * 
	 * @return
	 * 		a tile type at the given position, or {@code StatelessTileType.NONE} if the given x/y
	 * 		grid location is out of range
	 * 
	 */
	public TileType getTile(int x, int y) {
		// If out of bounds, allow to slip by
		if (x < 0 || x >= GameConstants.TILES_IN_ROW || y < 0 || y >= GameConstants.TILES_IN_COL)
			return StatelessTileType.NONE;
		
		return screenTiles[y][x].getType();
	}
	
	// Careful! This is return by reference
	public List<Sprite> getSpritesOnScreen() {
		return spritesOnScreen;
	}
	

	/**
	 * Adds a sprite to the screen. Typically reserved for level editor.
	 * 
	 * @param sprite
	 * 		the sprite to add. The sprite MUST have been skinned with a valid graphics resource first
	 */
	public void addSprite(Sprite sprite) {
		this.spritesOnScreen.add(sprite);
	}
	
	/**
	 * 
	 * Removes the given sprite off the screen. Typically reserved for level editor.
	 * 
	 * @param sprite
	 * 		the sprite to remove
	 * 
	 */
	public void remove(Sprite sprite) {
		this.spritesOnScreen.remove(sprite);
	}
	
	/**
	 * Returns a list of sprites within the given area. This is typically designed for the level editor.
	 * <p/>
	 * A rectangle, whose centre is 'point' and whose distance from the centre to a side is 'size', is drawn as a collision
	 * rectangle to find sprites in the area. Hence, the 'size' field is half the length and width of the final rectangle. Any
	 * sprites in which 'ANY' part of their 40x40 graphic touches within this rectangle will be returned.
	 * 
	 * @param point
	 * 		centre of rectangle
	 * 
	 * @param size
	 * 		distance from centre to a side of the rectangle
	 * 
	 * @return
	 * 		a list of sprites in the area. This may return an empty list if there is none, but never {@code null}
	 */
	public List<Sprite> getSpritesWithin(ImmutablePoint2D point, int size) {
		// Convert centre point into upper left point.
		final ImmutableRectangle box = ImmutableRectangle.of(point.x() - (size / 2), point.y() - (size / 2), size, size);
		final List<Sprite> returnList = new ArrayList<>();
		for (Sprite s : spritesOnScreen) {
			final ImmutableRectangle rect = s.getCurrentBounds();
			if (box.intersect(rect) ) returnList.add(s);
		}
		return returnList;
	}
	
	/*
	 * Editor functions. None of these effects are saved until the level is saved.
	 * Save often!
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
	 * 
	 * Sets bonzos starting position on this screen to be somewhere else. Should only be called by level editor.
	 * 
	 * @param point
	 * 		bonzos new starting location
	 * 
	 */
	public void setBonzoStartingLocation(ImmutablePoint2D point) {
		this.bonzoStart = point;
	}
	
	/**
	 * 
	 * Draw background, tiles, and sprites in one swoop.
	 * <p/>
	 * As the name suggests, this doesn't merely paint; going through every entity, it run updates on
	 * them as well. Updating is done after painting
	 * 
	 * TODO draws entire screen. May require a more intelligent algorithm to run on slower
	 * machines
	 * 
	 * @param g2d
	 * 
	 */
	public void paintAndUpdate(Graphics2D g2d) {
		g2d.drawImage(rsrc.getBackground(this.backgroundId), 0, 0, null);
		for (int i = 0; i < GameConstants.TILES_IN_COL; i++) { // for every tile in the row
			for (int j = 0; j < GameConstants.TILES_IN_ROW; j++) {
				if (screenTiles[i][j] != null) {
					screenTiles[i][j].paint(g2d);
					screenTiles[i][j].update();
				}
			}
		}
		for (Sprite s : spritesOnScreen) {
			s.paint(g2d);
			s.update();
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
