package edu.nova.erikaredmark.monkeyshines;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;

import edu.nova.erikaredmark.monkeyshines.graphics.WorldResource;


/**
 * 
 * Represents a hazard type in a world. Each world defines its own hazards. Hazards have an id (used to indicate which
 * graphic to display) along with other properties that affect how the hazard operates. This controls what death animation
 * bonzo goes through, and whether the hazard should explode on contact.
 * <p/>
 * Note that hazards that explode, whether killing bonzo or if he has a shield just exploding, come back after a screen is 
 * re-entered.
 * <p/>
 * Instances of this class are immutable. Hazards are comparable to other hazards by id.
 * <p/>
 * The serialized form is the default. All data in the class correctly indicates what is requierd to be saved. 
 * 
 * @author Erika Redmark
 *
 */
public final class Hazard implements Serializable, Comparable<Hazard> {
	private static final long serialVersionUID = 8405382950872267355L;
	
	private final int id;
	private final boolean explodes;
	private final DeathAnimation deathAnimation;
	
	// We take a WorldResource for consistency, but we need only save the hazard tile sheet
	private final BufferedImage hazardSheet;
	
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
	 * @param rsrc
	 * 		graphics resource for the world this hazard is in so it may perform painting routines
	 * 
	 */
	public Hazard(final int id, final boolean explodes, final DeathAnimation deathAnimation, WorldResource rsrc) {
		this.id = id;
		this.explodes = explodes;
		this.deathAnimation = deathAnimation;
		this.hazardSheet = rsrc.getHazardSheet();
	}
	
	/**
	 * 
	 * Private constructor intended for automatic creation of hazards within the same other group of hazards; instead of asking
	 * for a world resource, it asks for the actual sprite sheet (readily available in other hazards). This constructor is specialised
	 * for only static factories that generate new hazards.
	 * 
	 * @param hazardSheet
	 * 		the sprite sheet for the hazard
	 * 
	 */
	private Hazard(final int id, final boolean explodes, final DeathAnimation deathAnimation, final BufferedImage hazardSheet) {
		this.id = id;
		this.explodes = explodes;
		this.deathAnimation = deathAnimation;
		this.hazardSheet = hazardSheet;
	}
	
	/**
	 * 
	 * Constructs an instance of a hazard based off of the original hazard, but with the id modified
	 * 
	 * @param copy
	 * 		original hazard to copy from
	 * 
	 * @param newId
	 * 		the new id the new hazard should use
	 * 
	 */
	private Hazard(final Hazard copy, final int newId) {
		this.id = newId;
		this.explodes = copy.explodes;
		this.deathAnimation = copy.deathAnimation;
		this.hazardSheet = copy.hazardSheet;
	}
	
