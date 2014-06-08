package org.erikaredmark.monkeyshines;

public enum AnimationSpeed {
	NORMAL(1, "Normal Animation"),
	SLOW(2, "Slow Animation");
	
	private final int ticksToUpdate;
	private final String displayString;
	
	private AnimationSpeed(int ticksToUpdate, String displayString) {
		this.ticksToUpdate = ticksToUpdate;
		this.displayString = displayString;
	}
	
	/**
	 * 
	 * Returns the number of ticks that must pass in game time before the sprite with this animation speed should be
	 * updated in the sprite sheet. This does NOT affect movement or other update logic, ONLY sprite animation!
	 * 
	 * @return
	 * 		ticks to update
	 * 
	 */
	public int getTicksToUpdate() {
		return ticksToUpdate;
	}
	
	@Override public String toString() {
		return displayString;
	}
}
