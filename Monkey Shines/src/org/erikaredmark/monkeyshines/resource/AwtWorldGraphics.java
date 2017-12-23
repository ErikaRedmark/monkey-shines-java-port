package org.erikaredmark.monkeyshines.resource;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.background.Background;

public class AwtWorldGraphics {
	/* ---------------------------- TILES ----------------------------- */
	public final BufferedImage solidTiles;
	public final BufferedImage thruTiles;
	public final BufferedImage sceneTiles;
	
	/* --------------------------- HAZARDS ---------------------------- */
	public final BufferedImage hazardTiles;
	
	/* ----------------------- CONVERYER BELTS ------------------------ */
	public final BufferedImage conveyerTiles;
	// Special: lazily initialised (since the real game doesn't ask for
	// it) when editor asks for selecting conveyer belts.
	public BufferedImage editorConveyerTiles;
	public final int conveyerCount;

	/* -------------------------- COLLAPSING -------------------------- */
	public final BufferedImage collapsingTiles;
	// Another lazily initialised sprite sheet specific to the editor.
	public BufferedImage editorCollapsingTiles;
	public final int collapsingCount;
	
	/* -------------------------- BACKGROUND -------------------------- */
	public final Background backgrounds[];
	public final Background patterns[];
	
	/* --------------------------- SPRITES ---------------------------- */
	public final BufferedImage sprites[];

	
	/* --------------------------- GOODIES ---------------------------- */
	public final BufferedImage goodieSheet;
	public final BufferedImage yumSheet;
	
	/* -------------------------- Explosions -------------------------- */
	public final BufferedImage explosionSheet;
	
	/* ------------------------- UI Elements -------------------------- */
	// Shown on the top the main game screen; gives score, bonus, lives, current powerup, and current world.
	public final BufferedImage banner;
	// Bitmap numbers for drawing the score on the banner.
	public final BufferedImage scoreNumbers;
	// Bitmap numbers for drawing the bonus score remaining on the banner
	public final BufferedImage bonusNumbers;
	
	public final BufferedImage splashScreen;
	
	public final BufferedImage energyBar;
	
	public AwtWorldGraphics(
		final BufferedImage solidTiles,
	    final BufferedImage thruTiles,
	    final BufferedImage sceneTiles,
	    final BufferedImage hazardTiles,
	    final BufferedImage conveyerTiles,
	    final BufferedImage collapsingTiles,
	    final Background[] backgrounds,
	    final Background[] patterns,
	    final BufferedImage[] sprites,
	    final BufferedImage goodieSheet,
	    final BufferedImage yumSheet,
	    final BufferedImage banner,
	    final BufferedImage scoreNumbers,
	    final BufferedImage bonusNumbers,
	    final BufferedImage explosionSheet,
	    final BufferedImage splashScreen,
	    final BufferedImage energy)
	{
		this.solidTiles = solidTiles;
		this.thruTiles = thruTiles;
		this.sceneTiles = sceneTiles;
		this.hazardTiles = hazardTiles;
		this.collapsingTiles = collapsingTiles;
		this.backgrounds = backgrounds;
		this.patterns = patterns;
		this.sprites = sprites;
		this.conveyerTiles = conveyerTiles;
		this.goodieSheet = goodieSheet;
		this.yumSheet = yumSheet;
		// May be null
		this.banner = banner;
		this.scoreNumbers = scoreNumbers;
		this.bonusNumbers = bonusNumbers;
		this.explosionSheet = explosionSheet;
		this.splashScreen = splashScreen;
		
		// Height of conveyer sheet can calculate total conveyers in world
		// Remember, a single set is both clockwise and anti-clockwise (hence times 2)
		// Empty worlds, and perhaps other worlds, may have no conveyer belts
		conveyerCount =   conveyerTiles != null
						? conveyerTiles.getHeight() / (GameConstants.TILE_SIZE_Y * 2)
						: 0;
						
		// Simpler than conveyer; height / size of tiles easily gives collapsable tile count
		collapsingCount =   collapsingTiles != null
						  ? collapsingTiles.getHeight() / GameConstants.TILE_SIZE_Y
						  : 0;
		
		// Energy bar is special. We explode the 8x11 image into a full 150x11 image.
		this.energyBar = explodeEnergyBar(energy);
	}
	
