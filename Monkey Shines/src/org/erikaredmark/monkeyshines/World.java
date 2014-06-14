package org.erikaredmark.monkeyshines;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.erikaredmark.monkeyshines.Conveyer.Rotation;
import org.erikaredmark.monkeyshines.bounds.Boundable;
import org.erikaredmark.monkeyshines.bounds.IPoint2D;
import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.erikaredmark.monkeyshines.tiles.HazardTile;
import org.erikaredmark.monkeyshines.tiles.TileType;

/**
 * Holds all information about the entire world.
 * Loads up all sprite sheets, sends references of them to the screens who send references to the tiles.
 * stores all the data for the screens, allowing the screens to draw themselves easily.
 * 
 * Every World is composed of the following parts, which are stored in memory
 * 
 * An Array of full-screen backgrounds. Please keep the number of these to a minimum and use the
 * PPAT patterns implementation to save on memory.
 * 
 * A Map consisting of all the goodies in the world. The theoritical limit is infinite. Unlike in
 * the original, the limit of the number of goodies is governed by the memory used.
 * 
 * The Map of the world screens. The reason this is a hashmap is simple. In the original game, level
 * ID's, as seen by the editor, were given an integer value. Moving left or right incremented or decremented
 * by one, moving up or down incremented/decremented by 100. This means that if some worlds have levels that
 * are too horizontal, there can be a potential for collision. The new implementation is based on the same
 * principle for speed, and just increases the values so the collision will happen at a point that the world
 * size is unlikely to get to; already it is huge.
 * 
 * The integer value of the current screen.
 * 
 */
public class World {
	private final String worldName;
	
	// Goodies. Store as a hashmap. Why? For speed in collision detection.
	// The screen string, concatenated with "X", and then the the tile co-ordinates. . .
	// "1000X4,3"
	// That way, Bonzo just checks four places by making a string and checking the hash. Although strings are slower, this means
	// that no matter how many objects, it will take the same amount of time to detect collisions; won't need to loop and see if anything
	// is at that point
	private final Map<String, Goodie> goodiesInWorld;
	
	// Screens: Hashmap. That way, when moving across screens, take the levelid and add/subtract a value, check it in hash,
	// and quickly get the screen we need. It is fast and I believe the designers of the original did the same thing.
	private final Map<Integer, LevelScreen> worldScreens;
	
	// Each hazard tile references the hazard it needs, but the hazards themselves are part of the world.
	// Typically, a world includes hazard ids for dynamite, bombs, lightbulbs, and sometimes lava, although
	// others may be added by the level editor, along with custom graphics in the graphics pack.
	private final List<Hazard> hazards;
	
	// Similiar to, but less complicated than, hazards, each conveyer tile is represented by
	// a single conveyer immutable state. In this case, this is which kind of conveyer belt it
	// is and which direction it is moving.
	// Populated automatically conveyers up to the greater of the two:
	//   number of conveyer belt types in the original world file
	//   number of conveyer belt types in the resource when being skinned.
	// Changing the resource to have less conveyers whilst conveyer belt tiles are on the world referring
	// to them is probably a bad idea.
	private final List<Conveyer> conveyers;
	
	private       int currentScreen;
	
	
	private final WorldResource rsrc;
	
	/**
	 * 
	 * generates 'amt' conveyer SETS. Each set is two conveyers; one clockwise and one anti-clockwise. Newly generated conveyers
	 * are added to the end of the list.
	 * <p/>
	 * Because the list is mutated, it must NOT be an immutable type.
	 * 
	 */
	public static void generateConveyers(List<Conveyer> conveyers, int amt) {
		// Two entries in the list per conveyer id. Rotation determines which one.
		int startId = conveyers.size() / 2;
		for (int i = 0; i < amt; i++) {
			conveyers.add(new Conveyer(startId + i, Rotation.CLOCKWISE) );
			conveyers.add(new Conveyer(startId + i, Rotation.ANTI_CLOCKWISE) );
		}
	}
	
