package org.erikaredmark.monkeyshines.resource;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.erikaredmark.monkeyshines.GameSoundEffect;
import org.erikaredmark.monkeyshines.global.SoundUtils;
import org.erikaredmark.monkeyshines.graphics.exception.ResourcePackException;
import org.erikaredmark.monkeyshines.graphics.exception.ResourcePackException.Type;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

/**
 * Reads resource packs, and spits out {@code WorldResource} objects.
 * A resource pack is a zipped folder containing the following 
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
 * If there is any issue with the pack (missing resource, background2.png with no background1.png, for examples) this
 * method will throw an exception. Otherwise, this will load all the graphics into memory and have them ready to
 * be applied to a world.
 * <p/>
 * This is a separate utility calss from {@code WorldResource} because there is a lot of
 * slightly unwieldly code given that two completely separate image formats are used based
 * on the intent of the resource (AWT for Editor, Slick for game)
 * @author Goddess
 *
 */
public class PackReader {
	
	private static final String CLASS_NAME = "org.erikaredmark.monkeyshines.resource.PackReader";
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
	
	/* -- Internal -- */
	private static final Pattern INDEX_PATTERN = Pattern.compile("^.*?([0-9]+)\\.png$");
	
	/**
	 * Initialises this resource object from a resource pack into an AWT compatible {@code WorldResource}.
	 * 
	 * Only for use with level editor.
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
	public static WorldResource fromPackAwt(final Path packFile) throws ResourcePackException {
		// TODO replace with reading magic number http://www.coderanch.com/t/381509/java/java/check-file-zip-file-java
	
		
		// Declare non final versions of instance data. However, we enforce only replacing null in other ways in the below code.
		// Once they are added to the world resource object they will become final.
		// We de
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
		BufferedImage energyBar = null;
		
		// Sound clips
		// Unlike graphics, some sounds may not exist, and that is okay. The game just won't play
		// any sound when requested.
		// Optional is required to reduce ambigiuity in map
		Map<GameSoundEffect, Optional<Clip>> gameSounds = new IdentityHashMap<>();
		
		// It is okay for this to be null. No music simply means none will be played
		Optional<Clip> backgroundMusic = null;
		
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
				case "energy.png":
					if (energyBar != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "energy.png");
					energyBar = ImageIO.read(zipFile.getInputStream(entry) );
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
					} else if (entryName.matches("^pattern[0-9]+\\.png$") ) {
						int index = indexFromName(entryName);
						if (patterns.length > index) {
							if (patterns[index] != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, entry.getName() );
						}
						if (index > maxPatternIndex) maxPatternIndex = index;
						BufferedImage tempPattern = ImageIO.read(zipFile.getInputStream(entry) );
						patterns[index] = tempPattern;
					/* ---------------------- Sounds ----------------------- */
					// Due to the nature of graphics amounts being unknown,
					// but types of sounds being finite, any name of any file
					// not matching any other pattern IS a sound.
					// TODO may not be best plan. May cause issues if we wish to allow
					// additional 'stuff' as part of the resource pack, like readmes and whatnot.
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
		// 0) Nothing is null
		// 1) Array lists go from 0 to some value with no skips
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
		checkResourceNotNull(energyBar, "energy.png");
		
		// Backgrounds and Patterns may be empty, but sprites must contain at least 1
		if (sprites[0] == null)  throw new ResourcePackException(Type.NO_DEFINITION, "There are no sprites for this world; must contain at least one");
		
		// Sounds may be null
		// No null checks
		
		// We need to construct an array of sprites that has identical references save for being a lot smaller
		BufferedImage cutSprites[] = new BufferedImage[maxSpriteIndex + 1];
		for (int i = 0; i <= maxSpriteIndex; i++) {
			cutSprites[i] = sprites[i];
		}
		
		AwtWorldGraphics awtGraphics = new AwtWorldGraphics(
			solidTiles, 
			thruTiles, 
			sceneTiles,
			hazardTiles,
			conveyerTiles,
			collapsingTiles,
			backgrounds,
			patterns,
			cutSprites,
			goodieSheet, 
			yumSheet,
			explosionSheet);
		
		WorldResource worldRsrc = WorldResource.createAwtResource(awtGraphics, gameSounds, backgroundMusic);
		
