package edu.nova.erikaredmark.monkeyshines.editor.dialog;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.SpringLayout;

import com.google.common.collect.ImmutableList;

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
		final SpringLayout layout = new SpringLayout();
		getContentPane().setLayout(layout);
		
		final HazardListModel hazardListModel = new HazardListModel();
		
		/* ------------------ Hazard list ------------------- */
		final JList<Hazard> hazardList = new JList<Hazard>(hazardListModel);
		// Attach to upper left of window and extend to bottom
		layout.putConstraint(SpringLayout.NORTH, hazardList, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.WEST, hazardList, 0, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.SOUTH, hazardList, 0, SpringLayout.SOUTH, this);
		getContentPane().add(hazardList);
		
		/* ------------------ New Hazard -------------------- */
		final JButton newHazardButton = new JButton(new AbstractAction("New Hazard") {
			@Override public void actionPerformed(ActionEvent e) {
				// The new hazard by default will assume the id of the currently selected hazard, or the next id if no
				// hazard is currently selected.
				
				
				//Hazard.newHazardTo(hazardListModel.getCurrentHazardList(), rsrc );
			}
		});
		
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
	
	private final class HazardListModel extends AbstractListModel<Hazard> {
		private static final long serialVersionUID = 1L;
		
		List<Hazard> hazards = new ArrayList<Hazard>(model.getHazards() ); 
		
		@Override public Hazard getElementAt(int index) {
			return hazards.get(index);
		}

		@Override public int getSize() {
			return hazards.size();
		}
		
		
		/**
		 * 
		 * Returns a reference to the underlying list this model is working with. The returned reference is mutable and
		 * will affect the display of the model, so be careful.
		 * 
		 * @return
		 * 		hazard list for this model
		 * 
		 */
		public List<Hazard> getCurrentHazardList() {
			return hazards;
		}
	}
	
}