	/**
	 * 
	 * Creates a new world skinned with the current resource. The new world starts with one screen (1000) which
	 * contains no tiles, no goodies, sprites, or anything, background id 0, and bonzos starting position is in the upper right corner.
	 * 
	 * @param name
	 * 		the name of the world
	 * 
	 * @return
	 * 		a new world
	 * 
	 */
	public static World newWorld(final String name, final WorldResource rsrc) {
		
		LevelScreen initialScreen = LevelScreen.newScreen(1000, rsrc);
		
		Map<Integer, LevelScreen> screens = new HashMap<>();
		screens.put(1000, initialScreen);
		
		// Generate # of conveyers proportional to graphics context size
		List<Conveyer> conveyers = new ArrayList<>(rsrc.getConveyerCount() * 2);
		generateConveyers(conveyers, rsrc.getConveyerCount() );
		
		return new World(name, new HashMap<String, Goodie>(), screens, new ArrayList<Hazard>(), conveyers, rsrc);
	}
	
	
	/**
	 * 
	 * Returns the current resource skinning this worlds graphics, sounds, and music.
	 * 
	 * @return
	 * 		current resource
	 * 
	 */
	public WorldResource getResource() { return this.rsrc; }
	
	/**
	 * 
	 * Explicitly sets the modifiable data values of the world. Does not set the graphics resources, which must be done through a call to
	 * {@code skin}
	 * <p/>
	 * This is intended for static factories and the decoding system for .world files ONLY. No defensive copies of parameters are made,
	 * so it is the responsibility of the client to ensure there is no data sharing.
	 * 
	 */
	public World(final String worldName, 
				  final Map<String, Goodie> goodiesInWorld, 
				  final Map<Integer, LevelScreen> worldScreens,
				  final List<Hazard> hazards,
				  final List<Conveyer> conveyers,
				  final WorldResource rsrc) {
		
		/* Variable data		*/
		this.worldName = worldName;
		this.goodiesInWorld = goodiesInWorld;
		this.worldScreens = worldScreens;
		this.hazards = hazards;
		this.conveyers = conveyers;
		
		/* Constant data		*/
		this.currentScreen = 1000;
		this.rsrc = rsrc;
	}
	
	/**
	 * Returns a LevelScreen in this level pointed to by ID if that screen exists. 
	 * @param id the id number of the screen to retrive. Remember that the first screen in the map will be
	 * ID 1000. The id must resolve to a proper screen or an exception will be thrown. Use {@code #screenIdExists(int)} if unsure
	 * @return
	 */
	public LevelScreen getScreenByID(final int id) {
		LevelScreen s = worldScreens.get(id);
		if (s == null) throw new IllegalArgumentException("Id " + id + " refers to an invalid screen");
		return s;
	}
	
	/**
	 * Determines if a given screen id exists in this world. Attempts to get level screens for invalid ids will result in exceptions.
	 * 
	 * @param id
	 * 		the id of the screen
	 * 
	 * @return
	 * 		{@code true} if the screen exists, {@code false} if otherwise
	 */
	public boolean screenIdExists(final int id) {
		return (worldScreens.get(id) != null);
	}
	
	/**
	 * Take the currentScreen integer and uses it to resolve the actual LevelScreen object
	 * @return a LevelScreen object identified by the integer ID currentScreen. 
	 */
	public LevelScreen getCurrentScreen() {
		return getScreenByID(currentScreen);
	}
	
	/**
	 * Gets the currentScreen ID number. Does not return the actual LevelScreen object.
	 * @return The ID number of the current screen.
	 */
	public int getCurrentScreenId() {
		return currentScreen;
	}
	
	/**
	 * Looks at the loaded LevelScreen and determines where Bonzo is supposed to restart from. This restart
	 * value is either loaded from the LevelScreen constant (if Bonzo started here) or from the place he
	 * entered the screen from.
	 * @param theBonzo
	 */
	public void restartBonzo(Bonzo theBonzo) {
		theBonzo.restartBonzoOnScreen(getCurrentScreen() );
		getCurrentScreen().resetScreen();
	}
	
	/**
	 * Get the world name 
	 */
	public String getWorldName() {
		return this.worldName;
	}
	
