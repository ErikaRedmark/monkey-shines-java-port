package org.erikaredmark.monkeyshines.screendraw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;

import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.GameWorldLogic;
import org.erikaredmark.monkeyshines.Powerup;
import org.erikaredmark.monkeyshines.resource.WorldResource;


/**
 * 
 * Assumes a drawing size of 640x480 and draws the universe represented by the {@code GameWorldLogic} into that
 * size constraint. This only handles rendering the entire scene to an image, not how that image is then actually
 * painted to the screen.
 * <p/>
 * This surface has a 1:1 analog to the logical world. This means that a 20x20 unit tile is drawn at 20x20 pixels, no
 * graphics substitutions. Screen drawers that then draw the final graphics context on the screen may at their discretion
 * stretch or blow up the final result, however.
 * <p/>
 * The object itself is not drawn to anything, it merely provides the ability to render new images
 * that will be drawn to something.
 * 
 * TODO eventual goal is to have a HiDefSurface as well that uses larger graphics for a nicer looking game at a larger
 * size without compromising the gameplay (since the game logic assumes a 640x480 world).
 * 
 * @author Erika Redmark
 *
 */
public final class StandardSurface {
	
	private static final int SURFACE_SIZE_X = 640;
	private static final int SURFACE_SIZE_Y = 480;
	
	// Drawing location to start drawing the health bar.
	private static final int HEALTH_DRAW_X = 241;
	private static final int HEALTH_DRAW_Y = 50;
	private static final int HEALTH_DRAW_WIDTH = 151;
	private static final int HEALTH_DRAW_HEIGHT = 14;
	
	// Used to map the 'logical' health to the 'width' of the health bar.
	// Bonzos health will be converted to double and extended/contracted by this multplier to get draw width.
	private static final double HEALTH_MULTIPLIER = (double)HEALTH_DRAW_WIDTH / (double)GameConstants.HEALTH_MAX;
	
	// Color of health bar
	private static final Color HEALTH_COLOR = new Color(0, 255, 0, 255);
	
	// Score draw x/y is the top left location of the FIRST, leftmost digit.
	private static final int SCORE_DRAW_X = 13;
	private static final int SCORE_DRAW_Y = 32;
	private static final int SCORE_WIDTH = 16;
	private static final int SCORE_HEIGHT = 30;
	// Precomputation of effectively a constant
	private static final int SCORE_DRAW_Y2 = SCORE_DRAW_Y + SCORE_HEIGHT;

	
	private static final int LIFE_DRAW_X = 595;
	private static final int LIFE_DRAW_Y = 33;
	// Width and height are same as score width/height, as numerals are same
	// size.
	private static final int LIFE_DRAW_X2 = LIFE_DRAW_X + SCORE_WIDTH;
	private static final int LIFE_DRAW_Y2 = LIFE_DRAW_Y + SCORE_HEIGHT;

	private static int BONUS_DRAW_X = 152;
	// Bonus draw Y is same as score; same y level
	// widths and height same as score
	
	// POWERUPS
	private static final int POWERUP_DRAW_X = 418;
	private static final int POWERUP_DRAW_Y = 37;
	private static final int POWERUP_DRAW_X2 = POWERUP_DRAW_X + GameConstants.GOODIE_SIZE_X;
	private static final int POWERUP_DRAW_Y2 = POWERUP_DRAW_Y + GameConstants.GOODIE_SIZE_Y;
	
	private final GameWorldLogic universe;
	
	/**
	 * 
	 * Creates a new instance of a standard surface for the given world.
	 * 
	 * @param universe
	 * 
	 */
	public StandardSurface(final GameWorldLogic universe) {
		this.universe = universe;
	}
	/**
	 * 
	 * Draws the entire world onto a 640x480 image that will be stored in main memory, and
	 * returns the reference to that image. This is intended for windowed mode.
	 * 
	 */
	public BufferedImage renderBuffered() {
		BufferedImage page = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = page.createGraphics();
		try {
			renderToGraphics(g2d);
		} finally {
			g2d.dispose();
		}
		
		return page;
	}
	
