package org.erikaredmark.monkeyshines.sprite;

import org.erikaredmark.monkeyshines.Bonzo;
import org.erikaredmark.monkeyshines.ClippingRectangle;
import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.ImmutablePoint2D;
import org.erikaredmark.monkeyshines.ImmutableRectangle;
import org.erikaredmark.monkeyshines.Point2D;
import org.erikaredmark.monkeyshines.bounds.Boundable;
import org.erikaredmark.monkeyshines.resource.SlickWorldGraphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.opengl.Texture;

/**
 * Represents a potentially moving entity in the world, bounded within a region, that may affect Bonzo in some way if
 * he collides with it. Subclasses are responsible for providing the logic to decide what to
 * draw. This merely indicates the bounds and provides the pixel-perfect and bounding box collision
 * logic to reuse.
 * <p/>
 * There are three main types of sprites.
 * 1) Monsters, which is what most verteran players think of sprites.
 * 2) Goodies
 * 3) Hazards
 * 
 * Goodies and Hazards are always 'locked' to a non-moving bounding region.
 * 
 * @author Erika Redmark
 */
public abstract class Sprite {

	// Initial state information
	// Only set by way of super call to constructor.
	private final ImmutablePoint2D   startLocation;
	private final ImmutableRectangle boundingBox;
	private final int 				 initialSpeedX;
	private final int			     initialSpeedY;
	
	
	// Movement: Classic Monkeyshines bounding box movement
	// Realtime state movement
	/** Subclasses modify or set this to determine where the sprite actually is. Initially
	 *  set to the startLocation value. */
	protected Point2D currentLocation;
	
	/** Subclasses modify or set this to determine where the sprite should be drawn from. 
	 *  initially set to 0, 0*/
	protected ClippingRectangle currentClip = ClippingRectangle.of(0, 0);
	
	// These values are SET as the final speed (sprite speed times game constant multiplier)
	protected int speedX;
	protected int speedY;
	
	/** A sprite that is invisible is also intangible. */
	public abstract boolean isVisible();
	
	/** Returns the Slick2D image for the sprite sheet for this sprite. If there
	 *  are no slick style graphics, this should throw an exception.
	 *  <p/>
	 *  This will only be called for doing pixel collision calculations, NOT for drawing!
	 * @return
	 */
	public abstract Image spriteSheet();
	
	/**
	 * Returns the slick graphics object in the world resource. If there are no slick
	 * style graphics, this should throw an exception.
	 * <p/>
	 * This will only be called doing pixel collision calculations.
	 * @return
	 */
	public abstract SlickWorldGraphics slickGraphics();
	
	public Sprite(final ImmutablePoint2D startLocation, 
			  	  final ImmutableRectangle boundingBox, 
			  	  final int initialSpeedX, 
			  	  final int initialSpeedY) {
		
		this.startLocation = startLocation;
		this.boundingBox = boundingBox;
		this.initialSpeedX = initialSpeedX;
		this.initialSpeedY = initialSpeedY;
		
		this.currentLocation = Point2D.from(startLocation);
		
		// The only place speed is directly set OTHER THAN negation (which is fine and
		// doesn't require reapplying the multiplier)
		this.speedX = initialSpeedX * GameConstants.SPEED_MULTIPLIER;
		this.speedY = initialSpeedY * GameConstants.SPEED_MULTIPLIER;
	}
	
	/**
	 * Returns the immutable bounding box this sprite starts in.
	 * @return
	 */
	public ImmutableRectangle boundingBox() { return boundingBox; }
	
	/**
	 * Returns the starting location of this sprite. The returned object is immutable.
	 */
	public ImmutablePoint2D getStaringLocation() { return this.startLocation; }
	
	/**
	 * Returns a new point instance that represents the current sprite's position. This point instance may be freely modified
	 * without affecting the original sprite.
	 */
	public Point2D newPointFromSpritePosition() { return Point2D.of(currentLocation); }
	
