package org.erikaredmark.monkeyshines.editor.dialog;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.JCheckBox;

import org.erikaredmark.monkeyshines.encoder.WorldIO;
import org.erikaredmark.monkeyshines.encoder.exception.WorldSaveException;
import org.erikaredmark.util.BinaryLocation;


/**
 * 
 * Allows user to set settings for the newly generated world, and defers to utility class for carrying out creating the 
 * structure asked for
 * 
 * @author TJS
 *
 */
public class NewWorldDialog extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private final JDialog parentDialog;
	
	private final JTextField txtWorldName;
	private final JTextField txtResourcePack;
	private final JCheckBox chckbxUseDefaultPack;
	private final JButton btnBrowseResourcePack;
	
	
	private final NewWorldDialogModel model;
	private final Controller controller;
	
	private final Action btnResourcePackAction =
		new AbstractAction("Browse...") {
			private static final long serialVersionUID = 6168460348517517770L;
			@Override public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(BinaryLocation.BINARY_LOCATION.toFile() );
				if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					Path rsrcPath = file.toPath();
					model.setSelectedResourcePack(rsrcPath);
					txtResourcePack.setText(rsrcPath.toString() );
				}
			}
		};
	
	private final Action useDefaultAction =
		new AbstractAction("Use Default Pack") {
			private static final long serialVersionUID = 5848943844967927018L;
			@Override public void actionPerformed(ActionEvent e) {
				boolean selected = chckbxUseDefaultPack.isSelected();
				model.setUseDefaultPack(selected);
				// Enable or disable path and browse text and buttons
				txtResourcePack.setEnabled(!(selected) );
				btnBrowseResourcePack.setEnabled(!(selected) );
			}
		};
		
	private final Action saveAction =
		new AbstractAction("Save") {
			private static final long serialVersionUID = 3229707262753672755L;
			@Override public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(BinaryLocation.BINARY_LOCATION.toFile() );
				if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					Path savePath = file.toPath();
					String worldName = model.getWorldName();
					try {
						if (model.isUseDefaultPack() ) {
							WorldIO.newWorldWithDefault(savePath, worldName);
						} else {
							Path rsrcPath = model.getSelectedResourcePack();
							if (rsrcPath == null) {
								showResourcePackError("No resource pack selected.");
								return;
							}
							if (!(Files.exists(rsrcPath) ) ) {
								showResourcePackError("Location does not exist: ");
								return;
							}
							
							if (Files.isDirectory(rsrcPath) ) {
								showResourcePackError("Location is a directory: ");
								return;
							}
							
							try {
								WorldIO.newWorldWithResources(savePath, worldName, rsrcPath);
							} catch (WorldSaveException ex) {
								showResourcePackError("Issue with resource pack: " + ex.getMessage() + " for pack: ");
								// TODO logging API
								ex.printStackTrace();
								return;
							}
						}
						
						// Postcondition: World is saved. Dialog is no longer relevant: close it.
						// Save the state of what the user chose (the new file)
						model.saveLocation = savePath.resolve(worldName + ".world");
						parentDialog.setVisible(false);
						// dispose();
						// setVisible(false);
						// Dispatch event so system knows to actually close the dialog
						// dispatchEvent(new ComponentEvent(NewWorldDialog.this, ComponentEvent.COMPONENT_HIDDEN));
						
					// These catch clauses do not deal with resource pack issues, only other, more general ones.
					} catch (IOException | WorldSaveException ex) {
						JOptionPane.showMessageDialog(null,
						    "Unable to save due to " + ex.getMessage(),
						    "Saving Error",
						    JOptionPane.ERROR_MESSAGE);
						ex.printStackTrace();
					}
				}
			}
		};

	/**
	 * 
	 * Shows an error dialog intended to alert the user that the resource pack selected is invalid for some reason.
	 * 
	 * @param msg
	 * 		the reason
	 * 
	 */
	private void showResourcePackError(String msg) {
		JOptionPane.showMessageDialog(
			null,
			msg + (   model.getSelectedResourcePack() != null
					? model.getSelectedResourcePack().toString()
					: ""),
		    "Resource Pack Issue",
		    JOptionPane.ERROR_MESSAGE);
	}
		
	/**
	 * 
	 * Creates a new instance of this dialog and allows the user to create a new world. This method is
	 * NOT blocking. The dialog is initialised with its model but must be lauched. After the dialog
	 * is closed, it will have populated a result in the model that indicates where the new world
	 * was saved to, if it was saved at all. Clients may wish to query the model for that and
	 * any other state information after the dialog has been launched and closed.
	 * 
	 * @param parentDialog
	 * 		the dialog object that is directly supporting this dialog (since this is
	 * 		technically a JComponent). Used to provide early dialog closure
	 * 
	 */
	public NewWorldDialog(final JDialog parentDialog) {
		this.parentDialog = parentDialog;
		
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		JLabel lblWorldName = new JLabel("World Name");
		springLayout.putConstraint(SpringLayout.NORTH, lblWorldName, 13, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, lblWorldName, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, lblWorldName, -370, SpringLayout.EAST, this);
		add(lblWorldName);
		
		JLabel lblResourcePack = new JLabel("Resource Pack");
		springLayout.putConstraint(SpringLayout.NORTH, lblResourcePack, 33, SpringLayout.SOUTH, lblWorldName);
		springLayout.putConstraint(SpringLayout.WEST, lblResourcePack, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, lblResourcePack, 0, SpringLayout.EAST, lblWorldName);
		add(lblResourcePack);
		
		txtWorldName = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, txtWorldName, 0, SpringLayout.NORTH, lblWorldName);
		springLayout.putConstraint(SpringLayout.WEST, txtWorldName, 6, SpringLayout.EAST, lblWorldName);
		springLayout.putConstraint(SpringLayout.EAST, txtWorldName, 236, SpringLayout.EAST, lblWorldName);
		add(txtWorldName);
		txtWorldName.setColumns(10);
		txtWorldName.addFocusListener(new FocusListener() {
			@Override public void focusGained(FocusEvent arg0) { /* no op */ }
			@Override public void focusLost(FocusEvent arg0) {
				// Losing focus basically means update the world name internally. We don't want to update on a char by char basis 
				// as that would be bloody slow
				model.setWorldName(txtWorldName.getText() );
			}
		
		});
		
		txtResourcePack = new JTextField();
		// Only can be changed via browse: Minor TODO to allow typed paths
		txtResourcePack.setEditable(false);
		springLayout.putConstraint(SpringLayout.NORTH, txtResourcePack, 0, SpringLayout.NORTH, lblResourcePack);
		springLayout.putConstraint(SpringLayout.WEST, txtResourcePack, 6, SpringLayout.EAST, lblResourcePack);
		springLayout.putConstraint(SpringLayout.EAST, txtResourcePack, 236, SpringLayout.EAST, lblResourcePack);
		add(txtResourcePack);
		txtResourcePack.setColumns(10);
		
		btnBrowseResourcePack = new JButton(btnResourcePackAction);
		springLayout.putConstraint(SpringLayout.NORTH, btnBrowseResourcePack, 0, SpringLayout.NORTH, txtResourcePack);
		springLayout.putConstraint(SpringLayout.WEST, btnBrowseResourcePack, 6, SpringLayout.EAST, txtResourcePack);
		springLayout.putConstraint(SpringLayout.EAST, btnBrowseResourcePack, -10, SpringLayout.EAST, this);
		add(btnBrowseResourcePack);
		
		chckbxUseDefaultPack = new JCheckBox(useDefaultAction);
		springLayout.putConstraint(SpringLayout.NORTH, chckbxUseDefaultPack, 6, SpringLayout.SOUTH, txtResourcePack);
		springLayout.putConstraint(SpringLayout.WEST, chckbxUseDefaultPack, 372, SpringLayout.WEST, this);
		add(chckbxUseDefaultPack);
		
		JButton btnSave = new JButton(saveAction);
		springLayout.putConstraint(SpringLayout.WEST, btnSave, 420, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, btnSave, -10, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.EAST, btnSave, 0, SpringLayout.EAST, btnBrowseResourcePack);
		add(btnSave);

		model = NewWorldDialogModel.newInstance();
		controller = new Controller(model);
		
		setPreferredSize(new Dimension(500, 158) );
		setSize(500, 200);
	}

	
	/**
	 * 
	 * Returns the model associated with this view.
	 * 
	 * @return
	 */
	public NewWorldDialogModel getModel() {
		return this.model;
	}
	
	/**
	 * 
	 * Removes all listener bindings between the view and model, effectively killing this view. Only when use when view is no
	 * longer accessible.
	 * 
	 */
	public void dispose() {
		controller.dispose();
	}
	
	/**
	 * 
	 * Each view creates one controller that has access to all private fields. This controller handles 
	 * model->view interaction. View -> model interaction is done directly
	 * <p/>
	 * Interaction here, for obvious reasons, do not modify the model (since the data comes from the model)
	 * hence there will be no circularity to property changes.
	 * 
	 * @author Erika Redmark
	 *
	 */
	private final class Controller implements PropertyChangeListener {
		final NewWorldDialogModel model;
		
		Controller(final NewWorldDialogModel model) {
			this.model = model;
			this.model.addPropertyChangeListener(this);
		}

		@Override public void propertyChange(PropertyChangeEvent event) {
			switch (event.getPropertyName()) {
			case NewWorldDialogModel.PROPERTY_DEFAULT_PACK:
				break;
			case NewWorldDialogModel.PROPERTY_PACK:
				break;
			case NewWorldDialogModel.PROPERTY_WORLD_NAME:
				break;
			}
		}
		
		void dispose() {
			this.model.removePropertyChangeListener(this);
		}
	}
	
}