	/** 
	 * Adds a level screen to the world. Unless this is done via an editor, changes will not be persisted when the
	 * world is reloaded.
	 *  
	 * @param screen
	 * 		the new screen to add
	 */
	public void addScreen(final LevelScreen screen) {
		this.worldScreens.put(screen.getId(), screen);
	}
	
	
	/**
	 * Sets the current screen for the world. This should be used sparingly (instead relying on screenChange for most cases)
	 * and should always be followed up with a change to Bonzos location (unless called from the level editor)
	 * <p/>
	 * This method will perform no action and return false if the screen doesn't exist (to prevent crashes in game, but this
	 * generally shouldn't happen on a well designed world). Otherwise, changes the screen and returns true
	 * 
	 * @param screenId
	 * 		the id of the screen to change to
	 * 
	 * @return
	 * 		{@code true} if changing the screen was successful, {@code false} if the screen does not exist and thus
	 * 		could not be switched
	 * 
	 */
	public boolean changeCurrentScreen(int screenId) {
		if (screenIdExists(screenId) == false) return false;
		else {
			getCurrentScreen().resetScreen();
			this.currentScreen = screenId;
			return true;
		}
	}
	
	public void checkCollisions(Bonzo theBonzo) {
		// Another Screen?
		ImmutablePoint2D currentLocation = theBonzo.getCurrentLocation();
		ScreenDirection dir = ScreenDirection.fromLocation(currentLocation, Bonzo.BONZO_SIZE);
		if (dir != ScreenDirection.CURRENT) {
			int newId = dir.getNextScreenId(this.currentScreen);
			changeCurrentScreen(newId);
			// Update bonzos location to the new screen location
			theBonzo.changeScreen(newId);
			dir.transferLocation(theBonzo.getMutableCurrentLocation(), Bonzo.BONZO_SIZE);
			// Update the new screen with data about where we came from so deaths bring us to the same place
			getCurrentScreen().setBonzoCameFrom(theBonzo.getCurrentLocation() );
			// Ignore any other collisions for now.
			return;
		}
		// A Sprite?
		List<Sprite> allSprites = getCurrentScreen().getSpritesOnScreen();
		ImmutableRectangle bonzoBounding = theBonzo.getCurrentBounds();
		for (Sprite nextSprite : allSprites) {
			Boundable intersection = nextSprite.getCurrentBounds().intersect(bonzoBounding);
			if (intersection != null) {
				// Bounding box check done. Do more expensive pixel check
				if (nextSprite.pixelCollision(theBonzo, intersection) ) {
					nextSprite.getType().onBonzoCollision(theBonzo, this);
				}
			}
		}
		// A hazard?
		hazardCollisionCheck(theBonzo);
		
		
		// A goodie?
		
		int topLeftX = (currentLocation.x() + (GameConstants.GOODIE_SIZE_X / 2) ) / GameConstants.GOODIE_SIZE_X;
		int topLeftY = (currentLocation.y() + (GameConstants.GOODIE_SIZE_Y / 2) )/ GameConstants.GOODIE_SIZE_Y;
		
		String topLeftQuad = "" + (currentScreen) + "X" + topLeftX + "," + topLeftY;
		String topRightQuad = "" + (currentScreen) + "X" + (topLeftX + 1) + "," + topLeftY;
		String bottomLeftQuad = "" + (currentScreen) + "X" + topLeftX + "," + (topLeftY + 1);
		String bottomRightQuad = "" + (currentScreen) + "X" + (topLeftX + 1) + "," + (topLeftY + 1);
		
		Goodie gotGoodie;
		if ( (gotGoodie = goodiesInWorld.get(topLeftQuad) ) != null ) {
			gotGoodie.take(theBonzo);
		}
		if ( (gotGoodie = goodiesInWorld.get(topRightQuad) ) != null ) {
			gotGoodie.take(theBonzo);
		}
		if ( (gotGoodie = goodiesInWorld.get(bottomLeftQuad) ) != null ) {
			gotGoodie.take(theBonzo);
		}
		if ( (gotGoodie = goodiesInWorld.get(bottomRightQuad) ) != null ) {
			gotGoodie.take(theBonzo);
		}
	}
	
