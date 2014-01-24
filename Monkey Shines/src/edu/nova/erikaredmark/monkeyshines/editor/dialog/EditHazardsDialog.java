package edu.nova.erikaredmark.monkeyshines.editor.dialog;

import java.awt.FlowLayout;
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
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

import edu.nova.erikaredmark.monkeyshines.DeathAnimation;
import edu.nova.erikaredmark.monkeyshines.Hazard;
import edu.nova.erikaredmark.monkeyshines.graphics.WorldResource;

/**
 * 
 * For a given graphics resource which supplies a given number of hazard icons, allows the user to define each hazard
 * icon graphically in terms of what properties each hazard has when placed on a world. This is not WHERE the hazards are
 * placed, but rather what they actually are.
 * <p/>
 * Initially all hazards start out at some default value. Since hazard sprite sheets enumerate tile sized hazards and each 
 * column is a new hazard, the number of hazard available to the user is equal to the length-wise size of the hazard sheet 
 * divided by the pixels pertile (length-wise). Since these dialogs can only be launched with valid graphics resources users
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
		getContentPane().add(newHazardButton);
		
		hazardList.setVisible(true);
		setSize(500, 200);
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
		
		dialog.setVisible(true);
		
		return dialog.model;
	}
	
}
