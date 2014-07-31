package org.erikaredmark.monkeyshines.menu;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.erikaredmark.monkeyshines.KeyBindings;

public class KeyboardControlDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private static final int RIGHT_KEY_DRAW_X = 67; 
	private static final int RIGHT_KEY_DRAW_Y = 16;
	
	private static final int LEFT_KEY_DRAW_X = 67;
	private static final int LEFT_KEY_DRAW_Y = 47;
	
	private static final int JUMP_KEY_DRAW_X = 67;
	private static final int JUMP_KEY_DRAW_Y = 78;
	
	private static final int OKAY_DRAW_X = 133;
	private static final int OKAY_DRAW_Y = 120;
	
	private static final int DIALOG_SIZE_X = 328;
	private static final int DIALOG_SIZE_Y = 180;
	
	
	private final BufferedImage BACKGROUND;
	
	private final BufferedImage CONTEXT_RIGHT;
	private final BufferedImage CONTEXT_LEFT;
	private final BufferedImage CONTEXT_JUMP;
	
	// model state information
	// There is no 'original' as there is no 'cancel' button
	private KeyBindings bindings;
	
	{
		try {
			BACKGROUND = ImageIO.read(SoundControlDialog.class.getResourceAsStream("/resources/graphics/mainmenu/keyboard/keyboardMain.png") );
			CONTEXT_RIGHT = ImageIO.read(SoundControlDialog.class.getResourceAsStream("/resources/graphics/mainmenu/keyboard/keyboardRight.png") );
			CONTEXT_LEFT = ImageIO.read(SoundControlDialog.class.getResourceAsStream("/resources/graphics/mainmenu/keyboard/keyboardLeft.png") );
			CONTEXT_JUMP = ImageIO.read(SoundControlDialog.class.getResourceAsStream("/resources/graphics/mainmenu/keyboard/keyboardJump.png") );
		} catch (IOException e) {
			throw new RuntimeException("Bad .jar, could not find graphics resources for keyboard binding system: " + e.getMessage(), e);
		}
	}
	
	private KeyboardControlDialog(final KeyBindings current) {
		this.bindings = current;
		
		// JPanel will handle custom drawing.
		JPanel mainPanel = new JPanel() {
			private static final long serialVersionUID = 1L;
			// Direct painting is responsible for background and left/right sound images
			@Override public void paintComponent(Graphics g) {
				g.drawImage(BACKGROUND, 
							0, 0, 
							BACKGROUND.getWidth(), BACKGROUND.getHeight(), 
							0, 0, 
							BACKGROUND.getWidth(), BACKGROUND.getHeight(), 
							null);
			}
		};
		
		mainPanel.setLocation(0, 0);
		mainPanel.setSize(DIALOG_SIZE_X, DIALOG_SIZE_Y);
		mainPanel.setPreferredSize(new Dimension(DIALOG_SIZE_X, DIALOG_SIZE_Y) );
		mainPanel.setMinimumSize(new Dimension(DIALOG_SIZE_X, DIALOG_SIZE_Y) );
		mainPanel.setLayout(null);
		add(mainPanel);
		
		// Key controls
		
		final KeyBinder rightBinder = 
			new KeyBinder(
				CONTEXT_RIGHT,
			    new PropertyChangeListener() {
					@Override public void propertyChange(PropertyChangeEvent event) {
						int right = (Integer)event.getNewValue();
						bindings = new KeyBindings(bindings.left, right, bindings.jump);
					}
				},
				current.right);
		rightBinder.setLocation(RIGHT_KEY_DRAW_X, RIGHT_KEY_DRAW_Y);
		
		KeyBinder leftBinder = 
			new KeyBinder(
				CONTEXT_LEFT,
			    new PropertyChangeListener() {
					@Override public void propertyChange(PropertyChangeEvent event) {
						int left = (Integer)event.getNewValue();
						bindings = new KeyBindings(left, bindings.right, bindings.jump);
					}
				},
				current.left);
		leftBinder.setLocation(LEFT_KEY_DRAW_X, LEFT_KEY_DRAW_Y);
			
		KeyBinder jumpBinder = 
			new KeyBinder(
				CONTEXT_JUMP,
			    new PropertyChangeListener() {
					@Override public void propertyChange(PropertyChangeEvent event) {
						int jump = (Integer)event.getNewValue();
						bindings = new KeyBindings(bindings.left, bindings.right, jump);
					}
				},
				current.jump);
		jumpBinder.setLocation(JUMP_KEY_DRAW_X, JUMP_KEY_DRAW_Y);
		
		mainPanel.add(rightBinder);
		mainPanel.add(leftBinder);
		mainPanel.add(jumpBinder);
		
		// Okay button
		
		OkayButton okayButton = new OkayButton(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
			}
		});
		okayButton.setLocation(OKAY_DRAW_X, OKAY_DRAW_Y);
		
		mainPanel.add(okayButton);
	}
	
	/**
	 * 
	 * Launches the key control dialog, using the current key bindings as the initial model, and returning a key bindings
	 * object representative of the changes the user made. It is possible, if the user makes no changes, for the object
	 * returned to be equal to the parameter.
	 * 
	 * @param current
	 * 		the current key bindings
	 * 
	 * @return
	 * 		the newly modified key bindings. May be equal to the original key bindings semantically
	 * 
	 */
	public static KeyBindings launch(final KeyBindings current) {
		KeyboardControlDialog dialog = new KeyboardControlDialog(current);
		dialog.setModal(true);
		dialog.setUndecorated(true);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		
		assert dialog.bindings != null;
		
		return dialog.bindings;
	}
	
}
