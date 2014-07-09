package org.erikaredmark.monkeyshines.menu;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * 
 * The help dialog is rather simple; no controls, just the exit button and a static background.
 * 
 * @author Erika Redmark
 *
 */
public final class HelpDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private static final int SIZE_X = 640;
	private static final int SIZE_Y = 480;
	
	public HelpDialog() {
		// The loaded resource is very specific to this dialog only, so we don't bother with preloading
		// or persistence or any of that.
		try {
			final BufferedImage helpImage = ImageIO.read(HelpDialog.class.getResourceAsStream("/resources/graphics/mainmenu/help/helpScreen.png") );
			add(new JPanel() {
				private static final long serialVersionUID = 1L;
				
				// Anonymous class constructor
				{
					setPreferredSize(new Dimension(SIZE_X, SIZE_Y) );
					setSize(SIZE_X, SIZE_Y);
				}
				
				@Override public void paintComponent(Graphics g) {
					g.drawImage(helpImage, 
								0, 0, 
								SIZE_X, SIZE_Y, 
								0, 0, 
								SIZE_X, SIZE_Y, 
								null);
				}
			});
			
		} catch (IOException e) {
			throw new RuntimeException("Corrupted Jar: Help file missing: " + e.getMessage(), e);
		}
	}

	
	/**
	 * 
	 * Launches the help dialog if it is not already launched. Only one help dialog may be launched at any one time.
	 * Method is blocking and will return when the user has finished looking at the dialog and closed it.
	 * 
	 */
	public static void launch() {
		final HelpDialog dialog = new HelpDialog();
		
		dialog.setModal(true);
		dialog.setSize(SIZE_X, SIZE_Y);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		
		return;
	}

}
