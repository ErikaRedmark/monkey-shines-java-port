package org.erikaredmark.monkeyshines;

import java.util.ArrayList;
import java.util.List;

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
	 * All tiles are initialised to the empty tilenot null)
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
		map = new TileType[totalSize];
		for (int i = 0; i < totalSize; ++i) {
			map[i] = CommonTile.NONE;
		}
	}
	
	/**
	 * 
	 * Performs a <strong>Deep</strong> copy of the given tilemap. The generated tile map is guaranteed to be logically
	 * distinct from the original, including all tiles which contain state. Whilst references may be shared, that will only
	 * occur for immutable types.
	 * 
	 * @return
	 * 		a deep copy of the tile map
	 * 
	 */
	public TileMap copy() {
		TileMap newMap = new TileMap(rows, cols);
		for (int i = 0; i < rows * cols; ++i) {
			newMap.map[i] = this.map[i].copy();
		}
		return newMap;
	}
	
	/**
	 * 
	 * Adds the given tile to the mapping via the x,y co-ordinate, such as when clicked in a level editor. This will automatically
	 * handle converting to the row/col format. Note that the x, y location is a tile id, NOT a pixel location.
	 * <p/>	
	 * This does NOT throw exceptions. It is up to the client to provide sensible values. If there is a chance the value may not be sensible,
	 * check with {@code getRowCount() and getColumnCount() }
	 * <p/>
	 * This method, however, WILL fail if assertions are enabled, if the passed tile is {@code null}. {@code null} is NEVER a valid type.
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
	 * @param rsrc
	 * 		world resource for computing draw data when applicable. This may be {@code null} but only
	 * 		in special, like testing, circumstances. Rendering may be messed up if it is.
	 */
	public void setTileXY(int x, int y, TileType tile) {
		// Yah, it's a simple inversion of the parameters. But it gets confusing sometimes.
		setTileRowCol(y, x, tile);
	}
	
	/**
	 * 
	 * Erases the given tile at the given position. Erased tiles represent a logicial 'none' tile, which is effectively empty space.
	 * <p/>
	 * If the given location is out of bounds, this method does nothing.
	 * 
	 * @param x
	 * 		x location NOT IN PIXELS
	 * 
	 * @param y
	 * 		y location NOT IN PIXELS
	 * 
	 */
	public void eraseTileXY(int x, int y) {
		eraseTileRowCol(y, x);
	}
	
	/**
	 * 
	 * Adds the given tile to the mapping via a row/col position. This is resolved differently from x y position. As with that method,
	 * this is a tile id, NOT a pixel location.
	 * <p/>
	 * This does NOT throw exceptions for out of bounds row/col. It is up to the client to provide sensible values. If sensible values are not provided, this method
	 * simply does nothing. In the editor if the user somehow selects something outside of bounds, this is probably actually fine behaviour.
	 * <p/>
	 * This method, however, WILL fail if assertions are enabled, if the passed tile is {@code null}. {@code null} is NEVER a valid type.
	 * <p/>
	 * If the tile type requires recomputation for proper rendering, that will take place now.
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
	 */
	public void setTileRowCol(int row, int col, TileType tile) {
		assert tile != null;
		if (row >= rows || row < 0)  return;
		if (col >= cols || row < 0)  return;
		int index = resolveViaRowCol(row, col);
		map[index] = tile;
	}
	
	/**
	 * 
	 * Erases the given tile at the given position. Erased tiles represent a logicial 'none' tile, which is effectively empty space.
	 * <p/>
	 * If the given location is out of bounds, this method does nothing.
	 * 
	 * @param x
	 * 		x location NOT IN PIXELS
	 * 
	 * @param y
	 * 		y location NOT IN PIXELS
	 * 
	 */
	public void eraseTileRowCol(int row, int col) {
		if (row >= rows || row < 0)  return;
		if (col >= cols || row < 0)  return;
		int index = resolveViaRowCol(row, col);
		map[index] = CommonTile.NONE;
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
	

	/**
	 * 
	 * Indicates a direction in the tilemap, intended for the {@code expand} and {@code shrink} methods
	 * 
	 * @author Erika Redmark
	 *
	 */
	public enum Direction { NORTH, SOUTH, EAST, WEST }
	
	/**
	 * 
	 * Creates a new instance of the current tilemap, but with the tilemap resized in some given direction.
	 * The direction is basically thought of as extending or pinching the map towards that direction. It is intended to know
	 * where the relative locations of all the current existing tiles on the current map should be in relation to
	 * the new, bigger map.
	 * <p/>
	 * If the map is shrunk in some direction, it means that that amount of rows or columns will be deleted starting
	 * from that direction. Any tiles that were placed there will be gone in the new map.
	 * <p/>
	 * If the map is expanded in some direction, all tiles in the new rows/cols will be empty.
	 * <p/>
	 * This method returns a deep copied version of the current map, as the size of a tile map is immutable. As such,
	 * it is only intended to be used from the level editor. 
	 * 
	 * @param amount 
	 * 
	 * @param dir
	 * 		the direction to expand into for the new map
	 * 
	 * @return
	 * 		new instance of the tile map, with a deep copy of the current map and the row/col sized changed according to the
	 * 		expansion rules.
	 * 
	 */
	public TileMap resize(int amount, Direction dir) {
		// Determine the new size of the map
		// Direction will tell us which way to 'shift' the existing tile
		int newRows = rows;
		int newCols = cols;
		
		int rowShift = 0;
		int colShift = 0;
		
		switch(dir) {
		case NORTH:
			rowShift = -(amount);
			// Break omitted: SOUTH needs no shifting
		case SOUTH:
			newRows += amount;
			break;
		case WEST:
			colShift = -(amount);
			// break omnitted: EAST needs no shifting
		case EAST:
			newCols += amount;
			break;
		}
		
		// Idea: getTileRowCol returns none for tiles out of range. So, we fill in the new map only using getTileRowAndCol, applying required
		// 'shifts' to get the old map to overlay over the new map. If we try to get a position out of range, like a negative value, due to an
		// expansion either NORTH or WEST, we just fill the newly created row with nones. On the contrary, for a shrinking operation, we will
		// never even call getTileRowCol for certain values, hence properly meaning they aren't copied to the new map.
		
		TileMap newMap = new TileMap(newRows, newCols);
		for (int i = 0; i < newRows; ++i) {
			for (int j = 0; j < newCols; ++j) {
				// If out of bounds, NONE is returned. That's actually what we want, so don't worry if we shift out of reach
				newMap.setTileRowCol(i, j, this.getTileRowCol(i + rowShift, j + colShift));
			}
		}
		
		return newMap;
	}
	
	/**
	 * 
	 * Returns the tile at the given row/col position.
	 * <p/>
	 * If this is out of bounds, it will return {@code TileType.NONE}
	 * 
	 * @param row
	 * @param col
	 * 
	 * @return
	 * 		tile at the given row/column position
	 * 
	 */
	public TileType getTileRowCol(int row, int col) {
		if (row < 0 || row >= rows)  return CommonTile.NONE;
		if (col < 0 || col >= cols)  return CommonTile.NONE;
		return map[resolveViaRowCol(row, col)];
	}
	
	/**
	 * 
	 * Returns the tile at the given x/y position. Not a pixel location.
	 * <p/>
	 * If this is out of bounds, it will return {@code TileType.NONE}
	 * 
	 * @param x
	 * @param y
	 * 
	 * @return
	 * 		tile at the given x/y position
	 * 
	 */
	public TileType getTileXY(int x, int y) {
		if (y < 0 || y >= rows)  return CommonTile.NONE;
		if (x < 0 || x >= cols)  return CommonTile.NONE;
		return map[resolveViaRowCol(y, x)];
	}
	
	/**
	 * 
	 * Returns the tile at the given pixel location. Tiles are always 20x20. The pixel location should be normalised such that 0,0 is the top-left
	 * of the very first tile.
	 * <p/>
	 * If this is out of bounds, it will return {@code TileType.NONE}
	 * 
	 * @param xPixel
	 * @param yPixel
	 * 
	 * @return
	 * 		tile at the given x/y pixel location
	 * 
	 */
	public TileType getTileXYPixel(int xPixel, int yPixel) {
		final int row = yPixel / GameConstants.TILE_SIZE_Y;
		final int col = xPixel / GameConstants.TILE_SIZE_X;
		
		if (row < 0 || row >= rows)  return CommonTile.NONE;
		if (col < 0 || col >= cols)  return CommonTile.NONE;
		
		return map[resolveViaRowCol(
			yPixel / GameConstants.TILE_SIZE_Y, 
			xPixel / GameConstants.TILE_SIZE_X)
		];
	}
	
	/**
	 * 
	 * For every tile in the map, it's state, if it has any, is reset. This should be called when a tile map is
	 * loaded with relevant data, such as loading a tile map for a level screen for the first time, and as well
	 * when bonzo moves off the screen (or for the level editor when it decides to invalidate the current tiles).
	 * 
	 */
	public void resetTiles() {
		// Use array indexing as we need the odd/eveness for setting animation steps.
		// We need a little trick here: we invert the logic on odd ROWS. This is because
		// we need two 'adjacent in memory' tiles to animate the same, because logically the next one is 
		// on the next 'row' and would otherwise not be staggered with the animation cell above it
		int check = 1;
		for (int i = 0; i < map.length; ++i) {
			// did we hit the next row? invert the logic to stagger. This actually doesn't work for odd-sized
			// tile maps, but this almost always used with the main level and if it doesn't stagger in templates,
			// it really doesn't matter.
			if ( (i % cols) == 0)  check = (check == 0) ? 1 : 0;
			map[i].reset( (i % 2) == check);
		}
	}
	
	public int getRowCount() { return rows; }
	public int getColumnCount() { return cols; }
	
	private int resolveViaRowCol(int row, int col) {
		return (row * this.cols) + col;
	}
	
	/**
	 * 
	 * Updates all tiles in the map.
	 * 
	 */
	public void update() {
		for (TileType t : map)  t.update();
	}
	
	/**
	 * Returns the backing array of tiles. Should only truly be used if an external algorithm requires
	 * iterating over all tiles in the map.
	 * @return
	 * 		backing array of tiles in the map. Intended for iteration only
	 */
	public TileType[] internalMap() {
		return map;
	}
	
	@Override public boolean equals(Object o) {
		if (o == this) return true;
		if ( !(o instanceof TileMap) ) return false;
		
		TileMap other = (TileMap) o;
		
		// check size
		if (   other.rows != this.rows
		    || other.cols != this.cols) {
			
			return false;
			
		}
		
		// Now check underlying tiles for iteration order
		// Not the size check is NOT optional. Two differently sized maps could theoretically have the same tile type iteration order.
		TileType[] myTiles = this.internalMap();
		TileType[] otherTiles = other.internalMap();
		
		// If row and col check checked out the lengths MUST be the same
		assert myTiles.length == otherTiles.length : "Lengths should be identical if row/col check succeeded";
		
		for (int i = 0; i < myTiles.length; ++i) {
			if (!(myTiles[i].equals(otherTiles[i]) ) ) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override public int hashCode() {
		int result = 17;
		result += result * 31 + rows;
		result += result * 31 + cols;
		
		for (int i = 0; i < map.length; ++i) {
			result += map[i].hashCode();
		}
		
		return result;
	}

	private int rows;
	private int cols;
	private TileType[] map;
}
