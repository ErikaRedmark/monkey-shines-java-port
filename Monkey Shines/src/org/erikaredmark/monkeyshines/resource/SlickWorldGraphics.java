package org.erikaredmark.monkeyshines.resource;

import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.tiles.CommonTile.StatelessTileType;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

/**
 * Encodes all slick based image data for Monkey Shines. 
 * <p/>
 * This object is NOT immutable, as image data may have deferred loading, which will prevent the
 * object from being fully constructed until then. When deferred loading is over, finishInitialisation
 * must be called.
 * @author Goddess
 *
 */
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
	public int conveyerCount;

	/* -------------------------- COLLAPSING -------------------------- */
	public final Image collapsingTiles;
	// Another lazily initialised sprite sheet specific to the editor.
	public int collapsingCount;
	
	/* -------------------------- BACKGROUND -------------------------- */
	public final Image backgrounds[];
	public Image patternedBackgrounds[];
	public final Image patterns[];
	
	private int patternCount;
	private int backgroundCount;
	
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
	
	public final Image energySegment;
	public Image energyBar;
	
	/* -------------------------- Core Resources -----------------------*/
	// Core resources normally available everywhere must be manually converted
	// to slick based, and can only be converted when a gl context is active,
	// so each core resource has an analog here
	
	// Infinite Lives Thunderbird
	public final Image infinity;
	public final Image bonzo;
	public final Image getReady;
	public final Image pause;
	public final Image highScoresBackground;
	public final Image loseBackground;
	public final Image tallyScoresBackground;
	
	
	/**
	 * Partially constructs the initial world graphics. Because actual image loading may be
	 * deferred, graphics and values that require
	 */
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
		this.energySegment = energy;
		
		
		// --------- Core Images
		// infinity
		this.infinity = new Image("resources/graphics/thunderbird.png", false, Image.FILTER_NEAREST);
		this.bonzo = new Image("resources/graphics/thebonz.png", false, Image.FILTER_NEAREST);
		this.getReady = new Image("resources/graphics/getReady.png", false, Image.FILTER_NEAREST);
		this.pause = new Image("resources/graphics/pause.png", false, Image.FILTER_NEAREST);
		this.highScoresBackground = new Image("resources/graphics/mainmenu/highscores/background.png");
		this.tallyScoresBackground = new Image("resources/graphics/mainmenu/endgame/bonusTally.png");
		this.loseBackground = new Image("resources/graphics/mainmenu/endgame/lose.png");
	}
	
	public void finishInitialisation() throws SlickException {
		
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
		
		// --------- Dynamically generated
		// Energy bar is special. We explode the 8x11 image into a full 150x11 image.
		this.energyBar = explodeEnergyBar(energySegment);
		
		
		patternedBackgrounds = new Image[patterns.length];
		for (int i = 0; i < patterns.length; ++i) {
			Image ppat = patterns[i];
			if (ppat == null) 
				{ break; }
			patternedBackgrounds[i] = fromPattern(ppat);
		}
		
		int ppatCount = 0;
		for (int i = 0; i < patterns.length; ++i) {
			if (patterns[i] == null)
				{ break; }
			++ppatCount;
		}
		this.patternCount = ppatCount;
		
		int bgCount = 0;
		for (int i = 0; i < backgrounds.length; ++i) {
			if (backgrounds[i] == null) 
				{ break; }
			++bgCount;
		}
		this.backgroundCount = bgCount;
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
							   2, 11);
		
		// 2 pixel on each side from the 150 total gives 146 pixels to fill (41 iterations)
		// math is kept in to make calculations a bit more obvious
		for (int i = 0; i < 146; i += 4) {
			int startX = 2 + i;
			g2dEnergyBar.drawImage(energySegment, 
								   startX, 0, 
								   startX + 4, 11, 
								   2, 0, 
								   6, 11);
		}
		
		g2dEnergyBar.drawImage(energySegment,
							   148, 0,
							   150, 11,
							   6, 0,
							   8, 11);
		
		g2dEnergyBar.flush();
		return energyBar;
	}
	
	/**
	 * Returns the graphics sheet for the tiles that exist for the given tile type.
	 */
	public Image getStatelessTileTypeSheet(final StatelessTileType type) {
		switch (type) {
			case SOLID: return solidTiles;
			case THRU : return thruTiles;
			case SCENE: return sceneTiles;
			case NONE: throw new IllegalArgumentException("No tilesheet for NONE tiles");
			default: throw new IllegalArgumentException("Unknown tile type " + type);
		}
	}
	
	/**
	 * Returns the number of uniquely graphical hazards this world can display.
	 * @return
	 * 		number of unique hazards
	 */
	public int getHazardCount() {
		return hazardTiles.getWidth() / GameConstants.TILE_SIZE_X;
	}

	/**
	 * Returns the number of patterned backgrounds in this resource. This value may not be set on construction
	 * if images are deferred loading.
	 * @return
	 */
	public int getPatternBgCount() {
		return patternCount;
	}
	
	/**
	 * Returnsbackgrounds in this resource. This value may not be set on construction
	 * if images are deferred loading.
	 * @return
	 */
	public int getBackgroundCount() {
		return backgroundCount;
	}
	
	/**
	 * This classic background type (ppat resource) from the original Monkey Shines. Creates a
	 * background dynamically from a pattern that will fit the size of the playable area.
	 * <p/>
	 * Logic duplicated from {@code AwtWorldGraphics}
	 * 
	 * @param ppat
	 * 		the pattern to use
	 * 
	 * @param id
	 * 		the id of this background from the graphics resource. Required for encoding
	 * 		algorithms to properly save instances
	 * 
	 * @return
	 * 		instance of this object
	 * 
	 * @throws IllegalArgumentException
	 * 		if the background is bigger than 640x400
	 * 
	 */
	// TODO investigate how much of this can be factored out so both Awt and Slick use the same codebase.
	public static Image fromPattern(final Image ppat) throws SlickException {
		// We tile 640 / width. If there is any remainder, then we did NOT hit the edge
		// properly and must tile once more (albeit the last tile will only tile partway)
		int width = ppat.getWidth();
		int height = ppat.getHeight();
		
		int tileX = 640 / width + (   640 % width != 0
									? 1
								    : 0);
		
		if (tileX == 0)  throw new IllegalArgumentException("Width " + width + " too large for pattern: must be less than 640");
		
		int tileY = 400 / height + (   400 % height != 0
									 ? 1
									 : 0);
		
		if (tileY == 0)  throw new IllegalArgumentException("Height " + height + " too large for pattern: must be less than 400");
		
		Image background = new Image(640, 400);
		Graphics graphics = background.getGraphics();
		
		// Start with Y: for each ROW
		for (int j = 0; j < tileY; j++) {
			// For each COLUMN
			for (int i = 0; i < tileX; i++) {
				int dx = i * width, dy = j * height;
				graphics.drawImage(
					ppat, 
					dx, dy, 
					dx + width, dy + height, 
					0, 0, 
					width, height);
			}
		}
		
		return background;
	}
	
}
