package org.erikaredmark.monkeyshines.resource;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.GameSoundEffect;
import org.erikaredmark.monkeyshines.background.Background;
import org.erikaredmark.monkeyshines.background.FullBackground;
import org.erikaredmark.monkeyshines.graphics.exception.ResourcePackException;
import org.erikaredmark.monkeyshines.graphics.exception.ResourcePackException.Type;
import org.erikaredmark.monkeyshines.tiles.CommonTile.StatelessTileType;

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
 * Instances of this class are publically immutable until disposed. Once constructed with all available graphics resources, this can not change
 * until {@code dispose() } is called. This is mainly designed to release sound resources and should be thought of as the 'destructor' of this object.
 * Only call when about to otherwise remove references to an instance of this class.
 * 
 * @author Erika Redmark
 *
 */
public final class WorldResource {
	
	/* ---------------------------- TILES ----------------------------- */
	private final BufferedImage solidTiles;
	private final BufferedImage thruTiles;
	private final BufferedImage sceneTiles;
	
	/* --------------------------- HAZARDS ---------------------------- */
	private final BufferedImage hazardTiles;
	
	/* ----------------------- CONVERYER BELTS ------------------------ */
	private final BufferedImage conveyerTiles;
	// Special: lazily initialised (since the real game doesn't ask for
	// it) when editor asks for selecting conveyer belts.
	private BufferedImage editorConveyerTiles;

	/* -------------------------- COLLAPSING -------------------------- */
	private final BufferedImage collapsingTiles;
	// Another lazily initialised sprite sheet specific to the editor.
	private BufferedImage editorCollapsingTiles;
	
	/* -------------------------- BACKGROUND -------------------------- */
	private final Background backgrounds[];
	private final Background patterns[];
	
	/* --------------------------- SPRITES ---------------------------- */
	private final BufferedImage sprites[];

	
	/* --------------------------- GOODIES ---------------------------- */
	private final BufferedImage goodieSheet;
	private final BufferedImage yumSheet;
	
	/* -------------------------- Explosions -------------------------- */
	private final BufferedImage explosionSheet;
	
	/* ------------------------- UI Elements -------------------------- */
	// Shown on the top the main game screen; gives score, bonus, lives, current powerup, and current world.
	private final BufferedImage banner;
	// Bitmap numbers for drawing the score on the banner.
	private final BufferedImage scoreNumbers;
	// Bitmap numbers for drawing the bonus score remaining on the banner
	private final BufferedImage bonusNumbers;
	
	/* --------------------------- SOUNDS ----------------------------- */
	// Whilst sounds are stored here, they should only be played via the
	// SoundManager. null sounds are possible; in that case, that means
	// that there is no sound available for a particular event.
	private final Map<GameSoundEffect, Clip> sounds;
	
	// Generated automatically in constructor
	private final SoundManager soundManager;
	private int conveyerCount;
	private int collapsingCount;

	/* -- Internal -- */
	private static final Pattern INDEX_PATTERN = Pattern.compile("^.*?([0-9]+)\\.png$");
	/** Static factories call this with proper defensive copying. No defensive copying is done in constructor
	 */
	private WorldResource(final BufferedImage solidTiles,
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
					      final Map<GameSoundEffect, Clip> sounds) {
		
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
		this.sounds = sounds;
		this.banner = banner;
		this.scoreNumbers = scoreNumbers;
		this.bonusNumbers = bonusNumbers;
		this.explosionSheet = explosionSheet;
		
		// Generated data
		// this pointer escapes, but no one gets a reference to the manager until construction is over
		// and the manager constructor itself calls no methods on this class.
		soundManager = new SoundManager(this);
		
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
	}
	
