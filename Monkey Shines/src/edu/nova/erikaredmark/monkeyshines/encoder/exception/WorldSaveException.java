package edu.nova.erikaredmark.monkeyshines.encoder.exception;

public class WorldSaveException extends Exception {
	private static final long serialVersionUID = 3365703337199619717L;
	
//	private final Reason reason;
	
	
	/**
	 * 
	 * Automatically constructs the exception by analysing the root exception. Detail message is automatically generated.
	 * 
	 * @param cause
	 * 
	 */
	public WorldSaveException(final Throwable cause) {
		super(cause.getMessage(), cause);
	}
	
//	public enum Reason {
//		SAVE_LOCAT
//	}

}
