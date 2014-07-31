package org.erikaredmark.monkeyshines.menu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

/**
 * 
 * A component that allows a user to click in a text field and type a Single key (not a key combination 
 * like with using Shift, Control, or other modifiers). Said key will then be displayed and the underlying
 * model will be updated.
 * <p/>
 * Whilst this is a bit of a generic concept, this specific class works in the context of Monkey Shines,
 * with support for an image on the right side of a specific size so that it blends in with the original
 * look of the game. because of this, this component has a fixed, set size and is only used in the absence
 * of layout managers.
 * 
 * @author Erika Redmark
 *
 */
public class KeyBinder extends JComponent {
	private static final long serialVersionUID = 1L;

	// Constant size 
	private static final Dimension SIZE = new Dimension(191, 28);

	// This single character code is the entire model of this control.
	private int value;
	
	// The image displayed that indicates what action this key binding will be for.
	private final BufferedImage contextImage;
	private static final int CONTEXT_IMAGE_DRAW_X = 104;
	private static final int CONTEXT_IMAGE_DRAW_Y = 2;
	private static final Color BACKGROUND_BLUE = Color.decode("0x94adff");
	
	/**
	 * 
	 * Initialises the keybinder with the given image and will send all key-change selections to the passed listener. Uses
	 * the passed model for the initial value.
	 * 
	 * @param contextImage
	 * 		the image that should be displayed to the right of the key selector, to tell the user what
	 * 		they are determining a binding for
	 * 
	 * @param listener
	 * 		the listener that will be fired when the selected key is changed
	 * 
	 * @param initialValue
	 * 		the initial character used for this key binder. This is a character code from {@code KeyEvent}
	 * 
	 */
	public KeyBinder(BufferedImage contextImage, ChangeListener listener, int initialValue) {
		this.value = initialValue;
		this.contextImage = contextImage;
		
		setSize(SIZE);
		setPreferredSize(SIZE);
		setMinimumSize(SIZE);
	}
	
	/**
	 * 
	 * Paints the background, the image label to the right indicating the type of keybinding, and then the current
	 * character selected, whatever it may be.
	 * 
	 */
	@Override public void paintComponent(Graphics g) {
		// Background borders
		g.setColor(Color.BLACK);
		g.drawLine(0, 0, SIZE.width, 0);
		g.drawLine(0, 1, SIZE.width, 1);
		g.drawLine(0, 0, 0, SIZE.height);
		g.drawLine(1, 0, 1, SIZE.height);
		
		g.setColor(BACKGROUND_BLUE);
		g.fillRect(2, 2, SIZE.width - 2, SIZE.height - 2);
		
		g.setColor(Color.WHITE);
		g.drawLine(2, SIZE.height - 1, SIZE.width, SIZE.height - 1);
		g.drawLine(2, SIZE.height, SIZE.width, SIZE.height);
		g.drawLine(SIZE.width - 1, 0, SIZE.width - 1, SIZE.height);
		g.drawLine(SIZE.width, 0, SIZE.width, SIZE.height);
		
		// Context image
		g.drawImage(contextImage, 
					CONTEXT_IMAGE_DRAW_X, CONTEXT_IMAGE_DRAW_Y, 
					contextImage.getWidth() + CONTEXT_IMAGE_DRAW_X, contextImage.getHeight() + CONTEXT_IMAGE_DRAW_Y, 
					0, 0, 
					contextImage.getWidth(), contextImage.getHeight(), 
					null);
		
		// Stroke for the selected key
		g.setColor(Color.BLUE);
		g.setFont(new Font("sansserif", Font.BOLD, 16) );
		g.drawString(String.valueOf(this.value), 
					 41, 20);
		
		// done
	}
	
}
