package edu.nova.erikaredmark.monkeyshines;


public enum AnimationType {
	INCREASING_FRAMES("Increasing Frames"),
	CYCLING_FRAMES("Cycling Frames");
	
	private final String displayText;
	
	private AnimationType(String display) {
		this.displayText = display;
	}
	
	@Override public String toString() {
		return displayText;
	}
}
