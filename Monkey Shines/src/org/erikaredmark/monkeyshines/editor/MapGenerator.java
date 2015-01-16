package org.erikaredmark.monkeyshines.editor;

import java.awt.image.BufferedImage;

import org.erikaredmark.monkeyshines.World;

/**
 * 
 * static utility class that, given a world, outputs a rasterised .png file, at full size, of every screen in the world
 * in correct orientation. This effectively makes it easy to view a map in its entirety.
 * <p/>
 * Rasterisation starts at some screen id, and a 640x400 image is rendered for each screen, connected at the expected locations. However, due
 * to bonus worlds, other unconnected screens may exists. Multiple rasterisations are needed for each unconnected set (client sends in
 * the starting screen id, and only connected screens from there are rendered). Under typical circumstances, rendering starting at
 * screen 1000 and the bonus screen should be enough to render every screen in the level. Any other screens, if they exist, would be
 * otherwise unaccessible anyway.
 * 
 * @author Erika Redmark
 *
 */
public final class MapGenerator {

	private MapGenerator() { }
	
	public static BufferedImage generateMap(World world, int screenStart) { 
		// TODO will require a bit more thought; not as simple as initially reckoned.
		throw new UnsupportedOperationException("Creating an image raster of the world is not available yet");
	}
	
}
