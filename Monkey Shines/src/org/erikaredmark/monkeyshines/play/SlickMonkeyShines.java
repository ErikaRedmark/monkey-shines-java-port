package org.erikaredmark.monkeyshines.play;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.erikaredmark.monkeyshines.Bonzo;
import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.GameSoundEffect;
import org.erikaredmark.monkeyshines.GameWorldLogic;
import org.erikaredmark.monkeyshines.HighScores;
import org.erikaredmark.monkeyshines.KeyBindingsSlick;
import org.erikaredmark.monkeyshines.World;
import org.erikaredmark.monkeyshines.WorldStatistics;
import org.erikaredmark.monkeyshines.HighScores.HighScore;
import org.erikaredmark.monkeyshines.animation.GracePeriodAnimation;
import org.erikaredmark.monkeyshines.global.MonkeyShinesPreferences;
import org.erikaredmark.monkeyshines.global.SoundSettings;
import org.erikaredmark.monkeyshines.global.SoundUtils;
import org.erikaredmark.monkeyshines.global.SpecialSettings;
import org.erikaredmark.monkeyshines.graphics.exception.ResourcePackException;
import org.erikaredmark.monkeyshines.menu.MenuUtils;
import org.erikaredmark.monkeyshines.menu.slick.EnterHighScoreName;
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
	private static final String CLASS_NAME = "org.erikaredmark.monkeyshines.SlickMonkeyShines";
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
	
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
	
	// initialised in Game->Win transition. Used in Win State and High Scores
	// state.
	private WorldStatistics stats;
	
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
		addState(new Win());
		addState(new HighScoreState());
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
			else if (input.isKeyPressed(Input.KEY_P)) { 
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
			if (container.getInput().isKeyPressed(Input.KEY_P)) {
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
	private class Win extends BasicGameState {
		
		@Override public void init(GameContainer container, StateBasedGame game) throws SlickException {
			try {
				// Okay if we can't play sounds.
				tallySwoosh = Optional.of(SoundUtils.clipFromOggStream(
						SlickMonkeyShines.class.getResourceAsStream("/resources/sounds/mainmenu/endgame/bonusTally.ogg"), 
						"bonusTally.ogg"));
			} catch (UnsupportedAudioFileException | IOException e) {
				throw new RuntimeException("Missing resource in .jar file: " + e.getMessage(), e);
			} catch (LineUnavailableException e) {
				LOGGER.log(Level.WARNING,
						   "Could not play end game tally sounds: " + e.getMessage() + ". Check .jar integrity.",
						   e);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE,
						   "Could not play end game tally sounds, unexpected exception " + e.getMessage(),
						   e);
			}
		}

		@Override public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
			g.drawImage(slickGraphics.tallyScoresBackground, 0, 0);
			g.setColor(Color.green);
			switch(step) {
			// brackets intentional to reuse local variable names in each case.
			// Fallthrough is INTENDED. We don't just draw the current tally, we draw ALL
			// the tallys up to that point.
			default: {
				// anything higher than 0 hits every case.
			}
			case 5: {
				g.setColor(Color.black);
				g.fillRect(246, 402, 218, 16);
				g.setColor(Color.green);
				g.drawString("Press Enter to continue", 250, 400);
				
				String toDraw = String.valueOf(stats.getTotalScore() );
				int rightAlign = ALL_FIELDS_X - g.getFont().getWidth(toDraw);
				g.drawString(toDraw, rightAlign, TOTAL_SCORE_Y);
			} // fallthrough intended
			case 4: {
				String toDraw = String.valueOf(stats.getRawScore() );
				int rightAlign = ALL_FIELDS_X - g.getFont().getWidth(toDraw);
				g.drawString(toDraw, rightAlign, SCORE_Y);
			} // fallthrough intended
			case 3: {
				String toDraw = String.valueOf(stats.getTimeBonus() );
				int rightAlign = ALL_FIELDS_X - g.getFont().getWidth(toDraw);
				g.drawString(toDraw, rightAlign, TIME_BONUS_Y);
			} // fallthrough intended
			case 2: {
				String toDraw = String.valueOf(stats.getFruitBonus() );
				int rightAlign = ALL_FIELDS_X - g.getFont().getWidth(toDraw);
				g.drawString(toDraw, rightAlign, FRUIT_BONUS_Y);
			} // fallthrough intended
			case 1: {
				String toDraw = String.valueOf(stats.getFuritCollectedPercent()) + "%";
				int rightAlign = ALL_FIELDS_X - g.getFont().getWidth(toDraw);
				g.drawString(toDraw, rightAlign, FRUIT_COLLECTED_Y);
			}
			// Nothing happens for case zero, but it must appear here or the default case
			// will get hit
			case 0: {
				
			}
			}
		}

		@Override public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
			if (nextNumberDelay >= FRAMES_PER_DELAY) {
				++step;
				nextNumberDelay = 0;
				if (step <= 5) {
					tallySwoosh.ifPresent(snd -> {
						if (snd.isActive())
							{ snd.stop(); }
						snd.setFramePosition(0);
						snd.start();
					});
				}
			}
			
			if (step <= 5)
				{ ++nextNumberDelay; }
			
			if (step > 5) {
				Input input = container.getInput();
				if (input.isKeyPressed(Input.KEY_ENTER) || input.isKeyPressed(Input.KEY_ESCAPE)) {
					// TODO run into high score state
//					quit.run();
					((HighScoreState)game.getState(HIGH_SCORES)).setupHighScores(input);
					game.enterState(HIGH_SCORES); 
				}
			}
		}
		
		@Override public int getID() 
			{ return WIN; }
		
		// Together, work to draw the next tally in the win statistics. Each passing of 
		// FRAMES_PER_DELAY will tally up numbers until all are displayed. At that point input will be accepted
		// to move off of the tally screen.
		private long nextNumberDelay = 0;
		private static final long FRAMES_PER_DELAY = GameConstants.FRAMES_PER_SECOND + (GameConstants.FRAMES_PER_SECOND / 2);
		
		// 0 is show nothing, when moving from 0 to 1
		// show fruit collected, then 1-2 fruit bonus,
		// then time bonus, then score, then total score,
		// then 'hit enter or escape to continue.
		private int step = 0;
		
		private Optional<Clip> tallySwoosh = Optional.empty();
		
		// Static drawing information.
		private static final int ALL_FIELDS_X = 472;
		
		private static final int FRUIT_COLLECTED_Y = 186;
		private static final int FRUIT_BONUS_Y = 226;
		private static final int TIME_BONUS_Y = 266;
		private static final int SCORE_Y = 306;
		private static final int TOTAL_SCORE_Y = 356;
	}
	
	/* ------------------ High Scores ------------------- */
	// This state is entered from the Win state. It displays
	// the high scores and, before state transition, the high
	// scores will be updated and written out.
	// the stats variable should have been set before transition.
	// TODO currently there is a rendering bug (easy) and a worse bug...
	// ... the 'enter player name' uses Java AWT but we are still in Slick,
	// so an alternative will have to be created. This isn't highest
	// priority right now to fix so leaving this here for later.
	private class HighScoreState extends BasicGameState {
		// Called when win state transitions here
		// the enter name dialog may block the update loop,
		// but the game is effectively over anyway.
		void setupHighScores(Input input) {
			int score = stats.getTotalScore();
			highScores = HighScores.fromFile(MonkeyShinesPreferences.getHighScoresPath());
			if (highScores.isScoreHigh(score)) {
				soundControl.playOnce(GameSoundEffect.YES);
				askForPlayerName = new EnterHighScoreName(200, 200, input);
			}
			soundControl.playOnceDelayed(GameSoundEffect.APPLAUSE, 1, TimeUnit.SECONDS);
		}
		
		@Override public void init(GameContainer container, StateBasedGame game) throws SlickException {
		}

		@Override public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
			g.drawImage(slickGraphics.highScoresBackground, 0, 0);
			
			List<HighScore> scores = highScores.getHighScores();

			g.setColor(Color.green);
//			g.setFont(scoreFont);
			
			{
				// Keep index number; we need it for drawing purposes.
				int index = 0;
				for (HighScore score : scores) {
					int yPos =  (index * 24) + 128;
					g.drawString(MenuUtils.cutString(score.getName(), 50), 40, yPos);
					g.drawString(String.valueOf(score.getScore() ), 500, yPos);
					++index;
				}
			}
			
			if (askForPlayerName != null && !(askForPlayerName.isDone())) {
				askForPlayerName.render(g);
			}
		}

		@Override public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
			Input input = container.getInput();

			// If we are asking for a high score, we are always drawing the high scores
			// initially in the background anyway, but we won't listen for input ourselves
			// but instead pass that to EnterHighScoreName
			if (askForPlayerName != null) {
				if (askForPlayerName.isDone()) { 
					// Not null but done indicates the user just finished entering their name. Extract into high
					// scores
					String playerName = askForPlayerName.getEnteredText();
					askForPlayerName = null;
					highScores.addScore(playerName, stats.getTotalScore());
					// Don't do file operations on a tight update loop
					CompletableFuture.runAsync( () -> {
						highScores.persistScores(MonkeyShinesPreferences.getHighScoresPath());
					});
					soundControl.playOnce(GameSoundEffect.APPLAUSE);
				}
			} else {
				if (triggeredDelay && (input.isKeyPressed(Input.KEY_ENTER) || input.isKeyPressed(Input.KEY_ESCAPE))) {
					quit.run();
				}
				if (delay <= DELAY_FRAMES) {
					++delay;
				} else {
					triggeredDelay = true;
				}
			}
			
		}
		
		@Override public int getID() 
			{ return HIGH_SCORES; }
		
		private boolean triggeredDelay = false;
		long delay = 0;
		private static final long DELAY_FRAMES = 60;
		
		HighScores highScores;
		// If set to non null in state change because of high score being... a high score, will render
		// the name-entry asking thing before passing input control to escape from the high scores list
		// and will have the effect of updating the high scores chart after the name is entered.
		private EnterHighScoreName askForPlayerName;
	}
	
	
	public class GameOverHandler implements GameEndCallback {
		@Override public void gameOverFail(World w) {
			soundControl.stopPlayingMusic();
			soundControl.playOnceDelayed(GameSoundEffect.APPLAUSE, 1, TimeUnit.SECONDS);
			enterState(LOSE, new FadeOutTransition(Color.black), new FadeInTransition(Color.black));
		}

		@Override public void gameOverEscape(World w) {
			// Just jump back to menu
			quit.run();
		}

		@Override public void gameOverWin(World w) {
			soundControl.stopPlayingMusic();
			stats = world.getStatistics();
			enterState(WIN, new FadeOutTransition(Color.black, 1000), new FadeInTransition(Color.black, 1000));
		}
		
	}
}
