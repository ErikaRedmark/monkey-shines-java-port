package org.erikaredmark.monkeyshines.menu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.erikaredmark.monkeyshines.Bonzo;
import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.KeyboardInput;
import org.erikaredmark.monkeyshines.Powerup;
import org.erikaredmark.monkeyshines.World;
import org.erikaredmark.monkeyshines.resource.WorldResource;

/**
 * Initialises a JPanel that holds the game window. This window is where all the action and
 * sprites will be drawn to. 
 * @author Erika Redmark
 *
 */
public class GameWindow extends JPanel {
	private static final long serialVersionUID = -1418470684111076474L;

	private final Timer gameTimer;
	
	// Started when the last key is collected, stopped when the 'bonus score' hits zero.
	private final Timer bonusTimer;

	// Main drawing happens on gameplay. The world itself is the 'model' to this view.
	private final GameplayPanel gameplayCanvas;
	private Bonzo bonzo;
	private World currentWorld;
	
	// UI Canvas stores other stats. Through a basic callback, World communicates
	// back to this class for UI updates whenever one of the UI dependent stats
	// changes.
	private final UIPanel uiCanvas;

	
	// UI Panel constants: declared here due to initialision issues with non-const expressions
	// in inner classes.
	
	// Drawing location to start drawing the health bar.
	private static final int HEALTH_DRAW_X = 241;
	private static final int HEALTH_DRAW_Y = 50;
	private static final int HEALTH_DRAW_WIDTH = 151;
	private static final int HEALTH_DRAW_HEIGHT = 14;
	// Used to map the 'logical' health to the 'width' of the health bar.
	// Bonzos health will be converted to double and extended/contracted by this multplier to get draw width.
	private static final double HEALTH_MULTIPLIER = (double)HEALTH_DRAW_WIDTH / (double)GameConstants.HEALTH_MAX;
	
	// Color of health bar
	private static final Color HEALTH_COLOR = new Color(0, 255, 0, 255);
	
	// Score draw x/y is the top left location of the FIRST, leftmost digit.
	private static final int SCORE_DRAW_X = 13;
	private static final int SCORE_DRAW_Y = 32;
	private static final int SCORE_WIDTH = 16;
	private static final int SCORE_HEIGHT = 30;
	// Precomputation of effectively a constant
	private static final int SCORE_DRAW_Y2 = SCORE_DRAW_Y + SCORE_HEIGHT;
	
	private static final int SCORE_NUM_DIGITS = 7;
	// Digits are updated when score is updated. Digits are always drawn from this
	// array to avoid digit extraction algorithms every frame
	// Default value 0 is guaranteed by language. Bonzo's score always starts at 0
	private final int digits[] = new int[SCORE_NUM_DIGITS];
	
	// A single digit representing bonzos lives. He may have up to 9 lives
	// only. A value of Bonzo.INFINITE_LIVES draws nothing.
	private int lifeDigit;
	
	private static final int LIFE_DRAW_X = 595;
	private static final int LIFE_DRAW_Y = 33;
	// Width and height are same as score width/height, as numerals are same
	// size.
	private static final int LIFE_DRAW_X2 = LIFE_DRAW_X + SCORE_WIDTH;
	private static final int LIFE_DRAW_Y2 = LIFE_DRAW_Y + SCORE_HEIGHT;

	
	// Bonus digits
	private static int BONUS_NUM_DIGITS = 4;
	private static int BONUS_DRAW_X = 152;
	// Bonus draw Y is same as score; same y level
	// widths and height same as score
	
	// Each new game starts with 10000 countdown, represented by 9999
	private int countdownDigits[] = new int[] {9, 9, 9, 9};
	
	// POWERUPS
	private static final int POWERUP_DRAW_X = 418;
	private static final int POWERUP_DRAW_Y = 37;
	private static final int POWERUP_DRAW_X2 = POWERUP_DRAW_X + GameConstants.GOODIE_SIZE_X;
	private static final int POWERUP_DRAW_Y2 = POWERUP_DRAW_Y + GameConstants.GOODIE_SIZE_Y;
	
