package edu.nova.erikaredmark.monkeyshines;

import java.awt.Graphics;

import edu.nova.erikaredmark.monkeyshines.encoder.EncodedGoodie;
import edu.nova.erikaredmark.monkeyshines.graphics.WorldResource;

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
	private boolean isSkinned = false;
	
	// Static initialisation: Goodie sheet is shared by all Goodie objects, and only one instance should exist.
//	static {
//		try (InputStream goodiePath = "".getClass().getResourceAsStream("/resources/graphics/objects.gif");
//		     InputStream yumPath = "".getClass().getResourceAsStream("/resources/graphics/yummies.gif") ) {
//			
//		    goodieSheet = ImageIO.read(goodiePath);
//		    yumSheet = ImageIO.read(yumPath);
//		    
//		} catch (IOException e) {
//			System.out.println("Quand est la bien?");
//		}
//	}
	
	/**
	 * Creates a goodie for the specified screen, for the specified world. 
	 *
	 * @param type
	 * @param location
	 * @param screenID
	 */
	public static Goodie newGoodie(final Type type, final ImmutablePoint2D location, final int screenID, final WorldResource rsrc) {
		Goodie g = new Goodie(type, location, screenID);
		g.skin(rsrc);
		return g;
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
	

	/**
	 * 
	 * Creates an instance of this object from its encoded for.
	 * 
	 * @param value
	 * 
	 * @return
	 * 
	 */
	public static Goodie inflateFrom(EncodedGoodie value) {
		return new Goodie(value.getGoodieType(), value.getLocation(), value.getScreenId() );
	}
	
	public void skin(final WorldResource rsrc) {
		this.rsrc = rsrc;
		isSkinned = true;
	}
	
	public boolean isSkinned() { return isSkinned; }
	
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
	
	public void take() {
		taken = true;
	}
	
	public int getScreenID() {
		return screenID;
	}
	
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
		
		// TODO intended to be removed. Allows old XML parse code to still work. new version should serialise enum
		@Deprecated
		public static Type byValue(int val) {
			for (Type g : Type.values() ) {
				if (g.xOffset == val) return g;
			}
			throw new IllegalArgumentException("Value out of range");
		}
	}
	
}
