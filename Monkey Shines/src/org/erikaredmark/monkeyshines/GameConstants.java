package org.erikaredmark.monkeyshines;


/**
 * 
 * Repository of game constants used by the engine for drawing and running the game.
 * 
 * @author Erika Redmark
 *
 */
public final class GameConstants {
	
	private GameConstants() { }
	
	// How many frames occur per second in terms of BOTH graphic redraw AND game logic update. In this game,
	// redrawing and update are done on the same frame. It's a simple game and the FPS isn't that fast so this
	// really shouldn't be an issue.
	// The 'actual' frames per second may be slightly different, as the sleep time is calculated from this and
	// may not divide into 1000 evenly. For non-divisors of 1000, this ends up being an approximation.
	public static final int FRAMES_PER_SECOND = 42;
	
	/*
	 * Items that are made final are hard coded into the engine and would disrupt the game if they were changed
	 */
	public static final int SPRITE_SIZE_X = 40; // The size of all sprite's width
	public static final int SPRITE_SIZE_Y = 40; // The size of all sprite's height
	public static final int SPRITES_IN_ROW = 8; // The number of sprites in the row.
	public static final int TILE_SIZE_X = 20; // The size of a tile.
	public static final int TILE_SIZE_Y = 20;
	public static final int TILE_SIZE_X_HALF = TILE_SIZE_X / 2; // Used for some collision calculations
	public static final int TILE_SIZE_Y_HALF = TILE_SIZE_Y / 2;
	public static final int TILES_IN_ROW = 32; // every row has 32 tiles
	public static final int TILES_IN_COL = 20; // every coloum has 20 rows
	public static final int TOTAL_TILES = TILES_IN_ROW * TILES_IN_COL;
	public static final int GOODIE_SIZE_X = 20;
	public static final int GOODIE_SIZE_Y = 20;
	
	/* Terminal velocity must be at least one minus the verticle tile size, or hit ground collision calculations will fail.
	 */
	public static final double TERMINAL_VELOCITY = 14.0;
	
	// Notes
	// 1) Bonzos speed multiplier combined with any speed increases (such as conveyers) must not exceed 10. If it does,
	//	  collision detection in terms of 'snapping' him against walls will instead bounce him back.
	// 2) If the total speed ever exceeds 20 the game becomes basically broken. Don't do that.
	public static final int BONZO_SPEED_MULTIPLIER = 2; // The speed Bonzo dashes across the landscape
	public static final double BONZO_JUMP_MULTIPLIER = (double)FRAMES_PER_SECOND / 39; // The force of bonzo's jump
	
	// User selects an integer value (0 and up) for a sprite's 'speed', and that value is multiplied by this to get the actual
	// pixel speed. This is always set such that a sprite of 'speed' 2 will match Bonzo's speed.
	// Sprite movement can NOT be a double. It must be an integer. Sprites moving left and right must be able to traverse
	// the same distance in the same amount of time (truncation screws that up) so they remain synced up if the author
	// intended them to be so.
	public static final int SPEED_MULTIPLIER = BONZO_SPEED_MULTIPLIER / 2;
	
	// The number of pixels, per tick, to move Bonzo when he is under the effect of a conveyer belt.
	public static final double CONVEYER_SPEED = (double)FRAMES_PER_SECOND / 45.0; 
	
	// Bonzo accelerates downward until terminal velocity
	// The acceleration bonzo falls during a jump. Significantly slower than normal.
	public static final double BONZO_FALL_ACCELERATION_JUMP = -((double)FRAMES_PER_SECOND / 210.0); 
	// The acceleration bonzo falls normally
	public static final double BONZO_FALL_ACCELERATION_NORMAL = -((double)FRAMES_PER_SECOND / 42.0);

	// The number of pixels into bonzos sprite to start a fall. Bigger numbers make bonzo
	// fall earlier (he can't tiptoe to the edge of his normal bounding box)
	public static final int FALL_SIZE = 4;

