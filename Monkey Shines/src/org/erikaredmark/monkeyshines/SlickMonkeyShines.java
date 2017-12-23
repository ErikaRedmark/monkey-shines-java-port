package org.erikaredmark.monkeyshines;

import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.BONUS_DRAW_X;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.HEALTH_DRAW_X;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.HEALTH_DRAW_Y;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.HEALTH_DRAW_Y2;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.HEALTH_MULTIPLIER;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.INFINITY_DRAW_X;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.INFINITY_DRAW_X2;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.INFINITY_DRAW_Y;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.INFINITY_DRAW_Y2;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.INFINITY_HEIGHT;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.INFINITY_WIDTH;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.LIFE_DRAW_X;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.LIFE_DRAW_X2;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.LIFE_DRAW_Y;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.LIFE_DRAW_Y2;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.POWERUP_DRAW_X;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.POWERUP_DRAW_X2;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.POWERUP_DRAW_Y;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.POWERUP_DRAW_Y2;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.SCORE_DRAW_X;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.SCORE_DRAW_Y;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.SCORE_DRAW_Y2;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.SCORE_HEIGHT;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.SCORE_WIDTH;

import org.erikaredmark.monkeyshines.resource.CoreResource;
import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
/**
 * Begins running the actual game. This is typically started from the main menu, 
 * and takes control away from the Swing-based menu system.
 * <p/>
 * This class enforces that only one instance of the game may be running, in
 * case someone tries to get back to the main menu and start another world
 * whilst the main game engine is already processing one already.
 * @author Goddess
 */
public class SlickMonkeyShines extends BasicGame {

	/**
	 * Creates, but does not start, the primary object that will play the passed
	 * world. 
	 * <p/>
	 * The {@code WorldResource} must have been loaded using Slick2D compatible
	 * images, NOT bufferedImages from AWT.
	 * @throws IllegalArgumentException
	 * 		if the passed world's resource (graphics data) is using {@code BufferedImage}
	 * 		instead of Slick2D's image type. 
	 */
	public SlickMonkeyShines(World world) {
		super("Monkey Shines (Java Port)");
		this.universe = world;
		this.rsrc = world.getResource();
		
		if (!this.rsrc.isSlickGraphics())
		{
			throw new IllegalArgumentException("Cannot start game: wrong graphcis loaded (expected Slick, got AWT)");
		}
	}

	@Override public void init(GameContainer gc) throws SlickException {
		gc.setShowFPS(false);
	}

	@Override public void update(GameContainer gc, int delta) throws SlickException {
		// delta is ignored for Monkey Shines. The underlying game logic was never designed
		// with it in mind.
		universe.update();
	}

