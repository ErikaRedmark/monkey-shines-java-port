package org.erikaredmark.monkeyshines.background;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * 
 * Represents a full, 640x400 background that is displayed taking up the entire window. These backgrounds are capable of
 * being very finely detailed, to the point of being substituted as scenery if so desired. They can be created from a single
 * 640x400 piece (the full background) or created from a Pattern (which is a smaller image that will be tiled onto
 * a 640x400 context)
 * <p/>
 * Instances of this class are immutable. System should create one instance per full background and instances are accessible
 * via {@code WorldResource}
 * 
 * @author Erika Redmark
 *
 */
public class FullBackground extends Background {

	// 640x400
	private final BufferedImage background;
	private final int id;
	private final boolean isPattern;

	private FullBackground(final BufferedImage background, int id, boolean isPattern) {
		if (background.getWidth() != 640 || background.getHeight() != 400) {
			throw new IllegalArgumentException("Backgrounds MUST be 640x480. Only patterns may be variable sizes.");
		}
		
		this.background = background;
		this.id = id;
		this.isPattern = isPattern;
	}
	
	/**
	 * 
	 * Creates a basic full background. The background MUST be 640x400
	 * 
	 * @param background
	 * 		the background
	 * 
	 * @param id
	 * 		the id of this background from the graphics resource. Required for encoding
	 * 		algorithms to properly save instances
	 * 
	 * @return
	 * 		instance of this object
	 * 
	 * @throws IllegalArgumentException
	 * 		if the background is not 640x400
	 * 
	 */
	public static FullBackground of(final BufferedImage background, int id) {
		return new FullBackground(background, id, false);
	}
	
	/**
	 * 
	 * This classic background type (ppat resource) from the original Monkey Shines. Represents a variable
	 * size pattern that will be tiled onto the destination. Creates a full background by tiling the pattern
	 * onto a 640x480 context.
	 * 
	 * @param ppat
	 * 		the pattern to use
	 * 
	 * @param id
	 * 		the id of this background from the graphics resource. Required for encoding
	 * 		algorithms to properly save instances
	 * 
	 * @return
	 * 		instance of this object
	 * 
	 * @throws IllegalArgumentException
	 * 		if the background is bigger than 640x400
	 * 
	 */
	public static FullBackground fromPattern(final BufferedImage ppat, int id) {
		// We tile 640 / width. If there is any remainder, then we did NOT hit the edge
		// properly and must tile once more (albeit the last tile will only tile partway)
		int width = ppat.getWidth();
		int height = ppat.getHeight();
		
		int tileX = 640 / width + (   640 % width != 0
									? 1
								    : 0);
		
		if (tileX == 0)  throw new IllegalArgumentException("Width " + width + " too large for pattern: must be less than 640");
		
		int tileY = 400 / height + (   400 % height != 0
									 ? 1
									 : 0);
		
		if (tileY == 0)  throw new IllegalArgumentException("Height " + height + " too large for pattern: must be less than 400");
		
		BufferedImage background = new BufferedImage(640, 400, ppat.getType() );
		
		
		Graphics2D graphics = background.createGraphics();
		try {
			// Start with Y: for each ROW
			for (int j = 0; j < tileY; j++) {
				// For each COLUMN
				for (int i = 0; i < tileX; i++) {
					int dx = i * width, dy = j * height;
					graphics.drawImage(
						ppat, 
						dx, dy, 
						dx + width, dy + height, 
						0, 0, 
						width, height, null);
				}
			}
		} finally {
			graphics.dispose();
		}
		
		return new FullBackground(background, id, true);
	}
	
	@Override public void draw(Graphics2D g2d) {
		g2d.drawImage(background, 0, 0, 640, 400, 0, 0, 640, 400, null);
	}

	/**
	 * 
	 * Intended for encoder algorithms. Returns the id that this background or pattern is
	 * stored as.
	 * 
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * 
	 * Intended for encoder algorithms. Returns whether this background was created from a pattern or
	 * as a full background.
	 * 
	 */
	public boolean isPattern() {
		return isPattern;
	}
	
}
