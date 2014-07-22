package org.erikaredmark.monkeyshines.resource;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import org.erikaredmark.monkeyshines.GameSoundEffect;
import org.erikaredmark.monkeyshines.global.SoundSettings;

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

	// Created by WorldResource
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
	 * user defined levels.
	 * 
	 * @param value
	 * 		percentage to set music volume to
	 * 
	 */
	private void setMusicVolume(int value) {
		FloatControl gainControl = (FloatControl) rsrc.backgroundMusic.getControl(FloatControl.Type.MASTER_GAIN);
		float decibelLevelOffset = resolveDecibelOffsetFromPercentage(value);
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
		float decibelLevelOffset = resolveDecibelOffsetFromPercentage(value);
		for (GameSoundEffect effect : GameSoundEffect.values() ) {
			Clip clip = rsrc.getSoundFor(effect);
			FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(decibelLevelOffset);
		}
	}
	
	/**
	 * 
	 * Resolves a percentage from 0-100 to a decibel level offset for setting clip volume. By default, all clips have
	 * a master gain of 0.0 decibels, which is already pretty loud. Based on constants defined in {@code GameConstants},
	 * the percentage is transformed into either a positive or more likely negative offset used to change the 'gain' on
	 * associated clips and either make them louder or softer.
	 * 
	 * @param value
	 * 		the percentage, from 0-100, of volume. 0 is always no volume and optionally can be handled separately by not
	 * 		playing the clip at all
	 * 
	 * @return
	 * 		decibel offset level. Returns {@code Float.MIN_VALUE} for volumes of 0%
	 * 
	 */
	private static float resolveDecibelOffsetFromPercentage(int value) {
		// TODO method stub
		return 0;
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
