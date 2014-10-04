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
	
	/**
	 * 
	 * Cuts the string such that it contains {@code size} relevant characters, PLUS 3 periods for an ellipses. Intended
	 * for drawing routines with width restrictions. If the string is already at or less than the size, the string itself
	 * is returned unchanged.
	 * 
	 * @param s
	 * 		the string to cut
	 * 
	 * @param size
	 * 		the number of <strong> significant </strong> characters to keep
	 * 
	 * @return
	 * 		the cut string, or the original string if it didn't need cutting.
	 * 
	 */
	public static String cutString(String s, int size) {
		if (s.length() <= size)  return s;
		
		return s.substring(0, size + 1) + "...";
	}
	
}
