package org.erikaredmark.monkeyshines;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Path;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.erikaredmark.monkeyshines.encoder.EncodedWorld;
import org.erikaredmark.monkeyshines.encoder.WorldIO;
import org.erikaredmark.monkeyshines.encoder.exception.WorldRestoreException;
import org.erikaredmark.monkeyshines.graphics.exception.ResourcePackException;
import org.erikaredmark.monkeyshines.resource.WorldResource;

/**
 * Initialises a JPanel that holds the game window. This window is where all the action and
 * sprites will be drawn to. 
 * @author Erika Redmark
 *
 */
public class GameWindow extends JPanel implements ActionListener {
	private static final long serialVersionUID = -1418470684111076474L;

	private final Timer gameTimer;
	
	private final KeyboardInput keys;
	
	private final Bonzo bonzo;
	
	// Main drawing happens on gameplay. The world itself is the 'model' to this view.
	private final GameplayPanel gameplayCanvas;
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
	
	/**
	 * Constructs a GameWindow listening to the keyboard
	 * @param keys
	 */
	public GameWindow(final KeyboardInput keys) {
		super();
		this.keys = keys;
		
		// TODO DEBUG jump straight to file browser to select a world. Obviously a better menu
		// system should be in place 
		JFileChooser fileChooser = new JFileChooser();
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			Path worldFile = fileChooser.getSelectedFile().toPath();
			
			try {
				EncodedWorld world = WorldIO.restoreWorld(worldFile);
				// Try to load the resource pack
				String fileName = worldFile.getFileName().toString();
				// Remove .world extension so we can substitute with .zip.
				String worldName = fileName.substring(0, fileName.lastIndexOf('.') );
				Path packFile = worldFile.getParent().resolve(worldName + ".zip");
				WorldResource rsrc = WorldResource.fromPack(packFile);
				currentWorld = world.newWorldInstance(rsrc);
			} catch (WorldRestoreException ex) {
				JOptionPane.showMessageDialog(this,
				    "Cannot load world: Possibly corrupt or not a world file: " + ex.getMessage(),
				    "Loading Error",
				    JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
			} catch (ResourcePackException ex) {
				JOptionPane.showMessageDialog(this,
				    "Resource pack issues: " + ex.getMessage(),
				    "Loading Error",
				    JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(this,
				    "Low level I/O error: " + ex.getMessage(),
				    "Loading Error",
				    JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
			}
		} else {
			// No world chosen, no graphics loaded, nothing to do. Quit
			System.exit(0);
		}
		
		this.addKeyListener(keys);
		
		bonzo = new Bonzo(currentWorld);
		setDoubleBuffered(true);
		// Accomodate the UI and the game. UI is a banner and width is equal to screen width
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
		
		gameTimer = new Timer(GameConstants.GAME_SPEED, this);
		
		setVisible(true);
		
		gameTimer.start();
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
			// Draw banner under everything else
			WorldResource rsrc = currentWorld.getResource();
			g2d.drawImage(rsrc.getBanner(), 
						  0, 0,
						  GameConstants.SCREEN_WIDTH, GameConstants.UI_HEIGHT,
						  0, 0,
						  GameConstants.SCREEN_WIDTH, GameConstants.UI_HEIGHT,
						  null);
			
			// Draw health. Currently draws a basic rectangle
			g2d.setColor(HEALTH_COLOR);
			// Normalise bonzo's current health with drawing.
			double healthWidth = ((double)bonzo.getHealth()) * HEALTH_MULTIPLIER;
			//System.out.println("Drawing rect: " + HEALTH_DRAW_X + " " + HEALTH_DRAW_Y + " " + (int)healthWidth + " " + HEALTH_DRAW_HEIGHT);
			g2d.fillRect(HEALTH_DRAW_X, HEALTH_DRAW_Y, (int)healthWidth, HEALTH_DRAW_HEIGHT);
			
		}
	}


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
}
