package edu.nova.erikaredmark.monkeyshines.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.Timer;

import com.google.common.base.Optional;

import edu.nova.erikaredmark.monkeyshines.Conveyer;
import edu.nova.erikaredmark.monkeyshines.GameConstants;
import edu.nova.erikaredmark.monkeyshines.Goodie;
import edu.nova.erikaredmark.monkeyshines.Hazard;
import edu.nova.erikaredmark.monkeyshines.ImmutablePoint2D;
import edu.nova.erikaredmark.monkeyshines.KeyboardInput;
import edu.nova.erikaredmark.monkeyshines.Point2D;
import edu.nova.erikaredmark.monkeyshines.Sprite;
import edu.nova.erikaredmark.monkeyshines.editor.dialog.EditHazardsDialog;
import edu.nova.erikaredmark.monkeyshines.editor.dialog.EditHazardsModel;
import edu.nova.erikaredmark.monkeyshines.editor.dialog.SpriteChooserDialog;
import edu.nova.erikaredmark.monkeyshines.editor.dialog.SpritePropertiesDialog;
import edu.nova.erikaredmark.monkeyshines.editor.dialog.SpritePropertiesModel;
import edu.nova.erikaredmark.monkeyshines.encoder.EncodedWorld;
import edu.nova.erikaredmark.monkeyshines.encoder.WorldIO;
import edu.nova.erikaredmark.monkeyshines.encoder.exception.WorldSaveException;
import edu.nova.erikaredmark.monkeyshines.resource.CoreResource;
import edu.nova.erikaredmark.monkeyshines.resource.WorldResource;
import edu.nova.erikaredmark.monkeyshines.tiles.ConveyerTile;
import edu.nova.erikaredmark.monkeyshines.tiles.HazardTile;
import edu.nova.erikaredmark.monkeyshines.tiles.StatelessTileType;
import edu.nova.erikaredmark.monkeyshines.tiles.TileType;


/**
 * This window runs the same Engine that the main game runs. The only difference is that game mechanics
 * such as physics and collision, are not available. What is available is Sprite movement and graphics.
 * <p/>
 * Running this class, instead of MonkeyShines, starts up the level editor, which uses the same base
 * classes. In this mode, the user has full control over level editing, and can save their work.
 * 
 * @author Erika Redmark
 */
/* Despite inheriting from a serialisable class, this class is not designed to be serialised presently.					*/
@SuppressWarnings("serial")
public final class LevelEditorMainCanvas extends JPanel implements ActionListener, MouseListener, MouseMotionListener {

	// Point2D of the mouse position
	private Point2D mousePosition;
	
	// Information about what clicking something will do
	private int currentTileID;
	private Goodie.Type currentGoodieType;
	
	// Unlike the other properties, current hazard MAY be null. It is 100% possible for a world to not have hazards
	private Hazard currentHazard;
	// Used for hazards and conveyers. Indicates which id of conveyer/hazard to draw a new instance of, since those
	// tile types are stateful.
	private int specialId;
	
	// Current overlay graphic
	private BufferedImage currentTileSheet;
	
	// THESE MAY BE NULL!
	private LevelScreenEditor currentScreenEditor;
	private WorldEditor currentWorldEditor;
	
	private Timer editorFakeGameTimer;
	
	private KeyboardInput keys;
	
	private PaintbrushType currentTileType;
	private EditorState    currentState;
	
	public LevelEditorMainCanvas(final KeyboardInput keys) {
		super();
		currentTileID = 0;
		currentGoodieType = Goodie.Type.BANANA; // Need to pick something for default. Bananas are good.
		currentTileType = PaintbrushType.SOLIDS;
		currentState = EditorState.NO_WORLD_LOADED;
		
		setMinimumSize(
				new Dimension(GameConstants.SCREEN_WIDTH, 
					          GameConstants.SCREEN_HEIGHT) );
		
		setPreferredSize(
				new Dimension(GameConstants.SCREEN_WIDTH, 
					          GameConstants.SCREEN_HEIGHT) );
		this.keys=keys;
		setVisible(true);
		
		// Set the Keyboard Input
		this.addKeyListener(keys);
		addMouseMotionListener(this);
		addMouseListener(this);
		
		// Optimisations
		setDoubleBuffered(true);
		
		mousePosition = Point2D.of(0, 0);
		
		editorFakeGameTimer = new Timer(GameConstants.EDITOR_SPEED, this);
		editorFakeGameTimer.start();
		

	}
	
