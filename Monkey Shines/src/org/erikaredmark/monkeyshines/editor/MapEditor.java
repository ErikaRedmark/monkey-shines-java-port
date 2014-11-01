package org.erikaredmark.monkeyshines.editor;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.erikaredmark.monkeyshines.Conveyer;
import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.Hazard;
import org.erikaredmark.monkeyshines.Point2D;
import org.erikaredmark.monkeyshines.TileMap;
import org.erikaredmark.monkeyshines.World;
import org.erikaredmark.monkeyshines.background.Background;
import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.erikaredmark.monkeyshines.tiles.CollapsibleTile;
import org.erikaredmark.monkeyshines.tiles.CommonTile;
import org.erikaredmark.monkeyshines.tiles.CommonTile.StatelessTileType;
import org.erikaredmark.monkeyshines.tiles.ConveyerTile;
import org.erikaredmark.monkeyshines.tiles.HazardTile;

/**
 * 
 * Encapsulates the idea of editing a tile map. This ONLY handles the tile map and tiles; this does not handle sprites NOR
 * goodies (goodies are a world-level object, not a tile-level object)
 * <p/>
 * The editor allows the tilemap to be edited using a basic brush based system where brushes can be
 * set from the Palette object. This also handles drawing the tilemap along with an indicator (an image showing what will
 * be drawn) to a graphics context.
 * 
 * @author Erika Redmark
 *
 */
@SuppressWarnings("serial")
public final class MapEditor extends JPanel {

	/**
	 * 
	 * Creates an editor for the given tilemap, background and world. The world is indirectly required for certain drawing jobs, such as
	 * hazards, where dependence on the world as a whole.
	 * 
	 * @param map
	 */
	public MapEditor(final TileMap map, final Background background, final World world) {
		this.map = map;
		this.background = background;
		this.world = world;
		updateTileIndicator();
	}
	
	/**
	 * 
	 * Sets the current brush, and the id of the graphics resource specific to that brush.
	 * <p/>
	 * Valid brushtypes are {@code SOLIDS, THRUS, SCENES, HAZARDS, 
	 * 
	 * @param brush
	 * @param id
	 */
	public void setBrushAndId(final TileBrush brush, final int id) {
		this.currentBrush = brush;
		this.currentId = id;
		updateTileIndicator();
	}
	
