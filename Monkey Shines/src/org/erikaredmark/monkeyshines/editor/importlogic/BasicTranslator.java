package org.erikaredmark.monkeyshines.editor.importlogic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.erikaredmark.monkeyshines.LevelScreen;
import org.erikaredmark.monkeyshines.World;
import org.erikaredmark.monkeyshines.background.FullBackground;
import org.erikaredmark.monkeyshines.editor.importlogic.WorldTranslationException.TranslationFailure;
import org.erikaredmark.monkeyshines.encoder.EncodedWorld;
import org.erikaredmark.monkeyshines.encoder.exception.WorldSaveException;
import org.erikaredmark.monkeyshines.graphics.exception.ResourcePackException;
import org.erikaredmark.monkeyshines.resource.PackReader;
import org.erikaredmark.monkeyshines.resource.WorldResource;

/**
 * 
 * Entry point for translating old Monkey Shines levels to the new form. This particular translator is
 * a very basic one, and quite dumb. It cannot read .msh (windows) levels or any other form of Resource-Fork
 * begin encoded as data. It is designed for development to ease porting of addon and other worlds at a faster
 * pace, since the setup to use the translator is easier than the setup to recreate the world from scratch.
 * <p/>
 * The translator expects a filesystem path to a folder with a very particular file structure. As an example:
 * <pre>
 * {@code
 * 
 * 		world_name
 * 			|
 * 		  	+--------- lvl_1000.plvl [Plvl data for that level, taken from ResEdit]
 * 			|
 *  		+--------- lvl_1001.plvl
 *  		|
 *  		+--------- world_name.wrld [WrLd data, taken from ResEdit]
 *  		|
 *  		+--------- world_name.zip [Resource pack, MUST be manually created using other tools]
 * 
 * }
 * </pre>
 * 
 * The folder must be named after what the eventual world will be called (and all instances of 'world_name'
 * must be replaced with the same whatever name is chosen). There are three unique types of files. 
 * <p/>
 * The most complex
 * but most well known is the resource pack, which is the graphics, sounds, and music of the addon world. Graphics
 * and sounds can be ripped using Macintosh Classic specific tools and converted. 
 * <p/>
 * Bit easier are the .plvl files. The naming convention is 'lvl_####.plvl' (any number of digits). The value indicates
 * the id of the level screen (that data is not stored in the actual binary stream). This file is a copy-paste of the data
 * stored in the appropriate plvl resource fork of the world being converted. <strong> The file itself must be binary!</strong>
 * It is not enough to copy-paste the hex values from ResEdit into the file and save. The data must actually make up the file.
 * <p/>
 * At the easiest end of the spectrum is the 'world_name.wrld' file, which follows the same requirements as the .plvl resource
 * but there is only one .WrLd resource in any given world.
 * <p/>
 * Setting up the folder to being the conversion is indeed a bit cumbersome unfortunately, but it was the easiest to program first,
 * requring only knowledge of the game's data and not how the resource forks are packed into a data-stream for OS's other than
 * classic mac. It is, however, far easier than recreating each screen from scratch.
 * 
 * @author Erika Redmark
 *
 */
public final class BasicTranslator {
	private static final String CLASS_NAME = "org.erikaredmark.monkeyshines.editor.importlogic.BasicTranslator";
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
	
	/**
	 * In addition to checking if the filename is proper, this also stores in capture group 1 the
	 * id of the level.
	 */
	private static final Pattern LEVEL_FILE_NAME =
		Pattern.compile("^lvl_(\\d+)\\.plvl$"); // Example: lvl_1000.plvl
	
	private BasicTranslator() { }
	
	/**
	 * 
	 * Provides a way to run the translator headless. Takes a path to the level folder as source. The source folder
	 * must conform to the specification of this object. The original level is then translated and saved into the same
	 * folder as 'WorldName.world'. If the .world file already exists, this program will exit immediately and NOT overwrite
	 * the file.
	 * 
	 * @param args
	 * 		must contain only one argument: the path to the translation folder.
	 * 
	 * @throws WorldTranslationException
	 * 		if the world cannot be translated. The exception will detail the issues; this could be either an issue
	 * 		with the level or the translator
	 * 
	 * @throws ResourcePackException
	 * 		if the resource pack for the world does not contain all required components
	 * 
	 * @throws WorldSaveException
	 * 		if the translation was successful, but something prevented the world from being saved.
	 * 
	 * @throws IOException
	 * 		if an unexpected low-level IO error occurs
	 * 
	 */
	public static void main(String args[]) throws IOException, WorldTranslationException, ResourcePackException, WorldSaveException {
		if (args.length != 1) {
			System.err.println("Translator must be called with exactly one argument: a path to the world folder according to the javadoc specifications for this class.");
			return;
		}
		
		final Path sourceFolder = Paths.get(args[0]);
		if (!(Files.isDirectory(sourceFolder) ) ) {
			System.err.println("Path supplied must be a valid folder");
		}
		
		final World world = importWorld(sourceFolder);
		final Path saveTo = sourceFolder.resolve(world.getWorldName() + ".world");
		
		if (Files.exists(saveTo) ) {
			System.err.println("Translation was successful, but cannot save: .world file already exists.");
			return;
		}
		
		final EncodedWorld writeWorld = EncodedWorld.fromMemory(world);
		try (OutputStream os = Files.newOutputStream(saveTo) ) {
			writeWorld.save(os);
		}
		
	}
	
