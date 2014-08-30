package org.erikaredmark.monkeyshines.menu;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.VolatileImage;

import javax.swing.JPanel;

import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.GameWorldLogic;
import org.erikaredmark.monkeyshines.KeyBindings;
import org.erikaredmark.monkeyshines.KeyboardInput;
import org.erikaredmark.monkeyshines.World;
import org.erikaredmark.monkeyshines.screendraw.StandardSurface;
import org.erikaredmark.monkeyshines.util.GameEndCallback;

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
public class GamePanel extends JPanel {
	private static final long serialVersionUID = -1418470684111076474L;
	// The actual surface that will provide drawing to this component
	
	private final GameWorldLogic universe;
	private final StandardSurface surface;

	private GamePanel(final KeyboardInput keys, 
					  final KeyBindings keyBindings,
					  final GameEndCallback endGame, 
					  final World world) {
		super();
		this.addKeyListener(keys);
		
		// Wrap the endGame callback in another callback. We intercept for drawing the end screens
		// if needed.
		final GameEndCallback endGameWrapped = new GameEndCallback() {
			@Override public void gameOverWin() {
				Graphics2D g2d = (Graphics2D) getGraphics();
				EndGameBonusAnimation.runOn(
					g2d, 
					universe.getWorld(),
					new Runnable() { @Override public void run() { repaint(); } });
				endGame.gameOverWin();
			}
			
			@Override public void gameOverFail() {
				endGame.gameOverFail();
			}
			
			@Override public void gameOverEscape() {
				endGame.gameOverEscape();
			}
		};
		
		this.universe = 
			new GameWorldLogic(
				keys,
				keyBindings,
				world,
				endGameWrapped,
				new Runnable() { @Override public void run() { repaint(); } } );
		
		this.surface = new StandardSurface(universe);
		
		setDoubleBuffered(true);
		// Accommodate the UI and the game. UI is a banner and width is equal to screen width
		setMinimumSize(new Dimension(GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT + GameConstants.UI_HEIGHT) );
		setPreferredSize(new Dimension(GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT + GameConstants.UI_HEIGHT) );
		
		// Both UI and game will share the same panel
		setLayout(null);
		JPanel gameplayPanel = pickBestPanel();
		gameplayPanel.setSize(GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT + GameConstants.UI_HEIGHT);
		gameplayPanel.setBounds(0, 0, GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT + GameConstants.UI_HEIGHT);

		add(gameplayPanel);
	
	}
	
	
	/**
	 * 
	 * Constructs a GamePanel listening to the keyboard. When the game is over, the callback is called. 
	 * <p/>
	 * Note that it takes a few seconds after this method returns for the game to start proper, as it will
	 * start by going through the splash screen.
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
	public static GamePanel newGamePanel(final KeyboardInput keys, 
										 final KeyBindings keyBindings,
										 final GameEndCallback endGame, 
										 final World world) {
		
		final GamePanel panel = new GamePanel(keys, keyBindings, endGame, world);
		
		panel.setVisible(true);
		panel.universe.start(true);

		return panel;
	}
	
	/**
	 * 
	 * internal method that examines the graphical hardware available for the user's configuration,
	 * and if it supports hardware acceleration, constructs the appropriate graphics configuration
	 * and returns a panel that will draw the main game screen in the best possible way.
	 * 
	 * @return
	 * 		best pick game panel for the current system to use
	 * 
	 */
	private JPanel pickBestPanel() {
		GraphicsEnvironment env = 
			GraphicsEnvironment.getLocalGraphicsEnvironment();
		
		GraphicsDevice mainScreen = env.getDefaultScreenDevice();

		// TODO actually determine which one is better?
		GraphicsConfiguration primary = mainScreen.getDefaultConfiguration();
		return new VolatileUIPanel(primary, universe);
	}
	
	// UI Panel for displaying buffered images. May not be as fast as volatile
	// but used when required hardware is unavailable.
	// TODO Currently unused. Need to test if VolatileImage works on all majour systems
//	private final class BufferedUIPanel extends JPanel {
//		private static final long serialVersionUID = 1L;
//
//		@Override public void paint(Graphics g) {
//			final BufferedImage page = surface.renderBuffered();
//			g.drawImage(page, 0, 0, null);
//		}
//	}
	
	// UI panel for displaying volatile images. Preferred method if hardware is
	// available.
	private final class VolatileUIPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		
		private final GraphicsConfiguration gc;
		private final GameWorldLogic universe;
		
		// The panel will start the game after it is done rendering the splash screen.
		public VolatileUIPanel(final GraphicsConfiguration gc, final GameWorldLogic universe) {
			this.gc = gc;
			this.universe = universe;
		}
		
		@Override public void paint(Graphics g) {
			VolatileImage page = null;
			do {
				page = surface.renderVolatile(gc, !(universe.showingSplash() ) );
			} while (page.contentsLost() );
			// TODO should drawImage be IN the loop or OUTSIDE of the loop?
			g.drawImage(page, 0, 0, null);
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
