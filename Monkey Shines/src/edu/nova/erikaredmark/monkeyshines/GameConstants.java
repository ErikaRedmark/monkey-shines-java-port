package edu.nova.erikaredmark.monkeyshines;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.nova.erikaredmark.monkeyshines.bounds.IPoint2D;

/**
 * 
 * Repository of game constants used by the engine for drawing and running the game.
 * 
 * TODO Over time, refactor constants/logic into appropriate classes.
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
	public static final int TILES_IN_ROW = 32; // every row has 32 tiles
	public static final int TILES_IN_COL = 20; // every coloum has 20 rows
	public static final int GOODIE_SIZE_X = 20;
	public static final int GOODIE_SIZE_Y = 20;
	public static double MAX_FALL_SPEED = -7; // THIS MUST BE NEGATIVE
	public static double SPEED_MULTIPLIER = 1.0; // The speed of a sprite is multiplied by this to get how fast it moves across screen.
	public static double BONZO_SPEED_MULTIPLIER = 2.0; // The speed Bonzo dashes across the landscape
	public static double BONZO_JUMP_MULTIPLIER = 1.6; // The force of bonzo's jump
	public static double FRUIT_SCORE = 10.0; // Standard score for fruit.
	public static double YUMMY_FRUIT_SCORE = 50.0; // score for Yummy fruit.
	public static double EXIT_KEY_SCORE = 10.0; // Score for exit key
	public static double BONUS_KEY_SCORE = 10.0; // Score for bonus keys
	public static double LIFE_INCREASE = 25.0; // Amount of life recovered when an energy thingy is taken.
	public static int SAFE_FALL_DISTANCE = 80; // Number of pixels fall before Bonzo starts taking damage.
	public static double FALL_DAMAGE_MULTIPLIER = 2.0; // Number of pixels of distance fell times this is the damage bonzo will take.
	public static int BONUS_SCREEN = 10000; // The screenID of the bonus world; or where the bonus door takes one too.
	
	public static final int WINDOW_WIDTH = 640;
	public static final int WINDOW_HEIGHT = 480;
	
	public static final int SCREEN_DRAW_X = 0;
	public static final int SCREEN_DRAW_Y = 0;
	public static final int SCREEN_WIDTH = TILE_SIZE_X * TILES_IN_ROW;
	public static final int SCREEN_HEIGHT = TILE_SIZE_Y * TILES_IN_COL;
	
	// Directions
	public static final int LEFT = 1;
	public static final int UP= 2;
	public static final int RIGHT = 3;
	public static final int DOWN = 4;
	public static final int CENTRE = -1;
	
	// Speed
	public static final int GAME_SPEED = 30;
	
	public static int directionLeftTheScreen(final IPoint2D loc, final int width, final int height) {
		// Check to see if it is in bounds. If so, return -1.
		if (loc.x() < 0)
			return LEFT;
		else if (loc.y() < 0)
			return UP;
		else if (loc.x() + width > SCREEN_WIDTH )
			return RIGHT;
		else if (loc.y() + height > SCREEN_HEIGHT)
			return DOWN;
		else
			return CENTRE;
	}
	
	// Helper method for parsing XML
	public static int getIntValue(Element ele, String tagName) {
		//in production application you would catch the exception
		return Integer.parseInt(getTextValue(ele,tagName));
	}
	
	// Helper method for the helper method!!!
	public static String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) {
			Element el = (Element)nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}

		return textVal;
	}

}
