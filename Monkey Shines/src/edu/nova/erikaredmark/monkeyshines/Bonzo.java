package edu.nova.erikaredmark.monkeyshines;

import java.awt.Graphics2D;

import edu.nova.erikaredmark.monkeyshines.Conveyer.Rotation;
import edu.nova.erikaredmark.monkeyshines.resource.CoreResource;
import edu.nova.erikaredmark.monkeyshines.tiles.ConveyerTile;
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
	
	// When bonzo is on a conveyer, he is moved in one direction or the other based on which way the
	// conveyer is rotated.
	private Rotation affectedConveyer;
	
	// When dying, everything is overridden, and bonzo is reset.
	private boolean isDying;
	// The current animation to use for bonzo dying. This field is only used when going through
	// death animations and is only updated when bonzo is killed.
	private DeathAnimation deathAnimation;
	
	// Animation data
	// ticks to next frame means how many ticks before advancing bonzo's sprite sheet.
	// this is NOT the same as ticks between updates. Updates happen every tick (collisions)
	// but advancing the sprite sheet is slower.
	private int ticksToNextFrame;
	private static final int TICKS_BETWEEN_FRAMES = 0;
	
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
		affectedConveyer = Rotation.NONE;
		
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
	public GroundState onGround() {
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
	 * This method does not modify any state and merely returns a value. The value contains how far bonzo must be
	 * pushed up as well as a boolean indicating if any of the ground he landed on contained a conveyer belt.
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
	public GroundState onGround(int originalPositionY) {
		// If rising, not falling, no need to check for ground. In fact, we are allowed to go through certain ground.
		if (currentVelocity.precisionY() < 0)  return GroundState.RISING;
		
		LevelScreen currentScreen = worldPointer.getCurrentScreen();
		int bonzoOneBelowFeetY = (currentLocation.y() + BONZO_SIZE.y() ) + 1;
		// Four points, each point 'snaps' to a tile. We need to check two centres, otherwise it is possible for bonzo
		// to be flanked by emptiness, be right in the middle of a solid block, and fall through.
		TileType[] grounds = new TileType[4];
		int bonzoSizeXHalf = BONZO_SIZE.x() / 2;
		grounds[0] = currentScreen.getTileAt(currentLocation.x() + 3, bonzoOneBelowFeetY);
		grounds[1] = currentScreen.getTileAt(currentLocation.x() + bonzoSizeXHalf, bonzoOneBelowFeetY);
		grounds[2] = currentScreen.getTileAt(currentLocation.x() + bonzoSizeXHalf + 1, bonzoOneBelowFeetY);
		grounds[3] = currentScreen.getTileAt(currentLocation.x() + (BONZO_SIZE.x() - 4), bonzoOneBelowFeetY );

		// State variables for later in the method. We want to loop over the tile types returned only one time.
		Rotation onConveyer = Rotation.NONE;
		boolean atLeastThru = false;
		boolean atLeastGround = false;
		
		for (TileType t : grounds) {
			if (t instanceof ConveyerTile) {
				if (onConveyer == Rotation.NONE)  onConveyer = ((ConveyerTile) t).getConveyer().getRotation();
				else {
					// Are the rotations the same? If not, choose one to take precedence according to the
					// following order
					// 1) Bonzo's current conveyer rotation state variable IF NOT NONE
					// 2) The first conveyer selected (which would be the leftmost one)
					Rotation newRotation = ((ConveyerTile) t).getConveyer().getRotation();
					if (onConveyer != newRotation) {
						if (this.affectedConveyer != Rotation.NONE)  onConveyer = this.affectedConveyer;
						// else don't change the current conveyer.
					}
				}
			}
			if (t.isThru() )  atLeastThru = true;
			if (t == StatelessTileType.SOLID)  atLeastGround = true;
		}
		
		// Check #2; bonzo may be INSIDE the ground. Determine if he is and how much to snap him up by
		// If at least part of him is on a thru tile.
		// Thrus differ from solids; he could jump up into a thru. We must handle that case. Solids are more simple.
		if (atLeastThru) {
			// If bonzo is already exactly on the ground, everything is fine. Otherwise, we may have to fall through it.
			int depth = (bonzoOneBelowFeetY - 1) % GameConstants.TILE_SIZE_Y;
			if (depth == 0)  return new GroundState(0, onConveyer);
			
			// Very important! If we are inside of a thru, we do NOT bounce onto it unless bonzo's original position was
			// ABOVE the thru. Otherwise, it is too easy for him to snap up if a jump didn't quite make it.
			// We do not snap up at zero velocity only if bonzos original position was equal to or less than this position. 
			// Zero velocity with identical Y position means bonzo is not falling; he is standing, so he IS on the ground
			// exactly already.
			if (originalPositionY / GameConstants.TILE_SIZE_Y == currentLocation.y() / GameConstants.TILE_SIZE_Y) {
				
				return new GroundState(-1, onConveyer);
				// Effectively, if we snap the original position and the current position and we end up at the same tile, then
				// we approached it from the side, not above.
			}
			
			//We need to make sure that we are exactly on the thing, we don't budge it.
			return new GroundState( (bonzoOneBelowFeetY - 1) % GameConstants.TILE_SIZE_Y, onConveyer); 
			
		// Landing or on a solid.
		} else if (atLeastGround) {
			return new GroundState( (bonzoOneBelowFeetY - 1) % GameConstants.TILE_SIZE_Y, onConveyer);
		}
		
		return new GroundState(-1, onConveyer);
	}
	
	/** 
	 * 
	 * C-style struct for returning multiple values from the onGround method.
	 * 
	 * @author Erika Redmark
	 *
	 */
	private static final class GroundState {
		public final int snapUpBy;
		public final Rotation onConveyer;
		
		private GroundState(final int snapUpBy, final Rotation onConveyer) {
			this.snapUpBy = snapUpBy;
			this.onConveyer = onConveyer;
		}
		
		// Immutable singleton for when bonzo is rising, not falling.
		private static final GroundState RISING = new GroundState(-1, Rotation.NONE);
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
	 * was to try to jump. X locations taken from state.
	 * <p/>
	 * This method has a special case: If bonzo hits a solid on either the left or right BUT he is a few pixels
	 * off from jumping through an opening (at least two air tiles), this will automatically correct his X position
	 * and return that no solid is up. This is done ONLY when he is jumping and ONLY when he is only a few pixels off.
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
		TileType[] above = new TileType[4];
		// The two middle points will never refer to the same tile, but may refer to different tiles
		// from extreme edge.
		// Two middles being Open but others showing a solid will activate the special case, snapping
		// bonzo in place.
		above[0] = currentScreen.getTileAt(currentLocation.x(), newY);
		above[1] = currentScreen.getTileAt(currentLocation.x() + 2, newY );
		above[2] = currentScreen.getTileAt(currentLocation.x() + (BONZO_SIZE.x() - 1) - 2, newY );
		above[3] = currentScreen.getTileAt(currentLocation.x() + (BONZO_SIZE.x() - 1), newY );
		
		boolean atLeastSolid = false;
		for (TileType t : above) {
			if (t == StatelessTileType.SOLID)  atLeastSolid = true;
		}
		
		// No solids no problem
		if (!(atLeastSolid) )  return false;
		
		// Solids? Check our special case. If we can't use that then it is a solid wall.
		if (   above[1] != StatelessTileType.SOLID 
		    && above[2] != StatelessTileType.SOLID) {
			
			// Activate special case: Snap bonzo to nearest tile boundary, which should
			// be enough to line him up to move up.
			int bonzoNormalised = currentLocation.x() / GameConstants.TILE_SIZE_X;
			int rounding = currentLocation.x() % GameConstants.TILE_SIZE_X;
			
			// Techincally, he can ONLY be within about 2 pixels from the side, but to be safe
			// we just use the middle (tile size half) to determine whether to round up the
			// normalised position or keep it truncated.
			if (rounding > GameConstants.TILE_SIZE_X_HALF)  ++bonzoNormalised;
			
			// Apply change to location. remember the normalised position is a tile position!
			currentLocation.setX(bonzoNormalised * GameConstants.TILE_SIZE_X);
			
			// Nothing to above now!
			return false;
			
		} else {
			return true;
		}
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
	
	/**
	 * 
	 * Moves bonzo at the given velocity, augmented by GameConstants. Given velocity is normally
	 * a simple +/-1 multiplier to determine direction.
	 * 
	 * @param velocity
	 * 		velocity to move Bonzo
	 * 
	 */
	public void move(double velocity) {
		// If we are not jumping, increment the sprite
		if (!isJumping) {
			if (readyToAnimate() ) {
				currentSprite++;
				if (currentSprite >= 16)
					currentSprite = 0;
			}
		}
		
		//currentVelocity.x = velocity * GameConstants.BONZO_SPEED_MULTIPLIER;
		//only move if not a solid tile ahead
		
		double newX = currentLocation.precisionX() + ( velocity * GameConstants.BONZO_SPEED_MULTIPLIER );
		if (velocity < 0) {
			walkingDirection = 1;
			if (!solidToSide((int)newX) )
				currentLocation.setX(newX);
		} else {
			// Need to use right side of sprite
			int rightSide = ((int)newX) + Bonzo.BONZO_SIZE.x();
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
		// Conveyer state at this point is irrelevant. Update method handles that.
		GroundState groundState = onGround();
		if (groundState.snapUpBy != -1 && (currentVelocity.y() == 0)  ) {
			currentVelocity.setY(-(velocity * GameConstants.BONZO_JUMP_MULTIPLIER) );
			setJumping(true);
			currentSprite = 0;
		}
	}
	
	/**
	 * 
	 * Makes bonzo stop moving in terms of velocity. If bonzo is under the effects of a conveyer
	 * belt those effects will be removed.
	 * 
	 */
	public void stopMoving() {
		currentVelocity.setX(0);
		setAffectedByConveyer(Rotation.NONE);
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
	
	/**
	 * 
	 * Sets if bonzo is under the effects of a conveyer belt. Just as in the original game,
	 * this can overlap with jumping.
	 * <p/>
	 * Rotation indicates which direction bonzo moves. Rotation can be gathered from the conveyer tile
	 * during collision detection.
	 * 
	 * @param affectedConveyer
	 * 		The rotation of the conveyer belt affecting bonzo, or {@code Rotation.NONE} for no effects
	 * 
	 */
	private void setAffectedByConveyer(Rotation affectedConveyer) {
		assert affectedConveyer != null;
		this.affectedConveyer = affectedConveyer;
	}
	
	// We don't increment the sprite unless we are jumping or dying.
	public void update() {
		// If we are dying, animate the sprite and do nothing else
		if (isDying) {
			if (readyToAnimate() ) {
				currentSprite++;
				if (currentSprite >= 15) {
					// Death animation over. Restart bonzo. Also update drawing data
					worldPointer.restartBonzo(this);
				}
				return;
			}
		}
		
		int originalY = currentLocation.y();
		currentLocation.applyVelocity(currentVelocity);
		// At this time, we could be inside of a block. Since by game nature velocity is only either
		// upward or downward, check now to bump us out of the ground
		GroundState groundState = onGround(originalY);
		
		/* ------- Not on the ground, so start pulling downward ---------- */
		// Conveyer state is MAINTAINED.
		if ( groundState.snapUpBy == -1) {
			// if the current velocity has not yet hit terminal
			if (currentVelocity.precisionY() <= GameConstants.TERMINAL_VELOCITY ) {
				
				if (isJumping)  currentVelocity.translateYFine(GameConstants.BONZO_FALL_ACCELERATION_JUMP);
			    else 			currentVelocity.translateYFine(GameConstants.BONZO_FALL_ACCELERATION_NORMAL);
			}
		/* --------- On the ground. Keep y Velocities at zero ---------- */
		// Conveyer state is modified to whatever the ground state says it should.
		} else {
			currentVelocity.setY(0);
			// if pushing us up takes us OFF the ground, don't do it. Sloppy Kludge
			currentLocation.translateY(-groundState.snapUpBy); //Push back to level field.
			// If we are jumping when we land, move sprite to post-jump stage and stop jumping
			if (isJumping)  currentSprite = 3;
			setJumping(false);
			
			// Set conveyer belt state
			setAffectedByConveyer(groundState.onConveyer);
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
				// We did hit a solid
				// Only reverse the velocity if we are actually already going up. If we are falling and somehow still
				// inside a solid, don't reverse again or we get bouncing through ceilings.
				if (currentVelocity.y() < 0)  currentVelocity.reverseY();
			}
		}
		
		// If we are affected by a conveyer, we are moved. If we are moved into a wall, we move just enough
		// to touch it.
		double conveyerMovement = this.affectedConveyer.translationX();
		// required to have one loop handle two directions.
		double conveyerMultiplier =   conveyerMovement < 0.0
								 ? -1.0
								 : 1.0;
		// Find farthest point not in a solid block that we can move to.  Since double values
		// truncate to integer for actual position, we just look at truncated ints.
		// Loop does not execute if movement is 0
		// It is perfectly possible for no additional movement to be applied if against a wall.
		for (double i = Math.abs(conveyerMovement); i >= 0; i--) {
			double translation = i * conveyerMultiplier;
			double newX = this.currentLocation.precisionX() + translation;
			// Truncated value will be Bonzo's 'effective' location on the world at the given time.
			if (!(solidToSide((int)newX) ) ) {
				currentLocation.setX(newX);
				break;
			}
		}
		
		updateReadyToAnimate();
		
	}
	
	/**
	 * 
	 * Determines if bonzo's sprite should be updated, or if it must wait another tick.
	 * Calling this method does NOT update the tick count, unlike other objects that
	 * use a similiar trick. ticksToNextFrame is updated in the update method.
	 * 
	 * @return
	 */
	private boolean readyToAnimate() {
		return this.ticksToNextFrame >= TICKS_BETWEEN_FRAMES;
	}
	
	private void updateReadyToAnimate() {
		if (ticksToNextFrame >= TICKS_BETWEEN_FRAMES)  ticksToNextFrame = 0;
		else										   ++ticksToNextFrame;
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
