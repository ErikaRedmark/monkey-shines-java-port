package edu.nova.erikaredmark.monkeyshines.editor;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Path;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.google.common.base.Optional;

import edu.nova.erikaredmark.monkeyshines.*;
import edu.nova.erikaredmark.monkeyshines.editor.LevelEditorMainCanvas.EditorState;
import edu.nova.erikaredmark.monkeyshines.editor.dialog.GoToScreenDialog;
import edu.nova.erikaredmark.monkeyshines.editor.dialog.NewWorldDialog;
import edu.nova.erikaredmark.monkeyshines.encoder.EncodedWorld;
import edu.nova.erikaredmark.monkeyshines.encoder.WorldIO;
import edu.nova.erikaredmark.monkeyshines.encoder.exception.WorldRestoreException;
import edu.nova.erikaredmark.monkeyshines.encoder.exception.WorldSaveException;
import edu.nova.erikaredmark.monkeyshines.graphics.exception.ResourcePackException;
import edu.nova.erikaredmark.monkeyshines.resource.WorldResource;


/*
 * The main GUI for the level editor. Contains in it a JPanel just like the game that contains the current screen
 */
public class LevelEditor extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1004295925422699855L;
	private LevelEditorMainCanvas currentWorld;
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
	
	// Menu: Place Stuff
	private JMenu placeTiles = new JMenu("Place Objects");
	/* -------------------------- MENU ITEM PLACE SOLIDS -------------------------- */
	private JMenuItem placeSolids = new JMenuItem(new AbstractAction("Solids") { 
		private static final long serialVersionUID = 1L;
		@Override public void actionPerformed(ActionEvent e) {
			currentWorld.actionPlacingSolids();
			currentWorld.actionSelectingTiles();
		}
	});
	/* --------------------------- MENU ITEM PLACE THRUS -------------------------- */
	private JMenuItem placeThrus = new JMenuItem(new AbstractAction("Thrus") { 
		private static final long serialVersionUID = 1L;
		@Override public void actionPerformed(ActionEvent e) {
			currentWorld.actionPlacingThrus();
			currentWorld.actionSelectingTiles();
		}
	});
	/* -------------------------- MENU ITEM PLACE SCENES -------------------------- */
	private JMenuItem placeScenes = new JMenuItem(new AbstractAction("Scenes") { 
		private static final long serialVersionUID = 1L;
		@Override public void actionPerformed(ActionEvent e) {
			currentWorld.actionPlacingScenes();
			currentWorld.actionSelectingTiles();
		}
	});
	/* ------------------------- MENU ITEM PLACE CONVEYERS ------------------------ */
	private JMenuItem placeConveyers = new JMenuItem(new AbstractAction("Conveyer Belts") { 
		private static final long serialVersionUID = 1L;
		@Override public void actionPerformed(ActionEvent e) {
			currentWorld.actionPlacingConveyers();
			currentWorld.actionSelectingConveyers();
		}
	});
	/* -------------------------- MENU ITEM PLACE GOODIES ------------------------- */
	private JMenuItem placeGoodies = new JMenuItem(new AbstractAction("Goodies") { 
		private static final long serialVersionUID = 1L;
		@Override public void actionPerformed(ActionEvent e) {
			currentWorld.actionSelectingGoodies();
		}
	});
	/* ---------------------------- MENU ITEM ERASER ------------------------------ */
	private JMenuItem eraser = new JMenuItem(new AbstractAction("Eraser") {
		private static final long serialVersionUID = 1L;
		@Override public void actionPerformed(ActionEvent e) {
			currentWorld.actionEraser();
		}
	});
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
	
	/* ------------------------ MENU ITEM DELETE SPRITES --------------------------- */
	private JMenuItem deleteSprites = new JMenuItem(new AbstractAction("Delete Sprites") {
		private static final long serialVersionUID = 1L;
		@Override public void actionPerformed(ActionEvent e) {
			currentWorld.actionDeletingSprites();
		}
	});
	
	// Menu: Hazards
	private JMenu hazardMenu = new JMenu("Hazards");
	/* -------------------------- MENU ITEM PLACE HAZARD -------------------------- */
	private JMenuItem placeHazard = new JMenuItem(new AbstractAction("Place Hazards") {
		private static final long serialVersionUID = 1L;
		@Override public void actionPerformed(ActionEvent e) {
			// Set paintbrush to hazards first
			currentWorld.actionPlacingHazards();
			// Allow user to select a hazard
			currentWorld.actionSelectingHazards();
		}
	});
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
	
	// Special
	private JMenu specialMenu = new JMenu("Special");
	/* -------------------------- MENU ITEM PLACE BONZO --------------------------- */
	private JMenuItem placeBonzo = new JMenuItem(new AbstractAction("Place Bonzo") {
		private static final long serialVersionUID = 1L;
		@Override public void actionPerformed(ActionEvent e) {
			currentWorld.actionPlaceBonzo();
		}
	});
	
	public void actionGoToScreen() {
		if (currentWorld.getVisibleScreenEditor() == null) {
			JOptionPane.showMessageDialog(this,
				    "You must load a world first before being able to load a specific screen",
				    "World Not Loaded",
				    JOptionPane.ERROR_MESSAGE);
		}
		
		Optional<Integer> screenId = GoToScreenDialog.displayAndGetId(this, currentWorld.getVisibleScreenEditor().getId() );
		if (screenId.isPresent() ) {
			int goToScreenId = screenId.get();
			if (currentWorld.screenExists(goToScreenId ) ) {
				currentWorld.actionChangeScreen(goToScreenId);
			} else {
				// Confirm the user wishes to add a new screen if one is not already present before we jump to the
				// new screen code
				int result = JOptionPane.showConfirmDialog(this, "This screen does not yet exist. Create screen " + goToScreenId + "?");
				if (result == JOptionPane.YES_OPTION) {
					currentWorld.actionChangeScreen(goToScreenId );
				}
			}
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
	 */
	public void loadWorld(final Path worldFile) throws WorldRestoreException, ResourcePackException {
		EncodedWorld world = WorldIO.restoreWorld(worldFile);
		// Try to load the resource pack
		String worldName = world.getName();
		Path packFile = worldFile.getParent().resolve(worldName + ".zip");
		WorldResource rsrc = WorldResource.fromPack(packFile);
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
		}
	}
	
	/**
	 * 
	 * Constructs a new instance of the level editor and assigns all the required actions that could not be assigned in the constructor
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
					// TODO send to a better logging framework
					ex.printStackTrace();
					JOptionPane.showMessageDialog(editor,
					    "Cannot Save World: " + ex.getMessage(),
					    "Saving Error",
					    JOptionPane.ERROR_MESSAGE);
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
		placeTiles.setEnabled(enable);
		screenMenu.setEnabled(enable);
		hazardMenu.setEnabled(enable);
		specialMenu.setEnabled(enable);
//		placeThrus.setEnabled(enable);
//		placeScenes.setEnabled(enable);
//		placeSprites.setEnabled(enable);
//		placeGoodies.setEnabled(enable);
//		gotoScreen.setEnabled(enable);
		
	}

	private LevelEditor() {
		keys = new KeyboardInput();
		currentWorld = new LevelEditorMainCanvas(keys);
		// Must add to both.
		this.addKeyListener(keys);
		add(currentWorld);
		setTitle("Monkey Shines Editor");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		
		fileMenu.add(newWorld);
		fileMenu.add(loadWorld);
		fileMenu.add(saveWorld);
		fileMenu.add(quit);
		
		mainMenuBar.add(fileMenu);
		
		placeTiles.add(placeSolids);
		placeTiles.add(placeThrus);
		placeTiles.add(placeScenes);
		placeTiles.add(placeConveyers);
		placeTiles.add(eraser);
		placeTiles.addSeparator();
		placeTiles.add(placeGoodies);
		placeTiles.add(placeSprites);
		placeTiles.add(editSprites);
		placeTiles.add(deleteSprites);
		
		mainMenuBar.add(placeTiles);
		
		hazardMenu.add(placeHazard);
		hazardMenu.add(editHazards);
		
		mainMenuBar.add(hazardMenu);
		
		screenMenu.add(gotoScreen);
		
		mainMenuBar.add(screenMenu);
		
		specialMenu.add(placeBonzo);
		
		mainMenuBar.add(specialMenu);
		
		// Set up menus
		setJMenuBar(mainMenuBar);
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		setResizable(false);
	}


}
