package edu.nova.erikaredmark.monkeyshines.editor.dialog;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBox;


/**
 * 
 * Allows user to set settings for the newly generated world, and defers to utility class for carrying out creating the 
 * structure asked for
 * 
 * @author TJS
 *
 */
public class NewWorldDialog extends JPanel implements LaunchableDialog {
	private static final long serialVersionUID = 1L;
	
	private final JTextField txtWorldName;
	private final JTextField txtResourcePack;
	private final JCheckBox chckbxUseDefaultPack;
	
	
	private final NewWorldDialogModel model;
	private final Controller controller;
	
	private final Action btnResourcePackAction =
		new AbstractAction("Browse...") {
			private static final long serialVersionUID = 6168460348517517770L;
			@Override public void actionPerformed(ActionEvent e) {
				// TODO open file browser, update path in model
			}
		};
	
	private final Action useDefaultAction =
		new AbstractAction("Use Default Pack") {
			private static final long serialVersionUID = 5848943844967927018L;
			@Override public void actionPerformed(ActionEvent e) {
				model.setUseDefaultPack(chckbxUseDefaultPack.isSelected() );
			}
		};
		
	private final Action okayAction =
		new AbstractAction("Okay") {
			private static final long serialVersionUID = 3229707262753672755L;
			@Override public void actionPerformed(ActionEvent e) {
				// TODO offload model to world generator and close dialog
			}
		};
		
	private final Action cancelAction =
		new AbstractAction("Cancel") {
			private static final long serialVersionUID = -7231610038298935644L;
			@Override public void actionPerformed(ActionEvent e) {
				// TODO close dialog forget everything
			}
		};

	public NewWorldDialog() {
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
		springLayout.putConstraint(SpringLayout.NORTH, txtWorldName, 10, SpringLayout.NORTH, this);
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
		springLayout.putConstraint(SpringLayout.NORTH, txtResourcePack, 24, SpringLayout.SOUTH, txtWorldName);
		springLayout.putConstraint(SpringLayout.WEST, txtResourcePack, 6, SpringLayout.EAST, lblResourcePack);
		springLayout.putConstraint(SpringLayout.EAST, txtResourcePack, 236, SpringLayout.EAST, lblResourcePack);
		add(txtResourcePack);
		txtResourcePack.setColumns(10);
		
		JButton btnBrowseResourcePack = new JButton(btnResourcePackAction);
		springLayout.putConstraint(SpringLayout.NORTH, btnBrowseResourcePack, 51, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, btnBrowseResourcePack, 6, SpringLayout.EAST, txtResourcePack);
		springLayout.putConstraint(SpringLayout.EAST, btnBrowseResourcePack, -10, SpringLayout.EAST, this);
		btnBrowseResourcePack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		add(btnBrowseResourcePack);
		
		chckbxUseDefaultPack = new JCheckBox(useDefaultAction);
		springLayout.putConstraint(SpringLayout.NORTH, chckbxUseDefaultPack, 6, SpringLayout.SOUTH, txtResourcePack);
		springLayout.putConstraint(SpringLayout.WEST, chckbxUseDefaultPack, 0, SpringLayout.WEST, btnBrowseResourcePack);
		add(chckbxUseDefaultPack);
		
		JButton btnOkay = new JButton(okayAction);
		springLayout.putConstraint(SpringLayout.SOUTH, btnOkay, -10, SpringLayout.SOUTH, this);
		add(btnOkay);
		
		JButton btnCancel = new JButton(cancelAction);
		springLayout.putConstraint(SpringLayout.WEST, btnOkay, -76, SpringLayout.WEST, btnCancel);
		springLayout.putConstraint(SpringLayout.EAST, btnOkay, -6, SpringLayout.WEST, btnCancel);
		springLayout.putConstraint(SpringLayout.SOUTH, btnCancel, -10, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.EAST, btnCancel, 0, SpringLayout.EAST, btnBrowseResourcePack);
		add(btnCancel);

		model = NewWorldDialogModel.newInstance();
		controller = new Controller(model);
		
		setPreferredSize(new Dimension(500, 200) );
		setSize(500, 200);
		
		// TODO remove listeners on dispose action
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
