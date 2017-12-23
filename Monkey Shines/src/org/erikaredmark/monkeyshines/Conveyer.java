package org.erikaredmark.monkeyshines;


/**
 * 
 * Stores the immutable properties of a conveyer belt. Similiar to Hazards, instances of this class
 * are added to the world depending on the size of the conveyer belt sprite sheet. Since there is
 * no customisation, this happens automatically.
 * 
 * @author Erika Redmark
 *
 */
public final class Conveyer {
	// Together, id and rotation can map to a specific row of sprites.
	private int id;
	private Rotation rotation;
	
	public Conveyer(final int id, final Rotation rotation) {
		this.id = id;
		this.rotation = rotation;
	}
	
	/** Intended for test methods and encoder/decoders*/
	public int getId() { return id; }
	/** Intended for test methods and encoder/decoders*/
	public Rotation getRotation() { return rotation; }
	
	public static final int CONVEYER_SET_SIZE = GameConstants.TILE_SIZE_Y * 2;
	
	public enum Rotation {
		CLOCKWISE {
			@Override public double translationX() {
				return GameConstants.CONVEYER_SPEED;
			}
			
			@Override public int drawYOffset() { return 0; }
		},
		ANTI_CLOCKWISE {
			@Override public double translationX() {
				return -GameConstants.CONVEYER_SPEED;
			}
			
			@Override public int drawYOffset() { return GameConstants.TILE_SIZE_Y; }
		},
		/**
		 * Special enumeration used for Bonzo when he is on no conveyer belt. Moves him by 0
		 * amount. Removes need for null checking and extra branching.
		 */
		NONE {
			@Override public double translationX() { return 0; }
			@Override public int drawYOffset() { throw new UnsupportedOperationException("NONE is a valid rotation type ONLY for bonzo; cannot draw a non-rotating conveyer belt"); }
		};
		
		/**
		 * 
		 * Returns the number of pixels of translation that should be applied to bonzo
		 * when on a conveyer belt of this rotation.
		 * 	
		 */
		public abstract double translationX();
		
		/**
		 * 
		 * Returns the number of pixels in the Y direction from the beginning of a set 
		 * of conveyer belts that this conveyer should be drawn from. This really only
		 * has two values, {@code 0} for clockwise and {@code GameConstants.TILE_SIZE_Y}
		 * for anti-clockwise. Makes it easier to draw the right conveyer belt.
		 * 
		 */
		public abstract int drawYOffset();
	}
	
	@Override public boolean equals(Object o) {
		if (o == this)  return true;
		if (!(o instanceof Conveyer) )  return false;
		
		Conveyer other = (Conveyer) o;
		
		return    id == other.id
			   && rotation.equals(other.rotation);
	}
	
	@Override public int hashCode() {
		int result = 17;
		result += result * 31 + id;
		result += result * 31 + rotation.hashCode();
		return result;
	}
	
	@Override public String toString() {
		return "Conveyer of id " + id + " with rotation " + rotation;
	}
}