	// Number of screens 'remembered' as bonzo moves between screens. If respawn algorithms can't find a safe
	// place on the current screen, they back up to other screens.
	public static final int SCREEN_HISTORY = 5;
	
	// Bonus tick speed handles how many milliseconds between countdown ticks. Once the last red key is grabbed,
	// this ticks down at this speed.
	public static final int BONUS_COUNTDOWN_DELAY = 700;
	
	// Powerup lasts this many milliseconds before STARTING TO FADE.
	public static final int MAX_POWERUP_TIME = 20 * 1000;
	public static final int MAX_WARNINGS = 4;
	// Milliseconds between flashes. This * 2 gives the time for each warning sound effect
	public static final int TIME_BETWEEN_FLASHES = 500; 
	
	// When a world is first created and no bonus doors are added, it is given this default
	// as bonus screen id.
	public static final int DEFAULT_BONUS_SCREEN = 10000;
	// The return location when exiting a bonus door on the bonus screen
	public static final int DEFAULT_RETURN_SCREEN = 1000;
	
	/* -------------------------- Health --------------------------- */
	// Total units of health bonzo starts with
	public static final int HEALTH_MAX = 100;
	// Number of ticks bonzo can be not standing on ground (longer for coming from a jump)
	public static final int SAFE_FALL_TIME = (int)((double)FRAMES_PER_SECOND / 3.2);
	// The number used here (until I decide a better way to auto calculate) is effectively the amount
	// of time bonzo spends doing a complete arc from start of jump to bottom of jump before he falls more
	// than he rose.
	public static final int SAFE_FALL_JUMP_TIME = SAFE_FALL_TIME + 46;
	
	// Once bonzo passes the threshold for time, the number of ticks he is passed the
	// threshold is put to the POWER of this value (important this be greater than 1).
	// Bonzo damage from high heights should be exponential.
	public static final double FALL_DAMAGE_MULTIPLIER = 1.8;
	// Units of health per TICK that are drained when bonzo is touching a
	// health draining sprite. This effect is NOT cumulative (touching 2 health
	// drainers at once is the same as touching 1)
	public static final int HEALTH_DRAIN_PER_TICK = 2; 
	
	// Amount of life recovered when an energy thingy is taken.
	public static final int LIFE_INCREASE = 15; 

	public static final int BONUS_SCREEN = 10000; // The screenID of the bonus world; or where the bonus door takes one too.

	
	public static final int WINDOW_WIDTH = 640;
	public static final int WINDOW_HEIGHT = 480;
	
	public static final int SCREEN_DRAW_X = 0;
	public static final int SCREEN_DRAW_Y = 0;
	public static final int SCREEN_WIDTH = TILE_SIZE_X * TILES_IN_ROW;
	public static final int SCREEN_HEIGHT = TILE_SIZE_Y * TILES_IN_COL;
	public static final int UI_HEIGHT = 80;
	
	// Directions
	public static final int LEFT = 1;
	public static final int UP= 2;
	public static final int RIGHT = 3;
	public static final int DOWN = 4;
	public static final int CENTRE = -1;
	
	// Speed
	// Number of milliseconds that make up a 'tick' of gameplay. Lower numbers mean faster gameplay and animation,
	// and bigger numbers mean slower. This is automatically calculated from frames per second
	public static final int GAME_SPEED = 1000 / FRAMES_PER_SECOND;
	
	// Originally editor was to use different speed. It is, however, easier to place sprites and get a feel for their
	// movement with the same speed as game speed. The new features that pause the sprites when editing them makes
	// the need for a 'slower' editor speed moot.
	public static final int EDITOR_SPEED = GAME_SPEED;
	
	// 10 to the power of 0 through 7. Used for digit calculation when drawing the score sprites
	// index is the 'exponent', and result is the calculation. Base is 10.
	public static final int[] TEN_POWERS = new int[] {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000};


}
