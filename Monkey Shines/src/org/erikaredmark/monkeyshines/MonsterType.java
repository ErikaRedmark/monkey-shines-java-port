package org.erikaredmark.monkeyshines;

import org.erikaredmark.monkeyshines.resource.SoundManager;

public enum MonsterType {
	NORMAL("Instant Kill") {
		@Override public void onBonzoCollision(Bonzo bonzo, World world, SoundManager sound) {
			bonzo.tryKill(DeathAnimation.NORMAL, sound);
		}
	},
	HEALTH_DRAIN("Health Drain") {
		@Override public void onBonzoCollision(Bonzo bonzo, World world, SoundManager sound) {
			bonzo.hurt(GameConstants.HEALTH_DRAIN_PER_TICK, DamageEffect.BEE, sound);
		}
	},
	EXIT_DOOR("Exit") {
		@Override public void onBonzoCollision(Bonzo bonzo, World world, SoundManager sound) {
			bonzo.hitExitDoor();
		}
	},
	BONUS_DOOR("Bonus") {
		@Override public void onBonzoCollision(Bonzo bonzo, World world, SoundManager sound) {
			world.bonusTransfer(bonzo);
		}
	},
	SCENERY("Harmless") {
		@Override public void onBonzoCollision(Bonzo bonzo, World world, SoundManager sound) {
			// This should not be called. Optimisations should not bother checking collisions
			// for scenery sprites.
			assert false : "No collision checks should be performed on scenery sprites";
		}
	};
	
	private final String name;
	
	private MonsterType(final String name) {
		this.name = name;
	}
	
	/**
	 * 
	 * Performs some action, either on the world or bonzo, when a sprite of this
	 * type is collided with.
	 * 
	 * @param bonzo
	 * 
	 * @param world
	 * 
	 */
	public abstract void onBonzoCollision(Bonzo bonzo, World world, SoundManager sound);
	
	@Override public String toString() { return name; }
}