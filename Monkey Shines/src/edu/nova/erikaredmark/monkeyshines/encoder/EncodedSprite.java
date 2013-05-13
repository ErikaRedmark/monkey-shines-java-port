package edu.nova.erikaredmark.monkeyshines.encoder;

import edu.nova.erikaredmark.monkeyshines.ImmutablePoint2D;
import edu.nova.erikaredmark.monkeyshines.ImmutableRectangle;
import edu.nova.erikaredmark.monkeyshines.Sprite;

/**
 * A serialisable class that maintains the static state data of everything required to recreate a sprite. This includes
 * the following things:
 * <br/>
 * <ul>
 * <li> Id of the sprite. This indicates which sprite sheet to load </li>
 * <li> Starting location </li>
 * <li> Bounding box </li>
 * </ul>
 * <strong> Only these proxy classes are serialised.</strong>. The regular classes are free to evolve as long as no
 * changes are made to the static contents of the level.
 * <p/>
 * Instances of this class are immutable.
 * 
 * @author Erika Redmark
 */
public class EncodedSprite {
	
	private int id;
	private ImmutablePoint2D location;
	private ImmutableRectangle boundingBox;
	
	private EncodedSprite(final int id, final ImmutablePoint2D location, final ImmutableRectangle boundingBox) {
		this.id = id;
		this.location = location;
		this.boundingBox = boundingBox;
	}
	
	public static EncodedSprite from(final Sprite s) {
		int _id = 0; // TODO need to incorperate new refactorings to move graphics responsibility away from Sprite.
					 // TODO for now, all sprites are bees.
		ImmutablePoint2D _location = s.getStaringLocation();
		ImmutableRectangle _boundingBox = s.getBoundingBox();
		
		return new EncodedSprite(_id, _location, _boundingBox);
	}
	
	/**
	 * 
	 * Helper method for clients since level screens store an array of sprites. Converts an array of sprites into the
	 * corresponding array of encoded sprites.
	 * 
	 * @param s
	 * @return
	 */
	public static EncodedSprite[] fromAll(final Sprite[] sArr) {
		EncodedSprite[] returnSprites =
			new EncodedSprite[sArr.length];
		
		for (int i = 0; i < sArr.length; i++) {
			returnSprites[i] = from(sArr[i]);
		}
		
		return returnSprites;
	}
	
}
