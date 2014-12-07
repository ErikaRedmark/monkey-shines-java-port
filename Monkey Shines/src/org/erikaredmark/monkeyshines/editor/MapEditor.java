package org.erikaredmark.monkeyshines.editor;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.Point2D;
import org.erikaredmark.monkeyshines.TileMap;
import org.erikaredmark.monkeyshines.World;
import org.erikaredmark.monkeyshines.background.Background;
import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.erikaredmark.monkeyshines.tiles.TileType;
import org.erikaredmark.monkeyshines.tiles.TileTypes;
import org.erikaredmark.monkeyshines.tiles.CommonTile.StatelessTileType;

/**
 * 
 * Encapsulates the idea of editing a tile map. This ONLY handles the tile map and tiles; this does not handle sprites NOR
 * goodies (goodies are a world-level object, not a tile-level object)
 * <p/>
 * The editor allows the tilemap to be edited using a basic brush based system where brushes can be
 * set from the Palette object. This also handles drawing the tilemap along with an indicator (an image showing what will
 * be drawn) to a graphics context.
 * <p/>
 * This panel may not handle mouse events itself. The component containing this editor must handle mouse events and forward them
 * to the editor if it cannot handle them itself. That depends on construction parameters.
 * <p/>
 * Once created with a tilemap, the editor cannot be assigned a different tilemap. A new {@code MapEditor} instance would have
 * to be created
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
	 * @param autonomousMouseControl
	 * 		{@code true} to let this component handle its own events. The primary level editor will set this to {@code false}
	 * 		since it also must handle things beyond the basic tilemap
	 * 
	 */
	public MapEditor(final TileMap map, final Background background, final World world, final boolean autonomousMouseControl) {
		super();
		this.map = map;
		this.background = background;
		this.world = world;
		
		int sizeX = map.getColumnCount() * GameConstants.TILE_SIZE_X;
		int sizeY = map.getRowCount() * GameConstants.TILE_SIZE_Y;
		
		// Basically, this editor is ALWAYS a constant size based on the tilemap. Expanding and shrinking makes no sense
		// in this context.
		setMinimumSize(new Dimension(sizeX, sizeY) );
		setPreferredSize(new Dimension(sizeX, sizeY) );
//		setSize(new Dimension(sizeX, sizeY) );
		setMaximumSize(new Dimension(sizeX, sizeY) );
		
		// Optimisations
		setDoubleBuffered(true);
		
		updateTileIndicator();
		
		if (autonomousMouseControl) {
			// Since this is being autonomous, mouse changes cause repaints (since 
			// the mouse clicks will change the tiles, and mouse moves will change
			// the indicator position.
			addMouseListener(new MouseAdapter() {
				@Override public void mouseClicked(MouseEvent e) {
					MapEditor.this.mouseClicked(e.getX(), e.getY() );
					MapEditor.this.repaint();
				}
			});
			
			addMouseMotionListener(new MouseMotionListener() {
				@Override public void mouseMoved(MouseEvent e) {
					MapEditor.this.mouseMoved(e.getX(), e.getY() );
					MapEditor.this.repaint();
				}
				
				@Override public void mouseDragged(MouseEvent e) {
					MapEditor.this.mouseClicked(e.getX(), e.getY() );
					MapEditor.this.repaint();
				}
			});
		}
	}
	
	/**
	 * 
	 * Returns the backing tilemap to this editor. Changes to the returned tilemap WILL be reflected in the editor; this is
	 * not a copy.
	 * 
	 * @return
	 * 		tilemap for this editor.
	 * 
	 */
	public TileMap getTileMap() {
		return map;
	}
	
	/**
	 * 
	 * Returns the background used for the map editor. The returned object cannot be used to modify this object's background.
	 * 
	 * @return
	 * 		the tilemap background.
	 * 
	 */
	public Background getMapBackground() {
		return background;
	}
	
	/**
	 * 
	 * Returns the world that this editor was created with. Modifications to the returned world affect the same world
	 * object that backs this tilemap.
	 * 
	 * @return
	 * 		the world
	 * 
	 */
	public World getWorld() {
		return world;
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
	
	/** 
	 * 
	 * @return the id of the tile to be drawn by the current brush.
	 * 
	 */
	public int getTileId() { return currentId; }
	
	/**
	 * 
	 * @return the brush used for current painting
	 * 
	 */
	public TileBrush getCurrentBrush() { return currentBrush; }
	
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
		} else {
			g2d.setColor(Color.green);
			g2d.drawRect(snapX,
						 snapY, 
						 GameConstants.TILE_SIZE_X, GameConstants.TILE_SIZE_Y);
		}
	}

	public enum TileBrush {
		SOLIDS {
			@Override public void onClick(int pixelX, int pixelY, int id, World world, TileMap map) {
				TileType tile = TileTypes.solidFromId(id);
				map.setTileXY(pixelX / GameConstants.TILE_SIZE_X, pixelY / GameConstants.TILE_SIZE_Y, tile);
			}
		}, 
		THRUS {
			@Override public void onClick(int pixelX, int pixelY, int id, World world, TileMap map) {
				TileType tile = TileTypes.thruFromId(id);
				map.setTileXY(pixelX / GameConstants.TILE_SIZE_X, pixelY / GameConstants.TILE_SIZE_Y, tile);
			}
		},
		SCENES {
			@Override public void onClick(int pixelX, int pixelY, int id, World world, TileMap map) {
				TileType tile = TileTypes.sceneFromId(id);
				map.setTileXY(pixelX / GameConstants.TILE_SIZE_X, pixelY / GameConstants.TILE_SIZE_Y, tile);
			}
		},
		HAZARDS {
			@Override public void onClick(int pixelX, int pixelY, int id, World world, TileMap map) {
				TileType tile = TileTypes.hazardFromId(id, world);
				map.setTileXY(pixelX / GameConstants.TILE_SIZE_X, pixelY / GameConstants.TILE_SIZE_Y, tile);
			}
		},
		CONVEYERS_CLOCKWISE {
			@Override public void onClick(int pixelX, int pixelY, int id, World world, TileMap map) {
				TileType tile = TileTypes.clockwiseConveyerFromId(id, world);
				map.setTileXY(pixelX / GameConstants.TILE_SIZE_X, pixelY / GameConstants.TILE_SIZE_Y, tile);
			}
		}, 
		CONVEYERS_ANTI_CLOCKWISE {
			@Override public void onClick(int pixelX, int pixelY, int id, World world, TileMap map) {
				TileType tile = TileTypes.anticlockwiseConveyerFromId(id, world);
				map.setTileXY(pixelX / GameConstants.TILE_SIZE_X, pixelY / GameConstants.TILE_SIZE_Y, tile);
			}
		},
		COLLAPSIBLE {
			@Override public void onClick(int pixelX, int pixelY, int id, World world, TileMap map) {
				TileType tile = TileTypes.collapsibleFromId(id);
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
	 * <p/>
	 * If this editor was created with the ability to handle its own mouse events, this will automatically be called.
	 * 
	 * @param x
	 * 		location of mouse click, x position
	 * 
	 * @param y
	 * 		location of mouse click, y position
	 * 
	 */
	public void mouseClicked(int x, int y) {
		mousePosition.setX(x);
		mousePosition.setY(y);
		currentBrush.onClick(x, y, currentId, world, map);
	}
	
	/**
	 * 
	 * Parent must call this method in their mouseListener implementation to keep the indicator image up-to-date.
	 * <p/>
	 * If this editor was created with the ability to handle its own mouse events, this will automatically be called.
	 * 
	 * @param x
	 * 		location of mouse click, x position
	 * 
	 * @param y
	 * 		location of mouse click, y position
	 * 
	 */
	public void mouseMoved(int x, int y) {
		mousePosition.setX(x);
		mousePosition.setY(y);
	}
	
	/**
	 * 
	 * Converts the brush type here to the brush type in the map editor. The map editor only uses a smaller
	 * subset of tile brushes, so this method is in error if called with a paintbrush that isn't a tile brush.
	 * 
	 * @param t
	 * @return
	 */
	public static TileBrush paintbrushToTilebrush(PaintbrushType t) {
		switch(t) {
		case SOLIDS:  return TileBrush.SOLIDS;
		case THRUS:  return TileBrush.THRUS;
		case SCENES:  return TileBrush.SCENES;
		case CONVEYERS_CLOCKWISE:  return TileBrush.CONVEYERS_CLOCKWISE;
		case CONVEYERS_ANTI_CLOCKWISE:  return TileBrush.CONVEYERS_ANTI_CLOCKWISE;
		case COLLAPSIBLE:  return TileBrush.COLLAPSIBLE;
		case HAZARDS:  return TileBrush.HAZARDS;
		case ERASER_TILES:  return TileBrush.ERASER;
		default:  throw new IllegalArgumentException("Paintbrush type " + t + " not a valid tile brush");
		}
	}
	
	/**
	 * 
	 * Determines if the given paintbrush can be directly converted to tile brush.
	 * 
	 * @param t
	 * 
	 * @return
	 * 		{@code true} if the paintbrush can be a tile brush, {@code false} if otherwise
	 * 
	 */
	public static boolean isPaintbrushToTilebrush(PaintbrushType t) {
		switch(t) {
		case SOLIDS: // break omitted
		case THRUS: // break omitted
		case SCENES: // break omitted
		case CONVEYERS_CLOCKWISE: // break omitted
		case CONVEYERS_ANTI_CLOCKWISE: // break omitted
		case COLLAPSIBLE: // break omitted
		case HAZARDS: // break omitted
		case ERASER_TILES: // break omitted
			return true;
		default:
			return false;
		}
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
