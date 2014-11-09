package org.erikaredmark.monkeyshines.editor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.erikaredmark.monkeyshines.editor.model.Template;
import org.erikaredmark.monkeyshines.editor.model.TemplateUtils;
import org.erikaredmark.monkeyshines.menu.MenuUtils;
import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.erikaredmark.util.swing.layout.WrapLayout;

import com.google.common.base.Function;

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
	 * @param openTemplateEditorRequest
	 * 		a function accepting a template, requesting that a template editor be opened for the given template. The template
	 * 		passed to this function MAY be null, indicating a new template, not modification of an existing one
	 * 
	 */
	public TemplatePalette(final LevelDrawingCanvas mainCanvas, 
						   final List<Template> initialTemplates, 
						   final WorldResource rsrc,
						   final Function<Template, Void> openTemplateEditorRequest) {
		
		// We must store this info, as we can dynamically create new template buttons and should have this already available.
		this.openTemplateEditorRequest = openTemplateEditorRequest;
		this.rsrc = rsrc;
		this.mainCanvas = mainCanvas;
		
		this.setLayout(new BorderLayout() );
		
		templateViewer = new JPanel();
		templateViewer.setLayout(new WrapLayout(FlowLayout.LEFT, GRID_MARGIN_X, GRID_MARGIN_Y) );
		this.add(templateViewer, BorderLayout.CENTER);
		
		for (Template t : initialTemplates) {
			addTemplate(t);
		}
		
		// Control: The ability to add new templates, edit templates, and delete templates.
		// TODO actual implementation
		JPanel controlViewer = new JPanel();
		controlViewer.setLayout(new FlowLayout() );
		this.add(controlViewer, BorderLayout.SOUTH);
		
		JButton addTemplate = new JButton("Add...");
		addTemplate.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent event) {
				openTemplateEditorRequest.apply(null);
			}
		});
		controlViewer.add(addTemplate);
		
		// Logic will require that only edit or remove may be toggled at any one time. Toggling one
		// enables or disables the other. There is no other way to change state in the palette.
		// Note that action listener is called AFTER state change, not BEFORE.
		final JToggleButton editTemplate = new JToggleButton("Edit");
		controlViewer.add(editTemplate);
		
		final JToggleButton removeTemplate = new JToggleButton("Remove");
		controlViewer.add(removeTemplate);
		
		editTemplate.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent event) {
				if (editTemplate.isSelected() ) {
					currentState = State.EDITING;
					removeTemplate.setEnabled(false);
				} else {
					currentState = State.PLACING;
					removeTemplate.setEnabled(true);
				}
			}
		});
		
		removeTemplate.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent event) {
				if (removeTemplate.isSelected() ) {
					currentState = State.DELETING;
					editTemplate.setEnabled(false);
				} else {
					currentState = State.PLACING;
					editTemplate.setEnabled(true);
				}
			}
		});
		
	}
	
	private JButton createTemplateButton(final Template template) {
		
		JButton templateButton = new JButton(new ImageIcon(TemplateUtils.renderTemplate(template, rsrc) ) );
		MenuUtils.renderImageOnly(templateButton);
		MenuUtils.removeMargins(templateButton);
		templateButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent event) {
				switch (currentState) {
				case PLACING:	
					mainCanvas.setTemplateBrush(template);
					break;
				case EDITING:
					openTemplateEditorRequest.apply(template);
					break;
				case DELETING:
					removeTemplate(template);
					break;
				}
			}
		});
		return templateButton;
	}
	
	/**
	 * 
	 * Adds the given template to the palette so it may be placed. The template is added to the end of the palette.
	 * <p/>
	 * This method does nothing if the palette already has a button for the given template
	 * 
	 * @param template
	 * 		new template to add to palette
	 * 
	 * @return
	 * 		{@code true} if the template was added, {@code false} if a button for it already existed and it wasn't added
	 * 
	 */
	public boolean addTemplate(Template template) {
		// Do not allow duplicate templates. if it already existed do not add a button for it.
		if (templateToButton.containsKey(template) )  return false;
		JButton templateButton = createTemplateButton(template);
		
		templateViewer.add(templateButton);
		templateToButton.put(template, templateButton);
		doLayout();
		repaint();
		
		return true;
	}
	
	/**
	 * 
	 * Removes the given template from the palette.
	 * 
	 * @param template
	 * 		template to remove.
	 * 
	 * @return
	 * 		{@code true} if the template was removed, {@code false} if it never existed and thus was not removed
	 * 
	 */
	public boolean removeTemplate(Template template) {
		if (templateToButton.containsKey(template) ) {
			JButton oldButton = templateToButton.get(template);
			templateViewer.remove(oldButton);
			templateToButton.remove(template);
			doLayout();
			repaint();
			return true;
		} else {
			return false;
		}
	}
	
	
	// Affects what the result of clicking on a template will be.
	private enum State {
		PLACING,
		EDITING,
		DELETING;
	}
	
	private State currentState = State.PLACING;
	
	// Maps a template to the button controlling that template. Keeps track of all template buttons so that
	// the palette may be modified after construction with new/removed templates.
	private final Map<Template, JButton> templateToButton = new HashMap<>();
	
	// Immutable state information
	private final LevelDrawingCanvas mainCanvas;
	private final WorldResource rsrc;
	private final Function<Template, Void> openTemplateEditorRequest;
	
	//
	private final JPanel templateViewer;
	
	private static final int GRID_MARGIN_X = 14;
	private static final int GRID_MARGIN_Y = 14;
	
}
