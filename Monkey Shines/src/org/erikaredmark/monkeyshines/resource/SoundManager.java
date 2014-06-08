package org.erikaredmark.monkeyshines.resource;

import javax.sound.sampled.Clip;

import org.erikaredmark.monkeyshines.GameSoundEffect;

/**
 * 
 * Provided to all game objects capable of producing sounds; provides methods to indicate to the sound manager
 * to play certain sounds.
 * <p/>
 * The sound played, if at all, depends on the {@code WorldResource} used to construct this manager.
 * <p/>
 * Methods that change certain properties, such as volume, of a given sound effect, are stateful. All future
 * calls to that specific sound effect will use the previously selected properties.
 * 
 * @author Erika Redmark
 *
 */
public final class SoundManager {

	// Use as source of sounds
	private final WorldResource rsrc;

	// Created by WorldResource
	SoundManager(final WorldResource rsrc) {
		this.rsrc = rsrc;
	}
	
	/**
	 * 
	 * Plays the given sound effect one time. If the sound effect was already playing, it will stop it and restart
	 * from the beginning.
	 * <p/>
	 * If the given {@code WorldResource} has no sound for that effect, no sound is played.
	 * 
	 * @param effect
	 * 		the sound effect to play
	 * 
	 */
	public void playOnce(GameSoundEffect effect) {
		Clip clip = rsrc.getSoundFor(effect);
		if (clip != null) {
			if (clip.isActive() )  clip.stop();
			clip.setFramePosition(0);
			clip.start();
		}
	}
	
	/**
	 * 
	 * Plays the given sound effect for {@code amount} times, with {@code delayBetween} milliseconds between
	 * each successive playback.
	 * 
	 * @param effect
	 * 		the sound effect to play
	 * 
	 * @param amount
	 * 		the number of times to play
	 * 
	 * @param delayBetween
	 * 		time between each play of the sound
	 * 
	 */
	public void playRepeated(GameSoundEffect effect, int amount, int delayBetween) {
		// TODO method stub
	}
	
	/**
	 * 
	 * Plays the given sound effect in a loop, with {@code delayBetween} milliseconds between
	 * each successive playback. This does not stop playing the sound until {@code stopPlay} is
	 * used with the given sound. Be careful with this method, as not stopping a sound from looping
	 * in a reasonable amount of time will be very, very annoying.
	 * 
	 * @param effect
	 * 		the sound effect to play
	 * 
	 * @param delayBetween
	 * 		time between each play of the sound
	 * 
	 */
	public void playLoop(GameSoundEffect effect, int delayBetween) {
		// TODO method stub
	}
	
	/**
	 * 
	 * Stops the playback of a sound. If a sound was looping, either repated a certain number
	 * of times or infinite, it will no longer do so.
	 * 
	 * @param effect
	 * 		the sound effect to stop playing
	 * 
	 * @param immediate
	 * 		{@code true} to stop the sound instantly, {@code false} to let an already playing
	 * 		sound finish playing (although it won't be replayed)
	 * 
	 */
	public void stopPlay(GameSoundEffect effect, boolean immediate) {
		// TODO method stub
	}
	
}
