package org.erikaredmark.monkeyshines.resource;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import org.erikaredmark.monkeyshines.GameSoundEffect;
import org.erikaredmark.monkeyshines.global.SoundSettings;
import org.erikaredmark.monkeyshines.global.SoundUtils;

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
public final class SoundManager implements PropertyChangeListener {

	// Use as source of sounds
	private final WorldResource rsrc;
	
	private boolean musicOff;
	// Set true if music is switched off by volume whilst in the middle of playing.
	private boolean musicCut;
	
	private boolean soundOff;

	// Created by WorldResource ONLY. That also handles registering/unregistering it from listening to the
	// SoundSettings global.
	SoundManager(final WorldResource rsrc) {
		this.rsrc = rsrc;
		setMusicVolume(SoundSettings.getMusicVolumePercent() );
		setSoundVolume(SoundSettings.getSoundVolumePercent() );
	}
	
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
	public void playOnce(GameSoundEffect effect) {
		if (soundOff)  return;
		
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
	 * If the world has no background music or the volume is set to 0, this method does nothing.
	 * 
	 */
	public void playMusic() {
		if (rsrc.backgroundMusic == null)  return;
		if (rsrc.backgroundMusic.isActive() )  return;
		if (musicOff)  return;
		
		rsrc.backgroundMusic.setFramePosition(0);
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
	
	/**
	 * 
	 * Automatically called on construction and game setting change to match clip volume to
	 * user defined levels. Does nothing if there is no background music
	 * 
	 * @param value
	 * 		percentage to set music volume to
	 * 
	 */
	private void setMusicVolume(int value) {
		if (rsrc.backgroundMusic == null)  return;
		
		if (value == 0) {
			musicOff = true;
			// unlike sounds, music must manually be shut off, and then back on again if required.
			if (rsrc.backgroundMusic.isRunning() ) {
				musicCut = true;
				rsrc.backgroundMusic.stop();
			}
			return;
		} else {
			// if the music was previously cut because it was already running, then and only then do
			// we resume it.
			if (musicCut) {
				musicCut = false;
				rsrc.backgroundMusic.start();
			}
		}
		
		musicOff = false;
		FloatControl gainControl = (FloatControl) rsrc.backgroundMusic.getControl(FloatControl.Type.MASTER_GAIN);
		float decibelLevelOffset = SoundUtils.resolveDecibelOffsetFromPercentage(value);
		// Music seems to be naturally louder than sound effects, so give it a negative nudge.
		decibelLevelOffset -= 10;
		System.out.println("Decibel offset for music: " + decibelLevelOffset);
		gainControl.setValue(decibelLevelOffset);
	}
	
	/**
	 * 
	 * Automatically called on construction and game setting change to match clip volume to
	 * user defined levels.
	 * 
	 * @param value
	 * 		percentage to set music volume to
	 * 
	 */
	private void setSoundVolume(int value) {
		if (value == 0) {
			soundOff = true;
			return;
		}
		
		soundOff = false;
		
		float decibelLevelOffset = SoundUtils.resolveDecibelOffsetFromPercentage(value);
		System.out.println("Decibel offset for sound: " + decibelLevelOffset);
		for (GameSoundEffect effect : GameSoundEffect.values() ) {
			Clip clip = rsrc.getSoundFor(effect);
			FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(decibelLevelOffset);
		}
	}

	/*
	 * Handles property change events from the settings preferences, whenever the user modifies a sound setting.
	 */
	@Override public void propertyChange(PropertyChangeEvent event) {
		switch (event.getPropertyName() ) {
		case SoundSettings.PROPERTY_MUSIC:
			setMusicVolume(SoundSettings.getMusicVolumePercent() );	
			break;
		case SoundSettings.PROPERTY_SOUND:
			setSoundVolume(SoundSettings.getSoundVolumePercent() );
			break;
		default:
			throw new RuntimeException("Unknown sound manager observer property " + event.getPropertyName() );
		}
	}
	
}
