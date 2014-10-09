package org.erikaredmark.monkeyshines.logging;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.erikaredmark.util.BinaryLocation;

/**
 * 
 * Static class that initialises the java.util.logging system from the appropriate configuration file located in the
 * same folder as the binary is executed from, or initialises it using default parameters if no file can
 * be found.
 * <p/>
 * Provides easy access to logging facilities throughout the game code base.
 * 
 * @author Erika Redmark
 *
 */
public final class MonkeyShinesLog {
	
	// Effectively final: Initialisation done in method instead of static because otherwise
	// Java wouldn't automatically load the class.
	private static Logger LOG;
	

	/**
	 * 
	 * Initialises the logging system. May only be called once during startup. Calling this multiple
	 * times results an exception. To ensure oneness, only call once from main methods.
	 * 
	 */
	public static final void initialise() {
		if (LOG != null)  throw new IllegalStateException("Logging system already initialised");

		try (InputStream configStream = Files.newInputStream(BinaryLocation.BINARY_LOCATION.resolve("logging.properties") ) ){
		    LogManager.getLogManager().readConfiguration(configStream);
		} catch (IOException ex) {
			// Do it ourselves
			System.err.println("WARNING: Could not open configuration file. Dropping to defaults.");
			System.err.println("Reason: " + ex.getMessage() );
			
			LOG = Logger.getLogger("org.erikaredmark.monkeyshines");
			LOG.setLevel(Level.INFO);
			
			FileHandler logFileHandler;
			try {
				logFileHandler = new FileHandler("monkeyshines.log");
				logFileHandler.setFormatter(new SimpleFormatter() );
				LOG.addHandler(logFileHandler);
				System.err.println("File handler set up. Output is to console and 'monkeyshines.log'");
			} catch (SecurityException | IOException e) {
				System.err.println("File handler could not be set up. Output is to console only.");
			}

		    System.err.println("Log level for system is INFO");
		}
		
		LOG.info("Monkey Shines Java Port Log File Initialised");
	}
	
}
