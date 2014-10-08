package org.erikaredmark.monkeyshines.util;

import org.erikaredmark.monkeyshines.World;

/**
 * 
 * Passed to universe and world methods that allow menu systems to specify callbacks for when a game is
 * finished. This is similiar to the runnable interface but offers distinct kinds of game overs, each
 * of which may have different outcomes.
 * <p/>
 * All methods take a {@code World} object, representing the world that was just ended.
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
	void gameOverFail(World w);
	
	/**
	 * 
	 * Called when the game is over via the escape key (typically this should bring the
	 * player back to the menu fastest)
	 * 
	 */
	void gameOverEscape(World w);
	
	/**
	 * 
	 * Called when the game is legitimately beaten (hit the exit door)
	 * 
	 */
	void gameOverWin(World w);
	
}
