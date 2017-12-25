package org.erikaredmark.monkeyshines.resource;

import java.util.Optional;

import javax.sound.sampled.Clip;

import org.newdawn.slick.Image;

/**
 * Stores initialisation graphics and sound that should be loaded first before the rest of the
 * world is loaded. This is essentially the splash screen and music that should appear before the level
 * starts proper. 
 */
public class InitResource {
	public final Image splashScreen;
	public final Optional<Clip> backgroundMusic;
	
	public InitResource(Image splash, Optional<Clip> bgm) {
		this.splashScreen = splash;
		this.backgroundMusic = bgm;
	}
}
