package edu.nova.erikaredmark.monkeyshines;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;
import javax.swing.Timer;

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
	Dimension windowSize;
	
	KeyboardInput keys;
	
	Bonzo bonzo;
	
	/**
	 * Constructs a GameWindow listening to the keyboard
	 * @param keys
	 */
	public GameWindow(final KeyboardInput keys) {
		super();
		this.keys=keys;
		setVisible(true);
		
		currentWorld = new World("spooked");
		
		// Set the Keyboard Input
		
		this.addKeyListener(keys);
		
		// Put a Bonzo on the board
		bonzo = new Bonzo(currentWorld);
		
		//. Optimisations
		setDoubleBuffered(true);
		windowSize = getSize();
		
		// Main timer for game
		// 30
		gameTimer = new Timer(30, this);
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
		currentWorld.paint(g2d);
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
