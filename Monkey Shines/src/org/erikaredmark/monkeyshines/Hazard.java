package org.erikaredmark.monkeyshines;

import java.awt.Graphics2D;
import java.util.List;

import org.erikaredmark.monkeyshines.editor.HazardMutable;
import org.erikaredmark.monkeyshines.resource.WorldResource;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;


/**
 * 
 * Represents a hazard type in a world. Each world defines its own hazards. Hazards have an id (used to indicate which
 * graphic to display) along with other properties that affect how the hazard operates. This controls what death animation
 * bonzo goes through, and whether the hazard should explode on contact.
 * <p/>
 * Note that hazards that explode, whether killing bonzo or if he has a shield just exploding, come back after a screen is 
 * re-entered.
 * <p/>
 * Instances of this class are immutable. Hazards are comparable to other hazards by id. The actual hazard on the world
 * is controlled by {@code HazardTile}, which contains the mutable state data.
 * <p/>
 * 
 * @author Erika Redmark
 *
 */
public final class Hazard implements Comparable<Hazard> {
	private final int id;
	private final boolean explodes;
	private final DeathAnimation deathAnimation;
	// if harmless, death animation should be ignored as bonzo can't die from it.
	private final boolean harmless;
	
	/**
	 * 
	 * Constructs a new hazard object for the world
	 * 
	 * @param id
	 * 		the id of the hazard. This maps to the column in the hazard resource as to which set of two icons to animate 
	 * 		between to indicate this hazard is in the world
	 * 
	 * @param explodes
	 * 		this hazard explodes. This means that, upon contact with bonzo, an explosion graphic will form and the hazard
	 * 		will vanish from the screen (it will return if the screen is reset)
	 * 
	 * @param deathAnimation
	 * 		the death animation that should be played when this hazard is contacted with bonzo
	 * 
	 * @param harmless
	 * 		if {@code true}, this hazard cannot kill bonzo and the deathAnimation is ignored.
	 * 
	 * @param rsrc
	 * 		graphics resource for the world this hazard is in so it may perform painting routines
	 * 
	 */
	public Hazard(final int id, final boolean explodes, final DeathAnimation deathAnimation, final boolean harmless) {
		this.id = id;
		this.explodes = explodes;
		this.deathAnimation = deathAnimation;
		this.harmless = harmless;
	}

	/**
	 * 
	 * Creates a copy of this hazard that is mutable. The returned mutable object can not modify this immutable object.
	 * 
	 * @return
	 * 		a mutable version of this object that can not affect the original
	 * 
	 */
	public HazardMutable mutableCopy() {
		return new HazardMutable(this.id, this.explodes, this.deathAnimation, this.harmless);
	}
	
	public DeathAnimation getDeathAnimation() { return deathAnimation; }
	public int getId() { return id; }
	public boolean getExplodes() { return explodes; }
	
	/**
	 * 
	 * If a hazard is harmless, it will not affect him in any way. This does NOT affect the explodes attribute. A harmless
	 * hazard can explode. It will just be like if Bonzo had a shield.
	 * <p/>
	 * Note: The original game had harmless hazards that locked Bonzo's sprite animation and made it look like he was 'sliding'.
	 * This port does NOT keep that (arguably a bug) effect.
	 * 
	 * @return
	 * 		{@code true} if the hazard is harmless and should not kill Bonzo, {@code false} if otherwise
	 * 
	 */
	public boolean isHarmless() { return harmless; }
	
	/**
	 * 
	 * Creates the initial hazards for the world based on the number of hazards to create. Each hazard defaults to being a
	 * 'bomb'. It explodes and has the burn death animation. Default created hazards are never 'harmless'
	 * <p/>
	 * Start indicates at what Id the hazards will be created starting at. This is typically 0 for a new set and a higher number
	 * for adding to an existing set.
	 * 
	 * @param start
	 * 		starting id, inclusive, of the generated hazards
	 * 
	 * @param count
	 * 		number of hazards to create. This is typically obtained via the graphics sprite sheet.
	 * 
	 * @param rsrc
	 * 		the number of hazards created is dependent on the graphics context's ability to paint them.
	 * 		Measurement information is needed from it.
	 * 
	 * @return
	 * 		list of newly created hazards. The list is immutable. The list contains {@code count} elements with hazard ids ordered from {@code start}
	 * 		to {@code start + (count - 1)}
	 * 
	 * 
	 */
	public static ImmutableList<Hazard> initialise(int start, int count, WorldResource rsrc) {
		ImmutableList.Builder<Hazard> hazards = new ImmutableList.Builder<>();
		for (int i = 0; i < count; i++) {
			hazards.add(new Hazard(start + i, true, DeathAnimation.BURN, false) );
		}
		
		return hazards.build();
	}
	
