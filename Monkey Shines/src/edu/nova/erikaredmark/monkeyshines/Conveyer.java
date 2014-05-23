package edu.nova.erikaredmark.monkeyshines;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

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
	
	private final BufferedImage conveyerSheet;
	
	public Conveyer(final int id, final Rotation rotation, final BufferedImage conveyerSheet) {
		this.id = id;
		this.rotation = rotation;
		this.conveyerSheet = conveyerSheet;
	}
	
	/** Intended for test methods */
	int getId() { return id; }
	/** Intended for test methods */
	Rotation getRotation() { return rotation; }
	
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
	public void paint(Graphics2D g2d, int drawToX, int drawToY, int animationStep) {
		assert animationStep >= 0 && animationStep < 5;
		
		// X position depends 100% on animation step
		int drawFromX = animationStep * GameConstants.TILE_SIZE_X;
		
		// ySet indicates the set of conveyer belts an id
		// is specified for.
		int ySet = CONVEYER_SET_SIZE * id;
		
		// Y position is either the same as ySet for clockwise, or ySet + TILE_SIZE_Y for anti-clockwise
		int drawFromY = ySet + rotation.drawYOffset();
		
		g2d.drawImage(conveyerSheet, drawToX , drawToY, 											// Destination 1 (top left)
					  drawToX + GameConstants.TILE_SIZE_X, drawToY + GameConstants.TILE_SIZE_Y,     // Destination 2 (bottom right)
					  drawFromX, drawFromY, 													    // Source 1 (top Left)
					  drawFromX + GameConstants.TILE_SIZE_X, drawFromY + GameConstants.TILE_SIZE_Y, // Source 2 (bottom right)
					  null);
	}
	
	public enum Rotation {
		CLOCKWISE {
			@Override public void move(final Point2D point) {
				point.translateX(GameConstants.CONVEYER_SPEED);
			}
			
			@Override public int drawYOffset() { return 0; }
		},
		ANTI_CLOCKWISE {
			@Override public void move(final Point2D point) {
				point.translateX(-GameConstants.CONVEYER_SPEED);
			}
			
			@Override public int drawYOffset() { return GameConstants.TILE_SIZE_Y; }
		};
		
		/**
		 * 
		 * Moves the given point by the given conveyer belt speed (a constant).
		 * This method is intended to be called per tick if relevant.
		 * 
		 * @param point
		 * 		the point to move. This object is modified by the method
		 * 	
		 */
		public abstract void move(Point2D point);
		
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
}
