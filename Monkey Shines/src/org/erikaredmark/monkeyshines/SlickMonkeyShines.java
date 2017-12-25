package org.erikaredmark.monkeyshines;

import java.io.IOException;

import org.erikaredmark.monkeyshines.animation.GracePeriodAnimation;
import org.erikaredmark.monkeyshines.global.SoundSettings;
import org.erikaredmark.monkeyshines.global.SpecialSettings;
import org.erikaredmark.monkeyshines.graphics.exception.ResourcePackException;
import org.erikaredmark.monkeyshines.resource.InitResource;
import org.erikaredmark.monkeyshines.resource.SlickRenderer;
import org.erikaredmark.monkeyshines.resource.SlickWorldGraphics;
import org.erikaredmark.monkeyshines.resource.SoundManager;
import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.erikaredmark.monkeyshines.util.GameEndCallback;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.loading.DeferredResource;
import org.newdawn.slick.loading.LoadingList;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.FadeInTransition;
import org.newdawn.slick.state.transition.FadeOutTransition;
/**
 * Begins running the actual game. This is typically started from the main menu, 
 * and takes control away from the Swing-based menu system.
 * <p/>
 * This class enforces that only one instance of the game may be running, in
 * case someone tries to get back to the main menu and start another world
 * whilst the main game engine is already processing one already.
 * @author Goddess
 */
public class SlickMonkeyShines extends StateBasedGame {
	
	/* ------------------- Runnability ---------------------- */
	// Forces the app container for this game to exit, since the rest of the game (main menus and such)
	// still operate under AWT And Swing.
	private Runnable quit;
	
	/* ------------------- Sound Control -------------------- */
	private final SoundManager soundControl = SoundSettings.setUpSoundManager();
	
	/* ---------------------- States ------------------------ */
	private static final int SPLASHSCREEN = 0;
	private static final int GAME         = 1;
	private static final int GRACE        = 2;
	private static final int PAUSE        = 3;
	private static final int WIN          = 4;
	private static final int LOSE         = 5;
	private static final int HIGH_SCORES  = 6;
	
	/* ------------------ State-wide Data ------------------- */
	// the data required to load the universe, before we actually load it.
	private FrozenWorld frozenUniverse;
	private final KeyBindingsSlick keyBindings;
	
	// Starts at 0. When pause is pressed and this is not zero, nothing happens.
	// Otherwise state is changed and this is set to another value. This is to prevent accidentally
	// rapidly unpausing/pausing the game by holding down the key too much
	private long pauseDelay = 0;
	
	// The actual universe. By the time Splash Screen state is exited, all these should be set properly, and all
	// other states will have full access.
	// Technically, a lot of these things are accessible within GameWorldLogic, but
	// in many cases they are referenced often enough that keeping the references around
	// directly is nice.
	private GameWorldLogic universe;
	private World world;
	private WorldResource rsrc;
	private SlickWorldGraphics slickGraphics;
	private Bonzo bonzo;
	private GameOverHandler gameOverHandler;
	
	/* ---------------- Global Mutable Data ! --------------- */
	// mutable variable to make sure a game isn't already running.
	// Only this class and SlickMonkeyShinesStart should even touch this.
	static boolean running = false;
	
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
	public SlickMonkeyShines(FrozenWorld world, KeyBindingsSlick keys) {
		super("Monkey Shines (Java Port)");
		this.frozenUniverse = world;
		this.keyBindings = keys;
	}
	
	@Override public void initStatesList(GameContainer gc) throws SlickException {
		addState(new SplashScreen());
		addState(new Game());
		addState(new Grace());
		addState(new Pause());
		addState(new Lose());
	}
	
	@Override public boolean closeRequested() {
		System.out.println("setting running to false...");
		soundControl.stopPlayingMusic();
		soundControl.dispose();
		running = false;
		return true;
	}
	
	// Called when game exits, and should be called by anyone starting up
	// game after control returns.
	public void destroySounds() {
		System.out.println("Stopping sounds");
		soundControl.stopPlayingMusic();
		soundControl.dispose();
		SoundSettings.unregisterSoundManager(soundControl);
	}
	
