package org.erikaredmark.monkeyshines.sprite;

import org.erikaredmark.monkeyshines.AnimationSpeed;
import org.erikaredmark.monkeyshines.AnimationType;
import org.erikaredmark.monkeyshines.ClippingRectangle;
import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.ImmutablePoint2D;
import org.erikaredmark.monkeyshines.ImmutableRectangle;
import org.erikaredmark.monkeyshines.MonsterType;
import org.erikaredmark.monkeyshines.bounds.IPoint2D;
import org.erikaredmark.monkeyshines.resource.SlickWorldGraphics;
import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.newdawn.slick.Image;

public class Monster extends Sprite {

	public Monster(
			final int id, 
			final ImmutablePoint2D startLocation, 
			final ImmutableRectangle boundingBox, 
			final int initialSpeedX, 
			final int initialSpeedY, 
			final AnimationType animationType, 
			final AnimationSpeed speed,
			final MonsterType spriteType,
			final ForcedDirection forcedDirection,
			final TwoWayFacing twoWayDirection,
			final WorldResource rsrc) {
		
		super(startLocation, boundingBox, initialSpeedX, initialSpeedY);
	
		this.id = id;
		this.animationType = animationType;
		this.animationSpeed = speed;
		this.type = spriteType;
		this.rsrc = rsrc;
		boolean canBeTwoWayFacing = (rsrc.getSpritesheetHeight(id) > GameConstants.SPRITE_SIZE_Y);
		this.forcedDirection = forcedDirection;
		this.twoWayDirection =   canBeTwoWayFacing 
							   ? twoWayDirection
							   : TwoWayFacing.SINGLE;
		
		// State information
		setStateToDefaults();
		this.visible = !(type == MonsterType.EXIT_DOOR || type == MonsterType.BONUS_DOOR);
	}
	
	
	/**
	 * 
	 * Called from construtor or during reset: Sets all mutable state information to initial-constructed defaults.
	 * Note that the initial visibility is NOT set. That is set in constructor only. Visibility is never reset.
	 * 
	 */
	private void setStateToDefaults() {
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
		if (this.twoWayDirection == TwoWayFacing.HORIZONTAL) {
			if (   (dir == ForcedDirection.NONE && speedX >= 0)
			    ||  dir == ForcedDirection.LEFT_DOWN) {
				
				this.currentClip.setY(GameConstants.SPRITE_SIZE_Y);
			}
		} else if (this.twoWayDirection == TwoWayFacing.VERTICAL) {
		    if (   (dir == ForcedDirection.NONE && speedY >= 0)
		    	||  dir == ForcedDirection.LEFT_DOWN) {
		    	
		    	this.currentClip.setY(GameConstants.SPRITE_SIZE_Y);
		    }
		}
	}
	/**
	 * Creates a deep copy of the given monster
	 */
	public static Monster copyOf(Monster s) {
		return new Monster(s.getId(),
						   s.getStaringLocation(),
						   s.getBoundingBox(),
						   s.getInitialSpeedX(),
						   s.getInitialSpeedY(),
						   s.getAnimationType(),
						   s.getAnimationSpeed(),
						   s.getType(),
						   s.getForcedDirection(),
						   s.getTwoWayFacing(),
						   s.rsrc);
	}
	
	
	/** 
	 * Creates a new unmoving monster with the given and resource pack. The sprite starts at 0,0, 
	 * has no bounding box to
	 * move in, and has no velocity at all. If it is a two-way facing sprite, it starts as horizontal.
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
	public static Monster newUnmovingMonster(int id, AnimationType type, AnimationSpeed speed, MonsterType spriteType, WorldResource rsrc) {
		boolean canBeTwoWayFacing = (rsrc.getSpritesheetHeight(id) > GameConstants.SPRITE_SIZE_Y);
		return new Monster(id, 
						  ImmutablePoint2D.of(0, 0), 
						  ImmutableRectangle.of(0, 0, 0, 0), 
						  0, 
						  0, 
						  type, 
						  speed,
						  spriteType,
						  ForcedDirection.NONE,
						  canBeTwoWayFacing ? TwoWayFacing.HORIZONTAL : TwoWayFacing.SINGLE, 
						  rsrc);
	}
	
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
		return   twoWayDirection != TwoWayFacing.SINGLE
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
		// Don't swap directions unless it is not forced and two-way
		if (   getForcedDirection() == ForcedDirection.NONE
		    && twoWayDirection == TwoWayFacing.HORIZONTAL) {
			if (speedX < 0) {
				currentClip.setY(0);
		    } else {
				currentClip.setY(GameConstants.SPRITE_SIZE_Y);
			}
		}
	}
	
	private void reverseY() { 
		speedY = -speedY; 
		// Copy pasta of reverseY, but looks at up/down directions
		if (   getForcedDirection() == ForcedDirection.NONE
			&& twoWayDirection == TwoWayFacing.VERTICAL) {
			if (speedY < 0) {
				currentClip.setY(0);
		    } else {
				currentClip.setY(GameConstants.SPRITE_SIZE_Y);
			}
		}
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
			if (currentClip.x() >= rsrc.getSpritesheetWidth(id) ) {
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
		
		ImmutableRectangle boundingBox = boundingBox();
		
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
	
	@Override public Image spriteSheet() {
		return rsrc.getSlickGraphics().sprites[this.id];
	}
	
	@Override public SlickWorldGraphics slickGraphics() { return rsrc.getSlickGraphics(); }
	
	@Override public boolean isVisible() { return visible; }
	
	public int getId() { return id; }

	/**
	 * Returns whether this sprite is in increasing frames animation or in cylcing animation. This field cannot be changed
	 * once the sprite is created
	 */
	public AnimationType getAnimationType() { return animationType; }
	
