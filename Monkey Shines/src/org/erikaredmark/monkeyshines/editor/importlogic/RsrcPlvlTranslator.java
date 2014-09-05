package org.erikaredmark.monkeyshines.editor.importlogic;

import java.io.InputStream;

import org.erikaredmark.monkeyshines.LevelScreen;

/**
 * 
 * Handles translating the .plvl data from the original Monkey Shines. It is up to higher level
 * translators to provide the stream of just that data; this does not extract from .msh or other
 * file formats.
 * 
 * @author Erika Redmark
 *
 */
public class RsrcPlvlTranslator {

	/**
	 * 
	 * Translates the given stream for a {@code LevelScreen}. the stream pointer is advanced
	 * 122 bytes (which it must have at least that much of or an exception will be thrown)
	 * 
	 * @param is
	 * 		.plvl stream only
	 * 
	 * @param id
	 * 		id of the level screen. This id is not part of the binary data and is normally the id
	 * 		of the resource from the resource fork that the given data resided in
	 * 
	 * @param translationState
	 * 		a translation state object for information that can only be set after all levels are
	 * 		parsed.
	 * 
	 * @return
	 * 		instance of {@code LevelScreen}
	 * 
	 * @throws WorldTranslationException
	 * 		if the given stream does not have at least 122 bytes to work with, or the data is determined
	 * 		to be nonsensical in some way
	 * 
	 */
	public static LevelScreen translateLevel(InputStream is, int id, TranslationState translationState) {
		// TODO method stub
		return null;
	}

}
