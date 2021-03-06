package org.erikaredmark.monkeyshines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.erikaredmark.monkeyshines.Conveyer.Rotation;
import org.erikaredmark.monkeyshines.resource.SoundManager;
import org.erikaredmark.monkeyshines.tiles.CollapsibleTile;
import org.erikaredmark.monkeyshines.tiles.ConveyerTile;
import org.erikaredmark.monkeyshines.tiles.TileType;
import org.erikaredmark.monkeyshines.util.GameEndCallback;
import org.erikaredmark.util.collection.RingArray;

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
	public static final ImmutablePoint2D BONZO_SIZE_HALF = ImmutablePoint2D.of(20, 20);
	
	private static final int JUMP_SPRITES = 8;
	private static final int JUMP_Y = 80;
//	private static final int JUMP_RIGHT_X = 0;
	private static final int JUMP_LEFT_X = JUMP_SPRITES * BONZO_SIZE.x();
	
	// Sprite Info
	private int walkingDirection; // used during walking for determing what to blit.
								  // 1 for left, 0 for right.
	private int currentSprite; // used everywhere for whatever sprite he is on for animation
	
	/* ----------------------- location and history ------------------------ */
	private int currentScreenID;
	private Point2D currentLocation;
	private final RingArray<LevelScreen> screenHistory;
	
	
	/* ----------------------------- powerups ------------------------------ */
	// Initially set to null (no powerup). When bonzo grabs powerups this will be set until a
	// degrade timer unsets it after a delay
	private Powerup currentPowerup;
	// Internal stuff powerup decay. NO SCHEDULED THREADS because we don't want the game
	// loop to use multiple threads.
	private PowerupState powerupState;
	
	/* ----------------------------- velocity ------------------------------ */
	// Velocity applied to bonzo. Affected by the keyboard and falls.
	private Point2D currentVelocity;
	
	// Health goes down from long falls or certain health draining sprites
	// health is bounded from 0 to GameConstants.HEALTH_MAX
	private int health;
	
	// Bonzo starts with some set amount of lives. Once that reaches zero, death is no longer
	// cheap. 
	// If this is set to -2, bonzo has infinite lives. -1 means he's dead Jim
	private int lives;
	private final GameEndCallback gameEndCallback;
	public static final int INFINITE_LIVES = -2;
	
	// Score starts at zero and goes up until the game is over.
	// And yes, score is a property of the character, not the world.
	private int score;
	private final Runnable scoreCallback;
	
	// Use a pointer to the current world to get information such as the screen, and then from there where bonzo
	// is relative to the screen.
	private World worldPointer;
	
	private boolean freeze;
	
	
	/* **********************************************
	 * 
	 * State variables
	 * All states, except dying, can co-exist together. Hence, they are just booleans and not a more defined
	 * state machine.
	 * 
	 * **********************************************/
	// when :up: is hit, velocity is applied upwards. As long as jumping is yes and he has positive velocity and does NOT
	// have a jetpack, slowly decrease it until it reachs the max limit (MAX_FALL_SPEED)
	// Once there is ground below bonzo, stop jumping
	private boolean isJumping;
	
	// incremented every tick bonzo is not on the ground. When he lands, depending on whether he jumped
	// or not, this will be used to calculate fall damage, if any.
	// If bonzo is the air for so long that this overflows... well that's crazy level design.
	private int timeInAir;
	
	// When bonzo is on a conveyer, he is moved in one direction or the other based on which way the
	// conveyer is rotated.
	private Rotation affectedConveyer;
	
	// When dying, everything is overridden, and bonzo is reset.
	private boolean isDying;
	// The current animation to use for bonzo dying. This field is only used when going through
	// death animations and is only updated when bonzo is killed.
	private DeathAnimation deathAnimation;
	
	private Consumer<Bonzo> lifeLostCallback;
	
	/* **********************************************
	 * 
	 * Animation data
	 * ticks to next frame means how many ticks before advancing bonzo's sprite sheet.
     * this is NOT the same as ticks between updates. Updates happen every tick (collisions)
	 * but advancing the sprite sheet is slower.
	 * 
	 * **********************************************/
	private int ticksToNextFrame;
	private static final int TICKS_BETWEEN_FRAMES = 0;
	// Once bonzo lands, he is back in his original state, EXCEPT the animation needs to play the
	// jumping frames backwards. 
	private boolean unJumping;
	
	/**
	 * 
	 * Creates bonzo for use in the game
	 * 
	 * @param worldPointer
	 * 		reference to main world for collision detection purposes
	 * 
	 * @param startingLives
	 * 		bonzo starts with this many lives. After losing all of them the game is over. Bonzo
	 * 		can only have a max of 9 lives.
	 * 
	 * @param scoreCallback
	 * 		UI callback to use when bonzos score is updated (to reflect on GUI)
	 * 
	 * @param gameOverCallback
	 * 		UI callback for when the game is 'over', when bonzo loses all his lives
	 * 
	 * @param lifeLostCallback
	 * 		function called when bonzo loses a life, but it is NOT game over. Takes a reference
	 * 		to bonzo. NOT called if a life lost would result in a game over. If bonzo has infinite lives,
	 * 		this is still called to handle if bonzo had lost a life and had to respawn (the lives counter just
	 * 		won't be modified)
	 * 
	 */ 
	public Bonzo(final World worldPointer,
			     final int startingLives, 
			     final Runnable scoreCallback, 
			     final GameEndCallback gameEndCallback,
			     final Consumer<Bonzo> lifeLostCallback) {
		
		this.worldPointer = worldPointer;
		this.scoreCallback = scoreCallback;
		this.lives = startingLives;
		this.lifeLostCallback = lifeLostCallback;
		this.powerupState = new PowerupState();
		this.powerupState.clear();
		this.gameEndCallback = gameEndCallback;
		this.health = GameConstants.HEALTH_MAX;
		currentScreenID = 1000; // Always 1000. Everything starts on 1000
		final LevelScreen currentScreen = worldPointer.getScreenByID(currentScreenID);
		
		// Initialise Variables
		walkingDirection = 0;
		currentSprite = 0;
		affectedConveyer = Rotation.NONE;
		screenHistory = new RingArray<>(GameConstants.SCREEN_HISTORY);
		
		
		// Initialise starting points
		ImmutablePoint2D start = currentScreen.getBonzoStartingLocationPixels();
		currentLocation = Point2D.from(start);
		currentScreen.setBonzoCameFrom(BonzoSaveState.fromPoint(start) );
		
		currentVelocity = Point2D.of(0, 0);
		
		restartBonzoOnScreen(currentScreen, currentScreen.getBonzoCameFrom() );
		
	}
	/**
	 * 
	 * Restarts bonzo on the given screen at the given starting location. 
	 * 
	 * @param screen
	 * 
	 * @param startingLocation
	 * 
	 */
	public void restartBonzoOnScreen(final LevelScreen screen, BonzoSaveState startingLocation) {
		// The World events take care of moving Bonzo around, and Bonzo has methods to swap his position
		// on screen when moving between them.
		Point2D newLocation = Point2D.of(startingLocation.x, startingLocation.y);
		currentLocation = newLocation;
		currentVelocity.setX(startingLocation.velX);
		currentVelocity.setY(startingLocation.velY);
		// Not adding the current screen to history is deliberate. 
		currentScreenID = screen.getId();
		setJumping(startingLocation.jumping);
		if (startingLocation.rotation != Rotation.NONE)
			{ setAffectedByConveyer(startingLocation.rotation); }
		
		// When bonzo is restarted, he is not dead and the unjumping animation should not play
		setDying(false, this.deathAnimation);
		setUnjumping(false);
		
		// Bring health back
		this.health = GameConstants.HEALTH_MAX;
	}
	
	/**
	 * 
	 * Changes the current screen to the new id
	 * <p/>
	 * It is the responsibility of the caller to set Bonzos new location properly
	 * 
	 * @param newScreen
	 */
	public void changeScreen(final int newScreen) {
		// keep history. We only commit to history when moving OFF a screen, so the current
		// screen is not part of the history.
		screenHistory.pushFront(worldPointer.getScreenByID(currentScreenID) );
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
	 * 
	 * returns how much health bonzo has remaining. This returns '0' if his health drops below zero.
	 * 
	 * @return
	 * 
	 */
	public int getHealth() {
		return   health > 0
			   ? health
			   : 0;
	}
	
	/**
	 * 
	 * Adds to bonzos health. Bonzo loses health over the course of normal gameplay; this is
	 * intended for special circumstances, like energy goodies. If this would otherwise
	 * put bonzo above {@code GameConstants.HEALTH_MAX}, bonzo is simply set to maximum.
	 * <p/>
	 * If assertions are enabled, negative values cause errors.
	 * 
	 */
	public void incrementHealth(int amt) {
		assert amt >= 0;
		int newHealth = this.health + amt;
		this.health =   newHealth < GameConstants.HEALTH_MAX
					  ? newHealth
					  : GameConstants.HEALTH_MAX;
	}
	
	/**
	 * 
	 * Returns the number of lives Bonzo has remaining. If this is -2 that translates to 'Infinite' {@code Bonzo.INFINITE_LIVES}
	 * 
	 * @return
	 */
	public int getLives() {
		return lives;
	}
	
	/**
	 * 
	 * Increments bonzos score by the specified amount. Bonzos score can never decrease in-game.
	 * Calling this indicates to the UI that there is a new score and that the UI should be updated.
	 * 
	 * @param amt
	 * 		the amount to increase by
	 * 
	 */
	public void incrementScore(int amt) {
		this.score += amt;
		scoreCallback.run();
	}
	
	/**
	 * 
	 * Increments bonzos life count by the specified amount. Bonzo is capped at 9
	 * lives and will not go further.
	 * <p/>
	 * If assertions are enabled, errors will be fired if negative values are passed.
	 * External code is not allowed to decide when bonzo loses a life.
	 * <p/>
	 * Incrementing does not affect bonzo if he has infinite lives.
	 * 
	 * @param amt
	 * 		number of lives to add. If this amount would otherwise push him over 9
	 * 		lives he is kept at 9
	 * 
	 */
	public void incrementLives(int amt) {
		if (this.lives == INFINITE_LIVES)  return;
		
		assert amt >= 0;
		int newLives = this.lives + amt;
		this.lives =   newLives < 9
					 ? newLives
					 : 9;
	}
	
	public int getScore() { return this.score; }
	
	/**
	 * 
	 * Returns the screen history up to {@code GameConstants.LEVEL_HISTORY}.
	 * 
	 * @return
	 */
	public RingArray<LevelScreen> getScreenHistory() {
		return screenHistory;
	}
	
	/**
	 * 
	 * Called upon collision with the exit door. This effectively is part of the 
	 * control flow that ends the game.
	 * 
	 */
	void hitExitDoor() {
		gameEndCallback.gameOverWin(worldPointer);
	}
	

	/**
	 * Determines if bonzo has hit the ground. Intended ONLY to be called if bonzo is currently in a jump state. If he
	 * hits the ground, speed considerations may make it possible for him to go through the ground a couple units. The returned
	 * value indicates how far to 'bump' bonzo up if he goes through the ground too far. This should only be called once
	 * per tick and result used and/or stored. It does modify tile state in some cases.
	 * <p/>
	 * <strong> The speed bonzo is falling must NOT exceed one minus the verticle size of the tile!</strong> Otherwise
	 * he will end up being bumped up to the next tile down, being inside a solid. Terminal velocity should never reach above
	 * that amount.
	 * <p/>
	 * This method does not modify any major state OF BONZO and merely returns a value. The value contains how far bonzo must be
	 * pushed up, a rotation amount if bonzo is on a conveyer, and any collapsable tiles that may need collapsing if
	 * bonzo is on the ground properly. This method modifies ONE minor state variable; the fall assist
	 * <p/>
	 * The parameters for original position are required to determine, for dealing with thru blocks, if bonzo fell onto
	 * the block, or if he was already inside of it. 
	 * 
	 * @param originalPositionY
	 * 		bonzo's original y position before changing. Used to determine if he fell on a block. For thrus, this means
	 * 		preventing him from snapping on top if he just missed it and is now inside it.
	 * 
	 * @return
	 * 		Ground state object; primary value is the snapUpBy value:
	 * 	    {@code -1} if not on the ground, other a positive value indicating how deep into the ground bonzo is. This may
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
		int bonzoSizeXHalf = BONZO_SIZE_HALF.x();
		
		final TileMap map = currentScreen.getMap();
		
		grounds[0] = map.getTileXYPixel(currentLocation.x() + GameConstants.FALL_SIZE, bonzoOneBelowFeetY);
		grounds[1] = map.getTileXYPixel(currentLocation.x() + bonzoSizeXHalf, bonzoOneBelowFeetY);
		grounds[2] = map.getTileXYPixel(currentLocation.x() + bonzoSizeXHalf + 1, bonzoOneBelowFeetY);
		grounds[3] = map.getTileXYPixel(currentLocation.x() + (BONZO_SIZE.x() - 1 - GameConstants.FALL_SIZE), bonzoOneBelowFeetY );

		// State variables for later in the method. We want to loop over the tile types returned only one time.
		Rotation onConveyer = Rotation.NONE;
		boolean atLeastThru = false;
		boolean atLeastGround = false;
		
		// Because we are looking at four positions, with a max of three unique tiles and possibly two,
		// there may be repeats. We can't use == to check due to stateless tile types but the repeats 
		// are required checking for any code modifying tile state.
		TileType pastTile = null;
		List<CollapsibleTile> mayCollapse = new ArrayList<>(4);
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
			
			if (t.isThru() ) {
				atLeastThru = true;
				atLeastGround = true;
			}
			
			if (t.isSolid() )  atLeastGround = true;
			
			// If on a collapsing tile, save it. It will be returned as part of
			// the ground state for collapsing (should only do so if bonzo is on the tile
			if (t != pastTile && t instanceof CollapsibleTile) {
				mayCollapse.add((CollapsibleTile)t);
			}
			
			pastTile = t;
		}
		
		// Check #2; bonzo may be INSIDE the ground. Determine if he is and how much to snap him up by.
		// If at least part of him is on a thru tile.
		// Thrus differ from solids; he could jump up into a thru. We must handle that case. Solids are more simple.
		if (atLeastThru) {
			// If bonzo is already exactly on the ground, everything is fine. Otherwise, we may have to fall through it.
			int depth = (bonzoOneBelowFeetY - 1) % GameConstants.TILE_SIZE_Y;
			if (depth == 0)  return new GroundState(0, onConveyer, mayCollapse);
			
			// Very important! If we are inside of a thru, we do NOT bounce onto it unless bonzo's original position was
			// ABOVE the thru. Otherwise, it is too easy for him to snap up if a jump didn't quite make it.
			// This takes bonzos original location, and compares it to the current location. If he came from above they will
			// snap to different tiles. We look at the TOP of bonzo, but because he is 40x40 and divides evenly into tile
			// sizes, if his top points snap to different tiles, so would his bottom points.
			// One exception. If bonzos original location was RIGHT ON the tile border at the y point, it still counts as a
			// landing (this is for hitting a ceiling with a thru right below), thus the - 1 fudge factour
			if ( (originalPositionY - 1) / GameConstants.TILE_SIZE_Y == currentLocation.y() / GameConstants.TILE_SIZE_Y) {
				

				return new GroundState(-1, onConveyer, mayCollapse);
				// Effectively, if we snap the original position and the current position and we end up at the same tile, then
				// we approached it from the side, not above.
			}
			
			// Done. Thrus always toggle 'at least ground' which will run next if statement for calculating 'bounce up'
			// effect. This if statement was just to prevent bounce up if he shouldn't bounce up.
		}
		
		if (atLeastGround) {
			//We need to make sure that we are exactly on the thing, we don't budge it.
			// bonzoOneBelowFeet - 1 gives us bottom position of bonzo. Special case for when this
			// variable is aligned % = 0, it means bonzo is already on the ground. Return 0 for those
			// instances to prevent snapping up a full tile.
			if (bonzoOneBelowFeetY % GameConstants.TILE_SIZE_Y == 0)  return new GroundState(0, onConveyer, mayCollapse);
			else {
				return new GroundState( (bonzoOneBelowFeetY - 1) % GameConstants.TILE_SIZE_Y, onConveyer, mayCollapse);
			}
		}
		
		return new GroundState(-1, onConveyer, mayCollapse);
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
		public final List<CollapsibleTile> mayCollapse;
		
		private GroundState(final int snapUpBy, final Rotation onConveyer, final List<CollapsibleTile> mayCollapse) {
			this.snapUpBy = snapUpBy;
			this.onConveyer = onConveyer;
			this.mayCollapse = Collections.unmodifiableList(mayCollapse);
		}
		
		// Immutable singleton for when bonzo is rising, not falling.
		private static final GroundState RISING = new GroundState(-1, Rotation.NONE, Collections.<CollapsibleTile>emptyList() );
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
		// We give a little 'lee way', we don't check the very top or bottom, but a little off the extremes.
		// This allows Bonzo to fit easily into 2 space open passageways and then the ground snap algorithms
		// can take effect.
		final TileMap map = worldPointer.getCurrentScreen().getMap();
		if (   map.getTileXYPixel(newX, currentLocation.y() + 4 ).isSolid()
		    || map.getTileXYPixel(newX, currentLocation.y() + BONZO_SIZE.y() - 1 - 4).isSolid()
			|| map.getTileXYPixel(newX, currentLocation.y() + BONZO_SIZE_HALF.y() ).isSolid() ) {
			
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
		TileType[] above = new TileType[6];
		// The two 'early' middle points will never refer to the same tile, but may refer to different tiles
		// from extreme edge.
		// Two early middles being Open but others showing a solid will activate the special case, snapping
		// bonzo in place.
		// Two interior middles are to make sure bonzo can't jump through a single solid block above him.
		// 0,5 = extremes
		// 1,4 = exterior 'middles' intended for snapping special case
		// 2,3 == truly middle, middles, intended to make sure there is no single solid block
		// 		  hiding.
		final TileMap map = currentScreen.getMap();
		above[0] = map.getTileXYPixel(currentLocation.x(), newY);
		above[1] = map.getTileXYPixel(currentLocation.x() + 2, newY );
		above[2] = map.getTileXYPixel(currentLocation.x() + BONZO_SIZE_HALF.x(), newY ); // prefers left tile snap
		above[3] = map.getTileXYPixel(currentLocation.x() + BONZO_SIZE_HALF.x() + 1, newY ); // prefers right tile snap
		above[4] = map.getTileXYPixel(currentLocation.x() + (BONZO_SIZE.x() - 1) - 2, newY );
		above[5] = map.getTileXYPixel(currentLocation.x() + (BONZO_SIZE.x() - 1), newY );
		
		boolean atLeastSolid = false;
		for (TileType t : above) {
			if (t.isSolid())  atLeastSolid = true;
		}
		
		// No solids no problem
		if (!(atLeastSolid) )  return false;
		
		// Solids? Check our special case. If we can't use that then it is a solid wall.
		if (   !(above[1].isSolid() )
			&& !(above[2].isSolid() )
			&& !(above[3].isSolid() )
		    && !(above[4].isSolid() ) ) {
			
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
	 * Hurts bonzo by the given amount, draining his health. If it drops below zero during this call, bonzo
	 * is killed. Note that invincibility will be ignored; being hurt whilst invincible implies fall damage,
	 * which invincibility should not be protecting from.
	 * <p/>
	 * The thing hurting bonzo must pass in a death animation enum indicating what death type should be used
	 * if bonzo was to die from being hurt. Damage from fall is eliminated when in possesion of a wing
	 * powerup.
	 * 
	 * @param amt
	 * 		amount to hurt bonzos health
	 * 
	 * @param animation
	 * 		if bonzo does die, this is the animation that should be used
	 * 
	 */
	public void hurt(int amt, DamageEffect effect, SoundManager sound) {
		if (effect == DamageEffect.FALL && (currentPowerup != null && currentPowerup.isWing() ) )  return;
		if (effect == DamageEffect.BEE && (currentPowerup != null && currentPowerup.isShield() ) )  return;
		
		health -= amt;
		sound.playOnce(effect.soundEffect);
		if (health < 0)  kill(effect.deathAnimation, sound);
	}
	
	/**
	 * 
	 * Tries to kill bonzo. This takes into account if bonzo if invincible: if he is, does
	 * nothing. Otherwise, kills bonzo.
	 * <p/>
	 * this should not be used with the wing powerup, as falling from a high height and losing
	 * health should always kill bonzo. Wing powerup needs to simply prevent fall damage.
	 * <p/>
	 * If bonzo would otherwise explode, this also plays the explosion sound but still does not
	 * kill him
	 * 
	 * @param
	 * 		if bonzo does die, use this death animation
	 * 
	 */
	public void tryKill(DeathAnimation animation, SoundManager sound) {
		if (currentPowerup == null || !(currentPowerup.isShield() ) ) {
			kill(animation, sound);
		}
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
	public void kill(DeathAnimation animation, SoundManager sound) {
		currentVelocity.setX(0);
		currentVelocity.setY(0);
		currentSprite = 0;
		timeInAir = 0;
		currentPowerup = null;
		powerupState.clear();
		setDying(true, animation);
		sound.playOnce(animation.soundEffect() );
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
		// no zombies
		if (isDying || freeze)  return;
		// If we are not jumping, unjumping, or dying, increment the sprite
		// basically, as long as no other state is controlling animation, animate.
		if (   !(isJumping) 
			&& !(isDying) 
			&& !(unJumping) ) {
			
			if (readyToAnimate() ) {
				currentSprite++;
				if (currentSprite >= 16)
					currentSprite = 0;
			}
		}

		// Check if there is a solid. If not, walk there. Otherwise, we need to snap
		// him against the solid denying movement but allowing him to hug it. Otherwise
		// it will be impossible for the player to get aligned for jumping 
		double newX = currentLocation.precisionX() + ( velocity * GameConstants.BONZO_SPEED_MULTIPLIER );
		this.walkingDirection = velocity < 0 ? 1 : 0; // 1 for left, 0 for right.
		int solidCheck =   this.walkingDirection == 1
						 ? (int)newX
						 : ((int)newX) + Bonzo.BONZO_SIZE.x();
						 
		if (!solidToSide(solidCheck) )  currentLocation.setX(newX);
		else							snapBonzoX();
		
		// Once bonzo lands, he only has a few moves where he can be close to the edge. this doesn't prevent him from
		// falling, just allows him to get a few pixels closer to the edge.
		// if (fallAssistGrace > 0)  --fallAssistGrace;
	}
	
	/**
	 * Prevents bonzo from responding to move commands. Set by game world during certain
	 * states where movement should be ignored.
	 * @param b
	 */
	public void freeze(boolean frz) {
		this.freeze = frz;
	}
	
	public boolean isFrozen() 
		{ return this.freeze; }
	
	/**
	 * 
	 * Snaps bonzo's X position to be aligned with the tiles, such that % the size of a tile
	 * is zero. This is intended for hitting walls. See warning for bonzo speed in GameConstants.
	 * <p/>
	 * The snapping is done via whatever tile column he is closest to.
	 * 
	 */
	private void snapBonzoX() {
		// We divide by tile size X later to 'snap'. We add one to that result if bonzo was
		// closer to the OTHER side (his remainder was greater than halfway there)
		int rounding =   this.currentLocation.x() % GameConstants.TILE_SIZE_X < 10
					   ? 0
					   : GameConstants.TILE_SIZE_X;
		int locationNormalised = (this.currentLocation.x() / GameConstants.TILE_SIZE_X) * GameConstants.TILE_SIZE_X;
		this.currentLocation.setX(locationNormalised + rounding);
	}
	
	/**
	 * 
	 * Snaps bonzo's Y position to be aligned with the tiles. This is typically used when bonzo
	 * hits the ceiling to prevent him from going slightly 'thru' the ceiling, which completely
	 * ruins the snapping algorithms that the standard 'move' method uses.
	 * 
	 */
	private void snapBonzoY() {
		int rounding =   this.currentLocation.y() % GameConstants.TILE_SIZE_Y < 10
				   	   ? 0
				       : GameConstants.TILE_SIZE_Y;
		
		int locationNormalised = (this.currentLocation.y() / GameConstants.TILE_SIZE_Y) * GameConstants.TILE_SIZE_Y;
		this.currentLocation.setY(locationNormalised + rounding);
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
	 * Sets bonzos state to jumping. This only affects unjump state by disabling it if
	 * it was on already. It does not do the reverse (auto toggling it).
	 * 
	 * @param jumping
	 * 
	 */
	private void setJumping(boolean jumping) {
		this.isJumping = jumping;
		if (unJumping)  setUnjumping(false);
		
		// Jumping from ground toggles the safe bonzo ground state for the screen
		if (jumping) {
			worldPointer.getScreenByID(currentScreenID).setBonzoLastOnGround(getCurrentLocation() );
		}
	}
	
	private void setUnjumping(boolean unjump) {
		this.unJumping = unjump;
		if (unJumping) {
			// Sprite 6 is the last jump sprite, so the 'first' for unjumping
			this.currentSprite = 6;
		} else {
			// This is the first normal move sprite bonzo will be set to
			// This differs when moving left vs right due to how the sprite sheet is designed.
			// When moving right, as he falls his arms need to swing back, not towards the front.
			if (walkingDirection == 0) this.currentSprite = 6; // right
			else					   this.currentSprite = 0; // left 
			
		}
	}
	
	/**
	 * 
	 * Indicates that bonzo has collected a powerup. This is the entry point for giving Bonzo all
	 * powerups; class internally handles scheduling time to expire rules and overriding current powerups
	 * if any exist.
	 * 
	 * @param powerup
	 * 		the poweup bonzo collected. May not be {@code null}
	 * 
	 */
	public void powerupCollected(Powerup powerup) {
		assert powerup != null : "Cannot collect a null powerup!";
		
		currentPowerup = powerup;
		powerupState = new PowerupState();
	}
	
	/**
	 * 
	 * Returns bonzos current powerup, or {@code null} if bonzo does not have one
	 * 
	 * @return
	 * 		current powerup or {@code null}
	 * 
	 */
	public Powerup getCurrentPowerup() {
		return currentPowerup;
	}

	/**
	 * 
	 * Returns if the powerup is visible in the UI. A powerup is not visible if either
	 * 1) Bonzo has no powerup
	 * 2) Bonzo's powerup is fading and it is in a 'flash off' phase.
	 * 
	 * @return
	 * 		{@code true} if a powerup exists that should be drawn, {@code false} otherwise.
	 * 
	 */
	public boolean powerupUIVisible() {
		if (currentPowerup == null)  return false;
		// Because of the way the flashCount is implemented in the state, odd numbers (which is what
		// it starts with) represents a visible powerup, where as even numbers including zero represent
		// show nothing.
		return powerupState.flashCount % 2 == 1;
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
	/**
	 * Runs a single update tick on Bonzo, using the provided sound manager for any sounds that may
	 * play in the update.
	 * @param sound
	 */
	public void update(SoundManager sound) {
		// If we are dying, animate the sprite and do nothing else
		if (isDying) {
			if (readyToAnimate() ) {
				currentSprite++;
				if (currentSprite >= 15) {
					// Death animation over. If lives remain, restart bonzo. Otherwise UI callback
					if (lives != INFINITE_LIVES) {
						--lives;
					}
					// Equality because -2 is infinite
					assert lives >= -2 : "Lives can be -2 (infinite) -1 (dead) and 0-9, but never less than -2";
					if (lives == -1) {
						gameEndCallback.gameOverFail(worldPointer);
					} else {
						lifeLostCallback.accept(this);
					}
				}
				return;
			}
		}
		
		int originalY = currentLocation.y();
		currentLocation.applyVelocity(currentVelocity);
		// At this time, we could be inside of a block. Since by game nature velocity is only either
		// upward or downward, check now to bump us out of the ground
		GroundState groundState = onGround(originalY);
		
		// Set to true if bonzo was jumping or falling previously before hitting the ground.
		boolean landed = false;
		
		/* ------- Not on the ground, so start pulling downward ---------- */
		// Conveyer state is MAINTAINED.
		if ( groundState.snapUpBy == -1) {
			++timeInAir;
			// if the current velocity has not yet hit terminal
			if (currentVelocity.precisionY() <= GameConstants.TERMINAL_VELOCITY ) {
				
				if (isJumping)  currentVelocity.translateYFine(GameConstants.BONZO_FALL_ACCELERATION_JUMP);
			    else 			currentVelocity.translateYFine(GameConstants.BONZO_FALL_ACCELERATION_NORMAL);
			}
		/* --------- On the ground. Keep y Velocities at zero ---------- */
		// Conveyer state is modified to whatever the ground state says it should.
		} else {
			currentVelocity.setY(0);
			currentLocation.translateY(-groundState.snapUpBy); //Push back to level field.
			// If we are jumping when we land, start the 'unjump' animation. Also, set
			// the 'threshold' for falling time for fall calculations
			int fallThreshold;
			if (isJumping) {
				setJumping(false);
				setUnjumping(true);
				fallThreshold = GameConstants.SAFE_FALL_JUMP_TIME;
				// Landing on ground makes this a safe respawn if required ONLY if it doesn't kill bonzo.
				// We set a boolean that will toggle him being on the ground only if after health calculations
				// he is still alive.
				landed = true;
			} else {
				fallThreshold = GameConstants.SAFE_FALL_TIME;
			}
			
			// Apply fall damage if bonzo was beyond the threshold.
			int airDifference = timeInAir - fallThreshold;
			//System.out.println(timeInAir + " - " + fallThreshold + " = " + airDifference);
			//System.out.println(airDifference);
			if (airDifference > 0) {
				// Do not fear the casts: Cast airDifference to double to apply multiplier, then back to int to
				// get discrete units of damage to apply.
				hurt(
					(int)((Math.pow( ((double)airDifference), 
						GameConstants.FALL_DAMAGE_MULTIPLIER))), 
					DamageEffect.FALL,
					sound);
			}
			
			// If he is still alive, go ahead and set ground state
			if (landed && !(isDying) ) {
				worldPointer.getScreenByID(currentScreenID).setBonzoLastOnGround(getCurrentLocation() );
			}
			
			// Whether fall damage or not, bonzo no longer in air.
			timeInAir = 0;
			
			// Set conveyer belt state
			setAffectedByConveyer(groundState.onConveyer);
			
			// We landed, so any collapsibles must collapse.
			for (CollapsibleTile c : groundState.mayCollapse) {
				c.collapse();
			}
		} 
		
		/* ------------------------ Collision Checking --------------------------- */
		// Give all collisions to World
		worldPointer.checkCollisions(this, sound);
		
		/* ---------------------------- Jump Logic ------------------------------- */
		// if we are jumping, slowly increment the sprite until we get to the end, then leave it there.
		if (isJumping) {
			// check for a tile above us
			if (!solidToUp(currentLocation.y() ) ) {
				if (currentSprite < 7)  ++currentSprite;
				
			} else {
				// We did hit a solid
				// Only reverse the velocity if we are actually already going up. If we are falling and somehow still
				// inside a solid, don't reverse again or we get bouncing through ceilings.
				if (currentVelocity.y() < 0) {
					currentVelocity.reverseY();
					snapBonzoY();
				}
				
			}
		}
		
		// If landing from a jump, we are in an unjumping state; sole goal is to play sprites backwards.
		if (unJumping) {
			// Technically, sprite 0 is the end, but if we go up to it, we have 1 frame of bad animation.
			// Sprite 0 will be set with unjumping becoming false.
			if (currentSprite > 0)  --currentSprite;
			else				    setUnjumping(false);
		}
		
		/* ----------------------------- Conveyers ------------------------------ */
		
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
		
		/* ------------------------------ Powerup Decay ------------------------------ */
		powerupState.update(sound);
		
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
	 * Returns a point representing bonzos current velocity. The returned point is immutable and
	 * represents a snapshot of his velocity when the method was called
	 * @return
	 */
	public ImmutablePoint2D getCurrentVelocity() {
		return ImmutablePoint2D.from(currentVelocity);
	}
	
	/** Returns if bonzo is currently in a jump state. */
	public boolean isJumping() { return isJumping; }
	
	/** Returns any current conveyer effects bonzo is under */
	public Rotation getCurrentConveyerEffect() { return affectedConveyer; }
	/**
	 * 
	 * Returns the actual mutable point representing Bonzo's position. This method should be used with care in the smallest
	 * possible scope. Clients should never hold a reference to the returned point.
	 * <p/>
	 * This reference is <strong> not </strong> guaranteed to always remain valid with respect to Bonzo.
	 * 
	 * @return
	 * 		mutable location
	 * 
	 */
	public Point2D getMutableCurrentLocation() {
		return this.currentLocation;
	}
	
	
	/**
	 * 
	 * Explicitly sets bonzos current location on the screen based on the immutable point
	 * 
	 * @param location
	 * 
	 */
	public void setCurrentLocation(ImmutablePoint2D location) {
		this.currentLocation = Point2D.from(location);
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

	/**
	 * 
	 * Returns the exact point in the sprite sheet bonzo's current frame of animation is.
	 * <p/>
	 * It is an error to call this method whilst bonzo is dying, as the sprite frame is no longer guaranteed
	 * to be 40x40, and bonzo shouldn't be colliding with anything after already having been colliding with
	 * thing already.
	 *  
	 * @return
	 * 		location in sprite sheet that is currently the source frame of bonzos animation at the time
	 * 		of this call
	 * 
	 */
	public ImmutablePoint2D getDrawLocationInSprite() {
		if (isDying)  throw new IllegalStateException("Can't get 40x40 draw location during a death animation");

		// if walking right
		int takeFromX = currentSprite * BONZO_SIZE.x();
		// Standard Drawing
		if (!(isJumping) && (!unJumping) ) {
			return ImmutablePoint2D.of(takeFromX, walkingDirection * 40);
		// Jump/Unjump drawing
		} else {
			// if we are jumping to the left, we have to go 8 * 40 to the right to get to the right sprite level
			if (walkingDirection == 1)  takeFromX += JUMP_LEFT_X;
			return ImmutablePoint2D.of(takeFromX, JUMP_Y);
		}
	}

	/**
	 * 
	 * Encapsulates the state of a powerup. When a powerup is collected, it goes through several states. First
	 * is normal; it simply is there and has an effect. After a given number of ticks, it transitions into
	 * warning state, where it flashes visible and invisible for a given number of ticks. Finally the powerup
	 * is removed from bonzo.
	 * <p/>
	 * If at any time a new powerup is collected, the current state object will be destroyed and replaced with
	 * a new one. This effectively means bonzo may only carry one powerup at a time.
	 * 
	 * @author Erika Redmark
	 *
	 */
	private final class PowerupState {

		
		// Convert game speed (in game constants) to number of ticks to apporximate the speed
		private static final int START_TICKS = GameConstants.MAX_POWERUP_TIME / GameConstants.GAME_SPEED;
		private static final int FLASH_TICKS = GameConstants.TIME_BETWEEN_FLASHES / GameConstants.GAME_SPEED;
		// + 1 is for the initial decrement of flashCount when ticksForDegrade first hits zero.
		private static final int TOTAL_FLASHES = (GameConstants.MAX_WARNINGS * 2) + 1;
		
		// Set to a value and decremented until zero when a powerup is active.
		private int ticksForDegrade;
		private int flashCount;
		private boolean done;
		
		// Creates a new powerup state instance. Since powerup information is stored in bonzo, this only
		// initialises timing relevant data.
		private PowerupState() {
			// Ticks to degrade is re-used after hitting zero; it resets itself
			// and decrements flash count. Each decrement changes a special state in
			// bonzo that changes whether the powerup should be drawn even if it is still
			// active. When both hit zero, the state is 'finished'
			
			// Note: we are converting 
			ticksForDegrade = START_TICKS;
			flashCount = TOTAL_FLASHES;
			done = false;
		}
		
		// Updates the state. If the state reaches final state (powerup completely faded)
		// has the side effect of setting bonzos current powerup to null
		private void update(SoundManager sound) {
			if (done)  return;
			
			if (ticksForDegrade <= 0) {
				if (flashCount <= 0) {
					done = true;
					currentPowerup = null;
					return;
				}
				
				--flashCount;
				ticksForDegrade = FLASH_TICKS;
				// Play warning sound for each even flash count
				if (flashCount % 2 == 0)  sound.playOnce(GameSoundEffect.POWERUP_FADE);
			}
			
			--ticksForDegrade;
		}
		
		// Puts this state into the end state promptly. Only used by constructor for initial state.
		private void clear() {
			ticksForDegrade = 0;
			flashCount = 0;
			done = true;
		}
	}


	/** Intended for drawing routines. Returns if bonzo is currently going through the dying
	 * animation but has not yet died. */
	public boolean isDying() { return isDying; }
	public DeathAnimation getDeathAnimation() { return deathAnimation; }
	/** Id of the current sprite in Bonzo's animation sprite sheet. 
	 *  The semantics of what this means depends on the current animation bonzo is in. */
	public int getCurrentSprite() { return currentSprite; }
	// Intialises immutable singleton to be used as inital state (when there is no powerup)
	// so calling update does nothing. After bonzo collects his first powerup, this won't be
	// used anymore (all powerup states naturally decay to this state)
	
}
