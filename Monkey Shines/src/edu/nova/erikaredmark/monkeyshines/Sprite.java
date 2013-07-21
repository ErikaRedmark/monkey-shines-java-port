package edu.nova.erikaredmark.monkeyshines;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import edu.nova.erikaredmark.monkeyshines.encoder.EncodedSprite;

/** TODO
 * 
 * I have to make sure every graphic used is loaded once, and these sprites contain pointers to the sheet, and not
 * the actual sheet.
 *
 */

public class Sprite {

	// Initial state information
	private final ImmutablePoint2D   startLocation;
	private final ImmutableRectangle boundingBox;
	private final int				 id;
	private final int 				 initialSpeedX;
	private final int			     initialSpeedY;
	
	
	// Movement: Classic Monkeyshines bounding box movement
	// Realtime state movement
	private       Point2D currentLocation;
	private       int speedX;
	private       int speedY;
	
	// Images
	private       BufferedImage spriteSheet;
	private ClippingRectangle currentClip;

	// this boolean will be set dynamically depending on the size of the graphics context. Graphics that have
	// two rows of sprites are automatically considered two way facing.
	private       boolean twoWayFacing;
	
	public static Sprite inflateFrom(EncodedSprite encodedSprite) {
		final int id = encodedSprite.getId();
		final ImmutablePoint2D startLocation = encodedSprite.getLocation();
		final ImmutableRectangle boundingBox = encodedSprite.getBoundingBox();
		final int initialSpeedX = encodedSprite.getInitialSpeedX();
		final int initialSpeedY = encodedSprite.getInitialSpeedY();
		
		return new Sprite(id, startLocation, boundingBox, initialSpeedX, initialSpeedY);
	}
	
	private Sprite(final int id, final ImmutablePoint2D startLocation, final ImmutableRectangle boundingBox, final int initialSpeedX, final int initialSpeedY) {
		this.id = id;
		this.startLocation = startLocation;
		this.boundingBox = boundingBox;
		this.initialSpeedX = initialSpeedX;
		this.initialSpeedY = initialSpeedY;
		
		// State information
		this.speedX = initialSpeedX;
		this.speedY = initialSpeedY;
		this.currentLocation = Point2D.from(startLocation);
		
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

	public int getId() { return id; }
	public int getInitialSpeedX() { return initialSpeedX; }
	public int getInitialSpeedY() {	return initialSpeedY; }
	
}
