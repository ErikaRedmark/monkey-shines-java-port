package org.erikaredmark.monkeyshines.encoder;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.erikaredmark.monkeyshines.AnimationSpeed;
import org.erikaredmark.monkeyshines.AnimationType;
import org.erikaredmark.monkeyshines.Conveyer;
import org.erikaredmark.monkeyshines.DeathAnimation;
import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.Goodie;
import org.erikaredmark.monkeyshines.Hazard;
import org.erikaredmark.monkeyshines.ImmutablePoint2D;
import org.erikaredmark.monkeyshines.ImmutableRectangle;
import org.erikaredmark.monkeyshines.LevelScreen;
import org.erikaredmark.monkeyshines.Sprite;
import org.erikaredmark.monkeyshines.Tile;
import org.erikaredmark.monkeyshines.World;
import org.erikaredmark.monkeyshines.Conveyer.Rotation;
import org.erikaredmark.monkeyshines.Sprite.SpriteType;
import org.erikaredmark.monkeyshines.background.Background;
import org.erikaredmark.monkeyshines.background.FullBackground;
import org.erikaredmark.monkeyshines.background.SingleColorBackground;
import org.erikaredmark.monkeyshines.bounds.Boundable;
import org.erikaredmark.monkeyshines.bounds.IPoint2D;
import org.erikaredmark.monkeyshines.encoder.exception.WorldRestoreException;
import org.erikaredmark.monkeyshines.encoder.exception.WorldSaveException;
import org.erikaredmark.monkeyshines.encoder.proto.WorldFormatProtos;
import org.erikaredmark.monkeyshines.encoder.proto.WorldFormatProtos.World.BackgroundType;
import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.erikaredmark.monkeyshines.tiles.CollapsibleTile;
import org.erikaredmark.monkeyshines.tiles.CommonTile;
import org.erikaredmark.monkeyshines.tiles.ConveyerTile;
import org.erikaredmark.monkeyshines.tiles.HazardTile;
import org.erikaredmark.monkeyshines.tiles.TileType;
import org.erikaredmark.monkeyshines.tiles.CommonTile.StatelessTileType;

/**
 *
 * A facade class that provides a mapping between the in-memory world of running the game and
 * the level editor, vs the persistent world of the google protocol buffer format. Instances
 * of this class are initialised either from the Java object model (starting with {@link World})
 * or a {@code .world} file. Once initialised, it is possible to either create new instances of
 * {@code World} or save to/over instance of .world files on disk. 
 * <p/>
 * Instances of this class are immutable. This class is designed to be created from one form, so it
 * may produce a world of the counter-form.
 * 
 * @author Erika Redmark
 * 
 */
public final class EncodedWorld {

	private final WorldFormatProtos.World world;
	
	private EncodedWorld(final WorldFormatProtos.World world) {
		this.world = world;
	}
	
	/**
	 * 
	 * Creates the encoded version from the world. The encoded version will maintain only
	 * the immutable state of the world, and can be easily written to disk.
	 * 
	 * @param world
	 * 		world to encode
	 * 
	 * @return
	 * 		new instance of this object
	 * 
	 */
	public static EncodedWorld fromMemory(final World world) {
		WorldFormatProtos.World.Builder diskWorld = WorldFormatProtos.World.newBuilder();
		
		diskWorld.setAuthor(world.getAuthor() );
		diskWorld.setName(world.getWorldName() );
		diskWorld.setBonusScreen(world.getBonusScreen() );
		// Intentionally omitted; return screen auto-calculated from the first bonus door bonzo touches.
		//diskWorld.setReturnScreen(world.getReturnScreen() );
		diskWorld.addAllHazards(hazardsToProto(world.getHazards() ) );
		diskWorld.addAllGoodies(goodiesToProto(world.getGoodies() ) );
		diskWorld.addAllLevels(levelsToProto(world.getLevelScreens() ) );
		
		return new EncodedWorld(diskWorld.build() );
	}
	
