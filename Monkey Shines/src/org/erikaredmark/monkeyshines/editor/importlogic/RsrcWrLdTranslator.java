package org.erikaredmark.monkeyshines.editor.importlogic;

import java.io.InputStream;
import java.util.List;

import org.erikaredmark.monkeyshines.LevelScreen;
import org.erikaredmark.monkeyshines.World;

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
	 * 1980 bytes (which it must have at least that much of or an exception will be thrown)
	 * 
	 * @param is
	 * 		.wrld stream only
	 * 
	 * @param levels
	 * 		all the levels parsed by {@code RsrcPlvlTranslator}. They must ALL be parsed before parsing the .wrld data.
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
	 */
	public static World translateWorld(InputStream is, List<LevelScreen> levels, TranslationState translationState) {
		// TODO Auto-generated method stub
		return null;
	}

}
