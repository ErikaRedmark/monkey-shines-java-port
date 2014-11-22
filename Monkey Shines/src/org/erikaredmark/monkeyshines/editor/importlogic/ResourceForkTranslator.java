package org.erikaredmark.monkeyshines.editor.importlogic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.erikaredmark.monkeyshines.LevelScreen;
import org.erikaredmark.monkeyshines.World;
import org.erikaredmark.monkeyshines.editor.importlogic.WorldTranslationException.TranslationFailure;
import org.erikaredmark.monkeyshines.encoder.EncodedWorld;
import org.erikaredmark.monkeyshines.encoder.exception.WorldSaveException;
import org.erikaredmark.monkeyshines.graphics.exception.ResourcePackException;
import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.erikaredmark.monkeyshines.resource.WorldResource.UseIntent;

import ResourceManager.Resource;
import ResourceManager.ResourceModel;
import ResourceManager.ResourceType;
/**
 * 
 * Entry point for the resource fork translator, which is a much less labour intensive form of converting old worlds. The input is
 * assumed to be a valid Resource Fork filetype. When a file from a resource-fork supported filesystem is moved to one without,
 * and the resource fork is written as another file, it is still a valid resource fork file. This translator takes advantage of
 * an old third party library: http://gbsmith.freeshell.org/ResCafe/ (only the Non-GUI classes making up the core resource manager)
 * to parse the resource fork and send the appropriate data to other translators.
 * <p/>
 * This translator does NOT handle graphics, sound, and music conversions. Specifically, of the six resources making up an
 * old Monkey Shines world (PICT, ppat, clut, plvl, WrLd, MADH) only plvl and WrLd are used. Like the Basic Translator, one must
 * prepare a resource pack with all the proper graphics, sounds, and music formatted properly.
 * <p/>
 * Unlike the Basic Translator this can be run command line AND is intended to be run from the level editor.
 * 
 * @author Erika Redmark
 *
 */
public final class ResourceForkTranslator {
//	private static final String CLASS_NAME = "org.erikaredmark.monkeyshines.editor.importlogic.ResourceForkTranslator";
//	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
	
	/**
	 * 
	 * Runs this translator in headless mode, meaning it will take the resource fork file and a resource pack, translate the
	 * level to the new form, and then save it to some location on the filesystem.
	 * <p/>
	 * Most errors will be logged to the console. Only a few game-breakers will cause this to fail. In most cases, odd things (such
	 * as unknown tile entries) will be interpretted to some default (such as empty space). Be sure to look at the output as to know
	 * which things may need correcting manually using the level editor.
	 * <p/>
	 * Expects 2 argument: A path to the resource fork file, and a path to the resource pack (graphics, sound, music for the world, must be
	 * created manually). The new saved world will be created in the same directory as the resource pack and the same name as the resource
	 * pack.
	 * 
	 */
	public static void main(String args[]) throws IOException, WorldTranslationException, ResourcePackException, WorldSaveException {
		if (args.length != 2) {
			System.err.println("Translator must be called with exactly two arguments: path to the monkey shines level resource fork and a name/path to save the new world");
			return;
		}
		
		final Path resourceFork = Paths.get(args[0]);
		final Path resourcePack = Paths.get(args[1]);
		if (Files.isDirectory(resourceFork) ) {
			System.err.println("Path supplied must be a valid file");
		}
		
		// Calculate name and location to save
		String saveFileName = resourcePack.getFileName().toString();
		saveFileName = saveFileName.substring(0, saveFileName.lastIndexOf(".") ) + ".world";
		final Path saveTo = resourcePack.getParent().resolve(saveFileName);
		
		if (Files.exists(saveTo) ) {
			System.err.println("Cannot save: .world file already exists.");
			return;
		}
		
		final World world = importWorld(resourceFork, resourcePack);

		final EncodedWorld writeWorld = EncodedWorld.fromMemory(world);
		try (OutputStream os = Files.newOutputStream(saveTo) ) {
			writeWorld.save(os);
		}
	}
	
