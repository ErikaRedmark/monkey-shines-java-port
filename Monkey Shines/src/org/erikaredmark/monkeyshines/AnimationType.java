package org.erikaredmark.monkeyshines;


/**
 * 
 * Represents how a sprite animates, in how it cycles through all its frames of animation.
 * 
 * @author Erika Redmark
 *
 */
public enum AnimationType {
	/**
	 * 
	 * Indicates a sprite travels from the leftmost frame to the rightmost (0, 1, 2, 3, 4, 5, 6, 7) and then
	 * back again (6, 5, 4, 3, 2, 1, 0) and then front again (1, 2, 3...) etc. Note that it never plays a 
	 * frame twice in succession. This is the most often used animation type as sprites very often return 
	 * backwards to their originl form to show many basic animations (walking, teeth clentching, looking
	 * left and right, etc...)
	 * 
	 */
	INCREASING_FRAMES("Increasing Frames"),
	/**
	 * 
	 * Indicates a sprite travels from leftmost frame to rightmost (0, 1, 2, 3, 4, 5, 6, 7) and then
	 * repeats the same sequence, effectively skipping from frame 7 to frame 0.
	 * 
	 */
	CYCLING_FRAMES("Cycling Frames");
	
	private final String displayText;
	
	private AnimationType(String display) {
		this.displayText = display;
	}
	
	@Override public String toString() {
		return displayText;
	}
}
