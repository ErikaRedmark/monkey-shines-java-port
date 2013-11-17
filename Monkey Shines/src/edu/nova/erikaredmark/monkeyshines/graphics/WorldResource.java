package edu.nova.erikaredmark.monkeyshines.graphics;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

import javax.imageio.ImageIO;

import edu.nova.erikaredmark.monkeyshines.graphics.exception.ResourcePackException;
import edu.nova.erikaredmark.monkeyshines.graphics.exception.ResourcePackException.Type;
import edu.nova.erikaredmark.monkeyshines.tiles.HazardTile;
import edu.nova.erikaredmark.monkeyshines.tiles.StatelessTileType;
import edu.nova.erikaredmark.monkeyshines.tiles.TileType;

/**
 * 
 * Unlike {@code World}, this contains all the graphics for a world, and only that. There is no level information. All worlds
 * must have some resource set otherwise they will not draw properly, and the resource should be sane to that world. By 'sane',
 * it means that:
 * <ol>
 * <li>For each different type of tile, there is a tile graphic. So if the maximum solid tile id for a world is 48, there better
 * be at least 48 rectangluar slots available in the graphic.</li>
 * <li>For each different type of Sprite, there is a sprite graphic sheet. Same rules of 'enough sprite graphic sheets' in terms
 * of sprite id apply here</li>
 * </ol>
 * <p/>
 * Instances of this class are immutable. Once constructed with all available graphics resources, this can not change. Otherwise, 
 * objects that cache data about the graphics may perform oddly if this was to change in mid render. To change graphics, the world
 * must be re-skinned with a new instance of this class
 * 
 * @author Erika Redmark
 *
 */
public final class WorldResource {
	// TODO, Erika, you stopped here. We need to have this class hold every graphics context available. All other classes will remove all indidivual references to
	// graphics and will ALWAYS defer to this class for graphics needs.
	
	/* ---------------------------- TILES ----------------------------- */
	private final BufferedImage solidTiles;
	private final BufferedImage thruTiles;
	private final BufferedImage sceneTiles;
	
	/* -------------------------- BACKGROUND -------------------------- */
	private final BufferedImage backgrounds[];
	
	/* --------------------------- SPRITES ---------------------------- */
	private final BufferedImage sprites[];
	
	/* --------------------------- GOODIES ---------------------------- */
	private final BufferedImage goodieSheet;
	private final BufferedImage yumSheet;
	
	
	/* -- Internal -- */
	private static final Pattern INDEX_PATTERN = Pattern.compile("^.*?([0-9]+)\\.gif$");
	/** Static factories call this with proper defensive copying. No defensive copying is done in constructor
	 */
	private WorldResource(final BufferedImage solidTiles,
					      final BufferedImage thruTiles,
					      final BufferedImage sceneTiles,
					      final BufferedImage[] backgrounds,
					      final BufferedImage[] sprites,
					      final BufferedImage goodieSheet,
					      final BufferedImage yumSheet) {
		
		this.solidTiles = solidTiles;
		this.thruTiles = thruTiles;
		this.sceneTiles = sceneTiles;
		this.backgrounds = backgrounds;
		this.sprites = sprites;
		this.goodieSheet = goodieSheet;
		this.yumSheet = yumSheet;
	}
	
