package edu.nova.erikaredmark.monkeyshines.editor.dialog;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicArrowButton;

import org.redmark.erika.util.StringToNumber;

import com.google.common.base.Optional;

import edu.nova.erikaredmark.monkeyshines.World;
import edu.nova.erikaredmark.monkeyshines.editor.resource.EditorResource;

/**
 * 
 * This dialog is displayed when the user wishes to go to a specific screen (level) in the current world
 * 
 * @author Erika Redmark
 * 
 */
public final class GoToScreenDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	// The 'model' of the dialog is simply a number. No need for extra classes.
	private int selectedScreenId;
	
	// Canceling sets the selected back to original.
	private int originalScreenId;
	
	// Level thumbnail: Current id is mapped to a thumbnail to display
	private final Canvas levelThumbnail;
	private BufferedImage levelThumbnailToDraw;
	
	// Text box containing current screen id
	private final JTextField screenIdText;
	
	// World reference for generating thumbnails
	private final World world;
	
	private GoToScreenDialog(final int currentId, final World world) {
		getContentPane().setLayout(new GridBagLayout() );
		
		// -------- Left side: Thumbnail
		levelThumbnail = new Canvas() {
			private static final long serialVersionUID = 1L;

			@Override public void paint(Graphics g) {
				if (levelThumbnailToDraw == null)  return;
				
				Graphics2D g2d = (Graphics2D) g;
				g2d.drawImage(levelThumbnailToDraw, 
						  0, 0, 		// Dest 1
						  160, 100, 	// Dest 2
						  0, 0, 		// Src 1
						  160, 100, 	// Src 2
						  null);
			}
		};
		GridBagConstraints levelThumbnailGbc = new GridBagConstraints();
		levelThumbnailGbc.gridx = 0;
		levelThumbnailGbc.gridy = 0;
		levelThumbnailGbc.weightx = 1;
		levelThumbnailGbc.weighty = 1;
		levelThumbnailGbc.gridheight = 1;
		levelThumbnailGbc.gridwidth = 1;
		// Thumbnail size
		levelThumbnail.setPreferredSize(new Dimension(160, 100) ); // width and height are both /4 of normal game window
		getContentPane().add(levelThumbnail, levelThumbnailGbc);
		
		// -------- RightSide: Level id selection
		JPanel levelSelect = new JPanel();
		levelSelect.setLayout(new BorderLayout() );
		GridBagConstraints levelSelectGbc = new GridBagConstraints();
		levelThumbnailGbc.gridx = 1;
		levelThumbnailGbc.gridy = 0;
		levelThumbnailGbc.weightx = 1;
		levelThumbnailGbc.weighty = 1;
		levelThumbnailGbc.gridheight = 1;
		levelThumbnailGbc.gridwidth = 1;
		getContentPane().add(levelSelect, levelSelectGbc);
		
		// Arrows and id selection
		BasicArrowButton upArrow = new BasicArrowButton(BasicArrowButton.NORTH);
		BasicArrowButton rightArrow = new BasicArrowButton(BasicArrowButton.EAST);
		BasicArrowButton downArrow = new BasicArrowButton(BasicArrowButton.SOUTH);
		BasicArrowButton leftArrow = new BasicArrowButton(BasicArrowButton.WEST);
		
		upArrow.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) { modifyIdBy(100); }
		});
		
		rightArrow.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) { modifyIdBy(1); }
		});
		
		downArrow.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) { modifyIdBy(-100); }
		});
		
		leftArrow.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) { modifyIdBy(-1); }
		});
		
		screenIdText = new JTextField(6);
		screenIdText.addKeyListener(new KeyListener() {
			@Override public void keyPressed(KeyEvent e) { }
			@Override public void keyReleased(KeyEvent e) { 
				Optional<Integer> userValue = StringToNumber.string2Int(screenIdText.getText() );
				if (userValue.isPresent() ) {
					setId(userValue.get().intValue() );
				} else {
					// TODO show warning icon
					System.err.println("Non-integer value " + screenIdText.getText() );
				}
			}
			@Override public void keyTyped(KeyEvent e) { }

		});
		
		levelSelect.add(rightArrow, BorderLayout.EAST);
		levelSelect.add(upArrow, BorderLayout.NORTH);
		levelSelect.add(downArrow, BorderLayout.SOUTH);
		levelSelect.add(leftArrow, BorderLayout.WEST);
		levelSelect.add(screenIdText, BorderLayout.CENTER);
		
		// --------- Okay and Cancel buttons
		JButton okay = new JButton(new AbstractAction("Okay") {
			private static final long serialVersionUID = 1L;
			@Override public void actionPerformed(ActionEvent e) {
				// Close window, leaving model untouched
				setVisible(false);
			}
		});
		
		JButton cancel = new JButton(new AbstractAction("Cancel") {
			private static final long serialVersionUID = 1L;
			@Override public void actionPerformed(ActionEvent e) {
				// Reset model before closing window
				selectedScreenId = originalScreenId;
				setVisible(false);
			}
		});
		
		GridBagConstraints okayGbc = new GridBagConstraints();
		okayGbc.gridx = 1;
		okayGbc.gridy = 1;
		okayGbc.weightx = 1;
		okayGbc.weighty = 1;
		okayGbc.gridheight = 1;
		okayGbc.gridwidth = 1;
		
		GridBagConstraints cancelGbc = new GridBagConstraints();
		cancelGbc.gridx = 0;
		cancelGbc.gridy = 1;
		cancelGbc.weightx = 1;
		cancelGbc.weighty = 1;
		cancelGbc.gridheight = 1;
		cancelGbc.gridwidth = 1;
		
		getContentPane().add(okay, okayGbc);
		getContentPane().add(cancel, cancelGbc);
		
		// Set initial model values
		this.originalScreenId = currentId;
		this.selectedScreenId = currentId;
		this.world = world;
		this.screenIdText.setText(String.valueOf(currentId) );
		updateThumbnail();
	}
	
	/**
	 * 
	 * Modifies the current screen id this dialog displays. This is called by arrow buttons only, and
	 * it syncs up with the main textbox to reflect the new value, as well as updating the model.
	 * 
	 * @param value
	 * 		amount to change level screen id; may be negative
	 * 
	 */
	private void modifyIdBy(int value) {
		this.selectedScreenId += value;
		screenIdText.setText(String.valueOf(selectedScreenId) );
		updateThumbnail();
	}
	
	/**
	 * 
	 * Sets the current screen id. This is intended to be called ONLY from the text box so
	 * now view syncing is done; the model value is simply set to the new value.
	 * 
	 * @param value
	 * 
	 */
	private void setId(int value) {
		this.selectedScreenId = value;
		updateThumbnail();
	}
	
	/**
	 * 
	 * Draws the currently selected screen on the canvas. If no screen exists, draws special
	 * NEW image.
	 * 
	 */
	private void updateThumbnail() {
		if (this.world.screenIdExists(selectedScreenId) ) {
			this.levelThumbnailToDraw = EditorResource.generateThumbnailFor(world, selectedScreenId);
		} else {
			this.levelThumbnailToDraw = EditorResource.getNewScreenThumbnail();
		}
		
		this.levelThumbnail.repaint();
	}
	
	/**
	 * Displays the dialog and gets the id the user selected. This is a blocking call, and effectively makes the dialog 
	 * modal with respect to the current thread
	 * 
	 * @param currentId
	 * 		the currently displayed screen id, used as a default in the text field
	 * 
	 * @param world
	 * 		the current world. Used to show a thumbnail of the current screen
	 * 
	 * @return
	 * 		the id of the screen the user asked for. If the user cancels, this returns the
	 * 		same as the current id.
	 * 
	 */
	public static int displayAndGetId(int currentId, World world) {
		GoToScreenDialog dialog = new GoToScreenDialog(currentId, world);
		dialog.setLocationRelativeTo(null);
		dialog.setSize(300, 200);
		dialog.setModal(true);
		dialog.setVisible(true);
		
		return dialog.selectedScreenId;
	}
	
}
