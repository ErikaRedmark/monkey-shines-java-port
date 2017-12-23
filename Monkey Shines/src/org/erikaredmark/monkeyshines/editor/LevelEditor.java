package org.erikaredmark.monkeyshines.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
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
import org.erikaredmark.monkeyshines.editor.dialog.ImportWorldDialog;
import org.erikaredmark.monkeyshines.editor.dialog.NewWorldDialog;
import org.erikaredmark.monkeyshines.editor.exception.BadEditorPersistantFormatException;
import org.erikaredmark.monkeyshines.editor.model.Template;
import org.erikaredmark.monkeyshines.editor.persist.TemplateXmlReader;
import org.erikaredmark.monkeyshines.editor.persist.TemplateXmlWriter;
import org.erikaredmark.monkeyshines.encoder.EncodedWorld;
import org.erikaredmark.monkeyshines.encoder.WorldIO;
import org.erikaredmark.monkeyshines.encoder.exception.WorldRestoreException;
import org.erikaredmark.monkeyshines.encoder.exception.WorldSaveException;
import org.erikaredmark.monkeyshines.graphics.exception.ResourcePackException;
import org.erikaredmark.monkeyshines.logging.MonkeyShinesLog;
import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.erikaredmark.monkeyshines.resource.WorldResource.UseIntent;
import org.erikaredmark.util.BinaryLocation;

/*
 * The main GUI for the level editor. Contains in it a JPanel just like the game that contains the current screen
 */
@SuppressWarnings("serial")
public class LevelEditor extends JFrame {
	private static final String CLASS_NAME = "org.erikaredmark.monkeyshines.editor.LevelEditor";
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
	
	private final JDesktopPane editorDesktop;
	private final JInternalFrame brushPaletteFrame;
	private final JInternalFrame canvasFrame;
	private final JInternalFrame templatePaletteFrame;
	// There may be many open templates at once. Stored as associative because editors should not open
	// twice for the same template.
	Map<Template, JInternalFrame> openTemplateEditors = new HashMap<>();
	
	// Package access intended; Makes it easier for palettes to perform actions defined
	final LevelDrawingCanvas currentWorld;

	// Initialised each time the current world changes. Null and not added to the pane
	// when there is no world. Callback called whenever a new world is loaded into the editor
	private BrushPalette brushPalette;
	private TemplatePalette templatePalette;
	
	private void paletteUpdateCallback(World world) {
		assert currentWorld != null : "Callback for palettes activated too early!";
		// Remove original palettes if exists, create the new one, add it, and pack it.
		if (brushPalette != null) {
			brushPaletteFrame.remove(brushPalette);
		}
		
		if (templatePalette != null) {
			templatePaletteFrame.remove(templatePalette);
		}
		
		brushPalette = new BrushPalette(LevelEditor.this, world.getResource() );
		brushPaletteFrame.add(brushPalette, BorderLayout.CENTER);
		brushPaletteFrame.setVisible(true);
		brushPaletteFrame.repaint();
		
		// Load templates for the given world. TODO for now we ignore issues
		List<Template> worldTemplates = Collections.emptyList();
		final Path editorPreferencesLocation = BinaryLocation.BINARY_LOCATION.getParent().resolve("editor_prefs.xml");
		if (Files.exists(editorPreferencesLocation) ) {
			try (InputStream is = Files.newInputStream(editorPreferencesLocation) ) {
				worldTemplates = 
					TemplateXmlReader.read(
						is, 
						world, 
						t -> {});
			} catch (IOException | BadEditorPersistantFormatException e) {
				LOGGER.log(Level.WARNING,
						   "Could not open editor preferences (editor will have default preferences and no templates loaded: ",
						   e);
		}
		}
		
		templatePalette = new TemplatePalette(
			currentWorld, 
			worldTemplates,
			world,
			newTemplates -> {
				try {
					TemplateXmlWriter.writeOutTemplatesForWorld(editorPreferencesLocation, world.getWorldName(), newTemplates);
				} catch (BadEditorPersistantFormatException e) {
					JOptionPane.showMessageDialog(LevelEditor.this, "Could not save template data to preferences: " + e.getMessage() );
					LOGGER.log(Level.SEVERE,
							   "Could not save template data to preferences: " + e.getMessage(),
							   e);
				}
			});
		
		templatePaletteFrame.add(templatePalette, BorderLayout.CENTER);
		templatePaletteFrame.setVisible(true);
		templatePaletteFrame.repaint();
	}

	/* Only set during loading a world, and only used during saving.	*/
	private Path defaultSaveLocation;
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
	