	/**
	 * 
	 * Decodes a living instance of the encoded world into the editor.
	 * 
	 * @param world
	 * 		the encoded world to load into the editor and start editing
	 * 
	 */
	public void loadWorld(final EncodedWorld world, final WorldResource rsrc) {
		currentWorldEditor = WorldEditor.fromEncoded(world, rsrc);
		currentScreenEditor = currentWorldEditor.getLevelScreenEditor(1000);
		changeState(EditorState.PLACING_TILES);
	}
	
	/**
	 * 
	 * Saves the current state of the world in the editor to the given file
	 * 
	 * @param location
	 * 		the location to save to
	 * 
	 * @throws WorldSaveException
	 * 		if an error is occurred saving this editors state to the given file due to
	 * 		the world being in a corrupt state
	 * 
	 * @throws IOException
	 * 		if a low level I/O error prevents saving the world
	 * 
	 */
	public void saveWorld(final Path location) throws WorldSaveException, IOException {
		WorldIO.saveOnlyWorld(this.currentWorldEditor, location);
	}
	
	/**
	 * 
	 * Returns the current state of this object.
	 * 
	 */
	public EditorState getState() {
		return this.currentState;
	}
	
	/**
	 * 
	 * Determines if the screen pointed to by the id exists
	 * 
	 * @param id
	 * 		id of screen to check for
	 * 
	 * @return
	 * 		{@code true} if the	screen exists by that id, {@code false} if otherwise
	 * 
	 * @throws IllegalStateException
	 * 		if no world is loaded in the editor
	 * 
	 */
	public boolean screenExists(int id) {
		if (this.currentState == EditorState.NO_WORLD_LOADED) throw new IllegalArgumentException("No world loaded");
		return this.currentWorldEditor.screenExists(id);
	}
	
	/**
	 * Resolves the location the person clicked and places the currently selected tile
	 * on the map. Does nothing if there is no screen editor loaded.
	 * 
	 * @param mouseX mouse click location X co-ord
	 * @param mouseY mouse click location Y co-ord
	 */
	private void addTile(final int mouseX, final int mouseY) {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		currentScreenEditor.setTile(
				mouseX / GameConstants.TILE_SIZE_X,
				mouseY / GameConstants.TILE_SIZE_Y,
				paintbrush2TileType(currentTileType),
				currentTileID);
	}
	
	/**
	 * Adds the hazard to the given location. The current hazard selected in this editor is added to the given place. If 
	 * there is no currently selected hazard (its null) this method will do nothing
	 * <p/>
	 * IMPORTANT: This creates a new instance of the hazard tile with the given id paired with the given hazard. if the hazard types
	 * are rearranged and changed it won't affect any already placed. Will need to work on a solution.
	 */
	private void addHazard(final int mouseX, final int mouseY) {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		if (currentHazard == null)  return;
		
		currentScreenEditor.setTile(mouseX / GameConstants.TILE_SIZE_X,
									mouseY / GameConstants.TILE_SIZE_Y,
									HazardTile.forHazard(currentHazard),
									specialId);
	}
	
