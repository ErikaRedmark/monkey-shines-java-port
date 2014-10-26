package org.erikaredmark.monkeyshines.editor;

/**
 * 
 * Represets an enumeration of brush types. Some brush types would otherwise not really share a common base object (such as sprites vs tiles) yet
 * nevertheless can be selected as a brush and may have different behaviours, hence they are enumerated separately.
 * 
 * @author Erika Redmark
 *
 */
public enum PaintbrushType { 
	SOLIDS, 
	THRUS, 
	SCENES, 
	HAZARDS, 
	PLACE_SPRITES,
	EDIT_SPRITES,
	GOODIES, 
	CONVEYERS_CLOCKWISE, 
	CONVEYERS_ANTI_CLOCKWISE,
	COLLAPSIBLE,
	ERASER_GOODIES,
	ERASER_TILES,
	ERASER_SPRITES; 
}



