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

import org.erikaredmark.monkeyshines.GameWorldLogic;
import org.erikaredmark.monkeyshines.KeyBindings;
import org.erikaredmark.monkeyshines.KeyboardInput;
import org.erikaredmark.monkeyshines.World;
import org.erikaredmark.monkeyshines.screendraw.StandardSurface;

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
	private static final long serialVersionUID = 1L;

	private final StandardSurface surface;
	private final Runnable gameOverCallback;
	
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
								final Runnable gameOver,
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
							   new Runnable() { @Override public void run() { 
								   gameOver();
							   } },
							   // Each game tick, rerender volatile
							   new Runnable() { 
								   @Override public void run() {
									   // Whilst surface isn't initialised yet, the timer hasn't
									   // started so this won't be called if surface is null.
									   renderScene();
								   } 
							   });
		
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


//		oldDisplayMode = mainScreen.getDisplayMode();
//		displayChanged = false;
		
		mainScreen.setFullScreenWindow(this);
		//can we change the display mode? If not, we'll just take the performance hit
		if (mainScreen.isDisplayChangeSupported() ) {
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
			}
		}
		
		// Rid of that annoying cursor. 
		Toolkit toolkit = Toolkit.getDefaultToolkit();
	    BufferedImage inviso = new BufferedImage(1, 1, BufferedImage.TRANSLUCENT); 
	    this.setCursor(toolkit.createCustomCursor(inviso, new Point(0, 0), "Inviso") );
	    // Cursor WILL be reset back to normal when fullscreen ends.
	    
		// Finally, start the game. The render loop is called
		// once per game tick via the callback during this object's setup.
		universe.start();

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
					// Is the world still available for drawing? If not, rerender
					
					// Initial validation
					surface.renderDirect(g);
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
	 * 
	 */
	private void gameOver() {
		gameOver = true;
		mainScreen.setFullScreenWindow(null);
		
		// Display seems to automatically fix itself when fullscreen is ended
//		if (displayChanged) {
//			mainScreen.setDisplayMode(oldDisplayMode);
//		}
		
		universe.dispose();
		setVisible(false);
		dispose();
		gameOverCallback.run();
	}
	
}
