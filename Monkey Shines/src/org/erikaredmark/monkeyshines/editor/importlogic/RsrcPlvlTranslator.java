package org.erikaredmark.monkeyshines.editor.importlogic;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.erikaredmark.monkeyshines.Conveyer;
import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.Goodie;
import org.erikaredmark.monkeyshines.ImmutablePoint2D;
import org.erikaredmark.monkeyshines.LevelScreen;
import org.erikaredmark.monkeyshines.Sprite;
import org.erikaredmark.monkeyshines.TileMap;
import org.erikaredmark.monkeyshines.background.SingleColorBackground;
import org.erikaredmark.monkeyshines.editor.importlogic.WorldTranslationException.TranslationFailure;
import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.erikaredmark.monkeyshines.tiles.CollapsibleTile;
import org.erikaredmark.monkeyshines.tiles.CommonTile;
import org.erikaredmark.monkeyshines.tiles.CommonTile.StatelessTileType;
import org.erikaredmark.monkeyshines.tiles.PlaceholderTile;
import org.erikaredmark.monkeyshines.tiles.TileType;

/**
 * 
 * Handles translating the .plvl data from the original Monkey Shines. It is up to higher level
 * translators to provide the stream of just that data; this does not extract from .msh or other
 * file formats.
 * <p/>
 * This has a majour limitation; due to a mistake early on in the project, levels in the port increment by 100 when
 * going up... the original DECREMENTED by 100 going up, and vice-versa. There is a fudge factour; level ids
 * are rounded to the nearest hundreth. The difference between that value and 1000, the base, is then applied oppositely
 * and the original subtracted tens and ones place restored. For instance:
 * {@code
 * 		Original: Level 987
 * 		Port: 987 == rounds up to 1000, difference of 0. Same level as 1000
 * 
 *  	Original: Level 797
 *  	Port: 797 == rounds up to 800, difference of 200. 1000 + 200 = 1200. Put back tens and ones place for 1297.
 * }
 * 
 * @author Erika Redmark
 *
 */