	// Called from state transition from splash screen to let this state know that the
	// world resource has finished internally loading, and the actual world can be constructed.
	public void worldIsReady(StateBasedGame sbg) throws SlickException {
		this.rsrc.getSlickGraphics().finishInitialisation();
		this.world = frozenUniverse.encodedWorld.newWorldInstance(rsrc);
		this.slickGraphics = this.rsrc.getSlickGraphics();
		this.gameOverHandler = new GameOverHandler();
		this.universe = new GameWorldLogic(
			this.world, 
			this.gameOverHandler, 
			SpecialSettings.isThunderbird());
		this.bonzo = this.universe.getBonzo();
		// The init will be called after splash init; all states after this will not be
		// able to access resources.
		frozenUniverse.removeTemporaryFiles();
		frozenUniverse = null;
		soundControl.setSounds(this.rsrc.getSounds());
		
		// Set up initial grace animation 
		assert sbg.getState(GRACE) instanceof Grace : "Unexpected class: " + sbg.getState(GRACE).getClass().getName();
		((Grace)sbg.getState(GRACE)).initGrace();
	}
	
	public void setQuitAction(Runnable run)
		{ quit = run; }
	
	/* -------------------Splash Screen State ----------------- */
	private class SplashScreen extends BasicGameState {
		private static final int ID = SPLASHSCREEN;
		
		@Override public void init(GameContainer container, StateBasedGame game) throws SlickException {
			container.setShowFPS(false);
			LoadingList.setDeferredLoading(false);
			
			try {
				initRsrc = frozenUniverse.loadInit();
			} catch (ResourcePackException e) {
				throw new SlickException("Issue with world resource pack: " + e.getMessage(), e);
			}
			
			SoundSettings.registerSoundManager(soundControl);
			soundControl.setBgm(initRsrc.backgroundMusic);
			soundControl.playMusic();
		}

		@Override public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
			g.drawImage(initRsrc.splashScreen, 0, 0);
		}

		@Override public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
			 
			// load three at a time.
			for (int i = 0; i < LoadingList.get().getRemainingResources(); ++i) {
				DeferredResource asset = LoadingList.get().getNext();
				if (asset != null) {
					try {
						asset.load();
					} catch (IOException e) {
						throw new SlickException("Issue asynchronously loading world resources: " + e.getMessage(), e);
					}
				} else {
					break;
				}
				
				if (i > 2)
					{ break; }
			}
			
			if (LoadingList.get().getRemainingResources() == 0 && timeInSplash > MIN_FRAMES_IN_SPLASH) {
				// Tell next state to actually build the world now that the graphics are in place; remember,
				// certain calculations require the actual graphics, such as determining the number of hazards.
				
				worldIsReady(game);
				game.enterState(
					Game.ID, 
					new FadeOutTransition(Color.black), 
					new FadeInTransition(Color.black));
			}
			
