package edu.nova.erikaredmark.monkeyshines.encoder;

import java.io.Serializable;

import edu.nova.erikaredmark.monkeyshines.Goodie;
import edu.nova.erikaredmark.monkeyshines.ImmutablePoint2D;

/**
 * A serialisable class that maintains the static state data of everything required to recreate a Goodie. This includes
 * the following things:
 * <br/>
 * <ul>
 * <li> id of the goodie (Key, Orange, Apple...) </li>
 * <li> id of the screen the goodie will be drawn to </li>
 * <li> location of the goodie on the screen, as an immutable point </li>
 * </ul>
 * <strong> Only these proxy classes are serialised.</strong>. The regular classes are free to evolve as long as no
 * changes are made to the static contents of the level.
 * <p/>
 * Instances of this class are immutable.
 * 
 * @author Erika Redmark
 */
public final class EncodedGoodie implements Serializable {
	private static final long serialVersionUID = 198L;
	
	final int goodieId;
	final int screenId;
	final ImmutablePoint2D location;
	
	private EncodedGoodie(final int goodieId,
					      final int screenId,
					      final ImmutablePoint2D location) {
		
		this.goodieId = goodieId;
		this.screenId = screenId;
		this.location = location;
	}
	
	/**
	 * Creates an encoded version of the goodie based on a goodie instance
	 * 
	 * @param goodie
	 * 
	 * @return
	 * 		encoded goodie maintaing static intiailisation state of the target goodie
	 */
	public static EncodedGoodie from(Goodie goodie) {
		final int _goodieId = goodie.getGoodieID();
		final int _screenId = goodie.getScreenID();
		final ImmutablePoint2D _location = ImmutablePoint2D.from(goodie.getLocation() );
		
		return new EncodedGoodie(_goodieId, _screenId, _location);
	}
}
