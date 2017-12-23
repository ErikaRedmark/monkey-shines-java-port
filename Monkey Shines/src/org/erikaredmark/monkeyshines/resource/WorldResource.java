package org.erikaredmark.monkeyshines.resource;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.sound.sampled.Clip;

import org.erikaredmark.monkeyshines.GameSoundEffect;
import org.erikaredmark.monkeyshines.global.SoundSettings;
import org.erikaredmark.monkeyshines.tiles.CommonTile.StatelessTileType;

/**
 * Unlike {@code World}, this contains all the graphics for a world, and only that. There is no level information. All worlds
 * must have some resource set otherwise they will not draw properly, and the resource should be sane to that world. By 'sane',
 * it means that:
 * <ol>
 * <li>For each different type of tile, there is a tile graphic. So if the maximum solid tile id for a world is 48, there better
 * be at least 48 rectangluar slots available in the graphic.</li>
 * <li>For each different type of Sprite, there is a sprite graphic sheet. Same rules of 'enough sprite graphic sheets' in terms
 * of sprite id apply here</li>
 * </ol>
 * <p/>
 * Instances of this class are publicly immutable until disposed. Once constructed with all available graphics resources, this can not change
 * until {@code dispose() } is called. This is mainly designed to release sound resources and should be thought of as the 'destructor' of this object.
 * Only call when about to otherwise remove references to an instance of this class.
 * <p/>
 * Graphics data is split into Slick and AWT compatible for use with both the editor and the
 * main game. Since only one or the other is ever active at once, only one type is loaded
 * based on the usage.
 * 
 * @author Erika Redmark
 *
 */
public final class WorldResource {
	
	/* --------------------------- Graphics --------------------------- */
	// Either reside in AWT compatible BufferedImages or Slick2D comptaible images
	// One is always null, and the other never is. The context of the game should 
	// mean that standard gameplay -> slick is always available
	// editor -> AWT is always available.
	final AwtWorldGraphics awtGraphics;
	final SlickWorldGraphics slickGraphics;
	
	/* --------------------------- SOUNDS ----------------------------- */
	// Whilst sounds are stored here, they should only be played via the
	// SoundManager. null sounds are possible; in that case, that means
	// that there is no sound available for a particular event.
	private final Map<GameSoundEffect, Optional<Clip>> sounds;
	private final Set<GameSoundEffect> holdSounds = new HashSet<>();
	
	// Package-private: Only intended for SoundManager
	final Optional<Clip> backgroundMusic;
	
	// Generated automatically in constructor
	private final SoundManager soundManager;


	/** Static factories call this with proper defensive copying. No defensive copying is done in constructor
	 */
	private boolean isDisposed;
	
	public WorldResource(
		final AwtWorldGraphics awtGraphics,
		final SlickWorldGraphics slickGraphics,
	    final Map<GameSoundEffect, Optional<Clip>> sounds,
	    final Optional<Clip> backgroundMusic) 
	{
		
		this.awtGraphics = awtGraphics;
		this.slickGraphics = slickGraphics;
		
		this.sounds = sounds;
		// May be null
		this.backgroundMusic = backgroundMusic;
		
		// Generated data
		// this pointer escapes, but no one gets a reference to the manager until construction is over
		// and the manager constructor itself calls no methods on this class.
		soundManager = SoundSettings.setUpSoundManager(this);
		// unregistered in dispose method
		SoundSettings.registerSoundManager(soundManager);

	}

	public static WorldResource createAwtResource(
		AwtWorldGraphics awtGraphics, 
		Map<GameSoundEffect, Optional<Clip>> sounds, 
		Optional<Clip> bgm) 
	{
		return new WorldResource(awtGraphics, null, sounds, bgm);
	}
	
	public static WorldResource createSlickResource(
		SlickWorldGraphics slickGraphics, 
		Map<GameSoundEffect, Optional<Clip>> sounds, 
		Optional<Clip> bgm) 
	{
		return new WorldResource(null, slickGraphics, sounds, bgm);
	}
	
	/**
	 * Returns {@code true} if this world resource is using Slick-style graphics,
	 * {@code false} for standard AWT Style.
	 * Slick is used for main gameplay.
	 * AWT is used for Level Editor.
	 * <p/>
	 * Some methods in this object only operate correctly in one context or the other.
	 * @return
	 */
	public boolean isSlickGraphics()
	{
		return slickGraphics != null;
	}
	
