package org.erikaredmark.monkeyshines.resource;

import java.util.Optional;

import javax.sound.sampled.Clip;

import org.erikaredmark.monkeyshines.GameSoundEffect;

import com.google.common.collect.ImmutableMap;

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
 * Instances of this class are publicly immutable.
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
	// SoundManager.
	// Sounds won't be loaded when the level editor is running (basically,
	// the pack reader for AwtGraphics won't load sounds and music since
	// it assumes a level editor context.
	private final ImmutableMap<GameSoundEffect, Optional<Clip>> sounds;

	public WorldResource(
		final AwtWorldGraphics awtGraphics,
		final SlickWorldGraphics slickGraphics,
	    final ImmutableMap<GameSoundEffect, Optional<Clip>> sounds) 
	{
		
		this.awtGraphics = awtGraphics;
		this.slickGraphics = slickGraphics;
		
		this.sounds = sounds;
	}
	
	public ImmutableMap<GameSoundEffect, Optional<Clip>> getSounds() { return sounds; }

	/**
	 * Creates an AWT resource for the level editor. AWT Resources only have editor specific graphics in
	 * awt format, and do not include sounds.
	 * @param awtGraphics
	 * @return
	 */
	public static WorldResource createAwtResource(
		AwtWorldGraphics awtGraphics) 
	{
		return new WorldResource(awtGraphics, null, JavaDefaultSoundManager.EMPTY_SOUNDS_MAP);
	}
	
	/**
	 * Creates a Slick resource for the game itself. note that splash screens and
	 * music are in {@code InitResource}, and that should be read from the resource pack first
	 * and displayed/played before attempting the expensive process of loading the rest of the
	 * world data.
	 */
	public static WorldResource createSlickResource(
		SlickWorldGraphics slickGraphics, 
		ImmutableMap<GameSoundEffect, Optional<Clip>> sounds) 
	{
		return new WorldResource(null, slickGraphics, sounds);
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

	// -----------------------------------------------------------------------
	// These methods provide numerical data based on dimensions or other properties of 
	// graphical resources that are common between Slick and AWT.
	
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

	public int getBackgroundCount() {
		if (isSlickGraphics())
			{ return slickGraphics.backgrounds.length; }
		else
			{ return awtGraphics.backgrounds.length; }
	}

	public int getPatternCount() {
		if (isSlickGraphics())
			{ return slickGraphics.patterns.length; }
		else
			{ return awtGraphics.patterns.length; }
	}

	public int getSpritesCount() {
		if (isSlickGraphics())
			{ return slickGraphics.sprites.length; }
		else
			{ return awtGraphics.sprites.length; }
	}

	public int getSpritesheetHeight(int id) {
		if (isSlickGraphics())
			{ return slickGraphics.sprites[id].getHeight(); }
		else
			{ return awtGraphics.sprites[id].getHeight(); }
	}
	
	public int getSpritesheetWidth(int id) {
		if (isSlickGraphics())
			{ return slickGraphics.sprites[id].getWidth(); }
		else
			{ return awtGraphics.sprites[id].getWidth(); }
	}
}
