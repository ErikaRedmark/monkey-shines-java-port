package org.erikaredmark.monkeyshines;

/**
 * 
 * represents a tile coordinate in the world. This is effectively the level screen id, the row, and column of the 
 * location. Intended for Goodies, which exist in levels but are handled cross-level.
 * <p/>
 * Coordinates are equal if they refer to the same screen, row, and column.
 * <p/>
 * Effectively replaces the old, annoying, and difficult to use String style. This class still has methods to convert
 * to and back from the string style to communicate with the file format (which still uses the string style for backwards
 * compatibility)
 * 
 * @author Erika Redmark
 *
 */
public final class WorldCoordinate {
	
	/**
	 * 
	 * @param levelId
	 * 		id of level screen this coordinate refers to
	 * 
	 * @param row
	 * 		row of tile map (first array)
	 * 
	 * @param col
	 * 		col of tile map (second array)
	 * 
	 */
	public WorldCoordinate(final int levelId, final int row, final int col) {
		this.levelId = levelId;
		this.row = row;
		this.col = col;
	}
	
	public int getLevelId() { return levelId; }
	public int getRow() { return row; }
	public int getCol() { return col; }
	
	/**
	 * 
	 * Intended ONLY for {@code EncodedWorld} and storing/saving of the file format
	 * <p/>
	 * Returns a string representation of this object that matches the original representation when the
	 * file format was first created. Strings created here can be mapped back to instances of this object
	 * with {@code fromSavedStringForm(String) }
	 * 
	 * @return
	 * 		string representation for save format
	 * 
	 */
	public String createSavedStringForm() {
		return "" + levelId + "X" + row + "," + col;
	}
	
	/**
	 * 
	 * Intended ONLY for {@code EncodedWorld} and storing/saving of the file format
	 * <p/>
	 * Creates a world coordinate from a saved string form. Does not handle failure gracefully; it is intended
	 * to only be used with strings created from {@code createSavedStringForm} or older strings in world save
	 * files that by definition already conform to the form.
	 * 
	 * @param form
	 * 
	 * @return
	 * 		new instance of this object
	 * 
	 */
	public static WorldCoordinate fromSavedStringForm(final String form) {
		// Original form: 1000X12,10
		// 1000 = level id
		// 12 = row
		// 10 = col
		int indexOfX = form.indexOf('X');
		int indexOfComma = form.indexOf(',');
		int id = Integer.valueOf(form.substring(0, indexOfX ) );
		int row = Integer.valueOf(form.substring(indexOfX + 1, indexOfComma) );
		int col = Integer.valueOf(form.substring(indexOfComma + 1) );
		
		return new WorldCoordinate(id, row, col);
		
	}
	
	/**
	 * 
	 * Coordinates are equal if they refer to the same screen, row, and column.
	 * 
	 */
	@Override public boolean equals(Object o) {
		if (!(o instanceof WorldCoordinate) ) return false;
		if (o == this) return true;
		
		WorldCoordinate other = (WorldCoordinate) o;
		return   this.levelId == other.levelId
			  && this.row == other.row
			  && this.col == other.col;
	}

	@Override public int hashCode() {
		int result = 17;
		result += result * 31 + levelId;
		result += result * 31 + row;
		result += result * 31 + col;
		return result;
	}
	
	private final int levelId;
	private final int row;
	private final int col;
	
}