	/**
	 * Resolves the location the person clicked and places the selected Goodie on
	 * the map.
	 * @param x 
	 * @param y
	 */
	private void addGoodie(final int x, final int y) {
		if (this.currentState == EditorState.NO_WORLD_LOADED)  return;
		
		currentWorldEditor.addGoodie(x / GameConstants.GOODIE_SIZE_X, 
								 	 y / GameConstants.GOODIE_SIZE_Y, 
									 currentScreenEditor.getId(), 
									 currentGoodieType );
	}
	
	
	/**
	 * 
	 * Erases both tile and goodie data at the specified position
	 * 
	 * @param mouseX
	 * @param mouseY
	 * 
	 */
	private void eraseAt(final int mouseX, final int mouseY) {
		if (this.currentState == EditorState.NO_WORLD_LOADED)  return;
		
		int row = mouseX / GameConstants.TILE_SIZE_X;
		int col = mouseY / GameConstants.TILE_SIZE_Y;
		
		currentScreenEditor.eraseTile(row, col);
		
		currentWorldEditor.removeGoodie(row, col, currentScreenEditor.getId() );
	}
	

	/** 
	 *
	 * Sets bonzos starting location on the currently loaded screen. This method accepts pixel mouse coordinates, and will
	 * automatically snap them to the right tile. The tile clicked becomes the upper-left tile of bonzos 2 by 2 character.
	 *
	 * @param x
	 * 		x location, in pixels
	 * 
	 * @param y
	 * 		y location, in pixels
	 */
	public void setBonzo(final int x, final int y) {
		currentWorldEditor.setBonzo(snapMouseX(x) / GameConstants.TILE_SIZE_X, snapMouseY(y) / GameConstants.TILE_SIZE_Y, currentScreenEditor.getId() );
	}
	
	// Snap the mouse cursor to the square. For example, 125 and 135 should all go down to 120
	public int snapMouseX(final int X) {
		int takeAwayX = X % GameConstants.TILE_SIZE_X;
		return X - takeAwayX;
	}
	
	public int snapMouseY(final int Y) {
		int takeAwayY = Y % GameConstants.TILE_SIZE_Y;
		return Y - takeAwayY;
	}

	@Override public void actionPerformed(ActionEvent e) {
		// Poll Keyboard
		keys.poll();
		
		// Do not allow state changes if no world is loaded!
		
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		// When person hits spacebar, bring up the sprite sheet at 0,0, and next click
		// selected the tile depending on the position clicked vs the sprites.
		if (keys.keyDown(KeyEvent.VK_SPACE) ) {
			if (currentState == EditorState.PLACING_TILES) actionSelectingTiles();
			else if (currentState == EditorState.PLACING_GOODIES) actionSelectingGoodies();
		}
		
		if (currentState == EditorState.PLACING_TILES) {
			if (keys.keyDown(KeyEvent.VK_S) ) actionPlacingSolids();
		    else if (keys.keyDown(KeyEvent.VK_T) ) actionPlacingThrus();
			else if (keys.keyDown(KeyEvent.VK_B) ) actionPlacingScenes();
			else if (keys.keyDown(KeyEvent.VK_P) ) actionPlacingSprites();
			else if (keys.keyDown(KeyEvent.VK_G) ) actionPlacingGoodies();
		}
		
		repaint();
	}
	
	/* These actions are called from user input.																		*/
	
