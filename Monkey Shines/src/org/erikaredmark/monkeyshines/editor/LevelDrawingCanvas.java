package org.erikaredmark.monkeyshines.editor;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.Goodie;
import org.erikaredmark.monkeyshines.ImmutablePoint2D;
import org.erikaredmark.monkeyshines.Point2D;
import org.erikaredmark.monkeyshines.Sprite;
import org.erikaredmark.monkeyshines.TileMap;
import org.erikaredmark.monkeyshines.World;
import org.erikaredmark.monkeyshines.World.GoodieLocationPair;
import org.erikaredmark.monkeyshines.background.Background;
import org.erikaredmark.monkeyshines.editor.MapEditor.TileBrush;
import org.erikaredmark.monkeyshines.editor.dialog.AuthorshipDialog;
import org.erikaredmark.monkeyshines.editor.dialog.EditHazardsDialog;
import org.erikaredmark.monkeyshines.editor.dialog.EditHazardsModel;
import org.erikaredmark.monkeyshines.editor.dialog.SetBackgroundDialog;
import org.erikaredmark.monkeyshines.editor.dialog.SpriteChooserDialog;
import org.erikaredmark.monkeyshines.editor.dialog.SpritePropertiesDialog;
import org.erikaredmark.monkeyshines.editor.dialog.SpritePropertiesModel;
import org.erikaredmark.monkeyshines.editor.model.Template;
import org.erikaredmark.monkeyshines.editor.model.TemplateUtils;
import org.erikaredmark.monkeyshines.encoder.EncodedWorld;
import org.erikaredmark.monkeyshines.encoder.WorldIO;
import org.erikaredmark.monkeyshines.encoder.exception.WorldSaveException;
import org.erikaredmark.monkeyshines.resource.CoreResource;
import org.erikaredmark.monkeyshines.resource.WorldResource;

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
public final class LevelDrawingCanvas extends JPanel implements MouseListener, MouseMotionListener {
	
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
	public LevelDrawingCanvas(final Function<World, Void> worldLoaded) {
		super();
		this.setLayout(null);
		this.worldLoaded = worldLoaded;
		currentGoodieType = Goodie.Type.BANANA; // Need to pick something for default. Bananas are good.
		currentState = EditorState.NO_WORLD_LOADED;
		
		setMinimumSize(
			new Dimension(GameConstants.SCREEN_WIDTH, 
				          GameConstants.SCREEN_HEIGHT) );
		
		setPreferredSize(
			new Dimension(GameConstants.SCREEN_WIDTH, 
				          GameConstants.SCREEN_HEIGHT) );
		
		setVisible(true);
		
		addMouseMotionListener(this);
		addMouseListener(this);
		
		// Optimisations
		setDoubleBuffered(true);
		
		mousePosition = Point2D.of(0, 0);

		editorFakeGameTimer = 
			new Timer(
				GameConstants.EDITOR_SPEED,
				new ActionListener() {
					@Override public void actionPerformed(ActionEvent arg0) {
						if (currentState == EditorState.NO_WORLD_LOADED) return;
						repaint();
					}
			});
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
		setCurrentScreenEditor(currentWorldEditor.getLevelScreenEditor(1000) );
		changeState(EditorState.USE_MAP_EDITOR);
		// Map editor initialised in setting screen editor.
		currentMapEditor.setBrushAndId(TileBrush.SOLIDS, 0);
		worldLoaded.apply(currentWorldEditor.getWorld() );
	}
	
	// Sets the current screen editor, removing if needed the current map editor for the screen and adding a new one
	// to the existing panel.
	private void setCurrentScreenEditor(LevelScreenEditor screenEditor) {
		currentScreenEditor = screenEditor;
		if (currentMapEditor != null) {
			this.remove(currentMapEditor);
		}
		currentMapEditor = new MapEditor(screenEditor.getLevelScreen().getMap(), screenEditor.getBackground(), currentWorldEditor.getWorld() );
		currentMapEditor.setLocation(0, 0);
		this.add(currentMapEditor);
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
	
	private void eraseGoodieAt(final int mouseX, final int mouseY) {
		if (this.currentState == EditorState.NO_WORLD_LOADED)  return;
		
		int row = mouseX / GameConstants.TILE_SIZE_X;
		int col = mouseY / GameConstants.TILE_SIZE_Y;
		
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

	public void actionPlacingGoodies() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		changeState(EditorState.PLACING_GOODIES);
	}

	public void actionPlaceBonzo() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		changeState(EditorState.PLACING_BONZO);
	}
	