	/**
	 * Returns the speed this sprite animates. This field cannot be changed
	 */
	public AnimationSpeed getAnimationSpeed() { return animationSpeed; }
	
	/**
	 * Returns the back-forth direction required to make this sprite animate between its different
	 * frames of animation.
	 */
	public TwoWayFacing getTwoWayFacing() { return twoWayDirection; }
	
	/**
	 * Returns the resource that this sprite draws its graphics from.
	 */
	public WorldResource getWorldResource() { return this.rsrc; }
	
	/**
	 * Resets the sprite's position as well as all state animation to defaults
	 */
	public void resetMonster() { 
		setStateToDefaults(); 
	}
	
	/**
	 * Returns the type of monster this is, which mainly affects how the world and/or bonzo should
	 * react when colliding with it.
	 */
	public MonsterType getType() { return type; }
	
	private final int id;
	private final MonsterType type;
	private final AnimationType animationType;
	private final AnimationSpeed animationSpeed;
	// True means 0,1,2,3, etc.., and false means 3,2,1. This is only ever toggled to false and back if the animation type
	// is of cycling
	private boolean cycleDirection;
	
	// Determines if a monster is visible. Only visible monsters are updated and can collide with Bonzo.
	// Bonus and Exit doors start with this disabled. All others start with it enabled. 
	private boolean visible;
	
	// Update tick will go from 1->animationSpeed.getTicksToUpdate(). When it reaches that value it will reset to 1.
	// THIS VARIABLE SHOULD ONLY BE MODIFIED AND CHECKED IN THE update() METHOD!
	private int updateTick;
	

	// this boolean will be set dynamically depending on the size of the graphics context. Graphics that have
	// two rows of sprites are automatically considered two way facing.
	// A two-way facing sprite may be set not to be, but canBeTwoWayFacing simply states whether it has the 
	// capability, not whether it currently is.
	private TwoWayFacing twoWayDirection;
	private ForcedDirection forcedDirection;
	
	private WorldResource rsrc;
	
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
		// Right and Up are practically the same; right facing and top facing are always the first row
		RIGHT_UP,
		// Similiar practical identity to right/up. Left and down are always the bottom row
		LEFT_DOWN;
	}
	
	public enum TwoWayFacing {
		// One row of spritesheet
		SINGLE,
		// Two rows: default is horiztonal if two rows
		HORIZONTAL,
		VERTICAL;
	}
}
