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
import java.nio.file.Path;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.Timer;

import com.google.common.base.Optional;

import edu.nova.erikaredmark.monkeyshines.GameConstants;
import edu.nova.erikaredmark.monkeyshines.Goodie;
import edu.nova.erikaredmark.monkeyshines.ImmutablePoint2D;
import edu.nova.erikaredmark.monkeyshines.KeyboardInput;
import edu.nova.erikaredmark.monkeyshines.Point2D;
import edu.nova.erikaredmark.monkeyshines.Sprite;
import edu.nova.erikaredmark.monkeyshines.Tile.TileType;
import edu.nova.erikaredmark.monkeyshines.editor.dialog.SpriteChooserDialog;
import edu.nova.erikaredmark.monkeyshines.editor.dialog.SpritePropertiesDialog;
import edu.nova.erikaredmark.monkeyshines.editor.dialog.SpritePropertiesModel;
import edu.nova.erikaredmark.monkeyshines.encoder.EncodedWorld;
import edu.nova.erikaredmark.monkeyshines.encoder.WorldIO;
import edu.nova.erikaredmark.monkeyshines.encoder.exception.WorldSaveException;
import edu.nova.erikaredmark.monkeyshines.graphics.CoreResource;
import edu.nova.erikaredmark.monkeyshines.graphics.WorldResource;


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
	private int currentSpriteID;
	private int currentGoodieId;
	
	// Current overlay graphic
	private BufferedImage currentTileSheet;
	
	// THESE MAY BE NULL!
	private LevelScreenEditor currentScreenEditor;
	private WorldEditor currentWorldEditor;
	
	private Timer editorFakeGameTimer;
	private Dimension windowSize;
	
	private KeyboardInput keys;
	
	private PaintbrushType currentTileType;
	private EditorState    currentState;
	
	public LevelEditorMainCanvas(final KeyboardInput keys) {
		super();
		currentTileID = 0;
		currentGoodieId = 0;
		currentSpriteID = 0;
		currentTileType = PaintbrushType.SOLIDS;
		currentState = EditorState.NO_WORLD_LOADED;
		this.keys=keys;
		setVisible(true);
		
		// Set the Keyboard Input
		this.addKeyListener(keys);
		addMouseMotionListener(this);
		addMouseListener(this);
		
		//. Optimisations
		setDoubleBuffered(true);
		
		windowSize = getSize();
		// Main timer for game
		// 30
		
		mousePosition = Point2D.of(0, 0);
		
		editorFakeGameTimer = new Timer(100, this);
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
	 * @throws
	 * 		WorldSaveException
	 * 			if an error is occurred saving this editors state to the given file
	 * 
	 */
	public void saveWorld(final Path location)  throws WorldSaveException {
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
	 * @throws
	 * 		IllegalStateException
	 * 			if no world is loaded in the editor
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
	public void addTile(final int mouseX, final int mouseY) {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		currentScreenEditor.setTile(mouseX / GameConstants.TILE_SIZE_X,
				mouseY / GameConstants.TILE_SIZE_Y,
				paintbrush2TileType(currentTileType),
				currentTileID);
	}
	
	/**
	 * Resolves the location the person clicked and places the selected Goodie on
	 * the map.
	 * @param x 
	 * @param y
	 */
	public void addGoodie(final int x, final int y) {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		currentWorldEditor.addGoodie(x / GameConstants.GOODIE_SIZE_X, 
				y / GameConstants.GOODIE_SIZE_Y, 
				currentScreenEditor.getId(), 
				Goodie.Type.byValue(currentGoodieId) );
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
	
	/**
	 * 
	 * Changes the internal state of the editor from one state to the other, making any additional updates as needed.
	 * 
	 * @param newState
	 */
	private void changeState(EditorState newState) {
		currentState = newState;
	}
	
	public void actionChangeScreen(Integer screenId) {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		currentScreenEditor = currentWorldEditor.getLevelScreenEditor(screenId);
		currentWorldEditor.changeCurrentScreen(currentScreenEditor);
	}
	
	public int resolveObjectId(final BufferedImage sheet, final int x, final int y) {
		int width = sheet.getWidth();
		//int height = sheet.getHeight();
		// We need to resolve the location. Find out how many rows and cols the sheet has, and from there
		// pick out based on where the mouse clicked what tileType we selected.
		// Modulo to check if they are divisble TODO
		int tilesPerRow = width / GameConstants.TILE_SIZE_X;
		//int tilesPerCol = height / GameConstants.TILE_SIZE_Y;
		
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
			}
		}
		
	}
	
	public TileType paintbrush2TileType(PaintbrushType paintbrush) {
		TileType type = null;
		switch (paintbrush) {
		case SOLIDS:
			type = TileType.SOLID;
			break;
		case THRUS:
			type = TileType.THRU;
			break;
		case SCENES:
			type = TileType.SCENE;
			break;
		default:
			throw new IllegalArgumentException("Paintbrush type " + currentTileType.toString() + " not a valid tile type");
		}
		
		assert type != null;
		return type;
	}
	
	public enum PaintbrushType { SOLIDS, THRUS, SCENES, SPRITES, GOODIES; }
	
	
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
				
				editor.currentTileID = tileId;
				editor.changeState(EditorState.PLACING_TILES);
			}
			@Override public void defaultDragAction(LevelEditorMainCanvas editor) { 
				/* No Drag Action */
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
				
				editor.currentGoodieId = goodieId;
				editor.changeState(EditorState.PLACING_GOODIES);
			}
			@Override public void defaultDragAction(LevelEditorMainCanvas editor) { 
				/* No Drag Action */
			}
		}, 
		
		PLACING_SPRITES {
			@Override public void defaultClickAction(LevelEditorMainCanvas editor) { 
				SpritePropertiesModel model = SpritePropertiesDialog.launch(editor, editor.currentWorldEditor.getWorldResource(), ImmutablePoint2D.of(editor.mousePosition.x(), editor.mousePosition.y() ) );
				if (model.isOkay() ) {
					editor.currentScreenEditor.addSprite(model.getSpriteId(), model.getSpriteStartingLocation(), model.getSpriteBoundingBox(), model.getSpriteVelocity(), editor.currentWorldEditor.getWorldResource() );
				}
			}
			@Override public void defaultDragAction(LevelEditorMainCanvas editor) { }
		},
		
		EDITING_SPRITES {
			@Override public void defaultClickAction(LevelEditorMainCanvas editor) { 
				Optional<Sprite> selected = editor.resolveSpriteAtLocation(ImmutablePoint2D.from(editor.mousePosition) );
				if (selected.isPresent() ) {
					// Open properties editor for sprite
					SpritePropertiesModel model = SpritePropertiesDialog.launch(editor, editor.currentWorldEditor.getWorldResource(), selected.get() );
					// Remove sprite from screen, create a new one with new properties.
					editor.currentScreenEditor.removeSprite(selected.get() );
					editor.currentScreenEditor.addSprite(model.getSpriteId(), model.getSpriteStartingLocation(), model.getSpriteBoundingBox(), model.getSpriteVelocity(), editor.currentWorldEditor.getWorldResource() );
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
		}; }

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
