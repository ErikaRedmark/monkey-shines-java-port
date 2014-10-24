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
		
		final TranslationFailure FAIL = TranslationFailure.WRONG_LEVEL_SIZE;
		
		// Skip first 10 bytes (5 shorts). This is statistics information that we do not care about in any way.
		TranslationUtil.skip(is, 10, FAIL, "could not skip bonus screen location");

		// Generate hazards (combines WrLd data with graphics resource)
		int[] hazardTypes = TranslationUtil.readMacShortArray(is, 16, FAIL, "Could not read hazard types");
		
		boolean[] hazardExplodes = TranslationUtil.readMacBooleanArray(is, 16, FAIL, "Could not read hazard explosion properties");
		
		List<Hazard> hazards = new ArrayList<>();
		// For loop has early termination if hazardTypes is zero at any point.
		for (int i = 0; i < 16; ++i) {
			if (hazardTypes[i] == 0)  break;
			// TODO figure out how to interpret harmless hazards. Currently all hazards kill bonzo. Manual intervention is required
			// to set those that don't.
			hazards.add(new Hazard(i, hazardExplodes[i], TranslationUtil.deathType(hazardTypes[i]), false) );
		}
		
		// Nothing more can be learnt from the stream, relying on graphics resource and state info
		// from here. HOWEVER, we MUST skip the remaining bytes in the level data as promised.
		// Two more 16 size short arrays, making 64 bytes total to skip.
		TranslationUtil.skip(is, 64, FAIL, "Could not skip final bytes for world data");

		// Generate conveyers (only requires world resource)
		List<Conveyer> conveyers = new ArrayList<>();
		World.generateConveyers(conveyers, rsrc.getConveyerCount() );
		
		// Break levels list into map based on id
		Map<Integer, LevelScreen> levelsMap = new HashMap<>();
		for (LevelScreen lvl : levels) {
			levelsMap.put(lvl.getId(), lvl);
		}
		
		World theWorld = new World(name,
								   translationState.generateGoodieMap(),
								   levelsMap,
								   hazards,
								   conveyers,
								   // Always 10000 in original, always -8000 after inversion rules applied.
								   -8000,
								   rsrc);
		
		// REQUIRED: Some level data has placeholders (see docs on PlaceholderTile)
		theWorld.fixPlaceholders();
		
		return theWorld;
	}

}
