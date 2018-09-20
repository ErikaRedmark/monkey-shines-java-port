package org.erikaredmark.monkeyshines.resource;

import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.BONUS_DRAW_X;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.HEALTH_DRAW_X;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.HEALTH_DRAW_Y;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.HEALTH_DRAW_Y2;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.HEALTH_MULTIPLIER;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.INFINITY_DRAW_X;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.INFINITY_DRAW_X2;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.INFINITY_DRAW_Y;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.INFINITY_DRAW_Y2;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.INFINITY_HEIGHT;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.INFINITY_WIDTH;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.LIFE_DRAW_X;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.LIFE_DRAW_X2;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.LIFE_DRAW_Y;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.LIFE_DRAW_Y2;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.POWERUP_DRAW_X;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.POWERUP_DRAW_X2;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.POWERUP_DRAW_Y;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.POWERUP_DRAW_Y2;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.SCORE_DRAW_X;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.SCORE_DRAW_Y;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.SCORE_DRAW_Y2;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.SCORE_HEIGHT;
import static org.erikaredmark.monkeyshines.screendraw.GameUIElements.SCORE_WIDTH;

import java.util.Collection;

import org.erikaredmark.monkeyshines.Bonzo;
import org.erikaredmark.monkeyshines.ClippingRectangle;
import org.erikaredmark.monkeyshines.Conveyer;
import org.erikaredmark.monkeyshines.DeathAnimation;
import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.GameWorldLogic;
import org.erikaredmark.monkeyshines.Goodie;
import org.erikaredmark.monkeyshines.ImmutablePoint2D;
import org.erikaredmark.monkeyshines.LevelScreen;
import org.erikaredmark.monkeyshines.Point2D;
import org.erikaredmark.monkeyshines.Powerup;
import org.erikaredmark.monkeyshines.TileMap;
import org.erikaredmark.monkeyshines.World;
import org.erikaredmark.monkeyshines.background.Background;
import org.erikaredmark.monkeyshines.background.FullBackground;
import org.erikaredmark.monkeyshines.background.SingleColorBackground;
import org.erikaredmark.monkeyshines.sprite.Monster;
import org.erikaredmark.monkeyshines.tiles.CollapsibleTile;
import org.erikaredmark.monkeyshines.tiles.CommonTile;
import org.erikaredmark.monkeyshines.tiles.ConveyerTile;
import org.erikaredmark.monkeyshines.tiles.HazardTile;
import org.erikaredmark.monkeyshines.tiles.PlaceholderTile;
import org.erikaredmark.monkeyshines.tiles.TileType;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.erikaredmark.monkeyshines.tiles.CommonTile.StatelessTileType;

/**
 * Renders {@code SlickWorldGraphics} for when the game is running as a game and not inside
 * the level editor. 
 * At the moment, Slick is only used for the game proper, so it is launched after Monkey Shines is already
 * started and the user has navigated through the menus to choose a level. All rendering routines are basically
 * copies of {@code AwtRenderer, but with small changes due to ever so slight API differences. 
 */
// By slight API Differences I mean yes, it's code duplication.
// WARNING This is essentially code duplication for AwtRenderer. Changes here should be synced
// there as well.
// TODO it would be cool if one class could be the template, and some python script or something generates the other one,
// because they are almost identical save for a few API differences and the objects being of a different type.
public class SlickRenderer {

