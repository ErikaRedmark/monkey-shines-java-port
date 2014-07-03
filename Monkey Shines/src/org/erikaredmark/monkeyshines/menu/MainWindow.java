package org.erikaredmark.monkeyshines.menu;

import java.awt.Component;
import java.io.IOException;
import java.nio.file.Path;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.erikaredmark.monkeyshines.KeyboardInput;
import org.erikaredmark.monkeyshines.World;
import org.erikaredmark.monkeyshines.encoder.EncodedWorld;
import org.erikaredmark.monkeyshines.encoder.WorldIO;
import org.erikaredmark.monkeyshines.encoder.exception.WorldRestoreException;
import org.erikaredmark.monkeyshines.graphics.exception.ResourcePackException;
import org.erikaredmark.monkeyshines.resource.WorldResource;

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
	
	private GameState state = GameState.NONE;
	
	// current key setup is stored so it can be removed as an observer from the main window during state
	// transitions
	private KeyboardInput currentKeyListener;
	
	private final Runnable playGameCallback = new Runnable() {
		@Override public void run() {
			setGameState(GameState.PLAYING);
		}
	};
	
	// TODO level selection window, sound control window, keyboard mapping, credits, and some placeholder for registration?
	public MainWindow() { 
		setTitle("Monkey Shines");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		// Do not set size here: It would include title bar. JPanel is added and packed
		// during state transition to size window correctly.
		
		// Initial state is menu.
		setGameState(GameState.MENU);
		// Now we can set relative to, since window is already sized properly.
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	// Sets the new game state and calls transition methods to modify the proper
	// state information for this object
	// returns whether the state change was successful.
	private boolean setGameState(final GameState state) {
		// This method will actually modify the state variable stored in this object automatically.
		return state.transitionTo(this);
	}
	
	private final Runnable resetCallback = new Runnable() {
		@Override public void run() {
			setGameState(GameState.MENU);
		}
	};
	

	/**
	 * 
	 * Attempts to load a world by giving the user a file chooser. If a world is loaded, bonzo is set and
	 * gameplay begins proper. Otherwise, state does not transition to playing and user remains back on main
	 * menu.
	 * <p/>
	 * The world will be fully constructed and set up when returned and can be passed directly to a 
	 * {@code GameWindow} to be played.
	 * 
	 * @param parent
	 * 		the parent component for displaying the dialog on
	 * 
	 * @return
	 * 		the selected world, or {@code null} if no world was selected.
	 * 
	 */
	public static World loadCustomWorld(final Component parent) {
		JFileChooser fileChooser = new JFileChooser();
		if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			Path worldFile = fileChooser.getSelectedFile().toPath();
			try {
				EncodedWorld world = WorldIO.restoreWorld(worldFile);
				// Try to load the resource pack
				String fileName = worldFile.getFileName().toString();
				// Remove .world extension so we can substitute with .zip.
				String worldName = fileName.substring(0, fileName.lastIndexOf('.') );
				Path packFile = worldFile.getParent().resolve(worldName + ".zip");
				WorldResource rsrc = WorldResource.fromPack(packFile);
				return world.newWorldInstance(rsrc);
			} catch (WorldRestoreException ex) {
				JOptionPane.showMessageDialog(parent,
				    "Cannot load world: Possibly corrupt or not a world file: " + ex.getMessage(),
				    "Loading Error",
				    JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
			} catch (ResourcePackException ex) {
				JOptionPane.showMessageDialog(parent,
				    "Resource pack issues: " + ex.getMessage(),
				    "Loading Error",
				    JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(parent,
				    "Low level I/O error: " + ex.getMessage(),
				    "Loading Error",
				    JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
			}
		}
		
		// no world chosen if method hasn't returned yet
		return null;
	}
	
	private enum GameState {
		// Note: always change state at END of method. Perform any possible actions that could prevent
		// state change before actually modifying any state variables.
		PLAYING {
			@Override public boolean transitionTo(final MainWindow mainWindow) {
				World userWorld = loadCustomWorld(mainWindow);
				if (userWorld == null)  return false;
				
				mainWindow.state.transitionFrom(mainWindow);
				
				mainWindow.currentKeyListener = new KeyboardInput();
				mainWindow.runningGame = new GameWindow(mainWindow.currentKeyListener, mainWindow.resetCallback, userWorld);
				// Must add to both.
				mainWindow.addKeyListener(mainWindow.currentKeyListener);
				mainWindow.add(mainWindow.runningGame);
				mainWindow.pack();
				
				mainWindow.state = this;
				return true;
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
			@Override public boolean transitionTo(MainWindow mainWindow) {
				mainWindow.state.transitionFrom(mainWindow);
				mainWindow.menu = new MainMenuWindow(mainWindow.playGameCallback);
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