	/**
	 * 
	 * Draws the entire world onto a 640x480 image that will be stored in video memory. 
	 * Because of this, the image returned may suddenly cease to exist at any point. If
	 * it does, this method must be called again before repainting. It is important that
	 * clients check for lost contents of the image returned before using it.
	 * 
	 * @param configuration
	 * 		graphics configuration for the current hardware
	 * 
	 */
	public VolatileImage renderVolatile(GraphicsConfiguration configuration) {
		VolatileImage page = configuration.createCompatibleVolatileImage(SURFACE_SIZE_X, SURFACE_SIZE_Y);
		
		do {
			Graphics2D g2d = page.createGraphics();
			try {
				renderToGraphics(g2d);
			} finally {
				g2d.dispose();
			}
		} while (page.contentsLost() );
		
		// Successful draw; return the image.
		return page;
	}
	
	/**
	 * 
	 * Renders the world to the graphics context. 
	 * 
	 * @param g2d
	 */
	private void renderToGraphics(Graphics2D g2d) {
		/* --------------------- Initial Banner ---------------------- */
		WorldResource rsrc = universe.getResource();
		g2d.drawImage(rsrc.getBanner(), 
					  0, 0,
					  GameConstants.SCREEN_WIDTH, GameConstants.UI_HEIGHT,
					  0, 0,
					  GameConstants.SCREEN_WIDTH, GameConstants.UI_HEIGHT,
					  null);
		
		/* ------------------------- Health -------------------------- */
		g2d.setColor(HEALTH_COLOR);
		// Normalise bonzo's current health with drawing.
		double healthWidth = ((double)universe.getBonzoHealth()) * HEALTH_MULTIPLIER;
		//System.out.println("Drawing rect: " + HEALTH_DRAW_X + " " + HEALTH_DRAW_Y + " " + (int)healthWidth + " " + HEALTH_DRAW_HEIGHT);
		g2d.fillRect(HEALTH_DRAW_X, HEALTH_DRAW_Y, (int)healthWidth, HEALTH_DRAW_HEIGHT);
		
		/* -------------------------- Score -------------------------- */
		for (int i = 0; i < GameWorldLogic.SCORE_NUM_DIGITS; i++) {
			int drawToX = SCORE_DRAW_X + (SCORE_WIDTH * i);
			// draw to Y is always the same
			int drawFromX = SCORE_WIDTH * universe.getScoreDigits()[i];
			// draw from Y is always the same, 0
			g2d.drawImage(rsrc.getScoreNumbersSheet(), 
						  drawToX, SCORE_DRAW_Y,
						  drawToX + SCORE_WIDTH, SCORE_DRAW_Y2, 
						  drawFromX, 0, 
						  drawFromX + SCORE_WIDTH, SCORE_HEIGHT, 
						  null);
		}
		
		/* -------------------- Bonus Countdown ---------------------- */
		for (int i = 0; i < GameWorldLogic.BONUS_NUM_DIGITS; i++) {
			int drawToX = BONUS_DRAW_X + (SCORE_WIDTH * i);
			// draw to Y is always the same
			int drawFromX = SCORE_WIDTH * universe.getBonusDigits()[i];
			// draw from Y is always the same, 0
			g2d.drawImage(rsrc.getBonusNumbersSheet(),
						  drawToX, SCORE_DRAW_Y,
						  drawToX + SCORE_WIDTH, SCORE_DRAW_Y2,
						  drawFromX, 0,
						  drawFromX + SCORE_WIDTH, SCORE_HEIGHT,
						  null);
		}
		
		/* ------------------------- Lives --------------------------- */
		{
			int lifeDigit = universe.getLifeDigit();
			if (lifeDigit >= 0) {
				assert lifeDigit < 10;
				int drawFromX = SCORE_WIDTH * lifeDigit;
				
				g2d.drawImage(rsrc.getScoreNumbersSheet(),
							  LIFE_DRAW_X, LIFE_DRAW_Y,
							  LIFE_DRAW_X2, LIFE_DRAW_Y2,
							  drawFromX, 0,
							  drawFromX + SCORE_WIDTH, SCORE_HEIGHT,
							  null);
			} // else draw nothing TODO perhaps draw infinity symbol?
		}
		
		/* ------------------------ Powerup --------------------------- */
		{
			if (universe.isPowerupVisible() ) {
				Powerup powerup = universe.getCurrentPowerup();
				assert powerup != null : "Powerup should be invisible if null";
				
				g2d.drawImage(rsrc.getGoodieSheet(),
						      POWERUP_DRAW_X, POWERUP_DRAW_Y,
						      POWERUP_DRAW_X2, POWERUP_DRAW_Y2,
						      powerup.drawFromX(), Powerup.POWERUP_DRAW_FROM_Y,
						      powerup.drawFromX2(), Powerup.POWERUP_DRAW_FROM_Y2,
						      null);
			}
		}
	}

}
