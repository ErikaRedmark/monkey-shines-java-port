package edu.nova.erikaredmark.monkeyshines.encoder;

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
public final class EncodedLevelScreen {
	
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
			for (int j = 0; j < tiles[0].length; i++) {
				_tiles[i][j] = EncodedTile.from(tiles[i][j]);
			}
		}
		
		final List<EncodedSprite> _sprites = EncodedSprite.fromAll(level.getSpritesOnScreen() );
		
		final ImmutablePoint2D _bonzoLocation = level.getBonzoStartingLocation();
		
		return new EncodedLevelScreen(_id, _backgroundId, _tiles, _sprites, _bonzoLocation);
	}

	public int getId() { return id; }
	public int getBackgroundId() { return backgroundId; }
	public EncodedTile[][] getTiles() { return tiles; }
	public List<EncodedSprite> getSprites() { return sprites; }
	public ImmutablePoint2D getBonzoLocation() { return bonzoLocation; }

}