	/**
	 * 
	 * Initialises this resource object from a resource pack. A resource pack is a zipped folder containing the following 
	 * contents (note that brackets @{code [ ]} indicate a fill in, not a character literal):
	 * <ol>
	 * <li>solids.gif</li>
	 * <li>thrus.gif</li>
	 * <li>scenes.gif</li>
	 * <li>background[#].gif (from 0 to some value with no breaks)</li>
	 * <li>sprite[#] (from 0 to some value with no breaks)</li>
	 * <li>goodies.gif</li>
	 * <li>yums.gif</li>
	 * </ol>
	 * If there is any issue with the pack (missing resource, background2.gif with no background1.gif, for examples) this
	 * method will throw an exception. Otherwise, this will load all the graphics into memory and have them ready to
	 * be applied to a world.
	 * 
	 * @param packFile
	 * 		a .zip file containing the resource pack
	 * 
	 * @return
	 * 		a resource object with the pack loaded into memory
	 * 
	 * @throws
	 * 		ResourcePackException
	 * 			if the resource pack is corrupted
	 * 		IllegalArgumentException
	 * 			if the given path is not even a .zip file
	 * 
	 */
	public static WorldResource fromPack(final Path packFile) throws ResourcePackException {
		// TODO replace with reading magic number http://www.coderanch.com/t/381509/java/java/check-file-zip-file-java
		//if (packFile.getFileName().endsWith(".zip") == false ) throw new IllegalArgumentException("not a zipfile: " + packFile);
		
		// Declare non final versions of instance data. However, we enforce only replacing null in other ways in the below code.
		// Once they are added to the world resource object they will become final.
		BufferedImage solidTiles 	= null;
		BufferedImage thruTiles		= null;
		BufferedImage sceneTiles	= null;
		// Max background index will be used to tell the validator how far to count to in the array list to confirm 
		// contiguous entries.
		List<BufferedImage> backgrounds = new ArrayList<>();
		int maxBackgroundIndex = 0;
		List<BufferedImage> sprites		= new ArrayList<>();
		int maxSpriteIndex = 0;
		BufferedImage goodieSheet	= null;
		BufferedImage yumSheet		= null;
		
		try (ZipFile zipFile = new ZipFile(packFile.toFile() ) ) {
			// for (ZipEntry e : file.entries)
			// Java Specialists newsletter: more efficient way to do this when I have time 
			// TODO http://www.javaspecialists.eu/archive/Issue107.html
			for (ZipEntry entry : Collections.list(zipFile.entries() ) ) {
				// FIRST: Handle hardcoded names that do not have continuations (numerical values from 0 to some number)
				switch (entry.getName() ) {
				case "solids.gif":
					if (solidTiles != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "solids.gif");
					solidTiles = ImageIO.read(zipFile.getInputStream(entry) );
					break;
				case "thrus.gif":
					if (thruTiles != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "thrus.gif");
					thruTiles = ImageIO.read(zipFile.getInputStream(entry) );
					break;
				case "scenes.gif":
					if (sceneTiles != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "scenes.gif");
					sceneTiles = ImageIO.read(zipFile.getInputStream(entry) );
					break;
				case "goodies.gif":
					if (goodieSheet != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "goodies.gif");
					goodieSheet = ImageIO.read(zipFile.getInputStream(entry) );
					break;
				case "yums.gif":
					if (yumSheet != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "yums.gif");
					yumSheet = ImageIO.read(zipFile.getInputStream(entry) );
					break;
				// SECOND: Handle background[#] and sprite[#] entries
				default:
					// all graphic resources of this have indexes of a valid form
					int index = indexFromName(entry.getName() );
					// BACKGROUNDS
					if (entry.getName().matches("^background[0-9]+\\.gif$") ) {
						// Index out of bounds exception if we check and the array isn't big enough. If index is greater than size, then
						// there was no previous anyway. If it isn't, make sure it is null
						if (backgrounds.size() > index) {
							if (backgrounds.get(index) != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, entry.getName() );
						}
						if (index > maxBackgroundIndex) maxBackgroundIndex = index;
						BufferedImage tempBackground = ImageIO.read(zipFile.getInputStream(entry) );
						backgrounds.add(index, tempBackground);
					// SPRITES
					} else if (entry.getName().matches("^sprite[0-9]+\\.gif$") ) {
						if (sprites.size() > index) {
							if (sprites.get(index) != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, entry.getName() );
						}
						if (index > maxSpriteIndex) maxSpriteIndex = index;
						BufferedImage tempSprite = ImageIO.read(zipFile.getInputStream(entry) );
						sprites.add(index, tempSprite);
					}
				}
			}
		
		} catch (IOException e) {
			throw new ResourcePackException(e);
		}
		
		// FINAL CHECKS
		// 1) Nothing is null
		// 2) Array lists go from 0 to some value with no skips
		checkResourceNotNull(solidTiles, "solids.gif");
		checkResourceNotNull(thruTiles, "thrus.gif");
		checkResourceNotNull(sceneTiles, "scenes.gif");
		checkResourceNotNull(goodieSheet, "goodies.gif");
		checkResourceNotNull(yumSheet, "yums.gif");
		checkResourceContiguous(backgrounds, maxBackgroundIndex, "background");
		checkResourceContiguous(sprites, maxSpriteIndex, "sprite");
		
		return new WorldResource(solidTiles, 
								 thruTiles, 
								 sceneTiles, 
								 backgrounds.toArray(new BufferedImage[0]), 
								 sprites.toArray(new BufferedImage[0]), 
								 goodieSheet, 
								 yumSheet);
	}
	
