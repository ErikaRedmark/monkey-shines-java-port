package org.erikaredmark.monkeyshines;

import org.erikaredmark.monkeyshines.resource.SoundManager;
/**
 * 
 * Represents a specific goodie instance on the map
 * 
 * @author Erika Redmark
 *
 */
public class Goodie {
	
	/**
	 * Creates a goodie for the specified screen, for the specified world. 
	 */
	public static Goodie newGoodie(final Type type, 
								   final ImmutablePoint2D location, 
								   final int screenID) {
		
		return new Goodie(type, location, screenID);
	}
	
	private Goodie(final Type type, final ImmutablePoint2D location, final int screenId) {
		this.screenID = screenId;
		this.location = location;
		goodieType = type;
		taken = false;
		dead = false;
		yumSprite = -1; // The yumsprite will be set to zero before any drawing goes. This insures the first frame is not skipped.
		
		
		drawToX = (int)location.x() * GameConstants.GOODIE_SIZE_X;
		drawToY = (int)location.y() * GameConstants.GOODIE_SIZE_Y;
		
		drawX = type.getDrawX();
		drawY = type.getDrawY();
	}

	// Very simple animation.
	public void update() {
		if (!taken && !dead) {
			if (readyToAnimate() ) {
				if (drawY == 0)
					drawY = GameConstants.GOODIE_SIZE_Y;
				else
					drawY = 0;
			}
		} else if (taken && !dead) {
			yumSprite++;
			if (yumSprite >= 15) {
				dead = true;
			}
		}
	}
	
	/**
	 * 
	 * Resets this goodie, making it appear back on the screen, ONLY if it is persistant.
	 * TODO this method is not currently used
	 * 
	 */
	public void resetIfApplicable() {
		if (goodieType.persistant) {
			// Both must reset; together they make up taken, but not finished playing 'Yum', vs not taken.
			taken = false;
			dead = false;
		}
	}
	
	/**
	 * 
	 * Tells the goodie that it has been taken. This starts the 'Yum' animation and plays the appropriate
	 * sound. A goodie may normally only be taken once. If a goodie is already taken, this method does nothing.
	 * <p/>
	 * Some goodies are reset when a level screen is restarted.
	 * <p/>
	 * If a non absentsound manager is passed in, the goodie take sound will play.
	 * 
	 * @param bonzo
	 * 		a reference to bonzo, so that the goodies effects (score and misc.) may be applied to him.
	 * 
	 * @param world
	 * 		a reference to the world, so that goodies effects (keys) can apply there
	 * 
	 * @return
	 * 		{@code true} if the goodie was just taken, {@code false} if it was already taken
	 * 
	 */
	public boolean take(final Bonzo bonzo, final World world, SoundManager snd) {
		if (taken)  return false;
		
		taken = true;
		snd.playOnce(goodieType.soundEffect);
		// If bonzo under the effects if a powerup? Multiply the score
		int extraScore =   bonzo.getCurrentPowerup() == null
						 ? goodieType.score
						 : bonzo.getCurrentPowerup().multiplier() * goodieType.score;
		
		bonzo.incrementScore(extraScore);
		goodieType.affectBonzo(this, bonzo, world, snd);
		
		// Finally, if this is a powerup, grant bonzo the powerup.
		Powerup powerup = goodieType.powerupForGoodie();
		if (powerup != null) {
			bonzo.powerupCollected(powerup);
		}
		
		return true;
	}
	
	public int getScreenID() {
		return screenID;
	}

	/**
	 * Returns the type of this goodie, representing what kind of goodie it is
	 * 
	 * @return
	 * 		goodie id
	 */
	public Type getGoodieType() { return goodieType; }

	/**
	 * Returns this goodie's location
	 * 
	 * @return
	 * 		the location this goodie resides on whatever level it is on
	 */
	public ImmutablePoint2D getLocation() { return location; }

