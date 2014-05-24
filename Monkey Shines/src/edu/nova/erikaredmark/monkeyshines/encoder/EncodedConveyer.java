package edu.nova.erikaredmark.monkeyshines.encoder;

import java.io.Serializable;

import edu.nova.erikaredmark.monkeyshines.Conveyer;
import edu.nova.erikaredmark.monkeyshines.Conveyer.Rotation;

/**
 * 
 * Encoded form for the immutable state of the conveyer belt.
 * 
 * @author Erika Redmark
 *
 */
public final class EncodedConveyer implements Serializable {
	private static final long serialVersionUID = -8963663081909020192L;

	// Can be used to get the graphics context when inflated
	private final int id;
	
	// Direction facing is an enum and serializable
	private final Rotation rotation;
	
	private EncodedConveyer(final int id, final Rotation rotation) {
		this.id = id;
		this.rotation = rotation;
	}
	
	/**
	 * 
	 * Constructs the encoded form of this object from the Conveyer instance.
	 * 
	 * @param conveyer
	 * 		the conveyer instance to encode
	 * 
	 * @return
	 * 		instance of this class
	 * 
	 */
	public static EncodedConveyer from(final Conveyer conveyer) {
		return new EncodedConveyer(conveyer.getId(), conveyer.getRotation() );
	}
	
	public Rotation getRotation() { return rotation; }
	public int getId() { return id; }
}
