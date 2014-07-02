package org.erikaredmark.monkeyshines.menu;

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
	
	// TODO a LOT of work ahead in calculating all the sizes and positions required to lay the buttons out exactly as
	// intended. Currently all sizes are zero.
	
	// All clickable buttons are the same size. This size is used in constant expressions to calculate
	// the absolete locations the buttons will be drawn on the screen (no layout managers)
	private static final int BUTTON_SIZE_X = 0;
	private static final int BUTTON_SIZE_Y = 0;
	
	// --------------- Play Game
	private static final int PLAY_GAME_X = 0;
	private static final int PLAY_GAME_Y = 0;
	private static final int PLAY_GAME_X2 = PLAY_GAME_X + BUTTON_SIZE_X;
	private static final int PLAY_GAME_Y2 = PLAY_GAME_Y + BUTTON_SIZE_Y;
	
	// --------------- Controls
	private static final int CONTROLS_X = 0;
	private static final int CONTROLS_Y = 0;
	private static final int CONTROLS_X2 = CONTROLS_X + BUTTON_SIZE_X;
	private static final int CONTROLS_Y2 = CONTROLS_Y + BUTTON_SIZE_Y;
	
	// --------------- Music
	private static final int MUSIC_X = 0;
	private static final int MUSIC_Y = 0;
	private static final int MUSIC_X2 = MUSIC_X + BUTTON_SIZE_X;
	private static final int MUSIC_Y2 = MUSIC_Y + BUTTON_SIZE_Y;
	
	// --------------- Sound
	private static final int SOUND_X = 0;
	private static final int SOUND_Y = 0;
	private static final int SOUND_X2 = SOUND_X + BUTTON_SIZE_X;
	private static final int SOUND_Y2 = SOUND_Y + BUTTON_SIZE_Y;
	
	// --------------- High Scores
	private static final int HIGH_X = 0;
	private static final int HIGH_Y = 0;
	private static final int HIGH_X2 = HIGH_X + BUTTON_SIZE_X;
	private static final int HIGH_Y2 = HIGH_Y + BUTTON_SIZE_Y;
	
	// --------------- Help
	private static final int HELP_X = 0;
	private static final int HELP_Y = 0;
	private static final int HELP_X2 = HELP_X + BUTTON_SIZE_X;
	private static final int HELP_Y2 = HELP_Y + BUTTON_SIZE_Y;
	
	// --------------- Exit
	private static final int EXIT_X = 0;
	private static final int EXIT_Y = 0;
	private static final int EXIT_X2 = EXIT_X + BUTTON_SIZE_X;
	private static final int EXIT_Y2 = EXIT_Y + BUTTON_SIZE_Y;
	
	public MainMenuWindow() {
		// TODO create graphics and clicking
	}
}
