package edu.nova.erikaredmark.monkeyshines.graphics;

import java.awt.image.BufferedImage;

import edu.nova.erikaredmark.monkeyshines.ImmutablePoint2D;

/**
 * 
 * Holds the actual reference to a sprite sheet.
 * TODO full responsibilities of this object not decided yet
 * </ul>
 * Once a sprite graphic is initialised, it is immutable and will be freely shared among related sprites. 
 * The {@code SpriteGraphics} class handles static factory methods for instance controlling sprite graphics.
 * 
 * @author Erika Redmark
 *
 */
public class SpriteGraphic {

	private static final ImmutablePoint2D SPRITE_SIZE =
		ImmutablePoint2D.of(40, 40);

	
	private final BufferedImage spriteSheet;
	
	/** Instance controlled: Package private and only created from {@code SpriteGraphics}	*/
	SpriteGraphic(BufferedImage i) {
		spriteSheet = i;
	}
	
}