	/**
	 * 
	 * Determines if the given hazard explodes. Exploding hazards always do so when touched by Bonzo, regardless of
	 * him having a shield or being harmless (they just won't kill him in those cases)
	 * 
	 * @return
	 * 		{@code true} if this hazard explodes, {@code false} if otherwise
	 * 
	 */
	public boolean explodes() {
		return this.explodes;
	}
	
	/**
	 * 
	 * Paints this hazard to the given graphics context at the given cordinates. This is used by {@code HazardTile}, which 
	 * will compute and provide the position data/graphics context that this needs to draw on. The hazard object itself merely
	 * provides the reference to the graphics pointer for the world hazards.
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
	 * 		{@code 0} for first frame of hazard, {@code 1} for second. Other values will produce incorrect behaviour
	 * 
	 */
	public void paint(Graphics2D g2d, int drawToX, int drawToY, WorldResource rsrc, int animationStep) {
		assert animationStep == 0 || animationStep == 1;
		
		int drawFromX = id * GameConstants.TILE_SIZE_X;
		int drawFromY = animationStep * GameConstants.TILE_SIZE_Y;
		
		g2d.drawImage(rsrc.getHazardSheet(), drawToX , drawToY, 								    // Destination 1 (top left)
					  drawToX + GameConstants.TILE_SIZE_X, drawToY + GameConstants.TILE_SIZE_Y,     // Destination 2 (bottom right)
					  drawFromX, drawFromY, 													    // Source 1 (top Left)
					  drawFromX + GameConstants.TILE_SIZE_X, drawFromY + GameConstants.TILE_SIZE_Y, // Source 2 (bottom right)
					  null);
	}
	
	/**
	 * 
	 * Given a list of hazard in which a hazard of the same id as the replacement exists, replaces that hazard with the new replacement
	 * hazard.
	 * <p/>
	 * The list must be mutable, as it will be mutated by removing a reference to whatever hazard of that id already existed and replacing
	 * it with the replacement
	 * 
	 * @param existingHazards
	 * 		original list of hazards
	 * 
	 * @param replacement
	 * 		hazard to use to replace existing hazard of the same id
	 * 		
	 * @throws IllegalArgumentException
	 * 		if no hazard exists of the id of the replacement
	 * 
	 */
	public static void replaceHazard(List<Hazard> existingHazards, Hazard replacement) {
		// Find index of old hazard
		int placementIndex = -1;
		int index = 0;
		for (Hazard h : existingHazards) {
			if (h.getId() == replacement.getId() ) {
				placementIndex = index;
				break;
			}
			++index;
		}
		
		if (placementIndex == -1)  throw new IllegalArgumentException("No replacement hazard of id " + replacement.getId() );
		
		// Add replacement to same index
		existingHazards.remove(placementIndex);
		existingHazards.add(placementIndex, replacement);
	}
	
	/**
	 * 
	 * Orders hazards in ascending form based on their id. <strong> inconsistent with equals</strong> as this only cares
	 * about the numerical ordering of id and not graphics or other fields which can't be logically ordered and are normally
	 * equal in circumstances where this is used to sort a list.
	 * 
	 */
	@Override public int compareTo(final Hazard that) {
		if (this == that)  return 0;
		
		return Ints.compare(this.id, that.id);
	}
	
	/**
	 * 
	 * Equality of hazards depend on having the same values to all fields.
	 * 
	 */
	@Override public boolean equals(final Object obj) {
		if (this == obj)  return true;
		if (!(obj instanceof Hazard) )  return false;
		
		final Hazard that = (Hazard) obj;
		
		return    this.id == that.id
		       && this.explodes == that.explodes
		       && this.deathAnimation == that.deathAnimation;
	}
	
	@Override public int hashCode() {
		int result = 17;
		result += result * 31 + id;
		result += result * 31 + (explodes ? 1 : 0);
		result += result * 31 + deathAnimation.hashCode();
		return result;
	}
	
	/**
	 * 
	 * Returns the id of the hazard preceeded by the phrase "Hazard ". This is intended for debugging purposes only. No code should
	 * rely on the format of the string.
	 * 
	 */
	@Override public String toString() {
		return "Hazard " + id + ", " + (explodes ? "explodes" : "does not explode") + ", uses " + deathAnimation;
	}


}
