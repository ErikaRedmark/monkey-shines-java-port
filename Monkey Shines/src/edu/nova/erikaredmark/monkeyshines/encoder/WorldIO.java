package edu.nova.erikaredmark.monkeyshines.encoder;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import edu.nova.erikaredmark.monkeyshines.editor.WorldEditor;
import edu.nova.erikaredmark.monkeyshines.encoder.exception.WorldRestoreException;
import edu.nova.erikaredmark.monkeyshines.encoder.exception.WorldSaveException;

/**
 * Provides utility methods for saving and restoring worlds. Used as a saving mechanism for the level editor; changes to a world
 * during gameplay should never be encoded and saved!
 * 
 * @author Erika Redmark
 * 
 */
public final class WorldIO {
	private WorldIO() { }
	
	private static final String WORLD_EXTENSION = ".world";
	
	/**
	 * Takes a world editor and translates the underlying world, and ALL data making it up into a persistable format.
	 * This only saves the logical level data (placement of tiles and their underlying types and data) and does not hold
	 * any resource data.
	 * <p/>
	 * This method is blocking until the file is saved. The file is always saved as the name of the world with a .world
	 * extension.
	 * 
	 * @param worldEditor
	 * 		a world editor that needs to be encoded
	 * 
	 * @param path
	 * 		a location to save the world to. The encoder will generate the save format at that location. This does not include
	 * 		the file name, and should point to a folder
	 * 
	 * @return
	 * 		an encoded object representing the world, whose contents can be written out to a file and then
	 * 		decoded later
	 * 
	 * @throws 
	 * 		IlegalArgumentException
	 * 			if the given path points to anything other than a valid folder
	 * 		WorldSaveException
	 * 			if an error occurs during saving the world. Clients must recover gracefully from this error by alerting the 
	 * 			user of all relevant details
	 * 
	 */
	public static void saveWorld( WorldEditor worldEditor, Path path ) throws WorldSaveException {
		if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS) == false)
			throw new IllegalArgumentException("Path " + path + " must point to a valid folder.");
		
		EncodedWorld encoded = EncodedWorld.from(worldEditor);
		final Path outputPath = path.resolve(worldEditor.getWorldName() + WorldIO.WORLD_EXTENSION);
		
		try (ObjectOutputStream os = new ObjectOutputStream(Files.newOutputStream(outputPath, StandardOpenOption.CREATE) ) ) {
			os.writeObject(encoded);
		} catch (IOException e) {
			throw new WorldSaveException(e);
		}
		
		
	}
	
	/**
	 * 
	 * Restores a world saved at a given location. This only loads world data, and not resources (graphics) for the world
	 * which are loaded separately.
	 * <p/>
	 * The returned object represents an immutable version of the starting state of the world for either the world editor
	 * or game to blow into a full world world with operational real-time.
	 * 
	 * @param world
	 * 		path to the .world file
	 * 
	 * @throws 
	 * 		IllegalArgumentException
	 * 			if the given path does not point to an actual file
	 * 		WorldRestoreException
	 * 			if the given file cannot be read for whatever reason (possible corruption) resulting in an inability to
	 * 			generate a world from it
	 * 
	 */
	public static EncodedWorld restoreWorld( Path world ) throws WorldRestoreException {
		if (Files.isRegularFile(world, LinkOption.NOFOLLOW_LINKS) == false)
			throw new IllegalArgumentException("Path " + world + " must point to a valid file.");
		
		try (ObjectInputStream is = new ObjectInputStream(Files.newInputStream(world, StandardOpenOption.READ))) {
			EncodedWorld recoveredWorld = (EncodedWorld) is.readObject();
			return recoveredWorld;
		} catch (IOException | ClassCastException | ClassNotFoundException e) {
			throw new WorldRestoreException(e);
		}
	}
	
}
