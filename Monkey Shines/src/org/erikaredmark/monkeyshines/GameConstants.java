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
	public static double TERMINAL_VELOCITY = 14.0;
	public static double SPEED_MULTIPLIER = 1.0; // The speed of a sprite is multiplied by this to get how fast it moves across screen.
	
	// Notes
	// 1) Bonzos speed multiplier combined with any speed increases (such as conveyers) must not exceed 10. If it does,
	//	  collision detection in terms of 'snapping' him against walls will instead bounce him back.
	// 2) If the total speed ever exceeds 20 the game becomes basically broken. Don't do that.
	public static double BONZO_SPEED_MULTIPLIER = 2.6; // The speed Bonzo dashes across the landscape
	public static double BONZO_JUMP_MULTIPLIER = 1.25; // The force of bonzo's jump
	
	// Bonzo accelerates downward until terminal velocity
	public static double BONZO_FALL_ACCELERATION_JUMP = -0.3; // The acceleration bonzo falls during a jump. Significantly slower than normal.
	public static double BONZO_FALL_ACCELERATION_NORMAL = -1.0; // The acceleration bonzo falls normally

	// The number of pixels into bonzos sprite to start a fall. Bigger numbers make bonzo
	// fall earlier (he can't tiptoe to the edge of his normal bounding box)
	public static int FALL_SIZE = 4;
	
	/* -------------------------- Health --------------------------- */
	// Total units of health bonzo starts with
	public static int HEALTH_MAX = 100;
	// Number of ticks bonzo can be not standing on ground (longer for coming from a jump)
	public static int SAFE_FALL_TIME = 13;
	public static int SAFE_FALL_JUMP_TIME = 46;
	
	// Once bonzo passes the threshold for time, the number of ticks he is passed the
	// threshold is put to the POWER of this value (important this be greater than 1).
	// Bonzo damage from high heights should be exponential.
	public static double FALL_DAMAGE_MULTIPLIER = 1.8;
	// Units of health per TICK that are drained when bonzo is touching a
	// health draining sprite. This effect is NOT cumulative (touching 2 health
	// drainers at once is the same as touching 1)
	public static int HEALTH_DRAIN_PER_TICK = 2; 
	
	// Amount of life recovered when an energy thingy is taken.
	public static double LIFE_INCREASE = 25.0; 
	
	/* -------------------------- Score ---------------------------- */
	public static double FRUIT_SCORE = 10.0; // Standard score for fruit.
	public static double YUMMY_FRUIT_SCORE = 50.0; // score for Yummy fruit.
	public static double EXIT_KEY_SCORE = 10.0; // Score for exit key
	public static double BONUS_KEY_SCORE = 10.0; // Score for bonus keys
	
	public static int BONUS_SCREEN = 10000; // The screenID of the bonus world; or where the bonus door takes one too.
	public static double CONVEYER_SPEED = 1.4; // The number of pixels, per tick, to move Bonzo when he is under the effect of a conveyer belt.
	
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
	// and bigger numbers mean slower.
	// Smaller numbers mean more ticks per second which means more fine control over how fast things are by allowing
	// certain things to wait for n ticks before performing an action.
	public static final int GAME_SPEED = 30; // 20 = 50 ticks per second
	public static final int EDITOR_SPEED = GAME_SPEED + 30;


}
