package org.erikaredmark.monkeyshines;

/**
 * 
 * Effectively a subset of {@code Goodie.Type}. Certain goodies don't merely just give an immediate effect,
 * like counting down the key count or granting score. Some stick with Bonzo for a while, and give latent
 * effects as long as they are active. A powerup is Active for bonzo one at a time; activiating a new one
 * deactivates any remaining ones.
 * <p/>
 * Powerups are not permenant and expire. Expiration time is granted based on 
 * {@code GameConstants.MAX_POWERUP_TIME} for the initial 'no warnings' phase,
 * {@code GameConstants.TIME_BETWEEN_FLASHES} for the delay between each flash of the powerup on/
 * off during UI animation, and {@code GameConstants.MAX_WARNINGS} for the number of times the warning
 * sound should play before the powerup goes away. Two flashes are made (one off one on) per warning.
 * Effectively, the total time bonzo will have a powerup is
 * {@code MAX_POWERUP_TIME + (MAX_WARNINGS * TIME_BETWEEN_FLASHES * 2) )}
 * 
 * Drawing routines take from the same sheet, the Goodie sheet. Each powerup holds the X offset
 * required to draw it when drawing to the UI. Only the top row is drawn for powerups; they do
 * not animate.
 * 
 * @author Erika Redmark
 *
 */
public enum Powerup {
	SHIELD(1, 11) {
		@Override public boolean isShield() { return true; }
	},
	WING(1, 10) {
		@Override public boolean isWing() { return true; }
	},
	X2(2, 9),
	X3(3, 13),
	X4(4, 14);
	
	private final int multiplier;
	private final int xOffset;
	private final int x2Offset;
	
	// Draw from Y axis are ALWAYS constant
	public static final int POWERUP_DRAW_FROM_Y = 0;
	public static final int POWERUP_DRAW_FROM_Y2 = GameConstants.GOODIE_SIZE_Y;
	
	private Powerup(final int multiplier, final int idInGoodieSheet) {
		this.multiplier = multiplier;
		this.xOffset = idInGoodieSheet * GameConstants.GOODIE_SIZE_X;
		this.x2Offset = xOffset + GameConstants.GOODIE_SIZE_X;
	}
	
	/**
	 * 
	 * Returns the offset in the goodie sheet that represents this powerup. drawFromY will always
	 * be 0.
	 * 
	 * @return
	 * 		x offset in goodie sheet for drawing
	 * 
	 */
	public int drawFromX() {
		return xOffset;
	}
	
	/**
	 * 
	 * Returns the endpoint offset in the goodie sheet for this powerup.
	 * 
	 * @return
	 * 		second x offset in goodie sheet for drawing
	 * 
	 */
	public int drawFromX2() {
		return x2Offset;
	}
	
	/**
	 * 
	 * A powerup acting as a shield prevents bonzo from all forms of death EXCEPT fall damage.
	 * 
	 * @return
	 * 		{@code true} if the powerup is a shield, {@code false} if otherwise
	 * 
	 */
	public boolean isShield() { return false; }
	
	/**
	 * 
	 * A powerup acting as wing prevents bonzo from ONLY death by fall. Technically speaking, it
	 * prevents any fall damage (so bonzo can never lose health due to a fall which would cause
	 * him to die)
	 * 
	 * @return
	 * 		{@code true} if the powerup is a wing, {@code false} if otherwise
	 * 
	 */
	public boolean isWing() { return false; }
	
	/**
	 * 
	 * This returns the muliplier the powerup applies. Every yum or 'goodie' with a point value
	 * has its value muliplied by this multiplier before being added to bonzos total.
	 * 
	 * @return
	 * 		multiplier for score. May be 1 for non-multiplier powerups
	 * 	
	 */
	public final int multiplier() {
		return multiplier;
	}
}
