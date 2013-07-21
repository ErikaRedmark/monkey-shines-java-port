package edu.nova.erikaredmark.monkeyshines.editor;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import edu.nova.erikaredmark.monkeyshines.Goodie;
import edu.nova.erikaredmark.monkeyshines.LevelScreen;
import edu.nova.erikaredmark.monkeyshines.Tile.TileType;
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
	
	private Map<Integer, LevelScreenEditor> levelScreenEditors =
		new HashMap<Integer, LevelScreenEditor>();
	
	private WorldEditor(final World world) {
		this.world = world;
	}
	
	/** 
	 * 
	 * Creates a world editor based on a world already existing. The world file must not be corrupt, and the resource
	 * pack must be present in the same directory
	 * 
	 * @param existingWorld
	 * 		a path to the actual .world file
	 * 
	 * @return
	 * 		a world editor for editing the world
	 * 
	 */
	public static WorldEditor fromExisting(Path existingWorld) {
		return new WorldEditor();
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
		
		// TODO code
		throw new UnsupportedOperationException("Still need to code adding new screens! Can't create screen " + id);
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
	
	/**
	 * Resolves the 'paintbrush type' to the proper tile type, and from there extracts the graphics for the tile sprite
	 * sheet
	 * 
	 * @param currentTileType
	 * 		paintbrush type selected in the edtior
	 * 
	 * @return
	 * 		the sprite sheet from the world. Do not modify. If the current paintbrush is not set to a tile type, then
	 * 		this method will throw an exception. It is client responsbility to only invoke this method on proper
	 * 		paintbrushes
	 * 
	 * @throws
	 * 		IllegalArgumentException
	 * 			if the paintbrush type can not map to a tile type
	 * 
	 */
	public BufferedImage getTileSheetByType(TileType currentTileType) {
		return this.world.getTileSheetByType(currentTileType);
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
	
	
}
