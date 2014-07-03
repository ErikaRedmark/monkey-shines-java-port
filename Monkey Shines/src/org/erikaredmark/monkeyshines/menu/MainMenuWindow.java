package org.erikaredmark.monkeyshines.menu;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;


/**
 * 
 * The main menu. What the user sees when they initialise the game.
 * <p/>
 * Beyond the actual gameplay, this is their main jump-point to access all game functions.
 * 
 * @author Erika Redmark
 *
 */
public class MainMenuWindow extends JPanel {
	private static final long serialVersionUID = 1L;
	
	// A single instance of the resource is tied to the window. When it is GC'd the image resources
	// should be freed as well
	private final MainMenuResource rsrc = new MainMenuResource();
	
	// All clickable buttons are the same size. This size is used in constant expressions to calculate
	// the absolete locations the buttons will be drawn on the screen (no layout managers)
	private static final int BUTTON_SIZE_X = 60;
	private static final int BUTTON_SIZE_Y = 60;
	
	// --------------- Play Game
	private static final int PLAY_GAME_X = 35;
	private static final int PLAY_GAME_Y = 150;
	
	// --------------- Controls
	private static final int CONTROLS_X = 35;
	private static final int CONTROLS_Y = 215;
	
	// --------------- Music
	private static final int MUSIC_X = 35;
	private static final int MUSIC_Y = 280;
	
	// --------------- Sound
	private static final int SOUND_X = 35;
	private static final int SOUND_Y = 345;
	
	// --------------- High Scores
	private static final int HIGH_X = 87;
	private static final int HIGH_Y = 410;
	
	// --------------- Help
	private static final int HELP_X = 453;
	private static final int HELP_Y = 345;
	
	// --------------- Exit
	private static final int EXIT_X = 400;
	private static final int EXIT_Y = 410;
	
	// --------------- Border
	private static final int BORDER_X = 262; 
	private static final int BORDER_Y = 157;
	private static final int BORDER_X2 = 590;
	private static final int BORDER_Y2 = 337;
	private static final int BORDER_SIZE_X = BORDER_X2 - BORDER_X;
	private static final int BORDER_SIZE_Y = BORDER_Y2 - BORDER_Y;
	
	// --------------- Informationals
	private static final int INFORMATIONAL_X = 270; 
	private static final int INFORMATIONAL_Y = 165;
	private static final int INFORMATIONAL_X2 = 581;
	private static final int INFORMATIONAL_Y2 = 328;
	private static final int INFORMATIONAL_SIZE_X = INFORMATIONAL_X2 - INFORMATIONAL_X;
	private static final int INFORMATIONAL_SIZE_Y = INFORMATIONAL_Y2 - INFORMATIONAL_Y;
	
	/**
	 * 
	 * Constructs the main menu window, with callables back to the window manager whenever an action is performed that
	 * requires transitioning to a different game state
	 * 
	 * @param playGame
	 */
	public MainMenuWindow(final Runnable playGameCallback) {
		// The buttons are the only thing that uses a Swing component; everything else is explicitly painted
		// on. Hence we are mainly focusing on button positioning and functionality here.
		setLayout(null);
		
		JButton playGame = menuButton(
			new Runnable() {
				@Override public void run() { playGameCallback.run(); }
			},
			rsrc.BUTTON_PLAY_GAME,
			PLAY_GAME_X,
			PLAY_GAME_Y);
		
		JButton controls = menuButton(
			new Runnable() {
				@Override public void run() { System.err.println("Controls Not Implemented Yet"); }
			},
			rsrc.BUTTON_CONTROLS,
			CONTROLS_X,
			CONTROLS_Y);
		
		JButton music = menuButton(
			new Runnable() {
				@Override public void run() { System.err.println("Music Not Implemented Yet"); }
			},
			rsrc.BUTTON_MUSIC,
			MUSIC_X,
			MUSIC_Y);
		
		JButton sound = menuButton(
			new Runnable() {
				@Override public void run() { System.err.println("Sound Not Implemented Yet"); }
			},
			rsrc.BUTTON_SOUND,
			SOUND_X,
			SOUND_Y);
		
		JButton highscores = menuButton(
			new Runnable() {
				@Override public void run() { System.err.println("High Scores Not Implemented Yet"); }
			},
			rsrc.BUTTON_HIGH,
			HIGH_X,
			HIGH_Y);
		
		JButton help = menuButton(
			new Runnable() {
				@Override public void run() { System.err.println("Help Not Implemented Yet"); }
			},
			rsrc.BUTTON_HELP,
			HELP_X,
			HELP_Y);
		
		JButton exit = menuButton(
			new Runnable() {
				@Override public void run() { System.err.println("Exit Not Implemented Yet"); }
			},
			rsrc.BUTTON_EXIT,
			EXIT_X,
			EXIT_Y);
		
		add(playGame);
		add(controls);
		add(music);
		add(sound);
		add(highscores);
		add(help);
		add(exit);
		
		setPreferredSize(new Dimension(640, 480) );
	}
	
	/**
	 * 
	 * Creates a menu clickable button with the given action and resource, at the specified location
	 * 
	 * @param action
	 * @param images
	 * @param locationX
	 * @param locationY
	 * @return
	 */
	private JButton menuButton(final Runnable action, final BufferedImage[] images, int locationX, int locationY) {
		JButton button =
			new JButton(new AbstractAction("", new ImageIcon(images[0]) ) {
				private static final long serialVersionUID = 1L;
				@Override public void actionPerformed(ActionEvent arg0) {
					action.run();
				}
			});
		 
		button.setLocation(locationX, locationY);
		button.setSize(BUTTON_SIZE_X, BUTTON_SIZE_Y);
		button.setPressedIcon(new ImageIcon(images[1]) );
		button.setSelectedIcon(new ImageIcon(images[2]) );
		renderImageOnly(button);
		return button;
	}
	
	/**
	 * 
	 * Changes style formatting of a button so that only the images will show; no additional drawing.
	 * 
	 */
	private void renderImageOnly(JButton button) {
		button.setBorderPainted(false); 
		button.setContentAreaFilled(false); 
		button.setFocusPainted(false); 
		button.setOpaque(false);
	}
	
	/**
	 * 
	 * Paint the standard button components for clicking, but everything else can easily just be painted on since they
	 * aren't interactive.
	 * Component painting (calling super) takes place after backgrounds have been painted.
	 * 
	 */
	@Override public void paintComponent(Graphics g) {
		g.drawImage(rsrc.BACKGROUND, 
					0, 0, 
					640, 480, 
					0, 0, 
					640, 480, 
					null);
		
		g.drawImage(rsrc.INFORMATION_BORDER,
					BORDER_X, BORDER_Y,
					BORDER_X2, BORDER_Y2,
					0, 0,
					BORDER_SIZE_X, BORDER_SIZE_Y,
					null);
		
		g.drawImage(rsrc.INFORMATION_CONTENT,
					INFORMATIONAL_X, INFORMATIONAL_Y,
					INFORMATIONAL_X2, INFORMATIONAL_Y2,
					0, 0,
					INFORMATIONAL_SIZE_X, INFORMATIONAL_SIZE_Y,
					null);
		
		// Do not call; destroys background. Components are still painted regardless.
		// super.paintComponent(g);
	}
	
}
