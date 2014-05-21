package edu.nova.erikaredmark.monkeyshines;

import java.awt.Graphics2D;

import edu.nova.erikaredmark.monkeyshines.resource.CoreResource;
import edu.nova.erikaredmark.monkeyshines.tiles.StatelessTileType;
import edu.nova.erikaredmark.monkeyshines.tiles.TileType;

/**
 * 
 * Represents the main character. Object can draw itself and has awareness to the world capable of handling collision detection
 * 
 * @author Erika Redmark
 *
 */
public final class Bonzo {
	
	// Constants for bonzo
	public static final ImmutablePoint2D BONZO_SIZE = ImmutablePoint2D.of(40, 40);
	
	private static final int JUMP_SPRITES = 8;
	private static final int JUMP_Y = 80;
//	private static final int JUMP_RIGHT_X = 0;
	private static final int JUMP_LEFT_X = JUMP_SPRITES * BONZO_SIZE.x();
	
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
	// The current animation to use for bonzo dying. This field is only used when going through
	// death animations and is only updated when bonzo is killed.
	private DeathAnimation deathAnimation;
	
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
		setDying(false, this.deathAnimation);
		setJumping(false);
	}
	
	public void changeScreen(final int newScreen) {
		this.currentScreenID = newScreen;
	}
	
	/**
	 * 
	 * Calls onGround(originalPositionY) with the same original position as the current location. Used from when bonzo is
	 * just standing around and needs to know if there is ground beneath.
	 * 
	 */
	public int onGround() {
		return onGround(currentLocation.y() );
	}
	
	/**
	 * Determines if bonzo has hit the ground. Intended ONLY to be called if bonzo is currently in a jump state. If he
	 * hits the ground, speed considerations may make it possible for him to go through the ground a couple units. The returned
	 * value indicates how far to 'bump' bonzo up if he goes through the ground too far.
	 * <p/>
	 * <strong> The speed bonzo is falling must NOT exceed one minus the verticle size of the tile!</strong> Otherwise
	 * he will end up being bumped up to the next tile down, being inside a solid. Terminal velocity should never reach above
	 * that amount.
	 * <p/>
	 * This method does not modify any state and merely returns a value.
	 * <p/>
	 * The parameters for original position are required to determine, for dealing with thru blocks, if bonzo fell onto
	 * the block, or if he was already inside of it. 
	 * 
	 * @param originalPositionY
	 * 		bonzo's original y position before changing. Used to determine if he fell on a block. For thrus, this means
	 * 		preventing him from snapping on top if he just missed it and is now inside it.
	 * 
	 * @return
	 * 		{@code -1} if not on the ground, other a positive value indicating how deep into the ground bonzo is. This may
	 * 		return 0... in which case bonzo is perfectly fine on the ground.
	 * 
	 */
	public int onGround(int originalPositionY) {
		// If rising, not falling, no need to check for ground. In fact, we are allowed to go through certain ground.
		if (currentVelocity.precisionY() < 0)  return -1;
		
		LevelScreen currentScreen = worldPointer.getCurrentScreen();
		int bonzoOneBelowFeetY = (currentLocation.y() + BONZO_SIZE.y() ) + 1;
		TileType onGroundLeft = currentScreen.getTileAt(currentLocation.x() + 3, bonzoOneBelowFeetY);
		TileType onGroundRight = currentScreen.getTileAt(currentLocation.x() + (BONZO_SIZE.x() - 4), bonzoOneBelowFeetY );
		
		// If at least part of him is on a thru tile.
		if (    onGroundLeft == StatelessTileType.THRU
			 || onGroundRight == StatelessTileType.THRU) {
			// If bonzo is already exactly on the ground, everything is fine. Otherwise, we may have to fall through it.
			int depth = (bonzoOneBelowFeetY - 1) % GameConstants.TILE_SIZE_Y;
			if (depth == 0)  return 0;
			
			// Very important! If we are inside of a thru, we do NOT bounce onto it unless bonzo's original position was
			// ABOVE the thru. Otherwise, it is too easy for him to snap up if a jump didn't quite make it.
			// We do not snap up at zero velocity only if bonzos original position was equal to or less than this position. 
			// Zero velocity with identical Y position means bonzo is not falling; he is standing, so he IS on the ground
			// exactly already.
			if (originalPositionY / GameConstants.TILE_SIZE_Y == currentLocation.y() / GameConstants.TILE_SIZE_Y) {
				
				return -1;
				// Effectively, if we snap the original position and the current position and we end up at the same tile, then
				// we approached it from the side, not above.
			}
			
			//We need to make sure that we are exactly on the thing, we don't budge it.
			return ( (bonzoOneBelowFeetY - 1) % GameConstants.TILE_SIZE_Y ); 
			
		} else if (    onGroundLeft == StatelessTileType.SOLID
				    || onGroundRight == StatelessTileType.SOLID) {
			return (bonzoOneBelowFeetY - 1) % GameConstants.TILE_SIZE_Y;

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
		if (   currentScreen.getTileAt(newX, currentLocation.y() ) == StatelessTileType.SOLID 
		    || currentScreen.getTileAt(newX, currentLocation.y() + BONZO_SIZE.y() - 1) == StatelessTileType.SOLID
			|| currentScreen.getTileAt(newX, currentLocation.y() + (BONZO_SIZE.y() / 2) ) == StatelessTileType.SOLID) {
			
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * Checks if there is a solid block at the given y co-ordinate, that would interfere with bonzo's jumping if he
	 * was to try to jump
	 * 
	 * @param newY
	 * 		the 'new' y level bonzo is or is going to be
	 * 
	 * @return
	 * 		{@code true} if there is a solid block in that position, {@code false} if otherwise
	 * 
	 */
	public boolean solidToUp(final int newY) {
		LevelScreen currentScreen = worldPointer.getCurrentScreen();
		if (   currentScreen.getTileAt(currentLocation.x(), newY) == StatelessTileType.SOLID 
			|| currentScreen.getTileAt(currentLocation.x() + (BONZO_SIZE.x() - 1), newY ) == StatelessTileType.SOLID 
			|| currentScreen.getTileAt(currentLocation.x() + (BONZO_SIZE.x() / 2), newY ) == StatelessTileType.SOLID) {
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * Tries to kill bonzo. This takes into account if bonzo if invincible: if he is, does
	 * nothing. Otherwise, kills bonzo.
	 * 
	 * @param
	 * 		if bonzo does die, use this death animation
	 * 
	 */
	public void tryKill(DeathAnimation animation) {
		// TODO do invincibility checks. Right now this just forwards to kill()
		kill(animation);
	}
	
	/**
	 * 
	 * Kills bonzo. He stops moving and begins the given death animation. After effects of death (such
	 * as level reset) are deferred until the animation finishes.
	 * 
	 * @param
	 * 		uses this death animatino for bonzo
	 * 
	 */
	public void kill(DeathAnimation animation) {
		currentVelocity.setX(0);
		currentVelocity.setY(0);
		currentSprite = 0;
		setDying(true, animation);
		worldPointer.getResource().getSoundManager().playOnce(animation.soundEffect() );
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
			int rightSide = currentLocation.x() + Bonzo.BONZO_SIZE.x() + (int)( velocity * GameConstants.BONZO_SPEED_MULTIPLIER);
			walkingDirection = 0;
			if (!solidToSide(rightSide) ) {
				currentLocation.setX(newX);
			}
		}
	}
	
	/**
	 * 
	 * Jump action from player input: If there is no solid directly above Bonzo, then this will set bonzo's state to jumping
	 * and apply some starting y velocity.
	 * 
	 * @param velocity
	 * 		starting velocity to apply
	 * 
	 */
	public void jump(double velocity) {
		// Don't start a jump if there isn't enough room.
		if (solidToUp(currentLocation.y() - 1) )  return;
		
		// Only allow jumping if on the ground and velocity going up is zero. And not already in the middle of a jump
		if (isJumping)  return;
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
	
	/**
	 * 
	 * Sets bonzo to the 'dying' state. This just plays whatever animation was requested, and
	 * after the animation is finished post-die animatino routines are run (like level reset)
	 * 
	 * @param dying
	 * 		{@code true} to set to dying, {@code false} to stop dying (used after level reset)
	 * 
	 * @param animation
	 * 		animation to use for death. Ignored if setting the dying to false
	 * 
	 */
	private void setDying(boolean dying, DeathAnimation animation) {
		this.isDying = dying;
		this.deathAnimation = animation;
	}
	
	/**
	 * 
	 * Sets bonzos state to jumping
	 * 
	 * @param jumping
	 * 
	 */
	private void setJumping(boolean jumping) {
		this.isJumping = jumping;
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
		
		int originalY = currentLocation.y();
		currentLocation.applyVelocity(currentVelocity);
		// At this time, we could be inside of a block. Since by game nature velocity is only either
		// upward or downward, check now to bump us out of the ground
		int onGround = onGround(originalY);
		
		/* ------- Not on the ground, so start pulling downward ---------- */
		if ( onGround == -1) {
			// if the current velocity has not yet hit terminal
			if (currentVelocity.precisionY() <= GameConstants.TERMINAL_VELOCITY ) {
				
				// Jumps should be smoother
				// Nitpick: Bonzo falls slower if he jumps first. Not sure if this is a good idea.
				if (isJumping)  currentVelocity.translateYFine(-0.4);
			    else 			currentVelocity.translateYFine(-1);
			}
		/* --------- On the ground. Keep y Velocities at zero ---------- */
		} else {
			currentVelocity.setY(0);
			// if pushing us up takes us OFF the ground, don't do it. Sloppy Kludge
			currentLocation.translateY(-onGround); //Push back to level field.
			if (onGround() == -1)  currentLocation.translateY(onGround);
			// If we are jumping when we land, move sprite to post-jump stage and stop jumping
			if (isJumping)  currentSprite = 3;
			setJumping(false);
		} 
		
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
				// Only reverse the velocity if we are actually already going up. If we are falling and somehow still
				// inside a solid, don't reverse again or we get bouncing through ceilings.
				if (currentVelocity.y() < 0)  currentVelocity.reverseY();
			}
		}

		
	}
	
	public void paint(Graphics2D g2d) {
		// If dying, that overrides everything.
		if (isDying) {
			// TODO respect offset request of DeathAnimation enumeration.
			ImmutablePoint2D deathStart = deathAnimation.deathStart();
			ImmutablePoint2D deathSize = deathAnimation.deathSize();
			int yOffset = deathStart.y() + (deathSize.y() * (currentSprite / deathAnimation.framesPerRow() ) );
			int xOffset = deathSize.x() * (currentSprite % deathAnimation.framesPerRow() );
			g2d.drawImage(CoreResource.INSTANCE.getBonzoSheet(), currentLocation.x(), currentLocation.y(),  //DEST
						  currentLocation.x() + deathSize.x(), currentLocation.y() + deathSize.y(), // DEST2
						  xOffset, yOffset, xOffset + deathSize.x(), yOffset + deathSize.y(),
						  null);
			return;
		}
		// if walking right
		int takeFromX = currentSprite * BONZO_SIZE.x();
		if (!isJumping) {
			g2d.drawImage(CoreResource.INSTANCE.getBonzoSheet(), currentLocation.x(), currentLocation.y(),  //DEST
						  currentLocation.x() + BONZO_SIZE.x(), currentLocation.y() + BONZO_SIZE.y(), // DEST2
						  takeFromX, walkingDirection * 40, takeFromX + BONZO_SIZE.x(), (walkingDirection * 40) + 40,
						  null);
		} else if (isJumping) {
			// if we are jumping to the left, we have to go 8 * 40 to the right to get to the right sprite level
			if (walkingDirection == 1)
				takeFromX += JUMP_LEFT_X;
			g2d.drawImage(CoreResource.INSTANCE.getBonzoSheet(), currentLocation.x(), currentLocation.y(),  //DEST
						  currentLocation.x() + BONZO_SIZE.x(), (int)currentLocation.y() + BONZO_SIZE.y(), // DEST2
						  takeFromX, JUMP_Y, takeFromX + BONZO_SIZE.x(), JUMP_Y + 40,
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
	 * Returns the actual mutable point representing Bonzo's position. This method should be used with care in the smallest
	 * possible scope. Clients should never hold a reference to the returned point.
	 * 
	 * @return
	 */
	public Point2D getMutableCurrentLocation() {
		return this.currentLocation;
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
		return ImmutableRectangle.of(this.currentLocation.x(), this.currentLocation.y(), BONZO_SIZE.x(), BONZO_SIZE.y());
	}
	
	
}
