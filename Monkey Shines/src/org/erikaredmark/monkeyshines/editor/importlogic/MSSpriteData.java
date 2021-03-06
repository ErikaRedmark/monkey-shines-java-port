package org.erikaredmark.monkeyshines.editor.importlogic;

import java.io.IOException;
import java.io.InputStream;

import org.erikaredmark.monkeyshines.AnimationSpeed;
import org.erikaredmark.monkeyshines.AnimationType;
import org.erikaredmark.monkeyshines.ImmutablePoint2D;
import org.erikaredmark.monkeyshines.ImmutableRectangle;
import org.erikaredmark.monkeyshines.MonsterType;
import org.erikaredmark.monkeyshines.editor.importlogic.WorldTranslationException.TranslationFailure;
import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.erikaredmark.monkeyshines.sprite.Monster.ForcedDirection;
import org.erikaredmark.monkeyshines.sprite.Monster.TwoWayFacing;

import static org.erikaredmark.monkeyshines.editor.importlogic.TranslationUtil.*;

/**
 * 
 * This class mimics 1:1 the C structure in the original game that was saved in the old level file
 * format. For ease of use, translation utilities can read this structure whole-cloth from a binary
 * stream.
 * <p/>
 * Instances of this class are immutable and are built by the translator using the internal builder.
 * They can ONLY be constructed from a valid binary stream.
 * <p/>
 * Use accessor methods to get translated values for the port. Directly access the final public data
 * to get raw values.
 * 
 * @author Erika Redmark
 *
 */
final class MSSpriteData {
	public final ImmutablePoint2D location;
	public final ImmutablePoint2D minimum;
	public final ImmutablePoint2D maximum;
	public final ImmutablePoint2D speed;
	public final int id;
	public final int flags;
	
	// Precomputed non-raw data
	private final int portId;
	private final boolean alwaysFacingRight;
	private final boolean alwaysFacingLeft;
	
	// Bit twiddling for flags
	private static final int FLAG_INCREASING_FRAMES = 1;
	// Second bit is skipped. It has a 1 for cycling frames. No point since a 0 for increasing frames means the same thing
	private static final int FLAG_SLOW_ANIMATION = 1 << 2;
	
	private static final int FLAG_TWO_WAY_FACING_VERTICAL = 1 << 4;
	
	
	private static final int FLAG_TWO_WAY_FACING_HORIZONTAL = 1 << 3;
	
	// Sixth bit is probably unused.
	// Seventh bit if for door, but ID 0 is always bonus, ID 1 is always exit. This flag is not relevant
	// 	   and probably wasn't relevant in the original game either.
	private static final int FLAG_ENERGY_DRAINER = 1 << 7;
	
	private MSSpriteData(final ImmutablePoint2D location,
						 final ImmutablePoint2D minimum,
						 final ImmutablePoint2D maximum,
						 final ImmutablePoint2D speed,
						 final int id,
						 final int flags,
						 final WorldResource rsrc) {
		
		this.location = location;
		this.minimum = minimum;
		this.maximum = maximum;
		this.speed = speed;
		this.id = id;
		this.flags = flags;
		
		int idCounter = 0;
		int actualId = 0;
		boolean facingRight = false;
		// TODO interpret flags to allow for always left facing sprites.
		for (int i = 0; i < rsrc.getSpritesCount(); ++i) {
			if (idCounter == id) {
				actualId = i;
				break;
			}
			
			int spriteHeight = rsrc.getSpritesheetHeight(id);
			idCounter += spriteHeight / 40;
			
			// need to check if we skipped the id, indicating a right way facing sprite.
			if (idCounter == id + 1) {
				actualId = i;
				facingRight = true;
				break;
			}
		}
		
		this.portId = actualId;
		this.alwaysFacingRight = facingRight;
		// If the sprite is not always facing right, there is a CHANCE it may
		// always be facing left based on the flags.
		if (!(facingRight) ) {
			if ( (flags & FLAG_TWO_WAY_FACING_HORIZONTAL) == 0 ) {
				this.alwaysFacingLeft = true;
			} else {
				this.alwaysFacingLeft = false;
			}
		} else {
			this.alwaysFacingLeft = false;
		}
	}
	
	/**
	 * 
	 * Reads a single MSSpriteData object from the given stream. The stream must currently be positioned at the beginning of the
	 * definition of the proper object (up to translators to handle that). At the conclusion of this method, the stream will have
	 * been advanced 20 bytes, the size of this data as stored in the binary form.
	 * <p/>
	 * This method REQUIRES a world resource because the original game
	 * considered the left/right facing of a sprite two unique ids, the port does not. Only by counting
	 * each sprite in the resource pack, in order, as 1 or 2 can the actual sprite id be determined. If
	 * it ends up skipping the id, that means the sprite is facing right only.
	 * this information is precomputed for each sprite.
	 * 
	 * @param is
	 * 		input stream
	 * 
	 * @return
	 * 		an instance of this object
	 * 
	 * @throws IOException
	 * 		if something unexpected happens reading the stream
	 * 	
	 * @throws WorldTranslationException
	 * 		if the stream is either malformed or too small
	 * 
	 */
	static MSSpriteData fromStream(InputStream is, WorldResource rsrc) throws IOException, WorldTranslationException {
		// Just to ease on the typing for this method.
		final TranslationFailure FAIL = TranslationFailure.WRONG_LEVEL_SIZE;
		
		int locationY = readMacShort(is, FAIL, "Could not read sprite location Y");
		int locationX = readMacShort(is, FAIL, "Could not read sprite location X");
		
		int minimumY = readMacShort(is, FAIL, "Could not read sprite minimum Y");
		int minimumX = readMacShort(is, FAIL, "Could not read sprite minimum X");
		
		int maximumY = readMacShort(is, FAIL, "Could not read sprite maximum Y");
		int maximumX = readMacShort(is, FAIL, "Could not read sprite maximum X");
		
		int speedY = readMacShort(is, FAIL, "Could not read sprite speed Y");
		int speedX = readMacShort(is, FAIL, "Could not read sprite speed X");
		
		int spriteId = readMacShort(is, FAIL, "Could not read sprite id");
		int flags = readMacShort(is, FAIL, "Could not read sprite flags");
		
//		byte[] flagsRaw = new byte[2];
//		read(is, flagsRaw, FAIL, "Could not read sprite flags");
//		
//		ByteBuffer flags = ByteBuffer.allocate(2);
//		flags.order(ByteOrder.LITTLE_ENDIAN);
//		flags.put(flagsRaw);
		
		return new MSSpriteData(ImmutablePoint2D.of(locationX, locationY),
								ImmutablePoint2D.of(minimumX, minimumY),
								ImmutablePoint2D.of(maximumX, maximumY),
								ImmutablePoint2D.of(speedX, speedY),
								spriteId,
								flags,
								rsrc);
	}
	
