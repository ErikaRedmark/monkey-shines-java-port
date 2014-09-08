package org.erikaredmark.monkeyshines.editor.importlogic;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.erikaredmark.monkeyshines.Goodie;
import org.erikaredmark.monkeyshines.ImmutablePoint2D;
import org.erikaredmark.monkeyshines.LevelScreen;
import org.erikaredmark.monkeyshines.Sprite;
import org.erikaredmark.monkeyshines.Tile;
import org.erikaredmark.monkeyshines.background.SingleColorBackground;
import org.erikaredmark.monkeyshines.editor.importlogic.WorldTranslationException.TranslationFailure;
import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.erikaredmark.monkeyshines.tiles.CommonTile;
import org.erikaredmark.monkeyshines.tiles.CommonTile.StatelessTileType;
import org.erikaredmark.monkeyshines.tiles.TileType;

/**
 * 
 * Handles translating the .plvl data from the original Monkey Shines. It is up to higher level
 * translators to provide the stream of just that data; this does not extract from .msh or other
 * file formats.
 * 
 * @author Erika Redmark
 *
 */
public class RsrcPlvlTranslator {

	/**
	 * 
	 * Translates the given stream for a {@code LevelScreen}. the stream pointer is advanced
	 * 1980 bytes (which it must have at least that much of or an exception will be thrown)
	 * <p/>
	 * See https://github.com/ErikaRedmark/monkey-shines-java-port/wiki/Original-Level-File-Format
	 * for a detailed analysis of the format.
	 * 
	 * @param is
	 * 		.plvl stream only
	 * 
	 * @param id
	 * 		id of the level screen. This id is not part of the binary data and is normally the id
	 * 		of the resource from the resource fork that the given data resided in
	 * 
	 * @param rsrc
	 * 		graphics resource for the level, required for interpretation of some data, as well as
	 * 		sanity checking
	 * 
	 * @param translationState
	 * 		a translation state object for information that can only be set after all levels are
	 * 		parsed.
	 * 
	 * @return
	 * 		instance of {@code LevelScreen}
	 * 
	 * @throws WorldTranslationException
	 * 		if the given stream does not have at least 122 bytes to work with, or the data is determined
	 * 		to be nonsensical in some way
	 * 
	 */
	public static LevelScreen translateLevel(InputStream is, int id, WorldResource rsrc, TranslationState translationState) throws WorldTranslationException, IOException {
		// Just to ease on the typing for this method.
		final TranslationFailure FAIL = TranslationFailure.WRONG_LEVEL_SIZE;
		
		// Read the number of sprites an items. Important as data MAY be garbage so just looking for zeros
		// is not sufficient.
		int spriteCount = TranslationUtil.readMacShort(is, FAIL, "Could not read number of sprites");
		int goodieCount = TranslationUtil.readMacShort(is, FAIL, "Could not read number of goodies");
		
		MSSpriteData[] sprites = MSSpriteData.arrayFromStream(is, spriteCount, rsrc);
		// MUST SKIP past zeros/garbage
		TranslationUtil.skip(is, (10 - spriteCount) * 20, FAIL, "Could not skip sprites");
		
		// Goodies are stored as sprites in the original game. For simplicity, we will read them as sprites
		// since the raw data is still stored and manipulate them as needed.
		MSSpriteData[] goodies = MSSpriteData.arrayFromStream(is, goodieCount, rsrc);
		TranslationUtil.skip(is, (25 - goodieCount) * 20, FAIL, "Could not skip goodies");
		
		// The big one, the level data.
		int[] levelData = TranslationUtil.readMacShortArray(is, 20 * 32, FAIL, "Could not read level data");
		
		int bonzoStartY = TranslationUtil.readMacShort(is, FAIL, "Could not read bonzo's starting location vertical");
		int bonzoStartX = TranslationUtil.readMacShort(is, FAIL, "Could not read bonzo's starting location horizontal");
		
		int ppat = TranslationUtil.readMacShort(is, FAIL, "Could not read ppat id");
		
		// ----------- Done reading stream. Time to interpret data ------------
		// Sprites
		List<Sprite> spritesOnScreen = new ArrayList<>(spriteCount);
		for (MSSpriteData spriteData : sprites) {
			Sprite s = Sprite.newSprite(spriteData.getSpriteId(), 
										spriteData.getPortLocation(), 
										spriteData.getPortBoundingBox(),
										spriteData.getPortVelocity(), 
										spriteData.getSpriteAnimationType(), 
										spriteData.getSpriteAnimationSpeed(), 
										spriteData.getSpriteType(), 
										spriteData.getPortDirection(), 
										rsrc);
			spritesOnScreen.add(s);
		}
		
		// Level
		Tile[][] tiles = new Tile[20][32];
		// This is kinda thrashing the cache, but the raw level data from the .plvl resource
		// IS stored on a column by column basis.
		int rawLvlIndex = 0;
		for (int j = 0; j < 32; ++j) {
			for (int i = 0; i < 20; ++i) {
				tiles[i][j] = translateToTile(levelData[rawLvlIndex], i, j, rsrc);
				++rawLvlIndex;
			}
		}
		
		// ------------------- Add to translation state, goodies and ppat
		for (MSSpriteData goodie : goodies) {
			ImmutablePoint2D location = 
				ImmutablePoint2D.of(goodie.location.x() / 20, (goodie.location.y() - 80) / 20);
			Goodie g = Goodie.newGoodie(Goodie.Type.byValue(goodie.id), 
										location, 
										id, 
										rsrc);
			
			translationState.addGoodie(g);
		}
		
		translationState.addPpatMapping(id, ppat);
		
		// --------------------- Ready to return
		
		return new LevelScreen(id,
							   // Temporary. Client should use translation state to later set to proper pattern
							   new SingleColorBackground(Color.BLACK), 
							   tiles, 
							   ImmutablePoint2D.of(bonzoStartX, bonzoStartY - 80), 
							   spritesOnScreen, 
							   rsrc);
		
	}

	/**
	 * 
	 * Translate raw level data taken from .plvl into a valid tile type.
	 * 
	 */
	private static Tile translateToTile(int data, int row, int col, WorldResource rsrc) {
		if (data == 0)  return Tile.emptyTile();
		TileType type = null;
		// 1 - 32
		if (data <= 32) {
			type = CommonTile.of(data, StatelessTileType.SOLID, rsrc);
			
		// 33 - 64
		} else if (data <= 64) {
			type = CommonTile.of(data - 33 , StatelessTileType.THRU, rsrc);
			
		// 65 - 96?
		} else if (data <= 96) {
			type = CommonTile.of(data - 65, StatelessTileType.SCENE, rsrc);
			
		// TODO handle other types
		} else {
			throw new UnsupportedOperationException("Translator cannot yet handle all tile types");
		}
		
		return Tile.newTile(ImmutablePoint2D.of(col, row), 
				 			type, 
				 			rsrc);
		
	}
	
}
