package org.erikaredmark.monkeyshines.editor.importlogic;

/**
 * 
 * Thrown when importing a world from the original Monkey Shines file format fails.
 * 
 * @author Erika Redmark
 *
 */
public class WorldTranslationException extends Exception {
	private static final long serialVersionUID = 1L;
	
	private TranslationFailure failureType;
	
	WorldTranslationException(TranslationFailure cause) {
		super(cause.getMessage() );
		
		this.failureType = cause;
	}
	
	WorldTranslationException(TranslationFailure cause, String msg) {
		super(cause.getMessage() + " : " + msg);
		
		this.failureType = cause;
	}
	
	public TranslationFailure getTranslationFailure() {
		return failureType;
	}
	
	
	public enum TranslationFailure {
		WRONG_WORLD_SIZE(".wrld binaries shoudd always be exactly 122 bytes in length"),
		WRONG_LEVEL_SIZE(".plvl binaries shoudl always be exactly 1980 bytes in length"),
		/** Indicates that the issue is specific the the type of translator, and not to the underlying binary form of the resources. */
		TRANSLATOR_SPECIFIC("");
		
		private final String msg;
		
		private TranslationFailure(final String msg) {
			this.msg = msg;
		}
		
		public String getMessage() { return msg; }
	}
	
}
