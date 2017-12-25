package org.erikaredmark.monkeyshines.editor.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.TileMap;
import org.erikaredmark.monkeyshines.editor.model.Template.TemplateTile;
import org.erikaredmark.monkeyshines.resource.AwtRenderer;
import org.erikaredmark.monkeyshines.resource.AwtWorldGraphics;
import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.erikaredmark.monkeyshines.tiles.CommonTile;
import org.erikaredmark.monkeyshines.tiles.CommonTile.StatelessTileType;

/**
 * 
 * Static utility class for methods involving templates that aren't techincally part of the logical makeup of
 * a template (such as drawing helper routines)
 * 
 * @author Erika Redmark
 *
 */
public final class TemplateUtils {
	/**
	 * 
	 * Renders the given template to an image for display purposes.
	 * 
	 * @param t
	 * 		template to render
	 * 
	 * @param rsrc
	 * 		world awt graphics for drawing
	 * 
	 * @return
	 * 		an image representing the template
	 * 
	 */
	public static BufferedImage renderTemplate(final Template t, final WorldResource rsrc) {
		// Currently, do no scaling. Just get a tilemap to fit and render that to the graphics
		AwtWorldGraphics awtGraphics = rsrc.getAwtGraphics();
		TileMap map = t.fitToTilemap();
		map.updateDrawInformation(rsrc);
		BufferedImage icon = 
			new BufferedImage(
				// the + 1 is for the last pixel to be the grid.
				(map.getColumnCount() * GameConstants.TILE_SIZE_X) + 1,
				(map.getRowCount() * GameConstants.TILE_SIZE_Y) + 1,
				// All the images should have the same time. Just grab the type from solids as a base.
				awtGraphics.getStatelessTileTypeSheet(StatelessTileType.SOLID).getType() );
		
		Graphics2D g2d = icon.createGraphics();
		try {
			AwtRenderer.paintTileMap(g2d, map, awtGraphics);
			// Special: Render template tiles that represent No tile. We need to graphically differentiate between
			// emptiness in the template vs. explicitly no tile.
			g2d.setColor(Color.PINK);
			for (TemplateTile tile : t.getTilesInTemplate() ) {
				if (tile.tile.equals(CommonTile.NONE) ) {
					g2d.fillRect(tile.col * GameConstants.TILE_SIZE_X, 
								 tile.row * GameConstants.TILE_SIZE_Y, 
								 GameConstants.TILE_SIZE_X, 
								 GameConstants.TILE_SIZE_Y);
				}
			}
			
			// Finally, draw a grid since the control will not have other standard drawing
			g2d.setColor(Color.BLUE);
			int bottom = map.getRowCount() * GameConstants.TILE_SIZE_Y;
			int right = map.getColumnCount() * GameConstants.TILE_SIZE_X;
			for (int x = 0; x < map.getColumnCount(); ++x ) {
				// Verticle lines
				int drawX = x * GameConstants.TILE_SIZE_X;
				g2d.drawLine(drawX, 0, 
						 	 drawX, bottom);
				for (int y = 0; y < map.getRowCount(); ++y) {
					// Horizontal Lines
					int drawY = y * GameConstants.TILE_SIZE_Y;
					g2d.drawLine(0, drawY,
								 right, drawY);
				}
			}
			// draw final lines
			// vert
			g2d.drawLine(right, 0,
						 right, bottom);
			
			g2d.drawLine(0, bottom,
						 right, bottom);
			
		} finally {
			g2d.dispose();
		}
		
		return icon;
	}
}