	/**
	 * Explodes the energy bar segment into a full energy bar. This class
	 * does that automatically during construction.
	 * @param energySegment
	 */
	public static BufferedImage explodeEnergyBar(BufferedImage energySegment)
	{
		BufferedImage energyBar = new BufferedImage(150, 11, energySegment.getType() );
		Graphics2D g2dEnergyBar = energyBar.createGraphics();
		try {
			g2dEnergyBar.drawImage(energySegment, 
								   0, 0, 
								   2, 11, 
								   0, 0,
								   2, 11, 
								   null);
			
			// 2 pixel on each side from the 150 total gives 146 pixels to fill (41 iterations)
			// math is kept in to make calculations a bit more obvious
			for (int i = 0; i < 146; i += 4) {
				int startX = 2 + i;
				g2dEnergyBar.drawImage(energySegment, 
									   startX, 0, 
									   startX + 4, 11, 
									   2, 0, 
									   6, 11, 
									   null);
			}
			
			g2dEnergyBar.drawImage(energySegment,
								   148, 0,
								   150, 11,
								   6, 0,
								   8, 11,
								   null);
		} finally {
			g2dEnergyBar.dispose();
		}
		return energyBar;
	}
	
	/**
	 * 
	 * Returns the number of uniquely graphical hazards this world can display.
	 * 
	 * @return
	 * 		number of unique hazards
	 * 
	 */
	public int getHazardCount() {
		return hazardTiles.getWidth() / GameConstants.TILE_SIZE_X;
	}
	
	/**
	 * 
	 * Designed for editor; returns the conveyer selection image that a user would use to select which
	 * conveyer belt they want. The sprite sheet is generated to best match the dimensions for the given
	 * amount of conveyer belts. Each 'frame' of the sheet contains the next id of conveyer clockwise, then
	 * the same id anti-clockwise, and then repeats.
	 * Mathmatically, the conveyer id and rotation can be deduced by determining which 'frame' the user 
	 * clicked on (basic division and modulus based on sheet size). The 'frame' index / 2 is the conveyer
	 * id. If the 'frame' index is odd, it is an anti-clockwise belt. Otherwise, it is clockwise.
	 * Each 'frame' is a TILE_SIZE_X by TILE_SIZE_Y check that represents a single tile, and they are ordered
	 * from top left to bottom right.
	 * The generated sprite sheet may not be completely filled with conveyers. Clients must check that the
	 * click actually is a valid conveyer for the world.
	 * 
	 * @return
	 * 		sprite sheet specific for editor to show user to allow conveyer picking
	 * 
	 */
	public BufferedImage getEditorConveyerSheet() {
		// TODO undo lazy init... AWT graphics only used for editor now anyway.
		// Lazy initialise; no need in creating sheet if the actual game is being played as it won't be used there
		if (editorConveyerTiles == null) {
			int width = conveyerTiles.getWidth() * 2;
			// 5 frames of animation * 2 gives 10 frames. 2 frames used per 'Type' meaning that
			// We need TILE_SIZE_Y units of height per 5 unique conveyer belts.
			int height = (1 + (conveyerCount / 5) ) * GameConstants.TILE_SIZE_Y;
			// Generate context for drawing
			BufferedImage sheet = new BufferedImage(width, height, conveyerTiles.getType() );
			
			// Draw on sheet
			Graphics2D graphics = sheet.createGraphics();
			//graphics.setColor(new Color(100, 100, 100, 100) );
			//graphics.fillRect(0, 0, width, height);
			for (int i = 0; i < conveyerCount; i++) {
				// Draw the second frame, which has a little rotation, to give user the sense
				// of which direction the conveyer is going in.
				int drawFromX = GameConstants.TILE_SIZE_X;
				int drawFromY = GameConstants.TILE_SIZE_Y * i * 2;
				
				// We drop down a level per 10 conveyers
				int drawToX = ((i * 2) % 10) * GameConstants.TILE_SIZE_X;
				int drawToY = ((i * 2) / 10) * GameConstants.TILE_SIZE_Y;
				// We have the x, y for the Clockwise conveyer in both source and destination
				graphics.drawImage(conveyerTiles, 
					drawToX, drawToY, 
					drawToX + GameConstants.TILE_SIZE_X, drawToY + GameConstants.TILE_SIZE_Y, 
					drawFromX, drawFromY, 
					drawFromX + GameConstants.TILE_SIZE_X, drawFromY + GameConstants.TILE_SIZE_Y, 
					null);
				
				// Do Anti-clockwise conveyer.
				drawFromY += GameConstants.TILE_SIZE_Y;
				drawToX += GameConstants.TILE_SIZE_X;
				
				graphics.drawImage(conveyerTiles, 
						drawToX, drawToY, 
						drawToX + GameConstants.TILE_SIZE_X, drawToY + GameConstants.TILE_SIZE_Y, 
						drawFromX, drawFromY, 
						drawFromX + GameConstants.TILE_SIZE_X, drawFromY + GameConstants.TILE_SIZE_Y, 
						null);
			}
			
			editorConveyerTiles = sheet;
			graphics.dispose();
		}
		

		
		return editorConveyerTiles;
	}
	