	/**
	 * 
	 * Regardless of the actual filename, extracts the number that appears in it. This is designed for filenames of the
	 * type "somename135.gif, where the number appears before the .gif extension. This is for numbered images that have
	 * an unknown number of graphics, such as sprites for a world.
	 * 
	 * @param name
	 * @return
	 */
	private static int indexFromName(final String name) throws ResourcePackException {
		Matcher matcher = INDEX_PATTERN.matcher(name);
		boolean match = matcher.matches();
		if (match == false) throw new ResourcePackException(Type.NO_INDEX_NUMBER, name + " should contain an index number before .gif");
		
		String integer = matcher.group(1);
		return Integer.parseInt(integer);
	}
	
	private static void checkResourceNotNull(BufferedImage img, String name) throws ResourcePackException {
		if (img == null) throw new ResourcePackException(Type.NO_DEFINITION, name);
	}
	
	/** Ensures that a list counts from 0 to max index, with no skips in between (skips mean a null entry)
	 */
	private static void checkResourceContiguous(final List<?> items, int maxIndex, String name) throws ResourcePackException {
		for (int i = 0; i <= maxIndex; i++) {
			if (items.get(i) == null) throw new ResourcePackException(Type.NON_CONTIGUOUS, name + i + ".gif");
		}
	}
	
	/**
	 * 
	 * Returns the graphics sheet for the tiles that exist for the given tile type
	 * 
	 * @param type
	 * 		the type of the tile
	 * 
	 * @return
	 * 		a reference to the sprite sheet for the tiles
	 * 
	 */
	public BufferedImage getTilesheetFor(final TileType type) {
		if (type instanceof StatelessTileType) {
			switch ((StatelessTileType)type) {
				case SOLID: return solidTiles;
				case THRU : return thruTiles;
				case SCENE: return sceneTiles;
				case CONVEYER_LEFT: throw new UnsupportedOperationException("Conveyerbelt Tiles are not implemented yet");
				case CONVEYER_RIGHT: throw new UnsupportedOperationException("Conveyerbelt Tiles are not implemented yet");
				case NONE: throw new IllegalArgumentException("No tilesheet for NONE tiles");
				default: throw new IllegalArgumentException("Unknown tile type " + type);
			}
		} else if (type instanceof HazardTile) {
			// TODO return hazardTiles
			throw new UnsupportedOperationException("Hazard Tiles are not implemented yet");
		} else {
			throw new RuntimeException("Unexpected tile type class " + type.getClass().getName() );
		}

	}
	
	/**
	 * 
	 * Returns the sprite sheet for the given id
	 * 
	 * @param id
	 * 		the id of the sprite sheet
	 * 
	 * @return
	 * 		a sprite sheet
	 * 
	 * @throws
	 * 		ArrayIndexOutOfBoundsException
	 * 			if the given id is more than the number of sprites this resource contains
	 * 
	 */
	public BufferedImage getSpritesheetFor(int id) {
		return sprites[id];
	}
	
	/**
	 * 
	 * The sprite sheet for goodies, such as powerups, fruit, and keys.
	 * 
	 * @return
	 * 		goodie sheet
	 * 
	 */
	public BufferedImage getGoodieSheet() { return this.goodieSheet; }
	
	/**
	 * 
	 * The animation sheet for a yum. This is the YUM letters that appear over the tile that contained a goodie right
	 * after bonzo grabs it.
	 * 
	 * @return
	 * 		the yum sheet
	 * 
	 */
	public BufferedImage getYumSheet() { return this.yumSheet; }
	
	/** 
	 * 
	 * Returns a non-repeating background exactly the proper dimensions of a single screen, from the given id.
	 * 
	 * @param id
	 * 		the id of the background
	 * 
	 * @return
	 * 		a background for the screen to use
	 * 
	 * @throws
	 * 		ArrayIndexOutOfBoundsException
	 * 			if the given id is more than the number of backgrounds this resource contains
	 * 
	 */
	public BufferedImage getBackground(int id) {
		return backgrounds[id];
	}

	/**
	 * 
	 * Returns the number of unique sprite graphics present in this world resource. Note that for indexing purposes in the
	 * sprite array, the last index of the sprite graphic is one less than this value.
	 * 
	 * @return
	 * 		number of unique sprites for world
	 * 
	 */
	public int getSpritesCount() {
		return sprites.length;
	}
}
