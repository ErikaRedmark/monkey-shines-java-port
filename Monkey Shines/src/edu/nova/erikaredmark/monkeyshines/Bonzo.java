package edu.nova.erikaredmark.monkeyshines;

import java.awt.Graphics2D;

import com.google.common.base.Optional;

import edu.nova.erikaredmark.monkeyshines.Tile.TileType;
import edu.nova.erikaredmark.monkeyshines.graphics.CoreResource;

/**
 * 
 * Represents the main character. Object can draw itself and has awareness to the world capable of handling collision detection
 * 
 * @author Erika Redmark
 *
 */
public final class Bonzo {
	
	// Constants for bonzo
	public static final int BONZO_SIZE_X = 40;
	public static final int BONZO_SIZE_Y = 40;
	private static final int BONZO_DEATH_SIZE_X = 80;
	private static final int BONZO_DEATH_SIZE_Y = 40;
	private static final int BONZO_BEE_SIZE_X = 40;
	private static final int BONZO_BEE_SIZE_Y = 80;
	
	// ALL X'S HERE ARE ZERO
	private static final int WALK_RIGHT_Y = 0;
	private static final int WALK_SPRITES = 16;
	private static final int WALK_LEFT_Y = 40;
	
	private static final int JUMP_SPRITES = 8;
	private static final int JUMP_Y = 80;
	private static final int JUMP_RIGHT_X = 0;
	private static final int JUMP_LEFT_X = JUMP_SPRITES * BONZO_SIZE_X;
	
	private static final int DEATH_SPRITES = 16;
	private static final int DEATH_ROWS = 2;
	private static final int DEATH_COLS = 8;
	private static final int DEATH_START_Y = 120;
	
	// Sprite Info
	private int walkingDirection; // used during walking for determing what to blit.
	private int currentSprite; // used everywhere for whatever sprite he is on for animation
	
	private int currentScreenID;
	private Point2D currentLocation;
	
	// Velocity applied to bonzo. Affected by the keyboard and falls.
	private Point2D currentVelocity;
	
	// when :up: is hit, velocity is applied upwards. As long as jumping is yes and he has positive velocity and does NOT
	// have a jetpack, slowly decrease it until it reachs the max limit (MAX_FALL_SPEED)
	// Once there is ground below bonzo, stop jumping
	private boolean isJumping;
	
	// When dying, everything is overridden, and bonzo is reset.
	private boolean isDying;
	
	// Use a pointer to the current world to get information such as the screen, and then from there where bonzo
	// is relative to the screen.
	private World worldPointer;
	
	public Bonzo(final World worldPointer) {
		this.worldPointer = worldPointer;
		currentScreenID = 1000; // Always 1000. Everything starts on 1000
		final LevelScreen currentScreen = worldPointer.getScreenByID(currentScreenID);
		
		// Initialise Variables
		walkingDirection = 0;
		currentSprite = 0;
		
		// Initialise starting points
		ImmutablePoint2D start = currentScreen.getBonzoStartingLocation().multiply(GameConstants.TILE_SIZE_X, GameConstants.TILE_SIZE_Y);
		currentLocation = Point2D.from(start);
		currentScreen.setBonzoCameFrom(start);
		
		currentVelocity = Point2D.of(0, 0);
		
		restartBonzoOnScreen(worldPointer.getScreenByID(currentScreenID) );
		
	}
	
	// The World events take care of moving Bonzo around, and Bonzo has methods to swap his position
	// on screen when moving between them. restartBonzoOnScreen uses either bonzoStart, or, if not null,
	// the location bonzo entered this screen from.
	public void restartBonzoOnScreen(final LevelScreen screen) {
		Point2D newLocation = screen.newPointFromWhereBonzoCame();
		currentLocation = newLocation;
		
		// When bonzo is restarted, he is not dead or jumping
		setDying(false);
		setJumping(false);
	}
	
	public void changeScreen(final int newScreen) {
		this.currentScreenID = newScreen;
	}
	
