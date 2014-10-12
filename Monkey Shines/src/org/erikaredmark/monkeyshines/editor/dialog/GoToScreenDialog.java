package org.erikaredmark.monkeyshines.editor.dialog;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;

import org.erikaredmark.monkeyshines.World;

/**
 * 
 * This dialog is displayed when the user wishes to go to a specific screen (level) in the current world
 * 
 * @author Erika Redmark
 * 
 */
public final class GoToScreenDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private final SelectScreenPanel selectScreen;
	
	private final int originalId;
	private boolean useOriginal;
	
	private GoToScreenDialog(final int currentId, final World world, final boolean allowNew) {
		getContentPane().setLayout(new FlowLayout(FlowLayout.LEFT) );
		originalId = currentId;
		useOriginal = false;
		selectScreen = new SelectScreenPanel(currentId, world, allowNew);
		getContentPane().add(selectScreen);
		
		
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
				useOriginal = true;
				setVisible(false);
			}
		});
		
		add(okay);
		add(cancel);
	}
	
	public int getSelectedScreenId() {
		return selectScreen.getSelectedScreenId();
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
	 * @param allowNew
	 * 		if {@code true}, user can select screens that do not exist yet (intended for systems that
	 * 		can make new screens). If the user MUST choose an existing screen, this must be {@code false}.
	 * 		In that state, user may not close the dialog via 'okay' without selecting a valid screen
	 * 
	 * @return
	 * 		the id of the screen the user asked for. If the user cancels, this returns the
	 * 		same as the current id.
	 * 
	 */
	public static int displayAndGetId(int currentId, World world, boolean allowNew) {
		GoToScreenDialog dialog = new GoToScreenDialog(currentId, world, allowNew);
		dialog.setLocationRelativeTo(null);
		dialog.setSize(300, 200);
		dialog.setModal(true);
		dialog.setVisible(true);
		
		if (dialog.useOriginal) {
			return dialog.originalId;
		} else {
			return dialog.getSelectedScreenId();
		}
	}
	
}
