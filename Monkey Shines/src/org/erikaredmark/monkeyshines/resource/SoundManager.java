package org.erikaredmark.monkeyshines.resource;

import java.beans.PropertyChangeListener;
import java.util.concurrent.TimeUnit;

import org.erikaredmark.monkeyshines.GameSoundEffect;

/**
 * Interface for sound manager types. This allows different implementations
 * for different sound systems.
 * <p/>
 * Property changes should use the predefined constants in {@code SoundSettings}
 * <p/>
 * All clip containers from {@code WorldResource} will use 
 * {@code Optional<Clip>}. Some, or all, clips may not be loaded.
 * @author Erika Redmark
 *
 */
public interface SoundManager extends PropertyChangeListener {

	/**
	 * 
	 * Plays the given sound effect one time. If the sound effect was already playing, it will stop it and restart
	 * from the beginning.
	 * <p/>
	 * If the given {@code WorldResource} has no sound for that effect, no sound is played. No sound is played if the
	 * volume has been set to 0.
	 * 
	 * @param effect
	 * 		the sound effect to play
	 * 
	 */
	void playOnce(GameSoundEffect effect);

	/**
	 * 
	 * Plays the given sound effect after the specified number of time have elapsed. If the
	 * sound effect is already playing, it will stop, and after the given time restart from the beginning.
	 * <p/>
	 * If the given {@code WorldResource} has no sound for that effect, no sound is played. No sound is played if the
	 * volume has been set to 0.
	 * <p/>
	 * This method is safe to use even if dispose happens after called but before the sound is played. The queued
	 * request contains all the value data required to play the sound even if the object is disposed.
	 * 
	 * @param effect
	 * 		the sound effect to play
	 * 
	 * @param delay
	 * 		the number of units of delay (see next parameter)
	 * 
	 * @param unit
	 * 		the measurement of the units in the previous argument, such as seconds or milliseconds
	 * 
	 */
	void playOnceDelayed(GameSoundEffect effect, int delay, TimeUnit unit);

	/**
	 * 
	 * Plays the given background music for this world. This is typically called once the game has started.
	 * If the world has no background music or the volume is set to 0, this method does nothing. This method
	 * also does nothing if music is already playing.
	 * 
	 */
	void playMusic();

	/**
	 * 
	 * Stops playing the background music for this world. Should be called before ending the game in progress.
	 * If no music is currently playing, this method does nothing.
	 * 
	 */
	void stopPlayingMusic();

}