	/**
	 * 
	 * See {@code fromStream(InputStream, WorldResource)}. Reads an array of instances of this class. The amount
	 * is determined by the size parameter. The stream will be read by 20 * size bytes at the conclusion of this
	 * method
	 * 
	 */
	static MSSpriteData[] arrayFromStream(InputStream is, int size, WorldResource rsrc) throws IOException, WorldTranslationException {
		MSSpriteData[] data = new MSSpriteData[size];
		for (int i = 0; i < size; ++i) {
			data[i] = fromStream(is, rsrc);
		}
		return data;
	}
	// Some information here is not even the same as it is in the original game level editor.
	// basic notes: -80s are because original game included UI banner as playable field, port doesn't
	// -40s are because bounds in port are widths, not actual bounds, so the sprite size is removed.
	
	/**
	 * Returns the location of the sprite in terms of the port, not the original.
	 */
	ImmutablePoint2D getPortLocation() {
		return ImmutablePoint2D.of(location.x(), location.y() - 80);
	}

	/**
	 * Returns bounding box for the port
	 */
	ImmutableRectangle getPortBoundingBox() {
		// Maximum x and y are Right and bottom (in the level editor) - 40. Based the info here
		// https://github.com/ErikaRedmark/monkey-shines-java-port/wiki/Porting-Old-Worlds
		// the -40s cancel so it is just max-min.
		return ImmutableRectangle.of(minimum.x(), 
									 minimum.y() - 80, 
									 maximum.x() - minimum.x(), 
									 maximum.y() - minimum.y() );
	}
	
	/**
	 * Returns the forced direction, if any, of the sprite
	 */
	ForcedDirection getPortDirection() {
		// Always facing left and always facing right can apply to always bottom/top identically given how
		// the sprite graphics are computed.
		if (alwaysFacingLeft)  		 return ForcedDirection.LEFT_DOWN;
		else if (alwaysFacingRight)  return ForcedDirection.RIGHT_UP;
		
		// Else if not returned yet
		return ForcedDirection.NONE;
	}
	
	/**
	 * Returns whether the sprite has two sets vertical, horizontal, or is just one set.
	 * @return
	 */
	TwoWayFacing getTwoFacing() {
		if ((flags & FLAG_TWO_WAY_FACING_HORIZONTAL) != 0) {
			return TwoWayFacing.HORIZONTAL;
		} else if ((flags & FLAG_TWO_WAY_FACING_VERTICAL) != 1) {
			return TwoWayFacing.VERTICAL;
		} else {
			return TwoWayFacing.SINGLE;
		}
	}
	
	/**
	 * Returns the velocity of the sprite
	 */
	ImmutablePoint2D getPortVelocity() {
		// Port inverts speeds.
		return ImmutablePoint2D.of(-speed.x(), -speed.y() );
	}
	
	/**
	 * Returns the id of the sprite, for the PORT, not the raw id.
	 */
	int getSpriteId() {
		return portId;
	}
	
	/**
	 * Returns if the sprite should always face right.
	 */
	boolean getSpriteFacingRight() {
		return alwaysFacingRight;
	}
	
	boolean getSpriteFacingLeft() {
		return alwaysFacingLeft;
	}
	
	/**
	 * Returns the animation type of the sprite.
	 */
	AnimationType getSpriteAnimationType() {
		return   (flags & FLAG_INCREASING_FRAMES) != 0
			   ? AnimationType.INCREASING_FRAMES
			   : AnimationType.CYCLING_FRAMES;
	}
	
	/**
	 * Returns the animation speed of the sprite
	 */
	AnimationSpeed getSpriteAnimationSpeed() {
		return   (flags & FLAG_SLOW_ANIMATION) != 0
			   ? AnimationSpeed.SLOW
			   : AnimationSpeed.NORMAL;
	}
	
	/**
	 * Returns the sprite type, Normal, Energy Drainer, Exit Door, or Bonus Door. Harmless sprites
	 * did not exist in the original game.
	 */
	MonsterType getSpriteType() {
		// original game ALWAYs had 0 be the bonus door, and 1 be the exit door. The editor manual
		// even advised not to change this. So, we can easily determine the bonus and exit doors
		// We check doors first so that a level always has doors.
		if (id == 0) {
			return MonsterType.BONUS_DOOR;
		} else if (id == 1) {
			return MonsterType.EXIT_DOOR;
		} else {
			return   (flags & FLAG_ENERGY_DRAINER) != 0
				   ? MonsterType.HEALTH_DRAIN
				   : MonsterType.NORMAL;
		}
	}
}
