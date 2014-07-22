package org.erikaredmark.monkeyshines.global;

import org.erikaredmark.monkeyshines.resource.SoundManager;
import org.erikaredmark.util.ObservableModel;

/**
 * 
 * Static 'preferences' class that maintains global preference state during the running of
 * the application.
 * <p/>
 * A {@code SoundManager} can register for events if the changes are desired to be able to
 * take place in real time.
 * 
 * @author Erika Redmark
 *
 */
public final class SoundSettings {

	private SoundSettings() { }
	
	// Contained obsevable object for implementing the observable effect
	private static final ObservableModel listeningSoundManagers = new ObservableModel();
	public static final String PROPERTY_MUSIC = "mus";
	public static final String PROPERTY_SOUND = "snd";
	
	// Value from 0 - 100: indicates relative volume of the music that plays
	// during the game
	private static int musicVolumePercent = 50;
	
	// Value from 0 - 100: indicates relative volume of all sound effects
	private static int soundVolumePercent = 100;
	
	/**
	 * 
	 * Sets the volume of the music that will play, from 0 percent (no music) to 100 percent (loudest)
	 * 
	 * @param value
	 * 
	 * @throws IllegalArgumentException
	 * 		if the given value is not between [0, 100]
	 * 
	 */
	public static void setMusicVolumePercent(final int value) {
		if (!(isPercent(value) ) )  throw new IllegalArgumentException("Music volume must be in terms of percent");
		int oldValue = musicVolumePercent;
		musicVolumePercent = value;
		listeningSoundManagers.firePropertyChange(PROPERTY_MUSIC, oldValue, musicVolumePercent); 
	}
	
	/**
	 * 
	 * Sets the volume of the sound that will play, from 0 percent (no music) to 100 percent (loudest)
	 * 
	 * @param value
	 * 
	 * @throws IllegalArgumentException
	 * 		if the given value is not between [0, 100]
	 * 
	 */
	public static void setSoundVolumePercent(final int value) {
		if (!(isPercent(value) ) )  throw new IllegalArgumentException("Sound volume must be in terms of percent");
		int oldValue = soundVolumePercent;
		soundVolumePercent = value;
		listeningSoundManagers.firePropertyChange(PROPERTY_SOUND, oldValue, soundVolumePercent);
	}
	
	public static int getMusicVolumePercent() { return musicVolumePercent; }
	public static int getSoundVolumePercent() { return soundVolumePercent; }
	
	/**
	 * 
	 * Registers the given sound manager to listen for any changes in settings
	 * 
	 * @param manager
	 * 
	 */
	public static void registerSoundManager(SoundManager manager) {
		listeningSoundManagers.addPropertyChangeListener(PROPERTY_SOUND, manager);
		listeningSoundManagers.addPropertyChangeListener(PROPERTY_MUSIC, manager);
	}
	
	/**
	 * 
	 * Unregisters the given sound manager. This MUST be called before the resource containing the sound manager goes
	 * out of scope, or there will be a rather large resource leak.
	 * 
	 * @param manager
	 * 
	 */
	public static void unregisterSoundManager(SoundManager manager) {
		listeningSoundManagers.removePropertyChangeListener(PROPERTY_SOUND, manager);
		listeningSoundManagers.removePropertyChangeListener(PROPERTY_MUSIC, manager);
	}

	private static boolean isPercent(final int value) {
		return !(value < 0 || value > 100);
	}
	
}
