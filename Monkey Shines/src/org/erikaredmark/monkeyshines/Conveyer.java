package org.erikaredmark.monkeyshines;

import java.awt.Graphics2D;

import org.erikaredmark.monkeyshines.resource.WorldResource;


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
	
	private static final int CONVEYER_SET_SIZE = GameConstants.TILE_SIZE_Y * 2;
	/**
	 * 
	 * Paints this conveyer belt to the given graphics context at the given cordinates. This is used by {@code ConveyerTile}, which 
	 * will compute and provide the position data/graphics context that this needs to draw on.
	 * 
	 * @param g2
	 * 
	 * @param drawToX
	 * 		x location to draw (in pixels)
	 * 
	 * @param drawToY
	 * 		y location to draw (in pixels)
	 * 
	 * @param animationStep
	 * 		A value between {@code 0 - 4}, as there are no other animation steps in a conveyer belt. If
	 * 		assertions are enabled, other values will fail. Otherwise, undefined behaviour.
	 * 
	 */
	public void paint(Graphics2D g2d, int drawToX, int drawToY, int animationStep, WorldResource rsrc) {
		assert animationStep >= 0 && animationStep < 5;
		
		// X position depends 100% on animation step
		int drawFromX = animationStep * GameConstants.TILE_SIZE_X;
		
		// ySet indicates the set of conveyer belts an id
		// is specified for.
		int ySet = CONVEYER_SET_SIZE * id;
		
		// Y position is either the same as ySet for clockwise, or ySet + TILE_SIZE_Y for anti-clockwise
		int drawFromY = ySet + rotation.drawYOffset();
		
		g2d.drawImage(rsrc.getConveyerSheet(), drawToX , drawToY, 									// Destination 1 (top left)
					  drawToX + GameConstants.TILE_SIZE_X, drawToY + GameConstants.TILE_SIZE_Y,     // Destination 2 (bottom right)
					  drawFromX, drawFromY, 													    // Source 1 (top Left)
					  drawFromX + GameConstants.TILE_SIZE_X, drawFromY + GameConstants.TILE_SIZE_Y, // Source 2 (bottom right)
					  null);
	}
	
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
		protected abstract int drawYOffset();
	}
	
	@Override public String toString() {
		return "Conveyer of id " + id + " with rotation " + rotation;
	}
}
