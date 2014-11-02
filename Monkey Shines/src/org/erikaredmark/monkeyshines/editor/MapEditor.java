package org.erikaredmark.monkeyshines.editor;

import java.awt.AlphaComposite;
import java.awt.Dimension;
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
import org.erikaredmark.monkeyshines.bounds.IPoint2D;
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
 * <p/>
 * This panel does not handle mouse events itself. The component containing this editor must handle mouse events and forward them
 * to the editor if it cannot handle them itself. 
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
	 * <p/>
	 * 
	 * @param map
	 * 		tile map to edit
	 * 
	 * @param background
	 * 		initial background for tile map
	 * 
	 * @param world
	 * 		reference to the entire world
	 * 
	 */
	public MapEditor(final TileMap map, final Background background, final World world) {
		super();
		this.map = map;
		this.background = background;
		this.world = world;
		
		setMinimumSize(
			new Dimension(map.getColumnCount() * GameConstants.TILE_SIZE_X, 
				          map.getRowCount() * GameConstants.TILE_SIZE_Y) );
		
		setPreferredSize(
			new Dimension(map.getColumnCount() * GameConstants.TILE_SIZE_X, 
				          map.getRowCount() * GameConstants.TILE_SIZE_Y) );
		
		// Optimisations
		setDoubleBuffered(true);
		
		updateTileIndicator();
	}
	
	/**
	 * 
	 * Sets the current brush, and the id of the graphics resource specific to that brush.
	 * <p/>
	 * If set to 'NONE', id is not relevant.
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
		default :
			indicatorImage = null;
		}
		
		// If sheet was null, a green square will be drawn instead.
		if (sheet != null) {
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
	 * Updates the state of all tiles on this editor. It is up to client to decide if this should update and at
	 * what speed.
	 * 
	 */
	public void update() {
		map.update();
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


		drawTileIndicator(g2d);
		// BELOW: Additional overlays that are not part of the actual world
	}
	
	/**
	 * Paint an indicator to the current tile location. This depends on the 'paintbrush' selected.
	 * No tile indicator will be drawn if null. In that case, indicator depends on the higher-level
	 * editor to draw.
	 * 
	 */
	private void drawTileIndicator(Graphics2D g2d) {
		int snapX = EditorMouseUtils.snapMouseX(mousePosition.x() );
		int snapY = EditorMouseUtils.snapMouseY(mousePosition.y() );
		if (indicatorImage != null) {
			g2d.drawImage(indicatorImage, 
						  snapX, snapY,
						  snapX + GameConstants.TILE_SIZE_X, snapY + GameConstants.TILE_SIZE_Y, 
						  0, 0, 
						  indicatorImage.getWidth(), indicatorImage.getHeight(), 
						  null);
		}
	}

	
	public enum TileBrush {
		SOLIDS {
			@Override public void onClick(int pixelX, int pixelY, int id, World world, TileMap map) {
				CommonTile tile = CommonTile.of(id, StatelessTileType.SOLID);
				map.setTileXY(pixelX / GameConstants.TILE_SIZE_X, pixelY / GameConstants.TILE_SIZE_Y, tile);
			}
		}, 
		THRUS {
			@Override public void onClick(int pixelX, int pixelY, int id, World world, TileMap map) {
				CommonTile tile = CommonTile.of(id, StatelessTileType.THRU);
				map.setTileXY(pixelX / GameConstants.TILE_SIZE_X, pixelY / GameConstants.TILE_SIZE_Y, tile);
			}
		},
		SCENES {
			@Override public void onClick(int pixelX, int pixelY, int id, World world, TileMap map) {
				CommonTile tile = CommonTile.of(id, StatelessTileType.SCENE);
				map.setTileXY(pixelX / GameConstants.TILE_SIZE_X, pixelY / GameConstants.TILE_SIZE_Y, tile);
			}
		},
		HAZARDS {
			@Override public void onClick(int pixelX, int pixelY, int id, World world, TileMap map) {
				Hazard hazard = world.getHazards().get(id);
				HazardTile tile = HazardTile.forHazard(hazard);
				map.setTileXY(pixelX / GameConstants.TILE_SIZE_X, pixelY / GameConstants.TILE_SIZE_Y, tile);
			}
		},
		CONVEYERS_CLOCKWISE {
			@Override public void onClick(int pixelX, int pixelY, int id, World world, TileMap map) {
				Conveyer conveyer = world.getConveyers().get(id * 2);
				ConveyerTile tile = new ConveyerTile(conveyer);
				map.setTileXY(pixelX / GameConstants.TILE_SIZE_X, pixelY / GameConstants.TILE_SIZE_Y, tile);
			}
		}, 
		CONVEYERS_ANTI_CLOCKWISE {
			@Override public void onClick(int pixelX, int pixelY, int id, World world, TileMap map) {
				Conveyer conveyer = world.getConveyers().get( (id * 2) + 1);
				ConveyerTile tile = new ConveyerTile(conveyer);
				map.setTileXY(pixelX / GameConstants.TILE_SIZE_X, pixelY / GameConstants.TILE_SIZE_Y, tile);
			}
		},
		COLLAPSIBLE {
			@Override public void onClick(int pixelX, int pixelY, int id, World world, TileMap map) {
				CollapsibleTile tile = new CollapsibleTile(id);
				map.setTileXY(pixelX / GameConstants.TILE_SIZE_X, pixelY / GameConstants.TILE_SIZE_Y, tile);
			}
		},
		// ERASER is only for tiles. Goodies and Sprites are not included.
		ERASER {
			@Override public void onClick(int pixelX, int pixelY, int id, World world, TileMap map) {
				map.eraseTileXY(pixelX / GameConstants.TILE_SIZE_X, pixelY / GameConstants.TILE_SIZE_Y);
			}
		},
		// Means nothing happens on click. This is useful for if a higher level editor needs to do something on click that a basic
		// map editor cannot, such as setting sprites or goodies.
		NONE {
			@Override public void onClick(int pixelX, int pixelY, int id, World world, TileMap map) { }
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
		public abstract void onClick(int pixelX, int pixelY, int id, World world, TileMap map);
	}
	
	/**
	 * 
	 * Parent must call this method to indicate a mouse click that the editor should handle (such as when the parent
	 * knows it has set the current brush properly and nothing non-map-editor related, like Sprites, are supposed to be
	 * handled by the click)
	 * <p/>
	 * This should also be called for mouse drags.
	 * 
	 * @param e
	 * 		location of mouse click
	 * 
	 */
	public void mouseClicked(IPoint2D e) {
		mousePosition.setX(e.x() );
		mousePosition.setY(e.y() );
		currentBrush.onClick(e.x(), e.y(), currentId, world, map);
	}
	
	/**
	 * 
	 * Parent must call this method in their mouseListener implementation to keep the indicator image up-to-date.
	 * 
	 * @param e
	 * 		location of mouseClick
	 * 
	 */
	public void mouseMoved(IPoint2D e) {
		mousePosition.setX(e.x() );
		mousePosition.setY(e.y() );
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
	private Point2D mousePosition = Point2D.of(0, 0);
}
