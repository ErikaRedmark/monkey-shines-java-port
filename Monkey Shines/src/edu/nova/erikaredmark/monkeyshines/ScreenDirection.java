package edu.nova.erikaredmark.monkeyshines;

import edu.nova.erikaredmark.monkeyshines.bounds.IPoint2D;

/**
 * 
 * Represents a direction the player can travel from one screen to another.
 * 
 * @author Erika Redmark
 *
 */
public enum ScreenDirection {
	LEFT(-1),
	UP(100),
	RIGHT(1),
	DOWN(-100),
	CURRENT(0);
	
	private final int idDelta;
	
	private ScreenDirection(int idDelta) {
		this.idDelta = idDelta;
	}
	
	/**
	 * 
	 * Returns the screen id of the screen in the direction to the passed screen. The direction is represented by
	 * this object instance.
	 * 
	 * @param currentScreenId
	 * 		the current screen id
	 * 	
	 * @return
	 * 		the screen id that lies in the direction given by this object
	 * 
	 */
	public int getNextScreenId(int currentScreenId) {
		return currentScreenId + idDelta;
	}
	
	/**
	 * 
	 * All locations are within the general screen size. This method returns the direction the player left the screen,
	 * if any.
	 * 
	 * @param loc
	 * 		the current location of the player
	 * 
	 * @param size
	 * 		the size of the player 
	 * 
	 * @return
	 * 		the direction the player left the screen, or {@code CURRENT} if the player is within the bounds still
	 * 
	 */
	public static ScreenDirection fromLocation(IPoint2D loc, IPoint2D size) {
		// Check to see if it is in bounds. If so, return -1.
		if (loc.x() < 0)
			return LEFT;
		else if (loc.y() < 0)
			return UP;
		else if (loc.x() + size.x() > GameConstants.SCREEN_WIDTH )
			return RIGHT;
		else if (loc.y() + size.y() > GameConstants.SCREEN_HEIGHT)
			return DOWN;
		else
			return CURRENT;
	}
}
