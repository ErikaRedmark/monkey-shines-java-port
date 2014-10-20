package org.erikaredmark.monkeyshines.editor;

import java.awt.AlphaComposite;
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

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.erikaredmark.monkeyshines.Conveyer;
import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.Goodie;
import org.erikaredmark.monkeyshines.Hazard;
import org.erikaredmark.monkeyshines.ImmutablePoint2D;
import org.erikaredmark.monkeyshines.KeyboardInput;
import org.erikaredmark.monkeyshines.Point2D;
import org.erikaredmark.monkeyshines.Sprite;
import org.erikaredmark.monkeyshines.background.Background;
import org.erikaredmark.monkeyshines.editor.dialog.AuthorshipDialog;
import org.erikaredmark.monkeyshines.editor.dialog.EditHazardsDialog;
import org.erikaredmark.monkeyshines.editor.dialog.EditHazardsModel;
import org.erikaredmark.monkeyshines.editor.dialog.SetBackgroundDialog;
import org.erikaredmark.monkeyshines.editor.dialog.SpriteChooserDialog;
import org.erikaredmark.monkeyshines.editor.dialog.SpritePropertiesDialog;
import org.erikaredmark.monkeyshines.editor.dialog.SpritePropertiesModel;
import org.erikaredmark.monkeyshines.encoder.EncodedWorld;
import org.erikaredmark.monkeyshines.encoder.WorldIO;
import org.erikaredmark.monkeyshines.encoder.exception.WorldSaveException;
import org.erikaredmark.monkeyshines.resource.CoreResource;
import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.erikaredmark.monkeyshines.tiles.CollapsibleTile;
import org.erikaredmark.monkeyshines.tiles.CommonTile;
import org.erikaredmark.monkeyshines.tiles.ConveyerTile;
import org.erikaredmark.monkeyshines.tiles.HazardTile;
import org.erikaredmark.monkeyshines.tiles.TileType;
import org.erikaredmark.monkeyshines.tiles.CommonTile.StatelessTileType;

import com.google.common.base.Function;
import com.google.common.base.Optional;


/**
 * 
 * This window runs the same game engine that the game runs, and allows the user to edit the world. 
 * <p/>
 * Brush types are set from {@code BrushPalette} window.
 * 
 * @author Erika Redmark
 */

@SuppressWarnings("serial")
public final class LevelDrawingCanvas extends JPanel implements ActionListener, MouseListener, MouseMotionListener {

	// Point2D of the mouse position
	private Point2D mousePosition;
	
	// Information about what clicking something will do
	// current tile is used for all types of tiles, and determines which specific graphic resource
	// a given type of tile will use.
	private int currentTileId;
	private Goodie.Type currentGoodieType;
	
	// Unlike the other properties, current hazard MAY be null. It is 100% possible for a world to not have hazards
	private Hazard currentHazard;
	
	// Current indicator (drawn at the cursor/tile position based on editor state). If null, a green square is drawn
	// instead.
	private BufferedImage indicatorImage;
	
	private final Function<WorldResource, Void> worldLoaded;
	
	// THESE MAY BE NULL!
	private LevelScreenEditor currentScreenEditor;
	private WorldEditor currentWorldEditor;
	
	private Timer editorFakeGameTimer;
	
	private KeyboardInput keys;
	
