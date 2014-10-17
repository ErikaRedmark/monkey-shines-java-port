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
			mainCanvas.setTileBrushAndId(PaintbrushType.SOLIDS, id);
		}
	};
	
	private ActionListener thruTileListener = new ActionListener() {
		@Override public void actionPerformed(ActionEvent e) {
			int id = Integer.parseInt(e.getActionCommand() );
			mainCanvas.setTileBrushAndId(PaintbrushType.THRUS, id);
		}
	};
	
	private ActionListener sceneTileListener = new ActionListener() {
		@Override public void actionPerformed(ActionEvent e) {
			int id = Integer.parseInt(e.getActionCommand() );
			mainCanvas.setTileBrushAndId(PaintbrushType.SCENES, id);
		}
	};
	
	public BrushPalette(final LevelEditorMainCanvas mainCanvas, final WorldResource rsrc) {
		this.mainCanvas = mainCanvas;
		setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		JTabbedPane brushTypes = new JTabbedPane(JTabbedPane.TOP);
		add(brushTypes);
		
		// Create tabs for all types of tiles
		JPanel solidsPanel = new JPanel();
		JPanel thrusPanel = new JPanel();
		JPanel scenesPanel = new JPanel();
		
		// Fill tabs with buttons that correspond to their type, where their index in the list (their
		// id) matches the graphic for the button, and the id that will be used when the button is clicked
		// when setting the canvas brush type.
		
		// SOLIDS, THRUS, and SCENES
		initialiseBasicTilePanel(solidsPanel, brushTypes, StatelessTileType.SOLID, solidTileListener, rsrc);
		initialiseBasicTilePanel(thrusPanel, brushTypes, StatelessTileType.THRU, thruTileListener, rsrc);
		initialiseBasicTilePanel(scenesPanel, brushTypes, StatelessTileType.SCENE, sceneTileListener, rsrc);
	}
	
	// Common code for initialising SOLIDS, THRUS, and SCENES Only
	private void initialiseBasicTilePanel(JPanel panel, JTabbedPane brushTypes, StatelessTileType type, ActionListener listener, WorldResource rsrc) {
		panel.setLayout(new FlowLayout(FlowLayout.LEFT) );
		BufferedImage[] tiles =
			WorldResource.chop(GameConstants.TILE_SIZE_X,
							   GameConstants.TILE_SIZE_Y,
							   rsrc.getStatelessTileTypeSheet(type) );
		
		for (int i = 0; i < tiles.length; ++i) {
			BufferedImage tile = tiles[i];
			JButton tileButton = new JButton(new ImageIcon(tile) );
			MenuUtils.renderImageOnly(tileButton);
			tileButton.setActionCommand(String.valueOf(i) );
			tileButton.addActionListener(listener);
		}
		
		// Add the panel to the tabbed pane. The icon for the tab will be the first tile for the sheet.
		brushTypes.addTab("", new ImageIcon(tiles[0]), panel);
	}
}
