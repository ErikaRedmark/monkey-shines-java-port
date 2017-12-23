package org.erikaredmark.monkeyshines.editor.dialog;

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
import java.util.Optional;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicArrowButton;

import org.erikaredmark.monkeyshines.World;
import org.erikaredmark.monkeyshines.editor.resource.EditorResource;
import org.erikaredmark.util.StringToNumber;

/**
 * 
 * Allows the user to select a level screen in the world. Can be set to allow selection of non-existent
 * screens or prevent selection of them.
 * <p/>
 * The model for each panel is simple; the screen selected.
 * 
 * @author Erika Redmark
 *
 */
public final class SelectScreenPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	// The 'model' of the dialog is simply a number. No need for extra classes.
	private int selectedScreenId;
	
	// Level thumbnail: Current id is mapped to a thumbnail to display
	private final Canvas levelThumbnail;
	private BufferedImage levelThumbnailToDraw;
	
	// Text box containing current screen id
	private final JTextField screenIdText;
	
	// World reference for generating thumbnails
	private final World world;
	
	private final boolean allowNew;
	
	/* ---------------- Observable Properties ------------------- */
	public static final String PROPERTY_SCREEN_ID = "propId";
	
	public SelectScreenPanel(final int currentId, final World world, final boolean allowNew) {
		setLayout(new GridBagLayout() );
		
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
		add(levelThumbnail, levelThumbnailGbc);
		
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
		add(levelSelect, levelSelectGbc);
		
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
					setIdFromTextField(userValue.get().intValue() );
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
		
		// Set initial model values
		this.allowNew = allowNew;
		this.world = world;
		// this.originalScreenId = currentId;
		this.selectedScreenId = currentId;
		this.screenIdText.setText(String.valueOf(currentId) );
		updateThumbnail();
	}
	
	
	/**
	 * 
	 * Modifies the current screen id this dialog displays. This is called by arrow buttons only, and
	 * it syncs up with the main textbox to reflect the new value, as well as updating the model.
	 * <p/>
	 * The id will NOT change if the given screen does not exist AND the dialog is set to now allow
	 * new screens.
	 * 
	 * @param value
	 * 		amount to change level screen id; may be negative
	 * 
	 */
	private void modifyIdBy(int value) {
		int newValue = selectedScreenId + value;
		if (!(this.allowNew) && !(this.world.screenIdExists(newValue) ) ) {
			return;
		}
	
		setId(newValue);
		screenIdText.setText(String.valueOf(selectedScreenId) );
		updateThumbnail();
	}
	
	/**
	 * 
	 * Sets the current screen id. This is intended to be called ONLY from the text box so
	 * now view syncing is done; the model value is simply set to the new value. If the
	 * goto screen is set to not allow new screens, this will NOT update the model, and
	 * the thumbnail update will indicate the screen model has not changed
	 * 
	 * @param value
	 * 
	 */
	private void setIdFromTextField(int value) {
		// Don't update model unless we either allow news, or are looking at existing screen
		if (this.allowNew || this.world.screenIdExists(value) ) {
			setId(value);
		}
		

		updateThumbnail();
	}
	
	/**
	 * Called ONLY after the value has been validated (such as a panel only allowing selection of existing
	 * level screens).
	 * 
	 * @param value
	 * 
	 */
	private void setId(int value) {
		// TODO if ever required, this is where a property change should be fired.
		this.selectedScreenId = value;
	}
	
	/**
	 * 
	 * Returns the id of the screen that is currently selected.
	 * 
	 * @return
	 */
	public int getSelectedScreenId() {
		return this.selectedScreenId;
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
			if (this.allowNew) {
				this.levelThumbnailToDraw = EditorResource.getNewScreenThumbnail();
			} else {
				this.levelThumbnailToDraw = EditorResource.getNoScreenHereThumbnail();
			}
		}
		
		this.levelThumbnail.repaint();
	}
	
	/* Forwarding methods to IObservable */
	/* Issue: Accidentally overriding 'firePropertyChange' in some swing interface which is called during superclass construction, effectively
	 * calling an overridable method in the constructor, leading to observing the 'obs' variable, which should be final and initialised first anyway,
	 * in an unconstructed state.
	 */
	/* @Override public void addPropertyChangeListener(PropertyChangeListener listener) { obs.addPropertyChangeListener(listener); };
	@Override public void removePropertyChangeListener(PropertyChangeListener listener) { obs.removePropertyChangeListener(listener); };
	@Override public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) { obs.addPropertyChangeListener(propertyName, listener); }
	@Override public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) { obs.removePropertyChangeListener(propertyName, listener); }
	@Override public void firePropertyChange(String propertyName, Object oldVal, Object newVal) { obs.firePropertyChange(propertyName, oldVal, newVal); }
	@Override public void suspendPropertyChangeEvents() { obs.suspendPropertyChangeEvents(); }
	@Override public void resumePropertyChangeEvents() { obs.resumePropertyChangeEvents(); }*/
}
