package org.erikaredmark.monkeyshines.menu;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * 
 * Modal dialog intended to pop up when the player has finished a level properly. They are entered in the 'high scores' table, so
 * the return of this dialog is intended to be used with the {@code HighScores} object.
 * 
 * @author Erika Redmark
 *
 */
public final class EnterHighScoreDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private static final int SIZE_X = 100;
	private static final int SIZE_Y = 200;
	
	private String playerName = "Dash Riprock";
	
	private EnterHighScoreDialog() {
		setLayout(new GridLayout(3, 1) );
		add(new JLabel("Congratulations on a high score! Enter your name.") );
		
		final JTextField entryText = new JTextField("Dash Riprock");
		add(entryText);
		
		final OkayButton okay = new OkayButton(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				playerName = entryText.getText();
				setVisible(false);
			}

		});
		
		setSize(500, 200);
		setPreferredSize(new Dimension(500, 200) );
		setMinimumSize(new Dimension(500, 200) );
		
		add(okay);
	}
	
	/**
	 * 
	 * Launches the dialog, indicating the player has achieved a new high score. Returns the
	 * name the player enters, so that the calculated score and entered name could be used
	 * in a high scores table.
	 * 
	 * @return
	 * 		the name of the player as they entered it.
	 * 
	 */
	public static String launch() {
		EnterHighScoreDialog dialog = new EnterHighScoreDialog();
		dialog.setModal(true);
		dialog.setSize(SIZE_X, SIZE_Y);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		
		return dialog.playerName;
	}
	
}
