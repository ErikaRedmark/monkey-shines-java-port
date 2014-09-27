package org.erikaredmark.monkeyshines.editor.dialog;

import org.erikaredmark.monkeyshines.AnimationSpeed;
import org.erikaredmark.monkeyshines.AnimationType;
import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.ImmutablePoint2D;
import org.erikaredmark.monkeyshines.ImmutableRectangle;
import org.erikaredmark.monkeyshines.Sprite;
import org.erikaredmark.monkeyshines.Sprite.ForcedDirection;
import org.erikaredmark.monkeyshines.Sprite.SpriteType;
import org.erikaredmark.monkeyshines.Sprite.TwoWayFacing;
import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.erikaredmark.util.ObservableModel;

/**
 * 
 * Backing model for the sprite properties view
 * 
 * @author Erika Redmark
 *
 */
public final class SpritePropertiesModel extends ObservableModel {


	private ImmutableRectangle spriteBoundingBox;
	private ImmutablePoint2D spriteVelocity;
	private ImmutablePoint2D spriteStartingLocation;
	
	// If dialog is not closed with okay this will stay false and client should disregard model properties.
	private boolean okay = false;
	
	private int spriteId;
	
	// Increasing frames jump from last frame to 0; cylcing start going back down to 0.
	private AnimationType animationType;
	private AnimationSpeed animationSpeed;
	private SpriteType spriteType;
	// This field is ignored for sprites that do not have two-way sprite sheets.
	// Defaults to horizontal internally but should be considered 'single' if the sprite
	// can't handle two-way facing.
	private TwoWayFacing twoWayDirection;
	private TwoWayFacing lastNonSingleDirection;
	
	// Only relevant for two-way facing sprites.
	private ForcedDirection forceDirection;
	
	// Graphics are needed to determine if a sprite can handle two-way facing or if it is stuck
	// without that. Recomputed on each id change
	private boolean canBeTwoWayFacing;
	private final WorldResource rsrc;
	
	/** Both old and new values will be {@code Integer}, never null
	 */
	public static final String PROPERTY_SPRITE_ID = "propSprite";
	
	private SpritePropertiesModel(final ImmutableRectangle spriteBoundingBox, 
								  final ImmutablePoint2D spriteVelocity, 
								  final ImmutablePoint2D spriteStartingLocation,
								  final AnimationType 	 animationType,
								  final AnimationSpeed   animationSpeed,
								  final SpriteType		 spriteType,
								  final ForcedDirection  forcedDirection,
								  final TwoWayFacing     twoWayDirection,
								  final WorldResource    rsrc,
								  final int 			 spriteId) {
		
		this.spriteBoundingBox = spriteBoundingBox;
		this.spriteVelocity = spriteVelocity;
		this.spriteStartingLocation = spriteStartingLocation;
		this.animationType = animationType;
		this.animationSpeed = animationSpeed;
		this.spriteId = spriteId;
		this.forceDirection = forcedDirection;
		this.spriteType = spriteType;
		this.rsrc = rsrc;
		
		if (twoWayDirection != TwoWayFacing.SINGLE) {
			this.lastNonSingleDirection = twoWayDirection;
		}
		
		computeTwoWayFacingInstructions();
	}
	
	private void computeTwoWayFacingInstructions() {
		this.canBeTwoWayFacing = (this.rsrc.getSpritesheetFor(this.spriteId).getHeight() > GameConstants.SPRITE_SIZE_Y);
		
		// Two way direction is ALWAYS single (and should be grayed out in dialog) regardless of input if the graphics
		// can't take it. Otherwise, set to passed value
		this.twoWayDirection =   !(this.canBeTwoWayFacing)
							   ? TwoWayFacing.SINGLE
							   : (   lastNonSingleDirection != null
								   ? lastNonSingleDirection
								   : TwoWayFacing.HORIZONTAL);
							   
		if (this.twoWayDirection != TwoWayFacing.SINGLE) {
			this.lastNonSingleDirection = this.twoWayDirection;
		}
	}
	
	/** Creates a new backing model with default sprite information (0, 0) for all points, sprite id 0, and increasing frames
	 *  for animation
	 */
	public static SpritePropertiesModel newModelWithDefaults(WorldResource rsrc) {
		return new SpritePropertiesModel(ImmutableRectangle.of(0, 0, 0, 0), 
										 ImmutablePoint2D.of(0, 0), 
										 ImmutablePoint2D.of(0, 0), 
										 AnimationType.INCREASING_FRAMES, 
										 AnimationSpeed.NORMAL, 
										 SpriteType.NORMAL,
										 ForcedDirection.NONE,
										 TwoWayFacing.HORIZONTAL,
										 rsrc,
										 0);
	}
	