	/**
	 * 
	 * Creates an encoded version from the given .world file. Once created, a new instance
	 * of a {@code World} can be easily generated from this encoded form. If any changes are
	 * made, a new instance of this object will need to be created via {@code fromMemory(World)}
	 * in order to save it.
	 * <p/>
	 * This method does not close the stream after using it.
	 * 
	 * @param stream
	 * 		properly encoded .world stream, typically from a file, to load
	 * 
	 * @return
	 * 		new instance of this object
	 * 
	 */
	public static EncodedWorld fromStream(final InputStream stream) throws WorldRestoreException {
		try {
			WorldFormatProtos.World temp = WorldFormatProtos.World.parseFrom(stream);
			return new EncodedWorld(temp);
		} catch (IOException e) {
			throw new WorldRestoreException(e);
		}
	}
	

	/**
	 * 
	 * Saves the given world to the given stream. Stream is left open after method ends.
	 * 
	 * @param out
	 * 		output stream to write to
	 * 
	 */
	public void save(OutputStream out) throws WorldSaveException {
		try {
			world.writeTo(out);
		} catch (IOException e) {
			throw new WorldSaveException(e);
		}
	}
	
	/**
	 * 
	 * Creates a new world instance for use with the game or level editor that duplicates the
	 * immutable state stored in the encoded form. Each call to this method returns a new object.
	 * 
	 * @param rsrc
	 * 		graphics resource, required to inflate objects that require access to graphics
	 * 		and sound resources
	 * 
	 * @return
	 * 		new world instance from encoded form
	 * 
	 */
	public World newWorldInstance(final WorldResource rsrc) {
		final String worldName = world.getName();
		final Map<String, Goodie> goodiesInWorld = protoToGoodies(world.getGoodiesList(), rsrc);
		final List<Hazard> hazards = protoToHazards(world.getHazardsList() );
		final int bonusScreen = this.world.getBonusScreen();
		
		// Intentionally omitted; return screen auto-calculated from the first bonus door bonzo touches.
		// final int returnScreen = this.world.getReturnScreen();
		
		// Size of conveyers may be added to if the world is skinned with an updated
		// resource containing new conveyers.
		// generate as many conveyer instances as graphics resource allows. Decoding of the
		// actual levels with the auto-generated conveyers will handle conveyer belts.
		List<Conveyer> conveyers = new ArrayList<>(rsrc.getConveyerCount() * 2 );
		World.generateConveyers(conveyers, rsrc.getConveyerCount() );
		
		// Finally, all the different kinds of tiles loaded, we can load the actually tilemap that requires references
		// to those tiles
		final Map<Integer, LevelScreen> worldScreens = protoToLevels(world.getLevelsList(), rsrc, hazards, conveyers);
		
		return new World(worldName, goodiesInWorld, worldScreens, hazards, conveyers, bonusScreen, rsrc);	
	}
	
	/* ------------------------------ Hazards -------------------------------- */
	static List<WorldFormatProtos.World.Hazard> hazardsToProto(List<Hazard> hazards) {
		List<WorldFormatProtos.World.Hazard> protoHazards = new ArrayList<>(hazards.size() );
		for (Hazard h : hazards) {
			protoHazards.add(hazardToProto(h) );
		}
		return protoHazards;
	}
	
	static WorldFormatProtos.World.Hazard hazardToProto(Hazard hazard) {
		WorldFormatProtos.World.Hazard.Builder protoHazard = WorldFormatProtos.World.Hazard.newBuilder();
		protoHazard.setId(hazard.getId() );
		protoHazard.setExplodes(hazard.getExplodes() );
		protoHazard.setDeathAnimation(deathAnimationToProto(hazard.getDeathAnimation() ) );
		return protoHazard.build();
	}
	
	static List<Hazard> protoToHazards(List<WorldFormatProtos.World.Hazard> protoHazards) {
		List<Hazard> hazards = new ArrayList<>(protoHazards.size() );
		for (WorldFormatProtos.World.Hazard h : protoHazards) {
			hazards.add(protoToHazard(h) );
		}
		return hazards;
	}
	