	/**
	 * Determines if bonzo has hit the ground. Intended ONLY to be called if bonzo is currently in a jump state. If he
	 * hits the ground, speed considerations may make it possible for him to go through the ground a couple units. The returned
	 * value indicates how far to 'bump' bonzo up if he goes through the ground too far.
	 * 
	 * @return
	 * 		{@code -1} if not on the ground, other a positive value indicating how deep into the ground bonzo is
	 * 
	 */
	public int onGround() {
		// If rising, not falling, no need to check for ground. In fact, we are allowed to go through certain ground.
		if (currentVelocity.precisionY() < 0)
			return -1;
		
		LevelScreen currentScreen = worldPointer.getCurrentScreen();
		int bonzoOneBelowFeetY = (currentLocation.y() + BONZO_SIZE_Y) + 1;
		Optional<TileType> onGroundLeft = currentScreen.checkForGroundTile(currentLocation.x(), bonzoOneBelowFeetY);
		Optional<TileType> onGroundMiddle = currentScreen.checkForGroundTile(currentLocation.x() + (GameConstants.TILE_SIZE_X), bonzoOneBelowFeetY);
		Optional<TileType> onGroundRight = currentScreen.checkForGroundTile(currentLocation.x() + BONZO_SIZE_X, bonzoOneBelowFeetY );
		
		// If at least part of him is on a thru tile.
		if ( (onGroundLeft.isPresent() && onGroundLeft.get() == TileType.THRU)     || 
			 (onGroundMiddle.isPresent() && onGroundMiddle.get() == TileType.THRU) || 
			 (onGroundRight.isPresent() && onGroundRight.get() == TileType.THRU )  ) {
			//We need to make sure that we are exactly on the thing, we don't budge it.
			return ( (bonzoOneBelowFeetY - 1) % GameConstants.TILE_SIZE_Y ); 
			
		} else if ( (onGroundLeft.isPresent() && onGroundLeft.get() == TileType.SOLID)     || 
				    (onGroundMiddle.isPresent() && onGroundMiddle.get() == TileType.SOLID) || 
				    (onGroundRight.isPresent() && onGroundRight.get() == TileType.SOLID) ) {
			// If we are falling, and we are In the ground halfway, snap up. If further, keep falling
			int depth = (bonzoOneBelowFeetY - 1) % GameConstants.TILE_SIZE_Y;
			if (depth < GameConstants.TILE_SIZE_Y / 2)
				return depth;
			else
				return -1;

		}
		return -1;
	}
	
	/**
	 * 
	 * Checks if there is a solid block at the given x co-ordinate, that would interfere with bonzo's movement if he was
	 * to try to move there
	 * 
	 * @param newX
	 * 
	 * @return
	 * 		{@code true} if there is a solid block in that position, {@code false} if otherwise		
	 * 
	 */
	public boolean solidToSide(final int newX) {
		LevelScreen currentScreen = worldPointer.getCurrentScreen();
		if (currentScreen.checkForTile(newX, currentLocation.y() ) ||
				currentScreen.checkForTile(newX, currentLocation.y() + BONZO_SIZE_Y - 1) ||
				currentScreen.checkForTile(newX, currentLocation.y() + (BONZO_SIZE_Y / 2) ) ) {
			return true;
		}
		return false;
	}
	
	public boolean solidToUp(final int newY) {
		LevelScreen currentScreen = worldPointer.getCurrentScreen();
		if (currentScreen.checkForTile(currentLocation.x(), newY) ||
				currentScreen.checkForTile(currentLocation.x() + (BONZO_SIZE_X - 1), newY ) ||
				currentScreen.checkForTile(currentLocation.x() + (BONZO_SIZE_X / 2), newY ) ) {
			return true;
		}
		return false;
	}
	
	public void kill() {
		currentVelocity.setX(0);
		currentVelocity.setY(0);
		currentSprite = 0;
		setDying(true);
	}
	
	public void move(double velocity) {
		// If we are not jumping, increment the sprite
		if (!isJumping) {
			currentSprite++;
			if (currentSprite >= 16)
				currentSprite = 0;
		}
		
		//currentVelocity.x = velocity * GameConstants.BONZO_SPEED_MULTIPLIER;
		//only move if not a solid tile ahead
		
		int newX = currentLocation.x() + (int)( velocity * GameConstants.BONZO_SPEED_MULTIPLIER );
		if (velocity < 0) {
			walkingDirection = 1;
			if (!solidToSide(newX) )
				currentLocation.setX(newX);
		} else {
			int rightSide = currentLocation.x() + Bonzo.BONZO_SIZE_X + (int)( velocity * GameConstants.BONZO_SPEED_MULTIPLIER);
			walkingDirection = 0;
			if (!solidToSide(rightSide) ) {
				currentLocation.setX(newX);
			}
		}
	}
	
	// Add some jump to the velocity, and set the animation to jumping.
	public void jump(double velocity) {
		// Only allow jumping if on the ground and velocity going up is zero. And not already in the middle of a jump
		if (isJumping)
			return;
		int onGround = onGround();
		if (onGround != -1 && (currentVelocity.y() == 0)  ) {
			currentVelocity.setY(-(velocity * GameConstants.BONZO_JUMP_MULTIPLIER) );
			setJumping(true);
			currentSprite = 0;
		}
	}
	
	public void stopMoving() {
		currentVelocity.setX(0);
	}
	
	public void setDying(boolean dying) {
		System.out.println("Setting dying to " + dying);
		this.isDying = dying;
	}
	
	public void setJumping(boolean jumping) {
		this.isJumping = jumping;
	}
	
	// These four functions swap bonzo's position on the screen for when he moves from one screen to another.
	public void screenChangeLeft() {
		currentLocation.setX(GameConstants.SCREEN_WIDTH - BONZO_SIZE_X - 1);
	}
	
	public void screenChangeRight() {
		currentLocation.setX(1);
	}
	
	public void screenChangeUp() {
		currentLocation.setY(GameConstants.SCREEN_HEIGHT - BONZO_SIZE_Y - 1);
	}
	
