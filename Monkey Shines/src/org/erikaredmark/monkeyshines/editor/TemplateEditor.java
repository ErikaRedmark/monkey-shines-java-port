package org.erikaredmark.monkeyshines.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicArrowButton;

import org.erikaredmark.monkeyshines.TileMap;
import org.erikaredmark.monkeyshines.World;
import org.erikaredmark.monkeyshines.background.SingleColorBackground;
import org.erikaredmark.monkeyshines.editor.model.Template;

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
public class TemplateEditor extends JDialog {

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
	 */
	public TemplateEditor(Template initial, World world) {
		
		currentTemplate =   initial != null
						  ? initial.mutableBuilder()
						  : new Template.Builder();
		
		TileMap map =   initial != null
				      ? initial.fitToTilemap()
				      : new TileMap(3, 3);
			
		final MapEditor internalEditor = new MapEditor(map, new SingleColorBackground(Color.BLACK), world);
		
		setLayout(new BorderLayout() );
		
		add(internalEditor, BorderLayout.CENTER);
		
		// Eight buttons, two per compass direction. One expands in that direction, one contracts in that direction.
		// These do not modify the template. They modify the size of the map in the editor
		
		// TOP
		JButton topExpand = new BasicArrowButton(SwingConstants.NORTH);
		topExpand.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				//internalEditor.expand(MapEditor.Direction.UP);
			}
		});
		
		JButton topShrink = new BasicArrowButton(SwingConstants.SOUTH);
		topShrink.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				//internalEditor.shrink(MapEditor.Direction.UP);
			}
		});
		
		// LEFT
		JButton leftExpand = new BasicArrowButton(SwingConstants.WEST);
		leftExpand.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				//internalEditor.expand(MapEditor.Direction.LEFT);
			}
		});
		
		JButton leftShrink = new BasicArrowButton(SwingConstants.EAST);
		leftShrink.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				//internalEditor.shrink(MapEditor.Direction.LEFT);
			}
		});
		
		// BOTTOM
		JButton bottomExpand = new BasicArrowButton(SwingConstants.SOUTH);
		bottomExpand.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
//				internalEditor.expand(MapEditor.Direction.BOTTOM);
			}
		});
		
		JButton bottomShrink = new BasicArrowButton(SwingConstants.NORTH);
		bottomShrink.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
//				internalEditor.shrink(MapEditor.Direction.BOTTOM);
			}
		});
		
		// RIGHT
		JButton rightExpand = new BasicArrowButton(SwingConstants.EAST);
		rightExpand.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
//				internalEditor.expand(MapEditor.Direction.RIGHT);
			}
		});
		
		JButton rightShrink = new BasicArrowButton(SwingConstants.EAST);
		rightShrink.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
//				internalEditor.shrink(MapEditor.Direction.RIGHT);
			}
		});
		
	}
	
	/**
	 * 
	 * Creates the template editor with no initial template (editor starts as 3x3 grid with nothing defined)
	 * 
	 * @param world
	 * 		the world the template is created for
	 * 
	 */
	public TemplateEditor(World world) {
		this(null, world);
	}
	
	
	private Template.Builder currentTemplate;
	
}
