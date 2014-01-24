package edu.nova.erikaredmark.monkeyshines.encoder;

import java.io.Serializable;

import edu.nova.erikaredmark.monkeyshines.DeathAnimation;
import edu.nova.erikaredmark.monkeyshines.Hazard;

/**
 * 
 * A serialisable class that maintains the static state data of a hazard. Generally, this would just be the hazard itself
 * but because it holds a reference to the resource image for drawing, it needs to be given back that resource properly on
 * re-construction. The only difference between this class and the actual {@code Hazard} is that this does not store
 * a BufferedImage pointer.
 * <strong> Only these proxy classes are serialised.</strong>. The regular classes are free to evolve as long as no
 * changes are made to the static contents of the level.
 * <p/>
 * Instances of this class are immutable.
 * 
 * @author Erika Redmark
 * 
 */
public final class EncodedHazard implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private final int id;
	private final boolean explodes;
	private final DeathAnimation deathAnimation;
	
	private EncodedHazard(final int id, final boolean explodes, final DeathAnimation deathAnimation) {
		this.id = id;
		this.explodes = explodes;
		this.deathAnimation = deathAnimation;
	}
	
	/**
	 * 
	 * Encodes the hazard for saving. This removes its graphics references.
	 * 
	 * @param hazard
	 * 		the hazard to encode
	 * 		
	 * @return
	 * 		an instance of this object
	 * 
	 */
	public static EncodedHazard from(final Hazard hazard) {
		return new EncodedHazard(hazard.getId(), hazard.getExplodes(), hazard.getDeathAnimation() );
	}
	
	public int getId() { return id; }
	public boolean getExplodes() { return explodes; }
	public DeathAnimation getDeathAnimation() { return deathAnimation; }
	
}
