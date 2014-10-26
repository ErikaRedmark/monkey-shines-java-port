package org.erikaredmark.monkeyshines.editor;

import javax.swing.JPanel;

import org.erikaredmark.monkeyshines.resource.WorldResource;

/**
 * 
 * Represents a graphical editor for templates. A template editor is created with the set of templates for the given world,
 * and it allows one to edit said templates. It provides an up to 20x32 (size of each level) tilemap editor similar to the
 * basic level editor, and is edited using the same palette that the regular editor does (although obviously some things,
 * such as sprites, are ignored). Templates can be added, deleted, edited to the world. Additionally, this provides a way
 * to 'save' the template data to a preferences file for the editor for the given world.
 * <p/>
 * Templates can be used to draw on top of other templates.
 * <p/>
 * For the palette to communicate with the editor, it must register it to listen for callbacks to brush changes.
 * 
 * @author Erika Redmark
 *
 */
@SuppressWarnings("serial")
public final class TemplateEditor extends JPanel {

	public TemplateEditor(final WorldResource rsrc) {
		
	}
	
}