	/**
	 * 
	 * 
	 * 
	 * @param basicFolder
	 * 		folder to import. Ensure folder meets specifications outlined in javadocs of this class.
	 * 
	 * @return
	 * 		a complete, full World object that can be played or saved in the new format
	 * 
	 * @throws IOException
	 * 		if there is an issue reading from the folder passed
	 * 
	 * @throws WorldTranslationException
	 * 		if the underlying binary format is malformed
	 * 
	 * @throws ResourcePackException
	 * 		if the resource pack is malformed
	 * 
	 */
	public static World importWorld(final Path basicFolder)
		throws IOException, WorldTranslationException, ResourcePackException {
		if (!(Files.isDirectory(basicFolder) ) ) {
			throw new IllegalArgumentException(basicFolder + " must be a folder");
		}
		
		// Get valid paths to all required files. Fail quickly if there are any problems before
		// we start translating anything.
		List<Path> levelFiles = new ArrayList<>();
		Path worldFile = null;
		Path resourcePackFile = null;
		
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(basicFolder) ) {
			for (Path p : stream) {
				if (!(Files.isDirectory(p) ) ) {
					// One of three things. This is a resource pack, a level data, or world data.
					String fileName = p.getFileName().toString();
					if (LEVEL_FILE_NAME.matcher(fileName).matches() ) {
						levelFiles.add(p);
						LOGGER.info(CLASS_NAME + ": Added " + p + " as level file");
					} else if (fileName.endsWith(".wrld") ) {
						if (worldFile != null)  throw new WorldTranslationException(TranslationFailure.TRANSLATOR_SPECIFIC, ".wrld file defined twice");
						worldFile = p;
						LOGGER.info(CLASS_NAME + ": Set world file to " + p);
					} else if (fileName.endsWith(".zip") ) {
						if (resourcePackFile != null)  throw new WorldTranslationException(TranslationFailure.TRANSLATOR_SPECIFIC, "multiple .zip files found; ensure only one exists (the resource pack)");
						resourcePackFile = p;
						LOGGER.info(CLASS_NAME + ": Set resource pack file to " + p);
					} else {
						LOGGER.info(CLASS_NAME + ": Skipping unknown file type " + p);
					}
					
				} else {
					LOGGER.info(CLASS_NAME + ": Ignoring directory " + p);
				}
			}
		}
		
		if (levelFiles.isEmpty() )  throw new WorldTranslationException(TranslationFailure.TRANSLATOR_SPECIFIC, "Missing at least one .plvl file");
		if (worldFile == null)  throw new WorldTranslationException(TranslationFailure.TRANSLATOR_SPECIFIC, "Missing .wrld file");
		if (resourcePackFile == null)  throw new WorldTranslationException(TranslationFailure.TRANSLATOR_SPECIFIC, "Missing resource pack");
		
		final String worldFileName = worldFile.getFileName().toString();
		final String worldName = worldFileName.substring(0, worldFileName.lastIndexOf('.') );
		
		LOGGER.info("Data files ready: Beginning translation");
		// We first need the resource pack, since certain level data can only be determined to be valid 
		// by looking at how many graphics resources are defined.
		WorldResource rsrc = PackReader.fromPackAwt(resourcePackFile);
		LOGGER.info(CLASS_NAME + ": Resource pack loaded");
		
		final TranslationState translationState = new TranslationState();
		
		final List<LevelScreen> levels = new ArrayList<>(levelFiles.size() );
		
		for (Path lvl : levelFiles) {
			try (InputStream is = Files.newInputStream(lvl) ) {
				// Id comes from filename
				Matcher m = LEVEL_FILE_NAME.matcher(lvl.getFileName().toString() );
				boolean result = m.matches();
				// Do not attempt to make this 'look nicer' with 'assert m.matches();' If assertions
				// are disabled matches would never be called.
				assert result : "Filename should have passed initial check";
				
				// Should succeed given the check for only numbers.
				int id = Integer.valueOf(m.group(1));
				
				LevelScreen next = RsrcPlvlTranslator.translateLevel(is, id, rsrc, translationState);
				levels.add(next);
				LOGGER.info(CLASS_NAME + ": Parsed level " + id);
			}
		}
		
		// Now handle total world data.
		World world = null;
		try (InputStream is = Files.newInputStream(worldFile) ) {
			world = RsrcWrLdTranslator.translateWorld(is, levels, rsrc, worldName, translationState);
			LOGGER.info(CLASS_NAME + ": Parsed world information");
		}
		
		assert world != null : "Translator should have returned non-null value";
		
		// Finally, that whole translation state thing. We need it to assign ppat backgrounds
		// properly
		Map<Integer, Integer> levelIdToPpat = translationState.getLevelIdToPpat();
		Map<Integer, Integer> ppatToResourceId = translationState.ppatToPatternId();
		
		for (LevelScreen level : levels) {
			// All original MS levels used patterned backgrounds. Spooked is so far the only
			// exception where it can be represented as a solid colour.
			Integer ppat = levelIdToPpat.get(level.getId() );
			level.setBackground(new FullBackground(ppatToResourceId.get(ppat), true) );
		}
		
		// World data has been set, all levels have been set, it's ready.
		return world;
	}
}
