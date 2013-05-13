package edu.nova.erikaredmark.monkeyshines;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GameConstants {
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
	
	/*public static final int INFORMATION_DRAW_Y = ;
	
	public static final int SCORE_DRAW_X = ;
	public static final int ENERGY_DRAW_X = ;
	public static final int POWERUP_DRAW_X = ;
	public static final int BONUS_DRAW_X = ;
	public static final int LIVES_DRAW_X = ;*/
	
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
	
	public static int directionLeftTheScreen(final Point2D loc, final int width, final int height) {
		// Check to see if it is in bounds. If so, return -1.
		if (loc.precisionX() < 0)
			return LEFT;
		else if (loc.precisionY() < 0)
			return UP;
		else if (loc.precisionX() + width > SCREEN_WIDTH )
			return RIGHT;
		else if (loc.precisionY() + height > SCREEN_HEIGHT)
			return DOWN;
		else
			return CENTRE;
	}
	
	// Takes any location and velocity vector, and adds the velocity to the location
	public static void moveUnit(final Point2D location, final Point2D velocity) {
		location.translateXFine(velocity.precisionX() );
		//using minus, because otherwise positive numbers go down and that makes no sense.
		location.translateYFine(-velocity.precisionY() );
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
	
	// TODO Refactor responsibility to ClippingRegion!! That's what is is there for!
	// Check for bounding box collision given a two points and widths of the sprites
	public static boolean checkBoundingBoxCollision(Point2D victim, Point2D target, int victimWidth, int victimHeight, int targetWidth, int targetHeight) {
		if (victim.precisionX() + victimWidth > target.precisionX() && 
				victim.precisionX() < target.precisionX() + targetWidth && 
				victim.precisionY() + victimHeight > target.precisionY() && 
				victim.precisionY() < target.precisionY() + targetWidth)
			return true;
		return false;
	}
}