	/**
	 * Called be subclass-- resets the sprite parts of a subclass, allowing the
	 * subclass to focus on only resetting new information.
	 */
	protected final void resetSprite() {
		// The only place speed is directly set OTHER THAN negation (which is fine and
		// doesn't require reapplying the multiplier)
		this.speedX = initialSpeedX * GameConstants.SPEED_MULTIPLIER;
		this.speedY = initialSpeedY * GameConstants.SPEED_MULTIPLIER;
		this.currentLocation = Point2D.from(startLocation);
	}
	
	/**
	 * 
	 * Returns the bounding rectangle of the sprite. The returned rectangle is a frozen copy of the rectangle of the current
	 * bounds of the sprite at the time of the call.
	 * <p/>
	 * This differs from {@link #getBoundingBox()} in that it is the 40x40 area and point where the sprite is at the time
	 * of the call, not its defined bounds that it bounces in.
	 * 
	 */
	public ImmutableRectangle getCurrentBounds() {
		return ImmutableRectangle.of(this.currentLocation.x(), this.currentLocation.y(), GameConstants.SPRITE_SIZE_X, GameConstants.SPRITE_SIZE_Y);
	}

	
	/**
	 * 
	 * Returns the bounding box of this sprite. The returned object is immutable. Bounding boxes of sprites may not
	 * be changed once loaded.
	 * 
	 * @return
	 * 		this sprites bounding box
	 * 
	 */
	public ImmutableRectangle getBoundingBox() { return this.boundingBox; }
	
	
	/**
	 * One of the few methods whose function depends on the graphics; checks if the passed
	 * Bonzo is colliding with this sprite, not with bounding box collision but on a pixel
	 * basis. If any part of Bonzo's sprite has a non-full alpha transparency, and it touches
	 * another part of this sprite with a non-full alpha transparency, that is a collision.
	 * <p/>
	 * This method is <strong> expensive</strong> and should only be used after doing a rough
	 * bounding box determination.
	 * <p/>
	 * This method should ONLY be called during actual gameplay. It assumes Slick graphics
	 * when grabbing bounding box information. It should not be needed in the level editor for
	 * any reason anyway.
	 * 
	 * @param theBonzo
	 * 
	 * @param intersection
	 * 		the intersection rectangle of the two bounding boxes for this sprite and bonzo.
	 * 		passed since it was calculated as part of bounding box collision anyway
	 * 
	 * @return
	 * 		{@code true} if there was a pixel based collision, {@code false} if otherwise
	 *
	 */
	public boolean pixelCollision(Bonzo theBonzo, Boundable intersection) {
		if (!(isVisible()) )  return false;
		
		SlickWorldGraphics slickGraphics = slickGraphics();
		
		// Got the images in memory. Get a bounding box representing which frame is being drawn at
		// this time. those 40x40 regions will be used for pixel collision
		Image bonzoSpriteSheet = slickGraphics.bonzo;
		ImmutablePoint2D bonzoSpriteLocation = theBonzo.getDrawLocationInSprite();
		
		Image mySpriteSheet = spriteSheet();
		
		// Basically, with the intersection, we will rip pixel data of the same size
		// as the intersection from the appropriate parts of the image. Together with'
		// the information regarding which frame is active, and the intersection data,
		// we can 'overlay' the pixels with two arrays where each index in the array refers
		// to a pixel in one sprite and the appropriate overlapping pixel in the other.
		// This way, we do not need to iterate over each pixel in both images.
		final int intersectionSizeX = intersection.getSize().x();
		final int intersectionSizeY = intersection.getSize().y();
		
		if (intersectionSizeX == 0 || intersectionSizeY == 0)
			{ return false; } 
		
		final int intersectionX = intersection.getLocation().x();
		final int intersectionY = intersection.getLocation().y();
		
		// Initial location logic: Resolve the intersection location in the world against
		// our drawing location. Wherever the difference is negative, the location in the
		// source image is just the normal initial 0 point for that frame. Otherwise, it
		// is the 0 point with the positive offset.
		final int spriteIntersectionX = intersectionX - currentLocation.x();
		final int spriteIntersectionY = intersectionY - currentLocation.y();
		
		ImmutablePoint2D bonzoLocation = theBonzo.getCurrentLocation();
		final int bonzoIntersectionX = intersectionX - bonzoLocation.x();
		final int bonzoIntersectionY = intersectionY - bonzoLocation.y();
		
		final int spriteIntersectX = spriteIntersectionX < 0 ? currentClip.x() : currentClip.x() + spriteIntersectionX;
		final int spriteIntersectY = spriteIntersectionY < 0 ? currentClip.y() : currentClip.y() + spriteIntersectionY;
		// re-uses intersectionSizeX  and Y
		
		final int bonzoIntersectX = bonzoIntersectionX < 0 ? bonzoSpriteLocation.x() : bonzoSpriteLocation.x() + bonzoIntersectionX;
		final int bonzoIntersectY = bonzoIntersectionY < 0 ? bonzoSpriteLocation.y() : bonzoSpriteLocation.y() + bonzoIntersectionY;
		// re-uses intersectionSizeX  and Y
		
		Texture spriteTex = mySpriteSheet.getTexture();
		byte[] spriteIntersectionAlpha = chopAlphaTextureData(spriteTex.getTextureData(), 
			spriteTex.getTextureWidth(), spriteTex.getTextureHeight(),
			spriteIntersectX, spriteIntersectY, 
			intersectionSizeX, intersectionSizeY);
		
		Texture bonzoTex = bonzoSpriteSheet.getTexture();
		byte[] bonzoIntersectionAlpha = chopAlphaTextureData(bonzoTex.getTextureData(),
			bonzoTex.getTextureWidth(), bonzoTex.getTextureHeight(),
			bonzoIntersectX, bonzoIntersectY, 
			intersectionSizeX, intersectionSizeY);
		
		// Setup complete. We have a 1:1 between the arrays; a pixel at position i in one pixel array
		// maps to the overlapping pixel in the other array. We simply extract the alphas from each pixel
		// in the intersection area and if both are > than a Transparency Threshold constant, collision.
		
		assert spriteIntersectionAlpha.length == bonzoIntersectionAlpha.length : 
			"intersecting pixel data must be of same size!";
		
		for (int i = 0; i < spriteIntersectionAlpha.length; ++i) {
			if (spriteIntersectionAlpha[i] != 0 && bonzoIntersectionAlpha[i] != 0) { 
				return true; 
			}
		}
		
		return false;
	}
	
