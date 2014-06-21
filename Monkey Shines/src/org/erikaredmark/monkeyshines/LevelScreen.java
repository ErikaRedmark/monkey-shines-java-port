package org.erikaredmark.monkeyshines;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import org.erikaredmark.monkeyshines.background.Background;
import org.erikaredmark.monkeyshines.background.SingleColorBackground;
import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.erikaredmark.monkeyshines.tiles.CommonTile;
import org.erikaredmark.monkeyshines.tiles.TileType;

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
	private       Background background;
	private final Tile screenTiles[][]; // 20 rows, 32 cols
	// Whilst this is generally final in gameplay, it is left non-final here so it may be modified by the level editor.
	private       ImmutablePoint2D bonzoStart;
	private final List<Sprite> spritesOnScreen;
	
	// state information for the screen
	private       ImmutablePoint2D bonzoCameFrom;
	private       ImmutablePoint2D bonzoLastOnGround;

	// Graphics
	// These are all pointers to what is stored in world.
	private WorldResource rsrc;
	

	/**
	 * 
	 * Creates an empty level screen initialised to no tiles or sprites with a default background. The default
	 * background is either the first full background in the resource, the first pattern in the resource, or
	 * failing that, a solid colour of black.
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
		Background defaultBackground =   rsrc.getBackgroundCount() > 0
									   ? rsrc.getBackground(0)
									   :    rsrc.getPatternCount() > 0
									   	  ? rsrc.getPattern(0)
									   	  : new SingleColorBackground(Color.BLACK);
		
		return new LevelScreen(screenId,
							   defaultBackground,
							   Tile.createBlankTileMap(),
							   ImmutablePoint2D.of(0, 0),
							   new ArrayList<Sprite>(),
							   rsrc);
	}
	

	/**
	 * 
	 * Intended for internal static factories and decoding system only
	 * 
	 */
	public LevelScreen(final int screenId, 
			 		   final Background background,
					   final Tile[][] screenTiles, 
					   final ImmutablePoint2D bonzoStart, 
					   final List<Sprite> spritesOnScreen,
					   final WorldResource rsrc) {
		
		this.screenId = screenId;
		this.background = background;
		this.screenTiles = screenTiles;
		this.bonzoStart = bonzoStart;
		this.spritesOnScreen = spritesOnScreen;
		this.rsrc = rsrc;
	}
	
	/** Returns the screen id of this screen																			*/
	public int getId() { return this.screenId; }
	
	/**
	 * Returns the background for this screen
	 * @return
	 */
	public Background getBackground() { 
		return this.background; 
	}
	
	/**
	 * 
	 * Only intended to be called from level editor: sets the background for the current screen.
	 * 
	 * @param newBackground
	 * 		new background for this screen
	 * 
	 */
	public void setBackground(Background newBackground) {
		this.background = newBackground;
	}

	
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
	 * Same as {@code getBonzoStartingLocation} but returns values as pixel co-ordinates.
	 * 
	 * @return
	 */
	public ImmutablePoint2D getBonzoStartingLocationPixels() {
		return this.bonzoStart.multiply(GameConstants.TILE_SIZE_X, GameConstants.TILE_SIZE_Y);
	}
	
	/**
	 * 
	 * Returns the location bonzo came from when entering this level, or the startign location if bonzo started
	 * on this screen.
	 * 
	 * @return
	 * 		a new instance of a point that represents the location bonzo entered this screen. This initially is set to his
	 * 		starting location on the screen but is changed as he moves through the screens. Value never {@code null}
	 * 
	 */
	public ImmutablePoint2D getBonzoCameFrom() {
		return bonzoCameFrom;
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
	 * 
	 * Resets screen. Hazards are returned to their locations, and sprites are reset to initial positions.
	 * 
	 */
	public void resetScreen() {
		resetSprites();
		resetTiles();
	}
	
	/**
	 * Sprites move around the screen. This makes them return to where they spawn when the screen is first entered. 
	 * Typically called alongside resetBonzo when Bonzo dies.
	 */
	private void resetSprites() {
		for (Sprite nextSprite : spritesOnScreen) {
			nextSprite.resetSpritePosition();
		}
	}
	
	private void resetTiles() {
		// TODO If required, a possible optimisation may be to store tiles in separate list to iterate over.
		for (Tile col[] : this.screenTiles) {
			for (Tile t : col) {
				t.getType().reset();
			}
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
	 * If the location is not valid for a tile, then {@code CommonTile.NONE} is returned.
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
		if (   x < 0 
			|| x >= GameConstants.TILES_IN_ROW 
			|| y < 0 
			|| y >= GameConstants.TILES_IN_COL) {
			
			return CommonTile.NONE;
		}
		
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
			if (box.intersect(rect) != null) returnList.add(s);
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
	 * Resets bonzos location last on the ground to 'null' The last on ground
	 * location is only used when bonzo dies on a screen where he never had a
	 * safe landing, and has to go back one or more screens to the last place he was
	 * safe on ground.
	 * <p/>
	 * Otherwise, this variable is set whenever bonzo executes a jump from some
	 * ground, or lands from a jump on ground. If bonzo dies from a jump or fall
	 * this variable is not set to that value
	 * 
	 */
	public void resetBonzoOnGround() {
		bonzoLastOnGround = null;
	}
	
	/**
	 * 
	 * Returns the last location on this screen bonzo was on the ground, or
	 * {@code null} if he never was (which is possible for screens he just falls
	 * through). Resetting bonzo to the last place on GROUND should be done only
	 * if the screen he died on had no ground (as in, moving him to the last location
	 * he entered the screen from may kill him again).
	 * 
	 * @return
	 * 		the last location he was on the ground safely in the screen, or
	 * 		{@code null} if he never landed on ground in the screen.
	 * 
	 */
	public ImmutablePoint2D getBonzoLastOnGround() {
		return bonzoLastOnGround;
	}
	
	/**
	 * 
	 * Called during bonzo collison algorithms. Allows the screen
	 * to store his last safe ground landing.
	 * TODO there is a minute change that this location may be set to a place a sprite will
	 * be if the screen is restarted. For now, keep it as this algorithm unless it is determined
	 * to impede level design and become a major issue.
	 * 
	 * @param ground
	 * 		safe ground location for a respawn
	 * 
	 */
	void setBonzoLastOnGround(ImmutablePoint2D ground) {
		bonzoLastOnGround = ground;
	}
	
	/**
	 * 
	 * Draw background, tiles, and sprites in one swoop.
	 * <p/>
	 * As the name suggests, this doesn't merely paint; going through every entity, it run updates on
	 * them as well. Updating is done after painting
	 * 
	 * @param g2d
	 * 
	 */
	public void paintAndUpdate(Graphics2D g2d) {
		background.draw(g2d);
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
	 * 
	 * Paints the level screen to the graphics context with no sprites. This
	 * is intended as the first step for making a thumbnail of a level screen.
	 * This does not update the game at all.
	 * 
	 */
	public void paintForThumbnail(Graphics2D g2d) {
		background.draw(g2d);
		for (int i = 0; i < GameConstants.TILES_IN_COL; i++) { // for every tile in the row
			for (int j = 0; j < GameConstants.TILES_IN_ROW; j++) {
				if (screenTiles[i][j] != null) {
					screenTiles[i][j].paint(g2d);
				}
			}
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
