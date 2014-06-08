package org.erikaredmark.monkeyshines.graphics.exception;

public class ResourcePackException extends Exception {
	private static final long serialVersionUID = 1961864077597399970L;

	private final Type type;
	
	/**
	 * 
	 * Used when another exception causes the resource pack exception
	 * 
	 * @param cause
	 * 
	 */
	public ResourcePackException(final Throwable cause) {
		this("", cause);
	}
	
	/**
	 * 
	 * Used when another exception causes the resource pack exception
	 * 
	 * @param extraMessage
	 * 		any extra information in addition to the exception that will be printed
	 * 
	 * @param cause
	 * 
	 */
	public ResourcePackException(final String extraMessage, final Throwable cause) {
		super(extraMessage + ": " + cause.getMessage(), cause);
		this.type = Type.EXCEPTION;
	}
	
	/**
	 * 
	 * Used when an invariant is broken when analysing the resource pack
	 *
	 */
	public ResourcePackException(final Type type, final String extra) {
		super(type.getMessage() + extra);
		this.type = type;
	}
	
	public Type getType() {
		return type;
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