	/**
	 * Paints the UI components to the world. Note that if this is chosen, then the 
	 * g2d object must be translated 80 pixels down before drawing the rest of the
	 * world, as otherwise the UI overlay will cut into the world.
	 * <p/>
	 * @param universe
	 * 		the game world logic to determine the state of the UI elements.
	 */
	public static void paintUI(Graphics g2d, GameWorldLogic universe, SlickWorldGraphics slickGraphics) {
		/* --------------------- Initial Banner ---------------------- */
		g2d.drawImage(slickGraphics.banner, 
					  0, 0,
					  GameConstants.SCREEN_WIDTH, GameConstants.UI_HEIGHT,
					  0, 0,
					  GameConstants.SCREEN_WIDTH, GameConstants.UI_HEIGHT);
		
		/* ------------------------- Health -------------------------- */
		// Normalise bonzo's current health with drawing.
		double healthWidth = ((double)universe.getBonzoHealth()) * HEALTH_MULTIPLIER;
		
		g2d.drawImage(slickGraphics.energyBar,
					  HEALTH_DRAW_X, HEALTH_DRAW_Y,
					  HEALTH_DRAW_X + (int)healthWidth, HEALTH_DRAW_Y2,
					  0, 0,
					  (int)healthWidth, 10);
		
		/* -------------------------- Score -------------------------- */
		for (int i = 0; i < GameWorldLogic.SCORE_NUM_DIGITS; i++) {
			int drawToX = SCORE_DRAW_X + (SCORE_WIDTH * i);
			// draw to Y is always the same
			int drawFromX = SCORE_WIDTH * universe.getScoreDigits()[i];
			// draw from Y is always the same, 0
			g2d.drawImage(slickGraphics.scoreNumbers, 
						  drawToX, SCORE_DRAW_Y,
						  drawToX + SCORE_WIDTH, SCORE_DRAW_Y2, 
						  drawFromX, 0, 
						  drawFromX + SCORE_WIDTH, SCORE_HEIGHT);
		}
		
		/* -------------------- Bonus Countdown ---------------------- */
		for (int i = 0; i < GameWorldLogic.BONUS_NUM_DIGITS; i++) {
			int drawToX = BONUS_DRAW_X + (SCORE_WIDTH * i);
			// draw to Y is always the same
			int drawFromX = SCORE_WIDTH * universe.getBonusDigits()[i];
			// draw from Y is always the same, 0
			g2d.drawImage(slickGraphics.bonusNumbers,
						  drawToX, SCORE_DRAW_Y,
						  drawToX + SCORE_WIDTH, SCORE_DRAW_Y2,
						  drawFromX, 0,
						  drawFromX + SCORE_WIDTH, SCORE_HEIGHT);
		}
		
		/* ------------------------- Lives --------------------------- */
		{
			int lifeDigit = universe.getLifeDigit();
			if (lifeDigit >= 0) {
				assert lifeDigit < 10;
				int drawFromX = SCORE_WIDTH * lifeDigit;
				
				g2d.drawImage(slickGraphics.scoreNumbers,
							  LIFE_DRAW_X, LIFE_DRAW_Y,
							  LIFE_DRAW_X2, LIFE_DRAW_Y2,
							  drawFromX, 0,
							  drawFromX + SCORE_WIDTH, SCORE_HEIGHT);
			} else {
				g2d.drawImage(slickGraphics.infinity,
							  INFINITY_DRAW_X, INFINITY_DRAW_Y,
							  INFINITY_DRAW_X2, INFINITY_DRAW_Y2,
							  0, 0,
							  INFINITY_WIDTH, INFINITY_HEIGHT);
			}
		}
		
		/* ------------------------ Powerup --------------------------- */
		{
			if (universe.isPowerupVisible() ) {
				Powerup powerup = universe.getCurrentPowerup();
				assert powerup != null : "Powerup should be invisible if null";
				
				g2d.drawImage(slickGraphics.goodieSheet,
						      POWERUP_DRAW_X, POWERUP_DRAW_Y,
						      POWERUP_DRAW_X2, POWERUP_DRAW_Y2,
						      powerup.drawFromX(), Powerup.POWERUP_DRAW_FROM_Y,
						      powerup.drawFromX2(), Powerup.POWERUP_DRAW_FROM_Y2);
			}
		}
	}
	
	/**
	 * Paints the given {@code Background} object, delegating to the proper painting methods based on
	 * the type, and syncs the data in the object with the graphics data to determine the actual
	 * thing to draw.
	 * @param bg
	 */
	public static void paintBackground(Graphics g2d, Background bg, SlickWorldGraphics slickGraphics) {
		if (bg instanceof FullBackground) {
			FullBackground fullBg = (FullBackground) bg;
			Image toDraw = fullBg.isPattern() 
				? slickGraphics.patternedBackgrounds[fullBg.getId()]
				: slickGraphics.backgrounds[fullBg.getId()];
			paintFullBackground(g2d, toDraw);
		} else if (bg instanceof SingleColorBackground) {
			SingleColorBackground bgColor = (SingleColorBackground) bg;
			// Convert to Slick color
			paintSingleColorBackground(g2d, bgColor.getColorSlick());
		}
	}
	
