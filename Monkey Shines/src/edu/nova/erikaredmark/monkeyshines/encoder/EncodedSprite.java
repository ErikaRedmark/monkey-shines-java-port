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
 * <li> Initial speed in both axis </li>
 * </ul>
 * <strong> Only these proxy classes are serialised.</strong>. The regular classes are free to evolve as long as no
 * changes are made to the static contents of the level.
 * <p/>
 * Instances of this class are immutable.
 * 
 * @author Erika Redmark
 */
public class EncodedSprite {
	
	private final int id;
	private final ImmutablePoint2D location;
	private final ImmutableRectangle boundingBox;
	private final int initialSpeedX;
	private final int initialSpeedY;
	
	private EncodedSprite(final int id, final ImmutablePoint2D location, final ImmutableRectangle boundingBox, final int initialSpeedX, final int initialSpeedY) {
		this.id = id;
		this.location = location;
		this.boundingBox = boundingBox;
		this.initialSpeedX = initialSpeedX;
		this.initialSpeedY = initialSpeedY;
	}
	
	public static EncodedSprite from(final Sprite s) {
		int _id = s.getId(); // TODO need to incorperate new refactorings to move graphics responsibility away from Sprite.
					 // TODO for now, all sprites are bees.
		ImmutablePoint2D _location = s.getStaringLocation();
		ImmutableRectangle _boundingBox = s.getBoundingBox();
		int _initialSpeedX = s.getInitialSpeedX();
		int _initialSpeedY = s.getInitialSpeedY();
		
		return new EncodedSprite(_id, _location, _boundingBox, _initialSpeedX, _initialSpeedY);
	}
	
	/**
	 * 
	 * Helper method for clients since level screens store an array of sprites. Converts an array of sprites into the
	 * corresponding array of encoded sprites.
	 * 
	 * @param s
	 * 
	 * @return
	 * 
	 */
	public static EncodedSprite[] fromAll(final Sprite[] sArr) {
		EncodedSprite[] returnSprites =
			new EncodedSprite[sArr.length];
		
		for (int i = 0; i < sArr.length; i++) {
			returnSprites[i] = from(sArr[i]);
		}
		
		return returnSprites;
	}

	public int getId() { return id; }
	public ImmutablePoint2D getLocation() { return location; }
	public ImmutableRectangle getBoundingBox() { return boundingBox; }
	public int getInitialSpeedX() { return initialSpeedX; }
	public int getInitialSpeedY() { return initialSpeedY; }
	
}