	/**
	 * 
	 * Generates a world skinned with the appropriate resource pack, translated from the given resource fork. The resource fork should
	 * be conforming to the Macintosh Toolbox standards for resource forks. Files created when transferring a world from SheepShaver (in
	 * the .rsrc folder) conform to this specification.
	 * <p/>
	 * Specifically, this method will be using the plvl and WrLd data.
	 * <p/>
	 * The name of the world will be the same name as the resource pack
	 * 
	 * @param resourceFork
	 * 		the macintosh resource fork data to interpret.
	 * 
	 * @param resourcePack
	 * 		the resource pack for the graphics and sounds. This fulfills the same basic functional idea as the MADH (music), ppat (background),
	 * 		PICT (other graphics resources) resources but must be provided manually and not translated automatically due to vastly different
	 * 		file formats
	 * 
	 * @return
	 * 		a generated world
	 * 
	 * @throws WorldTranslationException
	 * 		if there is a critical error in the resource fork that prevents the world from being translated properly. Smaller issues instead
	 *		will be logged to the log file
	 *
	 * @throws ResourcePackException
	 * 		if the resource pack is not properly formatted for the given world
	 * 
	 */
	public static World importWorld(final Path resourceFork, 
									final Path resourcePack)
										throws WorldTranslationException, ResourcePackException {
		final String worldName = resourceFork.getFileName().toString();
		
		ResourceModel forkModel = new ResourceModel(worldName);
		
		try (RandomAccessFile fork = new RandomAccessFile(resourceFork.toFile(), "r") ) {
			forkModel.read(fork);
		} catch (IOException e) {
			throw new WorldTranslationException(TranslationFailure.TRANSLATOR_SPECIFIC, "Unable to open resource fork for random access due to " + e.getMessage() );
		}
		
		final WorldResource rsrc = WorldResource.fromPack(resourcePack, UseIntent.GAME);
		
		// Postcondition: ResourceModel contains an in-memory representation of the resource fork. Let's start translating the levels first,
		// then with that context the world. Translators accept streams so we use memory streams to re-use the API
		final TranslationState translationState = new TranslationState();
		
		final ResourceType plvlResource = forkModel.getResourceType("Plvl");
		final Resource[] plvls = plvlResource.getResArray();
		
		final List<LevelScreen> levels = new ArrayList<>(plvls.length);
		World world = null; // initialised in try block
		
		try {
			for (Resource plvl : plvlResource.getResArray() ) {
				ByteArrayInputStream rawLevelData = new ByteArrayInputStream(plvl.getData() );
				LevelScreen screen = RsrcPlvlTranslator.translateLevel(rawLevelData, (int)plvl.getID(), rsrc, translationState);
				levels.add(screen);
			}
			
			
			// There is always exactly ONE wlrd resource.
			Resource[] wrldResources = forkModel.getResourceType("WrLd").getResArray();
			if (wrldResources.length != 1) {
				throw new WorldTranslationException(TranslationFailure.TRANSLATOR_SPECIFIC, "Multiple WrLd resources found in resource fork; there may be only one!");
			}
			
			ByteArrayInputStream rawWorldData = new ByteArrayInputStream(wrldResources[0].getData() );
			world = RsrcWrLdTranslator.translateWorld(rawWorldData, levels, rsrc, worldName, translationState);
			
		} catch (IOException e) {
			// This should never happen. The 'stream' is an in-memory stream where IO Exceptions make little sense.
			// if this happens it means a bug in the translator using the stream most likely.
			throw new RuntimeException("IOException for an in-memory stream??? " + e.getMessage(), e);
		}
		
		assert world != null : "World not initialised in try block";
		
		assert world != null : "Translator should have returned non-null value";
		
		// Finally, must assign ppat backgrounds properly
		Map<Integer, Integer> levelIdToPpat = translationState.getLevelIdToPpat();
		Map<Integer, Integer> ppatToResourceId = translationState.ppatToPatternId();
		
		for (LevelScreen level : levels) {
			Integer ppat = levelIdToPpat.get(level.getId() );
			level.setBackground(rsrc.getPattern(ppatToResourceId.get(ppat) ) );
		}
		
		return world;
	}
}
