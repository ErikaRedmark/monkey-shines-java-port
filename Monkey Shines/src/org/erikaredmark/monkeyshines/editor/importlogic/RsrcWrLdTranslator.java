package org.erikaredmark.monkeyshines.editor.importlogic;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.erikaredmark.monkeyshines.Conveyer;
import org.erikaredmark.monkeyshines.Hazard;
import org.erikaredmark.monkeyshines.LevelScreen;
import org.erikaredmark.monkeyshines.World;
import org.erikaredmark.monkeyshines.editor.importlogic.WorldTranslationException.TranslationFailure;
import org.erikaredmark.monkeyshines.resource.WorldResource;

/**
 * 
 * Handles translating the .wrld data from the original Monkey Shines. It is up to higher level
 * translators to provide the stream of just that data; this does not extract from .msh or other
 * file formats.
 * 
 * @author Erika Redmark
 *
 */
public class RsrcWrLdTranslator {

	/**
	 * 
	 * Translates the given stream for a {@code LevelScreen}. the stream pointer is advanced
	 * 122 bytes by the end of this method (which it must have at least that much of or an exception will be thrown)
	 * <p/>
	 * Level file format information can be found at:
	 * https://github.com/ErikaRedmark/monkey-shines-java-port/wiki/Original-Level-File-Format
	 * 
	 * @param is
	 * 		.wrld stream only
	 * 
	 * @param levels
	 * 		all the levels parsed by {@code RsrcPlvlTranslator}. They must ALL be parsed before parsing the .wrld data.
	 * 
	 * @param rsrc
	 * 		world resource
	 * 
	 * @param name
	 * 		world name (not included in binary stream)
	 * 
	 * @param translationState
	 * 		a translation state object for information that can only be set after all levels are
	 * 		parsed.
	 * 
	 * @return
	 * 		instance of {@code World}
	 * 
	 * @throws WorldTranslationException
	 * 		if the given stream does not have at least 1980 bytes to work with, or the data is determined
	 * 		to be nonsensical in some way
	 * 
	 * @throws IOException 
	 * 		if an unexpected error occurs reading the stream
	 * 
	 */
	public static World translateWorld(InputStream is, List<LevelScreen> levels, WorldResource rsrc, String name, TranslationState translationState) 
									       throws WorldTranslationException, IOException {

		// Skip first 8 bytes (4 shorts). We only care about the bonus door level.
		long skipped = is.skip(8L);
		if (skipped != 8L)  throw new WorldTranslationException(TranslationFailure.WRONG_WORLD_SIZE, "Failure to skip 8 bytes, possible incorrect size.");
		
		byte[] bonusLevel = new byte[2];
		int bonusRead = is.read(bonusLevel);
		if (bonusRead != 2)  throw new WorldTranslationException(TranslationFailure.WRONG_WORLD_SIZE, "Failure to read bonus level data");
		
		int bonusScreen = TranslationUtil.translateMacShort(bonusLevel);

		// Generate hazards (combines WrLd data with graphics resource)
		byte[] hazardTypesRaw = new byte[32]; // 16 shorts
		int hazardTypesRead = is.read(hazardTypesRaw);
		if (hazardTypesRead != 32)  throw new WorldTranslationException(TranslationFailure.WRONG_WORLD_SIZE, "Failure to read hazard types");
		
		int[] hazardTypes = TranslationUtil.translateMacShortArray(hazardTypesRaw);
		
		byte[] hazardExplodesRaw = new byte[16];
		int hazardExplodesRead = is.read(hazardExplodesRaw);
		if (hazardExplodesRead != 16)  throw new WorldTranslationException(TranslationFailure.WRONG_WORLD_SIZE, "Failure to read hazard explode info");
		
		boolean[] hazardExplodes = TranslationUtil.translateMacBooleanArray(hazardExplodesRaw);
		
		List<Hazard> hazards = new ArrayList<>();
		// For loop has early termination if hazardTypes is zero at any point.
		for (int i = 0; i < 16; ++i) {
			if (hazardTypes[i] == 0)  break;
			hazards.add(new Hazard(i, hazardExplodes[i], TranslationUtil.deathType(hazardTypes[i]) ) );
		}
		
		// Nothing more can be learnt from the stream, relying on graphics resource and state info
		// from here. HOWEVER, we MUST skip the remaining bytes in the level data as promised.
		// Two more 16 size short arrays, making 64 bytes total to skip.
		skipped = is.skip(64L);
		if (skipped != 64L)  throw new WorldTranslationException(TranslationFailure.WRONG_WORLD_SIZE, "Failure to skip remaining 64 bytes at end of resource");
		
		
		// Generate conveyers (only requires world resource)
		List<Conveyer> conveyers = new ArrayList<>();
		World.generateConveyers(conveyers, rsrc.getConveyerCount() );
		
		// Break levels list into map based on id
		Map<Integer, LevelScreen> levelsMap = new HashMap<>();
		for (LevelScreen lvl : levels) {
			levelsMap.put(lvl.getId(), lvl);
		}
		
		return new World(name,
						 translationState.generateGoodieMap(),
						 levelsMap,
						 hazards,
						 conveyers,
						 bonusScreen,
						 rsrc);
	}

}
