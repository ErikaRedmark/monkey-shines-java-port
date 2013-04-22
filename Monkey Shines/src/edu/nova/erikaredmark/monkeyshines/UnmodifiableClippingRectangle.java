package edu.nova.erikaredmark.monkeyshines;

/**
 * 
 * An unmodifiable substitute for a clipping rectangle. This does not render the clipping rectangle immutable. It merely
 * prevents whoever has a reference to this object from modifying the clipping rectangle.
 * <p/>
 * Overrides all mutator methods and causes exceptions to be thrown.
 * 
 * @author Erika Redmark
 *
 */
public final class UnmodifiableClippingRectangle extends ClippingRectangle {

	private static final String UNMODIFIABLE = "Object unmodfiable from this reference";
	
	private UnmodifiableClippingRectangle(final ClippingRectangle wrapped) {
		super(wrapped);
	}
	
	public static UnmodifiableClippingRectangle of(final ClippingRectangle toWrap) {
		return new UnmodifiableClippingRectangle(toWrap);
	}
	
	@Override public void move(final int X, final int Y) { throw new UnsupportedOperationException(UNMODIFIABLE); }
	@Override public void setX(final int X) { throw new UnsupportedOperationException(UNMODIFIABLE); }
	@Override public void setY(final int Y) { throw new UnsupportedOperationException(UNMODIFIABLE); }
	@Override public void translate(final int unitsX, final int unitsY) { throw new UnsupportedOperationException(UNMODIFIABLE); }
	@Override public void translateX(final int units) { throw new UnsupportedOperationException(UNMODIFIABLE); }
	@Override public void translateY(final int units) { throw new UnsupportedOperationException(UNMODIFIABLE); }
	
	
}
