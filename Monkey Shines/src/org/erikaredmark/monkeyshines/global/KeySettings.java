package org.erikaredmark.monkeyshines.global;

import org.erikaredmark.monkeyshines.KeyBindingsAwt;
import org.erikaredmark.monkeyshines.global.PreferencePersistException;

/**
 * 
 * Global class that handles the user's current keyboard preferences. On initialisation it defaults
 * to the basic (arrow keys for left and right movement, up arrow key for jump) or the preferences
 * loaded but can be changed in-game preferences
 * 
 * @author Erika Redmark
 *
 */
public final class KeySettings {
	
	private static KeyBindingsAwt currentBindings = MonkeyShinesPreferences.defaultKeyBindings();
	
	public static void setBindings(final KeyBindingsAwt bindings) {
		currentBindings = bindings;
	}
	
	public static KeyBindingsAwt getBindings() {
		return currentBindings;
	}
	
	/**
	 *
	 * Updates preferences file (if possible) with changes. This is called manually so that playing around with
	 * preferences doesn't cause excessive disk usage. Only call when the preference is okayed or saved by the user.
	 * 
	 */
	public static void persist() throws PreferencePersistException { MonkeyShinesPreferences.persistKeyBindings(); }
	
}