public class RsrcPlvlTranslator {
	private static final String CLASS_NAME = "org.erikaredmark.monkeyshines.editor.importlogic.RsrcPlvlTranslator";
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
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
	 * 		of the resource from the resource fork that the given data resided in. This is the ORIGINAL
	 * 		id, NOT the id the level should take on in the port.
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
	 * 		instance of {@code LevelScreen}. be sure to use the returned level-screens id over the passed
	 * 		one as the returned value is calibrated for the port.
	 * 
	 * @throws WorldTranslationException
	 * 		if the given stream does not have at least 122 bytes to work with, or the data is determined
	 * 		to be nonsensical in some way
	 * 
	 */
	public static LevelScreen translateLevel(InputStream is, int id, WorldResource rsrc, TranslationState translationState) throws WorldTranslationException, IOException {
		LOGGER.info(CLASS_NAME + ": Beginning Translation of level " + id);
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
		LOGGER.info(CLASS_NAME + ": Stream reading done. Beginning data interpretation starting with sprites");
		
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
										spriteData.getTwoFacing(),
										rsrc);
			spritesOnScreen.add(s);
		}
		
		// Level
		LOGGER.info(CLASS_NAME + ": Reading tile data");
		TileMap map = new TileMap(GameConstants.LEVEL_ROWS, GameConstants.LEVEL_COLS);
		// This is kinda thrashing the cache, but the raw level data from the .plvl resource
		// IS stored on a column by column basis. Given how rare this code will be called I don't see
		// it being worth it to optimise
		int rawLvlIndex = 0;
		for (int j = 0; j < 32; ++j) {
			for (int i = 0; i < 20; ++i) {
				map.setTileRowCol(i, j, translateToTile(levelData[rawLvlIndex], i, j, rsrc));
				++rawLvlIndex;
			}
		}
		// Create Id translation
		int portId = invertLevelId(id);
		LOGGER.info("Translated original Id " + id + " into " + portId);
		
		// ------------------- Add to translation state, goodies and ppat
		// Goodies are also stored in tile data... but I discovered that after I wrote this. Should give the same
		// results anyway.
		for (MSSpriteData goodie : goodies) {
			ImmutablePoint2D location = 
				ImmutablePoint2D.of(goodie.location.x() / 20, (goodie.location.y() - 80) / 20);
			Goodie g = Goodie.newGoodie(Goodie.Type.byValue(goodie.id), 
										location, 
										portId, 
										rsrc);
			
			translationState.addGoodie(g);
		}
		
		translationState.addPpatMapping(portId, ppat);
		// --------------------- Ready to return
		
		return new LevelScreen(portId,
							   // Temporary. Client should use translation state to later set to proper pattern
							   new SingleColorBackground(Color.BLACK), 
							   map, 
							   ImmutablePoint2D.of(bonzoStartX / GameConstants.TILE_SIZE_X, (bonzoStartY - 80) / GameConstants.TILE_SIZE_Y), 
							   spritesOnScreen, 
							   rsrc);
		
	}
	
	/**
	 * 
	 * Takes a level id referring to an original level, and inverts it to correspond to the port. Port goes +100 up, -100 down,
	 * the original was backwards. This must be done to all levels and any data referring to levels, such as bonus door screen.
	 * 
	 * @param id
	 * 		original level id
	 * 
	 * @return
	 * 		inverted id for port
	 * 
	 * @throws WorldTranslationException
	 * 		if the id cannot be inverted. This is unlikely to happen, but this method should only ever be
	 * 		used in the context of throwing that exception anyway
	 * 
	 */
	static int invertLevelId(int id)  throws WorldTranslationException {
		int tensAndOnes = id % 100;
		if (tensAndOnes > 47 && tensAndOnes < 53) {
			LOGGER.severe("Original Level id " + id + " may be improperly translated: it lies on the edge of the translation threshold for fixing the level id inversion in the port. Double check this level will appear where expected.");
		}
		
		// add 50 to turn truncation into rough rounding
		int rounded = ((id + 50) / 100) * 100;
		int portId = Integer.MAX_VALUE;
		// Yes, this totally brings the bonus levels into negative values. The bonus room id is given the same inversion treatment. However, the port can
		// handle negative values just fine.
		if (rounded == 1000) {
			portId = id;
		} else {
			if (id < rounded) {
				// Less than rounded means the 100's place is going to be offset by 100, hence 900 instead of 1000
				portId = 900 + (1000 - rounded) + tensAndOnes;
			} else {
				portId = 1000 + (1000 - rounded) + tensAndOnes;
			}
		}
		
		if (portId == Integer.MAX_VALUE) {
			throw new WorldTranslationException(TranslationFailure.TRANSLATOR_SPECIFIC, "Could not determine port id for level id " + id);
		}
		
		return portId;
	}

	/**
	 * 
	 * Translate raw level data taken from .plvl into a valid tile type.
	 * Hazards and conveyers cannot be set now because the entire world must be parsed first before
	 * they are generated. {@code PlaceholderTile} instances may be returned, with enough metadata to eventually be changed.
	 * 
	 */
	private static TileType translateToTile(int data, int row, int col, WorldResource rsrc)  throws WorldTranslationException {
		if (data == 0)  return CommonTile.NONE;
		
		TileType type = null;
		if (data <= 0x0020) {
			// -1 for the 0 being the Null tile
			type = CommonTile.of(data - 1, StatelessTileType.SOLID, rsrc);
		} else if (data <= 0x0050) {
			type = CommonTile.of(data - 0x0021 , StatelessTileType.THRU, rsrc);
		} else if (data <= 0x0090) {
			type = CommonTile.of(data - 0x0051, StatelessTileType.SCENE, rsrc);
		} else if (data <= 0x00A0) {
			type = PlaceholderTile.hazard(data - 0x0091);
		} else if (data == 0x00A1) {
			type = PlaceholderTile.conveyer(0, Conveyer.Rotation.ANTI_CLOCKWISE);
		} else if (data == 0x00A2) {
			type = PlaceholderTile.conveyer(1, Conveyer.Rotation.ANTI_CLOCKWISE);
		} else if (data == 0x00A9) {
			type = PlaceholderTile.conveyer(0, Conveyer.Rotation.CLOCKWISE);
		} else if (data == 0x00AA) {
			type = PlaceholderTile.conveyer(1, Conveyer.Rotation.CLOCKWISE);
		} else if (data == 0x00B1) {
			type = new CollapsibleTile(0);
		} else if (data == 0x00B2) {
			type = new CollapsibleTile(1);
		} else if (data >= 0x00C1 && data <= 0x00CF) {
			// We know what these values mean, we just don't care. Goodie data is stored in two different places and it
			// matters not which one we read from.
			LOGGER.fine(CLASS_NAME + ": skipping goodie from tile-data (should be picked up from earlier MSSpriteData array)");
			return CommonTile.NONE;
		} else if (data >= 0x00D0 && data <= 0x00EF) {
			// Scenes are stored also at the D range. Some levels have a lot of scenery and expand into this range for it.
			// 0 base the data, but also start it on the next round of 64 values.
			type = CommonTile.of( (data - 0x00D0) + 0x003F, StatelessTileType.SCENE, rsrc);
		} else {
			LOGGER.severe("Level data at row " 
						+ row 
						+ " col " 
						+ col 
						+ " contains " 
						+ Integer.toHexString(data) 
						+ " which is not a known original game level value and maps to no known tiles. Leaving as Blank");
						
			return CommonTile.NONE;
		}
		
		return type;
	}
	
}
