package org.erikaredmark.monkeyshines.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
	 * 		called when the template is 'saved'. Passes the template that was saved. Typically
	 * 		this saves the template somewhere for future use and 'closes' this panel.
	 * 
	 */
	public TemplateEditor(Template initial, World world, Function<Template, Void> saveAction) {
		
		TileMap map =   initial != null
				      ? initial.fitToTilemap()
				      : new TileMap(3, 3);
			
		internalEditor = new MapEditor(map, new SingleColorBackground(Color.BLACK), world);
		
		setLayout(new BorderLayout() );
		
		add(internalEditor, BorderLayout.CENTER);
		
		// Eight buttons, two per compass direction. One expands in that direction, one contracts in that direction.
		// These do not modify the template. They modify the size of the map in the editor
		
		// TOP
		JPanel topAll = new JPanel(new BorderLayout() );
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
		
		topAll.add(topExpand, BorderLayout.WEST);
		topAll.add(topShrink, BorderLayout.EAST);
		
		// LEFT
		JPanel leftAll = new JPanel(new BorderLayout() );
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
		
		leftAll.add(leftExpand, BorderLayout.NORTH);
		leftAll.add(leftShrink, BorderLayout.SOUTH);
		
		// BOTTOM
		JPanel bottomAll = new JPanel(new BorderLayout() );
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
		
		bottomAll.add(bottomExpand, BorderLayout.WEST);
		bottomAll.add(bottomShrink, BorderLayout.EAST);
		
		// RIGHT
		JPanel rightAll = new JPanel(new BorderLayout() );
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
		
		rightAll.add(rightExpand, BorderLayout.NORTH);
		rightAll.add(rightShrink, BorderLayout.SOUTH);
		
		add(topAll);
		add(leftAll);
		add(bottomAll);
		add(rightAll);
		
	}
	
	/**
	 * 
	 * Creates the template editor with no initial template (editor starts as 3x3 grid with nothing defined)
	 * 
	 * @param world
	 * 		the world the template is created for
	 * 
	 * @param saveAction
	 * 		called when the template is 'saved'. Passes the template that was saved. Typically
	 * 		this saves the template somewhere for future use and 'closes' this panel.
	 * 
	 */
	public TemplateEditor(World world, Function<Template, Void> saveAction) {
		this(null, world, saveAction);
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
		
		remove(internalEditor);
		
		internalEditor = new MapEditor(newMap, background, world);
		
		add(internalEditor, BorderLayout.CENTER);
	}
	
	// Modified whenever tilemap is resized.
	private MapEditor internalEditor;
	
}
