package org.erikaredmark.util;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 
 * Static utility class that allows client code to easily get the location that the game is running from, in other
 * words the folder/directory containing the 'binary' (the .jar file).
 * 
 * @author Erika Redmark
 *
 */
public final class BinaryLocation {
	public static final Path BINARY_FOLDER;
	
	static {
		Path tempBinaryFolder = null;
		try {
			tempBinaryFolder = Paths.get(BinaryLocation.class.getProtectionDomain().getCodeSource().getLocation().toURI() );
		} catch (Exception e) {
			// In the off chance that some system returns null for any of the above, or something goes wrong, it should be logged
			// but NOT destroy the ability to play the game.
			// TODO logging API
			System.err.println("Could not get binary location from URI class loader! " + e.getMessage() );
			tempBinaryFolder = Paths.get(".");
		}
		
		assert tempBinaryFolder != null : "Null binary folder";
		
		BINARY_FOLDER = tempBinaryFolder;
		System.out.println("Binary Location: " + BINARY_FOLDER);
	}
}