	/**
	 * 
	 * Performs a check if the bonzo is on one or more 'hazard' tiles. If so, then the hazard it set
	 * to explode (if required) and bonzo is killed based on the hazard properties.
	 * 
	 * @param bonzo
	 * 
	 */
	private void hazardCollisionCheck(Bonzo bonzo) {
		ImmutablePoint2D[] tilesToCheck = effectiveTilesCollision(bonzo.getCurrentBounds() );
		for (ImmutablePoint2D tile: tilesToCheck) {
			TileType type = getCurrentScreen().getTile(tile.x(), tile.y() );
			if (type instanceof HazardTile) {
				// Still can get out of doing anything if the hazard is already gone.
				HazardTile hazard = (HazardTile) type;
				// MUST check isExploding. If bonzo had invincibility and lost it 1 tick after touching
				// a bomb, the bomb is technically already no longer a hurt for Bonzo.
				if (hazard.isDead() || hazard.isExploding() )  continue;
				
				hazard.hazardHit();
				// Send a kill message to bonzo. Only invincibility will save him
				bonzo.tryKill(hazard.getHazard().getDeathAnimation() );
				return;
			}
		}
	}
	
	/**
	 * 
	 * Resolves a bounding box into its 'effective' four tiles it takes up. The bounding box MUST be 40x40; any
	 * other size will have unexpected behaviour (and fire an assertion error is assertions are enabled.
	 * <p/>
	 * A bounding box may cover more than four tiles. However, the four chosen will be the four 'most' covered by
	 * the box. This is a 'good enough' representation of where Bonzo is, and is typically used for things like
	 * hazards.
	 * <p/>
	 * Generally, the way the system works (moving between screens) this method should rarely end up enumerating
	 * a tile grid location that is outside of the number of actual tiles on the screen. However, fast speeds
	 * downwards MAY cause this to happen; it is important for clients to handle the case where any of the returned
	 * points may be out of range.
	 * 
	 * @param bounds
	 * 		the bounding rectangle. MUST be 40x40
	 * 
	 * @return
	 * 		array of size 4, from top-left clockwise, each 'point' that represents an x,y in the tile gride of
	 * 		the file this bounding box occupies
	 * 
	 * 
	 * @throws AssertionError
	 * 		if assertions are enabled and the bounds are not 40x40
	 * 
	 */
	static ImmutablePoint2D[] effectiveTilesCollision(ImmutableRectangle bounds) {
		assert bounds.getSize().x() == 40;
		assert bounds.getSize().y() == 40;
		// Solution: 
		// 1) 'snap' top left x,y cordinates. Whether the x/y stays in the grid tile, or moves right/
		//	  down depends on how close the position would be to the other.
		// 2) Divide to get the tile x,y, then build the other three points in clockwise form
		//
		//  Stays in tile...
		//  *-------*
		//	| X-    |
		//  | |     |
		//  |       |
		//  *-------*
		// 
		//  Snaps to right and bottom tile
		//  *-------*
		//	|       |
		//  |       |
		//  |     X-|
		//  *-----|-*
		
		IPoint2D topLeft = bounds.getLocation();
		int offsetInTileX = topLeft.x() % GameConstants.TILE_SIZE_X;
		int offsetInTileY = topLeft.y() % GameConstants.TILE_SIZE_Y;
		
		// The use of > means that tile snapping favours staying within a tile 
		// [0, TILE_SIZE_X_HALF] vs snapping (TILE_SIZE_X_HALF, TILE_SIZE_X]
		// Division transforms absolute point to grid point
		int newTopLeftX = (  offsetInTileX > GameConstants.TILE_SIZE_X_HALF
						   ? (topLeft.x() / GameConstants.TILE_SIZE_X) + 1
						   : topLeft.x() / GameConstants.TILE_SIZE_X); 
		
		int newTopLeftY = (  offsetInTileY > GameConstants.TILE_SIZE_Y_HALF
						   ? (topLeft.y() / GameConstants.TILE_SIZE_Y) + 1
						   : topLeft.y() / GameConstants.TILE_SIZE_Y);
	
		ImmutablePoint2D[] fourPoints = new ImmutablePoint2D[4];
		fourPoints[0] = ImmutablePoint2D.of(newTopLeftX, newTopLeftY);
		fourPoints[1] = ImmutablePoint2D.of(newTopLeftX + 1, newTopLeftY);
		fourPoints[2] = ImmutablePoint2D.of(newTopLeftX, newTopLeftY + 1);
		fourPoints[3] = ImmutablePoint2D.of(newTopLeftX + 1, newTopLeftY + 1);
		return fourPoints;
	}
	
	
	// Reminder: Form is like "1000X4,3"
	/**
	 * 
	 * Adds a goodie to the given world, typically only used by level editor.
	 * <p/>
	 * This world must already be skinned
	 * 
	 * @param x
	 * @param y
	 * @param screenId
	 * @param type
	 * 
	 * @throws IllegalStateException
	 * 		if the world has not yet been skinned
	 * 
	 */
	public void addGoodie(final int x, final int y, final int screenId, final Goodie.Type type) {
		String checker = collisionCheckerForGoodie(x, y, screenId);
		// If goodie already exists, take out and replace
		if (goodiesInWorld.get(checker) != null)
			goodiesInWorld.remove(checker);
		goodiesInWorld.put(checker, Goodie.newGoodie(type, ImmutablePoint2D.of(x, y), screenId, rsrc) );
	}
	
