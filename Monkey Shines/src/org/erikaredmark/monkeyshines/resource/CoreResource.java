package org.erikaredmark.monkeyshines.resource;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

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
	private final BufferedImage getReady;
	private final BufferedImage thunderbird;
	
	private CoreResource() {
		// Must initialise here, as static values cannot be references from enum initialiser.
//		final String CLASS_NAME = "org.erikaredmark.monkeyshines.resource.CoreResource";
//		final Logger LOGGER = Logger.getLogger(CLASS_NAME);
		
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
		
		try {
			getReady = ImageIO.read(this.getClass().getResourceAsStream("/resources/graphics/getReady.png") );
		} catch (IOException e) {
			throw new RuntimeException("Missing resource: get ready image: " + e.getMessage(), e);
		}
		
		try {
			thunderbird = ImageIO.read(this.getClass().getResourceAsStream("/resources/graphics/thunderbird.png") );
		} catch (IOException e) {
			throw new RuntimeException("Missing resource: get ready image: " + e.getMessage(), e);
		}

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
	 * Gets the get ready image to get get ready. Intended to be displayed in non-obstrusive location
	 * during respawn.
	 * 
	 * @return
	 * 		get ready image.
	 */
	public BufferedImage getGetReady() {
		return getReady;
	}

	/**
	 * 
	 * Returns the infinity image. Used in playtesting. Infinity image is designed to be drawn to 'lives' counter and is 44x36.
	 * 
	 * @return
	 * 		infinity image
	 * 
	 */
	public BufferedImage getInfinity() {
		return thunderbird;
	}
	
}