	private void updateTileIndicator() {
		WorldResource rsrc = world.getResource();
		
		BufferedImage sheet = null;
		int srcX = 0;
		int srcY = 0;
		switch(currentBrush) {
		case SOLIDS:
			sheet = rsrc.getStatelessTileTypeSheet(StatelessTileType.SOLID);
			srcX = computeSrcX(currentId, sheet);
			srcY = computeSrcY(currentId, sheet);
			break;
		case THRUS:
			sheet = rsrc.getStatelessTileTypeSheet(StatelessTileType.THRU);
			srcX = computeSrcX(currentId, sheet);
			srcY = computeSrcY(currentId, sheet);
			break;
		case SCENES:
			sheet = rsrc.getStatelessTileTypeSheet(StatelessTileType.SCENE);
			srcX = computeSrcX(currentId, sheet);
			srcY = computeSrcY(currentId, sheet);
			break;
		case COLLAPSIBLE:
			sheet = rsrc.getCollapsingSheet();
			srcX = 0;
			srcY = currentId * GameConstants.TILE_SIZE_Y;
			break;
		case CONVEYERS_CLOCKWISE:
			sheet = rsrc.getConveyerSheet();
			srcX = 0;
			srcY = currentId * (GameConstants.TILE_SIZE_Y * 2);
			break;
		case CONVEYERS_ANTI_CLOCKWISE:
			sheet = rsrc.getConveyerSheet();
			srcX = 0;
			srcY = (currentId * (GameConstants.TILE_SIZE_Y * 2) ) + 20;
			break;
		case HAZARDS:
			sheet = rsrc.getHazardSheet();
			srcX = currentId * (GameConstants.TILE_SIZE_X);
			srcY = 0;
			break;
		default:
			indicatorImage = null;
		}
		
		indicatorImage = new BufferedImage(GameConstants.TILE_SIZE_X, GameConstants.TILE_SIZE_Y, sheet.getType() );
		Graphics2D g = indicatorImage.createGraphics();
		try {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f) );
			g.drawImage(sheet, 
					    0, 0, 
					    GameConstants.TILE_SIZE_X, GameConstants.TILE_SIZE_Y, 
					    srcX, srcY, 
					    srcX + GameConstants.TILE_SIZE_X, srcY + GameConstants.TILE_SIZE_Y, 
					    null);
		} finally {
			g.dispose();
		}
	}
	
	private static int computeSrcX(int id, BufferedImage sheet) {
		return (id * GameConstants.TILE_SIZE_X) % (sheet.getWidth() );
	}
	
	// Resolves the id based on the width/height of sheet to the x location of the top-left coordinate to draw the tile.
	private static int computeSrcY(int id, BufferedImage sheet) {
		return (id / (sheet.getWidth() / GameConstants.TILE_SIZE_X) ) * (GameConstants.TILE_SIZE_Y);
	}
	
	/**
	 * 
	 * Changes the currently displayed background to the new one.
	 * 
	 * @param b
	 * 		new background
	 * 
	 */
	public void changeBackground(Background b) {
		this.background = b;
	}
	
	/**
	 * 
	 * Paints the tilemap along with editor specfic UI elements, such as the tile indicator.
	 * 
	 * @param g
	 * 
	 */
	@Override public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		
		background.draw(g2d);
		map.paint(g2d, world.getResource() );

		// Paint an indicator to the current tile location. This depends on the 'paintbrush' selected.
		drawTileIndicator(g2d);
		// BELOW: Additional overlays that are not part of the actual world
	}
	
	private void drawTileIndicator(Graphics2D g2d) {
		int snapX = snapMouseX(mousePosition.x() );
		int snapY = snapMouseY(mousePosition.y() );
		if (indicatorImage == null) {
			g2d.setColor(Color.green);
			g2d.drawRect(snapX,
						 snapY, 
						 GameConstants.TILE_SIZE_X, GameConstants.TILE_SIZE_Y);
		} else {
			g2d.drawImage(indicatorImage, 
						  snapX, snapY,
						  snapX + GameConstants.TILE_SIZE_X, snapY + GameConstants.TILE_SIZE_Y, 
						  0, 0, 
						  indicatorImage.getWidth(), indicatorImage.getHeight(), 
						  null);
		}
	}
	
	/*
	 * Snaps the mouse position to the top-left corner of whatever tile it is currently in. This is intended mostly for overlay drawing that needs
	 * to start at that position.
	 */
	public int snapMouseX(final int X) {
		int takeAwayX = X % GameConstants.TILE_SIZE_X;
		return X - takeAwayX;
	}
	
	public int snapMouseY(final int Y) {
		int takeAwayY = Y % GameConstants.TILE_SIZE_Y;
		return Y - takeAwayY;
	}
	
	public enum TileBrush {
		SOLIDS {
			@Override public void onClick(int pixelX, int pixelY, TileBrush brush, int id, World world, TileMap map) {
				CommonTile tile = CommonTile.of(id, StatelessTileType.SOLID);
				map.setTileXY(pixelX / GameConstants.TILE_SIZE_X, pixelY / GameConstants.TILE_SIZE_Y, tile);
			}
		}, 
		THRUS {
			@Override public void onClick(int pixelX, int pixelY, TileBrush brush, int id, World world, TileMap map) {
				CommonTile tile = CommonTile.of(id, StatelessTileType.THRU);
				map.setTileXY(pixelX / GameConstants.TILE_SIZE_X, pixelY / GameConstants.TILE_SIZE_Y, tile);
			}
		},
		SCENES {
			@Override public void onClick(int pixelX, int pixelY, TileBrush brush, int id, World world, TileMap map) {
				CommonTile tile = CommonTile.of(id, StatelessTileType.SCENE);
				map.setTileXY(pixelX / GameConstants.TILE_SIZE_X, pixelY / GameConstants.TILE_SIZE_Y, tile);
			}
		},
		HAZARDS {
			@Override public void onClick(int pixelX, int pixelY, TileBrush brush, int id, World world, TileMap map) {
				Hazard hazard = world.getHazards().get(id);
				HazardTile tile = HazardTile.forHazard(hazard);
				map.setTileXY(pixelX / GameConstants.TILE_SIZE_X, pixelY / GameConstants.TILE_SIZE_Y, tile);
			}
		},
		CONVEYERS_CLOCKWISE {
			@Override public void onClick(int pixelX, int pixelY, TileBrush brush, int id, World world, TileMap map) {
				Conveyer conveyer = world.getConveyers().get(id * 2);
				ConveyerTile tile = new ConveyerTile(conveyer);
				map.setTileXY(pixelX / GameConstants.TILE_SIZE_X, pixelY / GameConstants.TILE_SIZE_Y, tile);
			}
		}, 
		CONVEYERS_ANTI_CLOCKWISE {
			@Override public void onClick(int pixelX, int pixelY, TileBrush brush, int id, World world, TileMap map) {
				Conveyer conveyer = world.getConveyers().get( (id * 2) + 1);
				ConveyerTile tile = new ConveyerTile(conveyer);
				map.setTileXY(pixelX / GameConstants.TILE_SIZE_X, pixelY / GameConstants.TILE_SIZE_Y, tile);
			}
		},
		COLLAPSIBLE {
			@Override public void onClick(int pixelX, int pixelY, TileBrush brush, int id, World world, TileMap map) {
				CollapsibleTile tile = new CollapsibleTile(id);
				map.setTileXY(pixelX / GameConstants.TILE_SIZE_X, pixelY / GameConstants.TILE_SIZE_Y, tile);
			}
		},
		// ERASER is only for tiles. Goodies and Sprites are not included.
		ERASER {
			@Override public void onClick(int pixelX, int pixelY, TileBrush brush, int id, World world, TileMap map) {
				map.eraseTileXY(pixelX / GameConstants.TILE_SIZE_X, pixelY / GameConstants.TILE_SIZE_Y);
			}
		};
		
		/**
		 * 
		 * Performs the actual drawing of this tile entity to a specified pixel location with the given brush, id, and graphics,
		 * to the given tilemap. Tiles that contain state, such as Hazards, are always freshly created. 
		 * 
		 * @param pixelX
		 * 		pixel location x
		 * 
		 * @param pixelY
		 * 		pixel location y
		 * 
		 * @param brush
		 * 		brush type
		 * 
		 * @param id
		 * 		id of the graphics resource to use for the given brush. No bounds checking is done; it is up to client to ensure value never goes
		 * 		beyond the graphics resource indicated by the brush
		 * 
		 * @param world
		 * 		the world the tilemap is part of. All tilemaps must be part of some world, especially for special tiles like Hazards to be used properly.
		 * 
		 * @param map
		 * 		this parameter is being modified by the function. Drawing will take place on this object
		 * 
		 */
		public abstract void onClick(int pixelX, int pixelY, TileBrush brush, int id, World world, TileMap map);
	}
	
	// Only tile-type brushes are valid.
	private TileBrush currentBrush = TileBrush.SOLIDS;
	private int currentId = 0;
	
	private final TileMap map;
	private final World world;
	private Background background;
	
	// overlays on painting the tilemap where the brush mouse is positioned. Null is acceptable; means green square is drawn.
	private BufferedImage indicatorImage = null;
	// Constantly updated when mouse moves so tilemap can remember last position for overlay drawing purposes.
	private Point2D mousePosition;
}
