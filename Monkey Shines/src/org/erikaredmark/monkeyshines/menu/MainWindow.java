package org.erikaredmark.monkeyshines.menu;

import javax.swing.JFrame;

import org.erikaredmark.monkeyshines.KeyboardInput;

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

	// The currently running game. May be null if no game is running
	private GameWindow runningGame;
	
	// Main menu displayed. May be null if the main menu is no longer displayed
	private MainMenuWindow menu;
	
	private GameState state;
	
	// current key setup is stored so it can be removed as an observer from the main window during state
	// transitions
	private KeyboardInput currentKeyListener;
	
	// TODO level selection window, sound control window, keyboard mapping, credits, and some placeholder for registration?
	public MainWindow() { 
		setTitle("Monkey Shines");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);
		// Initial state is menu.
		setGameState(GameState.MENU);
		setVisible(true);
	}
	
	// Sets the new game state and calls transition methods to modify the proper
	// state information for this object
	private void setGameState(final GameState state) {
		// This method will actually modify the state variable stored in this object automatically.
		state.transitionTo(this);
	}
	
	private final Runnable resetCallback = new Runnable() {
		@Override public void run() {
			// TODO method stub
		}
	};
	
	private enum GameState {
		PLAYING {
			@Override public void transitionTo(final MainWindow mainWindow) {
				
				// TODO clean up menu before showing game window
				mainWindow.currentKeyListener = new KeyboardInput();
				mainWindow.runningGame = new GameWindow(mainWindow.currentKeyListener, mainWindow.resetCallback);
				// Must add to both.
				mainWindow.addKeyListener(mainWindow.currentKeyListener);
				mainWindow.add(mainWindow.runningGame);
				mainWindow.pack();
			}

			@Override protected void transitionFrom(MainWindow mainWindow) {
				if (mainWindow.runningGame != null) {
					mainWindow.remove(mainWindow.runningGame);
					// Nulling reference is important; running game state should be GC'ed as it will no longer be
					// transitioned back to.
					mainWindow.runningGame = null;
					assert mainWindow.currentKeyListener != null : "Keyboard based game played without a keyboard listener?";
					
					mainWindow.removeKeyListener(mainWindow.currentKeyListener);
				}
			}
		},
		MENU {
			@Override public void transitionTo(MainWindow mainWindow) {
				mainWindow.menu = new MainMenuWindow();
				mainWindow.pack();
			}

			@Override protected void transitionFrom(MainWindow mainWindow) {
				if (mainWindow.menu != null) {
					mainWindow.remove(mainWindow.menu);
					mainWindow.menu = null;
				}
			}
			
		};
		
		/**
		 * 
		 * Defines the transition logic for the state. Transitioning to a state will automatically
		 * cleanup the current state and show the new state.
		 * 
		 * @param mainWindow
		 * 		a 'this' parameter effectively to the main window, since enums have static visibility
		 * 
		 */
		public abstract void transitionTo(final MainWindow mainWindow);
		
		/**
		 * 
		 * Called automatically by {@code transitionTo} for whatever the current state was before actually
		 * performing the transition. This is intended for 'clean-up', such as removing swing objects no
		 * longer appropriate for the new state.
		 * 
		 * @param mainWindow
		 * 		a 'this' parameter effectively to the main window, since enums have static visibility
		 * 
		 */
		protected abstract void transitionFrom(final MainWindow mainWindow);
	}
	
}