	public void screenChangeDown() {
		currentLocation.setY(1);
	}
	
	// We don't increment the sprite unless we are jumping or dying.
	public void update() {
		// If we are dying, animate the sprite and do nothing else
		if (isDying) {
			currentSprite++;
			if (currentSprite >= 15) {
				// Death animation over. Restart bonzo. Also update drawing data
				worldPointer.restartBonzo(this);
			}
			return;
		}
		
		currentLocation.applyVelocity(currentVelocity);
		// If velocity is down, apply it. Else, wait until later to check before applying.
		
		// Give all collisions to World
		worldPointer.checkCollisions(this);
		
		// if we are jumping, slowly increment the sprite until we get to the end, then leave it there.
		if (isJumping) {
			// check for a tile above us
			if (!solidToUp(currentLocation.y() ) ) {
				//currentLocation.y -= currentVelocity.y;
				if (currentSprite < 7)
					currentSprite++;
			} else {
				currentVelocity.reverseY();
			}
		}
		
		// check for floor. If none, add some points of fall to velocity until max or hitting a floor.
		int onGround = onGround();
		if ( onGround == -1) {
			if (currentVelocity.precisionY() <= GameConstants.MAX_FALL_SPEED);
			else {
				// No incrementing once bonzo hits terminal velocity.
				// Note, bonzo's terminal velocity may be [8, 9] due to imprecision.
				//if (currentVelocity.y() > TERMINAL_VELOCITY) {
				
					// Jumps should be smoother
					// Nitpick: Bonzo falls slower if he jumps first. Not sure if this is a good idea.
					if (isJumping) {
						currentVelocity.translateYFine(-0.5);
					} else {
						currentVelocity.translateYFine(-1);
					}
				
				//}
				
			}
			// The Greater the absval of the current velocity the more the increase
		// if we are falling and hit the ground, stop the jump. onGround will have returned -1 if we were rising, not falling.
		} else {
			currentVelocity.setY(0);
			// if pushing us up takes us OFF the ground, don't do it. Sloppy Kludge
			currentLocation.translateY(-onGround); //Push back to level field.
			if (onGround() == -1)
				currentLocation.translateY(onGround);
			if (isJumping)
				currentSprite = 3;
			setJumping(false);
		} 
		
	}
	
	public void paint(Graphics2D g2d) {
		// If dying, that overrides everything.
		if (isDying) {
			int yOffset = DEATH_START_Y + (BONZO_DEATH_SIZE_Y * (currentSprite / 8) );
			int xOffset = BONZO_DEATH_SIZE_X * (currentSprite % 8);
			g2d.drawImage(CoreResource.INSTANCE.getBonzoSheet(), currentLocation.x(), currentLocation.y(),  //DEST
						  currentLocation.x() + BONZO_DEATH_SIZE_X, currentLocation.y() + BONZO_DEATH_SIZE_Y, // DEST2
						  xOffset, yOffset, xOffset + BONZO_DEATH_SIZE_X, yOffset + BONZO_DEATH_SIZE_Y,
						  null);
			return;
		}
		// if walking right
		int takeFromX = currentSprite * BONZO_SIZE_X;
		if (!isJumping) {
			g2d.drawImage(CoreResource.INSTANCE.getBonzoSheet(), currentLocation.x(), currentLocation.y(),  //DEST
						  currentLocation.x() + BONZO_SIZE_X, currentLocation.y() + BONZO_SIZE_Y, // DEST2
						  takeFromX, walkingDirection * 40, takeFromX + BONZO_SIZE_X, (walkingDirection * 40) + 40,
						  null);
		} else if (isJumping) {
			// if we are jumping to the left, we have to go 8 * 40 to the right to get to the right sprite level
			if (walkingDirection == 1)
				takeFromX += JUMP_LEFT_X;
			g2d.drawImage(CoreResource.INSTANCE.getBonzoSheet(), currentLocation.x(), currentLocation.y(),  //DEST
						  currentLocation.x() + BONZO_SIZE_X, (int)currentLocation.y() + BONZO_SIZE_Y, // DEST2
						  takeFromX, JUMP_Y, takeFromX + BONZO_SIZE_X, JUMP_Y + 40,
						  null);
		}
	}

	/**
	 * 
	 * Returns a point representing where bonzo is currently on the screen. The returned point is immutable and
	 * represents a snapshot of where he was when the method was called.
	 * 
	 * @return
	 * 		immutable point indicating where bonzo is on the screen at the time of the call
	 * 
	 */
	public ImmutablePoint2D getCurrentLocation() {
		return ImmutablePoint2D.from(currentLocation);
	}

	/**
	 * 
	 * Returns the region that bonzo currently occupies on the screen
	 * 
	 * @return
	 * 		immutable rectangle for the occupied region
	 * 
	 */
	public ImmutableRectangle getCurrentBounds() {
		return ImmutableRectangle.of(this.currentLocation.x(), this.currentLocation.y(), BONZO_SIZE_X, BONZO_SIZE_Y);
	}
	
	
}
