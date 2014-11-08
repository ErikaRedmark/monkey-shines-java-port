package org.erikaredmark.monkeyshines.tiles;

import java.awt.Graphics2D;

import org.erikaredmark.monkeyshines.Conveyer;
import org.erikaredmark.monkeyshines.resource.WorldResource;

/**
 * 
 * A very special type of tile that is {@code TileType} in name only. Calling any {@code TileType} methods
 * will cause an exception. It is ONLY intended to hold specific metadata to be converted to a valid tile
 * at a later time. This is intended for translating old level files from the original game; the actual
 * Hazard and Conveyer instances cannot be created without parsing the World, and parsing the World cannot
 * be completed without parsing the levels (based on the order the translators may work). Given this,
 * tiles are set with placeholders during translation, and after enough World information has been parsed
 * from the file, {@code World.fixPlaceholders() } should be called to create
 * valid instances.
 * <p/>
 * This is a very specific use-case and tiles of this type are not seen outside of old-file-format translators,
 * and consequently are not used anywhere else. If using them, remember that after more information becomes
 * available, the proper method in {@code World} should be called to fix them up.
 * <p/>
 * Instances of this class are immutable.
 * 
 * @author Erika Redmark
 *
 */
public class PlaceholderTile implements TileType {
	private static final String ERROR = "Placeholder tiles should not be used during gameplay or editing!!!";
	/** Call {@link #getMetaId() } **/
	@Override public int getId() { throw new UnsupportedOperationException(ERROR); }
	@Override public boolean isThru() { throw new UnsupportedOperationException(ERROR); }
	@Override public boolean isSolid() { throw new UnsupportedOperationException(ERROR); }
	@Override public boolean isLandable() { throw new UnsupportedOperationException(ERROR); }
	@Override public void update() { throw new UnsupportedOperationException(ERROR); }
    @Override public void reset() { throw new UnsupportedOperationException(ERROR); }
    @Override public PlaceholderTile copy() { throw new UnsupportedOperationException(ERROR); }
    @Override public void paint(Graphics2D g2d, int drawToX, int drawToY, WorldResource rsrc) { throw new UnsupportedOperationException(ERROR); }
    
    // Not from getId(). Technically this placeholder doesn't have a true id.
    private final int id;
    private final Type type;
    
    private PlaceholderTile(final int id, final Type type) {
    	if (id < 0)  throw new IllegalArgumentException("Id of " + id + " is not valid meta-data");
    	
    	this.id = id;
    	this.type = type;
    }
    
    public static PlaceholderTile hazard(int id) {
    	return new PlaceholderTile(id, Type.HAZARD);
    }
    
    public static PlaceholderTile conveyer(int id, Conveyer.Rotation rotation) {
    	return   rotation == Conveyer.Rotation.ANTI_CLOCKWISE
    		   ? new PlaceholderTile(id, Type.CONVEYER_ANTI_CLOCKWISE)
    		   : new PlaceholderTile(id, Type.CONVEYER_CLOCKWISE);
    }
    
    /**
     * The type of the placeholder to determine whether this should become a hazard or a conveyer.
     */
    public Type getType() {
    	return type;
    }
    
    /** Returns the meta id of this placeholder. This should refer to the index in the array of
     *  hazards, or a starting index for the array of conveyers (augmented by rotation)
     *  <p/>
     *  Hazards: Easy. Refers directly to index of list
     *  <p/>
     *  Conveyers: Multiply by two to get index. Add 1 if the conveyer is anti-clockwise.
     */
    public int getMetaId() {
    	return id;
    }
    
    public enum Type {
    	HAZARD,
    	CONVEYER_ANTI_CLOCKWISE,
    	CONVEYER_CLOCKWISE;
    }
    
    @Override public String toString() {
    	return "Placeholder tile of id " + id + " holding for type " + type;
    }

}
