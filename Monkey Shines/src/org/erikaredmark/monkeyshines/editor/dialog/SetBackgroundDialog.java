package org.erikaredmark.monkeyshines.editor.dialog;

import javax.swing.JDialog;

import org.erikaredmark.monkeyshines.background.Background;
import org.erikaredmark.monkeyshines.resource.WorldResource;

public class SetBackgroundDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	// The model for this dialog: The user's selected background choice.
	private Background selectedBackground;
	
	public SetBackgroundDialog(WorldResource rsrc) {
		// Three tabs: One for full backgrounds, one for patterns, and finally one for
		// a colour picker.
		// Show a 'selected background' to the right so they know which of the three tabs
		// is active and which background was picked.
		
		// TODO
	}
	
	/**
	 * 
	 * Launches this dialog, allowing the user to choose a background. The user may choose an existing
	 * full background, pattern, or solid color. If the user closes the dialog without choosing, this
	 * will return {@code null}; null should signify 'no change'. 
	 * 
	 * @param rsrc
	 * 		the world resource to grab the backgrounds from
	 * 
	 * @return
	 * 		a background instance the user chose, or {@code null} if they chose to cancel
	 * 
	 */
	public static Background launch(WorldResource rsrc) {
		SetBackgroundDialog dialog = new SetBackgroundDialog(rsrc);
		dialog.setVisible(true);
		
		return dialog.selectedBackground;
	}
	
}
