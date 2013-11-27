package edu.nova.erikaredmark.monkeyshines.editor.dialog;

import javax.swing.JComponent;
import javax.swing.JDialog;

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
	
	private EditHazardsDialog(final EditHazardsModel model) {
		this.model = model;
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
	public static void launch(JComponent parent, WorldResource rsrc) {
		
	}
	
}
