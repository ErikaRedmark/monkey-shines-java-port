package org.erikaredmark.monkeyshines.menu;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * 
 * Class that loads up main menu resources. All instances of this class are the same, but they
 * are not cached. This class is designed to be created when the graphics are needed, and disposed
 * of (falling out of scope and GC'd) when they are not. Menu graphics are quite large and should
 * not be persisting in memory during gameplay.
 * <p/>
 * Offers access to all main menu graphics, sounds, and music. All resources are public, final 
 * variables. This resource is immutable once created.
 * 
 * @author Erika Redmark
 *
 */
public final class MainMenuResource {

	// see main class description and constructor description; INTENTIONALLY NON-STATIC!
	// Three elements in array: first is normal, second is clicked, third is highlighted.
	// Package access also intended
	final BufferedImage[] BUTTON_CONTROLS = new BufferedImage[3];
	final BufferedImage[] BUTTON_PLAY_GAME = new BufferedImage[3];
	final BufferedImage[] BUTTON_MUSIC = new BufferedImage[3];
	final BufferedImage[] BUTTON_SOUND = new BufferedImage[3];
	final BufferedImage[] BUTTON_HIGH = new BufferedImage[3];
	final BufferedImage[] BUTTON_HELP = new BufferedImage[3];
	final BufferedImage[] BUTTON_EXIT = new BufferedImage[3];
	
	final BufferedImage INFORMATION_BORDER;
	final BufferedImage INFORMATION_CONTENT;
	
	final BufferedImage BACKGROUND;
	
	final String TEXT_CONTROLS = "Controls";
	final String TEXT_PLAY_GAME = "New Game";
	final String TEXT_MUSIC = "Music";
	final String TEXT_SOUND = "Sound";
	final String TEXT_HIGH = "High Scores";
	final String TEXT_HELP = "Help";
	final String TEXT_EXIT = "Quit";
	
	
	
	/**
	 * 
	 * Initialises this object with static main menu resources for access in the program. It is the responsibility
	 * of the caller to only initialise this once and let it go out of scope properly such that these resources
	 * are not persisted or duplicated.
	 * 
	 * @throws RuntimeException
	 * 		if the graphics resources could not be found. As they are packaged in the .jar, this indicates a completely
	 * 		unhandable issue
	 * 
	 */
	public MainMenuResource() {
		// Just to make it easier on the typing...
		Class<?> clazz = this.getClass();
		try {
			// Load Controls
			BUTTON_CONTROLS[0] = ImageIO.read(clazz.getResourceAsStream("/resources/graphics/mainmenu/btnControls1.png") );
			BUTTON_CONTROLS[1] = ImageIO.read(clazz.getResourceAsStream("/resources/graphics/mainmenu/btnControls2.png") );
			BUTTON_CONTROLS[2] = ImageIO.read(clazz.getResourceAsStream("/resources/graphics/mainmenu/btnControls3.png") );
			
			// Load Play Game
			BUTTON_PLAY_GAME[0] = ImageIO.read(clazz.getResourceAsStream("/resources/graphics/mainmenu/btnNewGame1.png") );
			BUTTON_PLAY_GAME[1] = ImageIO.read(clazz.getResourceAsStream("/resources/graphics/mainmenu/btnNewGame2.png") );
			BUTTON_PLAY_GAME[2] = ImageIO.read(clazz.getResourceAsStream("/resources/graphics/mainmenu/btnNewGame3.png") );
			
			// Load Music
			BUTTON_MUSIC[0] = ImageIO.read(clazz.getResourceAsStream("/resources/graphics/mainmenu/btnMusic1.png") );
			BUTTON_MUSIC[1] = ImageIO.read(clazz.getResourceAsStream("/resources/graphics/mainmenu/btnMusic2.png") );
			BUTTON_MUSIC[2] = ImageIO.read(clazz.getResourceAsStream("/resources/graphics/mainmenu/btnMusic3.png") );
			
			// Load Sound
			BUTTON_SOUND[0] = ImageIO.read(clazz.getResourceAsStream("/resources/graphics/mainmenu/btnSound1.png") );
			BUTTON_SOUND[1] = ImageIO.read(clazz.getResourceAsStream("/resources/graphics/mainmenu/btnSound2.png") );
			BUTTON_SOUND[2] = ImageIO.read(clazz.getResourceAsStream("/resources/graphics/mainmenu/btnSound3.png") );
			
			// Load High Scores
			BUTTON_HIGH[0] = ImageIO.read(clazz.getResourceAsStream("/resources/graphics/mainmenu/btnHighScores1.png") );
			BUTTON_HIGH[1] = ImageIO.read(clazz.getResourceAsStream("/resources/graphics/mainmenu/btnHighScores2.png") );
			BUTTON_HIGH[2] = ImageIO.read(clazz.getResourceAsStream("/resources/graphics/mainmenu/btnHighScores3.png") );
			
			// Load Help
			BUTTON_HELP[0] = ImageIO.read(clazz.getResourceAsStream("/resources/graphics/mainmenu/btnHelp1.png") );
			BUTTON_HELP[1] = ImageIO.read(clazz.getResourceAsStream("/resources/graphics/mainmenu/btnHelp2.png") );
			BUTTON_HELP[2] = ImageIO.read(clazz.getResourceAsStream("/resources/graphics/mainmenu/btnHelp3.png") );
			
			// Load Exit
			BUTTON_EXIT[0] = ImageIO.read(clazz.getResourceAsStream("/resources/graphics/mainmenu/btnQuit1.png") );
			BUTTON_EXIT[1] = ImageIO.read(clazz.getResourceAsStream("/resources/graphics/mainmenu/btnQuit2.png") );
			BUTTON_EXIT[2] = ImageIO.read(clazz.getResourceAsStream("/resources/graphics/mainmenu/btnQuit3.png") );
			
			// Load borders and info
			INFORMATION_BORDER = ImageIO.read(clazz.getResourceAsStream("/resources/graphics/mainmenu/menuInfoBorder.png") );
			INFORMATION_CONTENT = ImageIO.read(clazz.getResourceAsStream("/resources/graphics/mainmenu/menuInfoWelcome.png") );
			
			// The most important of all; the main background
			BACKGROUND = ImageIO.read(clazz.getResourceAsStream("/resources/graphics/mainmenu/menuBackground.png") );
			
		} catch (IOException e) {
			throw new RuntimeException("Could not load main menu resource; possibly corrupted .jar: " + e.getMessage(), e);
		}
	}
	
}
