package edu.nova.erikaredmark.monkeyshines;

import java.awt.Graphics2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableMap;

import edu.nova.erikaredmark.monkeyshines.encoder.EncodedGoodie;
import edu.nova.erikaredmark.monkeyshines.encoder.EncodedLevelScreen;
import edu.nova.erikaredmark.monkeyshines.encoder.EncodedWorld;
import edu.nova.erikaredmark.monkeyshines.graphics.WorldResource;

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
	private       int currentScreen;
	
	
	private WorldResource rsrc;
	private boolean isSkinned = false;
	
	/**
	 * 
	 * Creates a new, fresh copy of a world based on the encoded world (typically from restoring from a file). The passed 
	 * object may be reused any number of times with this method. This method will always generate a world that is distinct
	 * from any other world generated in terms of data sharing.
	 * 
	 * @param world
	 * 		the encoded world to create this world from
	 * 	
	 * @return
	 * 		a new world, ready for bonzo to inhabit
	 * 
	 */
	public static World inflateFrom(EncodedWorld world) {
		// TODO method stub
		final String worldName = world.getName();
		final Map<String, Goodie> goodiesInWorld = new HashMap<>();
		for (Entry<String, EncodedGoodie> goodie : world.getGoodies().entrySet() ) {
			goodiesInWorld.put(goodie.getKey(), Goodie.inflateFrom(goodie.getValue() ) );
		}
		
		final Map<Integer, LevelScreen> worldScreens = new HashMap<>();
		for (Entry<Integer, EncodedLevelScreen> screen : world.getLevels().entrySet() ) {
			worldScreens.put(screen.getKey(), LevelScreen.inflateFrom(screen.getValue() ) );
		}
			
		return new World(worldName, goodiesInWorld, worldScreens);	
	}
	
	/**
	 * 
	 * Sets the graphics resources this worlds will use when any of its parts, be it tiles, sprites, etc are drawn.
	 * 
	 * @param rsrc
	 * 
	 */
	public void skin(final WorldResource rsrc) {
		this.rsrc = rsrc;
		
		for (Entry<String, Goodie> goodie : goodiesInWorld.entrySet() ) {
			goodie.getValue().skin(rsrc);
		}
		
		for (Entry<Integer, LevelScreen> level : worldScreens.entrySet() ) {
			level.getValue().skin(rsrc);
		}
		
		this.isSkinned = true;
	}
	
	public boolean isSkinned() { return isSkinned; }
	
	
	/**
	 * 
	 * Explicitly sets the modifiable data values of the world. Does not set the graphics resources, which must be done through a call to
	 * {@code skin}
	 * <p/>
	 * Only static factories call this method. No defensive copying; static factory must make sure there is no data sharing.
	 * 
	 * @param worldName
	 * @param goodiesInWorld
	 * @param worldScreens
	 */
	private World(final String worldName, 
				  final Map<String, Goodie> goodiesInWorld, 
				  final Map<Integer, LevelScreen> worldScreens) {
		
		/* Variable data		*/
		this.worldName = worldName;
		this.goodiesInWorld = goodiesInWorld;
		this.worldScreens = worldScreens;
		
		/* Constant data		*/
		this.currentScreen = 1000;
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
		theBonzo.currentLocation = getCurrentScreen().newPointFromWhereBonzoCame();
		theBonzo.isDying = false;
		theBonzo.isJumping = false;
		getCurrentScreen().resetSprites();
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
			this.currentScreen = screenId;
			return true;
		}
	}
	
	/** Attempts to change the screen in the given direction. Returns false if there is no such screen in that direction.
	 *  Otherwise, modifies the current screen and returns true.														*/
	public boolean screenChange(final int direction) {
		int tempScreen = -1;
		switch (direction) {
			case (GameConstants.LEFT):
				tempScreen = currentScreen - 1;
				break;
			case (GameConstants.UP):
				tempScreen = currentScreen - 100;
				break;
			case (GameConstants.RIGHT):
				tempScreen = currentScreen + 1;
				break;
			case (GameConstants.DOWN):
				tempScreen = currentScreen + 100;
				break;
		}
		return changeCurrentScreen(tempScreen);
	}
	
	// TODO Design: Should it return to Bonzo a special object specifying the results?
	public void checkCollisions(Bonzo theBonzo) {
		// Another Screen?
		int dir = GameConstants.directionLeftTheScreen(theBonzo.currentLocation, Bonzo.BONZO_SIZE_X, Bonzo.BONZO_SIZE_Y);
		if (dir != GameConstants.CENTRE) {
			// Clear the previous screen, so that he starts from original location.
			getCurrentScreen().resetBonzoCameFrom();
			getCurrentScreen().resetSprites();
			switch(dir) {
			case (GameConstants.LEFT):
				screenChange(GameConstants.LEFT);
				theBonzo.screenChangeLeft();
				break;
			case (GameConstants.UP):
				screenChange(GameConstants.UP);
				theBonzo.screenChangeUp();
				break;
			case (GameConstants.RIGHT):
				screenChange(GameConstants.RIGHT);
				theBonzo.screenChangeRight();
				break;
			case (GameConstants.DOWN):
				screenChange(GameConstants.DOWN);
				theBonzo.screenChangeDown();
				break;
			}
			getCurrentScreen().setBonzoCameFrom(theBonzo.currentLocation);
		}
		
		// A Sprite?
		Sprite allSprites[] = getCurrentScreen().getSpritesOnScreen();
		if (allSprites != null) {
			for (Sprite nextSprite : allSprites) {
				Point2D spriteLocation = nextSprite.newPointFromSpritePosition();
				if (GameConstants.checkBoundingBoxCollision(theBonzo.currentLocation, spriteLocation,
						Bonzo.BONZO_SIZE_X, Bonzo.BONZO_SIZE_Y, GameConstants.SPRITE_SIZE_X, GameConstants.SPRITE_SIZE_Y) ) {
					theBonzo.kill();
				}
			}
		}
		
		
		// Assume that Java automatically optimises these with a StringBuilder. If it doesn't
		// we will manually add that feature.
		int topLeftX = (theBonzo.currentLocation.x() + (GameConstants.GOODIE_SIZE_X / 2) ) / GameConstants.GOODIE_SIZE_X;
		int topLeftY = (theBonzo.currentLocation.y() + (GameConstants.GOODIE_SIZE_Y / 2) )/ GameConstants.GOODIE_SIZE_Y;
		
		String topLeftQuad = "" + (currentScreen) + "X" + topLeftX + "," + topLeftY;
		String topRightQuad = "" + (currentScreen) + "X" + (topLeftX + 1) + "," + topLeftY;
		String bottomLeftQuad = "" + (currentScreen) + "X" + topLeftX + "," + (topLeftY + 1);
		String bottomRightQuad = "" + (currentScreen) + "X" + (topLeftX + 1) + "," + (topLeftY + 1);
		
		Goodie gotGoodie;
		if ( (gotGoodie = goodiesInWorld.get(topLeftQuad) ) != null ) {
			gotGoodie.take();
		}
		if ( (gotGoodie = goodiesInWorld.get(topRightQuad) ) != null ) {
			gotGoodie.take();
		}
		if ( (gotGoodie = goodiesInWorld.get(bottomLeftQuad) ) != null ) {
			gotGoodie.take();
		}
		if ( (gotGoodie = goodiesInWorld.get(bottomRightQuad) ) != null ) {
			gotGoodie.take();
		}
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
		if (isSkinned == false) throw new IllegalStateException("Can't add goodie: " + this + " not yet skinned with resource");
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
	
	//public addScreen(int screenID) 
	
	// Also updates the goodies. Why? Why not? They don't do much, saves on clock cycles.
	public void paint(Graphics2D g2d) {
		getCurrentScreen().paint(g2d);
		
		Collection<Goodie> goodiesVector = (Collection<Goodie>)goodiesInWorld.values();
		for (Goodie nextGoodie : goodiesVector) {
			if (nextGoodie.getScreenID() == currentScreen) {
				nextGoodie.update();
				nextGoodie.paint(g2d);
			}
		}
	}

	/**
	 * Returns an immutable copy of the current state of the goodies in this world. Clients can not modify the goodies
	 * in this world through the returned map.
	 * 
	 * @return
	 * 		immutable copy of the map representing the goodies in this world
	 */
	public Map<String, Goodie> getGoodies() { return ImmutableMap.copyOf(this.goodiesInWorld); }

	/**
	 * Returns an immutable copy of all the levels in the world
	 * 
	 * @return
	 */
	public Map<Integer, LevelScreen> getLevelScreens() { return ImmutableMap.copyOf(this.worldScreens); }
	
}
