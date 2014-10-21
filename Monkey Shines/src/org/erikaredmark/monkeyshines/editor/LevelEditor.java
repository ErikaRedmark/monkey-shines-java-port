package org.erikaredmark.monkeyshines.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.swing.AbstractAction;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.erikaredmark.monkeyshines.*;
import org.erikaredmark.monkeyshines.editor.LevelDrawingCanvas.EditorState;
import org.erikaredmark.monkeyshines.editor.dialog.CopyPasteDialog;
import org.erikaredmark.monkeyshines.editor.dialog.CopyPasteDialog.CopyPasteConfiguration;
import org.erikaredmark.monkeyshines.editor.dialog.GoToScreenDialog;
import org.erikaredmark.monkeyshines.editor.dialog.NewWorldDialog;
import org.erikaredmark.monkeyshines.encoder.EncodedWorld;
import org.erikaredmark.monkeyshines.encoder.WorldIO;
import org.erikaredmark.monkeyshines.encoder.exception.WorldRestoreException;
import org.erikaredmark.monkeyshines.encoder.exception.WorldSaveException;
import org.erikaredmark.monkeyshines.graphics.exception.ResourcePackException;
import org.erikaredmark.monkeyshines.logging.MonkeyShinesLog;
import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.erikaredmark.monkeyshines.resource.WorldResource.UseIntent;
import org.erikaredmark.util.BinaryLocation;

import com.google.common.base.Function;


/*
 * The main GUI for the level editor. Contains in it a JPanel just like the game that contains the current screen
 */
@SuppressWarnings("serial")
public class LevelEditor extends JFrame {
	
	private final JDesktopPane editorDesktop;
	private final JInternalFrame paletteFrame;
	private final JInternalFrame canvasFrame;
	// Initialised each time the current world changes. Null and not added to the pane
	// when there is no world. Callback called whenever a new world is loaded into the editor
	private BrushPalette palette;
	private Function<WorldResource, Void> paletteUpdateCallback = new Function<WorldResource, Void>() {
		@Override public Void apply(WorldResource rsrc) {
			assert currentWorld != null : "Callback for palette activated too early!";
			// Remove original palette if exists, create the new one, add it, and pack it.
			if (palette != null) {
				paletteFrame.remove(palette);
			}
			palette = new BrushPalette(currentWorld, rsrc);
			paletteFrame.add(palette);
			paletteFrame.pack();
			paletteFrame.setVisible(true);
			return null;
		}
		
	};
	
	private final LevelDrawingCanvas currentWorld;
	/* Only set during loading a world, and only used during saving.	*/
	private Path defaultSaveLocation;
	
	private KeyboardInput keys;
	// Main menu Bar
	private JMenuBar mainMenuBar = new JMenuBar();
	
	// Menu: File operations
	private JMenu fileMenu = new JMenu("File");
	/* --------------------------- MENU ITEM NEW WORLD ---------------------------- */
	private final JMenuItem newWorld = new JMenuItem("New World..."); // Can't be defined yet due to requiring enclosing instance of JFrame
	
	/* -------------------------- MENU ITEM LOAD WORLD ---------------------------- */
	private JMenuItem loadWorld = new JMenuItem("Load World..."); 
	
	/* -------------------------- MENU ITEM SAVE WORLD ---------------------------- */
	private JMenuItem saveWorld = new JMenuItem("Save World...");
	
	/* ----------------------------- MENU ITEM QUIT ------------------------------- */
	private JMenuItem quit = new JMenuItem(new AbstractAction("Quit") {
		private static final long serialVersionUID = 1L;
		@Override public void actionPerformed(ActionEvent e) {
			// TODO better way of quitting: Also ask for save changes
			System.exit(0);
		}
	});
	
	private JMenu spritesMenu = new JMenu("Sprites");
	/* ------------------------- MENU ITEM PLACE SPRITES -------------------------- */
	private JMenuItem placeSprites =  new JMenuItem(new AbstractAction("Place Sprites") { 
		private static final long serialVersionUID = 1L;
		@Override public void actionPerformed(ActionEvent e) {
			currentWorld.actionPlacingSprites();
		}
	});
	
	/* ------------------------- MENU ITEM EDIT SPRITES --------------------------- */
	private JMenuItem editSprites = new JMenuItem(new AbstractAction("Edit Sprites") {
		private static final long serialVersionUID = 1L;
		@Override public void actionPerformed(ActionEvent e) {
			currentWorld.actionEditingSprites();
		}
	});
	
