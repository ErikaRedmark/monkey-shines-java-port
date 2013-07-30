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
	
	private CoreResource() {
		try {
			transparentBonzo = ImageIO.read(this.getClass().getResourceAsStream("/resources/graphics/transbonzo.gif") );
		} catch (IOException e) {
			throw new RuntimeException("Missing resource: transparent bonzo: " + e.getMessage(), e);
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
}
