package org.erikaredmark.monkeyshines.editor.importlogic;

import java.util.TreeSet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * 
 * Created and added to during translation, and then used before finally declaring the world fully complete
 * to set any values in any levels that could not be determined until all the other levels were complete.
 * <p/>
 * By virtue of representing state, this object is not thread-safe.
 * 
 * @author Erika Redmark
 *
 */
class TranslationState {

	Multimap<Integer, Integer> ppatToLevelId = HashMultimap.create();
	TreeSet<Integer> ppats = new TreeSet<>();
	
	/** Constructs the object with default state. */
	TranslationState() { }
	
	/**
	 * 
	 * Indicates that the given ppat id is used for the given level. ppat resources are not always sequentially
	 * numbered so cannot easily be converted into a 0 origin ID for the pattern resources in this new version.
	 * 
	 * @param ppat
	 * 
	 * @param levelId
	 * 
	 */
	void addPpatMapping(int ppat, int levelId) {
		ppatToLevelId.put(ppat, levelId);
		ppats.add(ppat);
	}
	
	/**
	 * 
	 * Returns an unmodifiable view of the mapping ppat and the level ids they appear in
	 * 
	 */
	Multimap<Integer, Integer> getPpatToLevelId() {
		return Multimaps.unmodifiableMultimap(ppatToLevelId);
	}
	
	/**
	 * 
	 * Generates an array between the resource id for a pattern in the resource pack, and a ppat
	 * number. The size of this array is however many unique ppat
	 * resources were found. Between this mapping and the ppatToLevelId map, it should be possible
	 * to assign the proper pattern background to each level. This method should be called after
	 * parsing every possible level in the world with this state object.
	 * <p/>
	 * Each index in the array corresponds to the pattern resource id. The value of the array is the
	 * ppat resource id. Because of this, it is critical that the resource pack for the port have the
	 * patterns ordered with the 0th pattern being the lowest numbered ResEdit pattern, up to the nth
	 * pattern being the highest numbered ResEdit pattern.
	 * 
	 * @return
	 * 		array with index being resource id and value being ppat id.
	 * 
	 */
	int[] patternToPpat() {
		Integer[] raw = ppats.toArray(new Integer[ppats.size()]);
		// Convert to primitive form
		int[] pTp = new int[raw.length];
		for (int i = 0; i < raw.length; ++i) {
			pTp[i] = raw[i];
		}
		
		return pTp;
	}
	
}
