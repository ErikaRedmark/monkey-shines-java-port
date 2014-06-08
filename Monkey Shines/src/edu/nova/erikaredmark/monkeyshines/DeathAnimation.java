package edu.nova.erikaredmark.monkeyshines;

/**
 * 
 * Indicates a state of death for bonzo. The death animation objects indicate how bonzo will/is dying, and which sprite
 * bounds to use for display.
 * 
 * @author Erika Redmark
 *
 */
public enum DeathAnimation {
	NORMAL {
		private final ImmutablePoint2D start = ImmutablePoint2D.of(0, 120);
		private final ImmutablePoint2D size = ImmutablePoint2D.of(80, 40);
		
		@Override public ImmutablePoint2D deathStart() { return start; }
		@Override public ImmutablePoint2D deathSize() { return size; }
		@Override public int framesPerRow() { return 8; }
		@Override public GameSoundEffect soundEffect() { return GameSoundEffect.STANDARD_DEATH; }
	},
	BEE {
		private final ImmutablePoint2D start = ImmutablePoint2D.of(0, 349);
		private final ImmutablePoint2D size = ImmutablePoint2D.of(40, 52);
		private final ImmutablePoint2D offset = ImmutablePoint2D.of(0, -13);
		
		@Override public ImmutablePoint2D deathStart() { return start; }
		@Override public ImmutablePoint2D deathSize() { return size; }
		@Override public int framesPerRow() { return 16; }
		@Override public ImmutablePoint2D offset() { return offset; }
		@Override public GameSoundEffect soundEffect() { return GameSoundEffect.STANDARD_DEATH; }
	},
	BURN {
		private final ImmutablePoint2D start = ImmutablePoint2D.of(0, 200);
		private final ImmutablePoint2D size = ImmutablePoint2D.of(40, 40);
		
		@Override public ImmutablePoint2D deathStart() { return start; }
		@Override public ImmutablePoint2D deathSize() { return size; }
		@Override public int framesPerRow() { return 16; }
		@Override public GameSoundEffect soundEffect() { return GameSoundEffect.BOMB_DEATH; }
	},
	ELECTRIC {
		private final ImmutablePoint2D start = ImmutablePoint2D.of(0, 240);
		private final ImmutablePoint2D size = ImmutablePoint2D.of(80, 40);
		
		@Override public ImmutablePoint2D deathStart() { return start; }
		@Override public ImmutablePoint2D deathSize() { return size; }
		@Override public int framesPerRow() { return 8; }
		@Override public GameSoundEffect soundEffect() { return GameSoundEffect.ELECTRIC_DEATH; }
	};
	
	
	/**
	 * 
	 * location in the sprite sheet of where the start of this specific animation is.
	 * 
	 * @return
	 * 		location of starting animation
	 * 
	 */
	public abstract ImmutablePoint2D deathStart();
	
	/**
	 * 
	 * The size of a <strong> single </strong> frame of animation for the death. Some deaths may require a bigger size to
	 * draw Bonzo properly.
	 * <p/>
	 * x value is length, y value is width.
	 * 
	 * @return
	 * 		size of a SINGLE frame of death animation
	 * 
	 */
	public abstract ImmutablePoint2D deathSize();
	
	/**
	 * 
	 * the number of frames of animation in a row.
	 * 
	 * @return
	 * 		frames per row
	 * 
	 */
	public abstract int framesPerRow();
	
	/**
	 * 
	 * the offset that this drawing should be done from the standard locatino bonzo was before
	 * the death animation started. This is to support death animations that are bigger on the
	 * top/left sides. Drawing bonzo starts at bonzo's upper left point; drawing the death
	 * animation is typically the same. For death animations, such as bee, that are bigger
	 * in the top or left, drawing has to be offset so Bonzo 'lines up' with himself when
	 * switching between animations.
	 * 
	 */
	public ImmutablePoint2D offset() { 
		// Should return an immutable singleton
		return ImmutablePoint2D.of(0, 0); 
	}
	
	/**
	 * 
	 * The sound effect associated with this death animation
	 * 
	 * @return
	 * 		sound effect to play when this animation begins
	 * 
	 */
	public abstract GameSoundEffect soundEffect();
}
