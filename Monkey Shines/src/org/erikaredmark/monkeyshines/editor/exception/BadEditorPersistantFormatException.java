package org.erikaredmark.monkeyshines.editor.exception;

/**
 * 
 * Primarily a wrapper around the myriad of things that can go wrong reading the persistant xml state for the level editor
 * preferences.
 * 
 * @author Erika Redmark
 *
 */
public class BadEditorPersistantFormatException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public BadEditorPersistantFormatException(Exception inner) {
		super(inner);
	}

}
