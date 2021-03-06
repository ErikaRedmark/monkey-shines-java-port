package org.erikaredmark.monkeyshines;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.Set;

import org.erikaredmark.monkeyshines.Conveyer.Rotation;
import org.erikaredmark.monkeyshines.Goodie.Type;
import org.erikaredmark.monkeyshines.bounds.Boundable;
import org.erikaredmark.monkeyshines.bounds.IPoint2D;
import org.erikaredmark.monkeyshines.editor.importlogic.WorldTranslationException;
import org.erikaredmark.monkeyshines.editor.importlogic.WorldTranslationException.TranslationFailure;
import org.erikaredmark.monkeyshines.resource.SoundManager;
import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.erikaredmark.monkeyshines.sprite.Monster;
import org.erikaredmark.monkeyshines.tiles.ConveyerTile;
import org.erikaredmark.monkeyshines.tiles.HazardTile;
import org.erikaredmark.monkeyshines.tiles.PlaceholderTile;
import org.erikaredmark.monkeyshines.tiles.TileType;
import org.erikaredmark.util.collection.RingArray;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Holds all information about the entire world, including methods and data to perform the actual running of the
 * game.
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
	private static final String CLASS_NAME = "org.erikaredmark.monkeyshines.World";
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
	
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
		for (int i = 0; i < amt; ++i) {
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
		
		// Generate # of hazards proportional to graphics context size.
		List<Hazard> hazards = Hazard.initialise(0, rsrc.getHazardCount(), rsrc);
		
		World newWorld = 
			new World(name, 
			   		  new HashMap<WorldCoordinate, Goodie>(), 
					  screens, 
					  hazards, 
					  conveyers, 
					  GameConstants.DEFAULT_BONUS_SCREEN,
					  rsrc);
		
		newWorld.resetAllScreens();
		
		return newWorld;
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
				 final Map<WorldCoordinate, Goodie> goodiesInWorld,
				 final Map<Integer, LevelScreen> worldScreens,
				 final List<Hazard> hazards,
				 final List<Conveyer> conveyers,
				 final int bonusScreen,
				 final WorldResource rsrc) {
		
		/* Variable data		*/
		this.worldName = worldName;
		this.goodiesInWorld = goodiesInWorld;
		this.worldScreens = worldScreens;
		this.hazards = hazards;
		this.conveyers = conveyers;
		this.bonusScreen = bonusScreen;
		this.rsrc = rsrc;
		
		/* Constant data		*/
		this.currentScreen = 1000;
		this.bonusCountdown = 10000;
		
		/* Default data 		*/
		this.author = "Unknown";
		this.returnScreen = null;
		
		/* Data that can be computed */
		
		// We must pre-construct the mapping of screen to goodie list, for 
		// speed in some reset algorithms, and we must precompute red/blue
		// key sets.
		this.goodiesPerScreen = HashMultimap.create();
		this.redKeys = new HashSet<Goodie>();
		this.blueKeys = new HashSet<Goodie>();
		
		for (Entry<WorldCoordinate, Goodie> entry : goodiesInWorld.entrySet() ) {
			// Extract just the level id. Assume it can convert to integer, because otherwise would
			// indicate level corruption anyway.
			WorldCoordinate coordinate = entry.getKey();
			LevelScreen screenForGoodie = worldScreens.get(coordinate.getLevelId() );
			
			Goodie value = entry.getValue();
			goodiesPerScreen.put(screenForGoodie.getId(), new GoodieLocationPair(value, coordinate) );
			
			// Now fill in the proper red and blue keys as required
			if (value.getGoodieType() == Goodie.Type.RED_KEY)  		 this.redKeys.add(value);
			else if (value.getGoodieType() == Goodie.Type.BLUE_KEY)  this.blueKeys.add(value);
		}
		
		// To easily enable bonus and exit doors, we add all such sprites to lists
		// based on type.
		for (LevelScreen lvl : worldScreens.values() ) {
			for (Monster s : lvl.getMonstersOnScreen() ) {
				if 		(s.getType() == MonsterType.EXIT_DOOR)   this.exitDoors.add(s);
				else if (s.getType() == MonsterType.BONUS_DOOR)  this.bonusDoors.add(s);
			}
		}
		
		// Finally, if for some reason bonzo dies on the first screen, we set the initial safe place to be
		// the starting location, as this is the ONLY point in the entire game that our ring buffer of screens
		// will be empty.
		final LevelScreen currentScreen = getCurrentScreen();
		currentScreen.setBonzoLastOnGround(currentScreen.getBonzoStartingLocation() );
	}
	
	/**
	 * 
	 * Should be called after construction to reset all screens to default; creating the initial staggering animation
	 * effect. It is up to client to call this as otherwise an overridable method would be called from the constructor,
	 * and in some cases this is not desired (like import logic)
	 * 
	 */
	public void resetAllScreens() {
		for (LevelScreen lvl : worldScreens.values() ) {
			lvl.resetScreen();
		}
	}
	
	/**
	 * Returns a LevelScreen in this level pointed to by ID if that screen exists. 
	 * @param id the id number of the screen to retrieve. Remember that the first screen in the map will be
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
	public boolean screenIdExists(final int id) { return (worldScreens.get(id) != null); }
	
	/**
	 * Take the currentScreen integer and uses it to resolve the actual LevelScreen object
	 * @return a LevelScreen object identified by the integer ID currentScreen. 
	 */
	public LevelScreen getCurrentScreen() { return getScreenByID(currentScreen); }
	
	/**
	 * Gets the currentScreen ID number. Does not return the actual LevelScreen object.
	 * @return The ID number of the current screen.
	 */
	public int getCurrentScreenId() { return currentScreen; }
	
	/**
	 * Gets the bonus screen id of the current bonus screen
	 * @return id of bonus screen
	 */
	public int getBonusScreen() { return bonusScreen; }
	
	/** 
	 * Changes the location the bonus door on the return screen should take bonzo.
	 * Only to be called from level editor 
	 * @param id
	 */
	public void setBonusScreen(int id) { this.bonusScreen = id; }
	
	public String getAuthor() { return this.author; }
	
	public void setAuthor(final String author) { this.author = author; }
	
	
	/**
	 * 
	 * Indicates the given key has been collected and removes if from the set. If this was
	 * the last key, also toggles the 'allRedKeysTaken' method.
	 * <p/>
	 * If assertions are enabled, this throws an assertion error if the goodie type is not
	 * a red key or if the set does not contain the key anymore. Program logic should prevent
	 * collections for already taken goodies.
	 * 
	 * @param goodie
	 * 		the key collected
	 * 
	 */
	public void collectedRedKey(Goodie goodie, SoundManager sound) {
		assert goodie.getGoodieType() == Type.RED_KEY : "Cannot collect a red key of " + goodie + " as that isn't a red key";
		assert this.redKeys.contains(goodie) : "Red Key " + goodie + " already collected: Logic Error";
		
		this.redKeys.remove(goodie);
		if (this.redKeys.isEmpty() )  allRedKeysTaken(sound);
	}
	
	/**
	 * 
	 * Same as {@code collectedRedKey} only for blue keys
	 * 
	 * @param goodie
	 * 		the key collected
	 * 
	 */
	public void collectedBlueKey(Goodie goodie, SoundManager sound) {
		assert goodie.getGoodieType() == Type.BLUE_KEY : "Cannot collect a blue key of " + goodie + " as that isn't a blue key";
		assert this.blueKeys.contains(goodie) : "Blue Key " + goodie + " already collected: Logic Error";
		
		this.blueKeys.remove(goodie);
		if (this.blueKeys.isEmpty() )  
			{ allBlueKeysTaken(sound); }
	}
	
	public void allRedKeysTaken(SoundManager sound) {
		sound.playOnce(GameSoundEffect.LAST_RED_KEY);
		for (Monster s : exitDoors) {
			s.setVisible(true);
		}
		
		if (allRedKeysCollectedCallback != null) {
			allRedKeysCollectedCallback.run();
		}
	}
	
	public void allBlueKeysTaken(SoundManager sound) {
		sound.playOnce(GameSoundEffect.LAST_BLUE_KEY);
		for (Monster s : bonusDoors) {
			s.setVisible(true);
		}
	}
	
	/**
	 * 
	 * Sets the callback function to be called when all the red keys have been collected. This object has
	 * its own game logic handling for all red keys (making exit visible) but it is up to UI to start the 
	 * bonus countdown timer.
	 * 
	 * @param runnable
	 * 		the runnable that will run as soon as all the red keys are collected.
	 * 
	 */
	public void setAllRedKeysCollectedCallback(final Runnable runnable) {
		this.allRedKeysCollectedCallback = runnable;
	}
	
	/**
	 * 
	 * Decrements the bonus for this world by 10. When the bonus hits zero, this
	 * returns false to indicate no more countdowns can be down. Returns true otherwise.
	 * <p/>
	 * Has the sideffect of playing the bonus countdown sound effect.
	 * 
	 * @return
	 * 		{@code true} if the bonus can be decremented again, {@code false} if otherwise
	 * 
	 */
	public boolean bonusCountdown(SoundManager sound) {
		assert bonusCountdown > 9 : "Cannot decrement bonus anymore; timer should have stopped";
		bonusCountdown -= 10;
		sound.playOnce(GameSoundEffect.TICK);
		return bonusCountdown > 0;
	}
	
	/**
	 * 
	 * @return
	 * 		the current bonus for the world. This is the bonus shown in red numbers that counts down
	 * 		once all red keys are found.
	 * 
	 */
	public int getCurrentBonus() {
		return bonusCountdown;
	}
	
	/**
	 * Looks at the loaded LevelScreen and determines where Bonzo is supposed to restart from. This restart
	 * value is either loaded from the LevelScreen constant (if Bonzo started here) or from the place he
	 * entered the screen from IF his ground location is non null. If he never hit the ground on the
	 * current screen, we use a different algorithm (otherwise he may infinitely respawn over death)
	 * Up to {@code GameConstants.LEVEL_HISTORY} previous levels are checked for a non-null ground
	 * location to safely respawn bonzo. If that fails, bonzo will respawn at the place he entered the
	 * earliest screen in the history from, in the hopes that the level design isn't completely evil.
	 * <p/>
	 * After calling this method, it is advisable to call the 'respawnGrace' method, which pauses the game and points
	 * out where bonzo is, giving a more fair reaction time. {@link GameWorldLogic#respawnGrace()}
	 * 
	 * @param theBonzo
	 */
	public void restartBonzo(Bonzo theBonzo) {
		// no matter what, we are resetting this screen. Must do this first as restarting bonzo
		// is an if-else mess of early returns.
		resetCurrentScreen();
		// If the current screen has valid ground landings, just use the cameFrom location
		final LevelScreen currentScreen = getCurrentScreen();
		ImmutablePoint2D ground = currentScreen.getBonzoLastOnGround();
		if (ground != null) {
			// No need to change screen in world; It's the same one
			// First, check if where bonzo came from is safe (on the ground). If it is not, such as
			// bonzo falling onto the screen with a wing, then dying later, we use the ground state, which
			// we already confirmed is not null
			
			// Snap bonzo's come from position, and look TWO tiles below (bonzo takes up 2, so top left + 2 gets bottom)
			// on both left and right. Lack of solid ground indicates that the ground state, that the come-from state,
			// should be used.
			BonzoSaveState bonzoCameFrom = currentScreen.getBonzoCameFrom();
			ImmutablePoint2D bonzoCameFromPoint = ImmutablePoint2D.of(bonzoCameFrom.x, bonzoCameFrom.y);
			int t1x = bonzoCameFrom.x / GameConstants.TILE_SIZE_X;
			int ty = bonzoCameFrom.y / GameConstants.TILE_SIZE_Y + 2;
			int t2x = t1x + 1;
			
			// Basic sprite check

			// We look four tiles down, max before fall becomes damaging.
			if (spriteSafetyCheck(currentScreen, bonzoCameFromPoint) ) {
				for (int dist = 0; dist < 4; ++dist) {
					final TileMap map = currentScreen.getMap();
					if (   map.getTileXY(t1x, ty + dist).isLandable() 
					    || map.getTileXY(t2x, ty + dist).isLandable() 
					    // Special case: bonzo JUMPED into this room. Safe to respawn where he came from
					    || ty + dist > GameConstants.LEVEL_ROWS) {
						
						theBonzo.restartBonzoOnScreen(currentScreen, bonzoCameFrom);
						return;
					}
				} 
			} // else no early return, not safe to respawn
			
			// Entry point into screen not safe. Go to ground. This may make certain
			// levels easier, but easier is better than infinite death.
			if (spriteSafetyCheck(currentScreen, ground) ) {
				theBonzo.restartBonzoOnScreen(currentScreen, BonzoSaveState.fromPoint(ground) );
				return;
			}
		}
		
		// This is ONLY reached if we did not early return, which would only happen if a previous check
		// failed.
		// Uh oh! We need to progress backwards through screen history and find a good ground
		RingArray<LevelScreen> screenHistory = theBonzo.getScreenHistory();
		for (LevelScreen s : screenHistory) {
			ImmutablePoint2D sGround = s.getBonzoLastOnGround();
			if (sGround == null)  continue;
			if (!(spriteSafetyCheck(s, sGround) ) )  continue;
			
			// Must change world screen as well as bonzos reference
			changeCurrentScreen(s.getId(), theBonzo);
			
			// Valid ground: Restart bonzo and end the method early.
			theBonzo.restartBonzoOnScreen(s, BonzoSaveState.fromPoint(sGround) );
			return;
			
			// Note: we do NOT use starting locations defined on levels here, otherwise we may accidentally
			// backtrack the player if a screen contains multiple paths.
		}
		
		// Reaching the end of the for loop normally signifies no valid ground in ALL
		// history. That must be one LONG fall; move bonzo to the last screen.
		final LevelScreen lastResort = screenHistory.back();
		changeCurrentScreen(lastResort.getId(), theBonzo);
		BonzoSaveState lastResortCameFrom = lastResort.getBonzoCameFrom();
		if (spriteSafetyCheck(lastResort, ImmutablePoint2D.of(lastResortCameFrom.x, lastResortCameFrom.y ) ) ) {
			theBonzo.restartBonzoOnScreen(lastResort, lastResort.getBonzoCameFrom() );
		} else {
			// Okay, unconditional respawn on the starting location defined in the level... If there is
			// a sprite there, that is the level designers fault. We tried our best.
			ImmutablePoint2D lastResortPoint = lastResort.getBonzoStartingLocationPixels();
			theBonzo.restartBonzoOnScreen(lastResort, BonzoSaveState.fromPoint(lastResortPoint) );
		}
	}
	
	/**
	 * 
	 * Looks at bonzo and the given screen, and decides if that location is unsafe for spawn due to sprites
	 * 
	 */
	private static boolean spriteSafetyCheck(final LevelScreen screen, final ImmutablePoint2D respawnLocation) {
		ImmutableRectangle respawnBox = ImmutableRectangle.of(respawnLocation.x(), respawnLocation.y(), 40, 40);
		// Note: In ALL cases, only Killers and Energy Drainers affect this algorithm
		for (Monster nextSprite : screen.getMonstersOnScreen() ) {
			if (nextSprite.getType() != MonsterType.NORMAL && nextSprite.getType() != MonsterType.HEALTH_DRAIN)  continue;
			
			ImmutableRectangle spriteBounds = nextSprite.getCurrentBounds();
			
			if (spriteBounds.intersect(respawnBox) != null) {
				return false;
			}
		}
		
		// else if for loop terminated normally
		return true;
	}

	/**
	 * 
	 * Transfers bonzo to either the bonus room if he is not already in it, or the return room if in the bonus room.
	 * <p/>
	 * In the event that this transfer fails because of an incorrect world design, this may do nothing.
	 * 
	 * @param bonzo
	 * 
	 * @return
	 * 		{@code true} if bonzo was transferred, {@code false} if the bonus screen id was set to a non-existant screen
	 * 
	 */
	public boolean bonusTransfer(Bonzo bonzo) {
		int transferScreenId;
		if (returnScreen == null) {
			// Branch 1: Return screen not yet; this is bonzos first trip and his destination is the bonus screen. This current
			// screen is his return
			transferScreenId = bonusScreen;
			
			// Only place in entire object this variable should be set from!
			returnScreen = currentScreen;
		} else {
			// Branch 2: Return screen is already set. If bonzo is entering a bonus door ON the return screen, then he is sent
			// to the bonus room. Otherwise, he is sent to the return screen.
			// Note that the bonus room need not be the same room as the bonus door that leads off of it.
			transferScreenId =   currentScreen == returnScreen
							   ? bonusScreen
							   : returnScreen;
		}
		
		// Must change world screen as well as bonzos reference
		// Unlike respawning, this DOES count as screen history!
		if (!(screenIdExists(transferScreenId) ) ) {
			// Indicate that the level designer must fix this by logging the issue.
			LOGGER.severe("Bonus screen " + transferScreenId + " does not exist. Cannot teleport Bonzo: The bonus screen was NOT properly set in the level editor!");
			return false;
		}
		LevelScreen transferScreen = getScreenByID(transferScreenId);
		
		// No transfer screen indicates uncommon level design. Pacman it and just send Bonzo to the
		// other side of the same screen, pretending that the transfer screen is this one.
		if (transferScreen == null) {
			transferScreen = getCurrentScreen();
		}
		
		changeCurrentScreen(transferScreenId, bonzo);
		bonzo.setCurrentLocation(transferScreen.getBonzoStartingLocationPixels() );
		
		// Momentuem is always recent on bonus transfers
		transferScreen.setBonzoCameFrom(
			BonzoSaveState.fromPoint(transferScreen.getBonzoStartingLocationPixels() ) 
		);
		
		return true;
	}
	
	/**
	 * 
	 * Resets the current screen. This involves not only the basic level screen reset, but it must
	 * look for any world entities (like goodies) that need resetting if already grabbed.
	 * 
	 */
	private void resetCurrentScreen() {
		final LevelScreen currentScreen = getCurrentScreen();
		currentScreen.resetScreen();
		// reset goodies
		for (GoodieLocationPair pair : goodiesPerScreen.get(currentScreen.getId() ) ) {
			pair.goodie.resetIfApplicable();
		}
	}
	
	/**
	 * 
	 * Returns a listing of all the goodies that appear on the given level, including their locations.
	 * 
	 * @param id
	 * 		id of the level
	 * 
	 * @return
	 * 		goodies on level, or an empty collection if the level id does not exist
	 * 
	 */
	public Collection<GoodieLocationPair> getGoodiesForLevel(int id) {
		return goodiesPerScreen.get(id);
	}
	
	
	/**
	 * Get the world name 
	 */
	public String getWorldName() {
		return this.worldName;
	}
	
	/** 
	 * 
	 * Adds a level screen to the world. If the screen already exists, this method throws an exception
	 *  
	 * @param screen
	 * 		the new screen to add
	 * 
	 * @throws IllegalArgumentException
	 * 		if the given screen already exists in the world. Use {@code screenIdExists() } to check and {@code removeScreen}
	 * 		first if the intent is to replace
	 * 
	 */
	public void addScreen(final LevelScreen screen) {
		if (this.worldScreens.containsKey(screen.getId() ) ) {
			throw new IllegalArgumentException("Screen id " + screen.getId() + " already exists");
		}
		
		this.worldScreens.put(screen.getId(), screen);
	}
	
	/**
	 * 
	 * Adds the given level screen to the world, with no checks if a screen by that id already exists in the world. If it
	 * did, it is replaced with the new screen.
	 * 
	 * @param screen
	 * 		new screen to add
	 * 
	 */
	public void addOrReplaceScreen(final LevelScreen screen) {
		this.worldScreens.put(screen.getId(), screen);
	}
	
	/**
	 * 
	 * Removes the given level screen from the world based on the id. If the screen does not exist, throws an exception.
	 * <p/>
	 * There is one very special case: screen id 1000 may NEVER be removed under any circumstances. If the intent is to replace,
	 * use {@code addOrReplaceScreen(LevelScreen) }
	 * 
	 * @param screenId
	 * 		screen id to remove
	 * 
	 * @throws IllegalArgumentException
	 * 		if no screen by that id exists in the world, or if this tried to delete screen 1000
	 * 
	 */
	public void removeScreen(int screenId) {
		if (!(this.worldScreens.containsKey(screenId) ) ) {
			throw new IllegalArgumentException("Screen id " + screenId + " does not exist");
		}
		
		if (screenId == 1000) {
			throw new IllegalArgumentException("Screen 1000 may not be removed from a world, ever");
		}
		
		this.worldScreens.remove(screenId);
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
	 * @param bonzo
	 * 		reference to bonzo to update his screen location. Both values must stay synced. This May be {@code null}
	 * 		only if there is no bonzo at all (such as being called from the level editor)
	 * 
	 * @return
	 * 		{@code true} if changing the screen was successful, {@code false} if the screen does not exist and thus
	 * 		could not be switched
	 * 
	 */
	public boolean changeCurrentScreen(int screenId, Bonzo bonzo) {
		if (screenIdExists(screenId) == false) return false;
		else {
			resetCurrentScreen();
			this.currentScreen = screenId;
			if (bonzo != null)  bonzo.changeScreen(screenId);
			return true;
		}
	}
	
	/**
	 * Checks collisions in the world with respect to Bonzo.
	 * <p/>
	 * This may produce sounds if a sound manager is passed in.
	 * @param theBonzo
	 * @param sound
	 */
	public void checkCollisions(Bonzo theBonzo, SoundManager sound) {
		// Don't waste time checking collisions if bonzo is dying
		if (theBonzo.isDying()) return;
		
		// Another Screen?
		ImmutablePoint2D currentLocation = theBonzo.getCurrentLocation();
		ScreenDirection dir = ScreenDirection.fromLocation(currentLocation, Bonzo.BONZO_SIZE);
		if (dir != ScreenDirection.CURRENT) {
			int newId = dir.getNextScreenId(this.currentScreen);
			changeCurrentScreen(newId, theBonzo);
			dir.transferLocation(theBonzo.getMutableCurrentLocation(), Bonzo.BONZO_SIZE);
			final LevelScreen theNewScreen = getCurrentScreen();
			// Update the new screen with data about where we came from so deaths bring us to the same place
			ImmutablePoint2D bonzoVelocity = theBonzo.getCurrentVelocity();
			ImmutablePoint2D bonzoCurrentLocation = theBonzo.getCurrentLocation();
			theNewScreen.setBonzoCameFrom(
				BonzoSaveState.of(
					bonzoCurrentLocation.x(),
					bonzoCurrentLocation.y(),
					bonzoVelocity.x(), 
					bonzoVelocity.y(),
					theBonzo.isJumping(),
					theBonzo.getCurrentConveyerEffect()
				) 
			);
			// If the current screen has a 'bonzo last on ground' state, reset it. Otherwise dying may bring him
			// to the wrong screen in the wrong part.
			theNewScreen.resetBonzoOnGround();
			
			// Ignore any other collisions for now.
			return;
		}
		// A Sprite?
		List<Monster> allSprites = getCurrentScreen().getMonstersOnScreen();
		ImmutableRectangle bonzoBounding = theBonzo.getCurrentBounds();
		for (Monster nextSprite : allSprites) {
			Boundable intersection = nextSprite.getCurrentBounds().intersect(bonzoBounding);
			if (intersection != null) {
				// Bounding box check done. Do more expensive pixel check
				// TODO move to Slick based
				if (nextSprite.pixelCollision(theBonzo, intersection) ) {
					nextSprite.getType().onBonzoCollision(theBonzo, this, sound);
					// do not do further collisions after bonzo dies
					break;
				}
			}
		}
		
		// It is entirely possible that bonzo just transferred screens from the above collision. His position
		// must be recomputed.
		currentLocation = theBonzo.getCurrentLocation();
		
		// A hazard?
		hazardCollisionCheck(theBonzo, sound);
		
		
		// A goodie?
		
		int topLeftX = (currentLocation.x() + (GameConstants.GOODIE_SIZE_X / 2) ) / GameConstants.GOODIE_SIZE_X;
		int topLeftY = (currentLocation.y() + (GameConstants.GOODIE_SIZE_Y / 2) )/ GameConstants.GOODIE_SIZE_Y;
		
		// Top-left, Top-Right, Bottom-Left, Bottom-Right
		WorldCoordinate[] goodieQuads = new WorldCoordinate[] {
			new WorldCoordinate(currentScreen, topLeftX, topLeftY),
			new WorldCoordinate(currentScreen, topLeftX + 1, topLeftY),
			new WorldCoordinate(currentScreen, topLeftX, topLeftY + 1),
			new WorldCoordinate(currentScreen, topLeftX + 1, topLeftY + 1)
		};
		// Add to the total number of goodies the player has collected, provided the goodie actually grants non-zero
		// score.
		for (WorldCoordinate quad : goodieQuads) {
			Goodie gotGoodie;
			if ( (gotGoodie = goodiesInWorld.get(quad) ) != null ) {
				if (gotGoodie.take(theBonzo, this, sound) ) {
					if (gotGoodie.getGoodieType().score > 0)  ++goodiesCollected;
				}
			}
		}
	}
	
	public int getGoodiesCollected() {
		return goodiesCollected;
	}
	
	/**
	 * Performs a check if the bonzo is on one or more 'hazard' tiles. If so, then the hazard it set
	 * to explode (if required) and bonzo is killed based on the hazard properties.
	 */
	private void hazardCollisionCheck(Bonzo bonzo, SoundManager sound) {
		ImmutablePoint2D[] tilesToCheck = effectiveTilesCollision(bonzo.getCurrentBounds() );
		final TileMap map = getCurrentScreen().getMap();
		for (ImmutablePoint2D tile : tilesToCheck) {
			TileType type = map.getTileXY(tile.x(), tile.y() );
			if (type instanceof HazardTile) {
				// Still can get out of doing anything if the hazard is already gone.
				HazardTile hazard = (HazardTile) type;
				// MUST check isExploding. If bonzo had invincibility and lost it 1 tick after touching
				// a bomb, the bomb is technically already no longer a hurt for Bonzo.
				if (hazard.isDead() || hazard.isExploding() )  continue;
				
				hazard.hazardHit(sound);
				// Last check; is this hazard harmless? Harmless hazards still play hit sounds and explode, hence why
				// we did not check earlier.
				if (!(hazard.getHazard().isHarmless() ) ) {
					// Send a kill message to bonzo. Only invincibility will save him
					bonzo.tryKill(hazard.getHazard().getDeathAnimation(), sound);
				}
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
	
	/**
	 * 
	 * Adds a goodie to the given world, typically only used by level editor.
	 * 
	 * @param screenId
	 * @param row (also known as x)
	 * @param col (also known as y)
	 * @param type
	 * 
	 */
	public void addGoodie(final int screenId, final int row, final int col, final Goodie.Type type) {
		WorldCoordinate coordinate = new WorldCoordinate(screenId, row, col);
		// If goodie already exists, take out and replace
		removeGoodie(screenId, row, col);
		Goodie newGoodie = Goodie.newGoodie(type, ImmutablePoint2D.of(row, col), screenId);
		goodiesInWorld.put(coordinate, newGoodie);
		goodiesPerScreen.put(screenId, new GoodieLocationPair(newGoodie, coordinate) );
	}
	
	/**
	 * 
	 * Removes a goodie at the given position for the given screen. Goodie will be removed from all relevant structures.
	 * If there is no goodie at the given location, this method does nothing.
	 * 
	 * @param screenId
	 * @param row
	 * @param col
	 * 
	 */
	public void removeGoodie(final int screenId, final int row, final int col) {
		WorldCoordinate coordinate = new WorldCoordinate(screenId, row, col);
		if (goodiesInWorld.get(coordinate) != null) {
			goodiesInWorld.remove(coordinate);
			// We still have this goodie lurking somewhere in the other structure. Remove it there too.
			Collection<GoodieLocationPair> screenGoodies = goodiesPerScreen.get(screenId);
			for (Iterator<GoodieLocationPair> pairIt = screenGoodies.iterator(); pairIt.hasNext(); /* No op */ ) {
				GoodieLocationPair pair = pairIt.next();
				if (pair.location.getRow() == row && pair.location.getCol() == col) {
					pairIt.remove();
					// Only one to find. No need to keep searching there are not duplicates.
					break;
				}
			}
		}
	}
	
	/**
	 * 
	 * Called when this world is over, as in bonzo died, left, whatever. It is up to clients to decide when a world is done.
	 * When it is, final statistics computations are done and become available.
	 * <p/>
	 * Does nothing if the world is already finished.
	 * 
	 * @param bonzo
	 * 		reference to bonzo. Some of his data is used in stats calculations after finishing
	 * 
	 */
	public void worldFinished(Bonzo bonzo) {
		worldFinished = true;
		stats = new WorldStatistics(
			goodiesInWorld.values(), 
			goodiesCollected, 
			bonzo.getScore(), 
			bonusCountdown,
			bonzo.getLives() == Bonzo.INFINITE_LIVES ? true : false);
	}
	
	/**
	 * 
	 * Determines if the world is finished and final statistical computations are available.
	 * 
	 * @return
	 * 		{@code true} if the world is finished form a previous call to {@code worldFinished}, 
	 * 		{@code false} if otherwise.
	 * 
	 */
	public boolean isWorldFinished() {
		return worldFinished;
	}
	
	/**
	 * 
	 * Returns a statistics object after the world was over that indicates points and totals, with all multipliers applied
	 * as required. Intended for the final tally screen as well as to set the high score.
	 * <p/>
	 * This object is only available if {@code isWorldFinished} is {@code true}. Otherwise, calling this method is
	 * an error.
	 * 
	 * @return
	 * 		statistics of the finished world. Never {@code null}, but check preconditions to ensure the method
	 * 		does not throw an exception
	 * 
	 * @throws IllegalStateException
	 * 		if the world is not finished yet, and hence no statistics are available.
	 * 
	 */
	public WorldStatistics getStatistics() {
		if (stats == null)  throw new IllegalStateException("World should be finished before calling this method");
		
		return stats;
	}
	
	/**
	 * 
	 * Should be called every tick at {@code GameConstants.GAME_SPEED}. Updates the game. Each update
	 * call is one tick of game time.
	 * 
	 */
	public void update() {
		getCurrentScreen().update();
		
		// TODO group goodies into a better collection based on screen
		Collection<Goodie> goodiesVector = (Collection<Goodie>)goodiesInWorld.values();
		for (Goodie nextGoodie : goodiesVector) {
			if (nextGoodie.getScreenID() == currentScreen) {
				nextGoodie.update();
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
	public Map<WorldCoordinate, Goodie> getGoodies() { return Collections.unmodifiableMap(this.goodiesInWorld); }

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
	 * Returns an unmodifiable version of the list of conveyers in the world.
	 * Remember that each type (id) of conveyer is represented by two distinct conveyers; one moving clockwise and the next moving anti-clockwise
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
	
	/**
	 * 
	 * <strong> only intended for use by translation utilities for original Monkey Shines file format </strong>
	 * <p/>
	 * Goes through every level in the world, and for each placeholder tile replaces it with a real version. Please
	 * read the docs on {@code PlaceholderTile} for an explanation of why this is used. This is an expensive operation
	 * and should be done only when the hazards and conveyer lists are finalised, and all the level data has been
	 * added.
	 * <p/>
	 * At the conclusion of this method, the world will have no more placeholder tiles in any of its levels.
	 * 
	 * @throws WorldTranslationException
	 * 		if placeholders cannot be fixed because the resource pack did not define enough of either hazards or
	 * 		conveyer belts
	 * 
	 */
	public void fixPlaceholders() throws WorldTranslationException {
		for (LevelScreen lvl : worldScreens.values() ) {
			TileMap tileMap = lvl.getMap();
			// We iterate and assign internally because this is such a specific case that it isn't relevant to
			// be part of TileMap API
			TileType[] map = tileMap.internalMap();
			final int size = tileMap.getRowCount() * tileMap.getColumnCount();
			for (int i = 0; i < size; ++i) {
				if (map[i] instanceof PlaceholderTile) {
					int metadata = ((PlaceholderTile)map[i]).getMetaId();
					PlaceholderTile.Type type = (((PlaceholderTile)map[i])).getType();
					switch (type) {
					case HAZARD:
						if (metadata >= this.hazards.size() ) {
							throw new WorldTranslationException(TranslationFailure.TRANSLATOR_SPECIFIC, "Not enough hazards defined in resource pack. Must have at least " + (metadata + 1) );
						}
						map[i] = HazardTile.forHazard(this.hazards.get(metadata) );
						break;
					case CONVEYER_ANTI_CLOCKWISE:
					{
						int index = (metadata * 2) + 1;
						if (index >= this.conveyers.size() ) {
							throw new WorldTranslationException(TranslationFailure.TRANSLATOR_SPECIFIC, "Not enough unique conveyers defined in resource pack. Must have at least " + (metadata + 1) );
						}
						map[i] = new ConveyerTile(this.conveyers.get(index) );
						break;
					}
					case CONVEYER_CLOCKWISE: 
					{
						int index = (metadata * 2);
						if (index >= this.conveyers.size() ) {
							throw new WorldTranslationException(TranslationFailure.TRANSLATOR_SPECIFIC, "Not enough unique conveyers defined in resource pack. Must have at least " + (metadata + 1) );
						}
						map[i] = new ConveyerTile(this.conveyers.get(index) );
						break;
					}
					default:
						throw new RuntimeException("Unknown enumeration " + type + " for fixing placeholders");
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * Provides a pairing of a location in the world to a goodie. This is NOT used in normal calculations (cooridnates are
	 * in a map and obtained via checking the map). This is intended for when a system needs to know both the goodies
	 * and the locations of the goodies on a specific screen only.
	 * 
	 * @author Erika Redmark
	 *
	 */
	public static final class GoodieLocationPair {
		public final Goodie goodie;
		public final WorldCoordinate location;
		
		private GoodieLocationPair(final Goodie goodie, final WorldCoordinate location) {
			this.goodie = goodie;
			this.location = location;
		}
	}
	
	/***************
	 * Private Data
	 **************/
	
	private final String worldName;

	private final Map<WorldCoordinate, Goodie> goodiesInWorld;
	private int goodiesCollected;
	
	// Holds a list of all goodies on a particlar screen Id. During screen reset, relevant goodies may
	// need to be regenerated.
	// Goodies removed from a world are also removed from this map in parallel. This acts only as an optimisation so that
	// all goodies in a screen can be looked at at once (typically for drawing or updating)
	private final Multimap<Integer, GoodieLocationPair> goodiesPerScreen;
	
	// When a world is initialised, hold a set of all blue and red keys. When taken, they will
	// be removed from the set. The moment a set becomes empty, it toggles the 'all blue keys' or
	// 'all red keys' collected event.
	// NOTE: If the editor adds keys, this goes out of sync. IT DOESN'T MATTER. When the level is saved
	// and reloaded, this object is re-initialised for gameplay with the right values and keys can't be
	// added during gameplay.
	private final Set<Goodie> redKeys;
	private final Set<Goodie> blueKeys;
	
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
	
	private int currentScreen;
	
	// This is defaults to either 10000 or from the save file. It is up to the level editor
	// to set this. However, it should do so automatically on every save.
	// Effectively final for game, mutable for level editor
	private int bonusScreen;
	
	// Screen bonzo returns to when leaving bonus world. This is automatically set to the screen bonzo first
	// enters a bonus door. This is done so the level editor user doesn't need to set both screens.
	// This MAY be null, in which case it is awaiting initial setting.
	// In practise, the entry
	// to the bonus world is always on the same screen Id (as the bonus world has its own bonus door
	// somewhere that must take bonzo back), but this is left dynamically set in case two bonus doors
	// are to link to a dead-end bonus room.
	private Integer returnScreen;
	
	// When a world is first created, it has an associated bonus countdown of 10000. Once all red keys are collected,
	// the main game session will start to decrement this every second or so.
	private int bonusCountdown;
	
	// Lists of all bonus and exit doors so that setting them visible when all keys are collected doesn't
	// require iterating over every sprite in the world.
	private final List<Monster> bonusDoors = new ArrayList<>(4); // initial size 4. 2 bonus doors, possibly double doored sprites for some worlds.
	private final List<Monster> exitDoors = new ArrayList<>(4); // Just in case multiple exits, or exit made up of multiple sprites.
	
	// Intended for callback to UI when certain victory or defeat conditions are met
	// not set in constructor; will not be run if never set.
	private Runnable allRedKeysCollectedCallback;
	
	// The author of the world. Defaults to "Unknown"
	private String author;
	
	private final WorldResource rsrc;
	
	// This field is ONLY created after the game is over. See javadocs on accessor methods.
	private WorldStatistics stats;
	private boolean worldFinished;


}