	static Hazard protoToHazard(WorldFormatProtos.World.Hazard protoHazard) {
		return new Hazard(protoHazard.getId(), 
						  protoHazard.getExplodes(), 
						  protoToDeathAnimation(protoHazard.getDeathAnimation() ) );
	}
	
	/* ------------------------------ Death Animation -------------------------------- */
	static WorldFormatProtos.World.DeathAnimation deathAnimationToProto(DeathAnimation animation) {
		switch (animation) {
		case BEE: 		return WorldFormatProtos.World.DeathAnimation.BEE;
		case BURN: 		return WorldFormatProtos.World.DeathAnimation.BURN;
		case NORMAL: 	return WorldFormatProtos.World.DeathAnimation.NORMAL_DEATH;
		case ELECTRIC: 	return WorldFormatProtos.World.DeathAnimation.ELECTRIC;
		default: throw new RuntimeException("Death Animation " + animation + " has no version in Proto format!");
		}
	}
	
	static DeathAnimation protoToDeathAnimation(WorldFormatProtos.World.DeathAnimation protoAnimation) {
		switch (protoAnimation) {
		case BEE: return DeathAnimation.BEE;
		case BURN: return DeathAnimation.BURN;
		case NORMAL_DEATH: return DeathAnimation.NORMAL;
		case ELECTRIC: return DeathAnimation.ELECTRIC;
		default: throw new RuntimeException("Death Animation proto " + protoAnimation + " has no Java object!");
		}
	}
	
	/* ------------------------------ Goodies -------------------------------- */
	static List<WorldFormatProtos.World.StringToGoodieTuple> goodiesToProto(Map<String, Goodie> goodies) {
		List<WorldFormatProtos.World.StringToGoodieTuple> protoGoodies = new ArrayList<>(goodies.values().size() );
		for (Entry<String, Goodie> entry : goodies.entrySet() ) {
			WorldFormatProtos.World.Goodie goodie = goodieToProto(entry.getValue() );
			protoGoodies.add(WorldFormatProtos.World.StringToGoodieTuple.newBuilder()
							 .setOne(entry.getKey() )
							 .setTwo(goodie)
							 .build() );
		}
		return protoGoodies;
	}
	
	static WorldFormatProtos.World.Goodie goodieToProto(Goodie goodie) {
		WorldFormatProtos.World.Goodie.Builder protoGoodie = WorldFormatProtos.World.Goodie.newBuilder();
		protoGoodie.setId(goodie.getGoodieType().id() );
		protoGoodie.setScreenId(goodie.getScreenID() );
		protoGoodie.setLocation(pointToProto(goodie.getLocation() ) );
		return protoGoodie.build();
	}
	
	static Map<String, Goodie> protoToGoodies(List<WorldFormatProtos.World.StringToGoodieTuple> protoGoodies, WorldResource rsrc) {
		Map<String, Goodie> goodies = new HashMap<>(protoGoodies.size() );
		for (WorldFormatProtos.World.StringToGoodieTuple tuple : protoGoodies) {
			goodies.put(tuple.getOne(), protoToGoodie(tuple.getTwo(), rsrc) );
		}
		return goodies;
	}
	
	static Goodie protoToGoodie(WorldFormatProtos.World.Goodie protoGoodie, WorldResource rsrc) {
		return Goodie.newGoodie(Goodie.Type.byValue(protoGoodie.getId() ), 
								protoToPoint(protoGoodie.getLocation() ), 
								protoGoodie.getScreenId(), 
								rsrc);
	}
	
	/* ------------------------------ Point -------------------------------- */
	static WorldFormatProtos.World.Point pointToProto(IPoint2D point) {
		WorldFormatProtos.World.Point.Builder protoPoint = WorldFormatProtos.World.Point.newBuilder();
		protoPoint.setX(point.x() );
		protoPoint.setY(point.y() );
		return protoPoint.build();
	}
	
	static ImmutablePoint2D protoToPoint(WorldFormatProtos.World.Point protoPoint) {
		return ImmutablePoint2D.of(protoPoint.getX(), protoPoint.getY() );
	}
	
