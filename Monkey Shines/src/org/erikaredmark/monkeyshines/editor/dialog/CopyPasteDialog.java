package org.erikaredmark.monkeyshines.editor.dialog;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.erikaredmark.monkeyshines.World;

/**
 * 
 * Allows the user to select two screens; one that exists, and one that may or may not exist, and copy
 * the contents of the first screen to that of the second screen.
 * 
 * @author Erika Redmark
 *
 */
public final class CopyPasteDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private final SelectScreenPanel copyFromScreen;
	private final SelectScreenPanel copyToScreen;
	private final World world;
	private CopyPasteConfiguration copyPasteConfiguration;
	
	private CopyPasteDialog(final int currentId, final World world) {
		this.world = world;
		getContentPane().setLayout(new FlowLayout(FlowLayout.LEFT) );
		getContentPane().add(new JLabel("The level contents on the LEFT will be copied to the selected level on the RIGHT") );
		
		copyFromScreen = new SelectScreenPanel(currentId, world, false);
		copyToScreen = new SelectScreenPanel(currentId, world, true);
		getContentPane().add(copyFromScreen);
		getContentPane().add(copyToScreen);
		
		
		// --------- Okay and Cancel buttons
		JButton okay = new JButton(new AbstractAction("Okay") {
			private static final long serialVersionUID = 1L;
			@Override public void actionPerformed(ActionEvent e) {
				if (askAndPerformCopy() ) {
					setVisible(false);
				}
			}
		});
		
		JButton cancel = new JButton(new AbstractAction("Cancel") {
			private static final long serialVersionUID = 1L;
			@Override public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		
		add(okay);
		add(cancel);
	}
	
	/**
	 * 
	 * Alerts the user that the source screen will be copied to the target screen, and prompts 'are you sure', as
	 * this operation will remove the screen if it already exists.
	 * 
	 */
	private boolean askAndPerformCopy() {
		int copyFromId = copyFromScreen.getSelectedScreenId();
		int copyToId = copyToScreen.getSelectedScreenId();
		if (copyFromId == copyToId) {
			JOptionPane.showMessageDialog(this, "A screen cannot be copied into itself");
			return false;
		}
		
		int result = JOptionPane.showConfirmDialog(
			this, 
			  "You are about to copy the contents of screen " 
			+ copyFromId
			+ " to "
			+ copyToId
			+ ". "
			+ (   world.screenIdExists(copyToId)
				? System.lineSeparator() + "This will overwrite the current contents of screen " + copyToId
				: "") );
		
		if (result == JOptionPane.YES_OPTION) {
			this.copyPasteConfiguration = new CopyPasteConfiguration(copyFromId, copyToId);
			return true;
		} else {
			return false;
		}
	}

	
	/**
	 *
	 * Displays a copy-paste dialog allowing the user to choose an existing level and copy it to another level that
	 * may or may not already exist. This does NOT actually perform the copying, but merely returns the users decision
	 * for further use.
	 * 
	 * @param currentId
	 * 		the currently displayed screen id, used as a default in the text field
	 * 
	 * @param world
	 * 		the current world. Used to show a thumbnail of the current screen
	 * 
	 * @return
	 * 		a defintion of which screen is to be copied from, and where it should copy to, or {@code null} if a copy
	 * 		operation was decided against
	 * 
	 */
	public static CopyPasteConfiguration launch(int currentId, World world) {
		CopyPasteDialog dialog = new CopyPasteDialog(currentId, world);
		dialog.setSize(580, 200);
		dialog.setModal(true);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		
		return dialog.copyPasteConfiguration;
	}
	
	public final class CopyPasteConfiguration {
		public final int copyFromId;
		public final int copyToId;
		
		private CopyPasteConfiguration(final int copyFromId, final int copyToId) {
			assert copyFromId != copyToId;
			this.copyFromId = copyFromId;
			this.copyToId = copyToId;
		}
	}
}
