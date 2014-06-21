package org.erikaredmark.monkeyshines.background;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * 
 * Represents a background that is just a single colour. Nevertheless, in could work well in certain contexts.
 * <p/>
 * Instances of this class are immutable. System should create one instance per full background and instances are accessible
 * via {@code WorldResource}
 * 
 * @author Erika Redmark
 *
 */
public class SingleColorBackground extends Background {

	private final Color color;
	
	public SingleColorBackground(final Color color) {
		this.color = color;
	}

	@Override public void draw(Graphics2D g2d) {
		// Don't modify the state of the graphics object after we exit this method.
		final Color original = g2d.getColor();
		g2d.setColor(color);
		g2d.fillRect(0, 0, 640, 400);
		g2d.setColor(original);
	}
	
	/**
	 * 
	 * Intended for encoder algorithms; returns the color instance stored here to be saved.
	 * 
	 */
	public Color getColor() {
		return color;
	}
	
}
