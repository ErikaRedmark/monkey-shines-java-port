package org.erikaredmark.monkeyshines;

import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.erikaredmark.monkeyshines.util.GameEndCallback;

/**
 * 
 * Separates how the game will be drawn to the screen vs the game 'world', as in the flow of time
 * in a current game, where things are, some drawing information, etc. However, it is up to a dedicated
 * graphical class to decide how to draw this world onto the screen, and what transformations to do.
 * This STILL contains graphical components, but it does not actually DRAW them onto a graphics
 * context.
 * <p/>
 * The game logic runs at a speed defined in {@code GameConstants.GAME_SPEED}. The speed in which
 * the world is 'painted' onto some graphics context should either be equal to or faster than
 * this speed (most likely equal for passive rendering, and faster for active) for the best
 * user experience.
 * 
 * @author Erika Redmark
 *
 */
public final class GameWorldLogic {
	// Started when the last key is collected, stopped when the 'bonus score' hits zero.
	// Starts at -1. Once activated, it loops between 0 and GameConstants.BONUS_COUNTDOWN_DELAY.
	// The easiest way to poll this is to check each update tick if this is 0.
	private long bonusTimer = -1;
	private int[] bonusDigits = new int[] { 9, 9, 9, 9 };
	public static int BONUS_NUM_DIGITS = 4;
	
	// When paused, the game world does not process all updates.
	private boolean paused;
	
	// The player and the world
	private Bonzo bonzo;
	private World currentWorld;
	
	/* -------------------- Digits ---------------------- */
	/* Numerical values displayed in the UI are broken up
	 * into digits so that the class can easily map a digit
	 * to the image of the digit.
	 */
	public static final int SCORE_NUM_DIGITS = 7;
	// Digits are updated when score is updated. Digits are always drawn from this
	// array to avoid digit extraction algorithms every frame
	// Default value 0 is guaranteed by language. Bonzo's score always starts at 0
	private final int digits[] = new int[SCORE_NUM_DIGITS];
	
	/* ------------------- Grace Animation ------------------ */
	// When bonzo dies, the game world is in a state of 'grace'
	// for a few moments. It is in grace when this timer is changed from
	// -1 to 0, and whilst in grace it counts up to GameConstants.GRACE_PERIOD_FRAMES
	// before resetting back to -1.
	// Whilst in grace, renderers should instead draw an overlay over the world
	// based on how close the grace period is to ending.
	private int grace = -1;
	

	/**
	 * Constructs the living game world. This is essentially the universe wrapping the 
	 * static {@code World} to make it come alive. This includes information about whether
	 * a grace animation should run, whether it is paused, etc.
	 * <p/>
	 * There are no callbacks. This is intended only for use within an update loop that
	 * updates and polls when needed.
	 * <p/>
	 * This world will be in 'stasis', as in not running, when created. {@code start}
	 * must be called.
	 * 
	 * @param keys
	 * 		a keybord input device to allow the world to respond to the player
	 * 
	 * @param keyBindings
	 * 		a binding that specifies which keys are to perform what actions
	 * 
	 * @param world
	 * 		the world that needs to be brought to life and run
	 * 
	 * @param endGameCallback
	 * 		a callback for when the game ends
	 * 
	 * @param lifeLostCallback
	 * 		a callback when bonzo loses a life but it isn't game over. Note that bonzo's life counter
	 * 		has already decremented, and at the point of this call, bonzo has already 'restarted' on
	 * 		the screen and all respawn rules have been applied. This is mainly for graphics renders
	 * 		to do something special graphically to draw attention to his location.
	 * 
	 * @param playtestMode
	 * 		{@code true} to enter playtesting, infinite lives mode, {@code false} if otherwise
	 * 
	 */
	public GameWorldLogic(final World world,
			  			  final GameEndCallback gameEndCallback,
						  final boolean playtestMode) {
		
		this.currentWorld = world;

		bonzo = new Bonzo(currentWorld,
			playtestMode ? Bonzo.INFINITE_LIVES : 4,
			new Runnable() { @Override public void run() { scoreUpdate(); } },
			new GameEndCallback() {
				@Override public void gameOverWin(World sw) { endGame_internal(); gameEndCallback.gameOverWin(world); }
				@Override public void gameOverFail(World w) { endGame_internal(); gameEndCallback.gameOverFail(world); }
				@Override public void gameOverEscape(World w) { endGame_internal(); gameEndCallback.gameOverEscape(world); }
			},
			bonzo -> {
				currentWorld.restartBonzo(bonzo);
				// this pointer escape, but function will not be called until gameplay proper
				activateGrace();
			});
		
		currentWorld.setAllRedKeysCollectedCallback( () -> redKeysCollected());
	}
	
	/** Returns the digits in the bonus countdown in a form that is easy to draw, where
	 *  each index has a digit 0-9 only. Bonus numbers are made of four digits always.
	 *  This returned array is the backing array and should not be modified by the caller.
	 *  It is intended for rendering logic only.
	 */
	public int[] getBonusDigits() {
		return bonusDigits;
	}
	
	public boolean isBonusTickingDown()
		{ return bonusTimer != -1; }
	