	/**
	 * Paints the background onto the world assuming a full background; as in, an image that is exactly big 
	 * enough to fill the playable area. Patterned backgrounds are generated dynamically when a 
	 * resource is loaded and can be also used with this method (use the fully generated pattern
	 * background, not the pattern itself!!!
	 */
	public static void paintFullBackground(Graphics g2d, Image background) {
		g2d.drawImage(background, 0, 0, 640, 400, 0, 0, 640, 400);
	}
	
	/**
	 * Paints a background of just a color.
	 */
	public static void paintSingleColorBackground(Graphics g2d, Color color) {
		// Don't modify the state of the graphics object after we exit this method.
		final Color original = g2d.getColor();
		g2d.setColor(color);
		g2d.fillRect(0, 0, 640, 400);
		g2d.setColor(original);
	}
	
	/**
	 * Paints the goodie based on state. If the goodie has not been taken yet, it just animates there. Once it is taken,
	 * it will display the "yum" (taken && !dead) until that animation completes and it is dead, in which from there
	 * it will not longer be painted.
	 * <p/>
	 * If the goodie is not in yum state, this is effectively a sprite based drawing.
	 * 
	 * @param g2d
	 */
	public static void paintGoodie(Graphics g2d, Goodie goodie, SlickWorldGraphics rsrc) {
		int drawToX = goodie.getDrawToX();
		int drawToY = goodie.getDrawToY();
		int drawX = goodie.getDrawX();
		int drawY = goodie.getDrawY();
		if (!goodie.isTaken() && !goodie.isDead())
		{
			g2d.drawImage(rsrc.goodieSheet, drawToX , drawToY, // Destination 1
				drawToX + GameConstants.GOODIE_SIZE_X, drawToY + GameConstants.GOODIE_SIZE_Y, // Destination 2
				drawX, drawY, drawX + GameConstants.GOODIE_SIZE_X, drawY + GameConstants.GOODIE_SIZE_Y);
		}
		else if (goodie.isTaken() && !goodie.isDead()) 
		{
			int yumSprite = goodie.getYumSprite();
			g2d.drawImage(rsrc.yumSheet, drawToX , drawToY, // Destination 1
				drawToX + GameConstants.GOODIE_SIZE_X, drawToY + GameConstants.GOODIE_SIZE_Y, // Destination 2
				yumSprite * GameConstants.GOODIE_SIZE_X, 0, // Source 1
				yumSprite * GameConstants.GOODIE_SIZE_X + GameConstants.GOODIE_SIZE_X, GameConstants.GOODIE_SIZE_Y);
		}
	}

	/**
	 * Paints the world, including all tiles, goodies, hazards, and sprites.
	 * @param g2d
	 * @param world
	 */
	public static void paintWorld(Graphics g2d, World world) {
		WorldResource rsrc = world.getResource();
		SlickWorldGraphics slickGraphics = rsrc.getSlickGraphics();
		LevelScreen curScreen = world.getCurrentScreen();
		paintLevelScreen(g2d, curScreen, slickGraphics);
		
		// TODO group goodies into a better collection based on screen
		Collection<Goodie> goodies = (Collection<Goodie>)world.getGoodies().values();
		for (Goodie nextGoodie : goodies) {
			if (nextGoodie.getScreenID() == curScreen.getId()) {
				paintGoodie(g2d, nextGoodie, slickGraphics);
			}
		}
	}
	
