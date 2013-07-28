package edu.nova.erikaredmark.monkeyshines.editor.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;

/** 
 * 
 * Utility class for launching JComponent objects in full JFrame dialogs. Mainly designed for the many different dialogs
 * that are launched for the level editor
 * 
 * @author Erika Redmark
 *
 */
public final class DialogLauncher {
	private DialogLauncher() { }
	
	public static void launch(final JFrame parent, final String title, final JComponent toLaunch) {
		final JDialog dialog = new JDialog(parent);
		dialog.setLayout(new BorderLayout() );
		dialog.add(toLaunch, BorderLayout.CENTER);
		dialog.pack();
		
		// Center window
		Point pos = parent.getLocation();
		dialog.setLocation(pos.x + 10, pos.y + 10 );
		
		dialog.setVisible(true);
	}
	
}
