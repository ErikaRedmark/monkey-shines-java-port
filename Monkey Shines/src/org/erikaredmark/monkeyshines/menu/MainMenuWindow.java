package org.erikaredmark.monkeyshines.menu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.erikaredmark.monkeyshines.KeyBindings;
import org.erikaredmark.monkeyshines.global.KeySettings;
import org.erikaredmark.monkeyshines.global.PreferencePersistException;
import org.erikaredmark.monkeyshines.global.SoundSettings;
import org.erikaredmark.monkeyshines.global.SoundType;


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
	private static final String CLASS_NAME = "org.erikaredmark.monkeyshines.menu.MainMenuWindow";
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
	
	private static final long serialVersionUID = 1L;
	
	// A single instance of the resource is tied to the window. When it is GC'd the image resources
	// should be freed as well
	private final MainMenuResource rsrc = new MainMenuResource();
	
	// All clickable buttons are the same size. This size is used in constant expressions to calculate
	// the absolete locations the buttons will be drawn on the screen (no layout managers)
	private static final int BUTTON_SIZE_X = 60;
	private static final int BUTTON_SIZE_Y = 60;
	
	// All drawn text indicating the name of the button is drawn this many units to the right
	// side of the actual button
	private static final int TEXT_MARGIN = 10;
	private static final int FONT_HEIGHT = 12;
	
	// --------------- Play Game
	private static final int PLAY_GAME_X = 35;
	private static final int PLAY_GAME_Y = 150;
	private static final int PLAY_GAME_TEXT_X = calcTextX(PLAY_GAME_X);
	private static final int PLAY_GAME_TEXT_Y = calcTextY(PLAY_GAME_Y);
	
	// --------------- Controls
	private static final int CONTROLS_X = 35;
	private static final int CONTROLS_Y = 215;
	private static final int CONTROLS_TEXT_X = calcTextX(CONTROLS_X);
	private static final int CONTROLS_TEXT_Y = calcTextY(CONTROLS_Y);
	
	// --------------- Music
	private static final int MUSIC_X = 35;
	private static final int MUSIC_Y = 280;
	private static final int MUSIC_TEXT_X = calcTextX(MUSIC_X);
	private static final int MUSIC_TEXT_Y = calcTextY(MUSIC_Y);
	
	// --------------- Sound
	private static final int SOUND_X = 35;
	private static final int SOUND_Y = 345;
	private static final int SOUND_TEXT_X = calcTextX(SOUND_X);
	private static final int SOUND_TEXT_Y = calcTextY(SOUND_Y);
	
	// --------------- High Scores
	private static final int HIGH_X = 87;
	private static final int HIGH_Y = 410;
	private static final int HIGH_TEXT_X = calcTextX(HIGH_X);
	private static final int HIGH_TEXT_Y = calcTextY(HIGH_Y);
	
	// --------------- Help
	private static final int HELP_X = 453;
	private static final int HELP_Y = 345;
	private static final int HELP_TEXT_X = calcTextX(HELP_X);
	private static final int HELP_TEXT_Y = calcTextY(HELP_Y);
	
	// --------------- Exit
	private static final int EXIT_X = 400;
	private static final int EXIT_Y = 410;
	private static final int EXIT_TEXT_X = calcTextX(EXIT_X);
	private static final int EXIT_TEXT_Y = calcTextY(EXIT_Y);
	
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
	
	/*
	 * Static const-expressions to calculate X and Y locations of a given set of text based on button location
	 */
	private static final int calcTextX(int buttonX) { return buttonX + BUTTON_SIZE_X + TEXT_MARGIN; }
	private static final int calcTextY(int buttonY) { return buttonY + ( (BUTTON_SIZE_Y / 2) + FONT_HEIGHT); }
	
	/**
	 * 
	 * Constructs the main menu window, with callables back to the window manager whenever an action is performed that
	 * requires transitioning to a different game state.
	 * 
	 * @param playGameCallback
	 * 		runnable for when the game should start
	 * 
	 * @param highScoresCallback
	 * 		runnable for when the high scores page should be displayed
	 * 
	 */
	public MainMenuWindow(final Runnable playGameCallback, final Runnable highScoresCallback) {
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
				@Override public void run() { 
					KeyBindings newBindings = KeyboardControlDialog.launch(KeySettings.getBindings() );
					// return values used to make it easier to remove the singleton if ever required.
					KeySettings.setBindings(newBindings);
					// TODO dialog
					try {
						KeySettings.persist();
					} catch (PreferencePersistException e) {
						LOGGER.log(Level.WARNING,
								   CLASS_NAME + ": cannot persist preferences: " + e.getMessage(),
								   e);
					}
				}
			},
			rsrc.BUTTON_CONTROLS,
			CONTROLS_X,
			CONTROLS_Y);
		
		JButton music = menuButton(
			new Runnable() {
				@Override public void run() { 
					try {
						SoundControlDialog.launch(SoundType.MUSIC);
						try {
							SoundSettings.persist();
						} catch (PreferencePersistException e) {
							LOGGER.log(Level.WARNING,
									   CLASS_NAME + ": cannot persist preferences: " + e.getMessage(),
									   e);
						}
					} catch (SoundControlDialogInitException e) {
						displayNoSoundDialog(e);
					}
				}
			},
			rsrc.BUTTON_MUSIC,
			MUSIC_X,
			MUSIC_Y);
		
		JButton sound = menuButton(
			new Runnable() {
				@Override public void run() { 
					try {
						SoundControlDialog.launch(SoundType.SOUND); 
						try {
							SoundSettings.persist();
						} catch (PreferencePersistException e) {
							LOGGER.log(Level.WARNING,
									   CLASS_NAME + ": cannot persist preferences: " + e.getMessage(),
									   e);
						}
					} catch (SoundControlDialogInitException e) {
						displayNoSoundDialog(e);
					}
				}
			},
			rsrc.BUTTON_SOUND,
			SOUND_X,
			SOUND_Y);
		
		JButton highscores = menuButton(
			new Runnable() {
				@Override public void run() { highScoresCallback.run(); }
			},
			rsrc.BUTTON_HIGH,
			HIGH_X,
			HIGH_Y);
		
		JButton help = menuButton(
			new Runnable() {
				@Override public void run() { HelpDialog.launch(); }
			},
			rsrc.BUTTON_HELP,
			HELP_X,
			HELP_Y);
		
		JButton exit = menuButton(
			new Runnable() {
				@Override public void run() { 
					System.exit(0);
				}
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
	
	private void displayNoSoundDialog(SoundControlDialogInitException e) {
		Object[] options = { "Okay" };
		
		JOptionPane.showOptionDialog(
			null,
			"Sound Control levels are unavailable. The sound system was "
				+ "not initialised correctly. See error logs for details: "
				+ e.getMessage(),
			"Sound System Failure",
			JOptionPane.DEFAULT_OPTION,
			JOptionPane.WARNING_MESSAGE,
			null,
			options,
			options[0]
		);
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
		button.setRolloverEnabled(true);
		button.setRolloverIcon(new ImageIcon(images[2]) );
		MenuUtils.renderImageOnly(button);
		return button;
	}

	
	/**
	 * 
	 * Paint the standard button components for clicking, but everything else can easily just be painted on since they
	 * aren't interactive.
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
		
		// Now for text.
		g.setColor(Color.GREEN);
		g.setFont(new Font("sansserif", Font.BOLD, 24) );
		g.drawString(rsrc.TEXT_PLAY_GAME, PLAY_GAME_TEXT_X, PLAY_GAME_TEXT_Y);
		g.drawString(rsrc.TEXT_SOUND, SOUND_TEXT_X, SOUND_TEXT_Y);
		g.drawString(rsrc.TEXT_MUSIC, MUSIC_TEXT_X, MUSIC_TEXT_Y);
		g.drawString(rsrc.TEXT_CONTROLS, CONTROLS_TEXT_X, CONTROLS_TEXT_Y);
		g.drawString(rsrc.TEXT_HIGH, HIGH_TEXT_X, HIGH_TEXT_Y);
		g.drawString(rsrc.TEXT_EXIT, EXIT_TEXT_X, EXIT_TEXT_Y);
		g.drawString(rsrc.TEXT_HELP, HELP_TEXT_X, HELP_TEXT_Y);
		
		// Do not call; destroys background. Components are still painted regardless.
		// super.paintComponent(g);
	}
	
}
