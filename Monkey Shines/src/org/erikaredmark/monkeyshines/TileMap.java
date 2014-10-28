package org.erikaredmark.monkeyshines;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.erikaredmark.monkeyshines.tiles.CommonTile;
import org.erikaredmark.monkeyshines.tiles.TileType;

/**
 * 
 * Represents an n by m tile map, typically maxed out at around {@code GameConstants.TILES_IN_ROW by GameConstants.TILES_IN_COL} for use
 * with a full level.
 * <p/>
 * Provides an easy abstraction for setting tiles via either mouse position (x,y) or row, column position. This class is backed by a single
 * dimensional array.
 * <p/>
 * This class is not thread safe. When used by the game engine for playing, it should not be modified. Only in the editor context should it.
 * 
 * @author Erika Redmark
 *
 */
public class TileMap {

	/**
	 * 
	 * Creates a new tilemap at the given number of rows and columns.
	 * <p/>
	 * All tiles are initialised to the empty tile (not null)
	 * 
	 * @param rows
	 * 		rows in the tilemap
	 * 
	 * @param cols
	 * 		cols in the tilemap
	 * 
	 */
	public TileMap(final int rows, final int cols) {
		this.rows = rows;
		this.cols = cols;
		final int totalSize = rows * cols;
		for (int i = 0; i < totalSize; ++i) {
			map[i] = CommonTile.NONE;
		}
	}
	
	/**
	 * 
	 * Adds the given tile to the mapping via the x,y co-ordinate, such as when clicked in a level editor. This will automatically
	 * handle converting to the row/col format. Note that the x, y location is a tile id, NOT a pixel location.
	 * <p/>	
	 * This does NOT throw exceptions. It is up to the client to provide sensible values. If there is a chance the value may not be sensible,
	 * check with {@code getRowCount() and getColumnCount() }
	 * 
	 * @param x
	 * 		x location on screen, resolved to tile indicator (divide by tile size)
	 * 
	 * @param y
	 * 		y location on screen, resolved to tile indicator (divide by tile size)
	 * 
	 * @param tile
	 * 		This does NOT throw exceptions. It is up to the client to provide sensible values. If sensible values are not provided, this method
	 * 		simply does nothing. In the editor if the user somehow selects something outside of bounds, this is probably actually fine behaviour.
	 * 
	 */
	public void setTileXY(int x, int y, TileType tile) {
		// Yah, it's a simple inversion of the parameters. But it gets confusing sometimes.
		setTileRowCol(y, x, tile);
	}
	
	/**
	 * 
	 * Adds the given tile to the mapping via a row/col position. This is resolved differently from x y position. As with that method,
	 * this is a tile id, NOT a pixel location.
	 * <p/>
	 * This does NOT throw exceptions. It is up to the client to provide sensible values. If sensible values are not provided, this method
	 * simply does nothing. In the editor if the user somehow selects something outside of bounds, this is probably actually fine behaviour.
	 * 
	 * @param row
	 * 		row of the tilemap
	 * 
	 * @param col
	 * 		column of the tilemap
	 * 
	 * @param tile
	 * 	    the actual tile to place. This will be placed AS IS with NO COPYING, so it is up to the client to ensure that tiles with
	 * 		their own state are not added to multiple locations.
	 * 
	 */
	public void setTileRowCol(int row, int col, TileType tile) {
		if (row < rows)  return;
		if (col < cols)  return;
		int index = resolveViaRowCol(row, col);
		map[index] = tile;
	}
	
	/**
	 * 
	 * Returns a subset of the tiles starting from position [row1, col1] to [row2, col2]. The tiles are copied to a new array, but this
	 * is a shallow copy. Modifications to the contained tiles will affect them on the tile map.
	 * <p/>
	 * This is typically used when analysing a set of tiles, such as those around Bonzo, for collision.
	 * <p/>
	 * If any part of the subset is outside of bounds, it is simply ignored. If, however, row2 or col2 are less than row1/col1, an assertion
	 * error is produced; clients should never have this happen. This check is done AFTER cutting row2/col2 down to the maximum allowed size
	 * based on the size of the map.
	 * 
	 * @param row1
	 * 		top-left row
	 * 
	 * @param col1
	 * 		top-left col
	 * 
	 * @param row2
	 * 		bottom-right row
	 * 
	 * @param col2
	 * 		bottom-right col
	 * 
	 * @return
	 * 		list of all tiles in the given subset, in top-left to bottom-right order
	 * 
	 */
	public List<TileType> subset(int row1, int col1, int row2, int col2) {
		// Cut down row2 and col2 to the minimum of the row/col size of the map. Very important otherwise too big of a column
		// may cause an overlap that grabs from the next row in a totally different position.
		row2 = Math.min(row2, rows);
		col2 = Math.min(col2, cols);
		assert row2 > row1;
		assert col2 > col1;
		List<TileType> sub = new ArrayList<TileType>((row2 - row1) * (col2 - col1) );
		for (int i = row1; i < row2; ++i) {
			int index = resolveViaRowCol(i, col1);
			// We can just increment by one for inner array, saving time.
			for (int j = col1; j < col2; ++j) {
				sub.add(map[index + j]);
			}
		}
		
		return sub;
	}
	
	public int getRowCount() { return rows; }
	public int getColumnCount() { return cols; }
	
	private int resolveViaRowCol(int row, int col) {
		return (row * this.cols) + col;
	}
	
	/**
	 * 
	 * Paints the entire tilemap to the graphics context starting at the 0, 0 point (use affinity transforms before passing to change), and
	 * draws each tile at the given row/column dimensions this map was created with.
	 * 
	 * @param g2d
	 * 		graphics context to draw to
	 * 
	 * @param rsrc
	 * 		the world resource for drawing the tiles. Tile graphics are based on internal id synced with the given graphics object
	 * 
	 */
	public void paint(Graphics2D g2d, WorldResource rsrc) {
		for (int i = 0; i < map.length; ++i) {
			map[i].paint(g2d, i % cols, i / cols, rsrc);
		}
	}
	
	/**
	 * 
	 * Updates all tiles in the map.
	 * 
	 */
	public void update() {
		for (TileType t : map)  t.update();
	}
	
	private int rows;
	private int cols;
	private TileType[] map;
	
}