	/**
	 * Draw background, tiles, and sprites.
	 * @param g2d
	 */
	public static void paintLevelScreen(Graphics g2d, LevelScreen screen, SlickWorldGraphics slickGraphics) {
		paintBackground(g2d, screen.getBackground(), slickGraphics);
		paintTileMap(g2d, screen.getMap(), slickGraphics);
		for (Monster s : screen.getMonstersOnScreen()) {
			paintMonster(g2d, s, slickGraphics);
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
	public static void paintTileMap(Graphics g2d, TileMap map, SlickWorldGraphics rsrc) {
		TileType[] internalMap = map.internalMap();
		int cols = map.getColumnCount();
		for (int i = 0; i < internalMap.length; ++i) {
			paintTileType(
				g2d, 
				internalMap[i],
				(i % cols) * GameConstants.TILE_SIZE_X,
				(i / cols) * GameConstants.TILE_SIZE_Y,
				rsrc);
		}
	}
	
	/**
	 * Paints the given tile on the world for the level editor. 
	 * Container classes store location, and supply it to paint
	 * method in pixel position. The tile will draw according to its internal id, its state, and
	 * the current world resource.
	 * <p/>
	 * If the tile cannot draw, due to the passed resource not containing the appropriate graphics,
	 * this method will should NOT throw an exception. implementations should supply a placeholder graphic 
	 * or not draw at all (placeholder preferred)
	 */
	public static void paintTileType(Graphics g2d, TileType type, int drawToX, int drawToY, SlickWorldGraphics slickGraphics)
	{
		// ---------------- Collapsing Tiles --------------------
		if (type instanceof CollapsibleTile) {
			CollapsibleTile collapse = (CollapsibleTile) type;
			int damage = collapse.getDamange();
			// Paints the given collapsible tile onto the given graphics context, at the given position.
			// The appearence of this tile if fully controlled by its own state.
			// 10 frames of animation, 20 points of damage.
			int drawFromX = (damage / 2) * GameConstants.TILE_SIZE_X;
			// y position is controlled 100% by immutable id
			int drawFromY = type.getId() * GameConstants.TILE_SIZE_Y;
			
			g2d.drawImage(slickGraphics.collapsingTiles, drawToX , drawToY, 							    // Destination 1 (top left)
						  drawToX + GameConstants.TILE_SIZE_X, drawToY + GameConstants.TILE_SIZE_Y,     // Destination 2 (bottom right)
						  drawFromX, drawFromY, 													    // Source 1 (top Left)
						  drawFromX + GameConstants.TILE_SIZE_X, drawFromY + GameConstants.TILE_SIZE_Y);// Source 2 (bottom right)
		// -------------------- Common ------------------------
		} else if (type instanceof CommonTile) {
			CommonTile common = (CommonTile) type;
			StatelessTileType underlyingType = common.getUnderlyingType();
			if (underlyingType == StatelessTileType.NONE)  return;
			
			int tileDrawCol = common.getTileDrawCol();
			int tileDrawRow = common.getTileDrawRow();
			g2d.drawImage(slickGraphics.getStatelessTileTypeSheet(underlyingType), 
						  drawToX, drawToY, 																// Dest 1
						  drawToX + GameConstants.TILE_SIZE_X, drawToY + GameConstants.TILE_SIZE_Y,			// Dest 2
						  tileDrawCol, tileDrawRow, 														// Src 1
						  tileDrawCol + GameConstants.TILE_SIZE_X, tileDrawRow + GameConstants.TILE_SIZE_Y);// Src 2);
		// ------------------- Conveyer -----------------------
		} else if (type instanceof ConveyerTile) {
			ConveyerTile conveyer = (ConveyerTile) type;
			int animationStep = conveyer.getAnimationStep();
			
			assert animationStep >= 0 && animationStep < 5;
			
			// X position depends 100% on animation step
			int drawFromX = animationStep * GameConstants.TILE_SIZE_X;
			
			// ySet indicates the set of conveyer belts an id
			// is specified for.
			int ySet = Conveyer.CONVEYER_SET_SIZE * conveyer.getId();
			
			// Y position is either the same as ySet for clockwise, or ySet + TILE_SIZE_Y for anti-clockwise
			int drawFromY = ySet + conveyer.getConveyer().getRotation().drawYOffset();
			
			g2d.drawImage(slickGraphics.conveyerTiles, drawToX , drawToY, 									// Destination 1 (top left)
						  drawToX + GameConstants.TILE_SIZE_X, drawToY + GameConstants.TILE_SIZE_Y,     // Destination 2 (bottom right)
						  drawFromX, drawFromY, 													    // Source 1 (top Left)
						  drawFromX + GameConstants.TILE_SIZE_X, drawFromY + GameConstants.TILE_SIZE_Y);
		// ------------------- Hazard -------------------------
		} else if (type instanceof HazardTile) {
			HazardTile hazard = (HazardTile) type;
			if (hazard.isDead() ) 
				{ return; };
			
			int animationStep = hazard.getAnimationStep();
			// If exploding, paint the explosions instead:
			if (!(hazard.isExploding() ) ) {
				
				assert animationStep == 0 || animationStep == 1;
				
				int drawFromX = hazard.getId() * GameConstants.TILE_SIZE_X;
				int drawFromY = animationStep * GameConstants.TILE_SIZE_Y;
				
				g2d.drawImage(slickGraphics.hazardTiles, drawToX , drawToY, 								    // Destination 1 (top left)
							  drawToX + GameConstants.TILE_SIZE_X, drawToY + GameConstants.TILE_SIZE_Y,     // Destination 2 (bottom right)
							  drawFromX, drawFromY, 													    // Source 1 (top Left)
							  drawFromX + GameConstants.TILE_SIZE_X, drawFromY + GameConstants.TILE_SIZE_Y);
			} else {
				g2d.drawImage(slickGraphics.explosionSheet,
							  drawToX, drawToY, 
							  drawToX + GameConstants.TILE_SIZE_X, drawToY + GameConstants.TILE_SIZE_Y, 
							  animationStep * GameConstants.TILE_SIZE_X, 0, 
							  (animationStep + 1) * GameConstants.TILE_SIZE_X, GameConstants.TILE_SIZE_Y);
			}
		// ------------------ Placeholder ---------------------
		} else if (type instanceof PlaceholderTile) {
			// Placeholders are an indication something is wrong. They should have been elminated during the
			// construction of the world.
			Color saveColor = g2d.getColor();
			g2d.setColor(Color.magenta);
			g2d.fillRect(drawToX, drawToY, GameConstants.TILE_SIZE_X, GameConstants.TILE_SIZE_Y);
			g2d.setColor(saveColor);
		} else {
			throw new UnsupportedOperationException("A tile of type " + type.getClass().getName() + 
				" was encountered, but no AWT rendering support exists for it.");
		}
	}
	
	public static void paintMonster(Graphics g2d, Monster sprite, SlickWorldGraphics rsrc) {
		if (!(sprite.isVisible()) )  return;
		Point2D currentLocation = sprite.internalCurrentLocation();
		ClippingRectangle currentClip = sprite.internalCurrentClip();
		g2d.drawImage(
			rsrc.sprites[sprite.getId()], 
			currentLocation.x(), currentLocation.y(), 
			currentLocation.x() + GameConstants.SPRITE_SIZE_X, currentLocation.y() + GameConstants.SPRITE_SIZE_Y,
			currentClip.x(), currentClip.y(), currentClip.width() + currentClip.x(),
			currentClip.height() + currentClip.y());
	}
	
	public static void paintBonzo(Graphics g2d, Bonzo bonzo, SlickWorldGraphics slickGraphics) {
		// If dying, that overrides everything.
		Point2D currentLocation = bonzo.getMutableCurrentLocation();
		if (bonzo.isDying()) {
			int currentSprite = bonzo.getCurrentSprite();
			DeathAnimation deathAnimation = bonzo.getDeathAnimation();
			ImmutablePoint2D deathStart = deathAnimation.deathStart();
			ImmutablePoint2D deathSize = deathAnimation.deathSize();
			ImmutablePoint2D offset = deathAnimation.offset();
			int drawToX = currentLocation.x() + offset.x();
			int drawToY = currentLocation.y() + offset.y();
			int yOffset = deathStart.y() + (deathSize.y() * (currentSprite / deathAnimation.framesPerRow() ) );
			int xOffset = deathSize.x() * (currentSprite % deathAnimation.framesPerRow() );
			g2d.drawImage(slickGraphics.bonzo, drawToX, drawToY,  //DEST
					      drawToX + deathSize.x(), drawToY + deathSize.y(), // DEST2
						  xOffset, yOffset, xOffset + deathSize.x(), yOffset + deathSize.y());
			return;
		} else {
			// We can just get the draw location and assume 40x40
			ImmutablePoint2D sourceLocation = bonzo.getDrawLocationInSprite();
			g2d.drawImage(slickGraphics.bonzo, 
						  currentLocation.x(), currentLocation.y(),
						  currentLocation.x() + Bonzo.BONZO_SIZE.x(), currentLocation.y() + Bonzo.BONZO_SIZE.y(), 
						  sourceLocation.x(), sourceLocation.y(),
						  sourceLocation.x() + Bonzo.BONZO_SIZE.x(), sourceLocation.y() + Bonzo.BONZO_SIZE.y());
		}
	}
}
