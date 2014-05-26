package edu.nova.erikaredmark.monkeyshines.tiles;

/**
 * 
 * How the tile reacts to the game world. Some tile types are stateless; their nature represents all that needs to be known
 * about a tile at a given point. Other types may need to be constructed with some state data.
 * <p/>
 * 
 * <p/>
 * <strong> Stateless</strong>
 * <ul>
 * <li> Solid: Can not be passed. Bonzo may stand on it, but can not otherwise go through it. There is one exception:
 * 		if bonzo starts a screen inside of a solid, he may move out of it (and thus through it) but not back into it
 * 		again.</li>
 * <li> Thru: Bonzo may stand on it, but if he is walking through it on the side it will not impede his movement.
 * 		He may not, however, go down through it (it is standable only)</li>
 * <li> Scene: Has no effect on Bonzo. Has no effect on anything. Merely a stand-in for graphics. </li>
 * <li> Conveyer Left: Conveyer belt moving left. Moves Bonzo left if he is standing on it.</li>
 * <li> Conveyer Right: Moves bonzo right if he is standing on it</li>
 * <li> None: No tile. Acts as a null-safe way of simply saying "no tile" </li>
 * </ul>
 * 
 * @author Erika Redmark
 *
 */
public enum StatelessTileType implements TileType {
	SOLID {
		@Override public boolean isThru() { return false; }
		@Override public void update() { /* No op */ }
	},
	THRU {
		@Override public boolean isThru() { return true; }
		@Override public void update() { /* No op */ }
	}, 
	SCENE {
		@Override public boolean isThru() { return false; }
		@Override public void update() { /* No op */ }
	},
	NONE {
		@Override public boolean isThru() { return false; }
		@Override public void update() { /* No op */ }
	};
}
