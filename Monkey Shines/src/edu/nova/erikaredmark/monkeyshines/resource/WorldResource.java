package edu.nova.erikaredmark.monkeyshines.resource;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
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

import edu.nova.erikaredmark.monkeyshines.GameConstants;
import edu.nova.erikaredmark.monkeyshines.GameSoundEffect;
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
	
	/* -------------------------- BACKGROUND -------------------------- */
	private final BufferedImage backgrounds[];
	
	/* --------------------------- SPRITES ---------------------------- */
	private final BufferedImage sprites[];
	
	/* --------------------------- GOODIES ---------------------------- */
	private final BufferedImage goodieSheet;
	private final BufferedImage yumSheet;
	
	/* --------------------------- SOUNDS ----------------------------- */
	// Whilst sounds are stored here, they should only be played via the
	// SoundManager. null sounds are possible; in that case, that means
	// that there is no sound available for a particular event.
	private final Map<GameSoundEffect, Clip> sounds;
	
	private final SoundManager soundManager;
	
	// Implementation note: Even the arrays are null (not just empty) as this is not intended for any kind of paint methods
	private static final WorldResource EMPTY = new WorldResource(null, null, null, null, null, null, null, null, new HashMap<GameSoundEffect, Clip>() );
	
	
	/* -- Internal -- */
	private static final Pattern INDEX_PATTERN = Pattern.compile("^.*?([0-9]+)\\.gif$");
	/** Static factories call this with proper defensive copying. No defensive copying is done in constructor
	 */
	private WorldResource(final BufferedImage solidTiles,
					      final BufferedImage thruTiles,
					      final BufferedImage sceneTiles,
					      final BufferedImage hazardTiles,
					      final BufferedImage[] backgrounds,
					      final BufferedImage[] sprites,
					      final BufferedImage goodieSheet,
					      final BufferedImage yumSheet,
					      final Map<GameSoundEffect, Clip> sounds) {
		
		this.solidTiles = solidTiles;
		this.thruTiles = thruTiles;
		this.sceneTiles = sceneTiles;
		this.hazardTiles = hazardTiles;
		this.backgrounds = backgrounds;
		this.sprites = sprites;
		this.goodieSheet = goodieSheet;
		this.yumSheet = yumSheet;
		this.sounds = sounds;
		
		// Generated data
		// this pointer escapes, but no one gets a reference to the manager until construction is over
		// and the manager constructor itself calls no methods on this class.
		soundManager = new SoundManager(this);
	}
	
	/**
	 * 
	 * Initialises this resource object from a resource pack. A resource pack is a zipped folder containing the following 
	 * contents (note that brackets @{code [ ]} indicate a fill in, not a character literal):
	 * <ol>
	 * <li>solids.gif</li>
	 * <li>thrus.gif</li>
	 * <li>scenes.gif</li>
	 * <li>hazards.gif</li>
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
	 * @throws ResourcePackException
	 * 		if the resource pack is corrupted
	 * 
	 * @throws IllegalArgumentException
	 * 		if the given path is not even a .zip file
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
		BufferedImage hazardTiles   = null;
		// Max background index will be used to tell the validator how far to count to in the array list to confirm 
		// contiguous entries.
		List<BufferedImage> backgrounds = new ArrayList<>();
		int maxBackgroundIndex = 0;
		List<BufferedImage> sprites		= new ArrayList<>();
		int maxSpriteIndex = 0;
		BufferedImage goodieSheet	= null;
		BufferedImage yumSheet		= null;
		
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
				case "hazards.gif":
					if (hazardTiles != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, "hazards.gif");
					hazardTiles = ImageIO.read(zipFile.getInputStream(entry) );
					break;
				// All other types are handled in default, as many different names may belong to one 'class' of things.
				default:
					/* -------------------- Backgrounds -------------------- */
					if (entryName.matches("^background[0-9]+\\.gif$") ) {
						int index = indexFromName(entryName);
						// Index out of bounds exception if we check and the array isn't big enough. If index is greater than size, then
						// there was no previous anyway. If it isn't, make sure it is null
						if (backgrounds.size() > index) {
							if (backgrounds.get(index) != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, entry.getName() );
						}
						if (index > maxBackgroundIndex) maxBackgroundIndex = index;
						BufferedImage tempBackground = ImageIO.read(zipFile.getInputStream(entry) );
						backgrounds.add(index, tempBackground);
					/* ---------------------- Sprites ---------------------- */
					} else if (entryName.matches("^sprite[0-9]+\\.gif$") ) {
						int index = indexFromName(entryName);
						if (sprites.size() > index) {
							if (sprites.get(index) != null) throw new ResourcePackException(Type.MULTIPLE_DEFINITION, entry.getName() );
						}
						if (index > maxSpriteIndex) maxSpriteIndex = index;
						BufferedImage tempSprite = ImageIO.read(zipFile.getInputStream(entry) );
						sprites.add(index, tempSprite);
					/* ---------------------- Sounds ----------------------- */
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
		checkResourceNotNull(solidTiles, "solids.gif");
		checkResourceNotNull(thruTiles, "thrus.gif");
		checkResourceNotNull(sceneTiles, "scenes.gif");
		checkResourceNotNull(goodieSheet, "goodies.gif");
		checkResourceNotNull(yumSheet, "yums.gif");
		checkResourceNotNull(hazardTiles, "hazards.gif");
		checkResourceContiguous(backgrounds, maxBackgroundIndex, "background");
		checkResourceContiguous(sprites, maxSpriteIndex, "sprite");
		
		// Sounds may be null
		// No null checks
		
		WorldResource worldRsrc =
			new WorldResource(solidTiles, 
							  thruTiles, 
							  sceneTiles,
							  hazardTiles,
							  backgrounds.toArray(new BufferedImage[backgrounds.size()]), 
							  sprites.toArray(new BufferedImage[sprites.size()]),
							  goodieSheet, 
							  yumSheet,
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
			System.out.println("Clip " + entry.getName() + " loaded at " + clip.getFrameLength() + " frame length");

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
	 * Returns an empty world resource. This is intended for test methods to provide resource objects to satisfy constructors
	 * and factories when there is no actual painting involved in the test.
	 * <p/>
	 * All methods from the empty resource object return {@code null}. Painting code does not expect this; do not use when the
	 * test would involve painting code
	 * 
	 * @return
	 * 		empty resource
	 * 
	 */
	public static WorldResource empty() {
		return EMPTY;
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
	 * Returns the graphics sheet for the tiles that exist for the given tile type.
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
			return getHazardSheet();
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

}
