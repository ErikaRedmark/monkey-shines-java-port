package edu.nova.erikaredmark.monkeyshines.editor.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.nova.erikaredmark.monkeyshines.Hazard;
import edu.nova.erikaredmark.monkeyshines.editor.HazardMutable;
import edu.nova.erikaredmark.monkeyshines.graphics.WorldResource;

/**
 * 
 * For a given graphics resource which supplies a given number of hazard icons, allows the user to define each hazard
 * icon graphically in terms of what properties each hazard has when placed on a world. This is not WHERE the hazards are
 * placed, but rather what they actually are.
 * <p/>
 * Initially all hazards start out at some default value. Since hazard sprite sheets enumerate tile sized hazards and each 
 * column is a new hazard, the number of hazard available to the user is equal to the length-wise size of the hazard sheet 
 * divided by the pixels per tile (length-wise). Since these dialogs can only be launched with valid graphics resources users
 * cannot assign hazards to non-existent sprites.
 * 
 * @author Erika Redmark
 *
 */
public class EditHazardsDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private final EditHazardsModel model;
	
	/**
	 * 
	 * Constructs the dialog with the diven model. The dialog is ready but not immediately visible after this construction.
	 * 
	 * @param model
	 * 
	 * @param rsrc
	 * 
	 */
	private EditHazardsDialog(final EditHazardsModel model, final WorldResource rsrc) {
		this.model = model;
		//springLayout.putConstraint(SpringLayout.NORTH, txtVelocityY, -3, SpringLayout.NORTH, lblVelocityy);
		getContentPane().setLayout(new GridBagLayout() );
		
		

		/* ------------------ Hazard list ------------------- */
		// This list will sync changes to the underlying EditHazardsModel
		final DefaultListModel<Hazard> hazardListModel = new DefaultListModel<>();
		
		// Assumption: All elements in the passed model are sorted, since they can only be actually generated from here,
		// and all elements generated here will be sorted
		for (Hazard h : this.model.getHazards() ) {
			hazardListModel.addElement(h);
		}
		
		final JList<Hazard> hazardList = new JList<Hazard>(hazardListModel);
		hazardList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// Attach to upper left of window and extend to bottom
		
		final GridBagConstraints hazardListGbc = new GridBagConstraints();
		hazardListGbc.gridx = 0;
		hazardListGbc.gridy = 0;
		hazardListGbc.gridheight = GridBagConstraints.REMAINDER;
		// Embed list in Scrollable pane to allow scrollbars, but apply constraints to scrollable pane
		JScrollPane hazardListWrapped = new JScrollPane(hazardList);
		getContentPane().add(hazardListWrapped, hazardListGbc);
		
		/* ------------------ New Hazard -------------------- */
		final JButton newHazardButton = new JButton(new AbstractAction("New Hazard") {
			private static final long serialVersionUID = 1L;

			@Override public void actionPerformed(ActionEvent e) {
				// Get the list, make a new hazard to it, and redo the model.
				List<Hazard> newModel = model.getMutableHazards();
				
				// Perform a check: We cannot add a new hazard if there is not enough space in the graphics context to 
				// render the hazard. Number of hazards is limited by the size of our resource.
				if (!(rsrc.canAddHazard(newModel.size() ) ) ) {
					// TODO error message
					return;
				}
				
				// newModel.size means assign the next Id. Assuming sorted list, size will always equal the next index of a new element
				Hazard.newHazardTo(newModel, newModel.size(), rsrc );
				
				// Update the list model used by the view
				hazardListModel.clear();
				for (Hazard h : newModel) {
					hazardListModel.addElement(h);
				}
			}
		});
		
		final GridBagConstraints newHazardButtonGbc = new GridBagConstraints();
		newHazardButtonGbc.weightx = 0.5;
		getContentPane().add(newHazardButton, newHazardButtonGbc);
		
		
		/* ------------------ Delete Hazard ------------------- */
		final JButton deleteHazardButton = new JButton(new AbstractAction("Delete Hazard") {
			private static final long serialVersionUID = 1L;

			@Override public void actionPerformed(ActionEvent e) {
				// Get the list, remove the hazard currently selected in view
				List<Hazard> newModel = model.getMutableHazards();
				
				Hazard hazardToRemove = hazardList.getSelectedValue();
				
				if (hazardToRemove != null) {
				
					Hazard.removeHazard(newModel, hazardToRemove);
					
					// update the list model used by the view
					hazardListModel.clear();
					for (Hazard h : newModel) {
						hazardListModel.addElement(h);
					}
				
				}
			}
		});
		
		final GridBagConstraints deleteHazardButtonGbc = new GridBagConstraints();
		deleteHazardButtonGbc.weightx = 0.5;
		getContentPane().add(deleteHazardButton, deleteHazardButtonGbc);
		
		/* -------------------- Edit Hazard Panel --------------------- */
		final EditSingleHazardPanel editPanel = new EditSingleHazardPanel();
		
		/* ----------------- Selection Listener ----------------- */
		/* Selected hazards are under edit in the edit panel.	  */
		/* This listener binds to the original hazard list.		  */
		hazardList.addListSelectionListener(new ListSelectionListener() {
			@Override public void valueChanged(ListSelectionEvent event) {
				Hazard newHazard = hazardList.getSelectedValue();
				if (newHazard == null)  return;
				
				// TODO some 'are you sure?' code if replacing an existing edit hazard context
				editPanel.setHazardEditing(newHazard);
			}
		});
		
		final GridBagConstraints editPanelGbc = new GridBagConstraints();
		editPanelGbc.weightx = 2.0;
		editPanelGbc.gridwidth = GridBagConstraints.REMAINDER;
		editPanelGbc.gridheight = GridBagConstraints.REMAINDER;
		getContentPane().add(editPanel, editPanelGbc);
		
		/* ----------------------- Apply Edit ------------------------- */
		final JButton applyEditButton = new JButton(new AbstractAction("Apply Changes") {
			private static final long serialVersionUID = 1L;

			@Override public void actionPerformed(ActionEvent e) {
				// TODO apply changes
			}
		});
		
		final GridBagConstraints applyEditButtonGbc = new GridBagConstraints();
		editPanelGbc.weightx = 0.5;
		getContentPane().add(applyEditButton, applyEditButtonGbc);
		
		hazardList.setVisible(true);
		setSize(500, 200);
	}
	
	/**
	 * 
	 * Panel used by dialog that, given a specific Hazard property, alonws one to make modifications to it. Changes to
	 * a hazard can then replace a hazard in the view, and therefore the main model, or can be discarded.
	 * 
	 * @author Erika Redmark
	 *
	 */
	private static final class EditSingleHazardPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		
		// This mutable copy is never published outside of this object.
		private HazardMutable currentlyEditingHazard;
		
		// The immutable id of even the mutable hazard can tell us what to replace, but for easeness we
		// store a reference to the immutable hazard this one is derived to do equals calculations within
		// this object (for unsaved changes and such)
		private Hazard originalFromCurrentEdit;
		
		private EditSingleHazardPanel() {
			// TODO view code
		}
		
		
		/**
		 * 
		 * Makes a mutable copy of the given hazard and places it under editing
		 * 
		 */
		public void setHazardEditing(final Hazard hazard) {
			this.currentlyEditingHazard = hazard.mutableCopy();
			this.originalFromCurrentEdit = hazard;
			// TODO update view
		}
		
		/**
		 * 
		 * Returns an immutable copy of the hazard currently under edit. This call MAY fail with an exception if there is
		 * no hazard under edit. Use {@code isEditingHazard() } first to confirm
		 * 
		 * @throws IllegalStateException
		 * 		if called when there is no hazard being edited
		 * 
		 */
		public Hazard getHazardEdited() {
			if (currentlyEditingHazard == null)  throw new IllegalStateException("No hazard being editing");
			return this.currentlyEditingHazard.immutableCopy();
		}
		
		/**
		 * 
		 * Determines if this panel is currently editing a hazard or if it is not.
		 * 
		 * @return
		 * 		{@code true} if editing a hazard, {@code false} if otherwise
		 * 
		 */
		public boolean isEditingHazard() {
			return currentlyEditingHazard != null;
		}
		
		
	}

	/**
	 * 
	 * Launches the given dialog with the associated parent and world resource. This call will block until the user is
	 * finished, upon which the model will be returned representing the logical state that the user made changes to.
	 * This model should then be synced with the world resource currently being edited for changes to take effect.
	 * 
	 * @param parent
	 * 		the parent component
	 * 
	 * @param rsrc
	 * 		the world resource to draw the graphics from. <strong> This affects how many hazards are available</strong>
	 * 		based on the size of the sprite sheet.
	 * 
	 */
	public static EditHazardsModel launch(JComponent parent, WorldResource rsrc, List<Hazard> worldHazards) {
		final EditHazardsModel model = new EditHazardsModel(worldHazards);
		final EditHazardsDialog dialog = new EditHazardsDialog(model, rsrc);
		
		// Blocks due to dialog modality
		dialog.setModal(true);
		dialog.setVisible(true);
		
		return dialog.model;
	}
	
}