	/* ------------------------------ Levels -------------------------------- */
	static List<WorldFormatProtos.World.IntegerToLevelTuple> levelsToProto(Map<Integer, LevelScreen> levels) {
		List<WorldFormatProtos.World.IntegerToLevelTuple> protoLevels = new ArrayList<>(levels.size() );
		for (Entry<Integer, LevelScreen> entry : levels.entrySet() ) {
			WorldFormatProtos.World.LevelScreen level = levelToProto(entry.getValue() );
			protoLevels.add(WorldFormatProtos.World.IntegerToLevelTuple.newBuilder()
							.setOne(entry.getKey() )
							.setTwo(level)
							.build() );
		}
		return protoLevels;
	}
	
	static WorldFormatProtos.World.LevelScreen levelToProto(LevelScreen level) {
		WorldFormatProtos.World.LevelScreen.Builder protoLevel = WorldFormatProtos.World.LevelScreen.newBuilder();
		protoLevel.setId(level.getId() );
		protoLevel.setBonzoLocation(pointToProto(level.getBonzoStartingLocation() ) );
		protoLevel.setBackground(backgroundToProto(level.getBackground() ) );
		
		protoLevel.addAllSprites(spritesToProto(level.getSpritesOnScreen() ) );
		protoLevel.addAllTiles(tilesToProto(level.internalGetTiles() ) );
		
		return protoLevel.build();
	}
	
	static Map<Integer, LevelScreen> protoToLevels(List<WorldFormatProtos.World.IntegerToLevelTuple> protoLevels, WorldResource rsrc, List<Hazard> hazards, List<Conveyer> conveyers) {
		Map<Integer, LevelScreen> levels = new HashMap<>(protoLevels.size() );
		for (WorldFormatProtos.World.IntegerToLevelTuple tuple : protoLevels) {
			levels.put(tuple.getOne(), protoToLevel(tuple.getTwo(), rsrc, hazards, conveyers) );
		}
		return levels;
	}
	
	static LevelScreen protoToLevel(WorldFormatProtos.World.LevelScreen protoLevel, WorldResource rsrc, List<Hazard> hazards, List<Conveyer> conveyers) {
		return new LevelScreen(protoLevel.getId(), 
							   protoToBackground(protoLevel.getBackground(), rsrc), 
							   protoToTiles(protoLevel.getTilesList(), rsrc, hazards, conveyers), 
							   protoToPoint(protoLevel.getBonzoLocation() ), 
							   protoToSprites(protoLevel.getSpritesList(), rsrc ),
							   rsrc);
	}
	
