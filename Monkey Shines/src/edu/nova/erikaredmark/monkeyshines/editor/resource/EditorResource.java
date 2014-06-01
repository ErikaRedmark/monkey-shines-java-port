package edu.nova.erikaredmark.monkeyshines.editor.resource;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * 
 * Static class that allows easy access and handling of graphics resources specific to the editor.
 * Graphics specific to the editor are part of the program and are not generally modifiable outside
 * of it like {@code WorldResource} instances normally are. As such, this class has static access
 * 
 * @author The Doctor
 *
 */
public final class EditorResource {

	
	private static final BufferedImage newScreenThumbnail;
	
	static {
		try {
			newScreenThumbnail = ImageIO.read(EditorResource.class.getResourceAsStream("/resources/graphics/editor/newScreen.gif") );
		} catch (IOException e) {
			throw new RuntimeException("Missing resource: new screen thumbnail: " + e.getMessage(), e);
		}
	}
	/**
	 * 
	 * Returns a 160x100 image, which is the normal game dimensions divided by four, that
	 * indicates that a screen does not exist. Used for the 'go to screen' editor functinos.
	 * 
	 * @return
	 * 		image of 'new screen' thumbnail, 160x100
	 * 
	 */
	public static BufferedImage getNewScreenThumbnail() {
		return newScreenThumbnail;
	}
	
}
