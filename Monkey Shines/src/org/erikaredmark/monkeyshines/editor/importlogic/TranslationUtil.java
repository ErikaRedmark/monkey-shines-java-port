package org.erikaredmark.monkeyshines.editor.importlogic;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.erikaredmark.monkeyshines.DeathAnimation;

/**
 * 
 * Static translation helper methods for read old monkey shines file formats
 * 
 * @author Erika Redmark
 *
 */
public final class TranslationUtil {

	private TranslationUtil() { }
	
	/**
	 * 
	 * Reads a 2-byte array interpreted as a signed, little-endian 2 byte value. This is the most common value found
	 * in old resource fork data.
	 * <p/>
	 * Passed array must be of size 2.
	 * 
	 * @param macShort
	 * 		raw bytes representing the short
	 * 
	 * @return
	 * 		integral value of the short
	 * 
	 * @throws IllegalArgumentException
	 * 		if the byte array is not exactly 2 bytes
	 * 
	 */
	public static int readMacShort(byte[] macShort) {
		if (macShort.length != 2)  throw new IllegalArgumentException("Can not interpret " + Arrays.toString(macShort) + " as a short: must be exactly 2 bytes, not " + macShort.length);
		
		ByteBuffer buffer = ByteBuffer.allocate(2);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.put(macShort);
		return buffer.getShort();
	}
	
	/**
	 * 
	 * Reads the byte array as an array of shorts. Every two bytes is effectively one short, hence the size of 
	 * the array /2 is the size of the return result.
	 * 
	 * @param shorts
	 * 		raw byte array of shorts
	 * 
	 * @return
	 * 		Java int array of the values interpretted
	 * 
	 */
	public static int[] readMacShortArray(byte[] shorts) {
		ByteBuffer buffer = ByteBuffer.allocate(shorts.length);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.put(shorts);
		
		int[] returnShorts = new int[shorts.length / 2];
		for (int i = 0; i < shorts.length / 2; ++i) {
			returnShorts[i] = buffer.getShort();
		}
		
		return returnShorts;
	}
	
	/**
	 * 
	 * Reads a 1 byte value as either {@code true} (being 1) or {@code false} (being 0). Other values will cause
	 * an exception
	 * 
	 * @param bool
	 * 		the byte that represents the boolean
	 * 
	 * @return
	 * 		{@code true} if 1, {@code false} if 0
	 * 
	 * @throws IllegalArgumentException
	 * 		if bool is neither 0 nor 1
	 * 
	 */
	public static boolean readMacBoolean(byte bool) {
		switch (bool) {
		case (byte)0:
			return false;
		case (byte)1:
			return true;
		default:
			throw new IllegalArgumentException("Boolean values must be either 0 or 1, not " + bool);
		}
	}
	
	/**
	 * 
	 * Reads the array of bytes as an array of booleans. Each byte is 1 boolean, so the returned
	 * array size will match the argument size.
	 * 
	 * @param bools
	 * 		byte array of bools
	 * 
	 * @return
	 * 		java intepreted booleans
	 * 
	 */
	public static boolean[] readMacBooleanArray(byte[] bools) {
		boolean[] returnBools = new boolean[bools.length];
		for (int i = 0; i < bools.length; ++i) {
			returnBools[i] = readMacBoolean(bools[i]);
		}
		return returnBools;
	}
	
	/**
	 * 
	 * Resolves the integral value of the old-style hazard specification to the port's interpretation
	 * of a death animation. In the port, death animations include sound hardcoded. In the original this
	 * was not the case.
	 * 
	 * @param type
	 * 		the integral value interpretted from the binary stream
	 * 
	 * @return
	 * 		the proper death animation
	 * 
	 * @throws IllegalArgumentException
	 * 		if the type is not recognised as a valid death type, indicating either a corrupted world file, or the
	 * 		translator is missing some information
	 * 
	 */
	public static DeathAnimation deathType(int type) {
		// TODO This is intended for hazards, which normally burn and electrify. We MAY need to add
		// standard and bee deaths to this.
		switch (type) {
		case 1: return DeathAnimation.BURN;
		case 2: return DeathAnimation.ELECTRIC;
		default: throw new IllegalArgumentException("Death type " + type + " not supported");
		}
	}
	
}
