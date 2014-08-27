package org.erikaredmark.monkeyshines;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Timer;

import org.erikaredmark.monkeyshines.resource.WorldResource;

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
 * <p/>
 * Practically speaking, this allows both a windowed mode and a fullscreen mode.
 * 
 * @author Erika Redmark
 *
 */
public final class GameWorldLogic {
	
	// The primary flow of time
	private final Timer gameTimer;
	
	// Started when the last key is collected, stopped when the 'bonus score' hits zero.
	private final Timer bonusTimer;
	
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
	
	// Bonus digits
	public static int BONUS_NUM_DIGITS = 4;
	// Each new game starts with 10000 countdown, represented by 9999
	private int countdownDigits[] = new int[] {9, 9, 9, 9};
	
	private final Runnable endGameCallback;
	
	/**
	 * 
	 * Constructs the living game world. Gigantic constructor but admittedly it
	 * is the parent object for running the world. Most parameters are callbacks
	 * to the UI for critical actions.
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
	 * @param gameTickCallback
	 * 		a callback for each tick of the game. This indicates that whatever buffer is holding
	 * 		the 'image' of the game will have to be updated because it no longer reflects the
	 * 		game state
	 * 
	 */
	public GameWorldLogic(final KeyboardInput keys, 
			  			  final KeyBindings keyBindings,
						  final World world,
			  			  final Runnable endGameCallback,
						  final Runnable gameTickCallback) {
		assert keys != null;
		
		this.endGameCallback = endGameCallback;
		this.currentWorld = world;

		bonzo = new Bonzo(currentWorld,
			// DEBUG: Will eventually be based on difficulty
			4,
			new Runnable() { @Override public void run() { scoreUpdate(); } },
			new Runnable() { @Override public void run() { gameOver(); } },
			new Runnable() { @Override public void run() { levelComplete(); } });
		
		currentWorld.setAllRedKeysCollectedCallback(
			new Runnable() { 
				@Override public void run() { 
					redKeysCollected(); 
				} 
			});
		
		gameTimer = new Timer(GameConstants.GAME_SPEED, new ActionListener() {
			/**
			 * Polls the keyboard for valid operations the player may make on Bonzo. During gameplay, 
			 * the only allowed operations are moving left/right or jumping. 
			 * This is the method called every tick to run the game logic. This is effectively the
			 * 'entry point' to the main game loop.
			 */
			public void actionPerformed(ActionEvent e) {
				// Poll Keyboard
				keys.poll();
				if (keys.keyDown(keyBindings.left) ) {
					bonzo.move(-1);
				}
				if (keys.keyDown(keyBindings.right) ) {
					bonzo.move(1);
				}
				if (keys.keyDown(keyBindings.jump) ) {
					bonzo.jump(4);
				}
				
				// The only hardcoded key: Esc is a game over
				if (keys.keyDown(KeyEvent.VK_ESCAPE) ) {
					gameOver();
				}
				
				// Update the game first before calling what is possibly a paint
				// routine.
				currentWorld.update();
				bonzo.update();
				gameTickCallback.run();
			}
		});
		
		bonusTimer = new Timer(GameConstants.BONUS_COUNTDOWN_DELAY, new ActionListener() {
			/**
			 * Counts down the bonus score for this game session. Stops itself at zero. This will
			 * also be stopped when bonzo reaches the exit door or dies.
			 */
			public void actionPerformed(ActionEvent e) {
				// Note: Bonus starts at 10000. We only DISPLAY 9999 and because the
				// update function never runs until the first update and painting, it will end
				// up redrawing the value 9990.
				boolean keepCounting = currentWorld.bonusCountdown();
				createDigits(countdownDigits, BONUS_NUM_DIGITS, currentWorld.getCurrentBonus() );
				
				if (!(keepCounting) )  bonusTimer.stop();
			}
		});
	}
	
	/**
	 * 
	 * Paints the world to the given graphics context.
	 * TODO this is temporary. Eventually I want to segregate this even further so elements
	 * aren't responsible for painting themselves, making it possible to support hi-def graphics
	 * or any other interesting transformations.
	 * 
	 * @param g
	 * 
	 */
	public void paintTo(Graphics2D g) {
		currentWorld.paint(g);
		bonzo.paint(g);
	}
	