	private PaintbrushType currentTileType;
	private EditorState    currentState;
	
	
	/**
	 * 
     * Creates the canvas, is designed to display and allow editing of worlds.
	 * 
	 * @param keys
	 * 		key input to allow the canvas to respond to keyboard shortcuts
	 * 
	 * @param worldLoaded
	 * 		a callback that is called when a new world is loaded, indicating the resource for that world
	 * 		for other functions at require it in the main editor
	 * 
	 */
	public LevelDrawingCanvas(final KeyboardInput keys, final Function<WorldResource, Void> worldLoaded) {
		super();
		this.worldLoaded = worldLoaded;
		currentTileId = 0;
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
	 * Decodes a living instance of the encoded world into the editor. If a world instance already existed,
	 * the {@code WorldResource} object associated is disposed first
	 * 
	 * @param world
	 * 		the encoded world to load into the editor and start editing
	 * 
	 */
	public void loadWorld(final EncodedWorld world, final WorldResource rsrc) {
		if (currentWorldEditor != null) {
			currentWorldEditor.getWorldResource().dispose();
		}
		currentWorldEditor = WorldEditor.fromEncoded(world, rsrc);
		currentScreenEditor = currentWorldEditor.getLevelScreenEditor(1000);
		changeState(EditorState.PLACING_TILES);
		worldLoaded.apply(rsrc);
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
	 * Returns the current world editor for the level editor. This call will fail if called when no
	 * world is loaded.
	 * 
	 * @return
	 * 		world editor, never {@code null}
	 * 
	 * @throws IllegalStateException
	 * 		if no world is loaded in the editor
	 * 
	 */
	public WorldEditor getWorldEditor() {
		if (this.currentState == EditorState.NO_WORLD_LOADED)  throw new IllegalStateException("No world loaded");
		return this.currentWorldEditor;
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
				paintbrush2TileType(currentTileType) );
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
									HazardTile.forHazard(currentHazard) );
	}
	
	/**
	 * Resolves the location the person clicked and places the selected Goodie on
	 * the map.
	 * @param x 
	 * @param y
	 */
	private void addGoodie(final int x, final int y) {
		if (this.currentState == EditorState.NO_WORLD_LOADED)  return;
		
		currentWorldEditor.addGoodie(currentScreenEditor.getId(),
									 x / GameConstants.GOODIE_SIZE_X, 
								 	 y / GameConstants.GOODIE_SIZE_Y, 
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
		
		currentWorldEditor.removeGoodie(currentScreenEditor.getId(), row, col);
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
			if (currentState == EditorState.PLACING_GOODIES) actionSelectingGoodies();
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
	}
	
	/** User action to set state to placing thrus																		*/
	public void actionPlacingThrus() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		currentTileType = PaintbrushType.THRUS;
	}
	
