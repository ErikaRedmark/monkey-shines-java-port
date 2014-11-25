package org.erikaredmark.monkeyshines.editor.dialog;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.erikaredmark.monkeyshines.editor.importlogic.ResourceForkTranslator;
import org.erikaredmark.monkeyshines.editor.importlogic.WorldTranslationException;
import org.erikaredmark.monkeyshines.encoder.exception.WorldSaveException;
import org.erikaredmark.monkeyshines.graphics.exception.ResourcePackException;
import org.erikaredmark.util.BinaryLocation;

/**
 * 
 * Allows the user to use an import facility to import an existing world from the original Monkey Shines game into
 * the new game.
 * 
 * @author Erika Redmark
 *
 */
@SuppressWarnings("serial")
public final class ImportWorldDialog extends JDialog {
	private static final String CLASS_NAME = "org.erikaredmark.monkeyshines.editor.dialog.ImportWorldDialog";
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
	
	private ImportWorldDialog() {
		Container parent = getContentPane();
		SpringLayout layout = new SpringLayout();
		parent.setLayout(layout);
		
		final JLabel warning = new JLabel("Warning: Import does NOT work with .msh files (Windows Monkey Shines Worlds)");
		layout.putConstraint(SpringLayout.NORTH, warning, 5,
							 SpringLayout.NORTH, parent);
		
		layout.putConstraint(SpringLayout.WEST, warning, 5,
							 SpringLayout.WEST, parent);
		
		parent.add(warning);
		
		final JLabel lblResourceFork = new JLabel("Resource Fork");
		final JTextField resourceForkPath = new JTextField();
		resourceForkPath.setEditable(false);
		final JButton resourceForkBrowse = new JButton(new AbstractAction("Browse...") {
			@Override public void actionPerformed(ActionEvent event) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(BinaryLocation.BINARY_LOCATION.toFile() );
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					forkPath = file.toPath();
					resourceForkPath.setText(forkPath.toString() );
				}
			}
		});
		
		layout.putConstraint(SpringLayout.WEST, lblResourceFork, 5,
							 SpringLayout.WEST, parent);
		
		layout.putConstraint(SpringLayout.NORTH, lblResourceFork, 5,
							 SpringLayout.SOUTH, warning);
		
		layout.putConstraint(SpringLayout.EAST, resourceForkBrowse, -5,
							 SpringLayout.EAST, parent);
		
		layout.putConstraint(SpringLayout.NORTH, resourceForkBrowse, 0,
							 SpringLayout.NORTH, lblResourceFork);
		
		layout.putConstraint(SpringLayout.WEST, resourceForkPath, 5, 
						     SpringLayout.EAST, lblResourceFork);
		
		layout.putConstraint(SpringLayout.EAST, resourceForkPath, -5,
							 SpringLayout.WEST, resourceForkBrowse);
		
		layout.putConstraint(SpringLayout.NORTH, resourceForkPath, 0,
							 SpringLayout.NORTH, lblResourceFork);
		
		parent.add(lblResourceFork);
		parent.add(resourceForkPath);
		parent.add(resourceForkBrowse);
		
		final JLabel lblResourcePack = new JLabel("Resource Pack");
		final JTextField resourcePackPath = new JTextField();
		resourcePackPath.setEditable(false);
		final JButton resourcePackBrowse = new JButton(new AbstractAction("Browse...") {
			@Override public void actionPerformed(ActionEvent event) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(BinaryLocation.BINARY_LOCATION.toFile() );
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					rsrcPath = file.toPath();
					resourcePackPath.setText(rsrcPath.toString() );
				}
			}
		});
		
		layout.putConstraint(SpringLayout.WEST, lblResourcePack, 5,
							 SpringLayout.WEST, parent);

		layout.putConstraint(SpringLayout.NORTH, lblResourcePack, 15,
				 			 SpringLayout.SOUTH, lblResourceFork);

		layout.putConstraint(SpringLayout.EAST, resourcePackBrowse, -5,
				 			 SpringLayout.EAST, parent);

		layout.putConstraint(SpringLayout.NORTH, resourcePackBrowse, 0,
							 SpringLayout.NORTH, lblResourcePack);
		
		layout.putConstraint(SpringLayout.WEST, resourcePackPath, 5, 
						     SpringLayout.EAST, lblResourcePack);
		
		layout.putConstraint(SpringLayout.EAST, resourcePackPath, -5, 
			     			 SpringLayout.WEST, resourcePackBrowse);

		layout.putConstraint(SpringLayout.NORTH, resourcePackPath, 0,
							 SpringLayout.NORTH, lblResourcePack);
		
		parent.add(lblResourcePack);
		parent.add(resourcePackPath);
		parent.add(resourcePackBrowse);
		
		final JLabel lblSave = new JLabel("World will be saved to the same folder that the resource pack resides");
		layout.putConstraint(SpringLayout.WEST, lblSave, 5,
							 SpringLayout.WEST, parent);
		
		layout.putConstraint(SpringLayout.NORTH, lblSave, 15,
							 SpringLayout.SOUTH, lblResourcePack);
		
		parent.add(lblSave);
		
		final JButton importLevel = new JButton(new AbstractAction("Import") {
			@Override public void actionPerformed(ActionEvent event) {
				try {
					pathToTranslatedWorld = ResourceForkTranslator.importWorldAndAutoSave(forkPath, rsrcPath);
				} catch (WorldTranslationException | ResourcePackException | WorldSaveException e) {
					JOptionPane.showMessageDialog(ImportWorldDialog.this, "Could not import the world: " + e.getMessage() );
					LOGGER.log(Level.WARNING,
							   CLASS_NAME + ": Could not import the world: " + e.getMessage(),
							   e);
				}
				setVisible(false);
			}
		});
	
		layout.putConstraint(SpringLayout.EAST, importLevel, -5,
							 SpringLayout.EAST, parent);
		
		layout.putConstraint(SpringLayout.NORTH, importLevel, 5,
							 SpringLayout.SOUTH, lblSave);
		
		parent.add(importLevel);
		
		final JButton cancel = new JButton(new AbstractAction("Cancel") {
			@Override public void actionPerformed(ActionEvent event) { setVisible(false); }
		});
		
		layout.putConstraint(SpringLayout.WEST, cancel, 5,
							 SpringLayout.WEST, parent);

		layout.putConstraint(SpringLayout.NORTH, cancel, 0,
							 SpringLayout.NORTH, importLevel);
		
		parent.add(cancel);
	}
	
	/** Returns the world that was translated, or {@code null} if no such world was translated */
	private Path getPathToWorld() {
		return pathToTranslatedWorld;
	}
	
	/**
	 *
	 * Displays the dialog, asking the user to indicate which files to use for import. If they import and it is successful, the
	 * world is returned after being saved to another location
	 * 
	 * @return
	 * 		a path to generated world, or {@code null} if cancelled or a world could not be generated
	 * 
	 */
	public static Path launch() {
		ImportWorldDialog dialog = new ImportWorldDialog();
		dialog.setSize(580, 200);
		dialog.setModal(true);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		
		return dialog.getPathToWorld();
	}
	
	// Starts out null and remains so until world is translated.
	// Currently, due to how the level editor saves, the world must be saved to disk first before
	// it can be loaded into the editor.
	private Path pathToTranslatedWorld;
	private Path forkPath = Paths.get("");
	private Path rsrcPath = Paths.get("");
	
}
