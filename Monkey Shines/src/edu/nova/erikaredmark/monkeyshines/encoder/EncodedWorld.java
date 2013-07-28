package edu.nova.erikaredmark.monkeyshines.encoder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableMap;

import edu.nova.erikaredmark.monkeyshines.Goodie;
import edu.nova.erikaredmark.monkeyshines.LevelScreen;
import edu.nova.erikaredmark.monkeyshines.editor.WorldEditor;

/**
 * A serialisable class that maintains the static state data of everything required to recreate a World. This includes
 * the following things:
 * <br/>
 * <ul>
 * <li> Name </li>
 * <li> A mapping of String -> EncodedGoodie for all goodies </li>
 * <li> A mapping of Integer -> EncodedLevelScreen for all screens </li>
 * </ul>
 * See the relevant documentation for {@link EncodedGoodie} and {@link EncodedLevelScreen}.
 * <p/>
 * <strong> Only these proxy classes are serialised.</strong>. The regular classes are free to evolve as long as no
 * changes are made to the static contents of the level.
 * <p/>
 * Instances of this class are immutable.
 * 
 * @author Erika Redmark
 * 
 */
public class EncodedWorld implements Serializable {
	private static final long serialVersionUID = 264L;
	
	private final String name;
	private final Map<String, EncodedGoodie> goodies;
	private final Map<Integer, EncodedLevelScreen> levels;
	
	private EncodedWorld(final String name,
					     final Map<String, EncodedGoodie> goodies,
					     final Map<Integer, EncodedLevelScreen> levels) {
		this.name = name;
		this.goodies = goodies;
		this.levels = levels;
	}
	
	/**
	 * Encodes the world based on the world editor. The WorldEditor is required over just the world as the world's state
	 * is mutable, and may not contain all the requried information to re-initialise.
	 * 
	 * @param world
	 * 		the world editor wrapping a valid world
	 * 
	 * @return
	 * 		an encoded form of the static state of the world
	 */
	public static EncodedWorld from(WorldEditor world) {
		final String _name = world.getWorldName();
		
		Map<String, Goodie> transientGoodies =
			world.getGoodies();
		
																/* Transform all goodies into EncodedGoodies that   ... */
																/* ... can be serialised.								*/
		ImmutableMap.Builder<String, EncodedGoodie> encodedGoods =
				new ImmutableMap.Builder<String, EncodedGoodie>();
		
		for (Entry<String, Goodie> entry : transientGoodies.entrySet() ) {
			encodedGoods.put(entry.getKey(), EncodedGoodie.from(entry.getValue() ) );
		}

		final Map<String, EncodedGoodie> _goodies = encodedGoods.build();
		
																/* Transform all level screens into EncodedLevelScreens	*/
		
		Map<Integer, LevelScreen> transientLevels =
			world.getLevelScreens();
		
		ImmutableMap.Builder<Integer, EncodedLevelScreen> levelsBuilder =
			new ImmutableMap.Builder<Integer, EncodedLevelScreen>();
		
		for (Entry<Integer, LevelScreen> entry : transientLevels.entrySet() ) {
			levelsBuilder.put(entry.getKey(), EncodedLevelScreen.from(entry.getValue() ) );
		}
		
		final Map<Integer, EncodedLevelScreen> _levels =
			levelsBuilder.build();
		
		return new EncodedWorld(_name, _goodies, _levels);
		
	}
	

	/**
	 * 
	 * Creates a new world that is empty. The newly created world will have the given name and a single screen of id 1000.
	 * 
	 * @param name
	 * 		the name of the new world
	 * 
	 * @return
	 * 		an empty encoded world
	 * 
	 */
	public static EncodedWorld fresh(String name) {
		// Set up empty screen
		EncodedLevelScreen emptyScreen = EncodedLevelScreen.fresh(1000);
		Map<Integer, EncodedLevelScreen> screens = new HashMap<>();
		screens.put(1000, emptyScreen);
		
		// Set up empty goodie map
		Map<String, EncodedGoodie> goodies = new HashMap<>();
		
		// Return new empty world
		return new EncodedWorld(name, goodies, screens);
	}

	public String getName() { return name; }
	public Map<String, EncodedGoodie> getGoodies() { return goodies; }
	public Map<Integer, EncodedLevelScreen> getLevels() { return levels; }

	
}
