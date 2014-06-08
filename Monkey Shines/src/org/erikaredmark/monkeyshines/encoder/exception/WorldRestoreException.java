package org.erikaredmark.monkeyshines.encoder.exception;

public class WorldRestoreException extends Exception {
	private static final long serialVersionUID = 4287625350828925807L;
	
	/**
	 * 
	 * Automatically constructs the exception by analysing the root exception. Detail message is automatically generated.
	 * 
	 * @param cause
	 * 
	 */
	public WorldRestoreException(Throwable cause) {
		super(cause.getMessage(), cause);
	}
	
}
