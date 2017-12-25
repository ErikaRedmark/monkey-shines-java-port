package org.erikaredmark.monkeyshines.menu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

import org.erikaredmark.monkeyshines.ImmutablePoint2D;
import org.erikaredmark.monkeyshines.ImmutableRectangle;
import org.erikaredmark.monkeyshines.KeyBindingsSlick;

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

	// Determines state of control. Either it has not been clicked (not awaiting events, just showing
	// data) or it has been clicked and is waiting for a keyboard event to change the key.
	private boolean awaitingKeyInput;
	
	// This single character code is the entire model of this control.
	private int value;
	
	// Indicates if the binder is in error. When so, it displays 'Unmappable Key'.
	// This is in case a key can't map to a keycode Slick2D will understand.
	private boolean badKey = false;
	
	// The image displayed that indicates what action this key binding will be for.
	private final BufferedImage contextImage;
	private static final int CONTEXT_IMAGE_DRAW_X = 104;
	private static final int CONTEXT_IMAGE_DRAW_Y = 2;
	private static final Color BACKGROUND_BLUE = Color.decode("0x94adff");
	
	private static final int CLICKABLE_REGION_X = 7;
	private static final int CLICKABLE_REGION_Y = 6;
	private static final int CLICKABLE_REGION_X2 = 106;
	private static final int CLICKABLE_REGION_Y2 = 21;
	private static final ImmutableRectangle CLICKABLE_REGION = 
		ImmutableRectangle.of(CLICKABLE_REGION_X, 
						      CLICKABLE_REGION_Y, 
						      CLICKABLE_REGION_X2 - CLICKABLE_REGION_X, 
						      CLICKABLE_REGION_Y2 - CLICKABLE_REGION_Y);
	
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
	 * 		the listener that will be fired when the selected key is changed. The change event fired will contain
	 *		the old and new keycode values (as integers). The keycodes will be in AWT Form, BUT they will have already
	 *		been vetted as being translatable to Slick form if needed.
	 * 
	 * @param initialValue
	 * 		the initial character used for this key binder. This is a character code from {@code KeyEvent}
	 * 
	 */
	public KeyBinder(BufferedImage contextImage, final PropertyChangeListener listener, int initialValue) {
		this.value = initialValue;
		this.contextImage = contextImage;
		
		addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				if (CLICKABLE_REGION.inBounds(ImmutablePoint2D.of(e.getX(), e.getY() ) ) ) {
					setAwaitingKeyboardInput(true);
				} else {
					setAwaitingKeyboardInput(false);
				}
				repaint();
			}
		});
		
		addKeyListener(new KeyAdapter() {
			@Override public void keyPressed(KeyEvent e) {
				if (awaitingKeyInput) {
					// always clear bad key
					badKey = false;
					if (value != e.getKeyCode() ) {
						int oldValue = value;
						if (KeyBindingsSlick.keyMappingExistsFor(value)) {
							value = e.getKeyCode();
							listener.propertyChange(new PropertyChangeEvent(KeyBinder.this, "", oldValue, value) );
						} else {
							badKey = true;
						}
					}
					setAwaitingKeyboardInput(false);
					repaint();
				}
			}
		});
		
		setSize(SIZE);
		setPreferredSize(SIZE);
		setMinimumSize(SIZE);
	}
	
	private void setAwaitingKeyboardInput(boolean awaiting) {
		this.awaitingKeyInput = awaiting;
		if (awaiting) {
			requestFocus();
		}
	}
	
	/**
	 * 
	 * Returns the currently selected keycode for this binding.
	 * 
	 * @return
	 */
	public int getSelectedKeyCode() {
		return value;
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
		
		// Similiar black/white box for the inner clickable region. We draw lines bordering but not
		// inside the clickable region
		g.setColor(Color.BLACK);
		g.drawLine(CLICKABLE_REGION_X - 2, CLICKABLE_REGION_Y - 2, CLICKABLE_REGION_X2 + 2, CLICKABLE_REGION_Y - 2);
		g.drawLine(CLICKABLE_REGION_X - 2, CLICKABLE_REGION_Y - 1, CLICKABLE_REGION_X2 + 2, CLICKABLE_REGION_Y - 1);
		g.drawLine(CLICKABLE_REGION_X - 2, CLICKABLE_REGION_Y - 2, CLICKABLE_REGION_X - 2, CLICKABLE_REGION_Y2 + 2);
		g.drawLine(CLICKABLE_REGION_X - 1, CLICKABLE_REGION_Y - 2, CLICKABLE_REGION_X - 1, CLICKABLE_REGION_Y2 + 2);
		
		g.setColor(Color.WHITE);
		g.drawLine(CLICKABLE_REGION_X, CLICKABLE_REGION_Y2 + 2, CLICKABLE_REGION_X2 + 2, CLICKABLE_REGION_Y2 + 2);
		g.drawLine(CLICKABLE_REGION_X, CLICKABLE_REGION_Y2 + 1, CLICKABLE_REGION_X2 + 2, CLICKABLE_REGION_Y2 + 1);
		g.drawLine(CLICKABLE_REGION_X2 + 1, CLICKABLE_REGION_Y, CLICKABLE_REGION_X2 + 1, CLICKABLE_REGION_Y2 + 2);
		g.drawLine(CLICKABLE_REGION_X2 + 2, CLICKABLE_REGION_Y, CLICKABLE_REGION_X2 + 2, CLICKABLE_REGION_Y2 + 2);
		
		// Stroke for the selected key
		// If we have the focus AND awaiting events, colour red.
		g.setColor(   isFocusOwner() && awaitingKeyInput 
				    ? Color.RED
				   	: Color.BLUE);
		g.setFont(new Font("serif", Font.BOLD, 16) );
		
		if (!badKey) {
			g.drawString(KeyEvent.getKeyText(this.value), 
						 41, 20);
		} else {
			g.drawString("! Unmappable Key !",
						 41, 20);
		}
		
		// done
	}
	
}