	/**
	 * 
	 * Returns a sprite sheet optimised for the editor, allowing the user to choose a unique type of collapsing
	 * tile. From each set of 10 frames that make up one collapsing tile, the first frame will be used as the
	 * 'exmplar' for that collapsing tile set, and displayed such that the user may easily choose which unique
	 * collapsable tile they want.
	 * 
	 * @return
	 * 		lazily initialised collapsable tile sheet for the editor.
	 * 
	 */
	public BufferedImage getEditorCollapsingSheet() {
		// TODO undo lazy init... AWT graphics only used for editor now anyway.
		// Lazy initialise; no need in creating sheet if the actual game is being played as it won't be used there
		if (editorCollapsingTiles == null) {
			int width = collapsingTiles.getWidth() * 2;
			// 10 frames per collapsing. The editor sprite sheet will show 10 unique collapsing tiles
			// per row.
			int height = (1 + (collapsingCount / 10) ) * GameConstants.TILE_SIZE_Y;
			BufferedImage sheet = new BufferedImage(width, height, collapsingTiles.getType() );
			Graphics2D graphics = sheet.createGraphics();

			for (int i = 0; i < collapsingCount; i++) {
				// Draw only the first frame of the collapsing tile.
				int drawFromX = 0;
				int drawFromY = GameConstants.TILE_SIZE_Y * i;
				
				// We drop down a level per 10 collapsing tiles
				int drawToX = (i % 10) * GameConstants.TILE_SIZE_X;
				int drawToY = (i / 10) * GameConstants.TILE_SIZE_Y;

				graphics.drawImage(collapsingTiles, 
					drawToX, drawToY, 
					drawToX + GameConstants.TILE_SIZE_X, drawToY + GameConstants.TILE_SIZE_Y, 
					drawFromX, drawFromY, 
					drawFromX + GameConstants.TILE_SIZE_X, drawFromY + GameConstants.TILE_SIZE_Y, 
					null);
			}
			
			editorCollapsingTiles = sheet;
			graphics.dispose();
		}
		
		return editorCollapsingTiles;
	}
	
	/**
	 * 
	 * Chops the given image into an array of images, indexed from top left to bottom right. The width/height supplied
	 * must be divisible into the size of the passed image.
	 * <p/>
	 * This is NOT intended for gameplay, which should just draw from the sprite sheet when needed. This is intended for
	 * when the editor needs to show all the possible image selections and each one needs to be mapped to a separate button/
	 * icon.
	 * <p/>
	 * Because of the way the algorithm works, the index in the array has a 1:1 mapping to the id of a tile when used to
	 * chop up a basic tilesheet. 
	 * <br/>For hazards, the 1:1 mapping only applies to the first half
	 * <br/>Conveyers, there are 10 images between 'ids' but switches from clockwise to Anti-clockwise every 5.
	 * <br/>Collapsibles, there are 10 images between ids.
	 * 
	 * @param width
	 * 		width of each chop. Must be a divisor of the width of the entire image
	 * 
	 * @param height
	 * 		height of each chop. Must be a divisor of the height of the entire image
	 * 
	 * @param sprites
	 * 		the image containing the sprites
	 * 
	 * @return
	 * 		an array of buffered images of all the chops, indexed from 0 (top-left) to n (bottom-right)
	 * 
	 */
	public static BufferedImage[] chop(int width, 
									   int height,
									   BufferedImage sprites) {

		assert sprites.getWidth() % width == 0;
		assert sprites.getHeight() % height == 0;

		BufferedImage[] chops = new BufferedImage[(sprites.getWidth() / width) * (sprites.getHeight() / height)];
		
		{
			int index = 0;
			for (int j = 0; j < sprites.getHeight(); j += height) {
				for (int i = 0; i < sprites.getWidth(); i += width) {
					chops[index] = new BufferedImage(width, height, sprites.getType() );
					Graphics2D g2d = chops[index].createGraphics();
					try {
						g2d.drawImage(sprites, 
									  0, 0, 
									  width, height, 
									  i, j, 
									  i + width, j + height, 
									  null);
					} finally {
						g2d.dispose();
					}
					
					++index;
				}
			}
		}
		
		return chops;
	}
}
