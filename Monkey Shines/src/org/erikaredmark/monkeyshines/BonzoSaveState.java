package org.erikaredmark.monkeyshines;

import org.erikaredmark.monkeyshines.Conveyer.Rotation;

/**
 * Represents a saved state of bonzo. During respawn, bonzo must appear in the right position, in
 * the same state he was. This includes jump state and velocity.
 * @author Erika Redmark
 *
 */
public class BonzoSaveState {
	public final int x, y, velX, velY;
	public boolean jumping;
	public Rotation rotation;
	
	
	public BonzoSaveState(final int x, final int y, final int velX, 
			final int velY, final boolean jumping, final Rotation rotation) {
		this.x = x; this.y = y; this.velX = velX; this.velY = velY; this.jumping = jumping; this.rotation = rotation;
	}
	
	/**
	 * Creates a basic save state with all types explicitly set
	 */
	public static BonzoSaveState of(final int x, final int y, final int velX, 
			final int velY, final boolean jumping, final Rotation rotation) {
		return new BonzoSaveState(x, y, velX, velY, jumping, rotation);
	}
	
	/**
	 * Creates a save state with only a pre-existing point. Many types of respawn will use some ground
	 * location, but will not need to save any data about Bonzo, which would mean respawning him with defaults
	 */
	public static BonzoSaveState fromPoint(ImmutablePoint2D point) {
		return new BonzoSaveState(point.x(), point.y(), 0, 0, false, Rotation.NONE);
	}
}
