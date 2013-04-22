package edu.nova.erikaredmark.monkeyshines.encoder;

import edu.nova.erikaredmark.monkeyshines.editor.WorldEditor;

/**
 * provides utility methods for encoding worlds. Used as a saving mechanism for the level editor; changes to a world
 * during gameplay should never be encoded and saved!
 * <p/>
 * This class is a singleton to maintain consistency with other singleton utility objects that require the use of
 * the {@code getClass() } method to access external resources.
 * 
 * @author Erika Redmark
 */
public enum Encoder {
	INSTANCE;
	
	/**
	 * Takes a world editor and translates the underlying world, and ALL data making it up into a persistable format.
	 * 
	 * @param worldEditor
	 * 		a world editor that needs to be encoded
	 * 
	 * @return
	 * 		an encoded object representing the world, whose contents can be written out to a file and then
	 * 		decoded later
	 */
	public EncodedWorld encodeWorld( WorldEditor worldEditor ) {
		// TODO method stub
		return null;
	}
	
}