	/**
	 * Extracts the alpha bytes of the given data (assuming alpha is the last byte in data)
	 * for the given intersection rectangle.
	 */
	private static byte[] chopAlphaTextureData(byte[] data, int texW, int texH, int x, int y, int interW, int interH) {
		// four bytes per pixel, r g b a. Calculation otherwise starts at 
		// r. +3 to be aligned with alpha. +3 only added after setting index.
		int incY = y;
		int index = (x + (incY * texW)) * 4;
	
		byte[] extraction = new byte[interW * interH];
		for (int i = 0; i < interH; ++i) {
			// Align to alpha
			for (int j = 0; j < interW; ++j) {
				// split up so that if an error occurs, we know which
				// array index is causing an issue.
				byte alphaPixel = data[index + 3];
				extraction[(i * interW) + j] = alphaPixel;
				index += 4;
			}
			
			++incY;
			index = (x + (incY * texW)) * 4;
		}
		
		return extraction;
	}

	public int getInitialSpeedX() { return initialSpeedX; }
	public int getInitialSpeedY() {	return initialSpeedY; }
	
	// Intended for drawing routines only
	/** Intended for fast drawing routines only; returns the mutable current location instead of
	 	snapshotting it.*/
	public Point2D internalCurrentLocation() {
		return currentLocation;
	}
	
	public ClippingRectangle internalCurrentClip() {
		return currentClip;
	}

}
