package org.erikaredmark.monkeyshines.resource;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * 
 * Provides access to graphics and sounds that are hardcoded into the game. As such, this class
 * has static access.
 * TODO move single resource to editor, since core resources is no longer used for main game.
 * 
 * @author Erika Redmark
 *
 */
public enum CoreResource {
	INSTANCE;
	
	private final BufferedImage transparentBonzo;
	
	private CoreResource() {
		try {
			transparentBonzo = ImageIO.read(this.getClass().getResourceAsStream("/resources/graphics/editor/transbonzo.png") );
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
