package org.erikaredmark.monkeyshines.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.erikaredmark.monkeyshines.HighScores;
import org.erikaredmark.monkeyshines.KeyBindingsSlick;
import org.erikaredmark.monkeyshines.global.KeySettings;
import org.erikaredmark.monkeyshines.global.MonkeyShinesPreferences;
import org.erikaredmark.monkeyshines.global.PreferencePersistException;
import org.erikaredmark.monkeyshines.global.SpecialSettings;
import org.erikaredmark.monkeyshines.global.VideoSettings;
import org.newdawn.slick.SlickException;
import org.erikaredmark.monkeyshines.menu.SelectAWorld.WorldSelectionCallback;
import org.erikaredmark.monkeyshines.play.FrozenWorld;
import org.erikaredmark.monkeyshines.play.SlickMonkeyShinesStart;

/**
 * 
 * The primary entry point for the actual game. The main window is a fixed size window that contains, one at a time,
 * either the game window, main menu, splash screen, or other such 'screens' as needed. This is only used in the main
 * game. It is NOT a dialog launcher. It is effectively a manager to decide which type of screen (game, menu, etc) should
 * be shown at any one time.
 * 
 * @author Erika Redmark
 *
 */
public final class MainWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final String CLASS_NAME = "org.erikaredmark.monkeyshines.menu.MainWindow";
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
	
	// The currently running game. May be null if no game is running or if game is running in fullscreen.
//	private GamePanel runningGameWindowed;
	
	// The play game panel. This is always initialised as part of the playgame and leaves when that
	// state is transitioned away. this is shown when the play game button is pressed
	private SelectAWorld selectWorldPanel;
	
	// Main menu displayed. May be null if the main menu is no longer displayed
	private MainMenuWindow menu;
	
	// High scores displayed. May be null if the high scores are not displayed
	private ViewHighScores highScores;
	
	private GameState state = GameState.NONE;
	
	// Main menu Bar
	private JMenuBar mainMenuBar = new JMenuBar();
	
	// Menu: Options
	private JMenu options = new JMenu("Options");

	private final JMenuItem changeFullscreen = new JCheckBoxMenuItem("Fullscreen", null, VideoSettings.isFullscreen() );
	private final JMenuItem playtestMode = new JCheckBoxMenuItem("Playtesting", null, SpecialSettings.isThunderbird() );
	
	// Called when 'play game' is pressed in main menu. Transition to the 'choose a world' screen.
	private final Runnable playGameCallback = new Runnable() {
		@Override public void run() {
			setGameState(GameState.CHOOSE_WORLD);
		}
	};
	
	private final Runnable highScoresCallback = new Runnable() {
		@Override public void run() {
			setGameState(GameState.HIGH_SCORES);
		}
	};
	
	public MainWindow() { 
		setTitle("Monkey Shines");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		// Do not set size here: It would include title bar. JPanel is added and packed
		// during state transition to size window correctly.
		
		
		// Set the menu bar. Some functions are accessible from here in lue of buttons (this may change)
		changeFullscreen.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				VideoSettings.setFullscreen(changeFullscreen.isSelected() );
				try {
					VideoSettings.persist();
				} catch (PreferencePersistException e) {
					LOGGER.log(Level.WARNING,
							   CLASS_NAME + ": cannot persist preferences: " + e.getMessage(),
							   e);
				}
			}
		});
		
		options.add(changeFullscreen);
		
		playtestMode.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				SpecialSettings.setThunderbird(playtestMode.isSelected() );
				try {
					SpecialSettings.persist();
				} catch (PreferencePersistException e) {
					LOGGER.log(Level.WARNING,
							   CLASS_NAME + ": cannot persist preferences: " + e.getMessage(),
							   e);
				}
			}
		});
		
		options.add(playtestMode);
		
		mainMenuBar.add(options);
		
		setJMenuBar(mainMenuBar);
		
		// Initial state is menu.
		// This must be done after all contents are added to window to size correctly
		setGameState(GameState.MENU);

		// Now we can set relative to, since window is already sized properly.
		setLocationRelativeTo(null);
		setVisible(true);
		
		setAlwaysOnTop(false);
	}
	
	/**
	 * 
	 * Sets the game state to either playing in windowed or playing in fullscreen, based on settings.
	 * 
	 * @return
	 * 		{@code true} if state change is successful, {@code false} if otherwise
	 * 
	 */
