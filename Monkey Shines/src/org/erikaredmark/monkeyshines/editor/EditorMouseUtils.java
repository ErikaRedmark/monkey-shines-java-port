package org.erikaredmark.monkeyshines.editor;

import org.erikaredmark.monkeyshines.GameConstants;

/**
 * 
 * Common utility methods for mouse operations, typically for resolving clicks against the tile map.
 * 
 * @author Erika Redmark
 *
 */
public final class EditorMouseUtils {
	private EditorMouseUtils() { }
	
	
	/**
	 * Snaps the mouse position to the top-left corner of whatever tile it is currently in. This is intended mostly for overlay drawing that needs
	 * to start at that position.
	 */
	public static int snapMouseX(final int x) {
		int takeAwayX = x % GameConstants.TILE_SIZE_X;
		return x - takeAwayX;
	}
	
	/**
	 * Snaps the mouse position to the top-left corner of whatever tile it is currently in. This is intended mostly for overlay drawing that needs
	 * to start at that position.
	 */
	public static int snapMouseY(final int y) {
		int takeAwayY = y % GameConstants.TILE_SIZE_Y;
		return y - takeAwayY;
	}
	
}
