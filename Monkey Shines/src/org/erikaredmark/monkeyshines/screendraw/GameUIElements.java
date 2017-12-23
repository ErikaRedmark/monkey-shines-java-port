package org.erikaredmark.monkeyshines.screendraw;

import org.erikaredmark.monkeyshines.GameConstants;

/**
 * Constants for where parts of the UI of the game should be drawn.
 * This does not contain actually graphical sprite data. It is merely where 
 * something should be, not what it should look like.
 * @author Goddess
 */
public class GameUIElements {
	public static final int SURFACE_SIZE_X = 640;
	public static final int SURFACE_SIZE_Y = 480;
	
	// Drawing location to start drawing the health bar.
	public static final int HEALTH_DRAW_X = 241;
	public static final int HEALTH_DRAW_Y = 50;
	public static final int HEALTH_DRAW_WIDTH = 151;
	public static final int HEALTH_DRAW_HEIGHT = 14;
	public static final int HEALTH_DRAW_Y2 = HEALTH_DRAW_Y + HEALTH_DRAW_HEIGHT;
	
	// Used to map the 'logical' health to the 'width' of the health bar.
	// Bonzos health will be converted to double and extended/contracted by this multplier to get draw width.
	public static final double HEALTH_MULTIPLIER = (double)HEALTH_DRAW_WIDTH / (double)GameConstants.HEALTH_MAX;
	
	// Score draw x/y is the top left location of the FIRST, leftmost digit.
	public static final int SCORE_DRAW_X = 13;
	public static final int SCORE_DRAW_Y = 32;
	public static final int SCORE_WIDTH = 16;
	public static final int SCORE_HEIGHT = 30;
	// Precomputation of effectively a constant
	public static final int SCORE_DRAW_Y2 = SCORE_DRAW_Y + SCORE_HEIGHT;
	
	public static final int INFINITY_DRAW_X = 582; 
	public static final int INFINITY_DRAW_Y = 29;
	public static final int INFINITY_DRAW_X2 = 626;
	public static final int INFINITY_DRAW_Y2 = 65;
	public static final int INFINITY_WIDTH = 44;
	public static final int INFINITY_HEIGHT = 36;
	
	public static final int LIFE_DRAW_X = 595;
	public static final int LIFE_DRAW_Y = 33;
	// Width and height are same as score width/height, as numerals are same
	// size.
	public static final int LIFE_DRAW_X2 = LIFE_DRAW_X + SCORE_WIDTH;
	public static final int LIFE_DRAW_Y2 = LIFE_DRAW_Y + SCORE_HEIGHT;

	public static int BONUS_DRAW_X = 152;
	// Bonus draw Y is same as score; same y level
	// widths and height same as score
	
	// POWERUPS
	public static final int POWERUP_DRAW_X = 418;
	public static final int POWERUP_DRAW_Y = 37;
	public static final int POWERUP_DRAW_X2 = POWERUP_DRAW_X + GameConstants.GOODIE_SIZE_X;
	public static final int POWERUP_DRAW_Y2 = POWERUP_DRAW_Y + GameConstants.GOODIE_SIZE_Y;
}
