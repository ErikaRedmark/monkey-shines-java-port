package edu.nova.erikaredmark.monkeyshines.editor;

import edu.nova.erikaredmark.monkeyshines.DeathAnimation;
import edu.nova.erikaredmark.monkeyshines.Hazard;

/**
 * 
 * Represents the exact same properties of a {@code Hazard}, only mutable. This is intended for the level editors to easily
 * modify a hazard, and convert it from/back to an immutable hazard. Hazards are always represented in gameplay as their
 * immutable constructs. This is intended to allow one to easily make a mutable copy of that hazard for incremental changes,
 * and then produce another immutable hazard that would replace the original. 
 * <p/>
 * Not everything in this object is mutable. the ID may not be changed, and neither the sprite sheet. As of now, only the
 * death animation and whether or not it explodes are modifiable.
 * <p/>
 * Only editors should store this object.
 * <p/>
 * It is recommended instances of this class be created via {@code Hazard.mutableCopy() }
 * <p/>
 * This class is not thread safe.
 * 
 * @author Erika Redmark
 *
 */
public class HazardMutable {

	private boolean explodes;
	private DeathAnimation deathAnimation;
	
	// ID remains final: That is a function of where it is in the list.
	private final int id;
	
	public HazardMutable(final int id, final boolean explodes, final DeathAnimation deathAnimation) {
		this.id = id;
		this.explodes = explodes;
		this.deathAnimation = deathAnimation;
	}
	
	/**
	 * 
	 * Takes the current properties of this object at the time of the call and returns an immutable {@code Hazard} that
	 * represents those same properties. Further changes to this object will not affect the copy made at this time.
	 * 
	 * @return
	 * 		an immutable copy of this object
	 * 
	 */
	public Hazard immutableCopy() { return new Hazard(id, explodes, deathAnimation); }

	public boolean getExplodes() { return explodes; }
	public void setExplodes(boolean explodes) {	this.explodes = explodes; }

	public DeathAnimation getDeathAnimation() { return deathAnimation; }
	public void setDeathAnimation(DeathAnimation deathAnimation) { this.deathAnimation = deathAnimation; }
	
}
