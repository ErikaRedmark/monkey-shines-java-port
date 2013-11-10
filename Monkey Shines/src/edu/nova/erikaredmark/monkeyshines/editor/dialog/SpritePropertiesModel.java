package edu.nova.erikaredmark.monkeyshines.editor.dialog;

import org.redmark.erika.util.ObservableModel;

import edu.nova.erikaredmark.monkeyshines.AnimationSpeed;
import edu.nova.erikaredmark.monkeyshines.AnimationType;
import edu.nova.erikaredmark.monkeyshines.ImmutablePoint2D;
import edu.nova.erikaredmark.monkeyshines.ImmutableRectangle;

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
	
	/** Both old and new values will be {@code Integer}, never null
	 */
	public static final String PROPERTY_SPRITE_ID = "propSprite";
	
	private SpritePropertiesModel(final ImmutableRectangle spriteBoundingBox, 
								  final ImmutablePoint2D spriteVelocity, 
								  final ImmutablePoint2D spriteStartingLocation,
								  final AnimationType 	 animationType,
								  final AnimationSpeed   animationSpeed,
								  final int 			 spriteId) {
		
		this.spriteBoundingBox = spriteBoundingBox;
		this.spriteVelocity = spriteVelocity;
		this.spriteStartingLocation = spriteStartingLocation;
		this.animationType = animationType;
		this.animationSpeed = animationSpeed;
		this.spriteId = spriteId;
	}
	
	/** Creates a new backing model with default sprite information (0, 0) for all points, sprite id 0, and increasing frames
	 *  for animation
	 */
	public static SpritePropertiesModel newModelWithDefaults() {
		return new SpritePropertiesModel(ImmutableRectangle.of(0, 0, 0, 0), ImmutablePoint2D.of(0, 0), ImmutablePoint2D.of(0, 0), AnimationType.INCREASING_FRAMES, AnimationSpeed.NORMAL ,0);
	}
	

	public ImmutableRectangle getSpriteBoundingBox() { return spriteBoundingBox; }
	public ImmutablePoint2D getSpriteVelocity() { return spriteVelocity; }
	public ImmutablePoint2D getSpriteStartingLocation() { return spriteStartingLocation; }
	public int getSpriteId() { return spriteId; }
	public AnimationType getAnimationType() { return animationType; }
	public AnimationSpeed getAnimationSpeed() { return animationSpeed; }
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
	
	public void setSpriteBoundingBox(ImmutableRectangle spriteBoundingBox) { this.spriteBoundingBox = spriteBoundingBox; }
	public void setSpriteVelocity(ImmutablePoint2D spriteVelocity) { this.spriteVelocity = spriteVelocity; }
	public void setSpriteStartingLocation(ImmutablePoint2D spriteStartingLocation) { this.spriteStartingLocation = spriteStartingLocation; }
	
	/** Fires a property change event {@code PROPERTY_SPRITE}
	 */
	public void setSpriteId(int spriteId) { 
		int oldId = this.spriteId;
		this.spriteId = spriteId;
		firePropertyChange(PROPERTY_SPRITE_ID, oldId, this.spriteId);
	}
	
}
