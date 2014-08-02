package org.erikaredmark.monkeyshines.menu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.GameWorldLogic;
import org.erikaredmark.monkeyshines.KeyBindings;
import org.erikaredmark.monkeyshines.KeyboardInput;
import org.erikaredmark.monkeyshines.Powerup;
import org.erikaredmark.monkeyshines.World;
import org.erikaredmark.monkeyshines.resource.WorldResource;

/**
 * 
 * Initialises a JPanel that holds the game window. This window is where all the action and
 * sprites will be drawn to.
 * <p/>
 * Only used in non-fullscreen mode
 * 
 * @author Erika Redmark
 *
 */
public class GameWindow extends JPanel {
	private static final long serialVersionUID = -1418470684111076474L;

	// Main drawing happens on gameplay. The world itself is the 'model' to this view.
	private final GameplayPanel gameplayCanvas;
	
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

	
	private static final int LIFE_DRAW_X = 595;
	private static final int LIFE_DRAW_Y = 33;
	// Width and height are same as score width/height, as numerals are same
	// size.
	private static final int LIFE_DRAW_X2 = LIFE_DRAW_X + SCORE_WIDTH;
	private static final int LIFE_DRAW_Y2 = LIFE_DRAW_Y + SCORE_HEIGHT;

	

	private static int BONUS_DRAW_X = 152;
	// Bonus draw Y is same as score; same y level
	// widths and height same as score

	
	// POWERUPS
	private static final int POWERUP_DRAW_X = 418;
	private static final int POWERUP_DRAW_Y = 37;
	private static final int POWERUP_DRAW_X2 = POWERUP_DRAW_X + GameConstants.GOODIE_SIZE_X;
	private static final int POWERUP_DRAW_Y2 = POWERUP_DRAW_Y + GameConstants.GOODIE_SIZE_Y;

	// The actual game world that is run in this window.
	private final GameWorldLogic universe;
	
	/**
	 * Constructs a GameWindow listening to the keyboard. The window will bring up a dialog asking the user to choose a level
	 * and then will start the game.
	 * 
	 * When the game is over, the callback is called. 
	 * 
	 * @param keys
	 * 		keyboard input device to register
	 * 
	 * @param keyBindings
	 * 		a binding object that determines which keys on the keyboard map to which
	 * 		actions bonzo can take.
	 * 
	 * @param endGame
	 * 		callback for when the game is over
	 * 
	 * @param world
	 * 		world to start a game for
	 * 
	 */
	public GameWindow(final KeyboardInput keys, 
					  final KeyBindings keyBindings,
					  final Runnable endGame, 
					  final World world) {
		super();
		this.addKeyListener(keys);
		this.universe = 
			new GameWorldLogic(
				keys,
				keyBindings,
				world,
				endGame,
				new Runnable() { @Override public void run() { repaint(); } } );
		
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
	
		universe.start();
		
		setVisible(true);
	}
			
	private final class GameplayPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		@Override public void paint(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			// Clear the screen with the world
			universe.paintTo(g2d);
		}
	}
	
	private final class UIPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		@Override public void paint(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			/* --------------------- Initial Banner ---------------------- */
			WorldResource rsrc = universe.getResource();
			g2d.drawImage(rsrc.getBanner(), 
						  0, 0,
						  GameConstants.SCREEN_WIDTH, GameConstants.UI_HEIGHT,
						  0, 0,
						  GameConstants.SCREEN_WIDTH, GameConstants.UI_HEIGHT,
						  null);
			
			/* ------------------------- Health -------------------------- */
			g2d.setColor(HEALTH_COLOR);
			// Normalise bonzo's current health with drawing.
			double healthWidth = ((double)universe.getBonzoHealth()) * HEALTH_MULTIPLIER;
			//System.out.println("Drawing rect: " + HEALTH_DRAW_X + " " + HEALTH_DRAW_Y + " " + (int)healthWidth + " " + HEALTH_DRAW_HEIGHT);
			g2d.fillRect(HEALTH_DRAW_X, HEALTH_DRAW_Y, (int)healthWidth, HEALTH_DRAW_HEIGHT);
			
			/* -------------------------- Score -------------------------- */
			for (int i = 0; i < GameWorldLogic.SCORE_NUM_DIGITS; i++) {
				int drawToX = SCORE_DRAW_X + (SCORE_WIDTH * i);
				// draw to Y is always the same
				int drawFromX = SCORE_WIDTH * universe.getScoreDigits()[i];
				// draw from Y is always the same, 0
				g2d.drawImage(rsrc.getScoreNumbersSheet(), 
							  drawToX, SCORE_DRAW_Y,
							  drawToX + SCORE_WIDTH, SCORE_DRAW_Y2, 
							  drawFromX, 0, 
							  drawFromX + SCORE_WIDTH, SCORE_HEIGHT, 
							  null);
			}
			
			/* -------------------- Bonus Countdown ---------------------- */
			for (int i = 0; i < GameWorldLogic.BONUS_NUM_DIGITS; i++) {
				int drawToX = BONUS_DRAW_X + (SCORE_WIDTH * i);
				// draw to Y is always the same
				int drawFromX = SCORE_WIDTH * universe.getBonusDigits()[i];
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
				int lifeDigit = universe.getLifeDigit();
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
				if (universe.isPowerupVisible() ) {
					Powerup powerup = universe.getCurrentPowerup();
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
	
	/**
	 * 
	 * Disposes of graphics and sound resources for the running game. Intended to be called during a primary
	 * game state transition when leaving the game.
	 * 
	 */
	public void dispose() {
		universe.dispose();
	}

}
