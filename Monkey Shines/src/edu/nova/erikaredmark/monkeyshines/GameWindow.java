package edu.nova.erikaredmark.monkeyshines;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.nio.file.Path;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import edu.nova.erikaredmark.monkeyshines.encoder.EncodedWorld;
import edu.nova.erikaredmark.monkeyshines.encoder.WorldIO;
import edu.nova.erikaredmark.monkeyshines.encoder.exception.WorldRestoreException;
import edu.nova.erikaredmark.monkeyshines.graphics.exception.ResourcePackException;
import edu.nova.erikaredmark.monkeyshines.resource.WorldResource;

/**
 * Initialises a JPanel that holds the game window. This window is where all the action and
 * sprites will be drawn to. 
 * @author Erika Redmark
 *
 */
public class GameWindow extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1418470684111076474L;

	// DEBUG STUFF
	LevelScreen currentScreen;
	World currentWorld;
	
	Timer gameTimer;
	
	KeyboardInput keys;
	
	Bonzo bonzo;
	
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
			}
		} else {
			// No world chosen, no graphics loaded, nothing to do. Quit
			System.exit(0);
		}
		
		this.addKeyListener(keys);
		
		bonzo = new Bonzo(currentWorld);
		setDoubleBuffered(true);
		setMinimumSize(new Dimension(GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT) );
		setPreferredSize(new Dimension(GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT) );
		
		gameTimer = new Timer(GameConstants.GAME_SPEED, this);
		
		setVisible(true);
		
		gameTimer.start();
	}

	/**
	 * Calls the paint and update functions on the World object and Bonzo object
	 * The sprites and tiles are handled by the World object.
	 */
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		// Clear the screen with the world
		currentWorld.paintAndUpdate(g2d);
		bonzo.update();
		bonzo.paint(g2d);
	}

	/**
	 * Polls the keyboard for valid operations the player may make on Bonzo. During gameplay, 
	 * the only allowed operations are moving left/right or jumping. 
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
