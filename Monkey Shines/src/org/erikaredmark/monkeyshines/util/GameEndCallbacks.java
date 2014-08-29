package org.erikaredmark.monkeyshines.util;

/**
 * 
 * Factory class for some common types of end game callbacks.
 * 
 * @author Erika Redmark
 *
 */
public final class GameEndCallbacks {

	private GameEndCallbacks() { }
	
	/**
	 * 
	 * Returns an instance of this callback that performs the same action regardless of the type
	 * of game over.
	 * 
	 * @param callback
	 * 
	 * @return
	 * 		new instance of this callback
	 * 
	 */
	public static GameEndCallback singleCallback(final Runnable callback) {
		return new GameEndCallback() {
			@Override public void gameOverFail() { callback.run(); }
			@Override public void gameOverEscape() { callback.run(); }
			@Override public void gameOverWin() { callback.run(); }
		};
	}
	
}
