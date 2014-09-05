package org.erikaredmark.monkeyshines.editor.importlogic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.erikaredmark.monkeyshines.LevelScreen;
import org.erikaredmark.monkeyshines.World;
import org.erikaredmark.monkeyshines.editor.importlogic.WorldTranslationException.TranslationFailure;
import org.erikaredmark.monkeyshines.graphics.exception.ResourcePackException;
import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.erikaredmark.monkeyshines.resource.WorldResource.UseIntent;

import com.google.common.collect.Multimap;

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
	
	private static final Pattern WORLD_FILE_NAME =
		Pattern.compile("\\.wrld$"); // Example: BonzoWorld2.wrld
	
	private static final Pattern RESOURCE_PACK_NAME =
		Pattern.compile("\\.zip$"); // Example: BonzoWorld2.zip. Should be same as world file and folder name.
	
	private BasicTranslator() { }
	
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
					} else if (WORLD_FILE_NAME.matcher(fileName).matches() ) {
						worldFile = p;
						LOGGER.info(CLASS_NAME + ": Set world file to " + p);
					} else if (RESOURCE_PACK_NAME.matcher(fileName).matches() ) {
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
		
		LOGGER.info("Data files ready: Beginning translation");
		// We first need the resource pack, since certain level data can only be determined to be valid 
		// by looking at how many graphics resources are defined.
		WorldResource rsrc = WorldResource.fromPack(resourcePackFile, UseIntent.GAME);
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
				
				LevelScreen next = RsrcPlvlTranslator.translateLevel(is, id, translationState);
				levels.add(next);
				LOGGER.info(CLASS_NAME + ": Parsed level " + id);
			}
		}
		
		// Now handle total world data.
		World world = null;
		try (InputStream is = Files.newInputStream(worldFile) ) {
			world = RsrcWrLdTranslator.translateWorld(is, levels, translationState);
			LOGGER.info(CLASS_NAME + ": Parsed world information");
		}
		
		assert world != null : "Translator should have returned non-null value";
		
		// Finally, that whole translation state thing. We need it to assign ppat backgrounds
		// properly
		Multimap<Integer, Integer> ppatToLevelId = translationState.getPpatToLevelId();
		int[] ppats = translationState.patternToPpat();
		// TODO figure this out. Probably need to redo int array to be map ppatToPatternId for ease of use.
		
		// TODO stub
		return null;
	}
}
