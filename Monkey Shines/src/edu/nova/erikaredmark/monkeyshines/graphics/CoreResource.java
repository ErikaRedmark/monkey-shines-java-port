package edu.nova.erikaredmark.monkeyshines.graphics;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * 
 * Provides access to graphics and sounds that are hardcoded into the game. 
 * 
 * @author Erika Redmark
 *
 */
public enum CoreResource {
	INSTANCE;
	
	private final BufferedImage transparentBonzo;
	private final BufferedImage bonzoSprite;
	
	private CoreResource() {
		try {
			transparentBonzo = ImageIO.read(this.getClass().getResourceAsStream("/resources/graphics/transbonzo.gif") );
		} catch (IOException e) {
			throw new RuntimeException("Missing resource: transparent bonzo: " + e.getMessage(), e);
		}
		
		try {
			bonzoSprite = ImageIO.read(this.getClass().getResourceAsStream("/resources/graphics/thebonz.gif") );
		} catch (IOException e) {
			throw new RuntimeException("Missing resource: bonzo sprite sheet: " + e.getMessage(), e);
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
}
