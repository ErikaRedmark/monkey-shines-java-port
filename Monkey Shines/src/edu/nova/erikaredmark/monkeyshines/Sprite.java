package edu.nova.erikaredmark.monkeyshines;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** TODO
 * 
 * I have to make sure every graphic used is loaded once, and these sprites contain pointers to the sheet, and not
 * the actual sheet.
 *
 */

public class Sprite {

	// Images
	private final BufferedImage spriteSheet;
	
	// Do I even need these clipping rectangles?
//	private final ClippingRectangle leftFace;
//	private final ClippingRectangle rightFace;
	
	private ClippingRectangle currentClip;
	
	// Movement: Classic Monkeyshines bounding box movement
	private       Point2D currentLocation;
	private final ImmutablePoint2D startLocation;
	private final ImmutableRectangle boundingBox;
	private       int speedX;
	private       int speedY;
	
	private final boolean twoWayFacing;
	
	/**
	 * Constructs a new sprite with the given sprite sheet, location, speed, and a bound box. At no point will any of the
	 * sprites clipping region leaving the designated bounding box.
	 * <p/>
	 * The sprite is not 'spawned' yet. It needs to be added to a screen first.
	 * <p/>
	 * All properties are deep copied. That means that anything passed into this constructor may be re-used; the new
	 * instance stores no references to any objects passed to this constructor
	 * 
	 * @param spriteSheet
	 * 		the graphics for the sprite. A sprite sheet must be of proper dimensions
	 * 
	 * @param currentLocation
	 * 		the current location for the sprite. This is its spawn location.		
	 * 
	 * @param speedX
	 * 		speed in the horizontal direction
	 * 
	 * @param speedY
	 * 		speed in the verticle direction
	 * 
	 * @param boundingBox
	 * 		sprite will confined to this box. As it hits different sides of the box, it reverse direction, thus 'bouncing'
	 * 		within the box. If the sprite is spawned outside of this box, it will be unable to move at all
	 */
	public Sprite(final BufferedImage spriteSheet, 
				  final Point2D currentLocation, 
				  final int speedX, 
				  final int speedY, 
				  final ImmutableRectangle boundingBox) {
		
		this.spriteSheet = spriteSheet;
		currentClip = ClippingRectangle.of(GameConstants.SPRITE_SIZE_X, GameConstants.SPRITE_SIZE_Y);
		
		twoWayFacing = (spriteSheet.getHeight() > GameConstants.SPRITE_SIZE_Y);
		// If this sprite sheet has more than one row, Set seperate left/right face directions
		
//		final int spriteSheetWidth = GameConstants.SPRITE_SIZE_X * GameConstants.SPRITES_IN_ROW;
//		/* Regardless if whether it is a two way or one-way sprite, the right facing clipping is always the first row	*/
//		rightFace = ClippingRectangle.of(spriteSheetWidth,
//										 GameConstants.SPRITE_SIZE_Y);
//		
//		/* If sprite sheet is a double sheet, leftFace is identical to right face, and since they don't change we can
//		 * assign by reference. Otherwise, use a new instance that points to the second row.
//		 */
//		leftFace = (spriteSheet.getHeight() > 81) ?	// > 81 means two-row sprite sheet
//				   ClippingRectangle.of(spriteSheetWidth, GameConstants.SPRITE_SIZE_Y, 0, GameConstants.SPRITE_SIZE_Y * 2) :
//				   rightFace;

		
		this.boundingBox = boundingBox;
		this.startLocation = ImmutablePoint2D.from(currentLocation);
		this.currentLocation = Point2D.of(currentLocation);
		this.speedX = speedX;
		this.speedY = speedY;
		
		// If the sprite starts moving to the right (speed x is positve) we need to set current clipping region to 
		// the bottom sprite sheet row.
		if (twoWayFacing && speedX >= 0) {
			this.currentClip.setY(GameConstants.SPRITE_SIZE_Y);
		}
	}
	
	/**
	 * 
	 * Returns the starting location of this sprite. The returned object is immutable.
	 * 
	 * @return
	 * 		starting location
	 * 
	 */
	public ImmutablePoint2D getStaringLocation() { return this.startLocation; }

	
	/**
	 * Returns a new point instance that represents the current sprite's position. This point instance may be freely modified
	 * without affecting the original sprite.
	 * 
	 * @return
	 */
	public Point2D newPointFromSpritePosition() { return Point2D.of(currentLocation); }
	
//	/**
//	 * 
//	 * <strong> Warning: Method returns direct reference to this object's mutable location </strong>
//	 * <p/>
//	 * Returns the underlying mutable point representing the sprite's location on the screen. Intended only for drawing
//	 * functions that need fast access repeatedly to this object's location.
//	 * 
//	 * @return
//	 * 		underlying point for this sprite
//	 * 
//	 */
//	public Point2D getSpritePosition() { return this.currentLocation; }
//	
	public void resetSpritePosition() { currentLocation = Point2D.from(startLocation); }
	
	public void paint(Graphics2D g2d) {
			g2d.drawImage(spriteSheet, currentLocation.x(), currentLocation.y(), 
					currentLocation.x() + GameConstants.SPRITE_SIZE_X, 
					currentLocation.y() + GameConstants.SPRITE_SIZE_Y,
					currentClip.x(), currentClip.y(), currentClip.width() + currentClip.x(),
					currentClip.height() + currentClip.y(),  null  );
	}
	
	public void update() {
		// Update position on screen
		currentLocation.translateXFine(speedX);
		currentLocation.translateYFine(speedY);
		
		// Update Animation
		currentClip.translateX(GameConstants.SPRITE_SIZE_X);
		if (currentClip.x() >= spriteSheet.getWidth() )
			currentClip.setX(0);
		
		
		// Update both speed and Animation row. If it leaves its bounds reverse direction
		if (boundingBox.inBoundsX(currentLocation) == false) reverseX();
		if (boundingBox.inBoundsY(currentLocation) == false) reverseY();
		
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
	
	
	/** Reverse X speed, and if the sprite is a two-way facing sprite, swaps sprite sheet rows.							*/
	private void reverseX() { 
		speedX = -speedX;
		if (twoWayFacing == true) {
			if (speedX < 0)
				currentClip.setY(0);
			else
				currentClip.setY(GameConstants.SPRITE_SIZE_Y);
		}
	}
	
	
	private void reverseY() { speedY = -speedY; }
	
}