	/* ------------------------- MENU ITEM IMPORT WORLD --------------------------- */
	private JMenuItem importWorld = new JMenuItem("Import World...");
	
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
			currentWorld.setTileBrushAndId(PaintbrushType.PLACE_SPRITES, 0);
		}
	});
	
	/* ------------------------- MENU ITEM EDIT SPRITES --------------------------- */
	private JMenuItem editSprites = new JMenuItem(new AbstractAction("Edit Sprites") {
		private static final long serialVersionUID = 1L;
		@Override public void actionPerformed(ActionEvent e) {
			currentWorld.setTileBrushAndId(PaintbrushType.EDIT_SPRITES, 0);
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
			currentWorld.setTileBrushAndId(PaintbrushType.ERASER_SPRITES, 0);
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
	
	private JMenuItem exportAsPng = new JMenuItem(new AbstractAction("Export Level Map...") {
		private static final long serialVersionUID = 1L;
		@Override public void actionPerformed(ActionEvent e) {
			actionExportAsPng(1000);
		}
	});
	
	private JMenuItem exportAsPngBonus = new JMenuItem(new AbstractAction("Export Bonus Map...") {
		private static final long serialVersionUID = 1L;
		@Override public void actionPerformed(ActionEvent e) {
			World world = currentWorld.getWorldEditor().getWorld();
			if (world.screenIdExists(world.getBonusScreen() ) ) {
				actionExportAsPng(world.getBonusScreen() );
			} else {
				JOptionPane.showMessageDialog(LevelEditor.this,
				    "The bonus screen id " + world.getBonusScreen() + " does not exist.",
				    "Cannot Export Bonus Screens",
				    JOptionPane.ERROR_MESSAGE);
			}
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
	 * Exports the level asking for a file location as a .png. This is normally either called with level
	 * id 1000 for the main level, or the bonus level id for the bonus. There is no other physical way
	 * for any other playable levels to exist if they are not connected to either the bonus room or 1000,
	 * even if they are technically in the world data.
	 * <p/>
	 * If the given screen Id does not exist, this method will throw an exception. Check the id of the passed
	 * screen. 1000 will always exist but the bonus may not.
	 * 
	 * @param levelScreen
	 * 		id of the screen to start from (all connected screens will be exported as a .png)
	 * 
	 */
	public void actionExportAsPng(int levelScreen) {
		JFileChooser exportChooser = new JFileChooser();
		exportChooser.setDialogTitle("Save the generated map (as .png)");
		
		int selected = exportChooser.showSaveDialog(this);
		if (selected == JFileChooser.APPROVE_OPTION) {
			Path location = exportChooser.getSelectedFile().toPath();
			
			if (!(location.getFileName().toString().endsWith(".png") ) ) {
				location = location.getParent().resolve(location.getFileName().toString() + ".png");
			}
			
			BufferedImage map = MapGenerator.generateMap(currentWorld.getWorldEditor().getWorld(), levelScreen);
			
			try {
				ImageIO.write(map, "PNG", location.toFile() );
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this,
				    "Could not export world as .png: " + e.getMessage(),
				    "Export Failed",
				    JOptionPane.ERROR_MESSAGE);
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
		MonkeyShinesLog.initialise();
		newInstance();
	}
	
	/**
	 * 
	 * Called by BrushPalette when a brush is changed. Changes are propogated to interested frames.
	 * <p/>
	 * This should be called for every brush
	 * 
	 * @param brush
	 * @param id
	 */
	public void setTileBrushAndId(PaintbrushType brush, int id) {
		templatePalette.trySetTileIdAndBrush(brush, id);
		currentWorld.setTileBrushAndId(brush, id);
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
		
		editor.importWorld.setAction(new AbstractAction("Import World...") {
			private static final long serialVersionUID = 1L;
			@Override public void actionPerformed(ActionEvent e) {
				Path p = ImportWorldDialog.launch();
				// If the translation was succesful and no cancel, load world into editor.
				if (p != null) {
					editor.loadWorldNoisy(p);
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
		int downToMainCanvasEnd = (GameConstants.LEVEL_ROWS * GameConstants.TILE_SIZE_Y) + 40;
		brushPaletteFrame = new JInternalFrame("Palette", true);
		brushPaletteFrame.setLayout(new BorderLayout() );
		brushPaletteFrame.setSize(new Dimension(240, downToMainCanvasEnd) );
		
		templatePaletteFrame = new JInternalFrame("Templates", true);
		templatePaletteFrame.setLayout(new BorderLayout() );
		templatePaletteFrame.setSize(new Dimension(700, 260) );
		// Default dock it to bottom of editor
		templatePaletteFrame.setLocation(0, downToMainCanvasEnd + 4);
		// Finally, install listeners to handle resizing the tabbed pane to fit the parent and re-packing it.
		
		canvasFrame = new JInternalFrame("Level");
		canvasFrame.setLayout(new FlowLayout(FlowLayout.LEFT) );
		
		currentWorld = new LevelDrawingCanvas(this::paletteUpdateCallback);
		canvasFrame.add(currentWorld);
		canvasFrame.pack();
		
		setTitle("Monkey Shines Editor");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		setUpMenuBar();
		
		// Now, set up the desktop. The desktop will contain both frames
		editorDesktop = new JDesktopPane();
		editorDesktop.add(canvasFrame);
		// Give space for toolbar in left corner
		canvasFrame.setLocation(280, 0);
		canvasFrame.setVisible(true);
		
		editorDesktop.add(brushPaletteFrame);
		editorDesktop.add(templatePaletteFrame);
		// Set visible later when palette initialised
		add(editorDesktop);

		setPreferredSize(new Dimension(960, 800) );
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		setResizable(true);
	}

	private void setUpMenuBar() {
		fileMenu.add(newWorld);
		fileMenu.add(loadWorld);
		fileMenu.add(saveWorld);
		fileMenu.add(importWorld);
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
		specialMenu.addSeparator();
		specialMenu.add(exportAsPng);
		specialMenu.add(exportAsPngBonus);
		
		mainMenuBar.add(specialMenu);
		
		// Set up menus
		setJMenuBar(mainMenuBar);
	}

}
