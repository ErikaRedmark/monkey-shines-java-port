package org.erikaredmark.monkeyshines.menu;

import javax.swing.JButton;

/**
 * 
 * Static utility methods for some common menu functions
 * 
 * @author Erika Redmark
 *
 */
public final class MenuUtils {

	private MenuUtils() { }
	
	
	/**
	 * 
	 * Changes style formatting of a button so that only the images will show; no additional drawing. This
	 * modifies the passed button. This does not affect the button size or actually assigns images. It just
	 * removes the extra drawing so the button only draws the image and not other buttony thingies the OS
	 * would normally draw.
	 * 
	 * @param
	 * 		the button to remove formatting to
	 * 
	 */
	public static void renderImageOnly(JButton button) {
		button.setBorderPainted(false); 
		button.setContentAreaFilled(false); 
		button.setFocusPainted(false); 
		button.setOpaque(false);
	}
	
}
