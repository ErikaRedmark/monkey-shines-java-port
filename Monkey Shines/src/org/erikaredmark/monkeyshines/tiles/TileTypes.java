package org.erikaredmark.monkeyshines.tiles;

import org.erikaredmark.monkeyshines.World;
import org.erikaredmark.monkeyshines.tiles.CommonTile.StatelessTileType;

/**
 * 
 * Static utility class for creating different types of {@code TileType} in a more general and easy
 * form. Typically client code will have some references to the type of tile stored in some persistent
 * format and will need to convert that form into an actual tile of some type.
 * 
 * @author Erika Redmark
 *
 */
public final class TileTypes {

	/**
	 * 
	 * Generates a solid tile of the given id
	 * 
	 * @param id
	 * @return
	 * 
	 */
	public static TileType solidFromId(int id) {
		return CommonTile.of(id, StatelessTileType.SOLID);
	}
	
	/**
	 * 
	 * Generates a thru tile of the given id
	 * 
	 * @param id
	 * @return
	 * 
	 */
	public static TileType thruFromId(int id) {
		return CommonTile.of(id, StatelessTileType.THRU);
	}
	
	/**
	 * 
	 * Generates a scene tile of the given id
	 * 
	 * @param id
	 * @return
	 * 
	 */
	public static TileType sceneFromId(int id) {
		return CommonTile.of(id, StatelessTileType.SCENE);
	}
	
	/**
	 * 
	 * Generates a hazard from the given id and world. The id refers to the type of hazard, and the world reference
	 * is needed to generate a tile in default state based on global hazard properties.
	 * <p/>
	 * This method may fail if the world does not have enough hazards. If this is being called from a context where it is
	 * possible the id is out of range, use {@code canHazardFromId(int, World)} first
	 * @param id
	 * @param world
	 * @return
	 * 
	 * @throws IndexOutOfBoundsException
	 * 		if the id of the hazard refers to a hazard that does not exist in the world
	 * 
	 */
	public static TileType hazardFromId(int id, World world) {
		return HazardTile.forHazard(world.getHazards().get(id) );
	}
	
	/**
	 * 
	 * Determines if a hazard can be created with the given id. If {@code true}, then {@code hazardFromId(int, World)} is
	 * guaranteed to succeed.
	 * 
	 * @param id
	 * @param world
	 * @return
	 * 		{@code true} if a hazard can be created with the given id for the given world, {@code false} if otherwise
	 * 
	 */
	public static boolean canHazardFromId(int id, World world) {
		int size = world.getHazards().size();
		return    id >= 0
			   && id < size;
	}
	
	/**
	 * 
	 * Generates a conveyer belt moving clockwise of the given id based on the given world properties. Note that
	 * conveyer ids refer to a set of conveyers.
	 * This method may fail if the world does not have enough conveyers. If this is being called from a context where it is
	 * possible the id is out of range, use {@code canConveyerFromId(int, World)} first
	 * 
	 * @param id
	 * @param world
	 * @return
	 * 
	 * @throws IndexOutOfBoundsException
	 * 		if the id of the conveyer refers to a conveyer set that does not exist in the world
	 * 
	 */
	public static TileType clockwiseConveyerFromId(int id, World world) {
		return new ConveyerTile(world.getConveyers().get(id * 2) );
	}
	
	/**
	 * 
	 * Generates a conveyer belt moving anti-clockwise of the given id based on the given world properties. Note that
	 * conveyer ids refer to a set of conveyers.
	 * 
	 * @param id
	 * @param world
	 * @return
	 * 
	 * @throws IndexOutOfBoundsException
	 * 		if the id of the conveyer refers to a conveyer set that does not exist in the world
	 * 
	 */
	public static TileType anticlockwiseConveyerFromId(int id, World world) {
		return new ConveyerTile(world.getConveyers().get( (id * 2) + 1) );
	}
	
	/**
	 * 
	 * Determines if a conveyer can be created with the given id. If {@code true}, then both conveyer creation functions
	 * (clockwise and anti-clockwise) are guaranteed to succeed.
	 * 
	 * @param id
	 * @param world
	 * @return
	 * 		{@code true} if a conveyer of either direction can be created with the given id for the given world, {@code false} if otherwise
	 * 
	 */
	public static boolean canConveyerFromId(int id, World world) {
		// Indexing is based on left/right, but id refers to the 'set'
		int size = (world.getConveyers().size() ) / 2;
		return    id >= 0
			   && id < size;
	}
	
	/**
	 * 
	 * Generates a collapsible tile from the given id
	 * 
	 * @param id
	 * @return
	 */
	public static TileType collapsibleFromId(int id) {
		return new CollapsibleTile(id);
	}
}
