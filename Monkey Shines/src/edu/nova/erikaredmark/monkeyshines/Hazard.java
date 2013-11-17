package edu.nova.erikaredmark.monkeyshines;

import java.io.Serializable;


/**
 * 
 * Represents a hazard type in a world. Each world defines its own hazards. Hazards have an id (used to indicate which
 * graphic to display) along with other properties that affect how the hazard operates. This controls what death animation
 * bonzo goes through, and whether the hazard should explode on contact.
 * <p/>
 * Note that hazards that explode, whether killing bonzo or if he has a shield just exploding, come back after a screen is 
 * re-entered.
 * <p/>
 * The serialized form is the default. All data in the class correctly indicates what is requierd to be saved. 
 * 
 * @author Erika Redmark
 *
 */
public final class Hazard implements Serializable {
	private static final long serialVersionUID = 8405382950872267355L;
	
	private final int id;
	private final boolean explodes;
	private final DeathAnimation deathAnimation;
	
	public Hazard(final int id, final boolean explodes, final DeathAnimation deathAnimation) {
		this.id = id;
		this.explodes = explodes;
		this.deathAnimation = deathAnimation;
	}
	
	/**
	 * 
	 * Determines if the given hazard explodes.
	 * 
	 * @return
	 * 		{@code true} if this hazard explodes, {@code false} if otherwise
	 * 
	 */
	public boolean explodes() {
		return this.explodes;
	}
	
}
