package org.erikaredmark.monkeyshines.resource;

import org.erikaredmark.monkeyshines.GameConstants;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class SlickWorldGraphics {
	/* ---------------------------- TILES ----------------------------- */
	public final Image solidTiles;
	public final Image thruTiles;
	public final Image sceneTiles;
	
	/* --------------------------- HAZARDS ---------------------------- */
	public final Image hazardTiles;
	
	/* ----------------------- CONVERYER BELTS ------------------------ */
	public final Image conveyerTiles;
	// Special: lazily initialised (since the real game doesn't ask for
	// it) when editor asks for selecting conveyer belts.
	public Image editorConveyerTiles;
	public final int conveyerCount;

	/* -------------------------- COLLAPSING -------------------------- */
	public final Image collapsingTiles;
	// Another lazily initialised sprite sheet specific to the editor.
	public Image editorCollapsingTiles;
	public final int collapsingCount;
	
	/* -------------------------- BACKGROUND -------------------------- */
	public final Image backgrounds[];
	public final Image patterns[];
	
	/* --------------------------- SPRITES ---------------------------- */
	public final Image sprites[];

	
	/* --------------------------- GOODIES ---------------------------- */
	public final Image goodieSheet;
	public final Image yumSheet;
	
	/* -------------------------- Explosions -------------------------- */
	public final Image explosionSheet;
	
	/* ------------------------- UI Elements -------------------------- */
	// Shown on the top the main game screen; gives score, bonus, lives, current powerup, and current world.
	public final Image banner;
	// Bitmap numbers for drawing the score on the banner.
	public final Image scoreNumbers;
	// Bitmap numbers for drawing the bonus score remaining on the banner
	public final Image bonusNumbers;
	
	public final Image splashScreen;
	
	public final Image energyBar;
	
	public SlickWorldGraphics(
		final Image solidTiles,
	    final Image thruTiles,
	    final Image sceneTiles,
	    final Image hazardTiles,
	    final Image conveyerTiles,
	    final Image collapsingTiles,
	    final Image[] backgrounds,
	    final Image[] patterns,
	    final Image[] sprites,
	    final Image goodieSheet,
	    final Image yumSheet,
	    final Image banner,
	    final Image scoreNumbers,
	    final Image bonusNumbers,
	    final Image explosionSheet,
	    final Image splashScreen,
	    final Image energy) throws SlickException
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
	 * TODO yes this is a duplicate of AwtWorldGraphics. Dealing with code duplication either via
	 * better separation or unifying level editor/main game to use a single image format is another
	 * task.
	 * @param energySegment
	 */
	public static Image explodeEnergyBar(Image energySegment) throws SlickException
	{
		Image energyBar = new Image(150, 11);
		Graphics g2dEnergyBar = energyBar.getGraphics();
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
		return energyBar;
	}
	
	/**
	 * Returns the number of uniquely graphical hazards this world can display.
	 * @return
	 * 		number of unique hazards
	 */
	public int getHazardCount() {
		return hazardTiles.getWidth() / GameConstants.TILE_SIZE_X;
	}
	
}