	/**
	 * Generates a collision checker String for the Map of String -> Goodie. Used internally to create map and externally
	 * to create collision strings to check against.
	 * 
	 * @param x
	 * 		row of goodie
	 * 
	 * @param y
	 * 		column of goodie
	 * 
	 * @param screenId
	 * 		id of the screen goodie will appear on
	 * 
	 * @return
	 * 		string that can be used as a key to find the relevant goodie in the map
	 */
	public static String collisionCheckerForGoodie(final int x, final int y, final int screenId) { return "" + screenId + "X" + x + "," + y; }
	
	public void removeGoodie(final int x, final int y, final int screenId) {
		String checker = "" + screenId + "X" + x + "," + y;
		if (goodiesInWorld.get(checker) != null)
			goodiesInWorld.remove(checker);
	}

	public void paintAndUpdate(Graphics2D g2d) {
		getCurrentScreen().paintAndUpdate(g2d);
		
		Collection<Goodie> goodiesVector = (Collection<Goodie>)goodiesInWorld.values();
		for (Goodie nextGoodie : goodiesVector) {
			if (nextGoodie.getScreenID() == currentScreen) {
				nextGoodie.update();
				nextGoodie.paint(g2d);
			}
		}
	}

	/**
	 * Returns an unmodifiable view of the current state of the goodies in this world. Clients can not modify the goodies
	 * in this world through the returned map.
	 * 
	 * @return
	 * 		immutable copy of the map representing the goodies in this world
	 */
	public Map<String, Goodie> getGoodies() { return Collections.unmodifiableMap(this.goodiesInWorld); }

	/**
	 * Returns an immutable copy of all the levels in the world
	 * 
	 * @return
	 */
	public Map<Integer, LevelScreen> getLevelScreens() { return Collections.unmodifiableMap(this.worldScreens); }
	
	/**
	 * Returns an unmodifiable version of the list of hazards in this world. This is not their locations; that is in tile data
	 * as a {@code HazardTile}. This describes the 'types' of hazards (bombs, lava) in a world.
	 * 
	 * @return
	 */
	public List<Hazard> getHazards() {
		return Collections.unmodifiableList(this.hazards);
	}
	
	/**
	 * Returns an unmodifiable version of the list of conveyers in the world
	 * 
	 * @return
	 */
	public List<Conveyer> getConveyers() {
		return Collections.unmodifiableList(this.conveyers);
	}

	/**
	 * 
	 * <strong> intended only for use by level editor</strong>
	 * <p/>
	 * Sets the given hazards available for the world. This modifies the internal list, and does not store a reference to
	 * the passed one.
	 * 
	 * @param hazards
	 * 		new list of hazards
	 * 
	 */
	public void setHazards(List<Hazard> newHazards) {
		this.hazards.clear();
		this.hazards.addAll(newHazards);
	}
	
}