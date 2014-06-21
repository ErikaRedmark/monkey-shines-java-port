package org.erikaredmark.monkeyshines;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * An enumeration of all sound effects in the game. Is is the responsibility of a paricular
 * {@code WorldResource} to map a given sound effect to a specific clip. All sound effects are
 * stored in-memory. This does NOT handle streaming sounds such as music.
 * <p/>
 * These sound effects are specific to a particular world. This does not enumerate global 
 * sounds, such as any menu/button sounds, used outside of actual gameplay.
 * 
 * @author Erika Redmark
 *
 */
public enum GameSoundEffect {
	APPLAUSE {
		@Override public String filename() { return "applause.ogg"; }
	},
	BEE_STING {
		@Override public String filename() { return "bee.ogg"; }
	},
	BONZO_HURT {
		@Override public String filename() { return "bonzoHurt.ogg"; }
	},
	EXTRA_LIFE {
		@Override public String filename() { return "extraLife.ogg"; }
	},
	LAST_RED_KEY {
		@Override public String filename() { return "lastRedKey.ogg"; }
	},
	LAST_BLUE_KEY {
		@Override public String filename() { return "lastBlueKey.ogg"; }
	},
	LEVEL_FINISHED {
		@Override public String filename() { return "levelFinishedScreen.ogg"; }
	},
	POWERUP_FADE {
		@Override public String filename() { return "powerupFade.ogg"; }
	},
	POWERUP_SHIELD {
		@Override public String filename() { return "powerupShield.ogg"; }
	},
	POWERUP_WING {
		@Override public String filename() { return "powerupWing.ogg"; }
	},
	POWERUP_EXTRA_LIFE {
		@Override public String filename() { return "powerupExtralife.ogg"; }
	},
	STANDARD_DEATH {
		@Override public String filename() { return "bonzoStandardDeath.ogg"; }
	},
	ELECTRIC_DEATH {
		@Override public String filename() { return "bonzoElectricDeath.ogg"; }
	},
	BOMB_DEATH {
		@Override public String filename() { return "bonzoBombDeath.ogg"; }
	},
	EXPLOSION {
		@Override public String filename() { return "explosion.ogg"; }
	},
	ENERGY_RESTORE {
		@Override public String filename() { return "energyRestore.ogg"; }
	},
	TICK {
		@Override public String filename() { return "tick.ogg"; }
	},
	YES {
		@Override public String filename() { return "yes.ogg"; }
	},
	YUM_COLLECT {
		@Override public String filename() { return "yumCollect.ogg"; }
	};
	
	private static final Map<String, GameSoundEffect> filenameToEnum;
	
	static {
		filenameToEnum = new HashMap<>();
		for (GameSoundEffect e : GameSoundEffect.values() ) {
			filenameToEnum.put(e.filename(), e);
		}
	}
	
	/**
	 * 
	 * Returns the name of the file (including extension) of where the sound is expected to be
	 * located in the resource pack .zip file. This is only the file name; not a path.
	 * 
	 * @return
	 * 		expected filename for sound in resource pack, including extension
	 * 
	 */
	public abstract String filename();
	
	/**
	 * 
	 * Returns the enumerated {@code GameSoundEffect} that corresponds to the given filename, typically
	 * taken from a resource pack. This may return {@code null} if the filename maps to no known
	 * sound effect.
	 * 
	 * @param filename
	 * 		filename of sound effect
	 * 
	 * @return
	 * 		instance of this enum mapped to the given filename, or {@code null} if the filename corresponds
	 * 		to no known sound effect
	 * 
	 */
	public static GameSoundEffect filenameToEnum(String filename) {
		return filenameToEnum.get(filename);
	}
}