	/**
	 * 
	 * Two goodies are considered equal if they share the same, id, type, and location. State information (
	 * such as taken or dying) is NOT a factor in equality. Two goodies can NOT share the same id, type, and location
	 * and yet have different states.
	 * 
	 */
	@Override public boolean equals(Object o) {
		if (this == o)  return true;
		if (!(o instanceof Goodie) )  return false;
		
		Goodie other = (Goodie) o;
		return    this.goodieType.equals(other.goodieType)
			   && this.screenID == other.screenID
			   && this.location.equals(other.location);
	}
	
	@Override public int hashCode() {
		int result = 17;
		result += result * 31 + goodieType.hashCode();
		result += result * 31 + screenID;
		result += result * 31 + location.hashCode();
		return result;
	}
	
	private boolean readyToAnimate() {
		if (timeToNextFrame >= TICKS_BETWEEN_ANIMATIONS) {
			timeToNextFrame = 0;
			return true;
		} else {
			++timeToNextFrame;
			return false;
		}
	}
	
	public enum Type {
		// WARNING: xOffset is used as ID for encoding this in save file format!
		// Adding the concept of another row to this sprite sheet will require
		// modifying that logic!
		// Original game had all non essential goodies (ones that didn't do anything) and keys give 100 score, and everything
		// else gave nothing. For this port, extra lives will give 200 score.
		RED_KEY(0, 100, GameSoundEffect.YUM_COLLECT, false) {
			@Override public void affectBonzo(Goodie goodie, Bonzo bonzo, World world, SoundManager sound) {
				world.collectedRedKey(goodie, sound);
			}
		},
		BLUE_KEY(1, 100, GameSoundEffect.YUM_COLLECT, false) {
			@Override public void affectBonzo(Goodie goodie, Bonzo bonzo, World world, SoundManager sound) {
				world.collectedBlueKey(goodie, sound);
			}
		},
		APPLE(2, 100, GameSoundEffect.YUM_COLLECT, false),
		ORANGE(3, 100, GameSoundEffect.YUM_COLLECT, false),
		PEAR(4, 100,GameSoundEffect.YUM_COLLECT, false),
		PURPLE_GRAPES(5, 100, GameSoundEffect.YUM_COLLECT, false),
		BLUE_GRAPES(6, 100, GameSoundEffect.YUM_COLLECT, false),
		BANANA(7, 100, GameSoundEffect.YUM_COLLECT, false),
		ENERGY(8, 0, GameSoundEffect.POWERUP_SHIELD, false) {
			@Override public void affectBonzo(Goodie goodie, Bonzo bonzo, World world, SoundManager sound) {
				bonzo.incrementHealth(GameConstants.LIFE_INCREASE);
			}
		},
		X2MULTIPLIER(9, 0, GameSoundEffect.POWERUP_SHIELD, false) {
			@Override public Powerup powerupForGoodie() { return Powerup.X2; }
		},
		WHITE_MELRODE_WINGS(10, 0, GameSoundEffect.POWERUP_WING, true) {
			@Override public Powerup powerupForGoodie() { return Powerup.WING; }
		},
		SHIELD(11, 0, GameSoundEffect.POWERUP_SHIELD, true) {
			@Override public Powerup powerupForGoodie() { return Powerup.SHIELD; }
		},
		EXTRA_LIFE(12, 200, GameSoundEffect.POWERUP_EXTRA_LIFE, false) {
			@Override public void affectBonzo(Goodie goodie, Bonzo bonzo, World world, SoundManager sound) {
				bonzo.incrementLives(1);
			}
		},
		X3MULTIPLIER(13, 0, GameSoundEffect.POWERUP_SHIELD, false) {
			@Override public Powerup powerupForGoodie() { return Powerup.X3; }
		},
		X4MULTIPLIER(14, 0, GameSoundEffect.POWERUP_SHIELD, false) {
			@Override public Powerup powerupForGoodie() { return Powerup.X4; }
		};
		
		private final int xOffset;
		public final int score;
		private final GameSoundEffect soundEffect;
		private final boolean persistant;
		
