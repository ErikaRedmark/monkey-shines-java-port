package org.erikaredmark.monkeyshines;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.erikaredmark.monkeyshines.World.GoodieLocationPair;
import org.erikaredmark.monkeyshines.background.Background;
import org.erikaredmark.monkeyshines.background.SingleColorBackground;
import org.erikaredmark.monkeyshines.resource.WorldResource;

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
	// Id IS repeated in the Map holding the screens. Kept here for ease of use with other algorithms
	// that work only with screens and need an id.
	private final int screenId;
	private       Background background;
	
	private final TileMap map;
	// Whilst this is generally final in gameplay, it is left non-final here so it may be modified by the level editor.
	private       ImmutablePoint2D bonzoStart;
	private final List<Sprite> spritesOnScreen;
	
	// state information for the screen. Bonzo can respawn where he came from at the velocity that
	// he came into the screen
	private       ImmutableVector bonzoCameFrom;
	private       ImmutablePoint2D bonzoLastOnGround;

	
	// defaults to true on constructions; intended to allow level editor to stop sprite animations for easier
	// selection and editing.
	private boolean animateSprites;
	
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
	 * 		hash map. Id may be negative is so choosing
	 * 
	 * @param rsrc
	 * 		a graphics resource to skin this level
	 * 
	 * @param world
	 * 		a reference to the world that contains this level screen. Required for some world-level properties
	 * 		that affect all screens
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
							   new TileMap(GameConstants.LEVEL_ROWS, GameConstants.LEVEL_COLS),
							   ImmutablePoint2D.of(0, 0),
							   new ArrayList<Sprite>(),
							   rsrc);
	}
	

	/**
	 * 
	 * Intended for internal static factories and decoding system only
	 * BonzoStart is a resolved-to-tile-cordinate location, not pixel location
	 * 
	 */
	public LevelScreen(final int screenId, 
			 		   final Background background,
					   final TileMap map, 
					   final ImmutablePoint2D bonzoStart, 
					   final List<Sprite> spritesOnScreen,
					   final WorldResource rsrc) {
		
		this.screenId = screenId;
		this.background = background;
		this.map = map;
		this.bonzoStart = bonzoStart;
		this.spritesOnScreen = spritesOnScreen;
		this.rsrc = rsrc;
		this.animateSprites = true;
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
	 * Returns the location bonzo came from when entering this level, or the starting location if bonzo started
	 * on this screen.
	 * 
	 * @return
	 * 		a new instance of a point that represents the location bonzo entered this screen. This initially is set to his
	 * 		starting location on the screen but is changed as he moves through the screens. Value never {@code null}
	 * 
	 */
	public ImmutableVector getBonzoCameFrom() {
		return bonzoCameFrom;
	}
	
	/**
	 * Called when Bonzo enters the screen from another Screen. Sets the location he came from so if he dies on this screen, 
	 * he can return to that position at the given velocity.
	 * 
	 * @param bonzoCameFrom 
	 * 		the location Bonzo entered the screen from
	 */
	public void setBonzoCameFrom(final ImmutableVector bonzoCameFrom) {
		this.bonzoCameFrom = bonzoCameFrom;
	}
	
	/**
	 * If, for some reason, the location Bonzo Came from becomes invalid, this resets it.
	 */
	public void resetBonzoCameFrom() {
		this.bonzoCameFrom = ImmutableVector.fromPoint(bonzoStart, 0, 0);
	}
	

	/**
	 * 
	 * Resets screen. Hazards are returned to their locations, and sprites are reset to initial positions.
	 * 
	 */
	public void resetScreen() {
		resetSprites();
		map.resetTiles();
	}
	
	/**
	 * Sprites move around the screen. This makes them return to where they spawn when the screen is first entered. 
	 * Typically called alongside resetBonzo when Bonzo dies.
	 */
	private void resetSprites() {
		for (Sprite nextSprite : spritesOnScreen) {
			nextSprite.resetSprite();
		}
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
	public void removeSprite(Sprite sprite) {
		this.spritesOnScreen.remove(sprite);
	}
	
	/**
	 * 
	 * Replaces the sprite given by the first argument in the list with the one in the second argument.
	 * The order of the list is preserved.
	 * <p/>
	 * If {@code sprite} is not found in the sprite list, this method throws an exception.
	 * 
	 * @param sprite
	 * 		old sprite
	 * 
	 * @param newSprite
	 * 		new sprite to replace it with
	 * 
	 * @throws IllegalArgumentException
	 * 		if the first sprite argument is not found in the list
	 * 
	 */
	public void replaceSprite(Sprite sprite, Sprite newSprite) {
		for (ListIterator<Sprite> it = this.spritesOnScreen.listIterator(); it.hasNext(); /* no op */) {
			Sprite next = it.next();
			if (sprite.equals(next) ) {
				it.remove();
				it.add(newSprite);
				return;
			}
		}
		// Natural termination of loop means not found. not found is not legal.
		throw new IllegalArgumentException("Sprite not found in level screen to replace: " + sprite);
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
	 * TODO there is a minute chance that this location may be set to a place a sprite will
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
	 * 
	 * @param g2d
	 * 
	 */
	public void paint(Graphics2D g2d) {
		background.draw(g2d);
		map.paint(g2d, rsrc);
		for (Sprite s : spritesOnScreen) {
			s.paint(g2d);
		}
	}
	
	/**
	 * 
	 * Runs one tick of time for the given game screen.
	 * 
	 */
	public void update() {
		map.update();
		
		if (animateSprites) {
			for (Sprite s : spritesOnScreen) {
				s.update();
			}
		}
	}
	
	/**
	 * 
	 * Paints the level screen to the graphics context with no sprites. This
	 * is intended as the first step for making a thumbnail of a level screen.
	 * This does not update the game at all.
	 * <p/>
	 * The tilemap is drawn over the background
	 * 
	 */
	public void paintForThumbnail(Graphics2D g2d) {
		background.draw(g2d);
		map.paint(g2d, rsrc);
	}

	/**
	 * 
	 * Returns the underlying tile map backing this level. Changes to the array will affect tiles in the world, so
	 * this is intended only for either editors, internal methods, or viewing the map.
	 * 
	 */
	public TileMap getMap() { return this.map; }
	
	
	/** 
	 * 
	 * Intended for level editor only; turns on and off sprite animations. If sprite animations are turned off,
	 * the sprites do not animate or move around the screen in any way.
	 * 
	 * @param animation
	 * 		{@code true} to start animating, {@code false} to stop animating
	 * 
	 */
	public void setSpriteAnimation(boolean animation) {
		this.animateSprites = animation;
	}
	
	/**
	 * 
	 * @return
	 * 		{@code true} if sprites are currently animating, {@code false} if otherwise
	 * 
	 */
	public boolean getSpriteAnimation() {
		return this.animateSprites;
	}


	/**
	 * 
	 * Provides a deep-copy of all the elements of this screen, with the new id and a world reference
	 * so that the goodies that appear on this screen can have copies made for the next screen. This method
	 * ALSO has the side-effect of adding the new level to the given world (a requirement in order for the
	 * goodie information to transfer properly), so calling this method is good enough to actually add the
	 * new screen to the world. Returns an instance of the created screen
	 * <p/>
	 * WARNING: If the target screen, {@code newId}, already exists in the world it WILL be overwritten.
	 * 
	 * @param levelScreen
	 * 		the level screen to copy
	 * 
	 * @param newId
	 * 		the new id the copy will take on
	 * 
	 * @param world
	 * 		a reference to the world so the Goodies entries can be updated
	 * 
	 * @return
	 * 		a new instance of this level, identical in design to the target level but existing in a different
	 * 		location in the world.
	 * 
	 */
	public static LevelScreen copyAndAddToWorld(LevelScreen levelScreen, int newId, World world) {
		// Handle Tiles
		TileMap newTiles = levelScreen.getMap().copy();

 		// Handle Sprites
 		List<Sprite> originalSprites = levelScreen.getSpritesOnScreen();
 		List<Sprite> newSprites = new ArrayList<>(originalSprites.size() );
 		
 		for (Sprite s : originalSprites) {
 			newSprites.add(Sprite.copyOf(s) );
 		}
		
		LevelScreen newScreen = 
			new LevelScreen(newId, 
					    	levelScreen.background,
					    	newTiles,
					    	levelScreen.getBonzoStartingLocation(),
					    	newSprites,
					    	levelScreen.rsrc);

		world.addOrReplaceScreen(newScreen);
 		
 		// Handle goodies
 		Collection<GoodieLocationPair> originalGoodiePairs = world.getGoodiesForLevel(levelScreen.getId() );
 		for (GoodieLocationPair pair : originalGoodiePairs) {
 			WorldCoordinate loc = pair.location;
 			world.addGoodie(newId, loc.getRow(), loc.getCol(), pair.goodie.getGoodieType() );
 		}
 		
 		return newScreen;
	}


}
