package edu.nova.erikaredmark.monkeyshines.tiles;

import java.io.Serializable;

/**
 * 
 * Marker interface for indicating classes operate as a 'tile' in the game world. Some tile types are stateless; these
 * are implemented as enumerations. Other types may carry with them some intrinsic state, that is either supplied on
 * creation and never changed or can be changed during course of gameplay.
 * 
 * @author Erika Redmark
 *
 */
public interface TileType extends Serializable {

}
