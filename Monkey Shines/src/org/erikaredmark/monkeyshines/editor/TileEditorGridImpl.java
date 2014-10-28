//package org.erikaredmark.monkeyshines.editor;
//
//import java.awt.AlphaComposite;
//import java.awt.Color;
//import java.awt.Graphics2D;
//import java.awt.image.BufferedImage;
//
//import javax.swing.JPanel;
//
//import org.erikaredmark.monkeyshines.GameConstants;
//import org.erikaredmark.monkeyshines.Goodie;
//import org.erikaredmark.monkeyshines.Hazard;
//import org.erikaredmark.monkeyshines.Tile;
//import org.erikaredmark.monkeyshines.World;
//import org.erikaredmark.monkeyshines.background.Background;
//import org.erikaredmark.monkeyshines.resource.WorldResource;
//import org.erikaredmark.monkeyshines.tiles.CommonTile;
//import org.erikaredmark.monkeyshines.tiles.TileType;
//import org.erikaredmark.monkeyshines.tiles.CommonTile.StatelessTileType;
//
//import com.google.common.base.Function;
//
//@SuppressWarnings("serial")
//public final class TileEditorGridImpl extends JPanel implements TileEditorGrid {
//
//	/**
//	 * 
//	 * Creates a tile map editor from the given world and resource pack, for the given number of rows and columns.
//	 * 
//	 * @param world
//	 * 
//	 * @param rsrc
//	 * 
//	 * @param rows
//	 * 		rows for the drawing context, cannot exceed 20.
//	 * 
//	 * @param cols
//	 * 		cols for the drawing context, cannot exceed 32.
//	 * 
//	 */
//	public TileEditorGridImpl(final World world, final WorldResource rsrc, int rows, int cols) {
//		this.world = world;
//		this.rsrc = rsrc;
//		this.tileMap = new Tile[rows][cols];
//	}
//	
//	/*
//	 * 
//	 * Changes the internal state of the editor from one state to the other, making any additional updates as needed.
//	 * 
//	 * @param newState
//	 * 
//	 */
//	private void changeState(EditorState newState) {
//		// Make no state changes if the state isn't actually changing, but do update the tile indicator
//		// as calls for same state change can still bring about changes to the current tile
//		if (currentState == newState) {
//			updateTileIndicator();
//			return;
//		}
//		
//		if (   newState == EditorState.EDITING_SPRITES
//			|| newState == EditorState.DELETING_SPRITES) {
//			// Set a condition for the game timer to stop animating sprites.
//			// Stopping the timer completely would look like a freeze, so we don't do that.
//			currentScreenEditor.stopAnimatingSprites();
//		} else {
//			// Transitioning out of editing sprites state always restores the sprite animation.
//			currentScreenEditor.startAnimatingSprites();
//		}
//		currentState = newState;
//		updateTileIndicator();
//	}
//	
//	/** Updates the image drawn by the tile indicator based on the brush type. The actual drawing takes place in drawTileIndicator. */
//	private void updateTileIndicator() {
//		assert currentState != EditorState.NO_WORLD_LOADED;
//		WorldResource rsrc = currentWorldEditor.getWorldResource();
//		if (   currentState == EditorState.PLACING_TILES
//			|| currentState == EditorState.PLACING_HAZARDS
//			|| currentState == EditorState.PLACING_GOODIES) {
//			BufferedImage sheet = null;
//			int srcX = 0;
//			int srcY = 0;
//			switch(currentTileType) {
//			case SOLIDS:
//				sheet = rsrc.getStatelessTileTypeSheet(StatelessTileType.SOLID);
//				srcX = computeSrcX(currentTileId, sheet);
//				srcY = computeSrcY(currentTileId, sheet);
//				break;
//			case THRUS:
//				sheet = rsrc.getStatelessTileTypeSheet(StatelessTileType.THRU);
//				srcX = computeSrcX(currentTileId, sheet);
//				srcY = computeSrcY(currentTileId, sheet);
//				break;
//			case SCENES:
//				sheet = rsrc.getStatelessTileTypeSheet(StatelessTileType.SCENE);
//				srcX = computeSrcX(currentTileId, sheet);
//				srcY = computeSrcY(currentTileId, sheet);
//				break;
//			case COLLAPSIBLE:
//				sheet = rsrc.getCollapsingSheet();
//				srcX = 0;
//				srcY = currentTileId * GameConstants.TILE_SIZE_Y;
//				break;
//			case CONVEYERS_CLOCKWISE:
//				sheet = rsrc.getConveyerSheet();
//				srcX = 0;
//				srcY = currentTileId * (GameConstants.TILE_SIZE_Y * 2);
//				break;
//			case CONVEYERS_ANTI_CLOCKWISE:
//				sheet = rsrc.getConveyerSheet();
//				srcX = 0;
//				srcY = (currentTileId * (GameConstants.TILE_SIZE_Y * 2) ) + 20;
//				break;
//			case GOODIES:
//				sheet = rsrc.getGoodieSheet();
//				srcX = currentGoodieType.getDrawX();
//				srcY = currentGoodieType.getDrawY();
//				break;
//			case HAZARDS:
//				sheet = rsrc.getHazardSheet();
//				srcX = currentTileId * (GameConstants.TILE_SIZE_X);
//				srcY = 0;
//				break;
//			case PLACE_SPRITES:
//				throw new RuntimeException("Cannot have a paintbrush of sprite during a placing tile state");
//			default:
//				throw new RuntimeException("Unknown tile type " + currentTileType);
//			}
//			
//			indicatorImage = new BufferedImage(GameConstants.TILE_SIZE_X, GameConstants.TILE_SIZE_Y, sheet.getType() );
//			Graphics2D g = indicatorImage.createGraphics();
//			try {
//				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f) );
//				g.drawImage(sheet, 
//						    0, 0, 
//						    GameConstants.TILE_SIZE_X, GameConstants.TILE_SIZE_Y, 
//						    srcX, srcY, 
//						    srcX + GameConstants.TILE_SIZE_X, srcY + GameConstants.TILE_SIZE_Y, 
//						    null);
//			} finally {
//				g.dispose();
//			}
//
//		} else {
//			indicatorImage = null;
//		}
//	}
//	
//	// Resolves the id based on the width/height of sheet to the x location of the top-left coordinate to draw the tile.
//	private static int computeSrcX(int id, BufferedImage sheet) {
//		return (id * GameConstants.TILE_SIZE_X) % (sheet.getWidth() );
//	}
//	
//	// Resolves the id based on the width/height of sheet to the x location of the top-left coordinate to draw the tile.
//	private static int computeSrcY(int id, BufferedImage sheet) {
//		return (id / (sheet.getWidth() / GameConstants.TILE_SIZE_X) ) * (GameConstants.TILE_SIZE_Y);
//	}
//	
//	private void drawTileIndicator(Graphics2D g2d) {
//		int snapX = snapMouseX(mousePosition.x() );
//		int snapY = snapMouseY(mousePosition.y() );
//		if (indicatorImage == null) {
//			g2d.setColor(Color.green);
//			g2d.drawRect(snapX,
//						 snapY, 
//						 GameConstants.TILE_SIZE_X, GameConstants.TILE_SIZE_Y);
//		} else {
//			g2d.drawImage(indicatorImage, 
//						  snapX, snapY,
//						  snapX + GameConstants.TILE_SIZE_X, snapY + GameConstants.TILE_SIZE_Y, 
//						  0, 0, 
//						  indicatorImage.getWidth(), indicatorImage.getHeight(), 
//						  null);
//		}
//	}
//	
//	@Override public void setTileBrushAndId(PaintbrushType type, int id) {
//		currentTileType = type;
//		currentTileId = id;
//		
//		switch(type) {
//		case SOLIDS: // break omitted
//		case THRUS: // break omitted
//		case SCENES: // break omitted
//		case CONVEYERS_CLOCKWISE: // break omitted
//		case CONVEYERS_ANTI_CLOCKWISE: // break omitted
//		case COLLAPSIBLE:
//			changeState(EditorState.PLACING_TILES);
//			break;
//		case GOODIES:
//			currentGoodieType = Goodie.Type.byValue(id);
//			changeState(EditorState.PLACING_GOODIES);
//			break;
//		case HAZARDS:
//			currentHazard = world.getHazards().get(id);
//			changeState(EditorState.PLACING_HAZARDS);
//			break;
//		case PLACE_SPRITES:
//			currentSpriteId = id;
//			changeState(EditorState.PLACING_SPRITES);
//			break;
//		default:
//			throw new RuntimeException("method not updated to handle new brush type " + type);
//		}
//	}
//
//	@Override public void setTileBrush(PaintbrushType type) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override public void addTileModificationCallback(Function<Tile[][], Void> callback) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override public void newTiles(int rows, int cols) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override public void setBackground(Background background) {
//		// TODO Auto-generated method stub
//		
//	}
//	
//	// private data
//	
//	// Drawn to the current tilemap when a click is registered at some position
//	private PaintbrushType currentTileType = PaintbrushType.SOLIDS;
//	private int currentTileId = 0;
//	
//	private Goodie.Type currentGoodieType = Goodie.Type.APPLE;
//	
//	// No good default other than null. However, system should never be in a state of placing hazards without some hazard defined.
//	private Hazard currentHazard;
//	
//	// When placing sprites, the model window opens with this default id.
//	private int currentSpriteId = 0;
//	
//	private final World world;
//	
//	private final WorldResource rsrc;
//	
//	private EditorState currentState = EditorState.NO_WORLD_LOADED;
//
//	// Current indicator (drawn at the cursor/tile position based on editor state). If null, a green square is drawn
//	// instead.
//	private BufferedImage indicatorImage;
//	
//	// The primary backing tilemap array. This may be an entire level, or some subset, or a template, or whatever size that isn't bigger than an entire
//	// level.
//	private final Tile[][] tileMap;
//}