	/**
	 * 
	 * Returns the graphics sheet for the tiles that exist for the given tile type.
	 * <p/>
	 * For use only with editor; this will fail if used with regular game (assumes AWT
	 * style images.
	 * 
	 * @param type
	 * 		the type of the tile
	 * 
	 * @return
	 * 		a reference to the sprite sheet for the tiles
	 * 
	 * @throws IllegalStateException
	 * 		if Slick style graphics are loaded.
	 * 
	 */
	public BufferedImage getStatelessTileTypeSheet(final StatelessTileType type) {
		if (isSlickGraphics())
			{ throw new IllegalArgumentException("Tilesheets unavailable outside of level editor/slick graphics loaded"); }
		
		switch (type) {
			case SOLID: return awtGraphics.solidTiles;
			case THRU : return awtGraphics.thruTiles;
			case SCENE: return awtGraphics.sceneTiles;
			case NONE: throw new IllegalArgumentException("No tilesheet for NONE tiles");
			default: throw new IllegalArgumentException("Unknown tile type " + type);
		}
	}
	
	/**
	 * Returns the AWT graphics for the Level Editor. Fails when used from normal gameplay.
	 */
	public AwtWorldGraphics getAwtGraphics()
	{
		if (isSlickGraphics())
			{ throw new IllegalArgumentException("Request for AWT graphics when slick graphics loaded"); }
	
		return awtGraphics;
	}
	
	/**
	 * Returns Slick graphics for actual game. Fails when used from level editor.
	 * @return
	 */
	public SlickWorldGraphics getSlickGraphics()
	{
		if (!(isSlickGraphics()))
			{ throw new IllegalArgumentException("Request for Slick graphics when AWT Graphics loaded"); }
	
		return slickGraphics;
	}
	
	/**
	 * 
	 * Returns the clip for the given sound effect, or {@code null} if the sound effect has no clip. Incomplete
	 * resource packs may not contain all sounds.
	 * 
	 * @param effect
	 * 		the effect to get the sound clip for
	 * 
	 * @return
	 * 		the clip itself
	 * 
	 */
	Optional<Clip> getSoundFor(GameSoundEffect effect) {
		return sounds.get(effect);
	}
	
	public SoundManager getSoundManager() { return this.soundManager; }
	
	/**
	 * 
	 * The 'destructor' of this object. Only call when about to otherwise remove a reference to the given
	 * instance. Destroys all sound resources, and anything else claimed by this object that may not
	 * be released under normal gc.
	 * 
	 */
	public void dispose() {
		for (GameSoundEffect effect : sounds.keySet() ) {
			Optional<Clip> c = sounds.get(effect);
			if (c.isPresent() ) {
				if (!(isSoundHeld(effect) ) ) {
					c.get().close();
				}
			}
		}
		
		SoundSettings.unregisterSoundManager(soundManager);
		
		// Intended for anything that requires late disposal.
		isDisposed = true;
	}
	
	/**
	 * 
	 * Prevents the given sound effect from being disposed on the dispose call. This is intended for fine-tuned 
	 * resource holding in case a single effect is required later even if the rest of the world is disposed.
	 * <p/>
	 * It is an error to call this whilst a sound is already held
	 * 
	 * @param effect
	 * 		the effect to NOT dispose
	 * 
	 * @throws IllegalStateException
	 * 		if a hold is already on the sound
	 * 
	 */
	public void holdSound(GameSoundEffect effect) {
		if (!(holdSounds.add(effect) ) ) {
			throw new IllegalArgumentException("Sound effect " + effect + " already held in previous request");
		}
	}
	
	/**
	 * 
	 * Releases the resource, allowing it to be disposed. If this object was already disposed, the resource is closed
	 * as soon as this method returns. Otherwise, the resource becomes eligble to be destroyed on the next call to dispose.
	 * 
	 * @param effect
	 * 		the effect to release
	 * 
	 * @throws IllegalStateException
	 * 		if the resource is not already held
	 * 
	 */
	public void releaseSound(GameSoundEffect effect) {
		if (!(holdSounds.remove(effect) ) ) {
			throw new IllegalArgumentException("Sound effect " + effect + " was not previously held");
		}
		
		// Are we already disposed? Clean it now.
		if (isDisposed) {
			Optional<Clip> c = sounds.get(effect);
			c.get().close();
		}
	}
	
	/**
	 * 
	 * Determines if a resource is held. Held resources may not be destroyed until released.
	 * 
	 * @param effect
	 * 
	 */
	public boolean isSoundHeld(GameSoundEffect effect) {
		return holdSounds.contains(effect);
	}

	public int getConveyerCount() {
		if (isSlickGraphics())
			{ return slickGraphics.conveyerCount; }
		else
			{ return awtGraphics.conveyerCount; }
	}

	public int getHazardCount() {
		if (isSlickGraphics())
			{ return slickGraphics.getHazardCount(); }
		else
			{ return awtGraphics.getHazardCount(); }
	}
}
