package org.erikaredmark.monkeyshines.resource;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.erikaredmark.monkeyshines.MonkeyShines;

/**
 * 
 * Provides access to graphics and sounds that are hardcoded into the game. As such, this class
 * has static access.
 * 
 * TODO move resource loading to non static classes where required, so memory of the objects
 * are not taken up when not required (such as bonzo sprite sheet during editor) and are disposed
 * when required.
 * 
 * @author Erika Redmark
 *
 */
public enum CoreResource {
	INSTANCE;
	
	private final BufferedImage transparentBonzo;
	private final BufferedImage bonzoSprite;
	private final Font chicago;
	
	private CoreResource() {
		// Must initialise here, as static values cannot be references from enum initialiser.
		final String CLASS_NAME = "org.erikaredmark.monkeyshines.resource.CoreResource";
		final Logger LOGGER = Logger.getLogger(CLASS_NAME);
		
		try {
			transparentBonzo = ImageIO.read(this.getClass().getResourceAsStream("/resources/graphics/editor/transbonzo.png") );
		} catch (IOException e) {
			throw new RuntimeException("Missing resource: transparent bonzo: " + e.getMessage(), e);
		}
		
		try {
			bonzoSprite = ImageIO.read(this.getClass().getResourceAsStream("/resources/graphics/thebonz.png") );
		} catch (IOException e) {
			throw new RuntimeException("Missing resource: bonzo sprite sheet: " + e.getMessage(), e);
		}
		
		Font tempChicago = null;
		try {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			tempChicago = 
				Font.createFont(
					Font.TRUETYPE_FONT, 
					MonkeyShines.class.getResourceAsStream("/resources/fonts/chicago.ttf") 
				);
			ge.registerFont(tempChicago);
			assert tempChicago != null;
		} catch (IOException | FontFormatException e) {
			LOGGER.log(Level.SEVERE, 
					   CLASS_NAME + ": Missing or unable to load Chicago font. This may cause some display problems. Reason for failure: " + e.getMessage(),
					   e);
		}
		
		chicago = tempChicago;
	}
	
	/**
	 * 
	 * This is the graphic that is overlayed in the world editor to indicate Bonzo's starting location on a screen. It is 
	 * 40 x 40.
	 * 
	 * @return
	 * 		bonzo's starting location sprite
	 * 
	 */
	public BufferedImage getTransparentBonzo() {
		return transparentBonzo;
	}
	
	/**
	 * 
	 * This is the entire sprite sheet of all animations for Bonzo
	 * 
	 * @return
	 * 		bonzo's sprite sheet
	 * 
	 */
	public BufferedImage getBonzoSheet() {
		return bonzoSprite;
	}
	
	/**
	 * 
	 * Returns the chicago font. This MAY be {@code null} if there was a loading issue with the .jar.
	 * In that event, a different font should be used.
	 * <p/>
	 * In practise, this should rarely be {@code null}.
	 * 
	 */
	public Font getChicago() { return chicago; }
	
}
