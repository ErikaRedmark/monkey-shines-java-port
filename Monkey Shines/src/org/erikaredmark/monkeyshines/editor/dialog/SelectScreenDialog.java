package org.erikaredmark.monkeyshines.editor.dialog;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.erikaredmark.monkeyshines.World;
import org.erikaredmark.monkeyshines.editor.resource.EditorResource;


/**
 * 
 * Allows the user to choose between two screens. The selected screen id is returned back to the caller.
 * 
 * @author Erika Redmark
 *
 */
public final class SelectScreenDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	// This dialog model is simple; just the selected integer
	// defaults to first screen id
	private int selectedScreen;
	
	private SelectScreenDialog(final int firstScreenId, final int secondScreenId, final World world) {
		this.selectedScreen = firstScreenId;
		
		final BufferedImage firstScreenImg = EditorResource.generateThumbnailFor(world, firstScreenId);
		final BufferedImage secondScreenImg = EditorResource.generateThumbnailFor(world, secondScreenId);
		
		setLayout(new BorderLayout() );
		
		JLabel introductions = new JLabel("Please choose Bonus Room");
		add(introductions, BorderLayout.NORTH);
		
		JButton firstScreen = new JButton(new ImageIcon(firstScreenImg) );
		firstScreen.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				selectedScreen = firstScreenId;
				setVisible(false);
			}
		});
		JButton secondScreen = new JButton(new ImageIcon(secondScreenImg) );
		secondScreen.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				selectedScreen = secondScreenId;
				setVisible(false);
			}
		});
		
		add(firstScreen, BorderLayout.WEST);
		add(secondScreen, BorderLayout.EAST);
		
		// 2 160x100 thumbnails side by side, with a bit of text above plus some extra padding.
		setModal(true);
		setSize(385, 220);
	}
	
	/**
	 * 
	 * Launches the dialog and allows the user to choose which screen they want.
	 * 
	 * @param firstScreen
	 * 		screen id of the first choice
	 * 
	 * @param secondScreen
	 * 		screen id of the second choice
	 * 
	 * @param world
	 * 		world reference so that it can display the proper thumbnails
	 * 
	 * @return
	 * 		one of the passed arguments chosen. Never any value other than one of them
	 * 
	 */
	public static int launch(int firstScreen, int secondScreen, World world) {
		SelectScreenDialog select = new SelectScreenDialog(firstScreen, secondScreen, world);
		select.setVisible(true);
		
		return select.selectedScreen;
	}
	
}
