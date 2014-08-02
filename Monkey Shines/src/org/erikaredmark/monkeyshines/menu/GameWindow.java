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
	// The type of graphics hardware available determines whether this will be a basic
	// buffered canvas or one that draws using hardware-accelerated images.
	private final JPanel uiCanvas;
	
	// The actual surface that will provide drawing to this component
	
	private final GameWorldLogic universe;
	private final StandardSurface surface;
	
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
		
		this.surface = new StandardSurface(universe);
		
		setDoubleBuffered(true);
		// Accommodate the UI and the game. UI is a banner and width is equal to screen width
		setMinimumSize(new Dimension(GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT + GameConstants.UI_HEIGHT) );
		setPreferredSize(new Dimension(GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT + GameConstants.UI_HEIGHT) );
		
		// Place UI and main game screen as canvases with dedicated paint methods
		setLayout(null);
		gameplayCanvas = new GameplayPanel();
		uiCanvas = pickBestPanel();
		
		uiCanvas.setBounds(0, 0, GameConstants.SCREEN_WIDTH, GameConstants.UI_HEIGHT);
		gameplayCanvas.setBounds(0, GameConstants.UI_HEIGHT, GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);

		add(uiCanvas);
		add(gameplayCanvas);
	
		universe.start();
		
		setVisible(true);
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
		return new VolatileUIPanel(primary);
	}
			
	private final class GameplayPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		@Override public void paint(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			// Clear the screen with the world
			universe.paintTo(g2d);
		}
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
		
		public VolatileUIPanel(final GraphicsConfiguration gc) {
			this.gc = gc;
		}
		
		@Override public void paint(Graphics g) {
			VolatileImage page = null;
			do {
				page = surface.renderVolatile(gc);
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
