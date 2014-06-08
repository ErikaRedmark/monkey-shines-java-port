package org.erikaredmark.monkeyshines.editor.dialog;

import java.util.ArrayList;
import java.util.List;

import org.erikaredmark.monkeyshines.Hazard;

import com.google.common.collect.ImmutableList;

public class EditHazardsModel {

	/* Handles both quantity and properties of hazards
	 */
	final List<Hazard> hazards;
	
	/**
	 * 
	 * Initialises the model with a default list of hazards. This list is copied to the model so the original can not
	 * be modified.
	 * 
	 * @param initialHazards
	 * 		list of initial hazards
	 * 
	 */
	EditHazardsModel(final List<Hazard> initialHazards) {
		this.hazards = new ArrayList<>(initialHazards);
	}
	
	/**
	 * 
	 * Returns an immutable copy of the hazards in this model
	 * 
	 * @return
	 * 		immutable list of hazards in model
	 * 
	 */
	public ImmutableList<Hazard> getHazards() {
		return ImmutableList.copyOf(hazards);
	}
	
	/**
	 * 
	 * Intended only for view to modify: returns the actual backing list for direct modification. Once published to
	 * other code and the view deconstructed this method should not be used again
	 * 
	 * @return
	 * 		mutable list of hazards in model. Changes to returned list affect this object's model.
	 * 
	 */
	List<Hazard> getMutableHazards() {
		return hazards;
	}
	
}
