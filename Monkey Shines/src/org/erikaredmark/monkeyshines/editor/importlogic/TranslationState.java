package org.erikaredmark.monkeyshines.editor.importlogic;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.erikaredmark.monkeyshines.Goodie;

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

	Map<Integer, Integer> levelIdToPpat = new HashMap<>();
	TreeSet<Integer> ppats = new TreeSet<>();
	
	/** Constructs the object with default state. */
	TranslationState() { }
	
	/**
	 * 
	 * Indicates that the given level id uses the given ppat id. ppat resources are not always sequentially
	 * numbered so cannot easily be converted into a 0 origin ID for the pattern resources in this new version.
	 * 
	 * @param levelId
	 * 
	 * @param ppat
	 * 
	 */
	void addPpatMapping(int levelId, int ppat) {
		levelIdToPpat.put(levelId, ppat);
		ppats.add(ppat);
	}
	
	/**
	 * 
	 * Returns an unmodifiable view of the mapping ppat and the level ids they appear in
	 * 
	 */
	Map<Integer, Integer> getLevelIdToPpat() {
		return Collections.unmodifiableMap(levelIdToPpat);
	}
	
	/**
	 * 
	 * generates a mapping of all the ppats in all the levels to the resource id they belong to
	 * in the resource pack
	 * 
	 */
	Map<Integer, Integer> ppatToPatternId() {
		Integer[] raw = ppats.toArray(new Integer[ppats.size()]);
		// The array is basically the opposite of what we want (index is id in resource pack, value is ppat)
		Map<Integer, Integer> ppatToPatternId = new HashMap<>();
		for (int rsrcId = 0; rsrcId < raw.length; ++rsrcId) {
			ppatToPatternId.put(raw[rsrcId], rsrcId);
		}
		
		return ppatToPatternId;
	}

	/**
	 * 
	 * Adds the given type of goodie to the world at the given levelId, 
	 * 
	 * @param row
	 * 
	 * @param col
	 * 
	 * @param type
	 * 
	 */
	public void addGoodie(int levelId, int row, int col, int type) {
		// TODO method stub
	}

	/**
	 * 
	 * Generates the requested mapping required for the world object based on all goodies added to
	 * the levels.
	 * 
	 * @return
	 */
	public Map<String, Goodie> generateGoodieMap() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
