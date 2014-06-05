package edu.nova.erikaredmark.monkeyshines.editor.resource;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import edu.nova.erikaredmark.monkeyshines.LevelScreen;
import edu.nova.erikaredmark.monkeyshines.World;

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
			newScreenThumbnail = ImageIO.read(EditorResource.class.getResourceAsStream("/resources/graphics/editor/newScreen.png") );
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
	
	/**
	 * 
	 * Generates a 160x100 thumbnail of a specific screen in the world. The thumbnail is a reduced image
	 * that shows the general layout of the level, the different tiles, but critically does not show
	 * sprites or goodies. It is merely to be used as a 'oh, that's the screen I was looking for' kinda
	 * thing.
	 * <p/>
	 * If the screen id does not exist, this method fails. It is up to the client to determine what,
	 * if anything, they wish to display for non-existent screens.
	 * 
	 * @param world
	 * 		the actual world that stores the levels
	 * 
	 * @param selectedScreenId
	 * 		the screen id to generate the thumbnail for
	 * 
	 * @return
	 * 		thumbnail for the world
	 * 
	 * @throws IllegalArgumentException
	 * 		if the given screen id does not exist in the given world
	 * 
	 */
	public static BufferedImage generateThumbnailFor(World world, int selectedScreenId) {
		if (!(world.screenIdExists(selectedScreenId) ) ) {
			throw new IllegalArgumentException("Cannot generate thumbnail for non-existent screen " + selectedScreenId);
		}
		
		LevelScreen screen = world.getScreenByID(selectedScreenId);
		// Make full size temp
		BufferedImage tempLevel = new BufferedImage(640, 400, BufferedImage.TYPE_INT_ARGB);
		Graphics2D tempLevelGraphics = tempLevel.createGraphics();
		screen.paintForThumbnail(tempLevelGraphics);
		
		BufferedImage thumbnail = new BufferedImage(160, 100, BufferedImage.TYPE_INT_ARGB);
		// Iterate over each pixel of the temp image. Only use every fourth pixel from every fourth row.
		for (int i = 0; i < 160; i++) {
			for (int j = 0; j < 100; j++) {
				int srcPixel = tempLevel.getRGB(i * 4, j * 4);
				thumbnail.setRGB(i, j, srcPixel);
			}
		}
		
		tempLevelGraphics.dispose();
		
		return thumbnail;
	}
	
}
