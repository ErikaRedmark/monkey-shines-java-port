package org.erikaredmark.monkeyshines.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.border.EtchedBorder;

import org.erikaredmark.monkeyshines.World;
import org.erikaredmark.monkeyshines.editor.TemplateEditor.TemplatePair;
import org.erikaredmark.monkeyshines.editor.model.Template;
import org.erikaredmark.monkeyshines.editor.model.TemplateUtils;
import org.erikaredmark.monkeyshines.menu.MenuUtils;
import org.erikaredmark.util.swing.layout.WrapLayout;

import com.google.common.base.Function;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * 
 * Displays a collection of templates. Allows the user to select existing templates for a world, as well as add new ones or
 * remove some. A LevelDrawingCanvas reference is used to set the brush type to 'Template' when this panel is interacted with, and
 * provides the canvas with the currently active canvas
 * <p/>
 * To the side, an editor is available that allows existing templates to be modified from the palette. it is up to client code, however,
 * to actually 'save' this palette to a file.
 * <p/>
 * The order that the templates are displayed in the GUI (component order) is their logical ordering, which is basically whatever
 * comes first in the initial list. This control strives to ensure a logical ordering for the user and that editing existing templates
 * does not change the ordering.
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
	 * @param world
	 * 		world for rendering and editing the templates
	 * 
	 * @param templateSaveAction
	 * 		function that is called with a list of templates that the client should somehow save. Used when the palette
	 *		is modified by the user after creation
	 * 
	 */
	public TemplatePalette(final LevelDrawingCanvas mainCanvas, 
						   final List<Template> initialTemplates, 
						   final World world,
						   final Function<List<Template>, Void> templateSaveAction) {
		
		// We must store this info, as we can dynamically create new template buttons and should have this already available.
		this.world = world;
		this.mainCanvas = mainCanvas;
		
		this.setLayout(new GridBagLayout() );
		
		templateViewer = new JPanel();
		templateViewer.setLayout(new WrapLayout(FlowLayout.LEFT, GRID_MARGIN_X, GRID_MARGIN_Y) );
		
		for (Template t : initialTemplates) {
			addTemplate(t, false);
		}
		
		JScrollPane viewerScroller = new JScrollPane(templateViewer);
		// template viewer will take up 60% of the left side and whatever it can top/down
		GridBagConstraints viewerScrollerGbc = new GridBagConstraints();
		viewerScrollerGbc.gridx = 0;
		viewerScrollerGbc.gridy = 0;
		viewerScrollerGbc.gridwidth = 6;
		viewerScrollerGbc.gridheight = 1;
		viewerScrollerGbc.weightx = 2.0;
		viewerScrollerGbc.weighty = 4.0;
		viewerScrollerGbc.fill = GridBagConstraints.BOTH;
		viewerScroller.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED) );
		add(viewerScroller, viewerScrollerGbc);

		// Control: The ability to add new templates, edit templates, and delete templates.
		JPanel controlViewer = new JPanel();
		controlViewer.setLayout(new FlowLayout() );
		// Controls rest at the bottom left. Take the smallest space
		GridBagConstraints controlViewerGbc = new GridBagConstraints();
		controlViewerGbc.gridx = 0;
		controlViewerGbc.gridy = 1;
		controlViewerGbc.gridwidth = 6;
		controlViewerGbc.gridheight = 1;
		controlViewerGbc.weightx = 6.0;
		controlViewerGbc.weighty = 1.0;
		add(controlViewer, controlViewerGbc);
		
		JButton addTemplate = new JButton("New");
		addTemplate.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent event) {
				templateEditor.clearTemplate();
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
		
		JButton saveAll = new JButton("Save Templates");
		controlViewer.add(saveAll);
		
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
		
		saveAll.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent event) {
				// We generate the list of templates from how they are ordered on the GUI, which is consistent
				// with how they are ordered in XML and is kept consistent via proper ordering when replacing existing templates.
				List<Template> templatesToSave = new ArrayList<>();
				BiMap<JButton, Template> buttonToTemplate = templateToButton.inverse();
				Object treeLock = templateViewer.getTreeLock();
				synchronized (treeLock) {
					for (int i = 0; i < templateViewer.getComponentCount(); ++i) {
						Component button = templateViewer.getComponent(i);
						Template t = buttonToTemplate.get(button);
						if (t != null)  templatesToSave.add(t);
					}
				}
				
				templateSaveAction.apply(templatesToSave);
			}
		});
		
		// Finally, on the far right side of the palette is the editor for modifying templates.
		templateEditor = new TemplateEditor(
			world, 
			new Function<TemplatePair, Void>() {
				@Override public Void apply(TemplatePair pair) {
					// Check if the baseTemplate is null. If so, add the new one. Otherwise, replace.
					if (pair.base == null) {
						addTemplate(pair.modified, true);
					} else {
						replaceTemplate(pair.base, pair.modified);
					}
					
					return null;
				}
			});
		
		templateEditor.setBorder( BorderFactory.createLineBorder(Color.BLUE) );
		// template editor takes up entire right side 40%
		GridBagConstraints templateEditorGbc = new GridBagConstraints();
		templateEditorGbc.gridx = 6;
		templateEditorGbc.gridy = 0;
		templateEditorGbc.gridwidth = 4;
		templateEditorGbc.gridheight = 2;
		templateEditorGbc.weightx = 4.0;
		templateEditorGbc.weighty = 1.0;
		add(templateEditor, templateEditorGbc);
		
	}
	
	private JButton createTemplateButton(final Template template) {
		
		JButton templateButton = new JButton(new ImageIcon(TemplateUtils.renderTemplate(template, world.getResource() ) ) );
		MenuUtils.renderImageOnly(templateButton);
		MenuUtils.removeMargins(templateButton);
		templateButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent event) {
				switch (currentState) {
				case PLACING:	
					mainCanvas.setTemplateBrush(template);
					break;
				case EDITING:
					templateEditor.replaceTemplate(template);
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
	 * @param repaint
	 * 		if {@code true}, components are re-painted and layed out. Constructor will call this with false
	 * 		and other code will use true.
	 * 
	 * @return
	 * 		{@code true} if the template was added, {@code false} if a button for it already existed and it wasn't added
	 * 
	 */
	private boolean addTemplate(Template template, boolean repaint) {
		// Do not allow duplicate templates. if it already existed do not add a button for it.
		if (templateToButton.containsKey(template) )  return false;
		JButton templateButton = createTemplateButton(template);
		
		templateViewer.add(templateButton);
		templateToButton.put(template, templateButton);
		if (repaint) {
			getParent().revalidate();
			getParent().repaint();
		}
		
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
	private boolean removeTemplate(Template template) {
		if (templateToButton.containsKey(template) ) {
			JButton oldButton = templateToButton.get(template);
			templateViewer.remove(oldButton);
			templateToButton.remove(template);
			getParent().revalidate();
			getParent().repaint();
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 
	 * Replaces the given template with a new one. Unlike remove/add, this preserves the location in the GUI and list.
	 * 
	 * @param oldTemplate
	 * 		old template to replace
	 * 
	 * @param newTemplate
	 * 		new template to add
	 * 
	 * @return
	 * 		{@code true} if the template was replaced, {@code false} if otherwise.
	 * 
	 */
	private boolean replaceTemplate(Template oldTemplate, Template newTemplate) {
		if (templateToButton.containsKey(oldTemplate) ) {
			JButton oldButton = templateToButton.get(oldTemplate);
			// We must find the 'index' of this button so we can properly update the GUI.
			int index = -1;
			Object treeLock = templateViewer.getTreeLock();
			synchronized (treeLock) {
				for (int i = 0; i < templateViewer.getComponentCount(); ++i) {
					// Reference equality intended
					if (templateViewer.getComponent(i) == oldButton) {
						index = i;
						break;
					}
				}
			}
			
			templateViewer.remove(oldButton);
			JButton templateButton = createTemplateButton(newTemplate);
			
			templateViewer.add(templateButton, index);
			templateToButton.put(newTemplate, templateButton);
			getParent().revalidate();
			getParent().repaint();
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
	
	/**
	 * 
	 * Attempts to set the template brush for the template editor. Does nothing if the brush is incompatable
	 * 
	 * @param brush
	 * @param id
	 */
	public void trySetTileIdAndBrush(PaintbrushType brush, int id) {
		templateEditor.trySetTileIdAndBrush(brush, id);
	}
	
	private State currentState = State.PLACING;
	
	// Maps a template to the button controlling that template. Keeps track of all template buttons so that
	// the palette may be modified after construction with new/removed templates.
	private final BiMap<Template, JButton> templateToButton = HashBiMap.create();
	
	// Immutable state information
	private final LevelDrawingCanvas mainCanvas;
	private final World world;

	private final TemplateEditor templateEditor;
	private final JPanel templateViewer;
	
	private static final int GRID_MARGIN_X = 14;
	private static final int GRID_MARGIN_Y = 14;
	
}
