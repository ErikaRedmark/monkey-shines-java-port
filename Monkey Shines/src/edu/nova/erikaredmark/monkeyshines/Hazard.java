package edu.nova.erikaredmark.monkeyshines;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;

import com.google.common.collect.ImmutableList;

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
 * Instances of this class are immutable
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
	
}
