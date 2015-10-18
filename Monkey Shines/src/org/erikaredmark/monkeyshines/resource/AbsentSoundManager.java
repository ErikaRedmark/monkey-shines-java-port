package org.erikaredmark.monkeyshines.resource;

import java.beans.PropertyChangeEvent;
import java.util.concurrent.TimeUnit;

import org.erikaredmark.monkeyshines.GameSoundEffect;

/**
 * SoundManager that does not actually play sounds. In effect, it is a 
 * dummy implementation that effectively turns off all game sounds, whether
 * they are loaded successfully or not. This can be used for debugging
 * purposes or, if the sound system completely fails, a fall-back so the
 * game doesn't just crash.
 * @author Erika Redmark
 *
 */
public class AbsentSoundManager implements SoundManager {
	@Override public void propertyChange(PropertyChangeEvent arg0) { }
	@Override public void playOnce(GameSoundEffect effect) { }
	@Override public void playMusic() { }
	@Override public void stopPlayingMusic() { }
	@Override public void playOnceDelayed(
		GameSoundEffect effect, 
		int delay, 
		TimeUnit unit) {		
	}

}
