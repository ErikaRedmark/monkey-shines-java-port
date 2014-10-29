package org.erikaredmark.monkeyshines.editor;

import org.erikaredmark.monkeyshines.TileMap;
import org.erikaredmark.monkeyshines.World;
import org.erikaredmark.monkeyshines.background.Background;
import org.erikaredmark.monkeyshines.bounds.IPoint2D;

import com.google.common.base.Function;

/**
 * 
 * Indicates a class implements a tile grid which the user paints on. Tile editors contain ways of changing the current brush and
 * drawing to some canvas.
 * <p/>
 * This is intended to be used as a trait for a form of multiple inheritance, since visual classes already extends JPanel or some
 * other context and cannot extend another. This is used with {@code TileEditorBasicGridImpl} and {@code TileEditorSpriteGridImpl}
 * and contained in the target class as a form of multiple inheritance, since much functionality is the same.
 * <p/>
 * Tile editors have an area to draw on represented by a tile grid, filled in with a background. They may be up to 20 rows by 32
 * columns, the size of the standard playing area (smaller for templates). Changes to the tilemap fire off callbacks, which can be used
 * to update whatever model is interested (templates, a level screen)
 * <p/>
 * Whilst this is focused on tiles, implementations may choose to also include sprite editing support if applicable.
 * 
 * @author Erika Redmark
 *
 */
interface TileEditorGrid {
	
	/**
	 * 
	 * Sets the brush type for drawing. Almost all brushes refer to a type and an id for the graphics resource for the level.
	 * If a brush does not (such as erasers) then the id parameter will be ignored.
	 * <p/>
	 * It is up to the client to not set brushes that aren't supported for some context. The tile editor implementation will
	 * support all brushes.
	 * 
	 * @param type
	 * 		type of brush to set
	 * 
	 * @param id
	 * 		id of the graphics resource to draw, if applicable.
	 * 
	 */
	void setTileBrushAndId(PaintbrushType type, int id);
	
	/**
	 * 
	 * Has the same effect as {@code setTileBrushAndId(type, 0) }. Shorthand for brushes that do not require ids, like erasers
	 * or selection brushes.
	 * 
	 */
	void setTileBrush(PaintbrushType type);
	
	/**
	 * 
	 * Adds a function that accepts a tile map as a callback for whenever a tile is edited. The function will be called with a
	 * reference to the backing array so that other models may be updated as needed. Note that the passed array does indeed represent the 
	 * editor array, so changes this will effect the editor and vice versa (hence this is not thread safe). Make a defensive copy if required.
	 * 
	 * @param callback
	 * 		the function to call
	 * 
	 */
	void addTileModificationCallback(Function<TileMap, Void> callback);
	
	/**
	 * 
	 * Creates a new canvas for editing. If the editor currently contains a reference to an existing tile array, that reference is discarded 
	 * (but not invalidated; clients that stored it are okay). The editor will set up a new tile array of the specified size. The size cannot exceed
	 * the maximum of 20 rows by 32 columns
	 * 
	 * @param rows
	 * 		rows in the new tile editor. Must be less than 20
	 * 
	 * @param cols
	 * 		cols in the new tile editor. Must be less than 32
	 * 
	 */
	void newTiles(int rows, int cols);
	
	/**
	 * 
	 * Sets the background for the tile editor. This is typically either the actual background for the level, or some generic
	 * backdrop when editing templates.
	 * 
	 * @param background
	 * 		the background to show for the editor
	 * 
	 */
	void setBackground(Background background);
	
	/**
	 * 
	 * Adds the given tile to the world at the given location. The passed {@code PaintbrushType} must be a tile brush. This
	 * will generate a tile from that brush possibly based on other properties of the world passed
	 * 
	 */
	void addTile(int row, int col, PaintbrushType brush, World world);
	
	/**
	 * 
	 * Returns the current location of the mouse in this tile editor.
	 * 
	 */
	IPoint2D getMousePosition();
}