		return worldRsrc;
	}
	
	/**
	 * Loads the pack at the given location for use in the main game. This must be called AFTER an OpenGL
	 * context has been created, so the pack loading should only be done within a Slick {@code BasicGame}
	 * init method
	 * or when it is otherwise available
	 * @param packFile
	 * @return
	 * @throws ResourcePackException
	 */
	public static WorldResource fromPackSlick(final Path packFile) throws ResourcePackException {
		// TODO visitor pattern would help a great deal given that the same process is used
		// for both types of resource packs... but the split in resource packs is
		// only temporary so the level editor isn't broken whilst moving to Slick2D. Hopefully
		// a single format can be used throughout later.
		
		// Declare non final versions of instance data. However, we enforce only replacing null in other ways in the below code.
		// Once they are added to the world resource object they will become final.
		// We de
		Image solidTiles 	= null;
		Image thruTiles		= null;
		Image sceneTiles	= null;
		Image hazardTiles   = null;
		Image conveyerTiles = null;
		Image collapsingTiles = null;
		// Max index will be used to tell the validator how far to count to in the array list to confirm 
		// contiguous entries. (as in, if 'background4' exists, then 'background0, background1, etc' MUST exist.
		// Initially -1. That means no elements. The max index is NOT size, so 0 still would mean at least 1, which
		// we don't know yet.
		// TODO we assume no more than 256 backgrounds, 256 patterns, and 256 sprites. This is simply because we might
		// hit a later indexed item out of order and List implementations don't allow adding at specific future indexes.
		Image[] backgrounds = new Image[256];
		int maxBackgroundIndex = -1;
		Image[] patterns = new Image[256];
		int maxPatternIndex = -1;
		Image[] sprites = new Image[256];
		int maxSpriteIndex = -1;
		Image goodieSheet = null;
		Image yumSheet = null;
		Image bannerSheet = null;
		Image scoreNumbersSheet = null;
		Image bonusNumbersSheet = null;
		Image explosionSheet = null;
		Image splashScreen = null;
		Image energyBar = null;
		
		// Sound clips
		// Unlike graphics, some sounds may not exist, and that is okay. The game just won't play
		// any sound when requested.
		// Optional is required to reduce ambiguity in map
		Map<GameSoundEffect, Optional<Clip>> gameSounds = new IdentityHashMap<>();
		
		// It is okay for this to be null. No music simply means none will be played
		Optional<Clip> backgroundMusic = null;
		
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
					solidTiles = new Image(zipFile.getInputStream(entry), "solidTiles", false);
					break;
				case "thrus.png":
					if (thruTiles != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "thrus.png");
					thruTiles = new Image(zipFile.getInputStream(entry), "thruTiles", false);
					break;
				case "scenes.png":
					if (sceneTiles != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "scenes.png");
					sceneTiles = new Image(zipFile.getInputStream(entry), "scenes", false);
					break;
				case "conveyers.png":
					if (conveyerTiles != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "conveyer.png");
					conveyerTiles = new Image(zipFile.getInputStream(entry), "conveyers", false);
					break;
				case "collapsing.png":
					if (collapsingTiles != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "collapsing.png");
					collapsingTiles = new Image(zipFile.getInputStream(entry), "collapsing", false);
					break;
				case "goodies.png":
					if (goodieSheet != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "goodies.png");
					goodieSheet = new Image(zipFile.getInputStream(entry), "goodies", false);
					break;
				case "yums.png":
					if (yumSheet != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "yums.png");
					yumSheet = new Image(zipFile.getInputStream(entry), "yums", false);
					break;
				case "hazards.png":
					if (hazardTiles != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "hazards.png");
					hazardTiles = new Image(zipFile.getInputStream(entry), "hazards", false);
					break;
				case "uibanner.png":
					if (bannerSheet != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "uibanner.png");
					bannerSheet = new Image(zipFile.getInputStream(entry), "uibanner", false);
					break;
				case "energy.png":
					if (energyBar != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "energy.png");
					energyBar = new Image(zipFile.getInputStream(entry), "energy", false);
					break;
				case "bonusNumbers.png":
					if (bonusNumbersSheet != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "bonusNumbers.png");
					bonusNumbersSheet = new Image(zipFile.getInputStream(entry), "bonusNumbers", false);
					break;
				case "scoreNumbers.png":
					if (scoreNumbersSheet != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "scoreNumbers.png");
					scoreNumbersSheet = new Image(zipFile.getInputStream(entry), "scoreNumbers", false);
					break;
				case "explosion.png":
					if (explosionSheet != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "explosion.png");
					explosionSheet = new Image(zipFile.getInputStream(entry), "explosion", false);
					break;
				case "splash.png":
					if (splashScreen != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "splash.png");
					splashScreen = new Image(zipFile.getInputStream(entry), "splash", false);
					break;
				case "music.ogg":
					if (backgroundMusic != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "music.ogg");
					backgroundMusic = loadSoundClip(zipFile, entry);
					break;
				// All other types are handled in default, as many different names may belong to one 'class' of things.
				default:
					/* -------------------- Backgrounds -------------------- */
					if (entryName.matches("^background[0-9]+\\.png$") ) {
						int index = indexFromName(entryName);
						// Index out of bounds exception if we check and the array isn't big enough. If index is greater than size, then
						// there was no previous anyway. If it isn't, make sure it is null
						if (backgrounds.length > index) {
							if (backgrounds[index] != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, entry.getName() );
						}
						if (index > maxBackgroundIndex) maxBackgroundIndex = index;
						Image tempBackground = new Image(zipFile.getInputStream(entry), "background", false);
						backgrounds[index] = tempBackground;
					/* ---------------------- Sprites ---------------------- */
					} else if (entryName.matches("^sprite[0-9]+\\.png$") ) {
						int index = indexFromName(entryName);
						if (sprites.length > index) {
							if (sprites[index] != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, entry.getName() );
						}
						if (index > maxSpriteIndex) maxSpriteIndex = index;
						Image tempSprite = new Image(zipFile.getInputStream(entry), "sprite" + index, false);
						sprites[index] = tempSprite;
					} else if (entryName.matches("^pattern[0-9]+\\.png$") ) {
						int index = indexFromName(entryName);
						if (patterns.length > index) {
							if (patterns[index] != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, entry.getName() );
						}
						if (index > maxPatternIndex) maxPatternIndex = index;
						Image tempPattern = new Image(zipFile.getInputStream(entry), "pattern" + index, false);
						patterns[index] = tempPattern;
					/* ---------------------- Sounds ----------------------- */
					// Due to the nature of graphics amounts being unknown,
					// but types of sounds being finite, any name of any file
					// not matching any other pattern IS a sound.
					// TODO may not be best plan. May cause issues if we wish to allow
					// additional 'stuff' as part of the resource pack, like readmes and whatnot.
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
		
		} catch (IOException | SlickException e) {
			throw new ResourcePackException(e);
		}
		
		// FINAL CHECKS
		// -1) Splash screen is available for main game
		// 0) Nothing is null
		// 1) Array lists go from 0 to some value with no skips
		checkResourceNotNull(splashScreen, "splash.png");
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
		checkResourceNotNull(energyBar, "energy.png");
		
		// Backgrounds and Patterns may be empty, but sprites must contain at least 1
		if (sprites[0] == null)  throw new ResourcePackException(Type.NO_DEFINITION, "There are no sprites for this world; must contain at least one");
		
		// Sounds may be null
		// No null checks
		
		// We need to construct an array of sprites that has identical references save for being a lot smaller
		Image cutSprites[] = new Image[maxSpriteIndex + 1];
		for (int i = 0; i <= maxSpriteIndex; i++) {
			cutSprites[i] = sprites[i];
		}
		
		try
		{
			SlickWorldGraphics slickGraphics = new SlickWorldGraphics(
				solidTiles, 
				thruTiles, 
				sceneTiles,
				hazardTiles,
				conveyerTiles,
				collapsingTiles,
				backgrounds,
				patterns,
				cutSprites,
				goodieSheet, 
				yumSheet,
				bannerSheet,
				scoreNumbersSheet,
				bonusNumbersSheet,
				explosionSheet,
				splashScreen,
				energyBar);
			
			WorldResource worldRsrc = WorldResource.createSlickResource(slickGraphics, gameSounds, backgroundMusic);
			
			return worldRsrc;
		} catch (SlickException e) {
			throw new ResourcePackException(e);
		}
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
	 * <p/>
	 * If the clip cannot be loaded, absent is returned. It is up to sound manager
	 * systems to handle unwrapping Optionals.
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
	private static Optional<Clip> loadSoundClip(ZipFile file, ZipEntry entry) throws ResourcePackException {
		// Load the audio stream from the entry
		// Buffered stream to allow mark/reset
		try (InputStream bin = new BufferedInputStream(file.getInputStream(entry) ) ) {
			return Optional.of(
				SoundUtils.clipFromOggStream(bin, entry.getName() ) );
			
		} catch (UnsupportedAudioFileException e) {
			LOGGER.log(
				Level.SEVERE,
				"Check that resources are of ogg format and that system is " +
				    "able to read ogg format:" +
					e.getMessage(), 
				e);
		} catch (IOException | LineUnavailableException e) {
			LOGGER.log(
				Level.SEVERE,
				"Unable to get line to sound system; cannot initialise clip: " +
					e.getMessage(), 
				e);
		} catch (Exception e) {
			LOGGER.log(
				Level.SEVERE,
				"Unexpected exception initialising clip: " + e.getMessage(), 
				e);
		}
		
		return Optional.empty();
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
	
	private static void checkResourceNotNull(Object img, String name) throws ResourcePackException {
		if (img == null) throw new ResourcePackException(Type.NO_DEFINITION, name);
	}
	
	/** Ensures that an array counts from 0 to max index, with no skips in between (skips mean a null entry)
	 */
	private static void checkResourceContiguous(final Object[] items, int maxIndex, String name) throws ResourcePackException {
		for (int i = 0; i <= maxIndex; i++) {
			if (items[i] == null) throw new ResourcePackException(Type.NON_CONTIGUOUS, name + i + ".png");
		}
	}
	
}
