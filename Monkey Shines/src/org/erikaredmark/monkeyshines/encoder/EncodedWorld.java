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
import java.util.logging.Logger;

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
import org.erikaredmark.monkeyshines.MonsterType;
import org.erikaredmark.monkeyshines.TileMap;
import org.erikaredmark.monkeyshines.World;
import org.erikaredmark.monkeyshines.Conveyer.Rotation;
import org.erikaredmark.monkeyshines.WorldCoordinate;
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
import org.erikaredmark.monkeyshines.sprite.Monster;
import org.erikaredmark.monkeyshines.sprite.Monster.ForcedDirection;
import org.erikaredmark.monkeyshines.sprite.Monster.TwoWayFacing;
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
	private static final String CLASS_NAME = "org.erikaredmark.monkeyshines.encoder.EncodedWorld";
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
	
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
		final Map<WorldCoordinate, Goodie> goodiesInWorld = protoToGoodies(world.getGoodiesList(), rsrc);
		final List<Hazard> hazards = protoToHazards(world.getHazardsList() );
		
		// Compare the generated hazards with the number in the resource. If less, add extra.
		// If more... log a warning, user is losing a hazard if they save.
		int expectedHazards = rsrc.getHazardCount();
		if (hazards.size() > expectedHazards) {
			LOGGER.warning("Expected " + expectedHazards + " but save file shows " + hazards.size() + ". Hazards after the cutoff point will be auto-deleted on next size. Exit without saving and add hazards to the resource pack to fix.");
			for (int i = hazards.size() - 1; i >= expectedHazards; --i) {
				hazards.remove(i);
			}
		} else if (hazards.size() < expectedHazards){
			List<Hazard> newHazards = Hazard.initialise(hazards.size(), expectedHazards - hazards.size(), rsrc);
			hazards.addAll(newHazards);
		}
		
		final int bonusScreen = this.world.getBonusScreen();
		
		// Size of conveyers may be added to if the world is skinned with an updated
		// resource containing new conveyers.
		// generate as many conveyer instances as graphics resource allows. Decoding of the
		// actual levels with the auto-generated conveyers will handle conveyer belts.
		List<Conveyer> conveyers = new ArrayList<>(rsrc.getConveyerCount() * 2 );
		World.generateConveyers(conveyers, rsrc.getConveyerCount() );
		
		// Finally, all the different kinds of tiles loaded, we can load the actually tilemap that requires references
		// to those tiles
		final Map<Integer, LevelScreen> worldScreens = protoToLevels(world.getLevelsList(), rsrc, hazards, conveyers);
		
		World newWorld = new World(worldName, goodiesInWorld, worldScreens, hazards, conveyers, bonusScreen, rsrc);
		newWorld.resetAllScreens();
		return newWorld;
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
		protoHazard.setHarmless(hazard.isHarmless() );
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
						  protoToDeathAnimation(protoHazard.getDeathAnimation() ),
						  protoHazard.getHarmless() );
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
	static List<WorldFormatProtos.World.StringToGoodieTuple> goodiesToProto(Map<WorldCoordinate, Goodie> goodies) {
		List<WorldFormatProtos.World.StringToGoodieTuple> protoGoodies = new ArrayList<>(goodies.values().size() );
		for (Entry<WorldCoordinate, Goodie> entry : goodies.entrySet() ) {
			WorldFormatProtos.World.Goodie goodie = goodieToProto(entry.getValue() );
			protoGoodies.add(WorldFormatProtos.World.StringToGoodieTuple.newBuilder()
							 .setOne(entry.getKey().createSavedStringForm() )
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
	
	static Map<WorldCoordinate, Goodie> protoToGoodies(List<WorldFormatProtos.World.StringToGoodieTuple> protoGoodies, WorldResource rsrc) {
		Map<WorldCoordinate, Goodie> goodies = new HashMap<>(protoGoodies.size() );
		for (WorldFormatProtos.World.StringToGoodieTuple tuple : protoGoodies) {
			goodies.put(WorldCoordinate.fromSavedStringForm(tuple.getOne() ), protoToGoodie(tuple.getTwo(), rsrc) );
		}
		return goodies;
	}
	
	static Goodie protoToGoodie(WorldFormatProtos.World.Goodie protoGoodie, WorldResource rsrc) {
		return Goodie.newGoodie(Goodie.Type.byValue(protoGoodie.getId() ), 
								protoToPoint(protoGoodie.getLocation() ), 
								protoGoodie.getScreenId());
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
		
		protoLevel.addAllSprites(spritesToProto(level.getMonstersOnScreen() ) );
		protoLevel.addAllTiles(tilesToProto(level.getMap() ) );
		
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
			return new FullBackground(id, false);
		case PATTERN:
			if (id >= rsrc.getPatternCount() )  throw new RuntimeException("Requested pattern id " + id + " does not exist in resource pack");
			return new FullBackground(id, true);
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
	static List<WorldFormatProtos.World.Sprite> spritesToProto(List<Monster> sprites) {
		List<WorldFormatProtos.World.Sprite> protoSprites = new ArrayList<>(sprites.size() );
		for (Monster s : sprites) {
			protoSprites.add(spriteToProto(s) );
		}
		return protoSprites;
	}
	
	static WorldFormatProtos.World.Sprite spriteToProto(Monster sprite) {
		WorldFormatProtos.World.Sprite.Builder protoSprite = WorldFormatProtos.World.Sprite.newBuilder();
		protoSprite.setId(sprite.getId() );
		protoSprite.setStartLocation(pointToProto(sprite.getStaringLocation() ) );
		protoSprite.setBoundingBox(boxToProto(sprite.getBoundingBox() ) );
		protoSprite.setAnimation(animationTypeToProto(sprite.getAnimationType() ) );
		protoSprite.setAnimationSpeed(animationSpeedToProto(sprite.getAnimationSpeed() ) );
		protoSprite.setType(spriteTypeToProto(sprite.getType() ) );
		protoSprite.setForcedDirection(forcedDirectionToProto(sprite.getForcedDirection() ) );
		protoSprite.setTwoSetsDirection(twoWayFacingToProto(sprite.getTwoWayFacing() ) );
		
		// Build a point for storage
		WorldFormatProtos.World.Point.Builder initialSpeed = WorldFormatProtos.World.Point.newBuilder();
		initialSpeed.setX(sprite.getInitialSpeedX() );
		initialSpeed.setY(sprite.getInitialSpeedY() );
		protoSprite.setInitialSpeed(initialSpeed.build() );
		return protoSprite.build();
	}
	
	static List<Monster> protoToSprites(List<WorldFormatProtos.World.Sprite> protoSprites, WorldResource rsrc) {
		List<Monster> sprites = new ArrayList<>(protoSprites.size() );
		for (WorldFormatProtos.World.Sprite s : protoSprites) {
			sprites.add(protoToSprite(s, rsrc) );
		}
		return sprites;
	}
	
	static Monster protoToSprite(WorldFormatProtos.World.Sprite protoSprite, WorldResource rsrc) {
		ImmutablePoint2D initialSpeed = protoToPoint(protoSprite.getInitialSpeed());
		
		return new Monster(protoSprite.getId(), 
							    protoToPoint(protoSprite.getStartLocation() ), 
								protoToBox(protoSprite.getBoundingBox() ), 
								initialSpeed.x(),
								initialSpeed.y(),
								protoToAnimationType(protoSprite.getAnimation() ), 
								protoToAnimationSpeed(protoSprite.getAnimationSpeed() ),
								protoToSpriteType(protoSprite.getType() ),
								protoToForcedDirection(protoSprite.getForcedDirection() ),
								protoToTwoWayFacing(protoSprite.getTwoSetsDirection() ),
								rsrc);
	}
	
	/* ------------------------- Two Way Facing -------------------------- */
	static WorldFormatProtos.World.TwoWayFacing twoWayFacingToProto(TwoWayFacing facing) {
		switch (facing) {
		case SINGLE:      return WorldFormatProtos.World.TwoWayFacing.SINGLE;
		case HORIZONTAL:  return WorldFormatProtos.World.TwoWayFacing.TWO_WAY_HORIZONTAL;
		case VERTICAL:    return WorldFormatProtos.World.TwoWayFacing.TWO_WAY_VERTICAL;
		default:   throw new RuntimeException("Two Way facing type " + facing + " has no defined proto version");
		}
	}
	
	static TwoWayFacing protoToTwoWayFacing(WorldFormatProtos.World.TwoWayFacing protoFacing) {
		// note: protoFacing may be null and probably is even in the distributed worlds, as two-way facing
		// was an automatic property based on the graphics resource and assumed horizontal. Whilst it is
		// still automatic to a degree, it can now be requested vertical (although it won't comply if the
		// graphics can't handle it).
		if (protoFacing == null || protoFacing == WorldFormatProtos.World.TwoWayFacing.TWO_WAY_UNUSED) {
			// Will become SINGLE if the graphics can't take it.
			return TwoWayFacing.HORIZONTAL;
		} else {
			switch (protoFacing) {
			case SINGLE:             return TwoWayFacing.SINGLE;
			case TWO_WAY_HORIZONTAL: return TwoWayFacing.HORIZONTAL;
			case TWO_WAY_VERTICAL:   return TwoWayFacing.VERTICAL;
			default:   throw new RuntimeException("Two Way facing type " + protoFacing + " has no defined java object");
			}
		}
	}
	
	/* ------------------------ Forced Direction ------------------------- */
	
	static WorldFormatProtos.World.ForcedDirection forcedDirectionToProto(ForcedDirection forced) {
		switch (forced) {
		case NONE:  return WorldFormatProtos.World.ForcedDirection.FORCED_NONE;
		case RIGHT_UP: return WorldFormatProtos.World.ForcedDirection.FORCED_RIGHT;
		case LEFT_DOWN:  return WorldFormatProtos.World.ForcedDirection.FORCED_LEFT;
		default:   throw new RuntimeException("Forced Direction type " + forced + " has no defined proto version");
		}
	}
	
	static ForcedDirection protoToForcedDirection(WorldFormatProtos.World.ForcedDirection protoForced) {
		switch (protoForced) {
		case FORCED_NONE:  return ForcedDirection.NONE;
		case FORCED_RIGHT: return ForcedDirection.RIGHT_UP;
		case FORCED_LEFT:  return ForcedDirection.LEFT_DOWN;
		default:   throw new RuntimeException("Forced Direction type " + protoForced + " has no defined java object");
		}
	}
	
	/* --------------------------- Sprite Type --------------------------- */
	static WorldFormatProtos.World.SpriteType spriteTypeToProto(MonsterType spriteType) {
		switch (spriteType) {
		case NORMAL:  return WorldFormatProtos.World.SpriteType.NORMAL;
		case HEALTH_DRAIN:  return WorldFormatProtos.World.SpriteType.HEALTH_DRAIN;
		case BONUS_DOOR:  return WorldFormatProtos.World.SpriteType.BONUS_DOOR;
		case EXIT_DOOR:  return WorldFormatProtos.World.SpriteType.EXIT_DOOR;
		case SCENERY:  return WorldFormatProtos.World.SpriteType.SCENERY_SPRITE;
		default:  throw new RuntimeException("Sprite type " + spriteType + " has no defined proto version");
		}
	}
	
	static MonsterType protoToSpriteType(WorldFormatProtos.World.SpriteType spriteTypeProto) {
		switch (spriteTypeProto) {
		case NORMAL:  return MonsterType.NORMAL;
		case HEALTH_DRAIN:  return MonsterType.HEALTH_DRAIN;
		case BONUS_DOOR:  return MonsterType.BONUS_DOOR;
		case EXIT_DOOR:  return MonsterType.EXIT_DOOR;
		case SCENERY_SPRITE:  return MonsterType.SCENERY;
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
	// Saves the tiles themselves as well as proper dimensions of the tilemap.
	// NOTICE: This is intended as part of LevelScreen. Only tilemaps of 20 rows by 32 columns are supported.
	static List<WorldFormatProtos.World.Tile> tilesToProto(TileMap map) {
		List<WorldFormatProtos.World.Tile> protoTiles = new ArrayList<>(GameConstants.LEVEL_ROWS * GameConstants.LEVEL_COLS);
		for (TileType t : map.internalMap() ) {
			WorldFormatProtos.World.Tile.Builder protoTile = WorldFormatProtos.World.Tile.newBuilder();
			protoTile.setId(t.getId() );
			protoTile.setType(tileTypeToProto(t) );
			if (t instanceof ConveyerTile) {
				protoTile.setRotation(rotationToProto(((ConveyerTile) t).getConveyer().getRotation() ) );
			}
			
			protoTiles.add(protoTile.build() );
		}
		return protoTiles;
	}
	
	// Loads the tiles themselves as well as proper dimensions of the tilemap.
	// NOTICE: This is intended as part of LevelScreen. Only tilemaps of 20 rows by 32 columns are supported.
	static TileMap protoToTiles(List<WorldFormatProtos.World.Tile> protoTiles, WorldResource rsrc, List<Hazard> hazards, List<Conveyer> conveyers) {
		TileMap map = new TileMap(GameConstants.LEVEL_ROWS, GameConstants.LEVEL_COLS);
		TileType[] internalMap = map.internalMap();
		Iterator<WorldFormatProtos.World.Tile> it = protoTiles.iterator();
		for (int i = 0; i < GameConstants.TOTAL_TILES; ++i) {
			assert it.hasNext();
			WorldFormatProtos.World.Tile encodedTile = it.next();
			internalMap[i] = protoToTileType(encodedTile.getType(), encodedTile, rsrc, hazards, conveyers);
		}
		return map;
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
	static TileType protoToTileType(WorldFormatProtos.World.TileType type, WorldFormatProtos.World.Tile tile, WorldResource rsrc, List<Hazard> hazards, List<Conveyer> conveyers) {
		switch (type) {
		case NONE: return CommonTile.NONE;
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
		for (int i = 0; i < GameConstants.TOTAL_TILES; ++i) {
			tiles.add(WorldFormatProtos.World.Tile.newBuilder()
					  .setId(0) 
					  .setType(WorldFormatProtos.World.TileType.NONE)
					  .build() );
		}
		
		emptyLevel.addAllTiles(tiles);
		
		newWorld.addLevels(WorldFormatProtos.World.IntegerToLevelTuple.newBuilder()
						   .setOne(1000)
						   .setTwo(emptyLevel)
						   .build() );
		
		return new EncodedWorld(newWorld.build() );
	}
	
}
