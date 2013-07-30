package edu.nova.erikaredmark.monkeyshines.editor;

import edu.nova.erikaredmark.monkeyshines.ImmutablePoint2D;
import edu.nova.erikaredmark.monkeyshines.LevelScreen;
import edu.nova.erikaredmark.monkeyshines.Tile.TileType;

/**
 * This object forwards changes to the level screen, but for every change made stores it and provides functionality to write
 * the level to a disk. This is intended ONLY for the level editor.
 * 
 * @author Erika Redmark
 */
public class LevelScreenEditor {

	/* unmodifiable version from the level data.																		*/
	private final LevelScreen screen;
	
	private LevelScreenEditor(final LevelScreen screen) {
		this.screen = screen;
	}
	
	/**
	 *  Forwards call to the wrapped {@link LevelScreen}, and stores the change.
	 */
	public void setTile(int tileX, int tileY, TileType tileType, int tileId) {
		this.screen.setTile(tileX, tileY, tileType, tileId);
		// TODO store change
	}

	public int getId() { return screen.getId(); }
	public ImmutablePoint2D getBonzoStartingLocation() { return screen.getBonzoStartingLocation(); }

	/**
	 * Creates a new level screen editor based on an existing level screen
	 * 
	 * @param screen
	 * 		actual screen, most likely returned from a valid {@link World} instance
	 * 
	 * @return
	 * 		a new instance of a level screen editor for the given screen
	 */
	public static LevelScreenEditor from(LevelScreen screen) {
		return new LevelScreenEditor(screen);
	}


	
}
