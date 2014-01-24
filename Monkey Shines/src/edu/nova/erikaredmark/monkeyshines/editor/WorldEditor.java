package edu.nova.erikaredmark.monkeyshines.editor;

import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import edu.nova.erikaredmark.monkeyshines.Goodie;
import edu.nova.erikaredmark.monkeyshines.Hazard;
import edu.nova.erikaredmark.monkeyshines.ImmutablePoint2D;
import edu.nova.erikaredmark.monkeyshines.LevelScreen;
import edu.nova.erikaredmark.monkeyshines.encoder.EncodedWorld;
import edu.nova.erikaredmark.monkeyshines.graphics.WorldResource;
import edu.nova.erikaredmark.monkeyshines.World;

/**
 * 
 * Wraps a world and provides editor functions as well as the ability to save the world to a file. Intended ONLY for
 * the level editor.
 * 
 * @author Erika Redmark
 *
 */
public final class WorldEditor {
	
	private final World world;
	
	private final WorldResource rsrc;
	
	private Map<Integer, LevelScreenEditor> levelScreenEditors =
		new HashMap<Integer, LevelScreenEditor>();
	
	private WorldEditor(final World world, final WorldResource rsrc) {
		this.world = world;
		this.rsrc = rsrc;
	}
	
	public static WorldEditor newWorld(final String name, final WorldResource rsrc) {
		return new WorldEditor(World.newWorld(name, rsrc), rsrc);
	}
	
	/** 
	 * 
	 * Creates a world editor based on a world already existing, and inflates the world with the given graphics resource pack
	 * and assigns it to a new level editor instance
	 * 
	 * @param encoded
	 * 		the encoded world file
	 * 
	 * @param rsrc
	 * 		the graphics resource
	 * 
	 * @return
	 * 		a world editor for editing the world
	 * 
	 */
	public static WorldEditor fromEncoded(EncodedWorld encoded, WorldResource rsrc) {
		World world = World.inflateFrom(encoded, rsrc);
		
		return new WorldEditor(world, rsrc);
	}
	
	/**
	 * 
	 * Returns the world resource this editor currently is using for the world.
	 * 
	 * @return
	 * 
	 */
	public WorldResource getWorldResource() {
		return this.rsrc;
	}
	
	/**
	 * Returns the level screen editor for the given screen id. Screen editors are persisted in the world editor, so once
	 * initialised with the world's screen, all changes are saved as long as the reference to the world editor is kept.
	 * <p/>
	 * This method does NOT change the current screen displayed. That should be done with {@link #changeCurrentScreen(int)}
	 * 
	 * @param id
	 * 		the id of the screen
	 * 
	 * @return
	 * 		either a newly initialised, or pre-existing level screen editor for the given id. If the screen does not exist, 
	 * 	    a new one will be created
	 */
	public LevelScreenEditor getLevelScreenEditor(final int id) {
		LevelScreenEditor ed = levelScreenEditors.get(id);
		if (ed != null) return ed;
		
		// Create a new screen editor from an existing screen in the world.
		if (world.screenIdExists(id) ) {
			return LevelScreenEditor.from(world.getScreenByID(id) );
		}
		
		// Create a new, empty screen for the level
		LevelScreen newScreen = LevelScreen.newScreen(id, getWorldResource() );
		world.addScreen(newScreen);
		return LevelScreenEditor.from(newScreen);
	}
	
	/**
	 * Changes the world editor to display the screen for the current level screen editor. A valid editor instance must
	 * be held (get from {@link #getLevelScreenEditor(int)}) to insure that the screen actually exists before displaying
	 * 
	 * @param editor
	 * 		the level screen editor to mark as current. This editor will be displayed on the main screen i.e the game
	 * 		engine will be running this level
	 */
	public void changeCurrentScreen(LevelScreenEditor editor) {
		world.changeCurrentScreen(editor.getId() );
		// TODO display dialog if there is an unknown failure? Check return type
	}
	
	public void paint(Graphics2D g) {
		world.paint(g);
	}

	/**
	 * Forwarding call to {@link World#addGoodie(int, int, int, int) }													
	 * 
	 * @param i
	 * 		row of goodie
	 * 
	 * @param j
	 * 		column of goodie
	 * 
	 * @param id
	 * 		id of the screen this goodie will appear on
	 * 
	 * @param goodieId
	 * 		id of the actual goodie (apple, orange, gray key, etc...)
	 */
	public void addGoodie(int i, int j, int id, Goodie.Type goodieType) {
		world.addGoodie(i, j, id, goodieType);
	}
	
	/**
	 * Forwarding call to {@link World#removeGoodie(int, int, int) }
	 */
	public void removeGoodie(int row, int col, int screenId) {
		world.removeGoodie(row, col, screenId);
	}
	
	/**
	 * Forwarding call to {@link World#getHazards() }
	 */
	public List<Hazard> getHazards() {
		return world.getHazards();
	}
	
	/**
	 * Forwarding call to {@link World#setHazards() }
	 */
	public void setHazards(ImmutableList<Hazard> hazards) {
		world.setHazards(hazards);
	}
	
	/**
	 * 
	 * Sets bonzo to be starting at the given x/y tile location on the screen enumerated by the given id. These co-ordindates
	 * refer to tiles, not pixels. Bonzo is a 2 by 2 tile size, and this location is the upper-left location he will start.
	 * 
	 * @param x
	 * 		bonzo x starting location, tile number
	 * 
	 * @param y
	 * 		bonzo y starting location, number
	 * 
	 * @param id
	 * 		id of the screen. This screen must already exist
	 * 
	 * @throws
	 * 		IllegalArgumentException
	 * 			if no screen by the given id exists
	 * 
	 */
	public void setBonzo(int xTile, int yTile, int id) {
		if (world.screenIdExists(id) == false) throw new IllegalArgumentException("Screen id " + id + " does not exist");
		world.getScreenByID(id).setBonzoStartingLocation(ImmutablePoint2D.of(xTile, yTile) );
	}

	/** Forwarding call to {@link World#getWorldName() } 																*/
	public String getWorldName() { return world.getWorldName(); }

	/** 
	 * Forwarding call to {@link World#getGoodies() }
	 * <p/>
	 * Additionally, because the world is wrapped in the editor, goodies won't be deleted unless requested by editor,
	 * the returned map represents the static location of every goodie on the map.
	 * 
	 * @return
	 * 		immutable map of all goodies
	 */
	public Map<String, Goodie> getGoodies() { return world.getGoodies(); }

	
	/** Forwarding call to {@link World#getLevelScreens() } 															
	 *  Unless encoding, clients should stick with LevelScreenEditor objects.											*/
	public Map<Integer, LevelScreen> getLevelScreens() { return world.getLevelScreens(); }

	/**
	 * 
	 * @param id
	 * 
	 * @return
	 * 		{@code true} if the screen by the given id already exists, {@code false} if otherwise
	 * 
	 */
	public boolean screenExists(int id) {
		return world.screenIdExists(id);
	}


}
