package edu.nova.erikaredmark.monkeyshines.encoder;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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
	 * 
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
	 * @throws IOException 
	 * 
	 * @throws IlegalArgumentException
	 * 		if the given path points to anything other than a valid folder
	 * 
	 * @throws WorldSaveException
	 * 		if an error occurs during saving the world due to high level issues (such as world corruption)
	 * 
	 * @throws IOException
	 * 		if an error occurs saving the world due to low level I/O issues
	 * 
	 */
	public static void saveWorld( WorldEditor worldEditor, Path path ) throws WorldSaveException, IOException {
		if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS) == false)
			throw new IllegalArgumentException("Path " + path + " must point to a valid folder");
		
		EncodedWorld encoded = EncodedWorld.fromMemory(worldEditor.getWorld() );
		final Path outputPath = path.resolve(worldEditor.getWorldName() + WorldIO.WORLD_EXTENSION);
		
		try (OutputStream out = Files.newOutputStream(outputPath) ) {
			encoded.save(out);
		} 
		
	}
	
	/**
	 * 
	 * Takes the world editor's underlying world ONLY and saves that to the file pointed to by path. This method differs
	 * from the basic {@link #saveWorld(WorldEditor, Path)} in that it only updates the .world file and leaves the 
	 * resource pack as is. This is the default operation for when the user is saving changes to the world in the basic
	 * editor.
	 * 
	 * @param worldEditor
	 * 		a world editor that needs to be encoded
	 * 
	 * @param path
	 * 		a location to save the world to. Unlike the other save method, this must point to an existing file in which
	 * 		to overwrite (the original world)
	 * 
	 * @throws IlegalArgumentException
	 * 		if the given path points to anything other than a valid file
	 * 
	 * @throws WorldSaveException
	 * 		if an error occurs during saving the world due to high level issues (such as world corruption)
	 * 
	 * @throws IOException
	 * 		if an error occurs saving the world due to low level I/O issues
	 * 
	 */
	public static void saveOnlyWorld( WorldEditor worldEditor, Path path ) throws WorldSaveException, IOException {
		if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) == false)
			throw new IllegalArgumentException("Path " + path + " must point to the .world file to overwrite");
		
		EncodedWorld encoded = EncodedWorld.fromMemory(worldEditor.getWorld() );
		try (OutputStream out = Files.newOutputStream(path) ) {
			encoded.save(out);
		} 
	}
	
	/**
	 * 
	 * Creates a new world folder at the given location with the given name. The default resources pack is used by copying
	 * it to that location and renaming it to the given world name. Additionally, a clean-slate 'worldName'.world file is
	 * created with empty content ready for the editor.
	 * <p/>
	 * This method is guaranteed to have created the folder if it does not throw an exception.
	 * 
	 * @param newWorldFolder
	 * 		the location to create the new world folder. This location's parent must exist, but the actually folder should
	 * 		not as this method will create a new one
	 * 
	 * @param worldName
	 * 		the name of the world, which will be the name of the .world file as well as the name of the resource pack
	 * @throws IOException 
	 * 
	 * @throws IllegalArgumentException	
	 * 		if the path to the new world folder already exists, or if the parent of the path (all folders leading up)
	 * 		do not exist
	 * 
	 * @throws IOException
	 * 		if something occurs during folder and/or file creation that prevents the full creation of this world
	 * 
	 */
	public static void newWorldWithDefault(Path newWorldFolder, String worldName) throws WorldSaveException, IOException {
	    // Don't create the world data until we first verify the resource pack is valid
		InputStream resourceSource = WorldIO.class.getResourceAsStream("/resources/standard/default.zip");
	    if (resourceSource == null) throw new RuntimeException("Bad .jar file, default resource pack unavailable");
	    
		newWorldData(newWorldFolder, worldName);
		
		// Copy resources and create <worldName>.zip file

	    Path resourceDestination = newWorldFolder.resolve(worldName + ".zip");
	    Files.copy(resourceSource, resourceDestination);
	    
	    // No exceptions = done!
	}
	
	/**
	 * 
	 * Creates a new world folder at the given location with the given name, copying the contents of the resource pack
	 * pointed to to the new folder with the world.
	 * <p/>
	 * This method is guaranteed to have created the folder if it does not throw an exception.
	 * 
	 * @param newWorldFolder
	 * 		the location to create the new world folder. This location's parent must exist, but the actually folder should
	 * 		not as this method will create a new one
	 * 
	 * @param worldName
	 * 		the name of the world, which will be the name of the .world file as well as the name of the resource pack
	 * 
	 * @throws IOException 
	 * 
	 * @throws IllegalArgumentException	
	 * 		if the path to the new world folder already exists, or if the parent of the path (all folders leading up)
	 * 		do not exist, or if the given resource pack does not exist
	 * 
	 * @throws IOException
	 * 		if something occurs during folder and/or file creation that prevents the full creation of this world
	 * 
	 */
	public static void newWorldWithResources(Path newWorldFolder, String worldName, Path rsrcPack) throws WorldSaveException, IOException {
		checkArgument(Files.exists(rsrcPack, LinkOption.NOFOLLOW_LINKS) );
		newWorldData(newWorldFolder, worldName);
		
		// Copy resources from target into <worldName>.zip file
		InputStream resourceSource = new FileInputStream(rsrcPack.toFile() );
	    Path resourceDestination = newWorldFolder.resolve(worldName + ".zip");
	    Files.copy(resourceSource, resourceDestination, StandardCopyOption.COPY_ATTRIBUTES);
	    
	    // No exceptions = done!
	}
	
	/** Common code to both newWorldYYY functions. Makes the .world file, does not handle resources.					*/
	private static void newWorldData(Path newWorldFolder, String worldName) throws WorldSaveException, IOException {
		checkArgument(Files.exists(newWorldFolder, LinkOption.NOFOLLOW_LINKS) == false );    // child must not exist
		checkArgument(Files.exists(newWorldFolder.getParent(), LinkOption.NOFOLLOW_LINKS) ); // ... but parent must
		
		Files.createDirectory(newWorldFolder);
	    
		// Create .world file
		EncodedWorld newWorld = EncodedWorld.fresh(worldName);
		Path worldLocation = newWorldFolder.resolve(worldName + WorldIO.WORLD_EXTENSION);
		try (OutputStream out = Files.newOutputStream(worldLocation) ) {
			newWorld.save(out);
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
	 * @throws IllegalArgumentException
	 * 		if the given path does not point to an actual file
	 * 
	 * @throws WorldRestoreException
	 * 		if the given file cannot be read due to world corruption resulting in an inability to
	 * 		generate a world from it
	 * 
	 * @throws IOException
	 * 		if a low level I/O error prevents read
	 * 
	 */
	public static EncodedWorld restoreWorld(Path world) throws WorldRestoreException, IOException {
		if (Files.isRegularFile(world, LinkOption.NOFOLLOW_LINKS) == false)
			throw new IllegalArgumentException("Path " + world + " must point to a valid file.");
		
		try (InputStream in = Files.newInputStream(world) ) {
			return EncodedWorld.fromStream(in);
		}
	}
	
}
