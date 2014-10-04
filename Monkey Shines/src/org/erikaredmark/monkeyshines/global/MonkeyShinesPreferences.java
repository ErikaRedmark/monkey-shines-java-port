package org.erikaredmark.monkeyshines.global;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.erikaredmark.monkeyshines.KeyBindings;
import org.erikaredmark.util.BinaryLocation;

/**
 * 
 * Internal package class that, if possible (not required) will commit any preference changes made by the player
 * to a file in the same folder as the binary, in addition to loading the preferences file for setting the initial
 * values for the global static *Settings objects.
 * <p/>
 * This class is automatically intiailsed when a global preference object requests it's default preferences
 * on its initialisation.
 * 
 * @author Erika Redmark
 *
 */
public final class MonkeyShinesPreferences {
	private static final String CLASS_NAME = "org.erikaredmark.monkeyshines.global.MonkeyShinesPreferences";
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

	private static final Path PREFERENCES_EXPECTED = BinaryLocation.BINARY_FOLDER.resolve("monkeyshines.pref");

	private static final Properties PREF_INTERNAL = new Properties();

	
	// Preference defaults if a new preferences file must be created.
	private static final KeyBindings DEFAULT_BINDINGS = new KeyBindings(KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP);
	private static final int DEFAULT_MUSIC_VOLUME = 60;
	private static final int DEFAULT_SOUND_VOLUME = 100;
	private static final boolean DEFAULT_FULLSCREEN_MODE = false;
	
	// Names for each binding. 
	private static final String KEY_BINDING_LEFT = "keyleft";
	private static final String KEY_BINDING_RIGHT = "keyright";
	private static final String KEY_BINDING_JUMP = "keyjump";
	private static final String SOUND_VOLUME = "sound";
	private static final String MUSIC_VOLUME = "music";
	private static final String FULLSCREEN = "fullscreen";
	
	
	static {
		boolean preferencesFileAvailable = true;
		if (!(Files.exists(PREFERENCES_EXPECTED) ) ) {
			try {
				Files.createFile(PREFERENCES_EXPECTED);
			} catch (IOException e) {
				LOGGER.log(Level.WARNING,
						   "Preferences cannot be saved for this game session, nor can high scores! Unable to create preferences file: " + e.getMessage(),
						   e);
				preferencesFileAvailable = false;
			}
		}
		
		if (preferencesFileAvailable) {
			try (InputStream inStream = Files.newInputStream(PREFERENCES_EXPECTED) ) {
				PREF_INTERNAL.load(inStream);
				// If the file was just created or the game program logic has changed and added more values,
				// put defaults in for them now.
				if (!(PREF_INTERNAL.containsKey(KEY_BINDING_LEFT) ) ) {
					PREF_INTERNAL.setProperty(KEY_BINDING_LEFT, String.valueOf(DEFAULT_BINDINGS.left) );
				}
				
				if (!(PREF_INTERNAL.containsKey(KEY_BINDING_RIGHT) ) ) {
					PREF_INTERNAL.setProperty(KEY_BINDING_RIGHT, String.valueOf(DEFAULT_BINDINGS.right) );
				}
				
				if (!(PREF_INTERNAL.containsKey(KEY_BINDING_JUMP) ) ) {
					PREF_INTERNAL.setProperty(KEY_BINDING_JUMP, String.valueOf(DEFAULT_BINDINGS.jump) );
				}
				
				if (!(PREF_INTERNAL.containsKey(SOUND_VOLUME) ) ) {
					PREF_INTERNAL.setProperty(SOUND_VOLUME, String.valueOf(DEFAULT_SOUND_VOLUME) );
				}
				
				if (!(PREF_INTERNAL.containsKey(MUSIC_VOLUME) ) ) {
					PREF_INTERNAL.setProperty(MUSIC_VOLUME, String.valueOf(DEFAULT_MUSIC_VOLUME) );
				}
				
				if (!(PREF_INTERNAL.containsKey(FULLSCREEN) ) ) {
					PREF_INTERNAL.setProperty(FULLSCREEN, String.valueOf(DEFAULT_FULLSCREEN_MODE) );
				}
				
			} catch (IOException e) {
				LOGGER.log(Level.WARNING,
						   "Preferences cannot be saved for this game session, nor can high scores! Unable to load preferences file: possible preference corruption: " + e.getMessage(),
						   e);
			}
		}
		
	}
	
	/**
	 * 
	 * Returns the path to the expected preferences file. This file is NOT guaranteed to exist. Clients
	 * should make no attempt to create it themselves.
	 * 
	 * @return
	 * 		location the preferences file is expected to be
	 * 
	 */
	public static Path getPreferencesPath() { return PREFERENCES_EXPECTED; }
	
	/**
	 * 
	 * In order for other objects to store (such as highscores) if they have a reference to a properties file
	 * that is a preferences file, they must use the same comments.
	 * 
	 * @return
	 * 		comments for the {@code store} method in {@code Properties}
	 * 	
	 */
	public static String getPreferencesComments() { return "Monkey Shines Preferences File"; }


	static int defaultSoundVolume() { return Integer.valueOf(PREF_INTERNAL.getProperty(SOUND_VOLUME) ); }
	static int defaultMusicVolume() { return Integer.valueOf(PREF_INTERNAL.getProperty(MUSIC_VOLUME) ); }
	static boolean defaultFullscreen() { return Boolean.valueOf(PREF_INTERNAL.getProperty(FULLSCREEN) ); }
	static KeyBindings defaultKeyBindings() { 
		return new KeyBindings(Integer.valueOf(PREF_INTERNAL.getProperty(KEY_BINDING_LEFT) ), 
							   Integer.valueOf(PREF_INTERNAL.getProperty(KEY_BINDING_RIGHT) ), 
							   Integer.valueOf(PREF_INTERNAL.getProperty(KEY_BINDING_JUMP) ) ); 
	}
	
	private static void save() throws PreferencePersistException {
		try (OutputStream out = Files.newOutputStream(PREFERENCES_EXPECTED) ) {
			PREF_INTERNAL.store(out, getPreferencesComments() );
		} catch (IOException e) {
			throw new PreferencePersistException(e.getMessage(), e);
		}
	}

	static void persistKeyBindings() throws PreferencePersistException {
		PREF_INTERNAL.setProperty(KEY_BINDING_LEFT, String.valueOf(KeySettings.getBindings().left) );
		PREF_INTERNAL.setProperty(KEY_BINDING_RIGHT, String.valueOf(KeySettings.getBindings().right) );
		PREF_INTERNAL.setProperty(KEY_BINDING_JUMP, String.valueOf(KeySettings.getBindings().jump) );
		
		save();
	}
	
	static void persistSound() throws PreferencePersistException {
		PREF_INTERNAL.setProperty(SOUND_VOLUME, String.valueOf(SoundSettings.getSoundVolumePercent() ) );
		PREF_INTERNAL.setProperty(MUSIC_VOLUME, String.valueOf(SoundSettings.getMusicVolumePercent() ) );
		
		save();
	}
	
	static void persistVideo() throws PreferencePersistException {
		PREF_INTERNAL.setProperty(FULLSCREEN, String.valueOf(VideoSettings.isFullscreen() ) );
		
		save();
	}
	
}