//	private boolean playGame() {
//		boolean gameStarted = false;
//		if (VideoSettings.isFullscreen() ) {
//			// Try to run fullscreen, but fallback to windowed if unable to
//			if (!(setGameState(GameState.PLAYING_FULLSCREEN) ) ) {
//				gameStarted = setGameState(GameState.PLAYING_WINDOWED);
//			} else {
//				// Else if setting the game state to playing fullscreen succeeded.
//				gameStarted = true;
//			}
//		} else {
//			gameStarted = setGameState(GameState.PLAYING_WINDOWED);
//		}
//		
//		if (gameStarted) {
//			// Gave the world object to the game, no need to keep ref here anymore
//			tempWorld = null;
//		}
//		
//		return gameStarted;
//	}
	
	// Sets the new game state and calls transition methods to modify the proper
	// state information for this object
	// returns whether the state change was successful.
	private boolean setGameState(final GameState state) {
		// This method will actually modify the state variable stored in this object automatically.
		return state.transitionTo(this);
	}
	
	private enum GameState {
		// Note: always change state at END of method. Perform any possible actions that could prevent
		// state change before actually modifying any state variables.
		// In Transition To, ALWAYS:
		// 1) Transition FROM the current state after verifying state change is allowed
		// 2) Transition TO this state after setting up.
		// No extra-linguistic restrictions for transitionFrom
		CHOOSE_WORLD {
			@Override public boolean transitionTo(final MainWindow mainWindow) {
				mainWindow.state.transitionFrom(mainWindow);
				mainWindow.selectWorldPanel = new SelectAWorld(new WorldSelectionCallback() {
					@Override public void worldSelected(final FrozenWorld world) {
						// Blocks intentionally until the game context is over.
						try {
							SlickMonkeyShinesStart.startMonkeyShines(
								world, 
								KeyBindingsSlick.fromKeyBindingsAwt(KeySettings.getBindings()),
								VideoSettings.isFullscreen());
						} catch (SlickException e) {
							LOGGER.log(Level.SEVERE, "Expected to start game but could not: " + e.getMessage(), e);
						}
					}
				});
				
				mainWindow.add(mainWindow.selectWorldPanel);
				mainWindow.selectWorldPanel.setVisible(true);
				mainWindow.pack();
				// Required because otherwise the main menu will bleed through.
				mainWindow.repaint();
				mainWindow.state = this;
				
				return true;
			}
			
			@Override protected void transitionFrom(MainWindow mainWindow) {
				mainWindow.remove(mainWindow.selectWorldPanel);
				mainWindow.selectWorldPanel.setFocusable(false);
				// nothing to dispose, just null the reference for garbage collection.
				mainWindow.selectWorldPanel = null;
			}
		},
		MENU {
			@Override public boolean transitionTo(MainWindow mainWindow) {
				mainWindow.state.transitionFrom(mainWindow);
				mainWindow.menu = new MainMenuWindow(mainWindow.playGameCallback, mainWindow.highScoresCallback);
				mainWindow.add(mainWindow.menu);
				mainWindow.pack();
				
				mainWindow.state = this;
				return true;
			}

			@Override protected void transitionFrom(MainWindow mainWindow) {
				if (mainWindow.menu != null) {
					mainWindow.remove(mainWindow.menu);
					mainWindow.menu = null;
				}
			}
			
		},
		// TODO because High scores are viewable from both the main menu AND during gameplay, we have
		// a bad code duplication. High scores cannot use Slick based rendering in the main menu unless the entire
		// main menu becomes Slick rendered.
		HIGH_SCORES {
			@Override public boolean transitionTo(final MainWindow mainWindow) {
				mainWindow.state.transitionFrom(mainWindow);
				mainWindow.highScores = 
					new ViewHighScores(
						HighScores.fromFile(MonkeyShinesPreferences.getHighScoresPath() ),
						new ViewHighScores.BackButtonCallback() {
							@Override public void backButtonPressed() { mainWindow.setGameState(GameState.MENU); }
						});
				
				mainWindow.add(mainWindow.highScores);
				mainWindow.pack();
				mainWindow.highScores.setVisible(true);
				mainWindow.repaint();
				mainWindow.state = this;
				return true;
			}
			
			@Override protected void transitionFrom(MainWindow mainWindow) {
				if (mainWindow.highScores != null) {
					mainWindow.remove(mainWindow.highScores);
					mainWindow.highScores = null;
				}
			}
		},
		// Represents no state, so null checks aren't required on state object
		NONE {
			@Override public boolean transitionTo(MainWindow mainWindow) { return true; }
			@Override protected void transitionFrom(MainWindow mainWindow) { }
		};
		
		/**
		 * 
		 * Defines the transition logic for the state. Transitioning to a state will automatically
		 * cleanup the current state and show the new state.
		 * <p/>
		 * A state change can fail. If so, then {@code transitionFrom} was never called, no state
		 * variables were modified.
		 * 
		 * @param mainWindow
		 * 		a 'this' parameter effectively to the main window, since enums have static visibility
		 * 
		 * @return
		 * 		{@code true} if state changed, {@code false} if otherwise.
		 * 
		 */
		public abstract boolean transitionTo(final MainWindow mainWindow);
		
		/**
		 * 
		 * Called automatically by {@code transitionTo} for whatever the current state was before actually
		 * performing the transition. This is intended for 'clean-up', such as removing swing objects no
		 * longer appropriate for the new state.
		 * <p/>
		 * Transitioning away can never fail. If it otherwise should, it should be detected in transitionTo.
		 * 
		 * @param mainWindow
		 * 		a 'this' parameter effectively to the main window, since enums have static visibility
		 * 
		 */
		protected abstract void transitionFrom(final MainWindow mainWindow);
	}
	
}
