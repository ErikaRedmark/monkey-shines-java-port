package org.erikaredmark.monkeyshines.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicArrowButton;

import org.erikaredmark.monkeyshines.TileMap;
import org.erikaredmark.monkeyshines.TileMap.Direction;
import org.erikaredmark.monkeyshines.World;
import org.erikaredmark.monkeyshines.background.Background;
import org.erikaredmark.monkeyshines.background.SingleColorBackground;
import org.erikaredmark.monkeyshines.editor.model.Template;

import com.google.common.base.Function;

/**
 * 
 * Graphical interface for modifying a single template. Accepts brush updates from {@code BrushPalette} and can be re-sized
 * dynamically. Accepts an initial template for construction (or none for a default 3x3 window) and when exited will return
 * the new template that has been created.
 * 
 * @author Erika Redmark
 *
 */
@SuppressWarnings("serial")
public class TemplateEditor extends JPanel {

	/**
	 * 
	 * Creates the template editor for the given template.
	 * 
	 * @param initial
	 * 		the template for initial creation
	 * 
	 * @param world
	 * 		the world the template is created for
	 * 
	 * @param saveAction
	 * 		called when the template is 'saved'. Passes both the original template and the template that was saved. The original
	 * 		can be used to determine which template to 'replace' for edit operations. It is possible for base template to be null
	 * 		if this editor was in a 'new template' state and not a 'modifying template' state.
	 * 
	 */
	public TemplateEditor(Template initial, World world, final Function<TemplatePair, Void> saveAction) {
		
		// Okay to be null
		baseTemplate = initial;
		
		TileMap map =   initial != null
				      ? initial.fitToTilemap()
				      : new TileMap(3, 3);
				      
		// Template editor main part: top. The save will take up a special bottom part
		setLayout(new BorderLayout() );
		primaryEditor = new JPanel(new BorderLayout() );
		add(primaryEditor, BorderLayout.NORTH);
			
		internalEditor = new MapEditor(map, new SingleColorBackground(Color.BLACK), world, true);
		
		primaryEditor.add(internalEditor, BorderLayout.CENTER);
		
		// Eight buttons, two per compass direction. One expands in that direction, one contracts in that direction.
		// These do not modify the template. They modify the size of the map in the editor
		
		// TOP
		JPanel topAll = new JPanel();
		topAll.setLayout(new BoxLayout(topAll, BoxLayout.LINE_AXIS) );
		JButton topExpand = new BasicArrowButton(SwingConstants.NORTH);
		topExpand.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				TileMap newMap = internalEditor.getTileMap().resize(1, Direction.NORTH);
				replaceMap(newMap);
			}
		});
		
		JButton topShrink = new BasicArrowButton(SwingConstants.SOUTH);
		topShrink.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				TileMap newMap = internalEditor.getTileMap().resize(-1, Direction.NORTH);
				replaceMap(newMap);
			}
		});
		
		topAll.add(topExpand);
		topAll.add(topShrink);
		
		// LEFT
		JPanel leftAll = new JPanel();
		leftAll.setLayout(new BoxLayout(leftAll, BoxLayout.PAGE_AXIS) );
		JButton leftExpand = new BasicArrowButton(SwingConstants.WEST);
		leftExpand.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				TileMap newMap = internalEditor.getTileMap().resize(1, Direction.WEST);
				replaceMap(newMap);
			}
		});
		
		JButton leftShrink = new BasicArrowButton(SwingConstants.EAST);
		leftShrink.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				TileMap newMap = internalEditor.getTileMap().resize(-1, Direction.WEST);
				replaceMap(newMap);
			}
		});
		
		leftAll.add(leftExpand);
		leftAll.add(leftShrink);
		
		// BOTTOM
		JPanel bottomAll = new JPanel();
		bottomAll.setLayout(new BoxLayout(bottomAll, BoxLayout.LINE_AXIS) );
		JButton bottomExpand = new BasicArrowButton(SwingConstants.SOUTH);
		bottomExpand.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				TileMap newMap = internalEditor.getTileMap().resize(1, Direction.SOUTH);
				replaceMap(newMap);
			}
		});
		
		JButton bottomShrink = new BasicArrowButton(SwingConstants.NORTH);
		bottomShrink.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				TileMap newMap = internalEditor.getTileMap().resize(-1, Direction.SOUTH);
				replaceMap(newMap);
			}
		});
		
		bottomAll.add(bottomExpand);
		bottomAll.add(bottomShrink);
		
		// RIGHT
		JPanel rightAll = new JPanel();
		rightAll.setLayout(new BoxLayout(rightAll, BoxLayout.PAGE_AXIS) );
		JButton rightExpand = new BasicArrowButton(SwingConstants.EAST);
		rightExpand.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				TileMap newMap = internalEditor.getTileMap().resize(1, Direction.EAST);
				replaceMap(newMap);
			}
		});
		
		JButton rightShrink = new BasicArrowButton(SwingConstants.WEST);
		rightShrink.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				TileMap newMap = internalEditor.getTileMap().resize(-1, Direction.EAST);
				replaceMap(newMap);
			}
		});
		
		rightAll.add(rightExpand);
		rightAll.add(rightShrink);
		
		primaryEditor.add(topAll, BorderLayout.NORTH);
		primaryEditor.add(leftAll, BorderLayout.WEST);
		primaryEditor.add(bottomAll, BorderLayout.SOUTH);
		primaryEditor.add(rightAll, BorderLayout.EAST);

		// Controls:
		saveButton = new JButton("");
		saveButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				saveAction.apply(new TemplatePair(baseTemplate, Template.fromTileMap(internalEditor.getTileMap() ) ) );
			}
		});
		add(saveButton);
		
		updateSaveButtonText();
	}
	
	/**
	 * 
	 * Updates the text of the save button to either show 'save' for new templates or 'overwrite' for existing ones.
	 * This must be called after the button is constructed, and subsequently each time 'baseTemplate' is modified.
	 * 
	 */
	private void updateSaveButtonText() {
		saveButton.setText(   baseTemplate != null
				            ? "Overwrite"
				            : "Save");
	}
	
	private void setBaseTemplate(Template t) {
		baseTemplate = t;
		updateSaveButtonText();
	}
	
	/**
	 * 
	 * Creates the template editor with no initial template (editor starts as 3x3 grid with nothing defined)
	 * 
	 * @param world
	 * 		the world the template is created for
	 * 
	 * @param saveAction
	 * 		called when the template is 'saved'. Passes both the original template and the template that was saved. The original
	 * 		can be used to determine which template to 'replace' for edit operations. It is possible for base template to be null
	 * 		if this editor was in a 'new template' state and not a 'modifying template' state.
	 * 
	 */
	public TemplateEditor(World world, Function<TemplatePair, Void> saveAction) {
		this(null, world, saveAction);
	}
	
	/**
	 * 
	 * Replaces the template currently being editing with the given template. Any changes to the 
	 * current template are discarded.
	 * 
	 * @param t
	 * 		the template to now be editing.
	 * 
	 */
	public void replaceTemplate(Template t) {
		setBaseTemplate(t);
		replaceMap(t.fitToTilemap() );
	}
	
	/**
	 * 
	 * Removes the current template from the editor, replacing the editor with the standard 3x3
	 * tilemap editor and no previous template.
	 * 
	 */
	public void clearTemplate() {
		setBaseTemplate(null);
		replaceMap(new TileMap(3, 3) );
	}
	
	
	
	/**
	 * 
	 * Attempts to set the brush for this editor's underlying map editor according to the paintbrush type and id. If that is not
	 * possible, this method does nothing.
	 * <p/>
	 * This method is intended to sync with the level editor when new brushes are chosen.
	 * 
	 * @param brush
	 */
	public void trySetTileIdAndBrush(PaintbrushType brush, int id) {
		if (MapEditor.isPaintbrushToTilebrush(brush) ) {
			internalEditor.setBrushAndId(MapEditor.paintbrushToTilebrush(brush), id);
		}
	}

	
	/**
	 * 
	 * Enumerates a pairing of the original template that an editor was created with (or 
	 * 
	 * @author Erika Redmark
	 *
	 */
	public static class TemplatePair {
		public final Template base;
		public final Template modified;
		
		public TemplatePair(final Template base, final Template modified) {
			this.base = base;
			this.modified = modified;
		}
	}
	
	
	/**
	 * 
	 * Replaces the current map editor with an editor for the new map, automatically handling removing the component
	 * from the view and replacing it, and then resizing all relevant components.
	 * 
	 * @param newMap
	 * 		the new map to be editing. Typically a resized version of the older one
	 * 
	 */
	private void replaceMap(final TileMap newMap) {
		Background background = internalEditor.getMapBackground();
		World world = internalEditor.getWorld();
		
		primaryEditor.remove(internalEditor);
		
		internalEditor = new MapEditor(newMap, background, world, true);
		
		primaryEditor.add(internalEditor, BorderLayout.CENTER);
		getParent().revalidate();
		getParent().repaint();
		
	}
	
	// Modified whenever tilemap is resized.
	private MapEditor internalEditor;
	// Sent during a save operation to the appropriate callbacks
	private Template baseTemplate;
	// Saved because the save button will be 'save' for new templates and 'overwrite' for editing existing ones.
	private final JButton saveButton;
	// When replacing a tileMap, it must be added to the primary editor only.
	private final JPanel primaryEditor;
	
}