		/**
		 * 
		 * @param xOffset
		 * 		offset in the spirtesheet for drawing, in units (basically id)
		 * 
		 * @param score
		 * 		score that will be added to bonzo upon grabbing this goodie
		 * 
		 * @param soundEffect
		 * 		sound effect played when bonzo grabs goodie
		 * 
		 * @param persistant
		 * 		{@code true} to allow the goodie to return to the world when the screen
		 * 		it is on is reset (leaving the scren, bonzo dying) {@code false} if otherwise
		 * 
		 */
		private Type(final int xOffset, final int score, final GameSoundEffect soundEffect, final boolean persistant) {
			this.xOffset = xOffset;
			this.score = score;
			this.soundEffect = soundEffect;
			this.persistant = persistant;
		}
		
		/**
		 * 
		 * Returns the x position in the sprite sheet that goodies of this type draw from.
		 * 
		 * @return
		 */
		public int getDrawX() { return xOffset * GameConstants.GOODIE_SIZE_X; }
		
		/**
		 * 
		 * Returns the y position in the sprite sheet that goodies of this type draw from
		 * 
		 * @return
		 */
		public int getDrawY() { return 0; }
		
		/**
		 * 
		 * Returns a goodie type based on the given index value. Goodies are numbered 0-14, and when reference in the
		 * sprite sheet their 'id' is effectively their 'x offset * tile size' in the sprite sheet. Intended only for
		 * systems where an id is required to be resolved, such as the level editor or the save file format for id
		 * resolution.
		 * 
		 * @param val
		 * 		the integer value
		 * 
		 * @return
		 * 		the goodie type for this id
		 * 
		 * @throws IllegalArgumentException
		 * 		if the given id is not within the 0-14 bounds
		 * 
		 */
		public static Type byValue(int val) {
			for (Type g : Type.values() ) {
				if (g.xOffset == val) return g;
			}
			throw new IllegalArgumentException("Value out of range");
		}
		
		/**
		 * 
		 * Returns the powerup that this goodie represents. If this goodie is not a powerup, returns {@code null}
		 * 
		 * @return
		 * 		powerup for goodie or {@code null} if goodie not a powerup
		 * 
		 */
		public Powerup powerupForGoodie() { return null; }

		/**
		 * 
		 * Returns the numerical id of the enumeration. This is used for encoding the goodie
		 * for .world files.
		 * 
		 * @return
		 * 		unique id for enumeration.
		 * 
		 */
		public int id() {
			// Id == xOffset.
			return xOffset;
		}
		
		/**
		 * 
		 * Some goodies produce different effects on bonzo. The default is to do nothing (except increment
		 * any, if valid, score).
		 * 
		 * @param goodie
		 * 		reference to the actual goodie itself
		 * 
		 * @param bonzo
		 * 		reference to bonzo to affect him
		 * 
		 * @param world
		 * 		reference to world in case effects a more global, such as red and blue keys
		 * 
		 */
		public void affectBonzo(Goodie goodie, Bonzo bonzo, World world, SoundManager sound) 
			{ /* No op by default, overriden where required */ }
	}
	
	// These values indicate starting information for object construction. ONLY THESE
	// THREE VALUES determine object equality and hashcode.
	private final Type goodieType;
	private final int screenID;
	private final ImmutablePoint2D location;	
	
	// The sheet that contains all the goodies

	
	// These values represent game state information
	// Taken means gone but animating the YUM. Dead means gone.
	private boolean taken;
	private boolean dead;
	private int yumSprite;
	
	// Drawing info
	private int drawToX;
	private int drawToY;
	private int drawX;
	private int drawY;
	
	public int getDrawToX() { return drawToX; }
	public int getDrawToY() { return drawToY; }
	public int getDrawX() { return drawX; }
	public int getDrawY() { return drawY; }
	/** Goodie was taken and is now animating yum */
	public boolean isTaken() { return taken; }
	/** Goodie was taken, yum animation over, it's gone */
	public boolean isDead() { return dead; }
	/** If goodie is taken, but not dead, returns the current phase in yum animation. */
	public int getYumSprite() { return yumSprite; }
	
	// When this reaches TICKS_BETWEEN_ANIMATIONS, the y-level for drawing swaps between either the top
	// row or the bottom row.
	private int timeToNextFrame = 0;
	private static final int TICKS_BETWEEN_ANIMATIONS = 5;
	
}
