package edu.nova.erikaredmark.monkeyshines.editor;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.google.common.base.Optional;

import edu.nova.erikaredmark.monkeyshines.*;
import edu.nova.erikaredmark.monkeyshines.editor.dialog.DialogLauncher;
import edu.nova.erikaredmark.monkeyshines.editor.dialog.GoToScreenDialog;
import edu.nova.erikaredmark.monkeyshines.editor.dialog.NewWorldDialog;


/*
 * The main GUI for the level editor. Contains in it a JPanel just like the game that contains the current screen
 */
public class LevelEditor extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1004295925422699855L;
	private LevelEditorMainCanvas currentWorld;
	private KeyboardInput keys;
	// Main menu Bar
	private JMenuBar mainMenuBar = new JMenuBar();
	
	// Menu: File operations
	private JMenu fileMenu = new JMenu("File");
	/* --------------------------- MENU ITEM NEW WORLD ---------------------------- */
	private final JMenuItem newWorld = new JMenuItem("New World..."); // Can't be defined yet due to requiring enclosing instance of JFrame
	
	/* -------------------------- MENU ITEM LOAD WORLD ---------------------------- */
	private JMenuItem loadWorld = new JMenuItem(new AbstractAction("Load World...") {
		private static final long serialVersionUID = 1L;
		@Override public void actionPerformed(ActionEvent e) {
			// TODO load world dialog
		}
	});
	
	/* -------------------------- MENU ITEM SAVE WORLD ---------------------------- */
	private JMenuItem saveWorld = new JMenuItem(new AbstractAction("Save World...") {
		private static final long serialVersionUID = 1L;
		@Override public void actionPerformed(ActionEvent e) {
			// TODO save world
		}
	});
	
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
	/* -------------------------- MENU ITEM PLACE GOODIES ------------------------- */
	private JMenuItem placeGoodies = new JMenuItem(new AbstractAction("Goodies") { 
		private static final long serialVersionUID = 1L;
		@Override public void actionPerformed(ActionEvent e) {
			currentWorld.actionSelectingGoodies();
		}
	});
	/* ------------------------- MENU ITEM PLACE SPRITES -------------------------- */
	private JMenuItem placeSprites =  new JMenuItem(new AbstractAction("Sprites") { 
		private static final long serialVersionUID = 1L;
		@Override public void actionPerformed(ActionEvent e) {
			currentWorld.actionPlacingSprites();
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
	
	public void actionGoToScreen() {
		if (currentWorld.getVisibleScreenEditor() == null) {
			JOptionPane.showMessageDialog(this,
				    "You must load a world first before being able to load a specific screen",
				    "World Not Loaded",
				    JOptionPane.ERROR_MESSAGE);
		}
		
		Optional<Integer> screenId = GoToScreenDialog.displayAndGetId(this, currentWorld.getVisibleScreenEditor().getId() );
		if (screenId.isPresent() ) {
			currentWorld.actionChangeScreen(screenId.get() );
		}
	}
	
	public static void main(String[] args) {
		newInstance();

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
				DialogLauncher.launch(editor, "New World...", new NewWorldDialog() );
			}
		});
		
		return editor;
	}
	
	private LevelEditor() {
		keys = new KeyboardInput();
		currentWorld = new LevelEditorMainCanvas(keys);
		// Must add to both.
		this.addKeyListener(keys);
		add(currentWorld);
		setTitle("Monkey Shines Editor");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(GameConstants.SCREEN_WIDTH, 
				GameConstants.SCREEN_HEIGHT);
		setLocationRelativeTo(null); // Why even set this?
		
		fileMenu.add(newWorld);
		fileMenu.add(loadWorld);
		fileMenu.add(saveWorld);
		
		mainMenuBar.add(fileMenu);
		
		placeTiles.add(placeSolids);
		placeTiles.add(placeThrus);
		placeTiles.add(placeScenes);
		placeTiles.addSeparator();
		placeTiles.add(placeGoodies);
		placeTiles.add(placeSprites);
		
		mainMenuBar.add(placeTiles);
		
		screenMenu.add(gotoScreen);
		
		mainMenuBar.add(screenMenu);
		
		// Set up menus
		setJMenuBar(mainMenuBar);
		
		setVisible(true);
		setResizable(false);
	}


}
