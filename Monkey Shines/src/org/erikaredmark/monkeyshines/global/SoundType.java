package org.erikaredmark.monkeyshines.global;
/**
 * 
 * Indicates a type of sound in the game, be it music or a sound effect. Intended to allow volume control
 * via the user.
 * 
 * @author The Doctor
 *
 */
public enum SoundType {

	
	SOUND() {
		@Override public void adjustPercentage(int value) {
			SoundSettings.setSoundVolumePercent(value);
		}
	},
	MUSIC() {
		@Override public void adjustPercentage(int value) {
			SoundSettings.setMusicVolumePercent(value);
		}
	};
	
	/**
	 * 
	 * Called when slider updates the 'sound'. Depending on the sound type, the appropriate 
	 * thing (sound or music) will be adjusted.
	 * 
	 * @param value
	 * 		percentage from 0 - 100 of how loud the sound should be,
	 * 
	 */
	public abstract void adjustPercentage(int value);
}

