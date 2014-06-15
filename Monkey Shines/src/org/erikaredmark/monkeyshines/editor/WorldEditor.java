package org.erikaredmark.monkeyshines.editor;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.erikaredmark.monkeyshines.Conveyer;
import org.erikaredmark.monkeyshines.Goodie;
import org.erikaredmark.monkeyshines.Hazard;
import org.erikaredmark.monkeyshines.ImmutablePoint2D;
import org.erikaredmark.monkeyshines.LevelScreen;
import org.erikaredmark.monkeyshines.Sprite;
import org.erikaredmark.monkeyshines.World;
import org.erikaredmark.monkeyshines.encoder.EncodedWorld;
import org.erikaredmark.monkeyshines.resource.WorldResource;

import com.google.common.collect.ImmutableList;

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
		World world = encoded.newWorldInstance(rsrc);
		
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
		boolean couldChange = world.changeCurrentScreen(editor.getId() );
		if (!(couldChange) ) {
			System.err.println("Could not change screen to " + editor.getId() );
		}
	}
	
	/**
	 * 
	 * Determines, if any, all the screens in the world containing bonus doors, and returns a special object
	 * designed to represent a pairing of bonus doors in such a way that is easy to work with from the level
	 * editor and indicates any state information regarding if the bonus doors are set up properly.
	 * 
	 * @return
	 * 		bonus room object indicating the status of the bonus rooms in this world
	 * 
	 */
	public BonusRoomPair bonusRooms() {
		// Initial size assumes not too many bonus rooms and maybe some leeway
		// for accidentally adding too many bonus doors
		List<Integer> bonusRooms = new ArrayList<>(4);
		for (LevelScreenEditor screen : levelScreenEditors.values() ) {
			for (Sprite s : screen.getSpritesOnScreen() ) {
				if (s.getType() == Sprite.SpriteType.BONUS_DOOR) {
					bonusRooms.add(screen.getId() );
					break; // Skip other sprites on this level
				}
			}
		}
		
		// Break list into firstRoom, secondRoom, otherRooms, in that order, leaving nulls
		// when out of entires. This will allow BonusRoomPair to operate effectively.
		Integer firstRoom =   bonusRooms.size() > 0
							? bonusRooms.get(0)
							: null;
							
		Integer secondRoom =   bonusRooms.size() > 1
							 ? bonusRooms.get(1)
							 : null;
							 
		int[] otherRooms =   bonusRooms.size() > 2
						   ? new int[bonusRooms.size() - 2]
						   : null;
						   
		if (otherRooms != null) {
			for (int i = 0; i < otherRooms.length; i++) {
				otherRooms[i] = bonusRooms.get(i + 2);
			}
		}
		
		return new BonusRoomPair(firstRoom, secondRoom, otherRooms);
	}
	
	public void paint(Graphics2D g) {
		world.paintAndUpdate(g);
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
	 * Forwards call to {@link World$getConveyers() }
	 * Conveyer belts are auto-generated by the world based on the graphics resource,
	 * hence there is no explicit set command.
	 */
	public List<Conveyer> getConveyers() {
		return world.getConveyers();
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

	/**
	 * 
	 * Returns the underlying world being edited
	 * 
	 * @return
	 * 		world being edited
	 * 
	 */
	public World getWorld() { return this.world; }
	
	/**
	 * 
	 * Represents a pair of rooms that contain bonus doors. This also indicates proper
	 * state (if exactly two rooms in the world contain bonus doors). The state of this
	 * object must be checked before accessing values.
	 * <p/>
	 * Instances of this class are immutable
	 * 
	 * @author Erika Redmark
	 *
	 */
	public static class BonusRoomPair {
		// Use Integer as it has the null state, which is possible.
		private final Integer firstRoom;
		private final Integer secondRoom;
		
		// If this is non-null, we have a problem
		private final int[] otherRooms;
		
		/**
		 * 
		 * Constructs a new instance that pairs multiple rooms with bonus doors. Unlike many other
		 * classes, this can expect {@code null} as a valid parameter for ALL arguments, which would
		 * indicate no bonus doors.
		 * 
		 * @param firstRoom
		 * 
		 * @param secondRoom
		 * 
		 * @param otherRooms
		 * 
		 */
		private BonusRoomPair(final Integer firstRoom, final Integer secondRoom, final int[] otherRooms) {
			this.firstRoom = firstRoom;
			this.secondRoom = secondRoom;
			this.otherRooms = otherRooms;
		}

		/**
		 * 
		 * Returns if both first and second rooms are non null AND there are no additional rooms. This is
		 * bascially the primary status determiner: if this is {@code true} the object is valid.
		 * 
		 * @return
		 * 		{@code true} if this pair has exactly two bonus rooms, {@code false} if otherwise
		 * 
		 */
		public boolean hasTwo() {
			return firstRoom != null && secondRoom != null && otherRooms == null;
		}

		/**
		 * 
		 * Returns if there are no bonus rooms anywhere, indicating an absence of bonus doors
		 * 
		 * @return
		 * 		{@code true} if no rooms anywhere have bonus doors in this world, {@code false} if
		 *		otherwise
		 * 
		 */
		public boolean hasNone() {
			return firstRoom == null && secondRoom == null && otherRooms == null;
		}

		/**
		 * 
		 * Returns if this pair contains only one room with bonus doors
		 * 
		 * @return
		 * 		{@code true} if only one room has bonus doors in the world, {@code false} if 
		 * 		otherwise
		 * 
		 */
		public boolean hasOnlyOne() {
			return firstRoom != null && secondRoom == null && otherRooms == null;
		}

		/**
		 * 
		 * Returns string form containing all rooms in this pair. An empty string is returned if
		 * the pair contains no rooms
		 * 
		 * @return
		 * 		string form
		 * 
		 */
		public String getAllAsString() {
			StringBuilder builder = new StringBuilder(50);
			if (firstRoom != null)  builder.append(firstRoom).append(" ");
			if (secondRoom != null) builder.append(secondRoom).append(" ");
			if (otherRooms != null) {
				for (int id : otherRooms) {
					builder.append(id).append(" ");
				}
			}
			return builder.toString();
		}

		/**
		 * Returns first room. MAY be {@code null} if this pair is empty
		 * @return first room
		 */
		public Integer first() { return this.firstRoom; }
		
		/**
		 * Returns second room. MAY be {@code null} if this pair is empty or only contains one room
		 * @return second room
		 */
		public Integer second() { return this.secondRoom; }

		/**
		 * 
		 * Determines if this pair contains the given screen as EITHER a first or second room. It
		 * does not check the other rooms, as that is indicative of an error. This method should
		 * only be used when this object is in valid state (hasTwo returns {@code true} )
		 * 
		 * @param bonusRoom
		 * 		the room expected to contain a bonus door
		 * 
		 * @return
		 * 		{@code true} if that room does indeed contain a bonus door as this pair indicates,
		 * 		{@code false} if otherwise.
		 * 
		 */
		public boolean containsScreen(int bonusRoom) {
			assert hasTwo();
			if (firstRoom.equals(bonusRoom) )  return true;
			if (secondRoom.equals(bonusRoom) ) return true;
			
			return false;
		}

		/**
		 * 
		 * Requires this object be in valid state (hasTwo returns {@code true}. Given one screen id of where
		 * bonus doors are, determines what the other room is. This is used for determining which room is the actual
		 * bonus room vs which room is the return room.
		 * <p/>
		 * It is an error to call this when {@code containsScreen(bonusRoom)} would otherwise return false.
		 * 
		 * @param bonusRoom
		 * 		bonus room
		 * 
		 * @return
		 */
		public int getOther(int bonusRoom) {
			assert containsScreen(bonusRoom);
			if (firstRoom.equals(bonusRoom) )  return secondRoom;
			else							   return firstRoom;
		}
	}

}
