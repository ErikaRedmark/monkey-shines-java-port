package org.erikaredmark.monkeyshines.play;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.erikaredmark.monkeyshines.encoder.EncodedWorld;
import org.erikaredmark.monkeyshines.graphics.exception.ResourcePackException;
import org.erikaredmark.monkeyshines.resource.InitResource;
import org.erikaredmark.monkeyshines.resource.PackReader;
import org.erikaredmark.monkeyshines.resource.WorldResource;

/**
 * Represents the parts needed to load the world, but has not loaded the world yet. Graphics
 * data won't be available until the gl context is started so resource creation, and therefore world
 * creation, must be deferred until then.
 * <p/>
 * deleteOnLoad should ONLY be true for internal worlds, as the resources are extracted to a temporary
 * location! Putting this as true for custom worlds will delete them. 
 * <p/>
 * Feed this object into {@code startMonkeyShines} to actually start up the game engine and
 * run the world.
 */
public class FrozenWorld {
	private static final String CLASS_NAME = "org.erikaredmark.monkeyshines.FrozenWorld";
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
	
	public FrozenWorld(EncodedWorld enc, Path rsrcPck, boolean delOnLoad) {
		this.encodedWorld = enc;
		this.rsrcPack = rsrcPck;
		this.deleteOnLoad = delOnLoad;
	}
	
	/**
	 * Actually performs the loading of the world. Do not call this until the GL Context
	 * is available or the image loading will fail.
	 * <p/>
	 * The returned resource may not be initialised if LoadingList is set to deferred loading. Make sure
	 * to call finishInitialisation on the slick graphics at the appropriate time.
	 * @throws IllegalStateException
	 * 		if temporary files were already cleaned up before the load was executed.
	 */
	public WorldResource load() throws ResourcePackException {
		if (deletedOnLoad)
			{ throw new IllegalStateException("Temporary files already deleted: pack cannot be loaded"); }
		return PackReader.fromPackSlick(rsrcPack);
	}
	
	/**
	 * Loads the background music and splash first, to display before the rest of the
	 * world is unfrozen.
	 * @throws IllegalStateException
	 * 		if temporary files were already cleaned up before the load was executed.
	 */
	public InitResource loadInit() throws ResourcePackException {
		if (deletedOnLoad)
			{ throw new IllegalStateException("Temporary files already deleted: pack cannot be loaded"); }
		return PackReader.initFromPackSlick(rsrcPack);
	}
	
	/** Removes, if needed, temporary files created from loading an internal world. */
	public void removeTemporaryFiles() {
		if (deleteOnLoad) {
			deletedOnLoad = true;
			try {
				Files.delete(rsrcPack);
				Files.delete(rsrcPack.getParent());
			} catch (IOException e) {
				LOGGER.log(Level.WARNING,
						   CLASS_NAME + ": Could not delete temporary files (should not affect gameplay) due to: " + e.getMessage(),
						   e);
			}
		}
	}
	
	public final EncodedWorld encodedWorld;
	public final Path rsrcPack;
	public final boolean deleteOnLoad;
	
	private boolean deletedOnLoad = false;
}