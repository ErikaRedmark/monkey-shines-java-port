package edu.nova.erikaredmark.monkeyshines.editor.dialog;

import java.awt.BorderLayout;
import java.awt.Point;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;

/** 
 * 
 * Utility class for launching JComponent objects in full JFrame dialogs. Mainly designed for the many different dialogs
 * that are launched for the level editor.
 * 
 * @author Erika Redmark
 *
 */
public final class DialogLauncher {
	private DialogLauncher() { }
	
	/**
	 * 
	 * Launches the given {@code JComponent} as a dialog within the given parent with a title. This
	 * method is blocking until the user closes the dialog. 
	 * <p/>
	 * Some dialogs carry with them model
	 * objects that are populated with result data upon closure. In these cases the dialog
	 * MUST be modal otherwise the client won't be able to query the results of the dialog.
	 * 
	 * @param parent
	 * 		the parent frame this dialog will be rooted under
	 * 
	 * @param title
	 * 		the title of the dialog
	 * 
	 * @param toLaunch
	 * 		the actual component to launch as a dialog
	 * 
	 * @param modal
	 * 		{@code true} for a modal dialog, {@code false} otherwise
	 * 
	 */
	public static void launch(final JFrame parent, final String title, final JComponent toLaunch, boolean modal) {
		final JDialog dialog = new JDialog(parent);
		dialog.setLayout(new BorderLayout() );
		dialog.add(toLaunch, BorderLayout.CENTER);
		dialog.pack();
		
		dialog.setModal(modal);
		// Center window
		Point pos = parent.getLocation();
		dialog.setLocation(pos.x + 10, pos.y + 10 );
		
		dialog.setVisible(true);
	}
	
}
