package org.erikaredmark.monkeyshines;

import java.awt.Graphics;

import org.erikaredmark.monkeyshines.resource.WorldResource;
/**
 * 
 * Represents a specific goodie instance on the map
 * 
 * @author Erika Redmark
 *
 */
public class Goodie {
	// These values indicate starting information for object construction
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
	
	private WorldResource rsrc;
	
	/**
	 * 
	 * Creates a goodie for the specified screen, for the specified world. 
	 *
	 * @param type
	 * 
	 * @param location
	 * 
	 * @param screenId
	 * 
	 */
	public static Goodie newGoodie(final Type type, 
								   final ImmutablePoint2D location, 
								   final int screenID, 
								   final WorldResource rsrc) {
		
		return new Goodie(type, location, screenID, rsrc);
	}
	
	private Goodie(final Type type, final ImmutablePoint2D location, final int screenId, final WorldResource rsrc) {
		this.screenID = screenId;
		this.location = location;
		this.rsrc = rsrc;
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
			if (drawY == 0)
				drawY = GameConstants.GOODIE_SIZE_Y;
			else
				drawY = 0;
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
		if (goodieType.persistant)  taken = false;
	}
	
	/**
	 * 
	 * Tells the goodie that it has been taken. This starts the 'Yum' animation and plays the appropriate
	 * sound. A goodie may normally only be taken once. If a goodie is already taken, this method does nothing.
	 * <p/>
	 * Some goodies are reset when a level screen is restarted. TODO this is not yet implemented.
	 * 
	 * @param bonzo
	 * 		a reference to bonzo, so that the goodies effects (score and misc.) may be applied to him.
	 * 
	 */
	public void take(final Bonzo bonzo) {
		if (taken)  return;
		
		taken = true;
		rsrc.getSoundManager().playOnce(goodieType.soundEffect);
		bonzo.incrementScore(goodieType.score);
		goodieType.affectBonzo(bonzo);
	}
	
	public int getScreenID() {
		return screenID;
	}
	
	/**
	 * 
	 * Paints the goodie based on state. If the goodie has not been taken yet, it just animates there. Once it is taken,
	 * it will display the "yum" (taken && !dead) until that animation completes and it is dead, in which from there
	 * it will not logner be painted.
	 * 
	 * @param g2d
	 */
	public void paint(Graphics g2d) {
		if (!taken && !dead)
			g2d.drawImage(rsrc.getGoodieSheet(), drawToX , drawToY, // Destination 1
					drawToX + GameConstants.GOODIE_SIZE_X, drawToY + GameConstants.GOODIE_SIZE_Y, // Destination 2
					drawX, drawY, drawX + GameConstants.GOODIE_SIZE_X, drawY + GameConstants.GOODIE_SIZE_Y,
					null);
		else if (taken && !dead) {
			g2d.drawImage(rsrc.getYumSheet(), drawToX , drawToY, // Destination 1
					drawToX + GameConstants.GOODIE_SIZE_X, drawToY + GameConstants.GOODIE_SIZE_Y, // Destination 2
					yumSprite * GameConstants.GOODIE_SIZE_X, 0, // Source 1
					yumSprite * GameConstants.GOODIE_SIZE_X + GameConstants.GOODIE_SIZE_X, GameConstants.GOODIE_SIZE_Y, // Source 2
					null);
		}
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


	
	public enum Type {
		// WARNING: xOffset is used as ID for encoding this in save file format!
		// Adding the concept of another row to this sprite sheet will require
		// modifying that logic!
		// TODO all scores are placeholders. Need to play original and determine score
		// for each piece!
		RED_KEY(0, 20, GameSoundEffect.YUM_COLLECT, false),
		BLUE_KEY(1, 20, GameSoundEffect.YUM_COLLECT, false),
		APPLE(2, 20, GameSoundEffect.YUM_COLLECT, false),
		ORANGE(3, 20, GameSoundEffect.YUM_COLLECT, false),
		PEAR(4, 20,GameSoundEffect.YUM_COLLECT, false),
		PURPLE_GRAPES(5, 30,GameSoundEffect.YUM_COLLECT, false),
		BLUE_GRAPES(6, 40, GameSoundEffect.YUM_COLLECT, false),
		BANANA(7, 500, GameSoundEffect.YUM_COLLECT, false),
		ENERGY(8, 20, GameSoundEffect.POWERUP_SHIELD, false) {
			@Override public void affectBonzo(Bonzo bonzo) {
				bonzo.incrementHealth(GameConstants.LIFE_INCREASE);
			}
		},
		X2MULTIPLIER(9, 0, GameSoundEffect.POWERUP_SHIELD, false),
		WHITE_MELRODE_WINGS(10, 20, GameSoundEffect.POWERUP_WING, true),
		SHIELD(11, 20, GameSoundEffect.POWERUP_SHIELD, true),
		EXTRA_LIFE(12, 100, GameSoundEffect.POWERUP_EXTRA_LIFE, false) {
			@Override public void affectBonzo(Bonzo bonzo) {
				bonzo.incrementLives(1);
			}
		},
		X3MULTIPLIER(13, 0, GameSoundEffect.POWERUP_SHIELD, false),
		X4MULTIPLIER(14, 0, GameSoundEffect.POWERUP_SHIELD, false);
		
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
		 * sprite sheet their 'id' is effectively their 'x offset' for drawing purposes. This method only exists for
		 * the purpose of the level editor in selecting a goodie from the sprite sheet to translate a selection into
		 * a type... it is not intended currently to be used anywhere else; use actual type objects instead.
		 * 
		 * @param val
		 * 		the integer value
		 * 
		 * @return
		 * 		the goodie type for this id
		 * 
		 * @throws
		 * 		IllegalArgumentException
		 * 			if the given id is not within the 0-14 bounds
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
		 * @param bonzo
		 * 		reference to bonzo to affect him
		 * 
		 */
		public void affectBonzo(Bonzo bonzo) { /* No op by default, overriden where required */ }
	}
	
}