package org.erikaredmark.monkeyshines.editor;

import org.erikaredmark.monkeyshines.World;
import org.erikaredmark.monkeyshines.tiles.CollapsibleTile;
import org.erikaredmark.monkeyshines.tiles.CommonTile;
import org.erikaredmark.monkeyshines.tiles.ConveyerTile;
import org.erikaredmark.monkeyshines.tiles.HazardTile;
import org.erikaredmark.monkeyshines.tiles.TileType;
import org.erikaredmark.monkeyshines.tiles.CommonTile.StatelessTileType;

/**
 * 
 * Static utility class for paintbrushes.
 * 
 * @author Erika Redmark
 *
 */
public final class PaintbrushUtils {
	
	private PaintbrushUtils() { }
	
	/**
	 * 
	 * Converts the paintbrush type to a paintable tile type. Basically, this takes the current brush creates
	 * a unique and enterable 'TileType' that can be directly placed into the world as a Tile.
	 * <p/>
	 * Because some tile types have state, the return type from this method should be used to populate at most 1
	 * tile in the world. Others should be copies of this.
	 * <p/>
	 * This method only works with the tile paintbrushes:
	 * {@code SOLIDS, THRUS, SCENES, HAZARDS, CONVEYERS_CLOCKWISE, CONVEYERS_ANTI_CLOCKWISE, COLLAPSIBLE}
	 * 
	 * @param paintbrush
	 * 		the brush currently set
	 * 
	 * @param currentTileId
	 * 		the current tile id
	 * 
	 * @param world
	 * 		the current world, needed for generating specialised tiles like hazards properly
	 * 
	 * @return
	 * 		a tile type that can be painted into the world. The return of this method should not be used to
	 * 		populate more than 1 location in the world without copying (or calling this method again)
	 * 
	 * @throws IllegalArgumentException
	 * 		if used with a brush that does not map to a tile (such as an eraser or sprite brush)
	 * 
	 */
	public static TileType paintbrush2TileType(PaintbrushType paintbrush, int currentTileId, World world) {
		TileType type = null;
		switch (paintbrush) {
		case SOLIDS:
			type = CommonTile.of(currentTileId, StatelessTileType.SOLID);
			break;
		case THRUS:
			type = CommonTile.of(currentTileId, StatelessTileType.THRU);;
			break;
		case SCENES:
			type = CommonTile.of(currentTileId, StatelessTileType.SCENE);;
			break;
		case HAZARDS:
			// Stateful type: Create new based on id. Properties of hazard will be based on World
			// properties.
			// This instance will NOT be added to the world itself!! It must be copied, or multiple hazards may
			// end up sharing state (like hitting one bomb will blow up every other bomb painted with the same
			// paintbrush).
			type = HazardTile.forHazard(world.getHazards().get(currentTileId) );
			break;
		case CONVEYERS_CLOCKWISE:
			// Resolve the Id of the conveyer to its location in our list
			type = new ConveyerTile(world.getConveyers().get(currentTileId * 2) );
			break;
		case CONVEYERS_ANTI_CLOCKWISE:
			// Same as above, but add 1 for the other direction
			type = new ConveyerTile(world.getConveyers().get((currentTileId * 2) + 1) );
			break;
		case COLLAPSIBLE:
			// Stateful, but easy to create.
			type = new CollapsibleTile(currentTileId);
			break;
		default:
			throw new IllegalArgumentException("Paintbrush type " + paintbrush.toString() + " not a valid paintbrush type for tiles");
		}
		
		assert type != null;
		return type;
	}
}
