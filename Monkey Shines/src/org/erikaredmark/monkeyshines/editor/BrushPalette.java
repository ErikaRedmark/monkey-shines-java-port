package org.erikaredmark.monkeyshines.editor;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JTabbedPane;

import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.editor.LevelEditorMainCanvas.PaintbrushType;
import org.erikaredmark.monkeyshines.menu.MenuUtils;
import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.erikaredmark.monkeyshines.tiles.CommonTile.StatelessTileType;

/**
 * 
 * Represents the main palette for selecting tiles. The user clicks here, and then the palette is tied to
 * a {@code LevelEditorMainCanvas} that will be updated to use a different 'brush'.
 * 
 * @author Erika Redmark
 *
 */
@SuppressWarnings("serial")
public class BrushPalette extends JPanel {

	private final LevelEditorMainCanvas mainCanvas;

	/* 
	 * To save on object creation, each type of tile has one listener for each button. Each button has a different action
	 * command. The action command is the id of the tile. So the type of the tile goes to a type of listener, plus the id,
	 * gives the exact tile the user chose.
	 */
	private ActionListener solidTileListener = new ActionListener() {
		@Override public void actionPerformed(ActionEvent e) {
			int id = Integer.parseInt(e.getActionCommand() );
			mainCanvas.actionPlacingSolids();
			mainCanvas.setTileBrushAndId(PaintbrushType.SOLIDS, id);
		}
	};
	
	public BrushPalette(final LevelEditorMainCanvas mainCanvas, final WorldResource rsrc) {
		this.mainCanvas = mainCanvas;
		setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		JTabbedPane brushTypes = new JTabbedPane(JTabbedPane.TOP);
		add(brushTypes);
		
		// Create tabs for all types of tiles
		JPanel solidsPanel = new JPanel();
		brushTypes.add(solidsPanel);
		
		// Fill tabs with buttons that correspond to their type, where their index in the list (their
		// id) matches the graphic for the button, and the id that will be used when the button is clicked
		// when setting the canvas brush type.
		
		// SOLIDS
		solidsPanel.setLayout(new FlowLayout(FlowLayout.LEFT) );
		BufferedImage[] solids = 
			WorldResource.chop(GameConstants.TILE_SIZE_X, 
							   GameConstants.TILE_SIZE_Y, 
							   rsrc.getStatelessTileTypeSheet(StatelessTileType.SOLID) );
		
		for (int i = 0; i < solids.length; ++i) {
			BufferedImage solid = solids[i];
			JButton solidButton = new JButton(new ImageIcon(solid) );
			MenuUtils.renderImageOnly(solidButton);
			solidButton.setActionCommand(String.valueOf(i) );
			solidButton.addActionListener(solidTileListener);
		}
		
	}
}
