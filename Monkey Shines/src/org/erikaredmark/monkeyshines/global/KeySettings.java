package org.erikaredmark.monkeyshines.global;

import java.awt.event.KeyEvent;

import org.erikaredmark.monkeyshines.KeyBindings;

/**
 * 
 * Global class that handles the user's current keyboard preferences. On initialisation it defaults
 * to the basic (arrow keys for left and right movement, up arrow key for jump) but can be changed
 * in preferences
 * 
 * @author Erika Redmark
 *
 */
public final class KeySettings {

	private static KeyBindings currentBindings = new KeyBindings(KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP);
	
	public static void setBindings(final KeyBindings bindings) {
		currentBindings = bindings;
	}
	
	public static KeyBindings getBindings() {
		return currentBindings;
	}
	
}
