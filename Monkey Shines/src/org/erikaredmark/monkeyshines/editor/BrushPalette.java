package org.erikaredmark.monkeyshines.editor;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JTabbedPane;

import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.editor.LevelDrawingCanvas.PaintbrushType;
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

	private final LevelDrawingCanvas mainCanvas;

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
	
	public BrushPalette(final LevelDrawingCanvas mainCanvas, final WorldResource rsrc) {
		this.mainCanvas = mainCanvas;
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		
		final JTabbedPane brushTypes = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints brushTypesGbc = new GridBagConstraints();
		brushTypesGbc.gridx = 0;
		brushTypesGbc.gridy = 0;
		brushTypesGbc.weighty = 5;
		add(brushTypes, brushTypesGbc);
		
		// Create tabs for all types of tiles. Add to a list so that background modification can be done
		// globally
		// SOLIDS + THRUS + SCENES + CONVEYERS + COLLAPSIBLES + SPRITES + HAZARDS = 7.
		// * 2 since grid layout requires a flow layout nested to respect preferred sizes = 14
		final List<JPanel> palettePanels = new ArrayList<>(14);
		JPanel solidsPanel = new JPanel();
		palettePanels.add(solidsPanel);
		JPanel thrusPanel = new JPanel();
		palettePanels.add(thrusPanel);
		JPanel scenesPanel = new JPanel();
		palettePanels.add(scenesPanel);
		
		// Fill tabs with buttons that correspond to their type, where their index in the list (their
		// id) matches the graphic for the button, and the id that will be used when the button is clicked
		// when setting the canvas brush type.
		
		// SOLIDS, THRUS, and SCENES
		palettePanels.add(initialiseBasicTilePanel(solidsPanel, brushTypes, StatelessTileType.SOLID, solidTileListener, rsrc) );
		palettePanels.add(initialiseBasicTilePanel(thrusPanel, brushTypes, StatelessTileType.THRU, thruTileListener, rsrc) );
		palettePanels.add(initialiseBasicTilePanel(scenesPanel, brushTypes, StatelessTileType.SCENE, sceneTileListener, rsrc) );
		
		// Allow background colour to invert to black... this is for transparent tiles that look horrible on white (such as
		// cobwebs or any tiles predominantly white) to be visible easily.
		JButton invert = new JButton("Invert Background");
		invert.addActionListener(new InversionListener(palettePanels) );
		GridBagConstraints invertGbc = new GridBagConstraints();
		invertGbc.gridx = 0;
		invertGbc.gridy = 1;
		invertGbc.weighty = 1;
		add(invert, invertGbc);

	}
	
	// Common code for initialising SOLIDS, THRUS, and SCENES Only
	// Returns the direct panel the buttons are in, for inversion colour routines. The panel returned is not the same as the panel passed, due to
	// nesting requirements
	private JPanel initialiseBasicTilePanel(JPanel panel, JTabbedPane brushTypes, StatelessTileType type, ActionListener listener, WorldResource rsrc) {
		BufferedImage[] tiles =
			WorldResource.chop(GameConstants.TILE_SIZE_X,
							   GameConstants.TILE_SIZE_Y,
							   rsrc.getStatelessTileTypeSheet(type) );
		
		// each row has 6 columns
		int rows = tiles.length / 6;
		
		// Nest a grid layout inside another flow layout: force it to respect the preferred size of the
		// buttons. otherwise, if Thrus has more tiles than solids, for example, the tabs do not display
		// everything at the same size.
		panel.setLayout(new FlowLayout(FlowLayout.LEFT) );
		JPanel actualGrid = new JPanel();
		actualGrid.setLayout(new GridLayout(rows, 6, 4, 4) );
		panel.add(actualGrid);
		final JScrollPane typeScroller = new JScrollPane(panel);
		
		for (int i = 0; i < tiles.length; ++i) {
			BufferedImage tile = tiles[i];
			JButton tileButton = new JButton(new ImageIcon(tile) );
			MenuUtils.renderImageOnly(tileButton);
			MenuUtils.removeMargins(tileButton);
			tileButton.setActionCommand(String.valueOf(i) );
			tileButton.addActionListener(listener);
			actualGrid.add(tileButton);
		}
		
		// Add the panel to the tabbed pane. The icon for the tab will be the first tile for the sheet.
		brushTypes.addTab("", new ImageIcon(tiles[0]), typeScroller);
		
		return actualGrid;
	}
	
	/**
	 * 
	 * Switches the passed panels between their original colour and black when activated
	 * 
	 * @author Erika Redmark
	 *
	 */
	private final class InversionListener implements ActionListener {
		
		private InversionListener(final List<JPanel> panels) {
			assert !(panels.isEmpty() ) : "Number of panels is predefined; cannot work with empty collection in this method";
			inversionPanels = Collections.unmodifiableList(panels);
			originalColor = panels.get(0).getBackground();
			nextColor = Color.BLACK;
		}
		
		@Override public void actionPerformed(ActionEvent e) {
			for (JPanel p : inversionPanels) {
				p.setBackground(nextColor);
			}
			
			nextColor =   nextColor.equals(Color.BLACK)
						? originalColor
						: Color.BLACK;
		}
		
		private final List<JPanel> inversionPanels;
		private final Color originalColor;
		// Stores the color to change to on next click.
		private Color nextColor;
	}
}
