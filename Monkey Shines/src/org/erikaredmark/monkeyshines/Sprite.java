package org.erikaredmark.monkeyshines;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.erikaredmark.monkeyshines.bounds.Boundable;
import org.erikaredmark.monkeyshines.bounds.IPoint2D;
import org.erikaredmark.monkeyshines.resource.CoreResource;
import org.erikaredmark.monkeyshines.resource.WorldResource;

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
	private final SpriteType		 type;
	
	// Movement: Classic Monkeyshines bounding box movement
	// Realtime state movement
	private       Point2D currentLocation;
	
	// These values are SET as the final speed (sprite speed times game constant multiplier)
	private int speedX;
	private int speedY;
	
	private final AnimationType animationType;
	private final AnimationSpeed animationSpeed;
	// Update tick will go from 1->animationSpeed.getTicksToUpdate(). When it reaches that value it will reset to 1.
	// THIS VARIABLE SHOULD ONLY BE MODIFIED AND CHECKED IN THE update() METHOD!
	private int updateTick;
	// True means 0,1,2,3, etc.., and false means 3,2,1. This is only ever toggled to false and back if the animation type
	// is of cycling
	private boolean cycleDirection;
	
	// Determines if a sprite is visible. Only visible sprites are updated and can collide with Bonzo.
	// Bonus and Exit doors start with this disabled. All others start with it enabled. 
	private boolean visible;
	
	// Images. Controls where the sprite will draw from
	private ClippingRectangle currentClip;

	// this boolean will be set dynamically depending on the size of the graphics context. Graphics that have
	// two rows of sprites are automatically considered two way facing.
	// A two-way facing sprite may be set not to be, but canBeTwoWayFacing simply states whether it has the 
	// capability, not whether it currently is.
	private final boolean canBeTwoWayFacing;
	private ForcedDirection forcedDirection;
	
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
	 * @param spriteType
	 * 		primary type of this sprite. Determines effects of collision
	 * 
	 * @param rsrc
	 * 		world resource for obtaining the graphics context
	 * 
	 * @return
	 * 		a new instance of this class
	 */
	public static Sprite newUnmovingSprite(int id, AnimationType type, AnimationSpeed speed, SpriteType spriteType, WorldResource rsrc) {
		return new Sprite(id, 
						  ImmutablePoint2D.of(0, 0), 
						  ImmutableRectangle.of(0, 0, 0, 0), 
						  0, 
						  0, 
						  type, 
						  speed,
						  spriteType,
						  ForcedDirection.NONE,
						  rsrc);
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
	public static Sprite newSprite(int spriteId, 
								  ImmutablePoint2D spriteStartingLocation, 
								  ImmutableRectangle spriteBoundingBox, 
								  ImmutablePoint2D spriteVelocity,
								  AnimationType animationType, 
								  AnimationSpeed speed, 
								  SpriteType spriteType,
								  ForcedDirection forcedDirection,
								  WorldResource rsrc) {
		
		return new Sprite(spriteId, 
						  spriteStartingLocation, 
						  spriteBoundingBox, 
						  spriteVelocity.x(), 
						  spriteVelocity.y(), 
						  animationType, 
						  speed, 
						  spriteType,
						  forcedDirection,
						  rsrc);
	}
	
	
	private Sprite(final int id, 
			  	   final ImmutablePoint2D startLocation, 
			  	   final ImmutableRectangle boundingBox, 
			  	   final int initialSpeedX, 
			  	   final int initialSpeedY, 
			  	   final AnimationType animationType, 
			  	   final AnimationSpeed speed,
			  	   final SpriteType spriteType,
			  	   final ForcedDirection forcedDirection,
			  	   final WorldResource rsrc) {
		
		this.id = id;
		this.startLocation = startLocation;
		this.boundingBox = boundingBox;
		this.initialSpeedX = initialSpeedX;
		this.initialSpeedY = initialSpeedY;
		this.animationType = animationType;
		this.animationSpeed = speed;
		this.type = spriteType;
		this.rsrc = rsrc;
		this.canBeTwoWayFacing = (rsrc.getSpritesheetFor(this.id).getHeight() > GameConstants.SPRITE_SIZE_Y);
		this.forcedDirection = forcedDirection;
		
		// State information
		setStateToDefaults();
		this.visible = !(type == SpriteType.EXIT_DOOR || type == SpriteType.BONUS_DOOR);
	}
	
	/**
	 * 
	 * Called from construtor or during reset: Sets all mutable state information to initial-constructed defaults.
	 * Note that the initial visibility is NOT set. That is set in constructor only. Visibility is never reset.
	 * 
	 */
	private void setStateToDefaults() {
		// The only place speed is directly set OTHER THAN negation (which is fine and
		// doesn't require reapplying the multiplier)
		this.speedX = initialSpeedX * GameConstants.SPEED_MULTIPLIER;
		this.speedY = initialSpeedY * GameConstants.SPEED_MULTIPLIER;
		this.currentLocation = Point2D.from(startLocation);
		this.cycleDirection = true;
		this.updateTick = 1;
		
		resetClip();
		
	}
	
	/**
	 * 
	 * Resets the drawing from clip to default. This is either the top row for moving right, and bottom for moving left.
	 * 
	 */
	private void resetClip() {
		currentClip = ClippingRectangle.of(GameConstants.SPRITE_SIZE_X, GameConstants.SPRITE_SIZE_Y);
		currentClip.setX(0);
		currentClip.setY(0);
		ForcedDirection dir = getForcedDirection();
		if (canBeTwoWayFacing) {
			if (  (dir == ForcedDirection.NONE && speedX >= 0) 
			    || dir == ForcedDirection.LEFT) {
				this.currentClip.setY(GameConstants.SPRITE_SIZE_Y);
			}
		}
	}
	
	/**
	 * 
	 * Changes a sprites visibility, allowing it to update with the world and affect bonzo.
	 * This is really only used for enabling bonus and exit doors when all keys are collected.
	 * <p/>
	 * This state is not saved. All sprites when a world is loaded are initialised the same. 
	 * Invisible for doors, visible for instant kill, health drainer, and scenery sprites.
	 * The editor will need to override sprite visibility during creation.
	 * <p/>
	 * This state, unlike others, is NEVER reset when bonzo leaves the screen.
	 * 
	 * @param visible
	 * 		{@code true} to make sprite visible, {@code false} to make invisible
	 * 
	 */
	public void setVisible(boolean visible) { this.visible = visible; }

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
	
	/**
	 * 
	 * Resets the sprite's position as well as all state animation to defaults
	 * 
	 */
	public void resetSprite() { 
		setStateToDefaults(); 
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
	 * Draws the sprite to the given graphics context. The sprite will draw itself to the right location at the right frame of
	 * animation as long as each 'paint' is accompanied by an 'update'
	 * 
	 * @param g2d
	 * 
	 */
	public void paint(Graphics2D g2d) {
		if (!(visible) )  return;
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
		if (!(visible) )  return;
		// Update position on screen
		currentLocation.translateXFine(speedX);
		currentLocation.translateYFine(speedY);
		
		// Update Animation
		// If cycle directon is true, ascend. Otherwise, descend back down the sprite sheet
		// note that 'cycle direction' is from 'Increasing Frames', not cycling frames.
		if (updateTick >= animationSpeed.getTicksToUpdate() ) {
			int multiplier = cycleDirection ? 1 : -1;
			currentClip.translateX(multiplier * GameConstants.SPRITE_SIZE_X);
			if (currentClip.x() >= rsrc.getSpritesheetFor(this.id).getWidth() ) {
				if (getAnimationType() == AnimationType.INCREASING_FRAMES) {
					cycleDirection = !cycleDirection;
					// Set last sprite
					currentClip.setX( (GameConstants.SPRITES_IN_ROW - 2) * GameConstants.SPRITE_SIZE_X);
				} else {
					currentClip.setX(0);
				}
				
			} else if (currentClip.x() < 0) {
				// Assume increasing/decreasing frames; nothing else makes sense to be less than zero
				assert getAnimationType() == AnimationType.INCREASING_FRAMES;
				cycleDirection = !cycleDirection;
				currentClip.setX(GameConstants.SPRITE_SIZE_X);
			}
			updateTick = 1;
		} else {
			++updateTick;
		}
		
		
		// Update both speed and Animation row. We reverse if it has both left it's bounding box AND
		// it is heading away from the box. If it isn't, it started outside of its box and should not
		// reverse direction
		if (boundingBox.inBoundsX(currentLocation) == false) {
			// Easy way to tell. Apply velocity again to temporary location and see which location
			// is closer to the bounding box.
			int tempLocationX = currentLocation.x() + speedX;
			IPoint2D bounds = boundingBox.getLocation();
			// Is the tempLocation inbetween bounding box and current location? Speed okay. Else reverse.
			// TODO way to simplify this logical expression?
			if (   (bounds.x() > tempLocationX && tempLocationX > currentLocation.x() )
			    || (bounds.x() < tempLocationX && tempLocationX < currentLocation.x() ) ) {
				
				reverseX();
			}
		}
		
		if (boundingBox.inBoundsY(currentLocation) == false) {
			// Copy Pasta of above code
			int tempLocationY = currentLocation.y() + speedY;
			IPoint2D bounds = boundingBox.getLocation();
			// Is the tempLocation inbetween bounding box and current location? Speed okay. Else reverse.
			// TODO way to simplify this logical expression?
			if (   (bounds.y() > tempLocationY && tempLocationY > currentLocation.y() )
			    || (bounds.y() < tempLocationY && tempLocationY < currentLocation.y() ) ) {
				
				reverseY();
			}
		}
		
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
	 * 
	 * Determines if the given sprite is a two-way facing sprite that has been forced a direction. This does not
	 * apply to one-way facing sprites.
	 * <p/>
	 * Note that even if a sprite is CREATED with a forced direction other than {@code NONE}, this method will
	 * still return {@code NONE} if the sprite only has one row of a sprite sheet for the current graphics
	 * resource. Because of this, even in internal code this accessor should be used.
	 * 
	 * @return
	 * 		an enumeration resulting in the possible forced directions, {@code NONE} for normal operation and
	 * 		either {@code LEFT} or {@code RIGHT} for their respective directions.
	 * 
	 */
	public ForcedDirection getForcedDirection() {
		return   canBeTwoWayFacing
			   ? this.forcedDirection
			   : ForcedDirection.NONE;
	}
	
	/** 
	 * Reverse X speed, and if the sprite is a two-way facing sprite, swaps sprite sheet rows.
	 * Two way facing sprites may be set to only use one direction by the editor. If so, twoWayFacing will
	 * have been set to false and the Y part of the clip set accordingly.
	 */
	private void reverseX() { 
		speedX = -speedX;
		// Don't swap directinos unless it is not forced and two-way
		if (   getForcedDirection() == ForcedDirection.NONE
		    && canBeTwoWayFacing) {
			if (speedX < 0) {
				currentClip.setY(0);
		    } else {
				currentClip.setY(GameConstants.SPRITE_SIZE_Y);
			}
		}
	}
	
	private void reverseY() { speedY = -speedY; }
	
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
	 * @param intersection
	 * 		the intersection rectangle of the two bounding boxes for this sprite and bonzo.
	 * 		passed since it was calculated as part of bounding box collision anyway
	 * 
	 * @return
	 * 		{@code true} if there was a pixel based collision, {@code false} if otherwise
	 *
	 */
	public boolean pixelCollision(Bonzo theBonzo, Boundable intersection) {
		if (!(visible) )  return false;
		// Got the images in memory. Get a bounding box representing which frame is being drawn at
		// this time. those 40x40 regions will be used for pixel collision
		BufferedImage bonzoSpriteSheet = CoreResource.INSTANCE.getBonzoSheet();
		ImmutablePoint2D bonzoSpriteLocation = theBonzo.getDrawLocationInSprite();
		
		BufferedImage mySpriteSheet = rsrc.getSpritesheetFor(this.id);
		
		// Basically, with the intersection, we will rip pixel data of the same size
		// as the intersection from the appropriate parts of the image. Together with'
		// the information regarding which frame is active, and the intersection data,
		// we can 'overlay' the pixels with two arrays where each index in the array refers
		// to a pixel in one sprite and the appropriate overlapping pixel in the other.
		final int intersectionSizeX = intersection.getSize().x();
		final int intersectionSizeY = intersection.getSize().y();
		final int intersectionX = intersection.getLocation().x();
		final int intersectionY = intersection.getLocation().y();
		
		// Initial location logic: Resole the intersection location in the world against
		// our drawing location. Wherever the difference is negative, the location in the
		// source image is just the normal initial 0 point for that frame. Otherwise, it
		// is the 0 point with the positive offset.
		final int spriteIntersectionX = intersectionX - currentLocation.x();
		final int spriteIntersectionY = intersectionY - currentLocation.y();
		int[] spriteRgb = mySpriteSheet.getRGB(spriteIntersectionX < 0 ? currentClip.x() : currentClip.x() + spriteIntersectionX, 
											   spriteIntersectionY < 0 ? currentClip.y() : currentClip.y() + spriteIntersectionY, 
											   intersectionSizeX, 
											   intersectionSizeY, 
											   new int[intersectionSizeX * intersectionSizeY], 
											   0, 
											   intersectionSizeX);
		
		ImmutablePoint2D bonzoLocation = theBonzo.getCurrentLocation();
		final int bonzoIntersectionX = intersectionX - bonzoLocation.x();
		final int bonzoIntersectionY = intersectionY - bonzoLocation.y();
		int[] bonzoRgb = bonzoSpriteSheet.getRGB(bonzoIntersectionX < 0 ? bonzoSpriteLocation.x() : bonzoSpriteLocation.x() + bonzoIntersectionX, 
												 bonzoIntersectionY < 0 ? bonzoSpriteLocation.y() : bonzoSpriteLocation.y() + bonzoIntersectionY, 
											     intersectionSizeX, 
											     intersectionSizeY, 
											     new int[intersectionSizeX * intersectionSizeY], 
											     0, 
											     intersectionSizeX);
		
		// Setup complete. We have a 1:1 between the arrays; a pixel at position i in one pixel array
		// maps to the overlapping pixel in the other array. We simply extract the alphas from each pixel
		// in the intersection area and if both are > than a Transparency Threshold constant, collision.
		assert spriteRgb.length == bonzoRgb.length : "intersecting pixel data must be of same size!";
		for (int i = 0; i < spriteRgb.length; i++) {
			int spritePixel = (spriteRgb[i] >> 24) & 0xFF;
			int bonzoPixel = (bonzoRgb[i] >> 24) & 0xFF;
			
			if (spritePixel > 0 && bonzoPixel > 0) {
				return true;
			}
		}
		
		return false;
		
	}

	public int getId() { return id; }
	public int getInitialSpeedX() { return initialSpeedX; }
	public int getInitialSpeedY() {	return initialSpeedY; }
	
	/**
	 * 
	 * Returns the type of sprite this is, which mainly affects how the world and/or bonzo should
	 * react when colliding with it.
	 * 
	 * @return
	 * 		type
	 * 
	 */
	public SpriteType getType() { return type; }

	/**
	 * 
	 * Represents a forced direction for the sprite. This isn't the MOVEMENT direction, this is the sprite animation
	 * direction. Basically, a two-way facing sprite may move left-right or up-down and be forced to always look in one
	 * particular direction regardless of what the sprite sheet normally allows it to do.
	 * 
	 * @author Erika Redmark
	 *
	 */
	public enum ForcedDirection {
		NONE,
		RIGHT,
		LEFT;
	}
	
	public enum SpriteType {
		NORMAL("Instant Kill") {
			@Override public void onBonzoCollision(Bonzo bonzo, World world) {
				bonzo.tryKill(DeathAnimation.NORMAL);
			}
		},
		HEALTH_DRAIN("Health Drain") {
			@Override public void onBonzoCollision(Bonzo bonzo, World world) {
				bonzo.hurt(GameConstants.HEALTH_DRAIN_PER_TICK, DamageEffect.BEE);
			}
		},
		EXIT_DOOR("Exit") {
			@Override public void onBonzoCollision(Bonzo bonzo, World world) {
				bonzo.hitExitDoor();
			}
		},
		BONUS_DOOR("Bonus") {
			@Override public void onBonzoCollision(Bonzo bonzo, World world) {
				world.bonusTransfer(bonzo);
			}
		},
		SCENERY("Harmless") {
			@Override public void onBonzoCollision(Bonzo bonzo, World world) {
				// This should not be called. Optimisations should not bother checking collisions
				// for scenery sprites.
				assert false : "No collision checks should be performed on scenery sprites";
			}
		};
		
		private final String name;
		
		private SpriteType(final String name) {
			this.name = name;
		}
		
		/**
		 * 
		 * Performs some action, either on the world or bonzo, when a sprite of this
		 * type is collided with.
		 * 
		 * @param bonzo
		 * 
		 * @param world
		 * 
		 */
		public abstract void onBonzoCollision(Bonzo bonzo, World world);
		
		@Override public String toString() { return name; }
	}

}
