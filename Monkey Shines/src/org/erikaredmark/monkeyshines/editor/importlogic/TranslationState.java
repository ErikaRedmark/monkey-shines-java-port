package org.erikaredmark.monkeyshines.editor.importlogic;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.erikaredmark.monkeyshines.Goodie;
import org.erikaredmark.monkeyshines.World;

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
	private static final String CLASS_NAME = "org.erikaredmark.monkeyshines.editor.importlogic.TranslationState";
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

	Map<Integer, Integer> levelIdToPpat = new HashMap<>();
	TreeSet<Integer> ppats = new TreeSet<>();
	Map<String, Goodie> goodieMap = new HashMap<String, Goodie>();
	
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
	 * Adds the given type of goodie to the world.
	 * 
	 */
	public void addGoodie(Goodie goodie) {
		String checker = World.collisionCheckerForGoodie(goodie.getLocation().x(), goodie.getLocation().y(), goodie.getScreenID() );
		if (goodieMap.containsKey(checker) ) {
			LOGGER.warning(CLASS_NAME + ": Overlapping goodie check: " + checker + ". The goodie that is being skipped is " + goodie);
		}
		goodieMap.put(checker, goodie);
	}

	/**
	 * 
	 * Generates the requested mapping required for the world object based on all goodies added to
	 * the levels.
	 * 
	 */
	public Map<String, Goodie> generateGoodieMap() {
		return goodieMap;
	}
	
}
