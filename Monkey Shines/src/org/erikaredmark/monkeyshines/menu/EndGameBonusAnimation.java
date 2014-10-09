package org.erikaredmark.monkeyshines.menu;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.erikaredmark.monkeyshines.World;
import org.erikaredmark.monkeyshines.WorldStatistics;
import org.erikaredmark.monkeyshines.global.SoundUtils;

/**
 * 
 * This is the screen that appears after a level has been finished properly, when it now must tally up
 * the score.
 * <p/>
 * It is CRTICIAL that the game timer be STOPPED before using this class.
 * <p/>
 * This class takes control of the thread that creates it and draws to the provided surface, in specific
 * hardcoded intervals, different game statistics, before finally reliquishing control back to the thread.
 * It is presumed that the graphics surface passed is visible; a callback is used to allow any parent objects
 * to realise they must redraw for each new statistic.
 * <p/>
 * This object must be disposed after using to clean up the graphics and sound resources that are loaded
 * for its operation.
 * 
 * @author Erika Redmark
 *
 */
public final class EndGameBonusAnimation {

	private static final String CLASS_NAME = "org.erikaredmark.monkeyshines.menu.EndGameBonusSurface";
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
	
	private static final int ALL_FIELDS_X = 470;
	
	private static final int FRUIT_COLLECTED_Y = 198;
	private static final int FRUIT_BONUS_Y = 238;
	private static final int TIME_BONUS_Y = 278;
	private static final int SCORE_Y = 318;
	private static final int TOTAL_SCORE_Y = 368;

	// Not intended to be instantiated outside of this class. Instantiated in method context and 
	// state incremented until done.
	private EndGameBonusAnimation() { 

		try {
			// Okay if we can't play sounds. Not okay if background doesn't load.
			background = ImageIO.read(EndGameBonusAnimation.class.getResourceAsStream("/resources/graphics/mainmenu/endgame/bonusTally.png") );
			tallySwoosh = SoundUtils.clipFromOggStream(EndGameBonusAnimation.class.getResourceAsStream("/resources/sounds/mainmenu/endgame/bonusTally.ogg"), "bonusTally.ogg");
		} catch (UnsupportedAudioFileException | IOException e) {
			throw new RuntimeException("Missing resource in .jar file: " + e.getMessage(), e);
		} catch (LineUnavailableException e) {
			LOGGER.log(Level.WARNING,
					   "Could not play end game tally sounds: " + e.getMessage() + ". Check .jar integrity.",
					   e);
		}
		
		assert background != null;
		
		animationState = AnimationState.FRUIT_COLLECTED;
	};
	
	// Mutates the g2d instance to set it to the right font, if that font was successfully loaded.
	// In either case, it sets up the font for display on the tally page.
	private static void setupTallyFont(Graphics2D g2d) {
//		Font chicago = CoreResource.INSTANCE.getChicago();
//		if (chicago != null) {
//			g2d.setFont(chicago);
//		}
		
		g2d.setColor(Color.GREEN);
	}
	
	/**
	 * 
	 * Primary object cycles through each state in order before being disposed. Each state draws a different thing to a different
	 * place on the provided g2d instance. State is transitioned explicitly. This is intended to handle drawing to both
	 * volatile and non-volatile surfaces, as drawing to volatile surfaces may have to be repeated for state transition if video
	 * memory was lost.
	 * 
	 * @author Erika Redmark
	 *
	 */
	private enum AnimationState {
		// Note: All draw strings use Font Metrics for Right Alignment.
		FRUIT_COLLECTED {
			@Override public void drawState(Graphics2D g2d, WorldStatistics stats) {
				setupTallyFont(g2d);
				String stringToDraw = String.valueOf(stats.getFuritCollectedPercent() ) + "%";
				int rightAlign = ALL_FIELDS_X - g2d.getFontMetrics().stringWidth(stringToDraw);
				g2d.drawString(stringToDraw, rightAlign, FRUIT_COLLECTED_Y);
			}

			@Override public AnimationState nextState() { return FRUIT_BONUS; }
			@Override public int waitTime() { return 1500; }
		},
		FRUIT_BONUS {
			@Override public void drawState(Graphics2D g2d, WorldStatistics stats) {
				setupTallyFont(g2d);
				String stringToDraw = String.valueOf(stats.getFruitBonus() );
				int rightAlign = ALL_FIELDS_X - g2d.getFontMetrics().stringWidth(stringToDraw);
				g2d.drawString(stringToDraw, rightAlign, FRUIT_BONUS_Y);
			}
			
			@Override public AnimationState nextState() { return TIME_BONUS; }
			@Override public int waitTime() { return 1500; }
		},
		TIME_BONUS {
			@Override public void drawState(Graphics2D g2d, WorldStatistics stats) {
				setupTallyFont(g2d);
				String stringToDraw = String.valueOf(stats.getTimeBonus() );
				int rightAlign = ALL_FIELDS_X - g2d.getFontMetrics().stringWidth(stringToDraw);
				g2d.drawString(stringToDraw, rightAlign, TIME_BONUS_Y);
			}
			
			@Override public AnimationState nextState() { return SCORE; }
			@Override public int waitTime() { return 1500; }
		},
		SCORE {
			@Override public void drawState(Graphics2D g2d, WorldStatistics stats) {
				setupTallyFont(g2d);
				String stringToDraw = String.valueOf(stats.getRawScore() );
				int rightAlign = ALL_FIELDS_X - g2d.getFontMetrics().stringWidth(stringToDraw);
				g2d.drawString(stringToDraw, rightAlign, SCORE_Y);
			}
			
			@Override public AnimationState nextState() { return TOTAL_SCORE; }
			@Override public int waitTime() { return 2000; }
		},
		TOTAL_SCORE {
			@Override public void drawState(Graphics2D g2d, WorldStatistics stats) {
				setupTallyFont(g2d);
				String stringToDraw = String.valueOf(stats.getTotalScore() );
				int rightAlign = ALL_FIELDS_X - g2d.getFontMetrics().stringWidth(stringToDraw);
				g2d.drawString(stringToDraw, rightAlign, TOTAL_SCORE_Y);
			}
			
			@Override public AnimationState nextState() { return null; }
			@Override public int waitTime() { return 4000; }
		};
		/**
		 * 
		 * Draws this state to the given graphics context. The appropriate data will be taken from the statistics.
		 * 
		 * @param g2d
		 * 		graphics surface to draw on
		 * 
		 * @param stats
		 * 
		 */
		public abstract void drawState(Graphics2D g2d, WorldStatistics stats);
		
