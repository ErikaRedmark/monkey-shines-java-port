package org.erikaredmark.monkeyshines;

/**
 * 
 * Represents a mapping of which keys on the keyboard correspond to which actions the player
 * may take. It is up to clients to map an incoming event to this object and then decide what
 * to do based on that event.
 * <p/>
 * The key bindings should be based on Slick2D key bindings.
 * <p/>
 * Objects of this class are immutable.
 * 
 * @author Erika Redmark
 *
 */
public final class KeyBindingsAwt {
	public final int left;
	public final int right;
	public final int jump;
	
	/**
	 * Constructs a new key binding instance using the given keys. The keys must be in a form for Slick2D to
	 * understand.
	 * 
	 * @param left
	 * 		key id to move bonzo left
	 * 
	 * @param right
	 * 		key id to move bonzo right
	 * 
	 * @param jump
	 * 		key id for a jump
	 * 
	 */
	public KeyBindingsAwt(int left, int right, int jump) {
		this.left = left;
		this.right = right;
		this.jump = jump;
	}
}
