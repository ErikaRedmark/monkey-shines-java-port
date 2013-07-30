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

import javax.swing.JPanel;
import javax.swing.Timer;

import edu.nova.erikaredmark.monkeyshines.GameConstants;
import edu.nova.erikaredmark.monkeyshines.Goodie;
import edu.nova.erikaredmark.monkeyshines.KeyboardInput;
import edu.nova.erikaredmark.monkeyshines.Point2D;
import edu.nova.erikaredmark.monkeyshines.Tile.TileType;
import edu.nova.erikaredmark.monkeyshines.encoder.EncodedWorld;
import edu.nova.erikaredmark.monkeyshines.encoder.WorldIO;
import edu.nova.erikaredmark.monkeyshines.encoder.exception.WorldSaveException;
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
		currentState = EditorState.PLACING_TILES;
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
	
	
	// Snap the mouse cursor to the square. For example, 125 and 135 should all go down to 120
	public int snapMouseX(final int X) {
		int takeAwayX = X % GameConstants.TILE_SIZE_X;
		return X - takeAwayX;
	}
	
	public int snapMouseY(final int Y) {
		int takeAwayY = Y % GameConstants.TILE_SIZE_Y;
		return Y - takeAwayY;
	}

	public void actionPerformed(ActionEvent e) {
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
	
	/** User action to set state to placing solids																		*/
	public void actionPlacingSolids() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		currentTileType = PaintbrushType.SOLIDS;
		currentState = EditorState.PLACING_TILES;
	}
	
	/** User action to set state to placing thrus																		*/
	public void actionPlacingThrus() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		currentTileType = PaintbrushType.THRUS;
		currentState = EditorState.PLACING_TILES;
	}
	
	public void actionPlacingScenes() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		currentTileType = PaintbrushType.SCENES;
		currentState = EditorState.PLACING_TILES;
	}
	
	public void actionPlacingSprites() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		currentState = EditorState.PLACING_SPRITES;
	}
	
	public void actionPlacingGoodies() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		currentState = EditorState.PLACING_GOODIES;
	}
	
	public void actionSelectingTiles() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		currentState = EditorState.SELECTING_TILES;
	}
	
	public void actionSelectingGoodies() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		currentState = EditorState.SELECTING_GOODIES;
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

	@Override public void mouseClicked(MouseEvent e) {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		if (currentState == EditorState.PLACING_TILES)
			addTile(e.getX(), e.getY() );
		else if (currentState == EditorState.PLACING_GOODIES)
			addGoodie(e.getX(), e.getY() );
		else if (currentState == EditorState.SELECTING_TILES) {
			
			
			int tileId = resolveObjectId(currentTileSheet, e.getX(), e.getY() );
			
			//if (tileId < tilesPerRow * tilesPerCol) {
			currentTileID = tileId;
			currentState = EditorState.PLACING_TILES;
			//} else {
				// TODO better error here
				//System.out.println("Tile " + tileId + " doesn't exist.");
			//}
			
		} else if (currentState == EditorState.SELECTING_GOODIES) {
			int goodieId = resolveObjectId(currentWorldEditor.getWorldResource().getGoodieSheet(), e.getX(), e.getY() );
			
			currentGoodieId = goodieId;
			currentState = EditorState.PLACING_GOODIES;
		}
		
	}

	@Override public void mouseEntered(MouseEvent e) { }
	@Override public void mouseExited(MouseEvent e) { }
	@Override public void mousePressed(MouseEvent e) { }
	@Override public void mouseReleased(MouseEvent e) { }

	@Override public void mouseDragged(MouseEvent e) {
		mousePosition.setX(e.getX() );
		mousePosition.setY(e.getY() );
		if (currentState == EditorState.PLACING_TILES)
			addTile(mousePosition.x(), mousePosition.y() );

	}

	@Override public void mouseMoved(MouseEvent e) {
		// set the mouse position.
		mousePosition.setX(e.getX() );
		mousePosition.setY(e.getY() );
	}
	
	public void paint(Graphics g) {
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
	 * Represents the current state of the editor, like what is being placed. Note: If no world is loaded, many functions will
	 * not work, and state change will not be possible until a world is loaded.
	 * 
	 * @author Erika Redmark
	 *
	 */
	public enum EditorState { PLACING_TILES, SELECTING_TILES, PLACING_GOODIES, SELECTING_GOODIES, PLACING_SPRITES, SELECTING_SPRITES, NO_WORLD_LOADED; }

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
