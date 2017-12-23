package org.erikaredmark.monkeyshines.resource;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Collection;

import org.erikaredmark.monkeyshines.ClippingRectangle;
import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.Goodie;
import org.erikaredmark.monkeyshines.LevelScreen;
import org.erikaredmark.monkeyshines.Point2D;
import org.erikaredmark.monkeyshines.Sprite;
import org.erikaredmark.monkeyshines.TileMap;
import org.erikaredmark.monkeyshines.World;
import org.erikaredmark.monkeyshines.tiles.TileType;

/**
 * Provides all render logic for drawing different elements of the world onto an
 * AWT based graphics object.
 * @author Goddess
 */
public class AwtRenderer 
{

	/**
	 * Paints the goodie based on state. If the goodie has not been taken yet, it just animates there. Once it is taken,
	 * it will display the "yum" (taken && !dead) until that animation completes and it is dead, in which from there
	 * it will not logner be painted.
	 * 
	 * @param g2d
	 */
	public static void paintGoodie(Graphics g2d, Goodie goodie, AwtWorldGraphics rsrc) {
		int drawToX = goodie.getDrawToX();
		int drawToY = goodie.getDrawToY();
		int drawX = goodie.getDrawX();
		int drawY = goodie.getDrawY();
		if (!goodie.isTaken() && !goodie.isDead())
		{
			g2d.drawImage(rsrc.goodieSheet, drawToX , drawToY, // Destination 1
				drawToX + GameConstants.GOODIE_SIZE_X, drawToY + GameConstants.GOODIE_SIZE_Y, // Destination 2
				drawX, drawY, drawX + GameConstants.GOODIE_SIZE_X, drawY + GameConstants.GOODIE_SIZE_Y,
				null);
		}
		else if (goodie.isTaken() && !goodie.isDead()) 
		{
			int yumSprite = goodie.getYumSprite();
			g2d.drawImage(rsrc.goodieSheet, drawToX , drawToY, // Destination 1
				drawToX + GameConstants.GOODIE_SIZE_X, drawToY + GameConstants.GOODIE_SIZE_Y, // Destination 2
				yumSprite * GameConstants.GOODIE_SIZE_X, 0, // Source 1
				yumSprite * GameConstants.GOODIE_SIZE_X + GameConstants.GOODIE_SIZE_X, GameConstants.GOODIE_SIZE_Y, // Source 2
				null);
		}
	}
	
	public static void paintWorld(Graphics2D g2d, World world) {
		WorldResource rsrc = world.getResource();
		AwtWorldGraphics awtGraphics = rsrc.getAwtGraphics();
		LevelScreen curScreen = world.getCurrentScreen();
		paintLevelScreen(g2d, curScreen, awtGraphics);
		
		// TODO group goodies into a better collection based on screen
		Collection<Goodie> goodies = (Collection<Goodie>)world.getGoodies().values();
		for (Goodie nextGoodie : goodies) {
			if (nextGoodie.getScreenID() == curScreen.getId()) {
				paintGoodie(g2d, nextGoodie, awtGraphics);
			}
		}
	}
	
	/**
	 * Draw background, tiles, and sprites.
	 * @param g2d
	 */
	public static void paintLevelScreen(Graphics2D g2d, LevelScreen screen, AwtWorldGraphics awtGraphics) {
		screen.getBackground().draw(g2d);
		paintTileMap(g2d, screen.getMap(), awtGraphics);
		for (Sprite s : screen.getSpritesOnScreen()) {
			paintSprite(g2d, s, awtGraphics);
		}
	}
	
	/**
	 * 
	 * Paints the entire tilemap to the graphics context starting at the 0, 0 point (use affinity transforms before passing to change), and
	 * draws each tile at the given row/column dimensions this map was created with.
	 * 
	 * @param g2d
	 * 		graphics context to draw to
	 * 
	 * @param rsrc
	 * 		the world resource for drawing the tiles. Tile graphics are based on internal id synced with the given graphics object
	 * 
	 */
	public static void paintTileMap(Graphics2D g2d, TileMap map, AwtWorldGraphics rsrc) {
		TileType[] internalMap = map.internalMap();
		int cols = map.getColumnCount();
		for (int i = 0; i < internalMap.length; ++i) {
			internalMap[i].paintAwt(g2d, 
					 	 (i % cols) * GameConstants.TILE_SIZE_X, 
						 (i / cols) * GameConstants.TILE_SIZE_Y, 
						 rsrc);
		}
	}
	
	public static void paintSprite(Graphics2D g2d, Sprite sprite, AwtWorldGraphics rsrc) {
		if (!(sprite.isVisible()) )  return;
		Point2D currentLocation = sprite.internalCurrentLocation();
		ClippingRectangle currentClip = sprite.internalCurrentClip();
		g2d.drawImage(
			rsrc.sprites[sprite.getId()], 
			currentLocation.x(), currentLocation.y(), 
			currentLocation.x() + GameConstants.SPRITE_SIZE_X, currentLocation.y() + GameConstants.SPRITE_SIZE_Y,
			currentClip.x(), currentClip.y(), currentClip.width() + currentClip.x(),
			currentClip.height() + currentClip.y(), 
			null );
	}
	
}
