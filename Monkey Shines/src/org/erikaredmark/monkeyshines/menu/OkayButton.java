package org.erikaredmark.monkeyshines.menu;

import java.awt.event.ActionListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 * 
 * Represents a fully properly skinned Okay button that is displayed in some Monkey Shines dialogs. It can
 * be configured by the client for an action when pressed.
 * 
 * @author Erika Redmark
 *
 */
public class OkayButton extends JButton {
	private static final long serialVersionUID = 1L;
	
	private final Icon OKAY_ICON;
	private final Icon OKAY_PUSHED_ICON;
	
	{
		try {
			OKAY_ICON = new ImageIcon(ImageIO.read(OkayButton.class.getResourceAsStream("/resources/graphics/mainmenu/btnOK1.png") ) );
			OKAY_PUSHED_ICON = new ImageIcon(ImageIO.read(OkayButton.class.getResourceAsStream("/resources/graphics/mainmenu/btnOK2.png") ) );
		} catch (IOException e) {
			throw new RuntimeException("Bad .jar, could not find graphics resources for okay button: " + e.getMessage(), e);
		}
	}
	
	/**
	 * 
	 * Creates a new instance of this okay button whose function is determined by the passed listener.
	 * 
	 * @param listener
	 * 
	 */
	public OkayButton(final ActionListener listener) {
		super();
		setIcon(OKAY_ICON);
		setSize(OKAY_ICON.getIconWidth(), OKAY_ICON.getIconHeight() );
		setPressedIcon(OKAY_PUSHED_ICON);
		addActionListener(listener);
		MenuUtils.renderImageOnly(this);
	}
	
}