			++timeInSplash;
		}

		@Override public int getID() {
			return ID;
		}
		
		private InitResource initRsrc;
		
		// Incremented each update. If loading the world takes less time than MIN_TIME_IN_SPLASH_SECONDS,
		// then update ticks will continue waiting to pass this value before
		private long timeInSplash = 0;
		
		private static final long MIN_TIME_IN_SPLASH_SECONDS = 3;
		private static final long MIN_FRAMES_IN_SPLASH = MIN_TIME_IN_SPLASH_SECONDS * GameConstants.FRAMES_PER_SECOND;
		
	}
	
	/* -------------------- Main Game State ------------------- */
	private class Game extends BasicGameState {
		private static final int ID = GAME;
		
		@Override public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
			gc.setShowFPS(false);
			try {
				LoadingList.setDeferredLoading(true);
				rsrc = frozenUniverse.load();
			} catch (ResourcePackException e) {
				LoadingList.setDeferredLoading(false);
				throw new SlickException("Issue with world resource pack: " + e.getMessage(), e);
			}
			
		}
	
		@Override public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
			if (pauseDelay > 0) 
				{ --pauseDelay; }
			
			// delta is ignored for Monkey Shines. The underlying game logic was never designed
			// with it in mind.
			handleKeys(gc.getInput(), sbg);
			universe.update(soundControl);
			
			if (universe.isGrace()) {
				((Grace)sbg.getState(GRACE)).initGrace();
				sbg.enterState(GRACE); 
			}
		}
		
		private void handleKeys(Input input, StateBasedGame sbg) {
			// Technically, checking left or right should be mutually exclusive. But pressing both
			// causes bonzo to dance, and that's funny.
			if (input.isKeyDown(keyBindings.left)) 
				{ bonzo.move(-1); }
			if (input.isKeyDown(keyBindings.right)) 
				{ bonzo.move(1); }
			if (input.isKeyDown(keyBindings.jump)) 
				{ bonzo.jump(4); }
			// hardcoded
			if (input.isKeyDown(Input.KEY_ESCAPE))
				{ gameOverHandler.gameOverEscape(world); }
			else if (input.isKeyDown(Input.KEY_P) && pauseDelay <= 0) { 
				pauseDelay = 15;
				sbg.enterState(PAUSE); 
			}
		}
	
		@Override public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
			renderLevel(g);
		}
		
		@Override public int getID() 
			{ return ID; }
	}
	
	// Renders the level to the graphics. This should only be called after the splash
	// state is over. Multiple states (gameplay, pause, grace) use level rendering in one
	// form or another.
	private void renderLevel(Graphics g) {
		SlickRenderer.paintUI(g, universe, slickGraphics);
		
		g.translate(0, 80);
		g.pushTransform();
		SlickRenderer.paintWorld(g, world);
		SlickRenderer.paintBonzo(g, universe.getBonzo(), slickGraphics);
		g.popTransform();
	}
	
	/* ----------------- Grace State ------------------- */
	// renders the world but does not update it anymore until
	// grace period has ended.
	private class Grace extends BasicGameState {
		@Override public void init(GameContainer container, StateBasedGame game) throws SlickException {
			// nothing to do.
		}

		@Override public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
			renderLevel(g);
			grace.paint(g);
		}

		@Override public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
			boolean moreFrames = grace.update();
			if (!moreFrames) { 
				universe.resetGrace();
				game.enterState(GAME);
			}
		}
		
		@Override public int getID() 
			{ return GRACE; }
		
		// Called when graphics resources are ready and when grace is being reset. Must be called be state
		// that is making transition to get bonzo's position correct!!!
		void initGrace() {
			grace = new GracePeriodAnimation(bonzo, slickGraphics, GameConstants.GRACE_PERIOD_FRAMES, 0, 0);
		}
		
		// set from worldIsReady to initial animation, and reset before state
		// change out of here.
		private GracePeriodAnimation grace;
	}
	
	/* -------------------- Pause State ------------------ */
	// Stays paused until the p key
	// is pressed and game unpauses.
	private class Pause extends BasicGameState {
		@Override public void init(GameContainer container, StateBasedGame game) throws SlickException {
			// nothing to do.
		}

		@Override public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
			renderLevel(g);
			g.drawImage(slickGraphics.pause, 230, 120);
		}

		@Override public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
			if (pauseDelay > 0) 
				{ --pauseDelay; }
			
			if (container.getInput().isKeyDown(Input.KEY_P) && pauseDelay <= 0) {
				pauseDelay = 15;
				game.enterState(GAME);
			}
		}
		
		@Override public int getID() 
			{ return PAUSE; }
	}
	
	/* -------------------- Lose State --------------------- */
	// Upon losing, the sound manager should be told to play the
	// lose sound and then this comes up for enough frames to fit the sound effect.
	// or at least, it will play the lose sound effect when I find it. For
	// now it will play the applause sound because I find that funny.
	// TODO actually play lose sound effect when found.
	private class Lose extends BasicGameState {
		@Override public void init(GameContainer container, StateBasedGame game) throws SlickException {
			// nothing to do.
		}

		@Override public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
			g.drawImage(slickGraphics.loseBackground, 0, 0);
		}

		@Override public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
			if (delay >= FRAMES_IN_LOSE_STATE) {
				quit.run(); 
			}
			++delay;
		}
		
		@Override public int getID() 
			{ return LOSE; }
		
		private static final long FRAMES_IN_LOSE_STATE = GameConstants.FRAMES_PER_SECOND * 6;
		private long delay = 0;
	}
	
	/* ------------------- Win State --------------------- */
	// When a level is won, shows the score tally, saves the
	// score to a file, and if it made the high scores list,
	// transition to that state. Otherwise, quit.

	public class GameOverHandler implements GameEndCallback {
		@Override public void gameOverFail(World w) {
			soundControl.stopPlayingMusic();
			soundControl.playOnce(GameSoundEffect.APPLAUSE);
			enterState(LOSE);
		}

		@Override public void gameOverEscape(World w) {
			// Just jump back to menu
			quit.run();
		}

		@Override public void gameOverWin(World w) {
			//enterState(WIN);
			quit.run();
		}
		
	}
}
