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
	LEFT(-1) {
		@Override public void transferLocation(Point2D location, IPoint2D size) {
			location.setX(GameConstants.SCREEN_WIDTH - size.x() );
		}
	},
	UP(100) {
		@Override public void transferLocation(Point2D location, IPoint2D size) {
			location.setY(GameConstants.SCREEN_HEIGHT - size.y() );
		}
	},
	RIGHT(1) {
		@Override public void transferLocation(Point2D location, IPoint2D size) {
			location.setX(0);
		}
	},
	DOWN(-100) {
		@Override public void transferLocation(Point2D location, IPoint2D size) {
			location.setY(0);
		}
	},
	CURRENT(0) {
		@Override public void transferLocation(Point2D location, IPoint2D size) { /* No op */ }
	};
	
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
	 * Takes a mutable point and translates the location such that where it would be on the next screen. For example,
	 * if bonzo walks to the left, his close to 0 x location must be transformed to almost the screen width since he
	 * is now on the right side of the screen. This method works with any location.
	 * <p/>
	 * It is vital the screen size exactly match the exact size to fit the tiles, or this method won't work.
	 * 
	 * @param location
	 * 		the location to modify
	 * 
	 * @param size
	 * 		the size of the object. This affects calculations to prevent it from being drawn off the screen
	 * 
	 */
	public abstract void transferLocation(Point2D location, IPoint2D size);
	
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
