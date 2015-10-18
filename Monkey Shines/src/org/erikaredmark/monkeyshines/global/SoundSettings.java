package org.erikaredmark.monkeyshines.global;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.erikaredmark.monkeyshines.global.PreferencePersistException;
import org.erikaredmark.monkeyshines.resource.AbsentSoundManager;
import org.erikaredmark.monkeyshines.resource.JavaDefaultSoundManager;
import org.erikaredmark.monkeyshines.resource.SoundManager;
import org.erikaredmark.monkeyshines.resource.WorldResource;
import org.erikaredmark.util.ObservableModel;

/**
 * 
 * Static 'preferences' class that maintains global preference state during the running of
 * the application.
 * <p/>
 * A {@code SoundManager} can register for events if the changes are desired to be able to
 * take place in real time.
 * <p/>
 * Preference changes are not persisted to the filesystem until told to do so. This is prevent
 * large numbers of FS writes when a user is playing around with preferences.
 * 
 * @author Erika Redmark
 *
 */
public final class SoundSettings {
	private static final String CLASS_NAME = "org.erikaredmark.monkeyshines.global.SoundSettings";
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
	
	private SoundSettings() { }
	
	// Contained obsevable object for implementing the observable effect
	private static final ObservableModel listeningSoundManagers = new ObservableModel();
	public static final String PROPERTY_MUSIC = "mus";
	public static final String PROPERTY_SOUND = "snd";
	
	// Value from 0 - 100: indicates relative volume of the music that plays
	// during the game
	private static int musicVolumePercent = MonkeyShinesPreferences.defaultMusicVolume();
	
	// Value from 0 - 100: indicates relative volume of all sound effects
	private static int soundVolumePercent = MonkeyShinesPreferences.defaultSoundVolume();
	
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
	
	public static void setVolumePercentForType(int value, final SoundType type) {
		switch(type) {
		case SOUND:
			setSoundVolumePercent(value);
			break;
		case MUSIC:
			setMusicVolumePercent(value);
			break;
		default:
			throw new RuntimeException("Unknown sound type " + type);
		}
	}
	
	public static int getMusicVolumePercent() { return musicVolumePercent; }
	public static int getSoundVolumePercent() { return soundVolumePercent; }
	public static int getVolumePercentForType(final SoundType type) {
		switch(type) {
		case SOUND: return getSoundVolumePercent();
		case MUSIC: return getMusicVolumePercent();
		default: throw new RuntimeException("Unknown sound type " + type);
		}
	}
	
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
	
	/**
	 *
	 * Updates preferences file (if possible) with changes. This is called manually so that playing around with
	 * preferences doesn't cause excessive disk usage. Only call when the preference is okayed or saved by the user.
	 * 
	 */
	public static void persist() throws PreferencePersistException {
		MonkeyShinesPreferences.persistSound();
	}

	/**
	 * Factory method for creating an instance of a subtype of {@code
	 * SoundManager}. The method will attempt to instantiate a sound system
	 * for the current environment, and if that fails will generate a 'no
	 * op' sound system. Failures to initialise will be logged automatically.
	 * <p/>
	 * If the sound system fails to initialise, it will NOT affect actual
	 * gameplay. It will just prevent the sounds and music from working
	 * properly.
	 * @param worldResource
	 * @return
	 */
	public static SoundManager setUpSoundManager(
			WorldResource worldResource) {
		try
		{
			SoundManager manager = new JavaDefaultSoundManager(worldResource);
			return manager;
		} catch (Exception e) {
			LOGGER.log(
				Level.SEVERE,
				"Sound system cannot be initialised; falling back to no sound system." + e.getMessage(),
				e
			);
			return new AbsentSoundManager();
		}
	}
	
}
