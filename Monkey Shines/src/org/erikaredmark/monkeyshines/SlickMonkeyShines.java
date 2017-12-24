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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.erikaredmark.monkeyshines.encoder.EncodedWorld;
import org.erikaredmark.monkeyshines.global.SpecialSettings;
import org.erikaredmark.monkeyshines.graphics.exception.ResourcePackException;
import org.erikaredmark.monkeyshines.resource.CoreResource;
import org.erikaredmark.monkeyshines.resource.PackReader;
import org.erikaredmark.monkeyshines.resource.SlickRenderer;
import org.erikaredmark.monkeyshines.resource.SlickWorldGraphics;
import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.erikaredmark.monkeyshines.util.GameEndCallback;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.ScalableGame;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.opengl.InternalTextureLoader;
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
	private static final String CLASS_NAME = "org.erikaredmark.monkeyshines.SlickMonkeyShines";
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
	
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
	public SlickMonkeyShines(UnloadedWorld world, KeyBindings keys) {
		super("Monkey Shines (Java Port)");
		this.frozenUniverse = world;
		this.keyBindings = keys;
	}

	public void setQuitAction(Runnable run)
		{ quit = run; }
	
	@Override public void init(GameContainer gc) throws SlickException {
		gc.setShowFPS(false);
		try
		{
			this.world = frozenUniverse.load();
			this.rsrc = this.world.getResource();
			this.slickGraphics = this.rsrc.getSlickGraphics();
			this.universe = new GameWorldLogic(
				this.world, 
				new GameOverHandler(), 
				SpecialSettings.isThunderbird());
			frozenUniverse.removeTemporaryFiles();
			frozenUniverse = null;
		} catch (ResourcePackException e) {
			throw new SlickException("Issue with world resource pack: " + e.getMessage(), e);
		}
	}

	@Override public void update(GameContainer gc, int delta) throws SlickException {
		// delta is ignored for Monkey Shines. The underlying game logic was never designed
		// with it in mind.
		universe.update();
	}

	@Override public void render(GameContainer gc, Graphics g) throws SlickException {
		SlickRenderer.paintUI(g, universe, slickGraphics);
		
		g.translate(0, 80);
		g.pushTransform();
		SlickRenderer.paintWorld(g, world);
		SlickRenderer.paintBonzo(g, universe.getBonzo(), slickGraphics);
		g.popTransform();
	}
	
	@Override public boolean closeRequested() {
		running = false;
		return true;
	}
	
	public class GameOverHandler implements GameEndCallback {
		@Override public void gameOverFail(World w) {
			// TODO Auto-generated method stub
			// Need to show failure screen
			quit.run();
		}

		@Override public void gameOverEscape(World w) {
			// TODO Auto-generated method stub
			// Just jump back to menu after a fade to black.
			quit.run();
		}

		@Override
		public void gameOverWin(World w) {
			// TODO Auto-generated method stub
			// Show winning screen
			quit.run();
		}
		
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
	public static boolean startMonkeyShines(UnloadedWorld world, KeyBindings keyBindings, boolean fullScreen) 
		throws SlickException
	{
		if (running)
		{
			return false;
		}
		
		running = true;
		SlickMonkeyShines monkeyShines = new SlickMonkeyShines(world,  keyBindings);
		
		AppGameContainer bonzoContainer = new AppGameContainer(
			new ScalableGame(
				monkeyShines,
				GameConstants.SCREEN_WIDTH, 
				GameConstants.SCREEN_HEIGHT + GameConstants.UI_HEIGHT));
		monkeyShines.setQuitAction(() -> bonzoContainer.exit());
		
		// TODO if fullscreen, set screen width and height to actual resolution of current monitor.
		bonzoContainer.setDisplayMode(
			GameConstants.SCREEN_WIDTH, 
			(GameConstants.SCREEN_HEIGHT + GameConstants.UI_HEIGHT), 
			fullScreen);
		
		// This game was never set up with the ability to calculate things using a delta of time between
		// updating game logic. Easiest solution currently is to just clamp the speed to the exact speed it
		// should run, which shouldn't have a problem on modern systems given how simple the game is.
		bonzoContainer.setMinimumLogicUpdateInterval(GameConstants.GAME_SPEED);
		bonzoContainer.setTargetFrameRate(GameConstants.FRAMES_PER_SECOND);
		bonzoContainer.setForceExit(false);
		
		bonzoContainer.start();
		
		bonzoContainer.destroy();
		
		// This is VERY important! If the texture cache is not cleared, then if the user
		// starts another game the textures from the previous game will collide with the textures
		// from the... it's basically a fucking mess. Comment this out to see something cool when
		// choosing another world but otherwise keep this in.
		InternalTextureLoader.get().clear();
		
		return true;
	}
	
	// Not set until init function; requires graphics resources that are not
	// available until then. Is not used until update method anyway.
	private GameWorldLogic universe;
	private World world;
	private WorldResource rsrc;
	private SlickWorldGraphics slickGraphics;
	// the data required to load the universe, before we actually load it.
	private UnloadedWorld frozenUniverse;
	private final KeyBindings keyBindings;
	
	// Forces the app container for this game to exit, since the rest of the game (main menus and such)
	// still operate under AWT And Swing.
	private Runnable quit;
	
	
	// mutable variable to make sure a game isn't already running.
	private static boolean running = false;
	
	/**
	 * Represents the parts needed to load the world, but has not loaded the world yet. Graphics
	 * data won't be available until the gl context is started so resource creation, and therefore world
	 * creation, must be deferred until then.
	 * <p/>
	 * deleteOnLoad should ONLY be true for internal worlds, as the resources are extracted to a temporary
	 * location! Putting this as true for custom worlds will delete them. 
	 * <p/>
	 * Feed this object into {@code startMonkeyShines} to actually start up the game engine and
	 * run the world.
	 */
	public static class UnloadedWorld {
		public UnloadedWorld(EncodedWorld enc, Path rsrcPck, boolean delOnLoad) {
			this.encodedWorld = enc;
			this.rsrcPack = rsrcPck;
			this.deleteOnLoad = delOnLoad;
		}
		
		/**
		 * Actually performs the loading of the world. Do not call this until the GL Context
		 * is available or the image loading will fail.
		 */
		public World load() throws ResourcePackException {
			WorldResource rsrc = PackReader.fromPackSlick(rsrcPack);
			return encodedWorld.newWorldInstance(rsrc);
		}
		
		/** Removes, if needed, temporary files created from loading an internal world. */
		public void removeTemporaryFiles() {
			if (deleteOnLoad) {
				try {
					Files.delete(rsrcPack);
					Files.delete(rsrcPack.getParent());
				} catch (IOException e) {
					LOGGER.log(Level.WARNING,
							   CLASS_NAME + ": Could not delete temporary files (should not affect gameplay) due to: " + e.getMessage(),
							   e);
				}
			}
		}
		
		public final EncodedWorld encodedWorld;
		public final Path rsrcPack;
		public final boolean deleteOnLoad;
	}
	
}
