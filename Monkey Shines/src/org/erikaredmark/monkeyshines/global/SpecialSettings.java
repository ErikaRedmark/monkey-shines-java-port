package org.erikaredmark.monkeyshines.global;

/**
 * 
 * Global settings for preferences that just don't fit in any other category.
 * 
 * @author Erika Redmark
 *
 */
public class SpecialSettings {
	private static boolean thunderbird = MonkeyShinesPreferences.defaultThunderbird();
	
	/**
	 * 
	 * Determines if the user asked playtesting mode, with infinite lives.
	 * 
	 * @return
	 * 		{@code true} if the game should be played in fullscreen, {@code false} if otherwise.
	 * 
	 */
	public static boolean isThunderbird() { return thunderbird; }
	
	/**
	 * 
	 * Sets playtest mode
	 * 
	 * @param full
	 * 
	 */
	public static void setThunderbird(boolean thunder) { thunderbird = thunder; } 
	
	/**
	 *
	 * Updates preferences file (if possible) with changes. This is called manually so that playing around with
	 * preferences doesn't cause excessive disk usage. Only call when the preference is okayed or saved by the user.
	 * 
	 */
	public static void persist() throws PreferencePersistException { MonkeyShinesPreferences.persistThunderbird(); }
}
