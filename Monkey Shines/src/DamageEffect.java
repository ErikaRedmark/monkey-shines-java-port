import edu.nova.erikaredmark.monkeyshines.DeathAnimation;
import edu.nova.erikaredmark.monkeyshines.GameSoundEffect;


/**
 * 
 * Represents a 'damage' to bonzo. Unlike {@code DeathAnimation}, this is more representative of the
 * 'sound' that should play when being hurt, as well as the corresponding {@code DeathAnimation} if hurting
 * actually causes death
 * 
 * @author Erika Redmark
 *
 */
public enum DamageEffect {
	FALL(GameSoundEffect.BONZO_HURT, DeathAnimation.NORMAL),
	BEE(GameSoundEffect.BEE_STING, DeathAnimation.BEE);

	public final GameSoundEffect soundEffect;
	public final DeathAnimation deathAnimation;
	
	private DamageEffect(final GameSoundEffect soundEffect, final DeathAnimation animation) {
		this.soundEffect = soundEffect;
		this.deathAnimation = animation;
	}
	
	
}