	private JMenuItem editOffscreenSprites = new JMenuItem(new AbstractAction("Edit Offscreen Sprites...") { 
		private static final long serialVersionUID = 1L;
		@Override public void actionPerformed(ActionEvent e) {
			currentWorld.actionEditOffscreenSprites();
		}
	});
	
	/* ------------------------ MENU ITEM DELETE SPRITES --------------------------- */
	private JMenuItem deleteSprites = new JMenuItem(new AbstractAction("Delete Sprites") {
		private static final long serialVersionUID = 1L;
		@Override public void actionPerformed(ActionEvent e) {
			currentWorld.actionEraserSprites();
		}
	});
	
	// Menu: Hazards
	private JMenu hazardMenu = new JMenu("Hazards");
	/* ------------------------ MENU ITEM EDITING HAZARDS ------------------------- */
	private JMenuItem editHazards = new JMenuItem(new AbstractAction("Edit Hazards...") {
		private static final long serialVersionUID = 1L;
		@Override public void actionPerformed(ActionEvent e) {
			actionEditHazards();
		}
	});
	
	
	// Menu: Screens
	private JMenu screenMenu = new JMenu("Screens");
	/* -------------------------- MENU ITEM GOTO SCREEN --------------------------- */
	private JMenuItem gotoScreen = new JMenuItem(new AbstractAction("Go To Screen...") { 
		private static final long serialVersionUID = 1L;
		@Override public void actionPerformed(ActionEvent e) {
			actionGoToScreen();
		}
	});
	
	private JMenuItem copyPasteScreen = new JMenuItem(new AbstractAction("Copy Screen To...") {
		private static final long serialVersionUID = 1L;
		@Override public void actionPerformed(ActionEvent e) {
			actionCopyPasteLevel();
		}
	});
	
	private JMenuItem changeBackground = new JMenuItem(new AbstractAction("Change Background...") {
		private static final long serialVersionUID = 1L;
		@Override public void actionPerformed(ActionEvent e) {
			currentWorld.actionChangeBackground();
		}
	});
	
	private JMenuItem resetScreen = new JMenuItem(new AbstractAction("Reset Screen") {
		private static final long serialVersionUID = 1L;
		@Override public void actionPerformed(ActionEvent e) {
			currentWorld.actionResetScreen();
		}
	});
	
	// Special
	private JMenu specialMenu = new JMenu("Special");
	/* -------------------------- MENU ITEM PLACE BONZO --------------------------- */
	private JMenuItem placeBonzo = new JMenuItem(new AbstractAction("Place Bonzo") {
		private static final long serialVersionUID = 1L;
		@Override public void actionPerformed(ActionEvent e) {
			currentWorld.actionPlaceBonzo();
		}
	});
	
	private JMenuItem setAuthor = new JMenuItem(new AbstractAction("Author...") {
		private static final long serialVersionUID = 1L;
		@Override public void actionPerformed(ActionEvent e) {
			currentWorld.actionSetAuthor();
		}
	});
	
	private JMenuItem setBonus = new JMenuItem(new AbstractAction("Set Bonus Screen...") {
		private static final long serialVersionUID = 1L;
		@Override public void actionPerformed(ActionEvent e) {
			actionSelectBonusScreen();
		}
	});