	/**
	 * 
	 * Initialises this resource object from a resource pack. A resource pack is a zipped folder containing the following 
	 * contents (note that brackets @{code [ ]} indicate a fill in, not a character literal):
	 * <ol>
	 * <li>solids.png</li>
	 * <li>thrus.png</li>
	 * <li>scenes.png</li>
	 * <li>hazards.png</li>
	 * <li>conveyers.png</li>
	 * <li>collapsing.png</li>
	 * <li>bonusNumbers.png</li>
	 * <li>scoreNumbers.png</li>
	 * <li>background[#].png (from 0 to some value with no breaks)</li>
	 * <li>pattern[#].png (from 0 to some value with no breaks)</li>
	 * <li>sprite[#].png (from 0 to some value with no breaks)</li>
	 * <li>goodies.png</li>
	 * <li>yums.png</li>
	 * <li>explosion.png</li>
	 * <li>uibanner.png</li>
	 * </ol>
	 * TODO add sounds and music when names are finalised
	 * If there is any issue with the pack (missing resource, background2.png with no background1.png, for examples) this
	 * method will throw an exception. Otherwise, this will load all the graphics into memory and have them ready to
	 * be applied to a world.
	 * 
	 * @param packFile
	 * 		a .zip file containing the resource pack
	 * 
	 * @return
	 * 		a resource object with the pack loaded into memory
	 * 
	 * @throws ResourcePackException
	 * 		if the resource pack is corrupted
	 * 
	 * @throws IllegalArgumentException
	 * 		if the given path is not even a .zip file
	 * 
	 */
	public static WorldResource fromPack(final Path packFile) throws ResourcePackException {
		// TODO replace with reading magic number http://www.coderanch.com/t/381509/java/java/check-file-zip-file-java
		
		// Declare non final versions of instance data. However, we enforce only replacing null in other ways in the below code.
		// Once they are added to the world resource object they will become final.
		BufferedImage solidTiles 	= null;
		BufferedImage thruTiles		= null;
		BufferedImage sceneTiles	= null;
		BufferedImage hazardTiles   = null;
		BufferedImage conveyerTiles = null;
		BufferedImage collapsingTiles = null;
		// Max index will be used to tell the validator how far to count to in the array list to confirm 
		// contiguous entries. (as in, if 'background4' exists, then 'background0, background1, etc' MUST exist.
		// Initially -1. That means no elements. The max index is NOT size, so 0 still would mean at least 1, which
		// we don't know yet.
		// TODO we assume no more than 256 backgrounds, 256 patterns, and 256 sprites. This is simply because we might
		// hit a later indexed item out of order and List implementations don't allow adding at specific future indexes.
		BufferedImage[] backgrounds = new BufferedImage[256];
		int maxBackgroundIndex = -1;
		BufferedImage[] patterns = new BufferedImage[256];
		int maxPatternIndex = -1;
		BufferedImage[] sprites = new BufferedImage[256];
		int maxSpriteIndex = -1;
		BufferedImage goodieSheet = null;
		BufferedImage yumSheet = null;
		BufferedImage bannerSheet = null;
		BufferedImage scoreNumbersSheet = null;
		BufferedImage bonusNumbersSheet = null;
		BufferedImage explosionSheet = null;
		
		// Sound clips
		// Unlike graphics, some sounds may not exist, and that is okay. The game just won't play
		// any sound when requested.
		Map<GameSoundEffect, Clip> gameSounds = new IdentityHashMap<>();
		
		try (ZipFile zipFile = new ZipFile(packFile.toFile() ) ) {
			// for (ZipEntry e : file.entries)
			// Java Specialists newsletter: more efficient way to do this when I have time 
			// TODO http://www.javaspecialists.eu/archive/Issue107.html
			for (ZipEntry entry : Collections.list(zipFile.entries() ) ) {
				if (entry.isDirectory() )  continue; // contents of directories will be iterated over anyway.
				final String entryName = getFilename(entry);
				// FIRST: Handle hardcoded names that do not have continuations (numerical values from 0 to some number)
				switch (entryName ) {
				/* --------------------------------- Graphics Other Than Sprites --------------------------------- */
				case "solids.png":
					if (solidTiles != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "solids.png");
					solidTiles = ImageIO.read(zipFile.getInputStream(entry) );
					break;
				case "thrus.png":
					if (thruTiles != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "thrus.png");
					thruTiles = ImageIO.read(zipFile.getInputStream(entry) );
					break;
				case "scenes.png":
					if (sceneTiles != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "scenes.png");
					sceneTiles = ImageIO.read(zipFile.getInputStream(entry) );
					break;
				case "conveyers.png":
					if (conveyerTiles != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "conveyer.png");
					conveyerTiles = ImageIO.read(zipFile.getInputStream(entry) );
					break;
				case "collapsing.png":
					if (collapsingTiles != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "collapsing.png");
					collapsingTiles = ImageIO.read(zipFile.getInputStream(entry) );
					break;
				case "goodies.png":
					if (goodieSheet != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "goodies.png");
					goodieSheet = ImageIO.read(zipFile.getInputStream(entry) );
					break;
				case "yums.png":
					if (yumSheet != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "yums.png");
					yumSheet = ImageIO.read(zipFile.getInputStream(entry) );
					break;
				case "hazards.png":
					if (hazardTiles != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "hazards.png");
					hazardTiles = ImageIO.read(zipFile.getInputStream(entry) );
					break;
				case "uibanner.png":
					if (bannerSheet != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "uibanner.png");
					bannerSheet = ImageIO.read(zipFile.getInputStream(entry) );
					break;
				case "bonusNumbers.png":
					if (bonusNumbersSheet != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "bonusNumbers.png");
					bonusNumbersSheet = ImageIO.read(zipFile.getInputStream(entry) );
					break;
				case "scoreNumbers.png":
					if (scoreNumbersSheet != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "scoreNumbers.png");
					scoreNumbersSheet = ImageIO.read(zipFile.getInputStream(entry) );
					break;
				case "explosion.png":
					if (explosionSheet != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "explosion.png");
					explosionSheet = ImageIO.read(zipFile.getInputStream(entry) );
					break;
				// All other types are handled in default, as many different names may belong to one 'class' of things.
				default:
					// TODO repeated code here: consider refactoring?
					/* -------------------- Backgrounds -------------------- */
					if (entryName.matches("^background[0-9]+\\.png$") ) {
						int index = indexFromName(entryName);
						// Index out of bounds exception if we check and the array isn't big enough. If index is greater than size, then
						// there was no previous anyway. If it isn't, make sure it is null
						if (backgrounds.length > index) {
							if (backgrounds[index] != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, entry.getName() );
						}
						if (index > maxBackgroundIndex) maxBackgroundIndex = index;
						BufferedImage tempBackground = ImageIO.read(zipFile.getInputStream(entry) );
						backgrounds[index] = tempBackground;
					/* ---------------------- Sprites ---------------------- */
					} else if (entryName.matches("^sprite[0-9]+\\.png$") ) {
						int index = indexFromName(entryName);
						if (sprites.length > index) {
							if (sprites[index] != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, entry.getName() );
						}
						if (index > maxSpriteIndex) maxSpriteIndex = index;
						BufferedImage tempSprite = ImageIO.read(zipFile.getInputStream(entry) );
						sprites[index] = tempSprite;
					/* ---------------------- Sounds ----------------------- */
					// Due to the nature of graphics amounts being unknown,
					// but types of sounds being finite, any name of any file
					// not matching any other pattern IS a sound.
					} else if (entryName.matches("^pattern[0-9]+\\.png$") ) {
						int index = indexFromName(entryName);
						if (patterns.length > index) {
							if (patterns[index] != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, entry.getName() );
						}
						if (index > maxPatternIndex) maxPatternIndex = index;
						BufferedImage tempPattern = ImageIO.read(zipFile.getInputStream(entry) );
						patterns[index] = tempPattern;
					} else {
						GameSoundEffect sound = GameSoundEffect.filenameToEnum(entryName);
						if (sound == null) {
							System.out.println("Information: " + entry.getName() + " not a valid resource in resource pack. Skipping.");
							continue;
						} else {
							if (gameSounds.containsKey(sound) )  throw new ResourcePackException(Type.MULTIPLE_DEFINITION, entry.getName() );
							gameSounds.put(sound, loadSoundClip(zipFile, entry) );
						}
					}
				}
			}
		
		} catch (IOException e) {
			throw new ResourcePackException(e);
		}
		
		// FINAL CHECKS
		// 1) Nothing is null
		// 2) Array lists go from 0 to some value with no skips
		checkResourceNotNull(solidTiles, "solids.png");
		checkResourceNotNull(thruTiles, "thrus.png");
		checkResourceNotNull(sceneTiles, "scenes.png");
		checkResourceNotNull(goodieSheet, "goodies.png");
		checkResourceNotNull(yumSheet, "yums.png");
		checkResourceNotNull(hazardTiles, "hazards.png");
		checkResourceNotNull(conveyerTiles, "conveyers.png");
		checkResourceNotNull(collapsingTiles, "collapsing.png");
		checkResourceContiguous(backgrounds, maxBackgroundIndex, "background");
		checkResourceContiguous(sprites, maxSpriteIndex, "sprite");
		checkResourceContiguous(patterns, maxPatternIndex, "pattern");
		checkResourceNotNull(explosionSheet, "explosion.png");
		checkResourceNotNull(scoreNumbersSheet, "scoreNumbers.png");
		checkResourceNotNull(bonusNumbersSheet, "bonusNumbers.png");
		checkResourceNotNull(bannerSheet, "uibanner.png");
		
		// Backgrounds and Patterns may be empty, but sprites must contain at least 1
		if (sprites[0] == null)  throw new ResourcePackException(Type.NO_DEFINITION, "There are no sprites for this world; must contain at least one");
		
		// Sounds may be null
		// No null checks
		
		// We must convert backgrounds and patterns into proper background objects
		Background[] fullBackgrounds = new Background[maxBackgroundIndex + 1];
		Background[] patternBackgrounds = new Background[maxPatternIndex + 1];
		
		for (int i = 0; i <= maxBackgroundIndex ; i++) {
			fullBackgrounds[i] = FullBackground.of(backgrounds[i], i);
		}
		
		for (int i = 0; i <= maxPatternIndex ; i++) {
			patternBackgrounds[i] = FullBackground.fromPattern(patterns[i], i);
		}
		
		// We need to construct an array of sprites that has identical references save for being a lot smaller
		BufferedImage cutSprites[] = new BufferedImage[maxSpriteIndex + 1];
		for (int i = 0; i <= maxSpriteIndex; i++) {
			cutSprites[i] = sprites[i];
		}
		
		WorldResource worldRsrc =
			new WorldResource(solidTiles, 
							  thruTiles, 
							  sceneTiles,
							  hazardTiles,
							  conveyerTiles,
							  collapsingTiles,
							  fullBackgrounds,
							  patternBackgrounds,
							  cutSprites,
							  goodieSheet, 
							  yumSheet,
							  bannerSheet,
							  scoreNumbersSheet,
							  bonusNumbersSheet,
							  explosionSheet,
							  gameSounds);
		
		return worldRsrc;
	}
	
