package edu.nova.erikaredmark.monkeyshines;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class Goodie {
	/**
	 * MAJOR TODO
	 * Must fix the fact that goodies appear on all screens.
	 */

	// The sheet that contains all the goodies
	public static BufferedImage goodieSheet;
	public static BufferedImage yumSheet;
	public static final int RED_KEY = 0;
	public static final int BLUE_KEY = 1;
	public static final int APPLE = 2;
	public static final int ORANGE = 3;
	public static final int PEAR = 4;
	public static final int PURPLE_GRAPES = 5;
	public static final int BLUE_GRAPES = 6;
	public static final int BANANA = 7;
	public static final int ENERGY = 8;
	public static final int X2MULTIPLIER = 9;
	public static final int WHITE_MELRODE_WINGS = 10;
	public static final int WINGS = 10;
	public static final int SHEILD = 11;
	public static final int EXTRA_LIFE = 12;
	public static final int X3MULTIPLIER = 13;
	public static final int X4MULTIPLIER = 14;
	// The id of the goodie
	
	final int goodieId;
	
	// The world xml file stores where on the screens the goodies are, because since screens are reconstructed as is
	// and goodies must dissapear, this is needed.
	
	final int screenID;
	final Point2D location;	
	
	// Taken means gone but animating the YUM. Dead means gone.
	boolean taken;
	boolean dead;
	int yumSprite;
	
	// Drawing info
	int drawToX;
	int drawToY;
	int drawX;
	int drawY;
	
	// Pointer to world. Needed for comparing against Bonzo's inventory
	World worldPointer;
	
	// Static initialisation: Goodie sheet is shared by all Goodie objects, and only one instance should exist.
	static {
		try {
			InputStream goodiePath = "".getClass().getResourceAsStream("/resources/graphics/objects.gif");
			InputStream yumPath = "".getClass().getResourceAsStream("/resources/graphics/yummies.gif");
		    goodieSheet = ImageIO.read(goodiePath);
		    yumSheet = ImageIO.read(yumPath);
		} catch (IOException e) {
			System.out.println("Quand est la bien?");
		}
	}
	
	/**
	 * Creates a goodie for the specified screen, for the specified world. 
	 * 
	 * @param worldPointer
	 * @param type
	 * @param currentLocation
	 * @param screenID
	 */
	public Goodie(final World worldPointer, final int type, final Point2D currentLocation, final int screenID) {
		// Type refers to both where in the sprite sheet this powerup is, and for what it does. These elements are hardcoded.
		this.screenID = screenID;
		this.location = Point2D.of(currentLocation);
		goodieId = type;
		taken = false;
		dead = false;
		yumSprite = -1; // The yumsprite will be set to zero before any drawing goes. This insures the first frame is not skipped.
		
		
		drawToX = (int)currentLocation.precisionX() * GameConstants.GOODIE_SIZE_X;
		drawToY = (int)currentLocation.precisionY() * GameConstants.GOODIE_SIZE_Y;
		
		drawX = type * GameConstants.GOODIE_SIZE_X;
		drawY = 0;
	}
	
	// Very simple animation.
	public void update() {
		if (!taken && !dead) {
			if (drawY == 0)
				drawY = GameConstants.GOODIE_SIZE_Y;
			else
				drawY = 0;
		} else if (taken && !dead) {
			yumSprite++;
			if (yumSprite >= 15) {
				dead = true;
			}
		}
	}
	
	public void take() {
		taken = true;
	}
	
	public int getScreenID() {
		return screenID;
	}
	
	/**
	 * Editor Functions
	 * @param g2d
	 */
	
	public static BufferedImage getGoodieSheet() {
		return goodieSheet;
	}
	
	public void paint(Graphics g2d) {
		if (!taken && !dead)
			g2d.drawImage(goodieSheet, drawToX , drawToY, // Destination 1
					drawToX + GameConstants.GOODIE_SIZE_X, drawToY + GameConstants.GOODIE_SIZE_Y, // Destination 2
					drawX, drawY, drawX + GameConstants.GOODIE_SIZE_X, drawY + GameConstants.GOODIE_SIZE_Y,
					null);
		else if (taken && !dead) {
			g2d.drawImage(yumSheet, drawToX , drawToY, // Destination 1
					drawToX + GameConstants.GOODIE_SIZE_X, drawToY + GameConstants.GOODIE_SIZE_Y, // Destination 2
					yumSprite * GameConstants.GOODIE_SIZE_X, 0, // Source 1
					yumSprite * GameConstants.GOODIE_SIZE_X + GameConstants.GOODIE_SIZE_X, GameConstants.GOODIE_SIZE_Y, // Source 2
					null);
		}
	}

	/**
	 * Returns the id of this goodie, representing what kind of goodie it is
	 * 
	 * @return
	 * 		goodie id
	 */
	public int getGoodieID() { return goodieId; }

	/**
	 * Returns a copy of the goodie's location. Modifications to the returned object will not affect the goodie in
	 * any way.
	 * 
	 * @return
	 * 		a new {@code Point2D} initialised at the location the goodie is on the screen.
	 */
	public Point2D getLocation() { return Point2D.of(location); }
	
}
