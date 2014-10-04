package org.erikaredmark.util;

import com.google.common.base.Optional;

/**
 * Static utility methods for converting Strings to numbers. Goal is to remove need for client code to use exceptions
 * for flow control when using standard java libraries to parse strings. 
 * <p/>
 * This should only be used when it is possible, outside of the code, for the parse to fail (such as loading data that the
 * user can modify or parsing stuff the user enters). If the code makes it impossible for something other than
 * integer to be in a string, and such a thing would be an error, just use Integer.parseInt()
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