	private final Runnable endGameCallback;
	
	/**
	 * Constructs a GameWindow listening to the keyboard. The window will bring up a dialog asking the user to choose a level
	 * and then will start the game.
	 * TODO remove 'choose a level' responsibility and move it to main menu functions.
	 * 
	 * When the game is over, the callback is called. 
	 * 
	 * @param keys
	 * 		keyboard input device to register
	 * 
	 * @param endGame
	 * 		callback for when the game is over
	 * 
	 * @param world
	 * 		world to start a game for
	 * 
	 */
	public GameWindow(final KeyboardInput keys, final Runnable endGame, final World world) {
		super();
		this.endGameCallback = endGame;
		this.addKeyListener(keys);
		this.currentWorld = world;
		// DEBUG: Will eventually be based on difficulty
		this.lifeDigit = 4;
		
		bonzo = new Bonzo(currentWorld,
			// DEBUG: Will eventually be based on difficulty
			4,
			new Runnable() { @Override public void run() { scoreUpdate(); } },
			new Runnable() { @Override public void run() { gameOver(); } },
			new Runnable() { @Override public void run() { updateLives(); } },
			new Runnable() { @Override public void run() { levelComplete(); } });
		
		
		currentWorld.setAllRedKeysCollectedCallback(
			new Runnable() { 
				@Override public void run() { 
					redKeysCollected(); 
				} 
			});
		
		
		setDoubleBuffered(true);
		// Accommodate the UI and the game. UI is a banner and width is equal to screen width
		setMinimumSize(new Dimension(GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT + GameConstants.UI_HEIGHT) );
		setPreferredSize(new Dimension(GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT + GameConstants.UI_HEIGHT) );
		
		// Place UI and main game screen as canvases with dedicated paint methods
		setLayout(null);
		gameplayCanvas = new GameplayPanel();
		uiCanvas = new UIPanel();
		
		uiCanvas.setBounds(0, 0, GameConstants.SCREEN_WIDTH, GameConstants.UI_HEIGHT);
		gameplayCanvas.setBounds(0, GameConstants.UI_HEIGHT, GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);

		add(uiCanvas);
		add(gameplayCanvas);
		
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
				if (keys.keyDown(KeyEvent.VK_LEFT) ) {
					bonzo.move(-1);
					//System.out.println("left");
				}
				if (keys.keyDown(KeyEvent.VK_RIGHT) ) {
					bonzo.move(1);
				}
				if (keys.keyDown(KeyEvent.VK_UP) ) {
					bonzo.jump(4);
				}
				
				repaint();
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
		
		setVisible(true);
		
		gameTimer.start();
	}
	

	// Called from callback when bonzos score is updated in game. Sets digit values for
	// score redraw.
	private void scoreUpdate() {
		int rawScore = bonzo.getScore();
		createDigits(digits, SCORE_NUM_DIGITS, rawScore);
	}
	
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
		// TODO soft fade out and return to main menu. Right now we just return to
		// choosing a world.
		
		endGameCallback.run();
		
