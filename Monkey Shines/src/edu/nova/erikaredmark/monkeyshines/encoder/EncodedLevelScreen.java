package edu.nova.erikaredmark.monkeyshines.encoder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.nova.erikaredmark.monkeyshines.ImmutablePoint2D;
import edu.nova.erikaredmark.monkeyshines.LevelScreen;
import edu.nova.erikaredmark.monkeyshines.Tile;

/**
 * A serialisable class that maintains the static state data of everything required to recreate a level. This includes
 * the following things:
 * <br/>
 * <ul>
 * <li> Id of the screen </li>
 * <li> all tiles on the screen</li>
 * <li> all sprites on the screen</li>
 * <li> location bonzo starts </li>
 * </ul>
 * Please see {@link EncodedSprite} and {@link EncodedTile} for documentation on those objects
 * <p/>
 * <strong> Only these proxy classes are serialised.</strong>. The regular classes are free to evolve as long as no
 * changes are made to the static contents of the level.
 * <p/>
 * Instances of this class are immutable.
 * 
 * @author Erika Redmark
 */
public final class EncodedLevelScreen implements Serializable {
	private static final long serialVersionUID = 4408117657277863512L;
	
	private final int id;
	private final int backgroundId;
	private final EncodedTile[][] tiles;
	private final List<EncodedSprite> sprites;
	private final ImmutablePoint2D bonzoLocation;
	
	private EncodedLevelScreen(final int id, final int backgroundId, final EncodedTile[][] tiles, final List<EncodedSprite> sprites, final ImmutablePoint2D bonzoLocation) {
		this.id = id; this.backgroundId = backgroundId; this.tiles = tiles; this.sprites = sprites; this.bonzoLocation = bonzoLocation;
	}
	
	public static EncodedLevelScreen from(LevelScreen level) {
		final int _id = level.getId();
		
		final int _backgroundId = level.getBackgroundId();
		
		final Tile[][] tiles =
			level.internalGetTiles();
		
		final EncodedTile[][] _tiles =
			new EncodedTile[tiles.length][tiles[0].length];
		
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[0].length; j++) {
				_tiles[i][j] = EncodedTile.from(tiles[i][j]);
			}
		}
		
		final List<EncodedSprite> _sprites = EncodedSprite.fromAll(level.getSpritesOnScreen() );
		
		final ImmutablePoint2D _bonzoLocation = level.getBonzoStartingLocation();
		
		return new EncodedLevelScreen(_id, _backgroundId, _tiles, _sprites, _bonzoLocation);
	}
	
	/**
	 * 
	 * Creates an empty encoded level screen with the given id. Differs from {@link LevelScreen#newScreen(int, WorldResource)}
	 * since there is no need for a graphics resource to skin the level as it is created in a saveable encoded form
	 * 
	 * @param id
	 * 		the id of the screen. For creating new worlds from scratch, this should generally be id 1000
	 * 
	 * @return
	 * 		fresh, empty encoded level with the given id.
	 * 
	 */
	public static EncodedLevelScreen fresh(int id) {
		// Note: cannot use Collections.emptyList, as that list is an empty 'immutable' list, not a fresh list
		// 		 that new things can be added to, which is what this screen will need.
		return new EncodedLevelScreen(id, 0, EncodedTile.freshTiles(), new ArrayList<EncodedSprite>(), ImmutablePoint2D.of(0, 0) );
	}

	public int getId() { return id; }
	public int getBackgroundId() { return backgroundId; }
	public EncodedTile[][] getTiles() { return tiles; }
	public List<EncodedSprite> getSprites() { return sprites; }
	public ImmutablePoint2D getBonzoLocation() { return bonzoLocation; }



}