		/**
		 * 
		 * Returns the next state in sequence. Returns {@code null} at the last state.
		 * 
		 */
		public abstract AnimationState nextState();
		
		/**
		 * 
		 * Returns the amount of time, in milliseconds, that should elapse before the next state change.
		 * 
		 */
		public abstract int waitTime();
	}
	/**
	 * 
	 * Runs the bonus tally part of the game by confiscating the graphics context and drawing the
	 * tally animation on it. This method will return once the animation is complete. This does
	 * not return anything useful and serves only as an indication to the player how well they
	 * did. All total score data is calculated and saved once the game timer stops in the World, so
	 * this does not mutate that instance in any way either.
	 * <p/>
	 * World timer must be stopped before calling this method.
	 * <p/>
	 * This this method doesn't not actually return an instance of the object, the actual underlying
	 * instance data and all other resources will be disposed of upon completion of this method.
	 * 
	 * @param g2d
	 * 		graphics context to draw animation to
	 * 
	 * @param completedWorld
	 * 		the completed world (game timer stopped) of which this object will get the actual
	 * 		tally data for display
	 * 
	 * @param repaintCallback
	 * 		callback for each frame of animation, typically used to alert whatever drawing system
	 * 		that an update has been made and needs to be redrawn
	 * 
	 */
	public static void runOn(Graphics2D g2d,
							 World completedWorld, 
							 Runnable repaintCallback) {
		
		EndGameBonusAnimation animation = new EndGameBonusAnimation();
		final WorldStatistics stats = completedWorld.getStatistics();
		// Background need only be drawn once.
		g2d.drawImage(animation.background, 0, 0, null);
		
		while (animation.animationState != null) {
			animation.animationState.drawState(g2d, stats);
			repaintCallback.run();
			if (animation.tallySwoosh.isActive() )  animation.tallySwoosh.stop();
			animation.tallySwoosh.setFramePosition(0);
			animation.tallySwoosh.start();
			try {
				Thread.sleep(animation.animationState.waitTime() );
			} catch (InterruptedException e) {
				LOGGER.log(Level.WARNING,
						   CLASS_NAME + ": Thread interrupted whilst waiting between animations on Tally screen due to: " + e.getMessage(),
						   e);
				// Not to worry, just means this will jump by faster than normal. Not desirable but certainly not crash/do-over worthy.
			}
			animation.animationState = animation.animationState.nextState();
		}
		
		// Animation complete, nothing more to do. Cede control back to caller.
		
	}
	
	/**
	 * 
	 * See {@link runOn(Graphics2D, World, Runnable)}.
	 * <p/>
	 * Performs the same function, but assumes a volatile surface with a buffer strategy. This is required since
	 * video memory contents may have to be refreshed suddenly.
	 * 
	 * @param buffer
	 * 		buffer strategy for the volatile drawing
	 * 
	 * @param completedWorld
	 * 		the completed world (game timer stopped) of which this object will get the actual
	 * 		tally data for display
	 * 
	 */
	public static void runOnVolatile(BufferStrategy buffer,
									 World completedWorld) {
		
		EndGameBonusAnimation animation = new EndGameBonusAnimation();
		final WorldStatistics stats = completedWorld.getStatistics();

		List<AnimationState> previousStates = new ArrayList<>(5);
		while (animation.animationState != null) {
			// We must ALWAYS draw the background. Buffer strategy may use multiple buffers
			// and only by drawing completely will we prevent the last frame from the game
			// being blitted accidentally. Because of this, we must blit the current state
			// and all previous states.
			do {
				do {
					Graphics2D g2d = (Graphics2D) buffer.getDrawGraphics();
					try {
						g2d.drawImage(animation.background, 0, 0, null);
						for (AnimationState state : previousStates) {
							state.drawState(g2d, stats);
						}
						animation.animationState.drawState(g2d, stats);
					} finally {
						g2d.dispose();
					}
				} while (buffer.contentsRestored() );
				
				buffer.show();
				
			} while (buffer.contentsLost() );
			
			if (animation.tallySwoosh.isActive() )  animation.tallySwoosh.stop();
			animation.tallySwoosh.setFramePosition(0);
			animation.tallySwoosh.start();
			
			try {
				Thread.sleep(animation.animationState.waitTime() );
			} catch (InterruptedException e) {
				LOGGER.log(Level.WARNING,
						   CLASS_NAME + ": Thread interrupted whilst waiting between animations on Tally screen due to: " + e.getMessage(),
						   e);
				// Not to worry, just means this will jump by faster than normal. Not desirable but certainly not crash/do-over worthy.
			}
			previousStates.add(animation.animationState);
			animation.animationState = animation.animationState.nextState();
		}
	}
	
	
	private BufferedImage background;
	// Played when a stat is displayed on the tally, with a non-zero value
	private Clip tallySwoosh;
	
	private AnimationState animationState;
}
