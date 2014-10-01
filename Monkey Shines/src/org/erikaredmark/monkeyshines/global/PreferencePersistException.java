package org.erikaredmark.monkeyshines.global;

/**
 * 
 * Indicates that a preference save failed. This is not a critical bug and does not affect
 * gameplay or preferences for this game session. This means that when the game is quit and
 * restarted, user settings and high scores will not persist from the last game.
 * 
 * @author Erika Redmark
 *
 */
public final class PreferencePersistException extends Exception {
	private static final long serialVersionUID = 1L;
	
	PreferencePersistException(String msg, Throwable t) {
		super(msg, t);
	}
}