	public void actionEraserTiles() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		changeState(EditorState.USE_MAP_EDITOR);
		currentMapEditor.setBrushAndId(TileBrush.ERASER, 0);
	}
	
	public void actionEraserGoodies() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		changeState(EditorState.ERASING_GOODIES);
	}
	
	public void actionEraserSprites() {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		changeState(EditorState.DELETING_SPRITES);
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
	    // update map editor too, as it does not use the same reference
	    currentMapEditor.changeBackground(newBackground);
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
		// Make no state changes if the state isn't actually changing. Still update the tile indicator: no state change can
		// still mean the type of tile has changed, requiring redraw
		if (currentState == newState) {
			updateTileIndicator();
			return;
		}
		
		if (   newState == EditorState.EDITING_SPRITES
			|| newState == EditorState.DELETING_SPRITES) {
			// Set a condition for the game timer to stop animating sprites.
			// Stopping the timer completely would look like a freeze, so we don't do that.
			currentScreenEditor.stopAnimatingSprites();
		} else {
			// Transitioning out of editing sprites state always restores the sprite animation.
			currentScreenEditor.startAnimatingSprites();
		}
		currentState = newState;
		
		// Finally, check our state. if we are ceding control to the map editor, no worries, the correct paintbrush
		// info will be set elsewhere. otherwise, we must set the map editor to a state where it knows it is not being
		// used, even if it doesn't get the clicks so that the tile indicator doesn't ghost.
		if (currentState != EditorState.USE_MAP_EDITOR) {
			currentMapEditor.setBrushAndId(TileBrush.NONE, 0);
			updateTileIndicator();
		}
	}
	
	private void updateTileIndicator() {
		if (currentState == EditorState.PLACING_GOODIES) {
			BufferedImage goodieSheet = currentWorldEditor.getWorldResource().getGoodieSheet();
			int srcX = currentGoodieType.getDrawX();
			int srcY = currentGoodieType.getDrawY();
			
			indicatorImage = new BufferedImage(GameConstants.TILE_SIZE_X, GameConstants.TILE_SIZE_Y, goodieSheet.getType() );
			Graphics2D g = indicatorImage.createGraphics();
			try {
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f) );
				g.drawImage(goodieSheet, 
						    0, 0, 
						    GameConstants.TILE_SIZE_X, GameConstants.TILE_SIZE_Y, 
						    srcX, srcY, 
						    srcX + GameConstants.TILE_SIZE_X, srcY + GameConstants.TILE_SIZE_Y, 
						    null);
			} finally {
				g.dispose();
			}
		} else if (currentState == EditorState.PLACING_TEMPLATES) {
			// draw the tilemap from the template onto a separate buffer with 50% transparency. This is a bit of a complicated indicator...
			assert currentTemplate != null : "Null template during updating indicator";
			
			BufferedImage templateRendered = TemplateUtils.renderTemplate(currentTemplate, currentWorldEditor.getWorldResource() );
			indicatorImage = new BufferedImage(templateRendered.getWidth(), templateRendered.getHeight(), templateRendered.getType() );
			
			Graphics2D g = indicatorImage.createGraphics();
			try {
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f) );
				g.drawImage(templateRendered, 0, 0, null);
			} finally {
				g.dispose();
			}
			
		} else {
			indicatorImage = null;
		}
	}
	
	public void actionChangeScreen(Integer screenId) {
		if (this.currentState == EditorState.NO_WORLD_LOADED) return;
		
		boolean wasAnimating = currentScreenEditor.isAnimatingSprites();
		LevelScreenEditor screenEditor = currentWorldEditor.getLevelScreenEditor(screenId);
		setCurrentScreenEditor(screenEditor);
		currentWorldEditor.changeCurrentScreen(screenEditor);
		
		// Make sure sprites are not animating if they weren't before, and animating if they
		// were
		if (wasAnimating) screenEditor.startAnimatingSprites();
		else			  screenEditor.stopAnimatingSprites();
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
	 * the sprite brush and will default the creation of the sprite to the id presented.
	 * <p/>
	 * This method can NOT handle {@code PaintbrushType.TEMPLATE}. Use {@code setTemplateBrush} instead.
	 * 
	 * @param type
	 * 		the paintbrush type
	 * 
	 * @param id
	 * 		the id of the tile
	 * 
	 */
	public void setTileBrushAndId(PaintbrushType type, int id) {
		switch(type) {
		case SOLIDS: // break omitted
		case THRUS: // break omitted
		case SCENES: // break omitted
		case CONVEYERS_CLOCKWISE: // break omitted
		case CONVEYERS_ANTI_CLOCKWISE: // break omitted
		case COLLAPSIBLE: // break omitted
		case HAZARDS:
			changeState(EditorState.USE_MAP_EDITOR);
			currentMapEditor.setBrushAndId(paintbrushToTilebrush(type), id);
			break;
		case GOODIES:
			currentGoodieType = Goodie.Type.byValue(id);
			changeState(EditorState.PLACING_GOODIES);
			break;
		case PLACE_SPRITES:
		case EDIT_SPRITES: // break omitted
			currentSpriteId = id;
			changeState(EditorState.PLACING_SPRITES);
			break;
		case TEMPLATE:
			throw new RuntimeException("Templates must be set explicitly via 'setTemplateBrush'");
		default:
			throw new RuntimeException("method not updated to handle new brush type " + type);
		}
	}
	
	/**
	 * 
	 * Sets the current brush to be a template brush, using the given template as the template to be drawn.
	 * 
	 * @param template
	 */
	public void setTemplateBrush(Template template) {
		currentTemplate = template;
		changeState(EditorState.PLACING_TEMPLATES);
	}
	
	/**
	 * 
	 * Converts the brush type here to the brush type in the map editor. The map editor only uses a smaller
	 * subset of tile brushes, so this method is in error if called with a paintbrush that isn't a tile brush.
	 * 
	 * @param t
	 * @return
	 */
	private TileBrush paintbrushToTilebrush(PaintbrushType t) {
		switch(t) {
		case SOLIDS:  return TileBrush.SOLIDS;
		case THRUS:  return TileBrush.THRUS;
		case SCENES:  return TileBrush.SCENES;
		case CONVEYERS_CLOCKWISE:  return TileBrush.CONVEYERS_CLOCKWISE;
		case CONVEYERS_ANTI_CLOCKWISE:  return TileBrush.CONVEYERS_ANTI_CLOCKWISE;
		case COLLAPSIBLE:  return TileBrush.COLLAPSIBLE;
		case HAZARDS:  return TileBrush.HAZARDS;
		default:  throw new IllegalArgumentException("Paintbrush type " + t + " not a valid tile brush");
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
		// We update map editor and this position
		mousePosition.setX(e.getX() );
		mousePosition.setY(e.getY() );
		
		if (currentMapEditor != null) {
			currentMapEditor.mouseMoved(mousePosition);
		}
	}
	
	/**
	 * 
	 * Note: Each call to paint is synced with updating game state, so this updates all relevant states before painting them.
	 * 
	 */
	@Override public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;

		/* If the current world is null, there is no data to load. Draw a white screen*/
		if (currentState == EditorState.NO_WORLD_LOADED) {
			g2d.setColor(Color.WHITE);
			g2d.fillRect(0, 0, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
			
		} else {
			currentMapEditor.update();
			currentMapEditor.paint(g2d);

			// Map Editor has no concept of goodies or sprites, so paint them separately.
			List<Sprite> sprites = currentScreenEditor.getSpritesOnScreen();
			for (Sprite s : sprites) {
				if (currentScreenEditor.isAnimatingSprites() )  s.update();
				s.paint(g2d);
			}
			
			Collection<GoodieLocationPair> goodies = currentWorldEditor.getWorld().getGoodiesForLevel(currentScreenEditor.getId() );
			for (GoodieLocationPair good : goodies) {
				good.goodie.update();
				good.goodie.paint(g2d);
			}
			
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
			
			// Finally, draw indicator for mouse position only if map editor hasn't already taken care of it.
			drawTileIndicator(g2d);
		}
		
	}
	
	private void drawTileIndicator(Graphics2D g2d) {
		if (currentState == EditorState.USE_MAP_EDITOR)  return;
		
		int snapX = EditorMouseUtils.snapMouseX(mousePosition.x() );
		int snapY = EditorMouseUtils.snapMouseY(mousePosition.y() );
		if (indicatorImage == null) {
			g2d.setColor(Color.green);
			g2d.drawRect(snapX,
						 snapY, 
						 GameConstants.TILE_SIZE_X, GameConstants.TILE_SIZE_Y);
		} else {
			g2d.drawImage(indicatorImage, 
				  snapX, snapY,
				  snapX + indicatorImage.getWidth(), snapY + indicatorImage.getHeight(), 
				  0, 0, 
				  indicatorImage.getWidth(), indicatorImage.getHeight(), 
				  null);
		}
	}
	
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
		// The most common state; map editor is also listening to mouse clicks and taking care of updates. We delegate to the map
		// editor here.
		USE_MAP_EDITOR {
			@Override public void defaultClickAction(LevelDrawingCanvas editor) { 
				editor.currentMapEditor.mouseClicked(editor.mousePosition);
			}
			@Override public void defaultDragAction(LevelDrawingCanvas editor) { defaultClickAction(editor); }
		},
		
		PLACING_GOODIES {
			@Override public void defaultClickAction(LevelDrawingCanvas editor) { 
				editor.addGoodie(editor.mousePosition.x(), editor.mousePosition.y() );
			}
			@Override public void defaultDragAction(LevelDrawingCanvas editor) { 
				defaultClickAction(editor);
			}
		},
		
		ERASING_GOODIES {
			@Override public void defaultClickAction(LevelDrawingCanvas editor) { 
				editor.eraseGoodieAt(editor.mousePosition.x(), editor.mousePosition.y() );
			}
			@Override public void defaultDragAction(LevelDrawingCanvas editor) { 
				defaultClickAction(editor);
			}
		},
		PLACING_TEMPLATES {

			@Override
			public void defaultClickAction(LevelDrawingCanvas editor) {
				assert editor.currentTemplate != null : "Null template during drawing";
				
				TileMap map = editor.currentScreenEditor.getLevelScreen().getMap();
				// TODO currently no support for offsets; always draws at top-left.
				editor.currentTemplate.drawTo(map, 
											  editor.mousePosition.y() / GameConstants.TILE_SIZE_Y, 
											  editor.mousePosition.x() / GameConstants.TILE_SIZE_X, 
											  0, 
											  0);
			}

			@Override public void defaultDragAction(LevelDrawingCanvas editor) { 
				defaultClickAction(editor);
			}
			
		},
		
		PLACING_SPRITES {
			@Override public void defaultClickAction(LevelDrawingCanvas editor) { 
				SpritePropertiesModel model = 
					SpritePropertiesDialog.launch(
						editor, 
						editor.currentWorldEditor.getWorldResource(),
						editor.currentSpriteId,
						ImmutablePoint2D.of(editor.mousePosition.x(), editor.mousePosition.y() ) );
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
			// Still have to forward... slight drag movement would otherwise be ignored.
			@Override public void defaultDragAction(LevelDrawingCanvas editor) { 
				defaultClickAction(editor);
			}
		},
		
		DELETING_SPRITES {
			@Override public void defaultClickAction(LevelDrawingCanvas editor) { 
				Optional<Sprite> selected = editor.resolveSpriteAtLocation(ImmutablePoint2D.from(editor.mousePosition) );
				if (selected.isPresent() ) {
					editor.currentScreenEditor.removeSprite(selected.get() );
				}
			}
			@Override public void defaultDragAction(LevelDrawingCanvas editor) { 
				defaultClickAction(editor);
			}
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


	// Point2D of the mouse position
	private Point2D mousePosition;
	
	// Information about what clicking something will do
	private Goodie.Type currentGoodieType;
	private int currentSpriteId;
	// May be null, but should never be accessed until Editor State goes to placing templates, which
	// requires a non-null template to even get to that state.
	private Template currentTemplate;
	
	private final Function<World, Void> worldLoaded;
	
	// THESE MAY BE NULL!
	private LevelScreenEditor currentScreenEditor;
	private WorldEditor currentWorldEditor;
	private MapEditor currentMapEditor;
	
	private Timer editorFakeGameTimer;
	
	private BufferedImage indicatorImage = null;
	
	private EditorState    currentState;

}