		// for now, we just give bonzo more lives
		// +1 against the -1 goes to 0, resulting in 4
		//bonzo.incrementLives(5);
		//updateLives();
		//currentWorld.restartBonzo(bonzo);
		//System.out.println("This would normally be a game over.");
	}
	
	// Called when bonzos life is lost. Sets life digit for drawing.
	private void updateLives() {
		int lives = bonzo.getLives();
		assert lives < 10 && lives >= -2;
		// Gameover is called when lives is -1. Logically this should never
		// be called when Bonzo is in that state
		assert lives != -1;
		
		lifeDigit = lives;
	}
	
	// Called when bonzo has collected all the red keys.
	private void redKeysCollected() {
		// Start the countdown timer
		bonusTimer.start();
	}
	
	// Called when bonzo collides with the exit door.
	private void levelComplete() {
		bonusTimer.stop();
		System.out.println("If this was the final game you would have just finished the level. Congratulations!");
		// TODO differentiate between ending game BAD vs ending game GOOD
		endGameCallback.run();
	}
			
	private final class GameplayPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		@Override public void paint(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			// Clear the screen with the world
			currentWorld.paintAndUpdate(g2d);
			bonzo.update();
			bonzo.paint(g2d);
		}
	}
	
	
	private final class UIPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		@Override public void paint(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			/* --------------------- Initial Banner ---------------------- */
			WorldResource rsrc = currentWorld.getResource();
			g2d.drawImage(rsrc.getBanner(), 
						  0, 0,
						  GameConstants.SCREEN_WIDTH, GameConstants.UI_HEIGHT,
						  0, 0,
						  GameConstants.SCREEN_WIDTH, GameConstants.UI_HEIGHT,
						  null);
			
			/* ------------------------- Health -------------------------- */
			g2d.setColor(HEALTH_COLOR);
			// Normalise bonzo's current health with drawing.
			double healthWidth = ((double)bonzo.getHealth()) * HEALTH_MULTIPLIER;
			//System.out.println("Drawing rect: " + HEALTH_DRAW_X + " " + HEALTH_DRAW_Y + " " + (int)healthWidth + " " + HEALTH_DRAW_HEIGHT);
			g2d.fillRect(HEALTH_DRAW_X, HEALTH_DRAW_Y, (int)healthWidth, HEALTH_DRAW_HEIGHT);
			
			/* -------------------------- Score -------------------------- */
			for (int i = 0; i < SCORE_NUM_DIGITS; i++) {
				int drawToX = SCORE_DRAW_X + (SCORE_WIDTH * i);
				// draw to Y is always the same
				int drawFromX = SCORE_WIDTH * digits[i];
				// draw from Y is always the same, 0
				g2d.drawImage(rsrc.getScoreNumbersSheet(), 
							  drawToX, SCORE_DRAW_Y,
							  drawToX + SCORE_WIDTH, SCORE_DRAW_Y2, 
							  drawFromX, 0, 
							  drawFromX + SCORE_WIDTH, SCORE_HEIGHT, 
							  null);
			}
			
			/* -------------------- Bonus Countdown ---------------------- */
			for (int i = 0; i < BONUS_NUM_DIGITS; i++) {
				int drawToX = BONUS_DRAW_X + (SCORE_WIDTH * i);
				// draw to Y is always the same
				int drawFromX = SCORE_WIDTH * countdownDigits[i];
				// draw from Y is always the same, 0
				g2d.drawImage(rsrc.getBonusNumbersSheet(),
							  drawToX, SCORE_DRAW_Y,
							  drawToX + SCORE_WIDTH, SCORE_DRAW_Y2,
							  drawFromX, 0,
							  drawFromX + SCORE_WIDTH, SCORE_HEIGHT,
							  null);
			}
			
			/* ------------------------- Lives --------------------------- */
			{
				if (lifeDigit >= 0) {
					assert lifeDigit < 10;
					int drawFromX = SCORE_WIDTH * lifeDigit;
					
					g2d.drawImage(rsrc.getScoreNumbersSheet(),
								  LIFE_DRAW_X, LIFE_DRAW_Y,
								  LIFE_DRAW_X2, LIFE_DRAW_Y2,
								  drawFromX, 0,
								  drawFromX + SCORE_WIDTH, SCORE_HEIGHT,
								  null);
				} // else draw nothing TODO perhaps draw infinity symbol?
			}
			
			/* ------------------------ Powerup --------------------------- */
			{
				if (bonzo.powerupUIVisible() ) {
					Powerup powerup = bonzo.getCurrentPowerup();
					assert powerup != null : "Powerup should be invisible if null";
					
					g2d.drawImage(rsrc.getGoodieSheet(),
							      POWERUP_DRAW_X, POWERUP_DRAW_Y,
							      POWERUP_DRAW_X2, POWERUP_DRAW_Y2,
							      powerup.drawFromX(), Powerup.POWERUP_DRAW_FROM_Y,
							      powerup.drawFromX2(), Powerup.POWERUP_DRAW_FROM_Y2,
							      null);
				}
			}
		}
	}

}
