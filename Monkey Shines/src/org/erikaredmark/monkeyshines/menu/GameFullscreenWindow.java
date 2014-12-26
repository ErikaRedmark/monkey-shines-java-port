package org.erikaredmark.monkeyshines.menu;

import java.awt.DisplayMode;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.erikaredmark.monkeyshines.GameWorldLogic;
import org.erikaredmark.monkeyshines.KeyBindings;
import org.erikaredmark.monkeyshines.KeyboardInput;
import org.erikaredmark.monkeyshines.World;
import org.erikaredmark.monkeyshines.screendraw.StandardSurface;
import org.erikaredmark.monkeyshines.util.GameEndCallback;

/**
 * 
 * Represents the game running in full-screen mode.
 * <p/>
 * This class goes through three states: Constructed and ready to play, playing, and playing over. After a game
 * is finished using this object a new instance must be created to start another game.
 * 
 * @author Erika Redmark
 *
 */
public final class GameFullscreenWindow extends Frame {
	private static final String CLASS_NAME = "org.erikaredmark.monkeyshines.menu.GameFullscreenWindow";
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
	
	private static final long serialVersionUID = 1L;

	private final StandardSurface surface;
	private final GameEndCallback gameOverCallback;
	
	// Configuration information
	private final GraphicsDevice mainScreen;
	private final GameWorldLogic universe;
	private boolean gameOver;
	
	// State variables for drawing.
	private BufferStrategy buffer;

	/**
	 * 
	 * Creates the fullscreen window for the game, but critically does not actually make it visible and start the game
	 * until {@code start() } is called
	 * 
	 * @param keys
	 * 		keyboard input device to register
	 * 
	 * @param keyBindings
	 * 		a binding object that determines which keys on the keyboard map to which
	 * 		actions bonzo can take.
	 * 
	 * @param gameOver
	 * 		callback that is called when the game is over, and this object is done. Technically, this is called
	 * 		after this object has been disposed
	 * 
	 * @param world
	 * 		world to start a game for
	 * 
	 */
	public GameFullscreenWindow(final KeyboardInput keys, 
								final KeyBindings keyBindings, 
								final GameEndCallback gameOver,
								final World w) {
		
		gameOverCallback = gameOver;
		
		assert keys != null;
		
		addKeyListener(keys);
		
		GraphicsEnvironment env = 
			GraphicsEnvironment.getLocalGraphicsEnvironment();
		
		this.mainScreen = env.getDefaultScreenDevice();
		
		this.universe = 
			new GameWorldLogic(keys,
							   keyBindings,
							   w,
							   // Game over: set variable to stop loop
							   new GameEndCallback() { 
									@Override public void gameOverWin(World w) { 
										// Allow tally screen
										gameOverPreparations(false);
										gameOverCallback.gameOverWin(w);
									}
									
									@Override public void gameOverFail(World w) {
										gameOverPreparations(true);
										gameOverCallback.gameOverFail(w);
									}
									
									@Override public void gameOverEscape(World w) {
										gameOverPreparations(true);
										gameOverCallback.gameOverEscape(w);
									}
							   },
							   // Each game tick, rerender volatile
							   new Runnable() { 
								   @Override public void run() {
									   // Whilst surface isn't initialised yet, the timer hasn't
									   // started so this won't be called if surface is null.
									   renderScene();
								   } 
							   },
							   null); // TODO !
		
		this.surface = new StandardSurface(universe);
		
		setUndecorated(true);
		setIgnoreRepaint(true);
		
		// set size is fine on just the frame since no extra will be taken for the title bar
		// and window decoration.
		setSize(640, 480);
		setAlwaysOnTop(true);

	}