	// TODO a lot of these 'changeState(EditorState.PLACING_TILES)' can probably be removed
	/** User action to set state to placing solids																		*/
	public void actionPlacingSolids() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		currentTileType = PaintbrushType.SOLIDS;
		changeState(EditorState.PLACING_TILES);
	}
	
	/** User action to set state to placing thrus																		*/
	public void actionPlacingThrus() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		currentTileType = PaintbrushType.THRUS;
		changeState(EditorState.PLACING_TILES);
	}
	
	public void actionPlacingScenes() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		currentTileType = PaintbrushType.SCENES;
		changeState(EditorState.PLACING_TILES);
	}
	
	public void actionPlacingConveyers() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		currentTileType = PaintbrushType.CONVEYERS;
		changeState(EditorState.PLACING_TILES);
	}
	
	public void actionSelectingConveyers() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		changeState(EditorState.SELECTING_CONVEYERS);
	}
	
	public void actionPlacingSprites() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		changeState(EditorState.PLACING_SPRITES);
	}
	
	public void actionEditingSprites() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		changeState(EditorState.EDITING_SPRITES);
	}
	

	public void actionDeletingSprites() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		changeState(EditorState.DELETING_SPRITES);
	}
	
	public void actionPlacingGoodies() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		changeState(EditorState.PLACING_GOODIES);
	}
	
	// Called by most menu actions that ask to paint such and such type of tile. The first action
	// would specify 'start painting this type' and this action means 'show sprites on the screen
	// for selection'
	public void actionSelectingTiles() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		changeState(EditorState.SELECTING_TILES);
	}
	
	public void actionSelectingGoodies() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		changeState(EditorState.SELECTING_GOODIES);
	}

	public void actionPlaceBonzo() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		changeState(EditorState.PLACING_BONZO);
	}
	
	public void actionEraser() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		changeState(EditorState.ERASING_TILES);
	}
	
	/**
	 * 
	 * Blocks and opens a window for the level editor that allows editing of hazards in the map.
	 * 
	 */
	public void openEditHazards() {
		EditHazardsModel model = EditHazardsDialog.launch(this, currentWorldEditor.getWorldResource(), currentWorldEditor.getHazards() );
		// Sync any changes back to save state
		currentWorldEditor.setHazards(model.getHazards() );
	}
	
	/*
	 * Sets the current paintbrush for hazards, and changes state to PLACING_TILES.
	 */
	public void actionPlacingHazards() {
		if (this.currentState == EditorState.NO_WORLD_LOADED)  return;
		currentTileType = PaintbrushType.HAZARDS;
		
		changeState(EditorState.PLACING_TILES);
	}
	
	/*
	 * Changes state to SELECTING_HAZARDS, which will cause the hazard graphics picker to draw
	 * and allow selection
	 */
	public void actionSelectingHazards() {
		if (this.currentState == EditorState.NO_WORLD_LOADED)  return;
		
		changeState(EditorState.SELECTING_HAZARDS);
	}
	/**
	 * 
	 * Changes the internal state of the editor from one state to the other, making any additional updates as needed.
	 * 
	 * @param newState
	 * 
	 */
	private void changeState(EditorState newState) {
		currentState = newState;
	}
	
	public void actionChangeScreen(Integer screenId) {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		currentScreenEditor = currentWorldEditor.getLevelScreenEditor(screenId);
		currentWorldEditor.changeCurrentScreen(currentScreenEditor);
	}
	
	/**
	 * 
	 * This is for when a sprite sheet is shown on the level editor and the user selected a row/col to select a tile. 
	 * The method takes the sprite sheet reference itself, along with the location clicked, and attempts to determine
	 * the id of the tile/object that was clicked, based on the location clicked and the dimensions of the sprite 
	 * sheet under the idea that the sheet starts from the top left of the screen
	 * <p/>
	 * Assumptions: This is used for basic sheets that handle tile entities that are of of size {@code GameConstants.TILE_SIZE_X}
	 * by {@code GameConstants.TILE_SIZE_Y}
	 * 
	 * @param sheet
	 * 		the sprite sheet currently shown on the screen to the user
	 * 
	 * @param x
	 * 		x location of mouse click
	 * 
	 * @param y
	 * 		y location of mouse click
	 * 
	 * @return
	 * 		the id of the object the user selected. If the selection is outside the dimensions of the sheet, then 
	 * 		{@code -1} is returned, indicating no selection (ids can only be positive)
	 * 
	 */
	private int resolveObjectId(final BufferedImage sheet, final int x, final int y) {
		final int width = sheet.getWidth();
		final int height = sheet.getHeight();
		// Sanity check: Is this click within the sheet bounds?
		if ( x > width || y > height)  return -1;
		
		//int height = sheet.getHeight();
		// We need to resolve the location. Find out how many rows and cols the sheet has, and from there
		// pick out based on where the mouse clicked what tileType we selected.
		// Precondition: click was within bounds (checked earlier)
		int tilesPerRow = width / GameConstants.TILE_SIZE_X;
		
		int tileClickedX = x / GameConstants.TILE_SIZE_X;
		int tileClickedY = y / GameConstants.TILE_SIZE_Y;
		
		// Calulate
		int tileId = tileClickedX;
		tileId += tilesPerRow * tileClickedY;
		
		return tileId;
	}
	
	/**
	 * 
	 * Gets the sprite at the given location, bringing up a popup dialog to select between multiple if there are multiple
	 * in the given location.
	 * 
	 * @param location
	 * 		the location to resolve the sprites
	 * 	
	 * @return
	 * 		a single sprite at the given location, or no sprite it none such exist
	 * 
	 */
	public Optional<Sprite> resolveSpriteAtLocation(ImmutablePoint2D location) {
		List<Sprite> sprites = currentScreenEditor.getSpritesWithin(location, 3);
		switch(sprites.size() ) {
		case 0: return Optional.absent();
		case 1: return Optional.of(sprites.get(0) );
		// Most complex, requires dialog.
		default: return SpriteChooserDialog.launch(this, sprites, this.currentWorldEditor.getWorldResource() );
		}
	}

	@Override public void mouseClicked(MouseEvent e) {
		mousePosition.setX(e.getX() );
		mousePosition.setY(e.getY() );
		currentState.defaultClickAction(this);
	}

	@Override public void mouseEntered(MouseEvent e) { }
	@Override public void mouseExited(MouseEvent e) { }
	@Override public void mouseReleased(MouseEvent e) { }
	@Override public void mousePressed(MouseEvent e) { }
	
	@Override public void mouseDragged(MouseEvent e) {
		mousePosition.setX(e.getX() );
		mousePosition.setY(e.getY() );
		currentState.defaultDragAction(this);

	}

	@Override public void mouseMoved(MouseEvent e) {
		mousePosition.setX(e.getX() );
		mousePosition.setY(e.getY() );
	}
	
	@Override public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;

		/* If the current world is null, there is no data to load. Draw a white screen*/
		if (currentState == EditorState.NO_WORLD_LOADED) {
			g2d.setColor(Color.WHITE);
			g2d.fillRect(0, 0, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
			
		} else {
			currentWorldEditor.paint(g2d);

			// Paint the mouse box

			g2d.setColor(Color.green);
			g2d.drawRect(snapMouseX(mousePosition.x() ),
						 snapMouseY(mousePosition.y() ), 
						 GameConstants.TILE_SIZE_X, GameConstants.TILE_SIZE_Y);

			// BELOW: Additional overlays that are not part of the actual world
			
			// Draw bonzo starting location of screen
			final BufferedImage bonz = CoreResource.INSTANCE.getTransparentBonzo();
			final ImmutablePoint2D start = this.currentScreenEditor.getBonzoStartingLocation();
			final int startX = start.x() * GameConstants.TILE_SIZE_X;
			final int startY = start.y() * GameConstants.TILE_SIZE_Y;
			g2d.drawImage(bonz, startX, startY, startX + bonz.getWidth(), startY + bonz.getHeight(),
					0, 0, bonz.getWidth(), bonz.getHeight(),
					null);
			
			// If we are selecting a tile, draw the whole sheet to the side.
			if (currentState == EditorState.SELECTING_TILES) {
				currentTileSheet = currentWorldEditor.getWorldResource().getTilesheetFor(paintbrush2TileType(currentTileType) );
				g2d.drawImage(currentTileSheet, 0, 0, currentTileSheet.getWidth(), currentTileSheet.getHeight(), null );
			} else if (currentState == EditorState.SELECTING_GOODIES) {
				BufferedImage goodieSheet = currentWorldEditor.getWorldResource().getGoodieSheet(); // This contains their animation, so chop it in half.
				g2d.drawImage(goodieSheet, 0, 0, goodieSheet.getWidth(), goodieSheet.getHeight() / 2, // Destination
										   0, 0, goodieSheet.getWidth(), goodieSheet.getHeight() / 2, // Source
										   null);
			} else if (currentState == EditorState.SELECTING_HAZARDS) {
				BufferedImage hazardSheet = currentWorldEditor.getWorldResource().getHazardSheet();  // Like the goodie sheet, half height becomes second row has animation
				g2d.drawImage(hazardSheet, 0, 0, hazardSheet.getWidth(), hazardSheet.getHeight() / 2, // Destination
										   0, 0, hazardSheet.getWidth(), hazardSheet.getHeight() / 2, // Source
										   null);
			} else if (currentState == EditorState.SELECTING_CONVEYERS) {
				BufferedImage conveyerSheet = currentWorldEditor.getWorldResource().getEditorConveyerSheet();
				g2d.drawImage(conveyerSheet, 0, 0, conveyerSheet.getWidth(), conveyerSheet.getHeight(),
							  				 0, 0, conveyerSheet.getWidth(), conveyerSheet.getHeight(),
							  				 null);
			}
		}
		
	}
	
	// This isn't a member of paintbrush type because only a few types are actually tiles.
	/**
	 * 
	 * Converts the paintbrush type to a paintable tile type.
	 * <p/>
	 * Because some tile types have state, the return type from this method should be used to populate at most 1
	 * tile in the world.
	 * 
	 * @param paintbrush
	 * 
	 * @return
	 * 		a tile type that can be painted into the world. The return of this method should not be used to
	 * 		populate more than 1 location in the world
	 * 
	 */
	public TileType paintbrush2TileType(PaintbrushType paintbrush) {
		TileType type = null;
		switch (paintbrush) {
		case SOLIDS:
			type = StatelessTileType.SOLID;
			break;
		case THRUS:
			type = StatelessTileType.THRU;
			break;
		case SCENES:
			type = StatelessTileType.SCENE;
			break;
		case HAZARDS:
			// Stateful type: Create new based on id. Properties of hazard will be based on World
			// properties.
			// This instance will NOT be added to the world itself!! It must be copied, or multiple hazards may
			// end up sharing state (like hitting one bomb will blow up every other bomb painted with the same
			// paintbrush).
			type = HazardTile.forHazard(currentWorldEditor.getHazards().get(this.specialId) );
			break;
		case CONVEYERS:
			// Stateful type: Create new based on id. All state information is simply graphical drawing
			// so it isn't too difficult to create.
			// Please note: Special Id is assigned by the editor to be the INDEX in the conveyer list,
			// which would be the actual Conveyer id times 2, plus one IF anti-clockwise.
			type = new ConveyerTile(currentWorldEditor.getConveyers().get(specialId) );
			break;
		default:
			throw new IllegalArgumentException("Paintbrush type " + currentTileType.toString() + " not a valid tile type");
		}
		
		assert type != null;
		return type;
	}
	
	public enum PaintbrushType { SOLIDS, THRUS, SCENES, HAZARDS, SPRITES, GOODIES, CONVEYERS; }
	
	
	/**
	 * 
	 * Interface implemented only by EditorState. Intended to allow action information to be included in the state object itself
	 * to prevent instanceof bugs.
	 * <p/>
	 * Mouse location and all other editor properties must be set before calling the state methods.
	 * 
	 */
	private interface EditorStateAction {
		/** Action for the editor in this state during a mouse click
		 */
		public void defaultClickAction(LevelEditorMainCanvas editor);
		
		/** Action for the editor in this state during a mouse drag
		 */
		public void defaultDragAction(LevelEditorMainCanvas editor);
	}
	
	/**
	 * 
	 * Represents the current state of the editor, like what is being placed. Note: If no world is loaded, many functions will
	 * not work, and state change will not be possible until a world is loaded.
	 * <p/>
	 * No states need check for nulls because client code will make sure state changes only occur when allowed
	 * 
	 * @author Erika Redmark
	 *
	 */
	public enum EditorState implements EditorStateAction { 
		PLACING_TILES {
			@Override public void defaultClickAction(LevelEditorMainCanvas editor) { 
				editor.addTile(editor.mousePosition.x(), editor.mousePosition.y() );
			}
			@Override public void defaultDragAction(LevelEditorMainCanvas editor) {
				defaultClickAction(editor);
			}
		}, 
		
		/* Click actions on this type will always produce a state change to PLACING_TILES */
		SELECTING_TILES {
			@Override public void defaultClickAction(LevelEditorMainCanvas editor) { 
				int tileId = editor.resolveObjectId(editor.currentTileSheet, editor.mousePosition.x(), editor.mousePosition.y() );
				
				// do nothing if click is out of bounds
				if (tileId == -1)  return;
				
				editor.currentTileID = tileId;
				editor.changeState(EditorState.PLACING_TILES);
			}
			@Override public void defaultDragAction(LevelEditorMainCanvas editor) { 
				/* No Drag Action */
			}
		},
		
		/* Erases BOTH tiles and goodies from a space (although editor should enforce that goodies never occupy the
		 * same space as tiles)
		 */
		ERASING_TILES {
			@Override public void defaultClickAction(LevelEditorMainCanvas editor) { 
				editor.eraseAt(editor.mousePosition.x(), editor.mousePosition.y() );
			}
			@Override public void defaultDragAction(LevelEditorMainCanvas editor) { 
				defaultClickAction(editor);
			}
		},
		
		PLACING_GOODIES {
			@Override public void defaultClickAction(LevelEditorMainCanvas editor) { 
				editor.addGoodie(editor.mousePosition.x(), editor.mousePosition.y() );
			}
			@Override public void defaultDragAction(LevelEditorMainCanvas editor) { 
				defaultClickAction(editor);
			}
		}, 
		
		/* Click actions on this type will always produce a state change to PLACING_GOODIES */
		SELECTING_GOODIES {
			@Override public void defaultClickAction(LevelEditorMainCanvas editor) { 
				int goodieId = editor.resolveObjectId(editor.currentWorldEditor.getWorldResource().getGoodieSheet(), editor.mousePosition.x(), editor.mousePosition.y() );
				
				// do nothing if click is out of bounds
				if (goodieId == -1)  return;
				
				editor.currentGoodieType = Goodie.Type.byValue(goodieId);
				editor.changeState(EditorState.PLACING_GOODIES);
			}
			@Override public void defaultDragAction(LevelEditorMainCanvas editor) { 
				/* No Drag Action */
			}
		}, 
		
		SELECTING_HAZARDS {
			@Override public void defaultClickAction(LevelEditorMainCanvas editor) { 
				int hazardId = editor.resolveObjectId(editor.currentWorldEditor.getWorldResource().getHazardSheet(), 
													  editor.mousePosition.x(), 
													  editor.mousePosition.y() );
				
				// do nothing if click out of bounds
				if (hazardId == -1)  return;
				
				// We may not have a defined hazard
				List<Hazard> availableHazards = editor.currentWorldEditor.getHazards();
				if (hazardId >= availableHazards.size() )  return;
				
				// We have a valid hazard reference
				
				editor.currentHazard = availableHazards.get(hazardId);
				editor.specialId = hazardId;
				// Hazards are considered a tile
				editor.changeState(EditorState.PLACING_TILES);
			}
			
			@Override public void defaultDragAction(LevelEditorMainCanvas editor) { 
				defaultClickAction(editor);
			}
		},
		
		// Displays the editor sprite sheet for conveyers, setting the specialId to be indicative of the INDEX
		// in the conveyers list of the conveyer type selected.
		SELECTING_CONVEYERS {
			@Override public void defaultClickAction(LevelEditorMainCanvas editor) {
				int conveyerId = editor.resolveObjectId(editor.currentWorldEditor.getWorldResource().getEditorConveyerSheet(),
														editor.mousePosition.x(), 
														editor.mousePosition.y() );
				
				// Editor sprite sheet takes care of this id automatically. The sprites in the sheet, each
				// frame, map 1:1 with the index of the conveyers array. Just need to be basic house-keeping
				// checks
				if (conveyerId == -1)  return;
				
				// Check if we even have a conveyer at that index, otherwise another out of bounds click
				List<Conveyer> conveyers = editor.currentWorldEditor.getConveyers();
				if (conveyerId >= conveyers.size() )  return;
				
				// Valid conveyer. The id alone is enough
				editor.specialId = conveyerId;
				editor.changeState(EditorState.PLACING_TILES);
			}
			
			@Override public void defaultDragAction(LevelEditorMainCanvas editor) {
				defaultClickAction(editor);
			}
		},
		
		PLACING_HAZARDS {
			@Override public void defaultClickAction(LevelEditorMainCanvas editor) { 
				editor.addHazard(editor.mousePosition.x(), editor.mousePosition.y() );
			}
			
			@Override public void defaultDragAction(LevelEditorMainCanvas editor) { 
				defaultClickAction(editor);
			}
		},
		
		PLACING_SPRITES {
			@Override public void defaultClickAction(LevelEditorMainCanvas editor) { 
				SpritePropertiesModel model = SpritePropertiesDialog.launch(editor, editor.currentWorldEditor.getWorldResource(), ImmutablePoint2D.of(editor.mousePosition.x(), editor.mousePosition.y() ) );
				if (model.isOkay() ) {
					editor.currentScreenEditor.addSprite(model.getSpriteId(), model.getSpriteStartingLocation(), model.getSpriteBoundingBox(), model.getSpriteVelocity(), model.getAnimationType(), model.getAnimationSpeed(), editor.currentWorldEditor.getWorldResource() );
				}
			}
			@Override public void defaultDragAction(LevelEditorMainCanvas editor) { 
				/* No Drag Action */
			}
		},
		
		EDITING_SPRITES {
			@Override public void defaultClickAction(LevelEditorMainCanvas editor) { 
				Optional<Sprite> selected = editor.resolveSpriteAtLocation(ImmutablePoint2D.from(editor.mousePosition) );
				if (selected.isPresent() ) {
					// Open properties editor for sprite
					SpritePropertiesModel model = SpritePropertiesDialog.launch(editor, editor.currentWorldEditor.getWorldResource(), selected.get() );
					// Remove sprite from screen, create a new one with new properties.
					editor.currentScreenEditor.removeSprite(selected.get() );
					editor.currentScreenEditor.addSprite(model.getSpriteId(), model.getSpriteStartingLocation(), model.getSpriteBoundingBox(), model.getSpriteVelocity(), model.getAnimationType(), model.getAnimationSpeed(), editor.currentWorldEditor.getWorldResource() );
				}
			}
			@Override public void defaultDragAction(LevelEditorMainCanvas editor) { }
		},
		
		DELETING_SPRITES {
			@Override public void defaultClickAction(LevelEditorMainCanvas editor) { 
				Optional<Sprite> selected = editor.resolveSpriteAtLocation(ImmutablePoint2D.from(editor.mousePosition) );
				if (selected.isPresent() ) {
					editor.currentScreenEditor.removeSprite(selected.get() );
				}
			}
			@Override public void defaultDragAction(LevelEditorMainCanvas editor) { }
		},
		
		SELECTING_SPRITES {
			@Override public void defaultClickAction(LevelEditorMainCanvas editor) { }
			@Override public void defaultDragAction(LevelEditorMainCanvas editor) { }
		}, 
		
		PLACING_BONZO {
			@Override public void defaultClickAction(LevelEditorMainCanvas editor) { 
				editor.setBonzo(editor.mousePosition.x(), editor.mousePosition.y() );
			}
			@Override public void defaultDragAction(LevelEditorMainCanvas editor) {
				defaultClickAction(editor);
			}
		}, 
		
		NO_WORLD_LOADED {
			@Override public void defaultClickAction(LevelEditorMainCanvas editor) { }
			@Override public void defaultDragAction(LevelEditorMainCanvas editor) { }
		};
	}

	/**
	 * Returns the visible screen editor. Note that this may return {@code null} if no world is loaded!
	 * 
	 * @return
	 * 		the current screen editor, or {@code null} if no world is loaded
	 * 
	 */
	public LevelScreenEditor getVisibleScreenEditor() {
		return this.currentScreenEditor;
	}

	



}
