package edu.nova.erikaredmark.monkeyshines.graphics.exception;

public class ResourcePackException extends Exception {
	private static final long serialVersionUID = 1961864077597399970L;

	private final Type type;
	private final String extra;
	
	/**
	 * 
	 * Used when another exception causes the resource pack exception
	 * 
	 * @param cause
	 */
	public ResourcePackException(final Throwable cause) {
		super(cause.getMessage(), cause);
		this.type = Type.EXCEPTION;
		this.extra = "";
	}
	
	/**
	 * 
	 * Used when an invariant is broken when analysing the resource pack
	 *
	 */
	public ResourcePackException(final Type type, final String extra) {
		this.type = type;
		this.extra = extra;
	}
	
	
	
	public enum Type {
		/** Indicates that some resource has been defined more than once in the pack.
		 */
		MULTIPLE_DEFINITION("Multiple resource defitions: "),
		/** Indicates that some resource has no definition in the pack
		 */
		NO_DEFINITION("No resource definition: "),
		/** Indicates that a resource with a variable number of definitions does not have unbroken definitions for all 
		 *  values from 0 to some maximum value.
		 */
		NON_CONTIGUOUS("Non-contiguous resource numbering: "),
		/** For things like backgrounds and sprites, if an index number is expected but not encountered, this will be fired.
		 */
		NO_INDEX_NUMBER("Index number expected for resource, but none encountered: "),
		
		/** Indicates an exception occurred.
		 */
		EXCEPTION("Exception occurred: ");
		
		private final String msg;
		
		private Type(final String msg) { this.msg = msg; }
		public String getMessage() { return msg; }
	}
}
