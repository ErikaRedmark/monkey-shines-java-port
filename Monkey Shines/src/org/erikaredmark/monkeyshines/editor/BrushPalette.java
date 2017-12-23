package org.erikaredmark.monkeyshines.editor;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JTabbedPane;

import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.menu.MenuUtils;
import org.erikaredmark.monkeyshines.resource.AwtWorldGraphics;
import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.erikaredmark.monkeyshines.tiles.CommonTile.StatelessTileType;
import org.erikaredmark.util.swing.layout.WrapLayout;

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
	
	
	/**
	 * To save on object creation, each type of tile has one listener for each button. Each button has a different action
	 * command. The action command is the id of the tile. So the type of the tile goes to a type of listener, plus the id,
	 * gives the exact tile the user chose. They all derive the same id: Get the id from the action command, and then set
	 * the brush to some type.
	 */
	private class ChangeBrushListener implements ActionListener {
		private final PaintbrushType brush;
		
		private ChangeBrushListener(final PaintbrushType brush) {
			this.brush = brush;
		}
		
		@Override public void actionPerformed(ActionEvent e) {
			int id = Integer.parseInt(e.getActionCommand() );
			mainCanvas.setTileBrushAndId(brush, id);
		}
	}
	
	private final ChangeBrushListener SOLID_TILE_LISTENER = new ChangeBrushListener(PaintbrushType.SOLIDS);
	private final ChangeBrushListener THRU_TILE_LISTENER = new ChangeBrushListener(PaintbrushType.THRUS);
	private final ChangeBrushListener SCENE_TILE_LISTENER = new ChangeBrushListener(PaintbrushType.SCENES);
	private final ChangeBrushListener HAZARD_TILE_LISTENER = new ChangeBrushListener(PaintbrushType.HAZARDS);
	private final ChangeBrushListener CONVEYER_CLOCKWISE_LISTENER = new ChangeBrushListener(PaintbrushType.CONVEYERS_CLOCKWISE);
	private final ChangeBrushListener CONVEYER_ANTI_CLOCKWISE_LISTENER = new ChangeBrushListener(PaintbrushType.CONVEYERS_ANTI_CLOCKWISE);
	private final ChangeBrushListener COLLAPSIBLE_TILE_LISTENER = new ChangeBrushListener(PaintbrushType.COLLAPSIBLE);
	private final ChangeBrushListener GOODIE_LISTENER = new ChangeBrushListener(PaintbrushType.GOODIES);
	private final ChangeBrushListener SPRITE_LISTENER = new ChangeBrushListener(PaintbrushType.PLACE_SPRITES);
	private final ChangeBrushListener ERASER_TILE_LISTENER = new ChangeBrushListener(PaintbrushType.ERASER_TILES);
	private final ChangeBrushListener ERASER_SPRITE_LISTENER = new ChangeBrushListener(PaintbrushType.ERASER_SPRITES);
	private final ChangeBrushListener ERASER_GOODIES_LISTENER = new ChangeBrushListener(PaintbrushType.ERASER_GOODIES);
	private final ChangeBrushListener EDIT_SPRITE_LISTENER = new ChangeBrushListener(PaintbrushType.EDIT_SPRITES);
	
	public BrushPalette(final LevelEditor mainCanvas, final WorldResource rsrc) {
		this.mainCanvas = mainCanvas;
		
		// Some standard graphics regardless of the world. All drawn and set in constructor to proper places...
		// no need to save them as instance data in object.
		final BufferedImage eraserMain;
		final BufferedImage eraserTiles;
		final BufferedImage eraserSprites;
		final BufferedImage eraserGoodies;
		final BufferedImage spriteMain;
		final BufferedImage editSprite;
		// Overlayed on Conveyer belts to make it obvious which direction it is pointing.
		final BufferedImage leftArrow;
		final BufferedImage rightArrow;
		
		try {
			rightArrow = ImageIO.read(BrushPalette.class.getResourceAsStream("/resources/graphics/editor/rightArrow.png") );
			leftArrow = ImageIO.read(BrushPalette.class.getResourceAsStream("/resources/graphics/editor/leftArrow.png") );
			eraserMain = ImageIO.read(BrushPalette.class.getResourceAsStream("/resources/graphics/editor/eraserMain.png") );
			eraserTiles = ImageIO.read(BrushPalette.class.getResourceAsStream("/resources/graphics/editor/eraserTiles.png") );
			eraserSprites = ImageIO.read(BrushPalette.class.getResourceAsStream("/resources/graphics/editor/eraserSprites.png") );
			eraserGoodies = ImageIO.read(BrushPalette.class.getResourceAsStream("/resources/graphics/editor/eraserGoodies.png") );
			editSprite = ImageIO.read(BrushPalette.class.getResourceAsStream("/resources/graphics/editor/editSprite.png") );
			spriteMain = ImageIO.read(BrushPalette.class.getResourceAsStream("/resources/graphics/editor/spriteMain.png") );
		} catch (IOException e) {
			throw new RuntimeException("Corrupted .jar: missing right/left arrow pngs: " + e.getMessage(), e);
		}
		
		setLayout(new BorderLayout() );
		
		final JTabbedPane brushTypes = new JTabbedPane(JTabbedPane.TOP);
		add(brushTypes, BorderLayout.CENTER);
		
		// Create tabs for all types of tiles. Add to a list so that background modification can be done
		// globally
		
		// SOLIDS + THRUS + SCENES + CONVEYERS + COLLAPSIBLES + SPRITES + HAZARDS + ERASER = 8.
		final List<JPanel> palettePanels = new ArrayList<>(8);
		
		// Sprites (creation of sprites. Removal is in ERASER tab)
		{
			JPanel spritesPanel = new JPanel();
			spritesPanel.setLayout(new WrapLayout(FlowLayout.LEFT, GRID_MARGIN_X, GRID_MARGIN_Y) );
			palettePanels.add(spritesPanel);
			final int spriteCount = rsrc.getSpritesCount();
			
			// First button is always the Edit Sprite button
			spritesPanel.add(createTileButton(editSprite, 0, EDIT_SPRITE_LISTENER) );
			for (int i = 0; i < spriteCount; ++i) {
				BufferedImage spriteSheet = rsrc.getSpritesheetFor(i);
				BufferedImage firstFrame = new BufferedImage(GameConstants.SPRITE_SIZE_X, GameConstants.SPRITE_SIZE_Y, spriteSheet.getType() );
				Graphics2D g2d = firstFrame.createGraphics();
				try {
					g2d.drawImage(spriteSheet, 
								  0, 0, 
								  firstFrame.getWidth(), firstFrame.getHeight(), 
								  0, 0, 
								  firstFrame.getWidth(), firstFrame.getHeight(), 
								  null);
				} finally {
					g2d.dispose();
				}
				
				spritesPanel.add(createTileButton(firstFrame, i, SPRITE_LISTENER) );
			}
			
			final JScrollPane typeScroller = new JScrollPane(spritesPanel);
			brushTypes.addTab("", new ImageIcon(spriteMain), typeScroller);
		}
		

		{

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
			initialiseBasicTilePanel(solidsPanel, brushTypes, StatelessTileType.SOLID, SOLID_TILE_LISTENER, rsrc);
			initialiseBasicTilePanel(thrusPanel, brushTypes, StatelessTileType.THRU, THRU_TILE_LISTENER, rsrc);
			initialiseBasicTilePanel(scenesPanel, brushTypes, StatelessTileType.SCENE, SCENE_TILE_LISTENER, rsrc);
		}
		
		// Hazards
		{
			JPanel hazardsPanel = new JPanel();
			hazardsPanel.setLayout(new WrapLayout(FlowLayout.LEFT, GRID_MARGIN_X, GRID_MARGIN_Y) );
			palettePanels.add(hazardsPanel);
			
			BufferedImage[] tiles = 
				WorldResource.chop(GameConstants.TILE_SIZE_X,
								   GameConstants.TILE_SIZE_Y,
								   rsrc.getHazardSheet() );
			
			// Only the first half are relevant
			for (int i = 0; i < (tiles.length / 2); ++i) {
				hazardsPanel.add(createTileButton(tiles[i], i, HAZARD_TILE_LISTENER) );
			}
			
			final JScrollPane typeScroller = new JScrollPane(hazardsPanel);
			brushTypes.addTab("", new ImageIcon(tiles[0]), typeScroller);
		}
		
		// Conveyers
		{
			JPanel conveyersPanel = new JPanel();
			conveyersPanel.setLayout(new WrapLayout(FlowLayout.LEFT, GRID_MARGIN_X, GRID_MARGIN_Y) );
			palettePanels.add(conveyersPanel);
			
			BufferedImage[] tiles =
				WorldResource.chop(GameConstants.TILE_SIZE_X,
								   GameConstants.TILE_SIZE_Y,
								   rsrc.getConveyerSheet() );

			for (int i = 0; i < tiles.length; i += 10) {
				// TWO buttons per conveyer. There is the clockwise then anti-clockwise one
				BufferedImage clockwise = tiles[i];
				BufferedImage antiClockwise = tiles[i + 5];
				
				Graphics2D gClockwise = clockwise.createGraphics();
				Graphics2D gAntiClockwise = antiClockwise.createGraphics();
				
				try {
					gClockwise.drawImage(rightArrow, 0, 0, null);
					gAntiClockwise.drawImage(leftArrow, 0, 0, null);
				} finally {
					gClockwise.dispose();
					gAntiClockwise.dispose();
				}
				
				conveyersPanel.add(createTileButton(clockwise, i / 10, CONVEYER_CLOCKWISE_LISTENER) );
				conveyersPanel.add(createTileButton(antiClockwise, i / 10, CONVEYER_ANTI_CLOCKWISE_LISTENER) );
			}
			
			final JScrollPane typeScroller = new JScrollPane(conveyersPanel);
			brushTypes.addTab("", new ImageIcon(tiles[0]), typeScroller);
		}
		
		// Collapsibles
		{
			JPanel collapsiblesPanel = new JPanel();
			collapsiblesPanel.setLayout(new WrapLayout(FlowLayout.LEFT, GRID_MARGIN_X, GRID_MARGIN_Y) );;
			palettePanels.add(collapsiblesPanel);
			
			BufferedImage[] tiles =
				WorldResource.chop(GameConstants.TILE_SIZE_X,
								   GameConstants.TILE_SIZE_Y,
								   rsrc.getCollapsingSheet() );
			
			for (int i = 0; i < tiles.length; i += 10) {
				collapsiblesPanel.add(createTileButton(tiles[i], i / 10, COLLAPSIBLE_TILE_LISTENER) );
			}
			
			final JScrollPane typeScroller = new JScrollPane(collapsiblesPanel);
			brushTypes.addTab("", new ImageIcon(tiles[0]), typeScroller);
		}
		
		// Goodies
		{
			// Similar techniques to hazards as the sprite sheets are similar (one row of everything, plus row of second animation frame)
			JPanel goodiesPanel = new JPanel();
			goodiesPanel.setLayout(new WrapLayout(FlowLayout.LEFT, GRID_MARGIN_X, GRID_MARGIN_Y) );
			palettePanels.add(goodiesPanel);
			
			BufferedImage[] tiles = 
				WorldResource.chop(GameConstants.TILE_SIZE_X,
								   GameConstants.TILE_SIZE_Y,
								   rsrc.getGoodieSheet() );
			
			// Only the first half are relevant
			for (int i = 0; i < (tiles.length / 2); ++i) {
				goodiesPanel.add(createTileButton(tiles[i], i, GOODIE_LISTENER) );
			}
			
			final JScrollPane typeScroller = new JScrollPane(goodiesPanel);
			brushTypes.addTab("", new ImageIcon(tiles[0]), typeScroller);
		}
		
		// Erasers
		{
			JPanel erasersPanel = new JPanel();
			erasersPanel.setLayout(new WrapLayout(FlowLayout.LEFT, GRID_MARGIN_X, GRID_MARGIN_Y) );
			palettePanels.add(erasersPanel);
			
			erasersPanel.add(createTileButton(eraserTiles, 0, ERASER_TILE_LISTENER) );
			erasersPanel.add(createTileButton(eraserGoodies, 0, ERASER_GOODIES_LISTENER) );
			erasersPanel.add(createTileButton(eraserSprites, 0, ERASER_SPRITE_LISTENER) );
			
			final JScrollPane typeScroller = new JScrollPane(erasersPanel);
			brushTypes.addTab("", new ImageIcon(eraserMain), typeScroller);
		}
		
		// Allow background colour to invert to black... this is for transparent tiles that look horrible on white (such as
		// cobwebs or any tiles predominantly white) to be visible easily.
		JButton invert = new JButton("Invert Background");
		invert.addActionListener(new InversionListener(palettePanels) );
		add(invert, BorderLayout.SOUTH);

	}
	
	// Common code for initialising SOLIDS, THRUS, and SCENES Only
	private void initialiseBasicTilePanel(JPanel panel, JTabbedPane brushTypes, StatelessTileType type, ActionListener listener, WorldResource rsrc) {
		BufferedImage[] tiles = AwtWorldGraphics.chop(
			GameConstants.TILE_SIZE_X,
			GameConstants.TILE_SIZE_Y,
			rsrc.getStatelessTileTypeSheet(type));
		
		panel.setLayout(new WrapLayout(FlowLayout.LEFT, GRID_MARGIN_X, GRID_MARGIN_Y) );
		final JScrollPane typeScroller = new JScrollPane(panel);
		
		for (int i = 0; i < tiles.length; ++i) {
			panel.add(createTileButton(tiles[i], i, listener) );
		}
		
		// Add the panel to the tabbed pane. The icon for the tab will be the first tile for the sheet.
		brushTypes.addTab("", new ImageIcon(tiles[0]), typeScroller);
	}
	
	// Creates a button that graphically represents some drawable thing in the palette, setting margins and listener properly
	// and forwarding the click action to the appropriate listener for the appropriate tile id.
	private JButton createTileButton(BufferedImage img, int id, ActionListener listener) {
		JButton tileButton = new JButton(new ImageIcon(img) );
		MenuUtils.renderImageOnly(tileButton);
		MenuUtils.removeMargins(tileButton);
		tileButton.setActionCommand(String.valueOf(id) );
		tileButton.addActionListener(listener);
		return tileButton;
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
	

	private final LevelEditor mainCanvas;
	
	private static final int GRID_MARGIN_X = 4;
	private static final int GRID_MARGIN_Y = 4;
}