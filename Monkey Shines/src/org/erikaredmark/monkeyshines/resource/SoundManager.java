package org.erikaredmark.monkeyshines.resource;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

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
	 * Plays the given background music for this world. This is typically called once the game has started.
	 * If the world has no background music, this method does nothing.
	 * 
	 */
	public void playMusic() {
		if (rsrc.backgroundMusic == null)  return;
		if (rsrc.backgroundMusic.isActive() )  return;
		
		rsrc.backgroundMusic.setFramePosition(0);
		FloatControl gainControl = (FloatControl) rsrc.backgroundMusic.getControl(FloatControl.Type.MASTER_GAIN);
		gainControl.setValue(-15.0f);
		rsrc.backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
	}
	
	/**
	 * 
	 * Stops playing the background music for this world. Should be called before ending the game in progress.
	 * If no music is currently playing, this method does nothing.
	 * 
	 */
	public void stopPlayingMusic() {
		if (rsrc.backgroundMusic == null)  return;
		if (rsrc.backgroundMusic.isActive() ) {
			rsrc.backgroundMusic.stop();
		}
		
	}
	
}