	public DeathAnimation getDeathAnimation() { return deathAnimation; }
	public int getId() { return id; }
	public boolean getExplodes() { return explodes; }
	/**
	 * 
	 * Creates the initial hazards for the world based on the number of hazards to create. Each hazard defaults to being a
	 * 'bomb'. It explodes and has the burn death animation.
	 * <p/>
	 * If new hazards are added to the graphics after the fact, the {@code EditHazardModel} and dialog should call this method
	 * to add more.
	 * <strong> It is not advisable to 'shrink' the size of a hazard sprite sheet and remove a hazard after creating the world.</strong>
	 * This will offset the internal ids keyed to each hazards properties with how they are displayed, causing hazards to behave 
	 * as if they were others. It would have to be manually corrected by the user if that was the case
	 * <p/>
	 * If assertions are enabled, this method will fail if the rsrc sprite sheet cannot support all the ids.
	 * 
	 * @param start
	 * 		starting id for the set of hazards to create. If creating a batch for a new world, this is {@code 0}. If adding to an existing
	 * 		world, this should be the id of the 'next' hazard to create (so if you have 3 hazards, id 0 - 2, and you wanted to add more, this
	 * 		value would have to be '3'.
	 * 
	 * @param count
	 * 		number of hazards to create. This is typically obtained via the graphics sprite sheet.
	 * 
	 * @param rsrc
	 * 		a world resource for assigning to the newly created hazards.
	 * 
	 * @return
	 * 		list of newly created hazards. The list is immutable. The list contains {@code count} elements with hazard ids ordered from {@code start}
	 * 		to {@code start + (count - 1)}
	 * 
	 * 
	 */
	public static ImmutableList<Hazard> initialise(int start, int count, WorldResource rsrc) {
		// Ensure that sprite sheet for hazards at time of call can actually draw up to the last id
		assert start + (count - 1) * GameConstants.TILE_SIZE_X <= rsrc.getHazardSheet().getWidth();
		
		ImmutableList.Builder<Hazard> hazards = new ImmutableList.Builder<>();
		for (int i = 0; i < count; i++) {
			hazards.add(new Hazard(start + i, true, DeathAnimation.BURN, rsrc) );
		}
		
		return hazards.build();
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
	
	/**
	 * 
	 * Returns the id of the hazard preceeded by the phrase "Hazard ". This is intended for debugging purposes only. No code should
	 * rely on the format of the string.
	 * 
	 */
	@Override public String toString() {
		return "Hazard " + id;
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
	public void paint(Graphics2D g2d, int drawToX, int drawToY, int animationStep) {
		assert animationStep == 0 || animationStep == 1;
		
		int drawFromX = id * GameConstants.TILE_SIZE_X;
		int drawFromY = animationStep * GameConstants.TILE_SIZE_Y;
		
		g2d.drawImage(hazardSheet, drawToX , drawToY, 											    // Destination 1 (top left)
					  drawToX + GameConstants.TILE_SIZE_X, drawToY + GameConstants.TILE_SIZE_Y,     // Destination 2 (bottom right)
					  drawFromX, drawFromY, 													    // Source 1 (top Left)
					  drawFromX + GameConstants.TILE_SIZE_X, drawFromY + GameConstants.TILE_SIZE_Y, // Source 2 (bottom right)
					  null);
	}
	
	/**
	 * 
	 * A simple way of generating a new hazard based on a list of pre-existing ones. This basically just adds a new hazard
	 * with defaults with provided od. This method ensures the generated hazard has a unique id from
	 * the other hazards in the list.
	 * <p/>
	 * This list <strong> must be sorted </strong>. The new hazard will be generated with the given id, and placed in
	 * the list. Every hazard with an id equal or greater to the new hazard will be removed and replaced with hazards
	 * that were identical to the originals but with the id changed.
	 * <p/>
	 * This method <strong> heavily modifies</strong> the passed list, re-arranging the hazards within to suit the 
	 * addition of the new hazard. The list will be sorted at the conclusion of this method assuming it was already sorted
	 * 
	 * @param existingHazards
	 * 		the existing hazards in the world. It is the responsibility of the caller to ensure the list is sorted, or
	 * 		this method has undefined behaviour. <strong> This parameter will be modified by this method </strong>
	 * 
	 * @param id
	 * 		the id of the new hazard
	 * 
	 * @param rsrc
	 * 		technically, this should not normally be needed since one would use the same resource as what the other
	 * 		hazards were constructed with; this is to allow this method to function if the list is empty
	 * 
	 */
	public static void newHazardTo(final List<Hazard> existingHazards, int id, final WorldResource rsrc) {
		if (id < 0)  throw new IllegalArgumentException("Hazard ids must be positive");
		checkSorted(existingHazards);
		// Actual method logic
		
		final Hazard newHazard = new Hazard(id, true, DeathAnimation.BURN, rsrc);
		
		// Hazards removed in the loop will have copies constructed with new ids.
		final List<Hazard> removedHazards = new ArrayList<>();
		
		{
			// State variable for loop only
			boolean deleting = false;
			for (Iterator<Hazard> it = existingHazards.iterator(); it.hasNext(); /* No op */) {
				final Hazard next = it.next();
				if (!(deleting) )  {
					int nextId = next.id;
					if (nextId >= id )  deleting = true;
				}
				// Intentionally not an else! if the first if statement set deleting to true, then the current hazard
				// must also be deleted.
				if (deleting) {
					it.remove();
					removedHazards.add(next);
				}
			}
		}
		
		// Sanity Check 2: If nothing was removed, id of new hazard better be exactly one more than last id
		if (removedHazards.isEmpty() ) {
			final int largestId = existingHazards.get(existingHazards.size() - 1).getId();
			if (id != largestId + 1 )
				throw new IllegalArgumentException("Cannot make new hazard with id " + id + " as it must be within 0 - " + largestId + 1);
		}
		
		// Post Condition: List contains hazards with ids 0-(id - 1) containing the original hazards.
		existingHazards.add(newHazard);
		
		// After addition, list is still sorted as it only contained ids less than the new hazard.
		
		// removedHazards should have hazards placed in it already in sorted order, so placing things back relying on
		// just the iteration order should still maintain the sorted order of the collection.
		for (Hazard h : removedHazards) {
			existingHazards.add(new Hazard(h, h.id + 1));
		}
		
		// Post Condition: existingHazards is unmodified up to insertion point for new hazard, and all old hazards with
		// greater ids were reconstructed in proper order after the new insertion with the value 1 added to their ids to
		// bump them up
	}
	
	/**
	 * 
	 * Removes the given hazard from the list, modifying both the list structure AND changing the hazards in the list
	 * to conform to a new set of ids (all hazards in the list after the hazard removed are taken down by 1 in their
	 * id). This <strong> requires that the list be sorted</strong>. Otherwise, behaviour is undefined
	 * 
	 * @param existingHazards
	 * 		the existing hazards before the removal. The list must be sorted or the result of this function is undefined
	 * 
	 * @param hazardToRemove
	 * 		the hazard to remove from the list
	 * 
	 * @throws IllegalArgumentException
	 * 		if the given hazard is not in the list
	 * 
	 */
	public static void removeHazard(final List<Hazard> existingHazards, final Hazard hazardToRemove) {
		checkSorted(existingHazards);
		
		{
			// state variable for loop only
			// Once set, all further iterations will replace the hazard at a given index with a new
			// one containing a new id.
			boolean deleted = false;
			for (ListIterator<Hazard> it = existingHazards.listIterator(); it.hasNext(); /* No op */) {
				Hazard next = it.next();
				if (!(deleted) ) {
					if (next.equals(hazardToRemove) ) {
						it.remove();
						deleted = true;
					}
				} else {
					it.set(new Hazard(next, next.getId() - 1) );
				}
			}
			
			// If deleted was never set to true, the hazard never existed in this list. This is an exception
			if (!(deleted) )  throw new IllegalArgumentException("Hazard " + hazardToRemove + " could not be removed from list as it didn't exist");
		}
					
		
		// TODO Erika you stopped here: Write this function then write test methods for both the add and remove functions
		// for hazards, then use them with the dialog box to allow user to add/remove hazards from a world
	}

	/**
	 * 
	 * Becomes a no-op if assertions are disabled: checks if a list is sorted.
	 * 
	 * @param existingHazards
	 * 		hazard list to check if sorted.
	 * 
	 */
	private static void checkSorted(final List<Hazard> existingHazards) {
		boolean assertions = false;
		assert assertions = true;
		if (assertions) {
			List<Hazard> existingHazardsSorted = new ArrayList<>(existingHazards);
			Collections.sort(existingHazardsSorted);
			assert existingHazardsSorted.equals(existingHazards);
		}
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
		       && this.hazardSheet.equals(that.hazardSheet)
		       && this.explodes == that.explodes
		       && this.deathAnimation == that.deathAnimation;
	}
}