	/**
	 * 
	 * Resolves just the filename from an entry name. The entry name contains the path to the file relative
	 * to the .zip, but in many cases we care only about the actual name, regardless of origin.
	 * <p/>
	 * This method will fail on directories (since they end with a slash)
	 * 
	 * @param entryName
	 * 		entry name from the zip entry
	 * 	
	 * @return
	 * 		just the filename
	 * 
	 * @throws IllegalArgumentException
	 * 		if called on a directory entry
	 * 
	 */
	private static String getFilename(ZipEntry entry) {
		if (entry.isDirectory() )  throw new IllegalArgumentException("Cannot call method with directories");
		final String entryName = entry.getName();
		
		int slash = entryName.lastIndexOf("/");
		if (slash == -1)  return entryName;
		else			  return entryName.substring(slash + 1);
	}
	
	/**
	 * 
	 * Treats the contents of the zip as an ogg encoded sound file and loads the Entire File into memory, returning a
	 * {@code Clip} representing the sound. Only short sound effects are loaded completely; longer sounds like music
	 * should be streamed.
	 * 
	 * @param file
	 * 		the zip file the zip entry comes from
	 * 
	 * @param entry
	 * 		the zip entry containing the sound
	 * 
	 * @return
	 * 		a clip of the sound. The entire sound will be stored in memory
	 * 
	 * @throws ResourcePackException
	 * 		if the sound clip could not be loaded
	 * 
	 */
	private static Clip loadSoundClip(ZipFile file, ZipEntry entry) throws ResourcePackException {
		// Load the audio stream from the entry
		// Buffered stream to allow mark/reset
		try (InputStream bin = new BufferedInputStream(file.getInputStream(entry) );
			AudioInputStream in = AudioSystem.getAudioInputStream(bin) ) {

			AudioFormat baseFormat = in.getFormat();
			
			// Convert to basic PCM
			// Decoded input stream will be closed on disposing of the WorldResource itself.
			// Required for clip.
			AudioFormat decodedFormat =
			    new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
			                    baseFormat.getSampleRate(),
			                    16,
			                    baseFormat.getChannels(),
			                    baseFormat.getChannels() * 2,
			                    baseFormat.getSampleRate(),
			                    false);
			
			AudioInputStream decodedInputStream = AudioSystem.getAudioInputStream(decodedFormat, in);
			// Store in Clip and return
			Clip clip = AudioSystem.getClip();
			clip.open(decodedInputStream);
			if (clip.getFrameLength() == 0) {
				System.err.println("Clip " + entry.getName() + " has no loaded frames. There is an unknown issue decoding .ogg files of sizes less than or equal to around 6K. Please add inaudible noise to sound file to increase size");
			}

			return clip;
		
		} catch (UnsupportedAudioFileException e) {
			throw new ResourcePackException("Check that resources are of ogg format and that system is able to read ogg format:", e);
		} catch (IOException e) {
			throw new ResourcePackException(e);
		} catch (LineUnavailableException e) {
			throw new ResourcePackException(e);
		}
	}
	
	/**
	 * 
	 * Regardless of the actual filename, extracts the number that appears in it. This is designed for filenames of the
	 * type "somename135.png, where the number appears before the .png extension. This is for numbered images that have
	 * an unknown number of graphics, such as sprites for a world.
	 * 
	 * @param name
	 * @return
	 */
	private static int indexFromName(final String name) throws ResourcePackException {
		Matcher matcher = INDEX_PATTERN.matcher(name);
		boolean match = matcher.matches();
		if (match == false) throw new ResourcePackException(Type.NO_INDEX_NUMBER, name + " should contain an index number before .png");
		
		String integer = matcher.group(1);
		return Integer.parseInt(integer);
	}
	
	private static void checkResourceNotNull(BufferedImage img, String name) throws ResourcePackException {
		if (img == null) throw new ResourcePackException(Type.NO_DEFINITION, name);
	}
	
	/** Ensures that an array counts from 0 to max index, with no skips in between (skips mean a null entry)
	 */
	private static void checkResourceContiguous(final Object[] items, int maxIndex, String name) throws ResourcePackException {
		for (int i = 0; i <= maxIndex; i++) {
			if (items[i] == null) throw new ResourcePackException(Type.NON_CONTIGUOUS, name + i + ".png");
		}
	}
	
	/**
	 * 
	 * Returns the graphics sheet for the tiles that exist for the given tile type.
	 * 
	 * @param type
	 * 		the type of the tile
	 * 
	 * @return
	 * 		a reference to the sprite sheet for the tiles
	 * 
	 */
	public BufferedImage getStatelessTileTypeSheet(final StatelessTileType type) {
		switch (type) {
			case SOLID: return solidTiles;
			case THRU : return thruTiles;
			case SCENE: return sceneTiles;
			case NONE: throw new IllegalArgumentException("No tilesheet for NONE tiles");
			default: throw new IllegalArgumentException("Unknown tile type " + type);
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
	 * @throws ArrayIndexOutOfBoundsException
	 * 		if the given id is more than the number of sprites this resource contains
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
	 * @throws ArrayIndexOutOfBoundsException
	 * 		if the given id is more than the number of backgrounds this resource contains
	 * 
	 */
	public Background getBackground(int id) {
		return backgrounds[id];
	}
	
	/**
	 * 
	 * Returns the number of non-repeating backgrounds in this world
	 * 
	 * @return
	 * 
	 */
	public int getBackgroundCount() {
		return backgrounds.length;
	}
	
	/**
	 * 
	 * Returns the background pattern given by the current id
	 * 
	 * @param id
	 * 		id of pattern
	 * 
	 * @return
	 * 		a background created from tiling the pattern across 640x400 pixels of space
	 * 
	 * @throws ArrayIndexOutOfBoundsException
	 * 		if the given id is more than the number of patterns this resource contains
	 * 
	 */
	public Background getPattern(int id) {
		return patterns[id];
	}
	
	public int getPatternCount() {
		return patterns.length;
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

	/**
	 * 
	 * Returns the hazard sprite sheet for this world. There are two rows for an animating hazard, and each column is
	 * the id of the hazard that would be using that graphic.
	 * 
	 * @return
	 * 		hazard sprite sheet
	 * 
	 */
	public BufferedImage getHazardSheet() {
		return hazardTiles;
	}
	
	/**
	 * 
	 * Returns the explosion sprite sheet. Explosions are the size of a tile and have 8 frames
	 * of animation.
	 * 
	 * @return
	 * 		explosion sprite sheet
	 * 
	 */
	public BufferedImage getExplosionSheet() {
		return explosionSheet;
	}
	
	/**
	 * 
	 * Returns the sprite sheet used for drawing score numbers in the UI. Each number is 16 pixels by 30.
	 * Getting a number to draw is easy; the number needed (0-9) multiplied by 16 gives the x starting
	 * point, and all numbers are the same size and at the same y-level of 0
	 * 
	 * @return
	 * 		score numbers sheet
	 * 	
	 */
	public BufferedImage getScoreNumbersSheet() {
		return scoreNumbers;
	}
	
	/**
	 * 
	 * Returns the sprite sheet used for drawing bonus numbers in the UI. Each number is 16 pixels by 30.
	 * Getting a number to draw is easy; the number needed (0-9) multiplied by 16 gives the x starting
	 * point, and all numbers are the same size and at the same y-level of 0
	 * 
	 * @return
	 * 		bonus numbers sheet
	 * 	
	 */
	public BufferedImage getBonusNumbersSheet() {
		return bonusNumbers;
	}
	
	/**
	 * 
	 * Returns the banner that appears at the top of every game. This is the main UI where players will see
	 * bonzos health, lives, score, bonus score, current powerup, and current world. It is the width of the game
	 * screen by a {@code GameConstants.UI_HEIGHT) height.
	 * 
	 * @return
	 * 		main ui banner
	 * 
	 */
	public BufferedImage getBanner() {
		return banner;
	}
	
	/**
	 * 
	 * Returns the sprite sheet for the conveyers. Each conveyer belt is stored as 2 rows of 5 sprites
	 * each the size of a tile. The first row of five tiles is the first conveyer belt going clockwise.
	 * The second row of five tiles is the first conveyer belt going anti-clockwise. This goes on and
	 * on for as many conveyer belts exist. Because all conveyer belts come in pairs, the width will
	 * always be {@code 5 * GameConstants.TILE_SIZE_X} and {@code 2 * GameConstants.TILE_SIZE_Y * <number of conveyers>}
	 * 
	 * @return
	 * 		conveyer belt sprite sheet
	 * 
	 */
	public BufferedImage getConveyerSheet() {
		return conveyerTiles;
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
	 * Returns the sprite sheet for collapsing tiles, which have 10 frames in them. The final frame is typically
	 * empty, but does not need to be. It represents the 'final state' where bonzo can fall through the floor.
	 * The other nine are the process of the tile collapsing.
	 * <p/>
	 * Each row has 10 frames. The first frame of each row is the 'full version' of that specific collapsing tile.
	 * <p/>
	 * Each row indicates a new type of collapsing tile.
	 * 
	 * @return
	 * 
	 */
	public BufferedImage getCollapsingSheet() {
		return collapsingTiles;
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
	 * Returns the number of unique, collapsible tiles in this resource.
	 * 
	 * @return
	 * 
	 */
	public int getCollapsingCount() {
		return collapsingCount;
	}
	
	/**
	 * 
	 * Determines if a hazard of the given ID may be added to the world using this graphics resource. If the hazard 
	 * sprite sheet is too small to accommodate, this returns false.
	 * <p/>
	 * Note that this does NOT stop hazards having a greater ID than the resource is available to render them, as a
	 * hazard can be created and then after the fact, the resources changed to have less hazards. This is merely a
	 * precaution but won't guarantee every hazard in a world is renderable.
	 * <p/>
	 * This is currently defined as the size of the sprite sheet on the x-axis divided by the size of the hazard
	 * sprite.
	 * 
	 * @param size
	 */
	public boolean canAddHazard(int id) {
		int maxId = (hazardTiles.getWidth() / GameConstants.TILE_SIZE_X) - 1;
		return id <= maxId;
	}
	
	/**
	 * 
	 * Returns the clip for the given sound effect, or {@code null} if the sound effect has no clip. Incomplete
	 * resource packs may not contain all sounds.
	 * 
	 * @param effect
	 * 		the effect to get the sound clip for
	 * 
	 * @return
	 * 		the clip itself
	 * 
	 */
	Clip getSoundFor(GameSoundEffect effect) {
		return sounds.get(effect);
	}
	
	public SoundManager getSoundManager() { return this.soundManager; }
	
	/**
	 * 
	 * The 'destructor' of this object. Only call when about to otherwise remove a reference to the given
	 * instance. Destroys all sound resources, and anything else claimed by this object that may not
	 * be released under normal gc.
	 * 
	 */
	public void dispose() {
		for (Clip c : sounds.values() )  c.close();
	}

	/**
	 * 
	 * Returns the number of UNIQUE conveyer belts in this resource. Each 'count' includes
	 * both the clockwise and anti-clockwise verions of a {@code Conveyer} object.
	 * 
	 * @return
	 * 		number of unique conveyer belt sets
	 * 
	 */
	public int getConveyerCount() { return conveyerCount; }

}