	/**
	 * 
	 * Starts the game. This will open a window and set fullscreen, then start the game. The main
	 * game loop will handle rendering. Fullscreen will be turned off via game events.
	 * <p/>
	 * This will return instantly if fullscreen is not supported. 
	 * 
	 * @return
	 * 		{@code true} if the game has started, {@code false} if the hardware did not support fullscreen
	 * 		and thus the game couldn't even start.
	 * 
	 */
	public boolean start() {
		
		if (!(mainScreen.isFullScreenSupported() ) ) {
			return false;
		}
		setVisible(true);
		createBufferStrategy(2);
		buffer = getBufferStrategy();

		if (!(mainScreen.isFullScreenSupported() ) ) {
			LOGGER.info(CLASS_NAME + ": Full screen not supported on this machine.");
		}
		
		mainScreen.setFullScreenWindow(this);
		//can we change the display mode? If not, we'll just take the performance hit
		if (mainScreen.isDisplayChangeSupported() ) {
			LOGGER.info(CLASS_NAME + ": Display change is supported: going to 640x480 resolution");
			DisplayMode[] modes = mainScreen.getDisplayModes();
			for (DisplayMode mode : modes) {
				// Find a 640x480 display mode for the standardSurface
				if (   mode.getBitDepth() >= 24
					&& mode.getWidth() == 640
					&& mode.getHeight() == 480) {
					
					mainScreen.setDisplayMode(mode);
//					displayChanged = true;
					break;
				}
				
				// Reaching here means we did not find a suitable resolution. This effectively means no
				// resolution change.
				LOGGER.info(CLASS_NAME + ": No suitable resolution found (was looking for 640x480 at a bit-depth of greater than or equal to 24 bits");
			}
		}
		
		// Rid of that annoying cursor. 
		Toolkit toolkit = Toolkit.getDefaultToolkit();
	    BufferedImage inviso = new BufferedImage(1, 1, BufferedImage.TRANSLUCENT); 
	    this.setCursor(toolkit.createCustomCursor(inviso, new Point(0, 0), "Inviso") );
	    // Cursor WILL be reset back to normal when fullscreen ends.
	    
	    // Artifical delay: It takes time for the monitor to go into fullscreen mode. 
	    // TODO some way of polling it until it does so instead of guessing?
	    try {
			Thread.sleep(4500);
		} catch (InterruptedException ex) {
			LOGGER.log(Level.WARNING,
					   "Delay thread interrupted: Game may start before full-screen mode is entered. Issue caused by: " + ex.getMessage(),
					   ex);
		}
	    
	    
		// Finally, start the music, and set a timer for starting the game
		universe.start(true);
		
		return true;
	}
	
	@Override public void paint(Graphics g) {
		System.err.println("Paint called on fullscreen");
	}

	// This is added to the 'game loop' via the callback per game tick. Each game tick is one
	// scene re-render. Note that this makes game updating, rendering, and sycning single threaded.
	// TODO volatile images just don't like rendering to BufferStrategy properly. Using bufferedImage
	// until a fix is found.
	private void renderScene() {
		// stop renderScene from being called during the game over tick
		if (gameOver)  return;
		
		assert surface != null;
		do {
			do {
				Graphics2D g = (Graphics2D) buffer.getDrawGraphics();
				try {
					surface.renderDirect(g, !(universe.showingSplash() ) );
				} finally {
					g.dispose();
				}
			} while (buffer.contentsRestored() );
			
			buffer.show();
			
		} while (buffer.contentsLost() );
	}
	
	/**
	 * 
	 * This method basically disposes the fullscreen window, setting the device back to windowed mode and
	 * restoring all settings. At this point the object is effectively 'dead' and cannot be reused.
	 * <p/>
	 * If {@code endInstant} is {@code false}, this will display the score tally first.
	 * <p/>
	 * This does not call the gameOverCallback. It simply decides whether to show the tally screen first (which
	 * is shown before any callbacks are used)
	 * 
	 */
	private void gameOverPreparations(boolean endInstant) {
		gameOver = true;
		
		if (!(endInstant) ) {
			EndGameBonusAnimation.runOnVolatile(buffer, 
												universe.getWorld() );
		}
		
		mainScreen.setFullScreenWindow(null);
		
		// Display seems to automatically fix itself when fullscreen is ended
//		if (displayChanged) {
//			mainScreen.setDisplayMode(oldDisplayMode);
//		}
		
		universe.dispose();
		setVisible(false);
		dispose();
	}
	
}