	/* ---------------------------- Backgrounds ------------------------------ */
	static WorldFormatProtos.World.Background backgroundToProto(Background b) {
		WorldFormatProtos.World.Background.Builder protoBackground = WorldFormatProtos.World.Background.newBuilder();
		if (b instanceof FullBackground) {
			FullBackground full = (FullBackground)b;
			protoBackground.setId(full.getId() );
			BackgroundType backgroundType =   full.isPattern()
											? BackgroundType.PATTERN
											: BackgroundType.FULL;
			protoBackground.setType(backgroundType);
		} else {
			// Solid color
			Color color = ((SingleColorBackground)b).getColor();
			// Store as ARGB. Just in case the colour model ISN'T ARGB, we manually create
			// the integer. Alpha will be lost anyway, for now, but store it in case we ever decide not
			// to lose it during decoding.
			int argb = (color.getAlpha() << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | (color.getBlue() );
			protoBackground.setId(argb);
			protoBackground.setType(BackgroundType.SOLID_COLOR);
		}
		
		return protoBackground.build();
	}
	
	static Background protoToBackground(WorldFormatProtos.World.Background protoBackground, WorldResource rsrc) {
		int id = protoBackground.getId();
		switch (protoBackground.getType() ) {
		case FULL:
			if (id >= rsrc.getBackgroundCount() )  throw new RuntimeException("Requested full background id " + id + " does not exist in resource pack");
			return rsrc.getBackground(id);
		case PATTERN:
			if (id >= rsrc.getPatternCount() )  throw new RuntimeException("Requested pattern id " + id + " does not exist in resource pack");
			return rsrc.getPattern(id);
		case SOLID_COLOR:
			// Id is an ARGB encoded Color object.
			
			// Alpha is lost in this constructor. However, backgrounds should not
			// HAVE an alpha to begin with. For now, just be content with losing it.
			Color color = new Color(id);
			return new SingleColorBackground(color);
		default:  throw new RuntimeException("Decoder cannot handle background type " + protoBackground.getType() );
		}
	}
	
	/* ------------------------------ Sprites -------------------------------- */
	static List<WorldFormatProtos.World.Sprite> spritesToProto(List<Sprite> sprites) {
		List<WorldFormatProtos.World.Sprite> protoSprites = new ArrayList<>(sprites.size() );
		for (Sprite s : sprites) {
			protoSprites.add(spriteToProto(s) );
		}
		return protoSprites;
	}
	
	static WorldFormatProtos.World.Sprite spriteToProto(Sprite sprite) {
		WorldFormatProtos.World.Sprite.Builder protoSprite = WorldFormatProtos.World.Sprite.newBuilder();
		protoSprite.setId(sprite.getId() );
		protoSprite.setStartLocation(pointToProto(sprite.getStaringLocation() ) );
		protoSprite.setBoundingBox(boxToProto(sprite.getBoundingBox() ) );
		protoSprite.setAnimation(animationTypeToProto(sprite.getAnimationType() ) );
		protoSprite.setAnimationSpeed(animationSpeedToProto(sprite.getAnimationSpeed() ) );
		protoSprite.setType(spriteTypeToProto(sprite.getType() ) );
		
		// Build a point for storage
		WorldFormatProtos.World.Point.Builder initialSpeed = WorldFormatProtos.World.Point.newBuilder();
		initialSpeed.setX(sprite.getInitialSpeedX() );
		initialSpeed.setY(sprite.getInitialSpeedY() );
		protoSprite.setInitialSpeed(initialSpeed.build() );
		return protoSprite.build();
	}
	
	static List<Sprite> protoToSprites(List<WorldFormatProtos.World.Sprite> protoSprites, WorldResource rsrc) {
		List<Sprite> sprites = new ArrayList<>(protoSprites.size() );
		for (WorldFormatProtos.World.Sprite s : protoSprites) {
			sprites.add(protoToSprite(s, rsrc) );
		}
		return sprites;
	}
	
	static Sprite protoToSprite(WorldFormatProtos.World.Sprite protoSprite, WorldResource rsrc) {
		return Sprite.newSprite(protoSprite.getId(), 
								protoToPoint(protoSprite.getStartLocation() ), 
								protoToBox(protoSprite.getBoundingBox() ), 
								protoToPoint(protoSprite.getInitialSpeed() ), 
								protoToAnimationType(protoSprite.getAnimation() ), 
								protoToAnimationSpeed(protoSprite.getAnimationSpeed() ),
								protoToSpriteType(protoSprite.getType() ),
								rsrc);
	}
	
	/* --------------------------- Sprite Type --------------------------- */
	static WorldFormatProtos.World.SpriteType spriteTypeToProto(SpriteType spriteType) {
		switch (spriteType) {
		case NORMAL:  return WorldFormatProtos.World.SpriteType.NORMAL;
		case HEALTH_DRAIN:  return WorldFormatProtos.World.SpriteType.HEALTH_DRAIN;
		case BONUS_DOOR:  return WorldFormatProtos.World.SpriteType.BONUS_DOOR;
		case EXIT_DOOR:  return WorldFormatProtos.World.SpriteType.EXIT_DOOR;
		case SCENERY:  return WorldFormatProtos.World.SpriteType.SCENERY_SPRITE;
		default:  throw new RuntimeException("Sprite type " + spriteType + " has no defined proto version");
		}
	}
	
	static SpriteType protoToSpriteType(WorldFormatProtos.World.SpriteType spriteTypeProto) {
		switch (spriteTypeProto) {
		case NORMAL:  return SpriteType.NORMAL;
		case HEALTH_DRAIN:  return SpriteType.HEALTH_DRAIN;
		case BONUS_DOOR:  return SpriteType.BONUS_DOOR;
		case EXIT_DOOR:  return SpriteType.EXIT_DOOR;
		case SCENERY_SPRITE:  return SpriteType.SCENERY;
		default:  throw new RuntimeException("Proto sprite type " + spriteTypeProto + " has no defined java object");
		}
	}
	
	/* ------------------------------ Box -------------------------------- */
	static WorldFormatProtos.World.Box boxToProto(Boundable box) {
		WorldFormatProtos.World.Box.Builder protoBox = WorldFormatProtos.World.Box.newBuilder();
		protoBox.setTopLeft(pointToProto(box.getLocation() ) );
		protoBox.setSize(pointToProto(box.getSize() ) );
		return protoBox.build();
	}
	
	// We stick with immutable types for decoding
	static ImmutableRectangle protoToBox(WorldFormatProtos.World.Box protoBox) {
		IPoint2D start = protoToPoint(protoBox.getTopLeft() );
		IPoint2D size = protoToPoint(protoBox.getSize() );
		return ImmutableRectangle.of(start.x(), start.y(), size.x(), size.y() );
	}
	
	/* ------------------------------ Animation Type -------------------------------- */
	static WorldFormatProtos.World.AnimationType animationTypeToProto(AnimationType type) {
		switch (type) {
		case INCREASING_FRAMES: return WorldFormatProtos.World.AnimationType.INCREASING_FRAMES;
		case CYCLING_FRAMES: return WorldFormatProtos.World.AnimationType.CYLCING_FRAMES;
		default: throw new RuntimeException("Animation type " + type + " has no defined proto version!");
		}
	}
	
	static AnimationType protoToAnimationType(WorldFormatProtos.World.AnimationType type) {
		switch (type) {
		case INCREASING_FRAMES: return AnimationType.INCREASING_FRAMES;
		case CYLCING_FRAMES: return AnimationType.CYCLING_FRAMES;
		default: throw new RuntimeException("Animation type proto " + type + " has no defined Java object!");
		}
	}
	
	/* ------------------------------ Animation Speed -------------------------------- */
	static WorldFormatProtos.World.AnimationSpeed animationSpeedToProto(AnimationSpeed speed) {
		switch (speed) {
		case NORMAL: return WorldFormatProtos.World.AnimationSpeed.NORMAL_SPEED;
		case SLOW: return WorldFormatProtos.World.AnimationSpeed.SLOW;
		default: throw new RuntimeException("Animation speed " + speed + " has no defined proto version!");
		}
	}
	
	static AnimationSpeed protoToAnimationSpeed(WorldFormatProtos.World.AnimationSpeed speed) {
		switch (speed) {
		case NORMAL_SPEED: return AnimationSpeed.NORMAL;
		case SLOW: return AnimationSpeed.SLOW;
		default: throw new RuntimeException("Animation speed proto " + speed + " has no defined Java object!");
		}
	}
	
	/* ------------------------------ Tiles -------------------------------- */
	// Normalises the 2D array to 1D. Since the array's dimensions are hardcoded and known,
	// this is easily reversable to get the correct tiles.
	static List<WorldFormatProtos.World.Tile> tilesToProto(Tile[][] tiles) {
		List<WorldFormatProtos.World.Tile> protoTiles = new ArrayList<>(GameConstants.TILES_IN_ROW * GameConstants.TILES_IN_COL);
		for (int i = 0; i < GameConstants.TILES_IN_COL; i++) {
			for (int j = 0; j < GameConstants.TILES_IN_ROW; j++) {
				// Note that 2D array stores as y, x.
				protoTiles.add(tileToProto(tiles[i][j], j, i) );
			}
		}
		return protoTiles;
	}
	
	// x and y are locations tile exists in world. In game, that information is just in the array. On disk,
	// that information is stored with each tile.
	static WorldFormatProtos.World.Tile tileToProto(Tile tile, int x, int y) {
		WorldFormatProtos.World.Tile.Builder protoTile = WorldFormatProtos.World.Tile.newBuilder();
		// All tiles store id in their TileType class, but in proto form we keep it in the basic Tile object.
		protoTile.setId(tile.getType().getId() );
		protoTile.setLocation(WorldFormatProtos.World.Point.newBuilder()
							  .setX(x)
							  .setY(y)
							  .build() );
		
		TileType tileType = tile.getType();
		protoTile.setType(tileTypeToProto(tileType) );
		// Depending on type:
		// If conveyer we must set the rotation
		if (tileType instanceof ConveyerTile) {
			protoTile.setRotation(rotationToProto(((ConveyerTile) tileType).getConveyer().getRotation() ) );
		}
		
		return protoTile.build();
	}
	
	static Tile[][] protoToTiles(List<WorldFormatProtos.World.Tile> protoTiles, WorldResource rsrc, List<Hazard> hazards, List<Conveyer> conveyers) {
		Tile[][] tiles = new Tile[GameConstants.TILES_IN_COL][GameConstants.TILES_IN_ROW];
		Iterator<WorldFormatProtos.World.Tile> it = protoTiles.iterator();
		for (int i = 0; i < GameConstants.TILES_IN_COL; i++) {
			for (int j = 0; j < GameConstants.TILES_IN_ROW; j++) {
				assert it.hasNext();
				tiles[i][j] = protoToTile(it.next(), rsrc, hazards, conveyers);
			}
		}
		return tiles;
	}
	
	// Extra params needed for some tiles.
	static Tile protoToTile(WorldFormatProtos.World.Tile protoTile, WorldResource rsrc, List<Hazard> hazards, List<Conveyer> conveyers) {
		TileType resultType = protoToTileType(protoTile.getType(), protoTile, rsrc, hazards, conveyers);
		return   resultType == null
			   ? Tile.emptyTile()
			   : Tile.newTile(protoToPoint(protoTile.getLocation() ), 
							  protoTile.getId(), 
							  resultType, 
							  rsrc);
	}
	
	/* ------------------------------ Tile Types -------------------------------- */
	// This is not enough to convert ALL tile type information to proto form. One must
	// determine which exact type it is and if there is need for additional information extraction
	// for the proto form of Tile
	static WorldFormatProtos.World.TileType tileTypeToProto(TileType type) {
		if (type instanceof CommonTile) {
			switch (((CommonTile)type).getUnderlyingType() ) {
			case NONE: return WorldFormatProtos.World.TileType.NONE;
			case SOLID: return WorldFormatProtos.World.TileType.SOLID;
			case THRU: return WorldFormatProtos.World.TileType.THRU;
			case SCENE: return WorldFormatProtos.World.TileType.SCENERY;
			default: throw new RuntimeException("Stateless Tile Type " + type + " has no defined proto version!");
			}
		} else if (type instanceof HazardTile) {
			return WorldFormatProtos.World.TileType.HAZARD;
		} else if (type instanceof ConveyerTile) {
			return WorldFormatProtos.World.TileType.CONVEYER;
		} else if (type instanceof CollapsibleTile) {
			return WorldFormatProtos.World.TileType.BREAKING;
		} else {
			throw new RuntimeException("Tile type " + type + " has no defined proto version!");
		}
	}
	
	// Extra parameters are required for setting up some more complicated tiles.
	// VERY SPECIAL: This returns null for none tiles. Callers must convert null to the special
	// immutable singleton Tile.NO_TILE.
	static TileType protoToTileType(WorldFormatProtos.World.TileType type, WorldFormatProtos.World.Tile tile, WorldResource rsrc, List<Hazard> hazards, List<Conveyer> conveyers) {
		switch (type) {
		case NONE: return null;
		case SOLID: return CommonTile.of(tile.getId(), StatelessTileType.SOLID, rsrc);
		case THRU: return CommonTile.of(tile.getId(), StatelessTileType.THRU, rsrc);
		case SCENERY: return CommonTile.of(tile.getId(), StatelessTileType.SCENE, rsrc);
		case HAZARD: return HazardTile.forHazard(hazards.get(tile.getId() ) );
		case CONVEYER: return new ConveyerTile(conveyers.get(tile.getId() * 2 + getConveyerIdOffset(tile.getRotation() ) ) );
		case BREAKING: return new CollapsibleTile(tile.getId() );
		default: throw new RuntimeException("Proto tiletype " + type + " has no defined java object!");
		}
	}
	
	// Returns 0 for clockwise rotation and 1 for anti-clockwise rotation.
	// Used to get offset for mapping encoded conveyer to proper real conveyer
	private static int getConveyerIdOffset(WorldFormatProtos.World.Rotation rotation) {
		if (rotation == WorldFormatProtos.World.Rotation.CLOCKWISE)  return 0;
		else														 return 1;
	}
	
	/* ------------------------------ Rotation -------------------------------- */
	// specific to conveyer belts
	static WorldFormatProtos.World.Rotation rotationToProto(Rotation rotation) {
		switch (rotation) {
		case CLOCKWISE: return WorldFormatProtos.World.Rotation.CLOCKWISE;
		case ANTI_CLOCKWISE: return WorldFormatProtos.World.Rotation.ANTI_CLOCKWISE;
		case NONE: throw new RuntimeException("No rotation is not valid for save file format: API error");
		default: throw new RuntimeException("Rotation " + rotation + " has no defined proto version!");
		}
	}
	
	static Rotation protoToRotation(WorldFormatProtos.World.Rotation rotation) {
		switch (rotation) {
		case CLOCKWISE: return Rotation.CLOCKWISE;
		case ANTI_CLOCKWISE: return Rotation.ANTI_CLOCKWISE;
		default: throw new RuntimeException("Proto Rotation " + rotation + " has no defined java object!");
		}
	}
	

	/**
	 * 
	 * Creates a new world that is empty. The newly created world will have the given name and a single screen of id 1000.
	 * 
	 * @param name
	 * 		the name of the new world
	 * 
	 * @return
	 * 		an empty encoded world
	 * 
	 */
	public static EncodedWorld fresh(String name) {
		// Set up all defaults
		WorldFormatProtos.World.Builder newWorld = WorldFormatProtos.World.newBuilder();
		newWorld.setAuthor("Unknown");
		newWorld.setName(name);
		newWorld.setBonusScreen(10000);
		// Goodies and hazards have no entries.
		
		WorldFormatProtos.World.LevelScreen.Builder emptyLevel = WorldFormatProtos.World.LevelScreen.newBuilder();
		emptyLevel.setId(1000);
		// Use a solid colour, because barring additional resource pack information, solid colors will always
		// be present
		emptyLevel.setBackground(WorldFormatProtos.World.Background.newBuilder()
								 .setType(WorldFormatProtos.World.BackgroundType.SOLID_COLOR)
								 .setId(Color.BLACK.getRGB() )
								 .build() );
		emptyLevel.setBonzoLocation(WorldFormatProtos.World.Point.newBuilder()
									.setX(0)
									.setY(0)
									.build() );
		
		// MUST set up tiles to all empty
		
		List<WorldFormatProtos.World.Tile> tiles = new ArrayList<>(GameConstants.TOTAL_TILES);
		for (int i = 0; i < GameConstants.TILES_IN_COL; i++) {
			for (int j = 0; j < GameConstants.TILES_IN_ROW; j++) {
			
			tiles.add(WorldFormatProtos.World.Tile.newBuilder()
					  .setId(0)
					  .setLocation(WorldFormatProtos.World.Point.newBuilder()
							  	   .setX(i)
							  	   .setY(j) ) 
					  .setType(WorldFormatProtos.World.TileType.NONE)
					  .build() );
			}
		}
		
		emptyLevel.addAllTiles(tiles);
		
		newWorld.addLevels(WorldFormatProtos.World.IntegerToLevelTuple.newBuilder()
						   .setOne(1000)
						   .setTwo(emptyLevel)
						   .build() );
		
		return new EncodedWorld(newWorld.build() );
	}
	
}
