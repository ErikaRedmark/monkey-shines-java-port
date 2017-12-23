package org.erikaredmark.monkeyshines.resource;

import java.beans.PropertyChangeEvent;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;

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
public final class JavaDefaultSoundManager implements SoundManager {

	// Use as source of sounds
	private final WorldResource rsrc;
	
	private boolean musicOff;
	// Set true if music is switched off by volume whilst in the middle of playing.
	private boolean musicCut;
	
	private boolean soundOff;
	
	// Intended for playing sounds after a delayed period of time.
	private final ScheduledExecutorService delaySound = Executors.newSingleThreadScheduledExecutor();

	// Created by WorldResource ONLY. That also handles registering/unregistering it from listening to the
	// SoundSettings global.
	public JavaDefaultSoundManager(final WorldResource rsrc) {
		this.rsrc = rsrc;
		setMusicVolume(SoundSettings.getMusicVolumePercent() );
		setSoundVolume(SoundSettings.getSoundVolumePercent() );
	}

	@Override public void playOnce(GameSoundEffect effect) {
		if (soundOff)  return;
		
		Optional<Clip> clip = rsrc.getSoundFor(effect);
		if (clip.isPresent() ) {
			Clip c = clip.get();
			if (c.isActive() )  c.stop();
			c.setFramePosition(0);
			c.start();
		}
	}

	@Override public void playOnceDelayed(
			final GameSoundEffect effect, 
			final int delay, 
			final TimeUnit unit) {
		
		if (soundOff)  return;
		
		rsrc.holdSound(effect);
		
		delaySound.schedule(
			new Runnable() { 
				@Override public void run() { 
					playOnce(effect);
					// Block this scheduled thread until sound is over
					Optional<Clip> clip = rsrc.getSoundFor(effect);
					if (clip.isPresent() ) {
						clip.get().addLineListener(new LineListener() {
							@Override public void update(LineEvent event) {
								if (event.getType() == Type.STOP) {
									rsrc.releaseSound(effect);
								}
							}
						});
					}
				} 
			}, 
			delay,
			unit
		);
	}
	
	/* (non-Javadoc)
	 * @see org.erikaredmark.monkeyshines.resource.SoundManager#playMusic()
	 */
	@Override public void playMusic() {
		if (rsrc.backgroundMusic.isPresent() ) {
			Clip mus = rsrc.backgroundMusic.get();
			if (mus.isActive() )  return;
			if (musicOff)  return;
			
			mus.setFramePosition(0);
			mus.loop(Clip.LOOP_CONTINUOUSLY);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.erikaredmark.monkeyshines.resource.SoundManager#stopPlayingMusic()
	 */
	@Override public void stopPlayingMusic() {
		if (rsrc.backgroundMusic.isPresent() ) {
			Clip mus = rsrc.backgroundMusic.get();
			if (mus.isActive() ) {
				mus.stop();
			}
		}
		
	}
	
	/**
	 * Automatically called on construction and game setting change to match clip volume to
	 * user defined levels. Does nothing if there is no background music
	 * 
	 * @param value
	 * 		percentage to set music volume to
	 */
	private void setMusicVolume(int value) {
		if (rsrc.backgroundMusic.isPresent() ) {
			Clip mus = rsrc.backgroundMusic.get();
			if (value == 0) {
				musicOff = true;
				// unlike sounds, music must manually be shut off, and then back on again if required.
				if (mus.isRunning() ) {
					musicCut = true;
					mus.stop();
				}
				return;
			} else {
				// if the music was previously cut because it was already running, then and only then do
				// we resume it.
				if (musicCut) {
					musicCut = false;
					mus.start();
				}
			}
	
			if (rsrc.backgroundMusic.isPresent() ) {
				musicOff = false;
				FloatControl gainControl = (FloatControl) rsrc.
					backgroundMusic.
					get().
					getControl(FloatControl.Type.MASTER_GAIN);
				float decibelLevelOffset = SoundUtils.resolveDecibelOffsetFromPercentage(value);
				// Music seems to be naturally louder than sound effects, so give it a negative nudge.
				decibelLevelOffset -= 10;
				System.out.println("Decibel offset for music: " + decibelLevelOffset);
				gainControl.setValue(decibelLevelOffset);
			} else {
				musicOff = true;
			}
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
	private void setSoundVolume(int value) {
		if (value == 0) {
			soundOff = true;
			return;
		}
		
		soundOff = false;
		
		float decibelLevelOffset = SoundUtils.resolveDecibelOffsetFromPercentage(value);
		System.out.println("Decibel offset for sound: " + decibelLevelOffset);
		for (GameSoundEffect effect : GameSoundEffect.values() ) {
			Optional<Clip> clip = rsrc.getSoundFor(effect);
			if (clip.isPresent() ) {
				FloatControl gainControl = (FloatControl)
					clip.get().getControl(FloatControl.Type.MASTER_GAIN);
				gainControl.setValue(decibelLevelOffset);
			}
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
