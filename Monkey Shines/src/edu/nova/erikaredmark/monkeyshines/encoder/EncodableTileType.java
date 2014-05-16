package edu.nova.erikaredmark.monkeyshines.encoder;

import java.io.Serializable;

/**
 * 
 * Represents a tile type that can be serialised. Tile types that are enums naturally can do this; otherwise a more
 * specialised type is needed.
 * 
 * @author Erika Redmark
 *
 */
public interface EncodableTileType extends Serializable {
	
}
