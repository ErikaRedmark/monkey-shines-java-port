package org.erikaredmark.monkeyshines.util;

/**
 * 
 * Passed to universe and world methods that allow menu systems to specify callbacks for when a game is
 * finished. This is similiar to the runnable interface but offers distinct kinds of game overs, each
 * of which may have different outcomes.
 * 
 * @author Erika Redmark
 *
 */
public interface GameEndCallback {
	
	/**
	 * 
	 * Called when the player loses the game (loses all lives)
	 * 
	 */
	void gameOverFail();
	
	/**
	 * 
	 * Called when the game is over via the escape key (typically this should bring the
	 * player back to the menu fastest)
	 * 
	 */
	void gameOverEscape();
	
	/**
	 * 
	 * Called when the game is legitimately beaten (hit the exit door)
	 * 
	 */
	void gameOverWin();
	
}
