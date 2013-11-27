package edu.nova.erikaredmark.monkeyshines.editor.dialog;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import edu.nova.erikaredmark.monkeyshines.Hazard;

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
	
}
