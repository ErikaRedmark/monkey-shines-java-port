package org.erikaredmark.monkeyshines.editor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.erikaredmark.monkeyshines.editor.model.Template;
import org.erikaredmark.monkeyshines.editor.model.TemplateUtils;
import org.erikaredmark.monkeyshines.menu.MenuUtils;
import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.erikaredmark.util.swing.layout.WrapLayout;

/**
 * 
 * Displays a collection of templates. Allows the user to select existing templates for a world, as well as add new ones or
 * remove some. A LevelDrawingCanvas reference is used to set the brush type to 'Template' when this panel is interacted with, and
 * provides the canvas with the currently active canvas
 * 
 * @author Erika Redmark
 *
 */
@SuppressWarnings("serial")
public final class TemplatePalette extends JPanel {
	
	/**
	 * 
	 * Initialises the palette against a primary canvas with a list of initial templates, typically taken from
	 * some save file. An empty list may be passed but never {@code null}
	 * 
	 * @param mainCanvas
	 * 		reference to the main canvas so this palette can set the brush types when interacted with
	 * 
	 * @param initialTemplates
	 * 		listing of the initial templates to display
	 * 
	 * @param rsrc
	 * 		world resource for rendering the templates
	 * 
	 */
	public TemplatePalette(final LevelDrawingCanvas mainCanvas, final List<Template> initialTemplates, final WorldResource rsrc) {
		
		this.setLayout(new BorderLayout() );
		JPanel templateViewer = new JPanel();
		templateViewer.setLayout(new WrapLayout(FlowLayout.LEFT, GRID_MARGIN_X, GRID_MARGIN_Y) );
		this.add(templateViewer, BorderLayout.CENTER);
		
		for (Template t : initialTemplates) {
			JButton templateButton = createTemplateButton(t, mainCanvas, rsrc);
			templateViewer.add(templateButton);
		}
		
		// Control: The ability to add new templates, edit templates, and delete templates.
		// TODO actual implementation
		JPanel controlViewer = new JPanel();
		controlViewer.setLayout(new FlowLayout() );
		this.add(controlViewer, BorderLayout.SOUTH);
		
		JButton addTemplate = new JButton("Add...");
		controlViewer.add(addTemplate);
		
		JButton editTemplate = new JButton("Edit");
		controlViewer.add(editTemplate);
		
		JButton removeTemplate = new JButton("Remove");
		controlViewer.add(removeTemplate);
		
	}
	
	private JButton createTemplateButton(final Template template, final LevelDrawingCanvas mainCanvas, final WorldResource rsrc) {
		JButton templateButton = new JButton(new ImageIcon(TemplateUtils.renderTemplate(template, rsrc) ) );
		MenuUtils.renderImageOnly(templateButton);
		MenuUtils.removeMargins(templateButton);
		templateButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent event) {
				// If we are in 'drawing' state, this sets the brush. Editing state means we want to modify the selected template.
				// TODO modify state
				mainCanvas.setTemplateBrush(template);
			}
		});
		return templateButton;
	}
	
	private static final int GRID_MARGIN_X = 14;
	private static final int GRID_MARGIN_Y = 14;
	
}
