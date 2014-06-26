package org.erikaredmark.monkeyshines.editor.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * 
 * Shows the user a dialog that gives a good sized text area for them to write any authorship
 * information they wish to.
 * 
 * @author Erika Redmark
 *
 */
public final class AuthorshipDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	// Whatever this contains when the user okays out is effectively the new author. unless
	// the user hits okay, this is NOT overwritten from text from the field as remains
	// in original form.
	private String author;
	
	private AuthorshipDialog(final String originalAuthor) {
		this.author = originalAuthor;
		
		setLayout(new BorderLayout() );
		
		JLabel label = new JLabel();
		label.setText("Authorship Information");
		add(label, BorderLayout.NORTH);
		
		final JTextArea authorText = new JTextArea(originalAuthor);
		JScrollPane authorPane = new JScrollPane(authorText);
		add(authorPane, BorderLayout.CENTER);
		
		// Okay and Cancel buttons: Cancel does nothing (author stays as it was).
		// The okay button sets the author to the contents of the text field before
		// closing the window.
		JPanel okayCancelPanel = new JPanel();
		okayCancelPanel.setLayout(new FlowLayout() );
		
		JButton okay = new JButton(new AbstractAction("Set Authorship") {
			private static final long serialVersionUID = 1L;
			@Override public void actionPerformed(ActionEvent arg0) {
				author = authorText.getText();
				setVisible(false);
			}
		});
		
		JButton cancel = new JButton(new AbstractAction("Cancel") {
			private static final long serialVersionUID = 1L;
			@Override public void actionPerformed(ActionEvent arg0) {
				// Author is currently still set to original string. Just close.
				setVisible(false);
			}
		});
		
		okayCancelPanel.add(okay);
		okayCancelPanel.add(cancel);
		
		add(okayCancelPanel, BorderLayout.SOUTH);
		
		setSize(300, 200);
		
	}
	
	public static String launch(String initialAuthor) {
		AuthorshipDialog dialog = new AuthorshipDialog(initialAuthor);
		
		dialog.setModal(true);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		
		return dialog.author;
	}
	
}