	public void actionPlacingScenes() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		currentTileType = PaintbrushType.SCENES;
	}
	
	public void actionPlacingConveyers() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		currentTileType = PaintbrushType.CONVEYERS;
	}
	
	public void actionSelectingConveyers() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		changeState(EditorState.SELECTING_CONVEYERS);
	}
	
	public void actionPlacingCollapsibles() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		currentTileType = PaintbrushType.COLLAPSIBLE;
	}
	
	public void actionSelectingCollapsibles() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		changeState(EditorState.SELECTING_COLLAPSIBLE);
	}
	
	public void actionPlacingSprites() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		changeState(EditorState.PLACING_SPRITES);
	}
	
	public void actionEditingSprites() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		changeState(EditorState.EDITING_SPRITES);
	}
	
	public void actionEditOffscreenSprites() {
		List<Sprite> sprites = currentScreenEditor.getSpritesOutOfBounds();
		switch(sprites.size() ) {
		case 0: 
			JOptionPane.showMessageDialog(this, "There are no offscreen sprites to edit");
			break;
		case 1: { 
			Sprite s = sprites.get(0);
			SpritePropertiesModel model = SpritePropertiesDialog.launch(this, this.currentWorldEditor.getWorldResource(), s);
			changeSpriteFromDialogModel(s, model);
			break;
		}
		default: {
			Optional<Sprite> sOp = SpriteChooserDialog.launch(this, sprites, this.currentWorldEditor.getWorldResource() );
			if (sOp.isPresent() ) {
				Sprite s = sOp.get();
				SpritePropertiesModel model = SpritePropertiesDialog.launch(this, this.currentWorldEditor.getWorldResource(), s);
				changeSpriteFromDialogModel(s, model);
			}
		}
		
		}
	}

	public void actionDeletingSprites() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		changeState(EditorState.DELETING_SPRITES);
	}
	
	public void actionPlacingGoodies() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		changeState(EditorState.PLACING_GOODIES);
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
	public void actionResetScreen() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		reloadCurrentScreen();
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
	
	/**
	 * 
	 * Loads up a background picker for the world. Whatever the user's selection is, the background will be set
	 * to that.
	 * 
	 */
	public void actionChangeBackground() {
		Background newBackground = SetBackgroundDialog.launch(this.currentWorldEditor.getWorldResource(), this.currentScreenEditor.getBackground() );
	    currentScreenEditor.setBackground(newBackground);
	}
	
	/**
	 * 
	 * Gives the user a text field to enter the name of the author or authors of the world, or technically speaking whatever they want.
	 * If the dialog is cancelled the author is unchanged.
	 * 
	 */
	public void actionSetAuthor() {
		final String newAuthor = AuthorshipDialog.launch(currentWorldEditor.getWorld().getAuthor() );
		currentWorldEditor.getWorld().setAuthor(newAuthor);
	}
	
	/**
	 * 
	 * Changes the internal state of the editor from one state to the other, making any additional updates as needed.
	 * 
	 * @param newState
	 * 
	 */
	private void changeState(EditorState newState) {
		// Make no state changes if the state isn't actually changing.
		if (currentState == newState)  return;
		
		if (newState == EditorState.EDITING_SPRITES) {
			// Set a condition for the game timer to stop animating sprites.
			// Stopping the timer completely would look like a freeze, so we don't do that.
			currentScreenEditor.stopAnimatingSprites();
		} else {
			// Transitioning out of editing sprites state always restores the sprite animation.
			currentScreenEditor.startAnimatingSprites();
		}
		currentState = newState;
	}
	
	public void actionChangeScreen(Integer screenId) {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		boolean wasAnimating = currentScreenEditor.isAnimatingSprites();
		currentScreenEditor = currentWorldEditor.getLevelScreenEditor(screenId);
		currentWorldEditor.changeCurrentScreen(currentScreenEditor);
		
		// Make sure sprites are not animating if they weren't before, and animating if they
		// were
		if (wasAnimating) currentScreenEditor.startAnimatingSprites();
		else			  currentScreenEditor.stopAnimatingSprites();
	}
	
	/**
	 * 
	 * Reloads the current screen, resyncing the modified sprites and all animations together to how it will look when
	 * bonzo enters the screen in-game. In-between states that are possible in the editor are not saved; a screen after reset
	 * is how it will appear when bonzo enters it. Useful when aligning sprites that need to work with each other.
	 * 
	 */
	public void reloadCurrentScreen() {
		currentScreenEditor.resetCurrentScreen();
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
		
		// Calculate
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
	
	/**
	 * 
	 * Sets the brush tile type to the given type and sets the id of the brush to the passed id.
	 * <p/>
	 * This will automatically deduce the actual tile from the passed paintbrush.
	 * Calling this method will change the state of the editor to PLACING mode for either tiles,
	 * hazards, or goodies, based on the paintbrush type.
	 * <p/>
	 * Since sprites are basically the same thing (an id for initial placement) this method can also handle
	 * the sprite brush, although TODO presently id will be ignored.
	 * 
	 * @param type
	 * 		the paintbrush type
	 * 
	 * @param id
	 * 		the id of the tile
	 * 
	 */
	public void setTileBrushAndId(PaintbrushType type, int id) {
		currentTileType = type;
		currentTileId = id;
		
		switch(type) {
		case SOLIDS: // break omitted
		case THRUS: // break omitted
		case SCENES: // break omitted
		case CONVEYERS: // break omitted
		case COLLAPSIBLE:
			changeState(EditorState.PLACING_TILES);
			break;
		case GOODIES:
			currentGoodieType = Goodie.Type.byValue(id);
			changeState(EditorState.PLACING_GOODIES);
			break;
		case HAZARDS:
			currentHazard = currentWorldEditor.getHazards().get(id);
			changeState(EditorState.PLACING_HAZARDS);
			break;
		case SPRITES:
			changeState(EditorState.PLACING_SPRITES);
			break;
		default:
			throw new RuntimeException("method not updated to handle new brush type " + type);
		}
		
		updateTileIndicator();
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

			// Paint an indicator to the current tile location. This depends on the 'paintbrush' selected.
			drawTileIndicator(g2d);
			// BELOW: Additional overlays that are not part of the actual world
			
			// Draw bonzo starting location of screen
			final BufferedImage bonz = CoreResource.INSTANCE.getTransparentBonzo();
			final ImmutablePoint2D start = this.currentScreenEditor.getBonzoStartingLocationPixels();
			g2d.drawImage(bonz, 
						  start.x(), start.y(), 
						  start.x() + bonz.getWidth(), start.y() + bonz.getHeight(),
						  0, 0, 
						  bonz.getWidth(), bonz.getHeight(),
						  null);
			
			// If we are selecting a tile, draw the whole sheet to the side.
			// Lot of repeated code, but each tile type has a slightly different way to draw the sprite
			// sheet. TODO possible refactor to get ALL sprite sheets from world resource to bring this done to one call?
			if (currentState == EditorState.SELECTING_GOODIES) {
				BufferedImage goodieSheet = currentWorldEditor.getWorldResource().getGoodieSheet(); // This contains their animation, so chop it in half.
				g2d.drawImage(goodieSheet, 0, 0, goodieSheet.getWidth(), goodieSheet.getHeight() / 2, // Destination
										   0, 0, goodieSheet.getWidth(), goodieSheet.getHeight() / 2, // Source
										   null);
			} else if (currentState == EditorState.SELECTING_CONVEYERS) {
				BufferedImage conveyerSheet = currentWorldEditor.getWorldResource().getEditorConveyerSheet();
				g2d.drawImage(conveyerSheet, 0, 0, conveyerSheet.getWidth(), conveyerSheet.getHeight(),
							  				 0, 0, conveyerSheet.getWidth(), conveyerSheet.getHeight(),
							  				 null);
			} else if (currentState == EditorState.SELECTING_COLLAPSIBLE) {
				BufferedImage collapsingSheet = currentWorldEditor.getWorldResource().getEditorCollapsingSheet();
				g2d.drawImage(collapsingSheet, 0, 0, collapsingSheet.getWidth(), collapsingSheet.getHeight(),
							  				   0, 0, collapsingSheet.getWidth(), collapsingSheet.getHeight(),
							  				   null);
			}
		}
		
	}
	
	/** Updates the image drawn by the tile indicator based on the brush type. The actual drawing takes place in drawTileIndicator. */
	private void updateTileIndicator() {
		assert currentState != EditorState.NO_WORLD_LOADED;
		WorldResource rsrc = currentWorldEditor.getWorldResource();
		if (   currentState == EditorState.PLACING_TILES
			|| currentState == EditorState.PLACING_HAZARDS
			|| currentState == EditorState.PLACING_GOODIES) {
			BufferedImage sheet = null;
			int srcX = 0;
			int srcY = 0;
			switch(currentTileType) {
			case SOLIDS:
				sheet = rsrc.getStatelessTileTypeSheet(StatelessTileType.SOLID);
				srcX = computeSrcX(currentTileId, sheet);
				srcY = computeSrcY(currentTileId, sheet);
				break;
			case THRUS:
				sheet = rsrc.getStatelessTileTypeSheet(StatelessTileType.THRU);
				srcX = computeSrcX(currentTileId, sheet);
				srcY = computeSrcY(currentTileId, sheet);
				break;
			case SCENES:
				sheet = rsrc.getStatelessTileTypeSheet(StatelessTileType.SCENE);
				srcX = computeSrcX(currentTileId, sheet);
				srcY = computeSrcY(currentTileId, sheet);
				break;
			case COLLAPSIBLE:
				sheet = rsrc.getCollapsingSheet();
				srcX = 0;
				srcY = currentTileId * GameConstants.TILE_SIZE_Y;
				break;
			case CONVEYERS:
				sheet = rsrc.getConveyerSheet();
				srcX = 0;
				srcY = currentTileId * (GameConstants.TILE_SIZE_Y * 2);
				break;
			case GOODIES:
				sheet = rsrc.getGoodieSheet();
				srcX = currentGoodieType.getDrawX();
				srcY = currentGoodieType.getDrawY();
				break;
			case HAZARDS:
				sheet = rsrc.getHazardSheet();
				srcX = currentTileId * (GameConstants.TILE_SIZE_X);
				srcY = 0;
				break;
			case SPRITES:
				throw new RuntimeException("Cannot have a paintbrush of sprite during a placing tile state");
			default:
				throw new RuntimeException("Unknown tile type " + currentTileType);
			}
			
			indicatorImage = new BufferedImage(GameConstants.TILE_SIZE_X, GameConstants.TILE_SIZE_Y, sheet.getType() );
			Graphics2D g = indicatorImage.createGraphics();
			try {
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f) );
				g.drawImage(sheet, 
						    0, 0, 
						    GameConstants.TILE_SIZE_X, GameConstants.TILE_SIZE_Y, 
						    srcX, srcY, 
						    srcX + GameConstants.TILE_SIZE_X, srcY + GameConstants.TILE_SIZE_Y, 
						    null);
			} finally {
				g.dispose();
			}

		} else {
			indicatorImage = null;
		}
	}
	
	private void drawTileIndicator(Graphics2D g2d) {
		int snapX = snapMouseX(mousePosition.x() );
		int snapY = snapMouseY(mousePosition.y() );
		if (indicatorImage == null) {
			g2d.setColor(Color.green);
			g2d.drawRect(snapX,
						 snapY, 
						 GameConstants.TILE_SIZE_X, GameConstants.TILE_SIZE_Y);
		} else {
			g2d.drawImage(indicatorImage, 
						  snapX, snapY,
						  snapX + GameConstants.TILE_SIZE_X, snapY + GameConstants.TILE_SIZE_Y, 
						  0, 0, 
						  indicatorImage.getWidth(), indicatorImage.getHeight(), 
						  null);
		}
	}
	
	// Resolves the id based on the width/height of sheet to the x location of the top-left coordinate to draw the tile.
	private static int computeSrcX(int id, BufferedImage sheet) {
		return (id * GameConstants.TILE_SIZE_X) % (sheet.getWidth() );
	}
	
	// Resolves the id based on the width/height of sheet to the x location of the top-left coordinate to draw the tile.
	private static int computeSrcY(int id, BufferedImage sheet) {
		return (id / (sheet.getWidth() / GameConstants.TILE_SIZE_X) ) * (GameConstants.TILE_SIZE_Y);
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
			type = CommonTile.of(currentTileId, StatelessTileType.SOLID);
			break;
		case THRUS:
			type = CommonTile.of(currentTileId, StatelessTileType.THRU);;
			break;
		case SCENES:
			type = CommonTile.of(currentTileId, StatelessTileType.SCENE);;
			break;
		case HAZARDS:
			// Stateful type: Create new based on id. Properties of hazard will be based on World
			// properties.
			// This instance will NOT be added to the world itself!! It must be copied, or multiple hazards may
			// end up sharing state (like hitting one bomb will blow up every other bomb painted with the same
			// paintbrush).
			type = HazardTile.forHazard(currentWorldEditor.getHazards().get(currentTileId) );
			break;
		case CONVEYERS:
			// Stateful type: Create new based on id. All state information is simply graphical drawing
			// so it isn't too difficult to create.
			// Please note: Special Id is assigned by the editor to be the INDEX in the conveyer list,
			// which would be the actual Conveyer id times 2, plus one IF anti-clockwise.
			type = new ConveyerTile(currentWorldEditor.getConveyers().get(currentTileId) );
			break;
		case COLLAPSIBLE:
			// Stateful, but easy to create.
			type = new CollapsibleTile(currentTileId);
			break;
		default:
			throw new IllegalArgumentException("Paintbrush type " + currentTileType.toString() + " not a valid tile type");
		}
		
		assert type != null;
		return type;
	}
	
	public enum PaintbrushType { SOLIDS, THRUS, SCENES, HAZARDS, SPRITES, GOODIES, CONVEYERS, COLLAPSIBLE; }
	
	
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
		public void defaultClickAction(LevelDrawingCanvas editor);
		
		/** Action for the editor in this state during a mouse drag
		 */
		public void defaultDragAction(LevelDrawingCanvas editor);
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
			@Override public void defaultClickAction(LevelDrawingCanvas editor) { 
				editor.addTile(editor.mousePosition.x(), editor.mousePosition.y() );
			}
			@Override public void defaultDragAction(LevelDrawingCanvas editor) {
				defaultClickAction(editor);
			}
		}, 
		/* Erases BOTH tiles and goodies from a space (although editor should enforce that goodies never occupy the
		 * same space as tiles)
		 */
		ERASING_TILES {
			@Override public void defaultClickAction(LevelDrawingCanvas editor) { 
				editor.eraseAt(editor.mousePosition.x(), editor.mousePosition.y() );
			}
			@Override public void defaultDragAction(LevelDrawingCanvas editor) { 
				defaultClickAction(editor);
			}
		},
		
		PLACING_GOODIES {
			@Override public void defaultClickAction(LevelDrawingCanvas editor) { 
				editor.addGoodie(editor.mousePosition.x(), editor.mousePosition.y() );
			}
			@Override public void defaultDragAction(LevelDrawingCanvas editor) { 
				defaultClickAction(editor);
			}
		}, 
		
		/* Click actions on this type will always produce a state change to PLACING_GOODIES */
		SELECTING_GOODIES {
			@Override public void defaultClickAction(LevelDrawingCanvas editor) { 
				int goodieId = editor.resolveObjectId(editor.currentWorldEditor.getWorldResource().getGoodieSheet(), editor.mousePosition.x(), editor.mousePosition.y() );
				
				// do nothing if click is out of bounds
				if (goodieId == -1)  return;
				
				editor.currentGoodieType = Goodie.Type.byValue(goodieId);
				editor.changeState(EditorState.PLACING_GOODIES);
			}
			@Override public void defaultDragAction(LevelDrawingCanvas editor) { 
				/* No Drag Action */
			}
		}, 
		// Displays the editor sprite sheet for conveyers, setting the specialId to be indicative of the INDEX
		// in the conveyers list of the conveyer type selected.
		SELECTING_CONVEYERS {
			@Override public void defaultClickAction(LevelDrawingCanvas editor) {
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
				editor.currentTileId = conveyerId;
				editor.changeState(EditorState.PLACING_TILES);
			}
			
			@Override public void defaultDragAction(LevelDrawingCanvas editor) {
				defaultClickAction(editor);
			}
		},
		
		SELECTING_COLLAPSIBLE {
			@Override public void defaultClickAction(LevelDrawingCanvas editor) {
				// Editor sheet has layed them out in proper order for ids to match.
				final WorldResource rsrc = editor.currentWorldEditor.getWorldResource();
				int collapsibleId = editor.resolveObjectId(rsrc.getEditorCollapsingSheet(),
														   editor.mousePosition.x(), 
														   editor.mousePosition.y() );
				
				if (collapsibleId == -1)  return;
				if (collapsibleId >= rsrc.getCollapsingCount() )  return;

				editor.currentTileId = collapsibleId;
				editor.changeState(EditorState.PLACING_TILES);
			}
			
			@Override public void defaultDragAction(LevelDrawingCanvas editor) {
				defaultClickAction(editor);
			}
		},
		
		PLACING_HAZARDS {
			@Override public void defaultClickAction(LevelDrawingCanvas editor) { 
				editor.addHazard(editor.mousePosition.x(), editor.mousePosition.y() );
			}
			
			@Override public void defaultDragAction(LevelDrawingCanvas editor) { 
				defaultClickAction(editor);
			}
		},
		
		PLACING_SPRITES {
			@Override public void defaultClickAction(LevelDrawingCanvas editor) { 
				SpritePropertiesModel model = SpritePropertiesDialog.launch(editor, editor.currentWorldEditor.getWorldResource(), ImmutablePoint2D.of(editor.mousePosition.x(), editor.mousePosition.y() ) );
				if (model.isOkay() ) {
					editor.currentScreenEditor.addSprite(model.getSpriteId(), 
														 model.getSpriteStartingLocation(), 
														 model.getSpriteBoundingBox(), 
														 model.getSpriteVelocity(), 
														 model.getAnimationType(), 
														 model.getAnimationSpeed(), 
														 model.getSpriteType(), 
														 model.getForceDirection(),
														 model.getTwoWayFacing(),
														 editor.currentWorldEditor.getWorldResource() );
				}
			}
			@Override public void defaultDragAction(LevelDrawingCanvas editor) { 
				/* No Drag Action */
			}
		},
		
		EDITING_SPRITES {
			@Override public void defaultClickAction(LevelDrawingCanvas editor) { 
				Optional<Sprite> selected = editor.resolveSpriteAtLocation(ImmutablePoint2D.from(editor.mousePosition) );
				if (selected.isPresent() ) {
					// Open properties editor for sprite
					SpritePropertiesModel model = SpritePropertiesDialog.launch(editor, editor.currentWorldEditor.getWorldResource(), selected.get() );
					// Remove sprite from screen, create a new one with new properties.
					editor.changeSpriteFromDialogModel(selected.get(), model);

				}
			}
			@Override public void defaultDragAction(LevelDrawingCanvas editor) { }
		},
		
		DELETING_SPRITES {
			@Override public void defaultClickAction(LevelDrawingCanvas editor) { 
				Optional<Sprite> selected = editor.resolveSpriteAtLocation(ImmutablePoint2D.from(editor.mousePosition) );
				if (selected.isPresent() ) {
					editor.currentScreenEditor.removeSprite(selected.get() );
				}
			}
			@Override public void defaultDragAction(LevelDrawingCanvas editor) { }
		},
		
		SELECTING_SPRITES {
			@Override public void defaultClickAction(LevelDrawingCanvas editor) { }
			@Override public void defaultDragAction(LevelDrawingCanvas editor) { }
		}, 
		
		PLACING_BONZO {
			@Override public void defaultClickAction(LevelDrawingCanvas editor) { 
				editor.setBonzo(editor.mousePosition.x(), editor.mousePosition.y() );
			}
			@Override public void defaultDragAction(LevelDrawingCanvas editor) {
				defaultClickAction(editor);
			}
		}, 
		
		NO_WORLD_LOADED {
			@Override public void defaultClickAction(LevelDrawingCanvas editor) { }
			@Override public void defaultDragAction(LevelDrawingCanvas editor) { }
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

	/**
	 * 
	 * Removes the selected sprite from the level and replaces it with the information taken from the
	 * editor sprite dialog model
	 * <p/>
	 * The new sprite replaces the removed sprite, so the list of sprites is not re-ordered by this operation.
	 * This is important as sprite chooser dialogs, if the sprite is otherwise identical, the only other info
	 * the user has in choosing a sprite is where it appears in the dialog.
	 * 
	 * @param sprite
	 * 		the old sprite to replace
	 * 
	 * @param model
	 * 		the model for the new sprite
	 * 
	 */
	private void changeSpriteFromDialogModel(Sprite sprite, SpritePropertiesModel model) {
		Sprite newSprite = Sprite.newSprite(
			model.getSpriteId(), 
		    model.getSpriteStartingLocation(), 
		    model.getSpriteBoundingBox(), 
		    model.getSpriteVelocity(), 
		    model.getAnimationType(), 
		    model.getAnimationSpeed(), 
		    model.getSpriteType(),
		    model.getForceDirection(),
		    model.getTwoWayFacing(),
		    currentWorldEditor.getWorldResource() );
		
		currentScreenEditor.replaceSprite(sprite, newSprite);
	}


}
