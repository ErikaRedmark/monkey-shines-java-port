package org.erikaredmark.monkeyshines.background;

import java.awt.Graphics2D;

/**
 * 
 * Backgrounds are always the first thing drawn on a level. Different background types draw
 * backgrounds in different ways.
 * 
 * @author Erika Redmark
 *
 */
public abstract class Background {
	
	/**
	 * 
	 * Draws this background onto the given context
	 * 
	 * @param g2d
	 * 		graphics context
	 * 
	 */
	public abstract void draw(Graphics2D g2d);
}
