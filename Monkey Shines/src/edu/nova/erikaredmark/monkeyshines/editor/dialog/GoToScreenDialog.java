package edu.nova.erikaredmark.monkeyshines.editor.dialog;

import java.awt.Frame;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.redmark.erika.util.StringToNumber;

import com.google.common.base.Optional;

/**
 * This dialog is displayed when the user wishes to go to a specific screen (level) in the current world
 * 
 * @author Erika Redmark
 */
public final class GoToScreenDialog {
	
	public JTextField getScreenIdField() { return screenIdField; }
    public JLabel getLblScreenIdField() { return lblScreenIdField; }
	public JComponent[] getInputsArray() { return inputsArray; }

	private final JTextField screenIdField;
	private final JLabel     lblScreenIdField;
	private final JComponent[] inputsArray;
	
	private GoToScreenDialog() {
		screenIdField = new JTextField(8);
		lblScreenIdField = new JLabel("Screen Id:");
		inputsArray = new JComponent[] {lblScreenIdField, screenIdField};
	}
	
	/**
	 * Displays the dialog and gets the id the user selected. This is a blocking call, and effectively makes the dialog 
	 * modal with respect to the current thread
	 * 
	 * @param parent
	 * 		the parent frame to display this dialog for. {@code null} is a perfectly acceptable type for this parameter,
	 * 		and in that event a default frame will be used
	 * 
	 * @param currentId
	 * 		the currently displayed screen id, used as a default in the text field
	 * 
	 * @return
	 * 		the id of the screen the user asked for. If the user cancels, it returns nothing
	 * 
	 */
	public static Optional<Integer> displayAndGetId(Frame parent, int currentId) {
		GoToScreenDialog d = new GoToScreenDialog();
		int result = JOptionPane.showConfirmDialog(parent, d.getInputsArray(), "Go To Screen...", JOptionPane.OK_CANCEL_OPTION);
		
		return (result == JOptionPane.CANCEL_OPTION ? Optional.<Integer>absent() : 
													  StringToNumber.string2Int(d.getScreenIdField().getText()) );
	}
	
}