	@Override public void render(GameContainer gc, Graphics g2d) throws SlickException {
		/* --------------------- Initial Banner ---------------------- */
//		WorldResource rsrc = universe.getResource();
//		g2d.drawImage(rsrc.getBanner(), 
//					  0, 0,
//					  GameConstants.SCREEN_WIDTH, GameConstants.UI_HEIGHT,
//					  0, 0,
//					  GameConstants.SCREEN_WIDTH, GameConstants.UI_HEIGHT,
//					  null);
//		
//		/* ------------------------- Health -------------------------- */
//		// Normalise bonzo's current health with drawing.
//		double healthWidth = ((double)universe.getBonzoHealth()) * HEALTH_MULTIPLIER;
//		
//		g2d.drawImage(rsrc.getEnergyBar(),
//					  HEALTH_DRAW_X, HEALTH_DRAW_Y,
//					  HEALTH_DRAW_X + (int)healthWidth, HEALTH_DRAW_Y2,
//					  0, 0,
//					  (int)healthWidth, 10,
//					  null);
//		
//		/* -------------------------- Score -------------------------- */
//		for (int i = 0; i < GameWorldLogic.SCORE_NUM_DIGITS; i++) {
//			int drawToX = SCORE_DRAW_X + (SCORE_WIDTH * i);
//			// draw to Y is always the same
//			int drawFromX = SCORE_WIDTH * universe.getScoreDigits()[i];
//			// draw from Y is always the same, 0
//			g2d.drawImage(rsrc.getScoreNumbersSheet(), 
//						  drawToX, SCORE_DRAW_Y,
//						  drawToX + SCORE_WIDTH, SCORE_DRAW_Y2, 
//						  drawFromX, 0, 
//						  drawFromX + SCORE_WIDTH, SCORE_HEIGHT, 
//						  null);
//		}
//		
//		/* -------------------- Bonus Countdown ---------------------- */
//		for (int i = 0; i < GameWorldLogic.BONUS_NUM_DIGITS; i++) {
//			int drawToX = BONUS_DRAW_X + (SCORE_WIDTH * i);
//			// draw to Y is always the same
//			int drawFromX = SCORE_WIDTH * universe.getBonusDigits()[i];
//			// draw from Y is always the same, 0
//			g2d.drawImage(rsrc.getBonusNumbersSheet(),
//						  drawToX, SCORE_DRAW_Y,
//						  drawToX + SCORE_WIDTH, SCORE_DRAW_Y2,
//						  drawFromX, 0,
//						  drawFromX + SCORE_WIDTH, SCORE_HEIGHT,
//						  null);
//		}
//		
//		/* ------------------------- Lives --------------------------- */
//		{
//			int lifeDigit = universe.getLifeDigit();
//			if (lifeDigit >= 0) {
//				assert lifeDigit < 10;
//				int drawFromX = SCORE_WIDTH * lifeDigit;
//				
//				g2d.drawImage(rsrc.getScoreNumbersSheet(),
//							  LIFE_DRAW_X, LIFE_DRAW_Y,
//							  LIFE_DRAW_X2, LIFE_DRAW_Y2,
//							  drawFromX, 0,
//							  drawFromX + SCORE_WIDTH, SCORE_HEIGHT,
//							  null);
//			} else {
//				g2d.drawImage(CoreResource.INSTANCE.getInfinity(),
//							  INFINITY_DRAW_X, INFINITY_DRAW_Y,
//							  INFINITY_DRAW_X2, INFINITY_DRAW_Y2,
//							  0, 0,
//							  INFINITY_WIDTH, INFINITY_HEIGHT,
//							  null);
//			}
//		}
//		
//		/* ------------------------ Powerup --------------------------- */
//		{
//			if (universe.isPowerupVisible() ) {
//				Powerup powerup = universe.getCurrentPowerup();
//				assert powerup != null : "Powerup should be invisible if null";
//				
//				g2d.drawImage(rsrc.getGoodieSheet(),
//						      POWERUP_DRAW_X, POWERUP_DRAW_Y,
//						      POWERUP_DRAW_X2, POWERUP_DRAW_Y2,
//						      powerup.drawFromX(), Powerup.POWERUP_DRAW_FROM_Y,
//						      powerup.drawFromX2(), Powerup.POWERUP_DRAW_FROM_Y2,
//						      null);
//			}
//		}
//		
//		/* ----------------------- Actual Game -------------------------- */
//		// game is drawn at 80 pixels down if UI was drawn
//		g2d.translate(0, 80);
//		universe.paintTo(g2d);
//		g2d.translate(0, -80);
	}
	
	@Override public boolean closeRequested() {
		running = false;
		return true;
	}
	
	/**
	 * Starts the actual game either in fullscreen or not. Game always runs in 640x480 resolution
	 * internally, whether or not it is scaled visually.
	 * <p/>
	 * Returning false here indicates the game is already running and another instance won't start
	 * (so a user couldn't alt-tab to the other JFrame for the main menu and open another world).
	 * If the game is not running, this will return true indicating that <strong> it already ran</strong>.
	 * This is a blocking method... once called, control will not return until the user exits the game.
	 * <p/>
	 * More critical failures to run the game are found in the generated SlickException
	 * @param world
	 * 		the actual world to play
	 * @param fullScreen
	 * 		{@code true} to use fullscreen, {@code false} not to.
	 * @return
	 * @throws SlickException
	 */
	public boolean startMonkeyShines(World world, boolean fullScreen) 
		throws SlickException
	{
		if (running)
		{
			return false;
		}
		
		running = true;
		AppGameContainer bonzoContainer = new AppGameContainer(new SlickMonkeyShines(world));
		bonzoContainer.setDisplayMode(GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT, false);
		
		// This game was never set up with the ability to calculate things using a delta of time between
		// updating game logic. Easiest solution currently is to just clamp the speed to the exact speed it
		// should run, which shouldn't have a problem on modern systems given how simple the game is.
		bonzoContainer.setMinimumLogicUpdateInterval(GameConstants.GAME_SPEED);
		bonzoContainer.setTargetFrameRate(GameConstants.GAME_SPEED);
		
		bonzoContainer.start();
		
		return true;
	}
	
	private final World universe;
	private final WorldResource rsrc;
	
	// mutable variable to make sure a game isn't already running.
	private static boolean running = false;
	
}