	public void updateBonusTick() {
		if (bonusTimer != -1) {
			bonusTimer = (bonusTimer + 1) % GameConstants.BONUS_COUNTDOWN_DELAY;
			if (bonusTimer == 0)
			{
				boolean keepCounting = currentWorld.bonusCountdown();
				createDigits(bonusDigits, BONUS_NUM_DIGITS, currentWorld.getCurrentBonus() );
				
				if (!(keepCounting) )
					{ bonusTimer = -1; }
			}
		}
	}
	
	/** Called per tick of gameplay. Gameplay ticks must be clamped at proper speed; this class
	 *  has no concept of dealing with delta values.
	 *  Regardless of the state of the game world, this should always be called every update tick.
	 *  Grace period, pause, and other states are all handled properly here, and the proper items
	 *  will update the proper amount as long as this is consistently called every tick.
	 */
	public void update() {
		if (!paused && grace == -1) {
			currentWorld.update();
			bonzo.update();
		} else if (grace != -1) {
			updateGrace();
		}
		// otherwise it is just paused.
	}
	
	private void activateGrace() {
		grace = 0;
		bonzo.freeze(true);
		paused = false;
	}
	
	private void deactivateGrace() {
		grace = -1;
		bonzo.freeze(false);
	}
	
	
	private void updateGrace() {
		++grace;
		if (grace >= GameConstants.GRACE_PERIOD_FRAMES)
			{ deactivateGrace(); }
	}
	
	/** Pauses the game. Does nothing if the grace period is active. */
	public void pause(boolean p) { 
		if (grace == -1)
			{ paused = p; } 
	}
	
	/**
	 * Starts up background music
	 */
	public void startBgm() {
		this.currentWorld.getResource().getSoundManager().playMusic();
	}
	
	// Called from callback when bonzos score is updated in game. Sets digit values for
	// score redraw.
	private void scoreUpdate() {
		int rawScore = bonzo.getScore();
		createDigits(digits, SCORE_NUM_DIGITS, rawScore);
	}
	
	public World getWorld() { return currentWorld; }
	
	// Given a raw value that is the right size to fit each digit into an index of the
	// array, transforms it into an array of 0-9 integers for drawing algorithms.
	private static void createDigits(int[] digitArray, int numOfDigits, int rawValue) {
		// modulations are computed from biggest to smallest singificant digit.
		// We must store them in the opposite direction to properly handle digits.
		for (int i = numOfDigits - 1, modular = GameConstants.TEN_POWERS[i + 1], digitIndex = 0;
			 i >= 0;
			 --i, ++digitIndex) {
			// Note: We need to compute the divisor to normalise to a number between 0-9, which
			// will end up being the next modular anyway.
			int divisor = GameConstants.TEN_POWERS[i];
			
			// Small correction to arithmetic during the last digit. A modulus of 1 technically
			// is not divided again.
			// if (divisor == 0)  divisor = 1;
			
			digitArray[digitIndex] = (rawValue % modular) / divisor;
			// readies the next digit extraction for next loop.
			modular = divisor;
		}
	}

	
	// Called when bonzo has collected all the red keys.
	private void redKeysCollected() { bonusTimer = 0; }
	
	// Common code for all types of game endings
	private void endGame_internal() {
		bonusTimer = -1;
		currentWorld.getResource().getSoundManager().stopPlayingMusic();
		currentWorld.worldFinished(bonzo);
	}
	
	/**bonusDigits
	 * Returns the resource data for the world that is currently running. Note that when using 
	 * {@code GameWorldLogic, it is in the context of the actual game and not the level editor,
	 * so the resource will always have Slick based graphics.cfc
	 */
	public WorldResource getResource() { return currentWorld.getResource(); }
	
	/**
	 * Returns a numerical representation of bonzos health, required
	 * to draw the health bar. 
	 */
	public int getBonzoHealth() { return bonzo.getHealth(); }
	
	/**
	 * Returns the array of digits, from most to least significant, that represent
	 * the current player score. The returned array IS the backing array and should not
	 * be modified by the caller.
	 */
	public int[] getScoreDigits() { return digits; }
	
	/**
	 * 
	 * Returns the number of lives bonzo currently has as a single digit, which
	 * always represents the right number of lives as bonzo may only have up to
	 * 9 lives.
	 * <p/>
	 * This may return {@code -2}, which indicates an infinite number of lives and
	 * should be given special handling.
	 * 
	 * @return
	 * 
	 */
	public int getLifeDigit() { return bonzo.getLives(); }
	
	/**
	 * Determines if a powerup is visible in the powerup-indicator on the UI. This does
	 * NOT say if bonzo HAS a powerup, only if it should be drawn or not. Powerups are
	 * still available and not drawn when they are 'fading'. This is for drawing purposes
	 * only.
	 * 
	 * @return
	 * 		{@code true} if a powerup should be drawn, {@code false} if otherwise
	 */
	public boolean isPowerupVisible() { return bonzo.powerupUIVisible(); }
	
	/**
	 * Returns the current powerup bonzo is holding. This should only be called if
	 * {@code isPowerupVisible} returns {@code true}.
	 */
	public Powerup getCurrentPowerup() { return bonzo.getCurrentPowerup(); }
	
	/**
	 * 
	 * Disposes of graphics and sounds for this running game. This must be called
	 * exactly ONCE before the last reference of this object is about to go out of scope.
	 * Failure to call will result in memory leaks.
	 * 
	 */
	public void dispose() { currentWorld.getResource().dispose(); }

	public Bonzo getBonzo() { return bonzo; }
}
