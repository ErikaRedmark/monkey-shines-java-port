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
	private final Point2D startLocation;
	private final ClippingRectangle boundingBox;
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
				  final ClippingRectangle boundingBox) {
		
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

		
		this.boundingBox = ClippingRectangle.of(boundingBox);
		this.startLocation = Point2D.of(currentLocation);
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
	 * Returns a new point instance that represents the current sprite's position. This point instance may be freely modified
	 * without affecting the original sprite.
	 * 
	 * @return
	 */
	public Point2D newPointFromSpritePosition() { return Point2D.of(currentLocation); }
	
	public void resetSpritePosition() { currentLocation = Point2D.of(startLocation); }
	
	public void paint(Graphics2D g2d) {
			g2d.drawImage(spriteSheet, currentLocation.drawX(), currentLocation.drawY(), 
					currentLocation.drawX() + GameConstants.SPRITE_SIZE_X, 
					currentLocation.drawY() + GameConstants.SPRITE_SIZE_Y,
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
