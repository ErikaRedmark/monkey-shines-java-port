package org.erikaredmark.monkeyshines.editor.importlogic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.erikaredmark.monkeyshines.DeathAnimation;
import org.erikaredmark.monkeyshines.editor.importlogic.WorldTranslationException.TranslationFailure;

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
	public static int translateMacShort(byte[] macShort) {
		if (macShort.length != 2)  throw new IllegalArgumentException("Can not interpret " + Arrays.toString(macShort) + " as a short: must be exactly 2 bytes, not " + macShort.length);
		
		ByteBuffer buffer = ByteBuffer.allocate(2);
		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.put(macShort);
		buffer.rewind();
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
	public static int[] translateMacShortArray(byte[] shorts) {
		ByteBuffer buffer = ByteBuffer.allocate(shorts.length);
		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.put(shorts);
		buffer.rewind();
		
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
	public static boolean translateMacBoolean(byte bool) {
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
	public static boolean[] translateMacBooleanArray(byte[] bools) {
		boolean[] returnBools = new boolean[bools.length];
		for (int i = 0; i < bools.length; ++i) {
			returnBools[i] = translateMacBoolean(bools[i]);
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
	
	/**
	 * 
	 * Skips the given number of bytes in the input stream, automatically translating the inability to skip
	 * into an {@code WorldTranslationException} that is intended for the client that passed the stream in the
	 * first place.
	 * 
	 * @param is
	 * 
	 * @param skipBytes
	 * 
	 * @param ifFail
	 * 
	 * @param msg
	 * 		if the skip fails, the message to display
	 * 
	 * @throws IOException
	 * 		if an unknown error prevents skipping
	 * 
	 */
	public static void skip(InputStream is, long skipBytes, TranslationFailure ifFail, String msg) throws IOException, WorldTranslationException {
		long skipped = is.skip(skipBytes);
		if (skipped != skipBytes)  throw new WorldTranslationException(ifFail, msg);
	}
	
	/**
	 * 
	 * Reads the given number of bytes in the input stream, automatically translating the inability to read
	 * into an {@code WorldTranslationException} that is intended for the client that passed the stream in the
	 * first place.
	 * 
	 * @param is
	 * 
	 * @param read
	 * 
	 * @param ifFail
	 * 
	 * @param msg
	 * 		if the skip fails, the message to display
	 * 
	 * @throws IOException
	 * 		if an unknown error prevents skipping
	 * 
	 */
	public static void read(InputStream is, byte[] read, TranslationFailure ifFail, String msg) throws IOException, WorldTranslationException {
		int bytesRead = is.read(read);
		if (bytesRead != read.length)  throw new WorldTranslationException(ifFail, msg);
	}
	
	/**
	 * Reads a mac short from the stream. This is effectively reading 2 bytes from the stream and then converting
	 * the result using {@code translateMacShort}. The translation failure info is provided in case reading the
	 * stream fails for any reason. The info should include the type of data that was being read.
	 */
	public static int readMacShort(InputStream is, TranslationFailure ifFail, String msg) throws IOException, WorldTranslationException {
		byte[] raw = new byte[2];
		read(is, raw, ifFail, msg);
		return translateMacShort(raw);
	}
	
	/**
	 * Reads an array of shorts, similiar to {@code readMacShort} but for a consecutive amount of them. See javadocs
	 * for that method for explanation of other parameters
	 * 
	 * @param size
	 * 		size is in number of shorts, not in number of bytes (shorts take up two bytes)
	 * 
	 */
	public static int[] readMacShortArray(InputStream is, int size, TranslationFailure ifFail, String msg) throws IOException, WorldTranslationException {
		byte[] raw = new byte[size * 2];
		read(is, raw, ifFail, msg);
		return translateMacShortArray(raw);
	}
	
	/**
	 * Reads a mac boolean from the stream. This effectively reads 1 byte from the stream, and converts the result
	 * using {@code translateMacBoolean}. The translation failure info is provided in case reading the
	 * stream fails for any reason. The info should include the type of data that was being read.
	 */
	public static boolean readMacBoolean(InputStream is, TranslationFailure ifFail, String msg) throws IOException, WorldTranslationException {
		// Using array version to re-use existing code.
		byte[] raw = new byte[1];
		read(is, raw, ifFail, msg);
		return translateMacBoolean(raw[0]);
	}
	
	/**
	 * Reads an array of booleans, similiar to {@code readMacBoolean} but for a consecutive amount of them. See javadocs
	 * for that method for explanation of other parameters
	 */
	public static boolean[] readMacBooleanArray(InputStream is, int size, TranslationFailure ifFail, String msg) throws IOException, WorldTranslationException {
		byte[] raw = new byte[size];
		read(is, raw, ifFail, msg);
		return translateMacBooleanArray(raw);
	}
	
}
