package org.erikaredmark.util;

import com.google.common.base.Optional;

/**
 * Static utility methods for converting Strings to numbers. Goal is to remove need for client code to use exceptions
 * for flow control when using standard java libraries to parse strings. 
 * 
 * @author Erika Redmark
 *
 */
public class StringToNumber {

	private StringToNumber() { /* Prevent Instantiation */ }
	
	public static Optional<Integer> string2Int(final String value) {
		try {
			Integer i = Integer.parseInt(value);
			return Optional.of(i);
		} catch (NumberFormatException e) {
			return Optional.absent();
		}
	}
	
}
