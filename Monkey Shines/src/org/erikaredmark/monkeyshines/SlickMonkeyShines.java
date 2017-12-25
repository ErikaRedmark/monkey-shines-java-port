package org.erikaredmark.monkeyshines;

import java.io.IOException;

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
import org.newdawn.slick.state.GameState;
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
	
	/* ------------------ State-wide Data ------------------- */
	// the data required to load the universe, before we actually load it.
	private FrozenWorld frozenUniverse;
	private final KeyBindingsSlick keyBindings;
	
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
	
	
	public void setQuitAction(Runnable run)
		{ quit = run; }
	
	/* -------------------Splash Screen State ----------------- */
	private class SplashScreen extends BasicGameState {
		private static final int ID = SPLASHSCREEN;
		
		@Override public void init(GameContainer container, StateBasedGame game) throws SlickException {
			container.setShowFPS(false);
			
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
				
				GameState gameState = game.getState(GAME);
				assert gameState instanceof Game : "Unexpected class: " + gameState.getClass().getName();
				((Game)gameState).worldIsReady();
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
		
		// Called from state transition from splash screen to let this state know that the
		// world resource has finished internally loading, and the actual world can be constructed.
		public void worldIsReady() throws SlickException {
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
		}
		
		@Override public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
			gc.setShowFPS(false);
			// Start the main resource loading
//			unfreezeWorld = CompletableFuture.supplyAsync( () -> {
			try {
				LoadingList.setDeferredLoading(true);
//				world = frozenUniverse.load();
				rsrc = frozenUniverse.load();
//				slickGraphics = this.rsrc.getSlickGraphics();
//				gameOverHandler = new GameOverHandler();
//				universe = new GameWorldLogic(
//					this.world, 
//					this.gameOverHandler, 
//					SpecialSettings.isThunderbird());
//				bonzo = this.universe.getBonzo();
//				// The init will be called after splash init; all states after this will not be
//				// able to access resources.
//				frozenUniverse.removeTemporaryFiles();
//				frozenUniverse = null;
			} catch (ResourcePackException e) {
				LoadingList.setDeferredLoading(false);
				throw new SlickException("Issue with world resource pack: " + e.getMessage(), e);
			}
//			});
			
		}
	
		@Override public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
			// delta is ignored for Monkey Shines. The underlying game logic was never designed
			// with it in mind.
			handleKeys(gc.getInput());
			universe.update(soundControl);
		}
		
		private void handleKeys(Input input) {
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
				{ this.gameOverHandler.gameOverEscape(this.world); }
		}
	
		@Override public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
			SlickRenderer.paintUI(g, universe, slickGraphics);
			
			g.translate(0, 80);
			g.pushTransform();
			SlickRenderer.paintWorld(g, world);
			SlickRenderer.paintBonzo(g, universe.getBonzo(), slickGraphics);
			g.popTransform();
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
		
		@Override public int getID() 
			{ return ID; }
		
		// Not set until init function; requires graphics resources that are not
		// available until then. Is not used until update method anyway.
		// Technically, a lot of these things are accessible within GameWorldLogic, but
		// in many cases they are referenced often enough that keeping the references around
		// directly is nice.
		private GameWorldLogic universe;
		private World world;
		private WorldResource rsrc;
		private SlickWorldGraphics slickGraphics;
		private Bonzo bonzo;
		private GameOverHandler gameOverHandler;
	}
}
