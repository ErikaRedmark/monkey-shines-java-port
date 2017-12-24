package org.erikaredmark.monkeyshines.background;

import java.awt.Color;

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
	private final org.newdawn.slick.Color slickColor;
	
	public SingleColorBackground(final Color color) {
		this.color = color;
		this.slickColor = new org.newdawn.slick.Color(color.getRed(), color.getGreen(), color.getBlue());
	}
	
	/**
	 * 
	 * Intended for encoder algorithms; returns the color instance stored here to be saved.
	 * 
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Returns the color in Slick form.
	 * @return
	 */
	public org.newdawn.slick.Color getColorSlick() {
		return slickColor;
	}
	
}
