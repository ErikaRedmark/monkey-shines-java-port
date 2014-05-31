package edu.nova.erikaredmark.monkeyshines;

import java.awt.Graphics;

import edu.nova.erikaredmark.monkeyshines.resource.WorldResource;

public class Goodie {
	/**
	 * MAJOR TODO
	 * Must fix the fact that goodies appear on all screens.
	 */

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
	 * Tells the goodie that it has been taken. This starts the 'Yum' animation and plays the appropriate
	 * sound. A goodie may only be taken once. If a goodie is already taken, this method does nothing.
	 * 
	 */
	public void take() {
		if (taken)  return;
		
		taken = true;
		rsrc.getSoundManager().playOnce(GameSoundEffect.YUM_COLLECT);
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
		RED_KEY(0),
		BLUE_KEY(1),
		APPLE(2),
		ORANGE(3),
		PEAR(4),
		PURPLE_GRAPES(5),
		BLUE_GRAPES(6),
		BANANA(7),
		ENERGY(8),
		X2MULTIPLIER(9),
		WHITE_MELRODE_WINGS(10),
		SHIELD(11),
		EXTRA_LIFE(12),
		X3MULTIPLIER(13),
		X4MULTIPLIER(14);
		
		private final int xOffset;
		
		private Type(final int xOffset) {
			this.xOffset = xOffset;
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
	}
	
}
