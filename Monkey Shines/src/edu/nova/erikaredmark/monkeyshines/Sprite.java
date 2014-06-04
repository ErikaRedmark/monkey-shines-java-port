package edu.nova.erikaredmark.monkeyshines;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import edu.nova.erikaredmark.monkeyshines.resource.CoreResource;
import edu.nova.erikaredmark.monkeyshines.resource.WorldResource;

/**
 * 
 * Represents a moving entity in the world, bounded within a region, that will kill bonzo if he
 * touched it.
 * 
 * @author Erika Redmark
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
	
	private final AnimationType animationType;
	private final AnimationSpeed animationSpeed;
	// Update tick will go from 1->animationSpeed.getTicksToUpdate(). When it reaches that value it will reset to 1.
	// THIS VARIABLE SHOULD ONLY BE MODIFIED AND CHECKED IN THE update() METHOD!
	private int updateTick = 1;
	// True means 0,1,2,3, etc.., and false means 3,2,1. This is only ever toggled to false and back if the animation type
	// is of cycling
	private boolean cycleDirection = true;
	
	// Images
	private ClippingRectangle currentClip;

	// this boolean will be set dynamically depending on the size of the graphics context. Graphics that have
	// two rows of sprites are automatically considered two way facing.
	private       boolean twoWayFacing;
	
	private WorldResource rsrc;
	/** 
	 * 
	 * Creates a new unmoving sprite with the given and resource pack. The sprite starts at 0,0, has no bounding box to
	 * move in, and has no velocity at all.
	 * <p/>
	 * A valid animation type must still be supplied
	 * 
	 * @param id
	 * 		sprite id to map to sprite graphics
	 * 
	 * @param animationType
	 * 		animation type for this sprite
	 * 
	 * @param rsrc
	 * 		world resource for obtaining the graphics context
	 * 
	 * @return
	 * 		a new instance of this class
	 */
	public static Sprite newUnmovingSprite(int id, AnimationType type, AnimationSpeed speed, WorldResource rsrc) {
		return new Sprite(id, ImmutablePoint2D.of(0, 0), ImmutableRectangle.of(0, 0, 0, 0), 0, 0, type, speed, rsrc);
	}
	
	/**
	 * 
	 * Creates a new sprite with the given properties. The properties will define all the requirements for a sprite to be
	 * saved and loaded from a world file, and for the game engine to take over and give it life.
	 * <p/>
	 * The sprite must be added to a {@code LevelScreen} before it will become an official part of a given world.
	 * 
	 * @param spriteId
	 * 		the id of the sprite, for graphics only
	 * 	
	 * @param spriteStartingLocation
	 * 		location sprite will start	
	 * 
	 * @param spriteBoundingBox
	 * 		bounding box for sprite
	 * 
	 * @param spriteVelocity
	 * 		initial velocity in x and y direction. Positive values for right and down, negative for left and up
	 * 
	 * @param animationType
	 * 		the type of animation this sprite will undergo
	 * 
	 * @param rsrc
	 * 		graphics resource for giving the sprite a proper graphics context
	 * 
	 */
	public static Sprite newSprite(int spriteId, ImmutablePoint2D spriteStartingLocation, ImmutableRectangle spriteBoundingBox, ImmutablePoint2D spriteVelocity, AnimationType animationType, AnimationSpeed speed, WorldResource rsrc) {
		return new Sprite(spriteId, spriteStartingLocation, spriteBoundingBox, spriteVelocity.x(), spriteVelocity.y(), animationType, speed, rsrc);
	}
	
	
	private Sprite(final int id, 
			  	   final ImmutablePoint2D startLocation, 
			  	   final ImmutableRectangle boundingBox, 
			  	   final int initialSpeedX, 
			  	   final int initialSpeedY, 
			  	   final AnimationType type, 
			  	   final AnimationSpeed speed,
			  	   final WorldResource rsrc) {
		
		this.id = id;
		this.startLocation = startLocation;
		this.boundingBox = boundingBox;
		this.initialSpeedX = initialSpeedX;
		this.initialSpeedY = initialSpeedY;
		this.animationType = type;
		this.animationSpeed = speed;
		setUpGraphics(rsrc);
		
		// State information
		this.speedX = initialSpeedX;
		this.speedY = initialSpeedY;
		this.currentLocation = Point2D.from(startLocation);
		
	}
	
	private void setUpGraphics(final WorldResource rsrc) {
		this.rsrc = rsrc;
		currentClip = ClippingRectangle.of(GameConstants.SPRITE_SIZE_X, GameConstants.SPRITE_SIZE_Y);
		twoWayFacing = (rsrc.getSpritesheetFor(this.id).getHeight() > GameConstants.SPRITE_SIZE_Y);
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
	 * 
	 * Returns whether this sprite is in increasing frames animation or in cylcing animation. This field cannot be changed
	 * once the sprite is created
	 * 
	 * @return
	 * 		the animation type for the sprite
	 * 
	 */
	public AnimationType getAnimationType() { return animationType; }
	
	/**
	 * 
	 * Returns the speed this sprite animates. This field cannot be changed
	 * 
	 * @return
	 * 		animation speed of the sprite
	 * 
	 */
	public AnimationSpeed getAnimationSpeed() { return animationSpeed; }
	
	/**
	 * 
	 * Returns a new point instance that represents the current sprite's position. This point instance may be freely modified
	 * without affecting the original sprite.
	 * 
	 * @return
	 * 
	 */
	public Point2D newPointFromSpritePosition() { return Point2D.of(currentLocation); }
	
	public void resetSpritePosition() { currentLocation = Point2D.from(startLocation); }
	
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
	 * Draws the sprite to the given graphics context. The sprite will draw itself to the right location at the right frame of
	 * animation as long as each 'paint' is accompanied by an 'update'
	 * 
	 * @param g2d
	 * 
	 */
	public void paint(Graphics2D g2d) {
		g2d.drawImage(rsrc.getSpritesheetFor(this.id), currentLocation.x(), currentLocation.y(), 
				currentLocation.x() + GameConstants.SPRITE_SIZE_X, 
				currentLocation.y() + GameConstants.SPRITE_SIZE_Y,
				currentClip.x(), currentClip.y(), currentClip.width() + currentClip.x(),
				currentClip.height() + currentClip.y(),  null  );
	}
	
	/**
	 * 
	 * This method, whilst it does no drawing, should not be run until this has been skinned with a resource.
	 * 
	 */
	public void update() {
		// Update position on screen
		currentLocation.translateXFine(speedX);
		currentLocation.translateYFine(speedY);
		
		// Update Animation
		// If cycle directon is true, ascend. Otherwise, descend back down the sprite sheet
		if (updateTick >= animationSpeed.getTicksToUpdate() ) {
			int multiplier = cycleDirection ? 1 : -1;
			currentClip.translateX(multiplier * GameConstants.SPRITE_SIZE_X);
			if (currentClip.x() >= rsrc.getSpritesheetFor(this.id).getWidth() ) {
				if (getAnimationType() == AnimationType.CYCLING_FRAMES) {
					cycleDirection = !cycleDirection;
					// Set last sprite
					currentClip.setX( (GameConstants.SPRITES_IN_ROW - 2) * GameConstants.SPRITE_SIZE_X);
				} else {
					currentClip.setX(0);
				}
				
			} else if (currentClip.x() < 0) {
				// Assume cycling frames; nothing else makes sense to be less than zero
				assert getAnimationType() == AnimationType.CYCLING_FRAMES;
				cycleDirection = !cycleDirection;
				currentClip.setX(GameConstants.SPRITE_SIZE_X);
			}
			updateTick = 1;
		} else {
			++updateTick;
		}
		
		
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
	
	/**
	 * 
	 * One of the few methods whose function depends on the graphics; checks if the passed
	 * Bonzo is colliding with this sprite, not with bounding box collision but on a pixel
	 * basis. If any part of Bonzo's sprite has a non-full alpha transparency, and it touches
	 * another part of this sprite with a non-full alpha transparency, that is a collision.
	 * <p/>
	 * This method is <strong> expensive</strong> and should only be used after doing a rough
	 * bounding box determination.
	 * 
	 * @param theBonzo
	 * 
	 * @return
	 * 		{@code true} if there was a pixel based collision, {@code false} if otherwise
	 *
	 */
	public boolean pixelCollision(Bonzo theBonzo) {
		// First, calculate the offset bonzo's sprite is from the target. We can then
		// map a pixel in the sprite to a pixel on bonzo's sprite. If the pixel is out of
		// range, then the sprite is not colliding. if it is in range, we compare the image
		// pixel to the bonzo pixel. Both must be non-fully alpha transperent for a collision.
		ImmutableRectangle bonzoBounds = theBonzo.getCurrentBounds();
		// offsets are with respect from sprite TO bonzo, so positive values means bonzo is to the
		// right/down of the sprite.
		int xoffset = bonzoBounds.getLocation().x() - this.currentLocation.x();
		int yoffset = bonzoBounds.getLocation().y() - this.currentLocation.y();
		
		// Got the images in memory. Get a bounding box representing which frame is being drawn at
		// this time. those 40x40 regions will be used for pixel collision
		BufferedImage bonzoSpriteSheet = CoreResource.INSTANCE.getBonzoSheet();
		ImmutablePoint2D bonzoSpriteLocation = theBonzo.getDrawLocationInSprite();
		
		BufferedImage mySpriteSheet = rsrc.getSpritesheetFor(this.id);
		
		// Store both relevant parts of the images as integer arrays to
		// reduce need for absolete position computation each iteration
		int[] spriteRgb = mySpriteSheet.getRGB(currentClip.x(),
											   currentClip.y(), 
											   GameConstants.SPRITE_SIZE_X, 
											   GameConstants.SPRITE_SIZE_Y, 
											   new int[GameConstants.SPRITE_SIZE_X * GameConstants.SPRITE_SIZE_Y],
											   0,
											   GameConstants.SPRITE_SIZE_X);
		
		int[] bonzoRgb = bonzoSpriteSheet.getRGB(bonzoSpriteLocation.x(), 
												 bonzoSpriteLocation.y(),
												 Bonzo.BONZO_SIZE.x(),
												 Bonzo.BONZO_SIZE.y(),
												 new int[Bonzo.BONZO_SIZE.x() * Bonzo.BONZO_SIZE.y()],
												 0,
												 Bonzo.BONZO_SIZE.x() );
		
		// We already have our own. Let's begin. We iterate over our pixels, and calculate which pixel 
		// position in bonzos sprite sheet we are 'on'. Hitting any location where both pixels have
		// < 100% alpha transparency (0 alpha is completely solid)
		// Loops start at 0. Two separate counters are kept to hold absolete positions in the sprite sheet
		// for both bonzo and the sprite
		// Assumption: both sprites are 40x40 pixels.
		//
		for (int i = 0; i < GameConstants.SPRITE_SIZE_X; i++) {
			// Avoid calculation every pixel
			int rowOffset = i * GameConstants.SPRITE_SIZE_X;
			int fullBonzoOffset = rowOffset + (yoffset * GameConstants.SPRITE_SIZE_X) + xoffset;
			for (int j = 0; j < GameConstants.SPRITE_SIZE_Y; j++) {
				// don't bother with calculation if this pixel is not in bonzos bounding box
				// otherwise we will hit a wrong pixel or an out of bounds issue
				if (i + xoffset >= Bonzo.BONZO_SIZE.x() || j + yoffset >= Bonzo.BONZO_SIZE.y() )  continue;
				
				int spriteAlphaAtPixel = (spriteRgb[rowOffset + j] >> 24) & 0xff;
				int bonzoAlphaAtPixel = (bonzoRgb[fullBonzoOffset + j] >> 24) & 0xff;
				
				if (spriteAlphaAtPixel == 0 && bonzoAlphaAtPixel == 0) {
					// collision
					return true;
				}
			}

		}
		
		// No colliding non-transparent pixels
		return false;
		
	}
	
	
	private void reverseY() { speedY = -speedY; }

	public int getId() { return id; }
	public int getInitialSpeedX() { return initialSpeedX; }
	public int getInitialSpeedY() {	return initialSpeedY; }


}