	/**
	 * 
	 * Starts the music for this level on a separate thread. This is normally intended to start the music before showing the splash
	 * screen. This does NOT start the game timer.
	 * 
	 */
	public void startMusic() {
		this.currentWorld.getResource().getSoundManager().playMusic();
	}
	
	/**
	 * 
	 * Starts time. Does nothing if time has already started.
	 * Both the running music and the timer will operate on a different thread than what called this method.
	 * 
	 */
	public void start() {
		gameTimer.start();
		this.currentWorld.getResource().getSoundManager().playMusic();
	}
	
	/**
	 * 
	 * Freezes time. Game will not respond to user events and will not update. Does
	 * nothing if time has already been frozen.
	 * 
	 */
	public void freeze() {
		gameTimer.stop();
		this.currentWorld.getResource().getSoundManager().stopPlayingMusic();
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
	
	// Called during a game over.
	private void gameOver() {
		bonusTimer.stop();
		this.currentWorld.getResource().getSoundManager().stopPlayingMusic();
		
		// End world to update statistics:
		this.currentWorld.worldFinished(this.bonzo);
		
		endGameCallback.run();
	}
	
	// Called when bonzo has collected all the red keys.
	private void redKeysCollected() {
		// Start the countdown timer
		bonusTimer.start();
	}
	
	// Called when bonzo collides with the exit door.
	private void levelComplete() {
		bonusTimer.stop();
		currentWorld.getResource().getSoundManager().stopPlayingMusic();
		currentWorld.worldFinished(bonzo);
		// TODO differentiate between ending game BAD vs ending game GOOD
		endGameCallback.run();
	}
	
	/**
	 * 
	 * Returns the resource data for the world that is currently running.
	 * 
	 * @return
	 * 
	 */
	public WorldResource getResource() {
		return currentWorld.getResource();
	}
	
	/**
	 * 
	 * Returns a numerical representation of bonzos health, required
	 * to draw the health bar. 
	 * 
	 * @return
	 * 
	 */
	public int getBonzoHealth() {
		return bonzo.getHealth();
	}
	
	/**
	 * 
	 * Returns the array of digits, from most to least significant, that represent
	 * the current player score. The returned array IS the backing array and should not
	 * be modified by the caller.
	 * 
	 * @return
	 * 
	 */
	public int[] getScoreDigits() {
		return digits;
	}
	
	/**
	 * 
	 * Returns the array of digits, from most to least significant, that represent
	 * the countdown for the bonus, which is started when the last red key is collected.
	 * The returned array IS the backing array and should not be modified by the caller.
	 * 
	 * @return
	 * 
	 */
	public int[] getBonusDigits() {
		return countdownDigits;
	}
	
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
	public int getLifeDigit() {
		return bonzo.getLives();
	}
	
	/**
	 * 
	 * Determines if a powerup is visible in the powerup-indicator on the UI. This does
	 * NOT say if bonzo HAS a powerup, only if it should be drawn or not. Powerups are
	 * still available and not drawn when they are 'fading'. This is for drawing purposes
	 * only.
	 * 
	 * @return
	 * 		{@code true} if a powerup should be drawn, {@code false} if otherwise
	 * 
	 */
	public boolean isPowerupVisible() {
		return bonzo.powerupUIVisible();
	}
	
	/**
	 * 
	 * Returns the current powerup bonzo is holding. This should only be called if
	 * {@code isPowerupVisible} returns {@code true}.
	 * 
	 * @return
	 * 
	 */
	public Powerup getCurrentPowerup() {
		return bonzo.getCurrentPowerup();
	}
	
	/**
	 * 
	 * Disposes of graphics and sounds for this running game. This must be called
	 * exactly ONCE before the last reference of this object is about to go out of scope.
	 * Failure to call will result in memory leaks.
	 * 
	 */
	public void dispose() {
		currentWorld.getResource().dispose();
		gameTimer.stop();
		bonusTimer.stop();
	}
}
