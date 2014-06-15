package org.erikaredmark.monkeyshines.editor.dialog;


/**
 * 
 * Allows the user to choose between two screens. The selected screen id is returned back to the caller.
 * 
 * @author Erika Redmark
 *
 */
public final class SelectScreenDialog {

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
	 * @return
	 * 		one of the passed arguments chosen. Never any value other than one of them
	 * 
	 */
	public static int launch(int firstScreen, int secondScreen) {
		// TODO method stub
		return firstScreen;
	}
	
}