	// Returns false if a world is not loaded, after letting the user know
	// a world is not loaded.
	private boolean warnAndStopIfWorldNotLoaded() {
		if (currentWorld.getVisibleScreenEditor() == null) {
			JOptionPane.showMessageDialog(this,
			    "You must load a world first before being able to load a specific screen",
			    "World Not Loaded",
			    JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		return true;
	}
	
	public void actionGoToScreen() {
		if (!(warnAndStopIfWorldNotLoaded() ) ) {
			return;
		}
		
		int oldScreenId = currentWorld.getVisibleScreenEditor().getId();
		int screenId = GoToScreenDialog.displayAndGetId(oldScreenId, currentWorld.getWorldEditor().getWorld(), true);
		// If user changed screen id.
		if (screenId != oldScreenId) {
			if (currentWorld.screenExists(screenId ) ) {
				currentWorld.actionChangeScreen(screenId);
			} else {
				// Confirm the user wishes to add a new screen if one is not already present before we jump to the
				// new screen code
				int result = JOptionPane.showConfirmDialog(this, "This screen does not yet exist. Create screen " + screenId + "?");
				if (result == JOptionPane.YES_OPTION) {
					currentWorld.actionChangeScreen(screenId);
				}
			}
		}
	}
	
	public void actionSelectBonusScreen() {
		if (!(warnAndStopIfWorldNotLoaded() ) ) {
			return;
		}
		
		final World world = currentWorld.getWorldEditor().getWorld();
		
		int selectedScreenId = GoToScreenDialog.displayAndGetId(world.getBonusScreen(), world, false);
		
		world.setBonusScreen(selectedScreenId);
	}
	
	public void actionCopyPasteLevel() {
		CopyPasteConfiguration config = CopyPasteDialog.launch(currentWorld.getVisibleScreenEditor().getId(), currentWorld.getWorldEditor().getWorld() );
		if (config != null) {
			currentWorld.getWorldEditor().copyAndPasteLevel(config.copyFromId, config.copyToId);
		}
	}
	
	/**
	 * 
	 * Brings up the GUI editor for the list of hazards saved for that world.
	 * 
	 */
	public void actionEditHazards() {
		currentWorld.openEditHazards();
	}
	
	public static void main(String[] args) {
		MonkeyShinesLog.initialise();
		newInstance();
	}
	
	/**
	 * 
	 * Loads a world from the given path. Worlds must conform to the standard; {@code <worldName>.world} for the
	 * level data and {@code <worldName>.zip} for the resource pack with proper resources. 
	 * 
	 * @param path
	 * 		level to load. This should be the .world file, NOT the resource pack
	 * 
	 * @throws WorldRestoreException
	 * 		if the world cannot be loaded due to an issue with the .world file
	 * 
	 * @throws ResourcePackException
	 * 		if the world cannot be loaded due to an issue with the resource pack. This is generally
	 * 		less serious than an issue with the .world file, as the resource pack can be easily
	 * 		modified
	 * 
	 * @throws IOException
	 * 		if a low level I/O error prevents loading the world
	 * 
	 */
	public void loadWorld(final Path worldFile) throws WorldRestoreException, ResourcePackException, IOException {
		EncodedWorld world = WorldIO.restoreWorld(worldFile);
		// Try to load the resource pack
		String fileName = worldFile.getFileName().toString();
		String worldName = fileName.substring(0, fileName.lastIndexOf('.') );
		Path packFile = worldFile.getParent().resolve(worldName + ".zip");
		WorldResource rsrc = WorldResource.fromPack(packFile, UseIntent.EDITOR);
		this.currentWorld.loadWorld(world, rsrc);
		this.defaultSaveLocation = worldFile;
		this.manipulationFunctions(true);
	}
	
	/**
	 * 
	 * Delegates to {@code loadWorld}, catching any exceptions and printing them to the error
	 * console in addition to showing an error message to the user.
	 * 
	 * @param worldFile
	 * 		level to load. This should be the .world file, NOT the resource pack
	 * 
	 */
	private void loadWorldNoisy(final Path worldFile) {
		try {
			loadWorld(worldFile);
		} catch (WorldRestoreException ex) {
			JOptionPane.showMessageDialog(this,
			    "Cannot load world: Possibly corrupt or not a world file: " + ex.getMessage(),
			    "Loading Error",
			    JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		} catch (ResourcePackException ex) {
			JOptionPane.showMessageDialog(this,
			    "Resource pack issues: " + ex.getMessage(),
			    "Loading Error",
			    JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this,
			    "Low level I/O issue: " + ex.getMessage(),
			    "Loading Error",
			    JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}
	}
	
	/**
	 * 
	 * Constructs a new instance of the level editor and assigns all the required actions that could not be assigned in the constructor.
	 * Actions that can't be assigned during construction include references to the level editor itself.
	 * 
	 * @return
	 * 		new instance of this object
	 * 
	 */
	public static LevelEditor newInstance() {
		final LevelEditor editor = new LevelEditor();
		
		editor.newWorld.setAction(new AbstractAction("New World...") {
			private static final long serialVersionUID = 1L;
			@Override public void actionPerformed(ActionEvent e) {
				// Setup dialog
				final JDialog dialog = new JDialog(editor);
				dialog.setLayout(new BorderLayout() );
				dialog.setTitle("New World...");
				dialog.setModal(true);
				// Center window
				Point pos = editor.getLocation();
				dialog.setLocation(pos.x + 10, pos.y + 10 );
				
				// Setup custom content for dialog
				NewWorldDialog theNewWorld = new NewWorldDialog(dialog);
				dialog.add(theNewWorld, BorderLayout.CENTER);
				dialog.pack();
				
				// Launch (blocking operation)
				dialog.setVisible(true);
				
				// Did the user actually create a world? If so, load it up now!
				Path worldSave = theNewWorld.getModel().getSaveLocation();
				if (worldSave != null) {
					editor.loadWorldNoisy(worldSave);
				}
			}
		});
		
		editor.loadWorld.setAction(new AbstractAction("Load World...") {
			private static final long serialVersionUID = 1L;
			@Override public void actionPerformed(ActionEvent e) {
				
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(BinaryLocation.BINARY_LOCATION.toFile() );
				if (fileChooser.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION) {
					File worldFile = fileChooser.getSelectedFile();
					
					editor.loadWorldNoisy(worldFile.toPath() );
				}
			}
		});
		
		editor.saveWorld.setAction(new AbstractAction("Save World...") {
			private static final long serialVersionUID = 1L;
			@Override public void actionPerformed(ActionEvent e) {
				if (editor.currentWorld.getState() == EditorState.NO_WORLD_LOADED) 
					throw new IllegalStateException("Save should not be enabled when a world is not loaded");
					
				// Perform world sanity checks. We stop here if anything wrong goes... wrong.
								
				Path saveTo;
				if (editor.defaultSaveLocation != null) saveTo = editor.defaultSaveLocation;
				else {
					// TODO save as
					System.out.println("Save as not implemented yet");
					saveTo = null;
				}
				try {
					editor.currentWorld.saveWorld(saveTo);
				} catch (WorldSaveException ex) {
					JOptionPane.showMessageDialog(editor,
					    "Cannot Save World: Possible world corruption: " + ex.getMessage(),
					    "Saving Error",
					    JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(editor,
					    "Cannot Save World: I/O error: " + ex.getMessage(),
					    "Saving Error",
					    JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
				}
			}
		}); 
		
		// Disable all editor manipulation functions since no world is loaded by default
		editor.manipulationFunctions(false);
		
		return editor;
	}
	

	/**
	 * 
	 * Enables or disables menu items in the editor that require a world to be loaded. When no world is loaded, menu 
	 * functions manipulating the world should be disabled. Otherwise, they should be enabled. Any state changes the user
	 * makes to load/unload a world should produce a state change here to enable/disable the proper menus.
	 * 
	 * @param enable
	 */
	private void manipulationFunctions(boolean enable) {
		saveWorld.setEnabled(enable);
		screenMenu.setEnabled(enable);
		hazardMenu.setEnabled(enable);
		specialMenu.setEnabled(enable);
		spritesMenu.setEnabled(enable);
	}

	private LevelEditor() {
		keys = new KeyboardInput();
		paletteFrame = new JInternalFrame("Palette");
		paletteFrame.setLayout(new FlowLayout(FlowLayout.LEFT) );
		
		canvasFrame = new JInternalFrame("Level");
		canvasFrame.setLayout(new FlowLayout(FlowLayout.LEFT) );
		
		currentWorld = new LevelDrawingCanvas(keys, paletteUpdateCallback);
		canvasFrame.add(currentWorld);
		canvasFrame.pack();
		// Must add to both.
		this.addKeyListener(keys);
		setTitle("Monkey Shines Editor");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		setUpMenuBar();
		
		// Now, set up the desktop. The desktop will contain both frames
		editorDesktop = new JDesktopPane();
		editorDesktop.add(canvasFrame);
		// Give space for toolbar in left corner
		canvasFrame.setLocation(220, 0);
		canvasFrame.setVisible(true);
		
		editorDesktop.add(paletteFrame);
		// Set visible later when palette initialised
		add(editorDesktop);

		setPreferredSize(new Dimension(900, 640) );
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		setResizable(true);
	}

	private void setUpMenuBar() {
		fileMenu.add(newWorld);
		fileMenu.add(loadWorld);
		fileMenu.add(saveWorld);
		fileMenu.add(quit);
		
		mainMenuBar.add(fileMenu);
		
		spritesMenu.add(placeSprites);
		spritesMenu.add(editSprites);
		spritesMenu.add(editOffscreenSprites);
		spritesMenu.add(deleteSprites);
		mainMenuBar.add(spritesMenu);

		hazardMenu.add(editHazards);
		
		mainMenuBar.add(hazardMenu);
		
		screenMenu.add(gotoScreen);
		screenMenu.add(copyPasteScreen);
		screenMenu.add(changeBackground);
		screenMenu.add(resetScreen);
		
		mainMenuBar.add(screenMenu);
		
		specialMenu.add(placeBonzo);
		specialMenu.add(setAuthor);
		specialMenu.add(setBonus);
		
		mainMenuBar.add(specialMenu);
		
		// Set up menus
		setJMenuBar(mainMenuBar);
	}

}