	/** 
	 * 
	 * Creates a new backing model with all parameters initialised to replicate the data of the passed sprite.
	 * 
	 * @param s
	 * 		the sprite to base model off of
	 * 
	 * @return
	 * 		instance of this object
	 * 
	 */
	public static SpritePropertiesModel fromSprite(final Sprite s) {
		return new SpritePropertiesModel(s.getBoundingBox(), 
										 ImmutablePoint2D.of(s.getInitialSpeedX(), 
										 s.getInitialSpeedY() ), 
										 s.getStaringLocation(), 
										 s.getAnimationType(), 
										 s.getAnimationSpeed(),
										 s.getType(), 
										 s.getForcedDirection(),
										 s.getTwoWayFacing(),
										 s.getWorldResource(),
										 s.getId() );
	}
	

	public ImmutableRectangle getSpriteBoundingBox() { return spriteBoundingBox; }
	public ImmutablePoint2D getSpriteVelocity() { return spriteVelocity; }
	public ImmutablePoint2D getSpriteStartingLocation() { return spriteStartingLocation; }
	public int getSpriteId() { return spriteId; }
	public AnimationType getAnimationType() { return animationType; }
	public AnimationSpeed getAnimationSpeed() { return animationSpeed; }
	public SpriteType getSpriteType() { return spriteType; }
	public ForcedDirection getForceDirection() { return forceDirection; }
	public TwoWayFacing getTwoWayFacing() { return twoWayDirection; }
	public boolean isTwoWayCapable() { return canBeTwoWayFacing; }
	/**
	 * @return {@code true} if the user hit okay, {@code false} if the window was just closed or cancel was hit.
	 */
	public boolean isOkay() { return okay; }
	
	public void setSpriteBoundingBoxTopLeftX(int x) {
		spriteBoundingBox = spriteBoundingBox.newTopLeft(ImmutablePoint2D.of(x, spriteBoundingBox.getLocation().y() ) );
	}
	
	public void setSpriteBoundingBoxTopLeftY(int y) {
		spriteBoundingBox = spriteBoundingBox.newTopLeft(ImmutablePoint2D.of(spriteBoundingBox.getLocation().x(), y) );
	}
	
	public void setSpriteBoundingBoxWidth(int width) {
		spriteBoundingBox = spriteBoundingBox.newSize(ImmutablePoint2D.of(width, spriteBoundingBox.getSize().y() ) );
	}
	
	public void setSpriteBoundingBoxHeight(int height) {
		spriteBoundingBox = spriteBoundingBox.newSize(ImmutablePoint2D.of(spriteBoundingBox.getSize().x(), height ) );
	}
	
	public void setSpriteLocationX(int x) {
		spriteStartingLocation = spriteStartingLocation.newX(x);
	}
	
	public void setSpriteLocationY(int y) {
		spriteStartingLocation = spriteStartingLocation.newY(y);
	}
	
	public void setSpriteVelocityX(int x) {
		spriteVelocity = spriteVelocity.newX(x);
	}
	
	public void setSpriteVelocityY(int y) {
		spriteVelocity = spriteVelocity.newY(y);
	}
	
	public void setOkay(final boolean okay) {
		this.okay = okay;
	}
	
	public void setAnimationType(final AnimationType type) {
		this.animationType = type;
	}
	
	public void setAnimationSpeed(final AnimationSpeed speed) {
		this.animationSpeed = speed;
	}
	
	public void setForcedDirection(ForcedDirection force) { this.forceDirection = force; }
	
	public void setTwoWayFacing(TwoWayFacing direction) { this.twoWayDirection = direction; }
	
	public void setSpriteBoundingBox(ImmutableRectangle spriteBoundingBox) { this.spriteBoundingBox = spriteBoundingBox; }
	public void setSpriteVelocity(ImmutablePoint2D spriteVelocity) { this.spriteVelocity = spriteVelocity; }
	public void setSpriteStartingLocation(ImmutablePoint2D spriteStartingLocation) { this.spriteStartingLocation = spriteStartingLocation; }
	
	/** Fires a property change event {@code PROPERTY_SPRITE}
	 */
	public void setSpriteId(int spriteId) { 
		int oldId = this.spriteId;
		this.spriteId = spriteId;
		computeTwoWayFacingInstructions();
		firePropertyChange(PROPERTY_SPRITE_ID, oldId, this.spriteId);
	}

	public void setSpriteType(final SpriteType type) { this.spriteType = type; }
	
}
