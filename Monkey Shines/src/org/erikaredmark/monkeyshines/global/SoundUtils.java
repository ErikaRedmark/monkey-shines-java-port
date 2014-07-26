package org.erikaredmark.monkeyshines.global;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * 
 * Static utility class for easily loading sound files and converting them into sampled clips or other
 * forms for playback during game.
 * 
 * @author Erika Redmark
 *
 */
public final class SoundUtils {
	/**
	 * 
	 * Treats the contents of the stream as ogg encoded and loads the stream into memory, returning a
	 * {@code Clip} representing the sound. All sounds are loaded completely into memory.
	 * 
	 * @param oggStream
	 * 		stream containing an ogg-formatted soundbite. The stream will not be closed by this
	 * 		method
	 * 
	 * @param name
	 * 		name to refer to stream, in case of extra information required to print
	 * 		out to console (such as the issue with too small of streams not loading properly)
	 * 
	 * @return
	 * 		a clip of the sound. The entire sound will be stored in memory
	 * 
	 * @throws UnsupportedAudioException
	 * 		if the stream is not in ogg format
	 * 
	 * @throws LineUnavailableException
	 * 		if a sound device is not available for loading the sound
	 * 
	 * @throws IOException
	 * 		if the stream cannot be read
	 * 
	 */
	public static Clip clipFromOggStream(InputStream oggStream, String name) 
		throws UnsupportedAudioFileException, LineUnavailableException, IOException {
		
		// Load the audio stream from the entry
		// Buffered stream to allow mark/reset
		try (AudioInputStream in = AudioSystem.getAudioInputStream(oggStream) ) {

			AudioFormat baseFormat = in.getFormat();
			
			// Convert to basic PCM
			// Decoded input stream will be closed on disposing of the WorldResource itself.
			// Required for clip.
			AudioFormat decodedFormat =
			    new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
			                    baseFormat.getSampleRate(),
			                    16,
			                    baseFormat.getChannels(),
			                    baseFormat.getChannels() * 2,
			                    baseFormat.getSampleRate(),
			                    false);
			
			AudioInputStream decodedInputStream = AudioSystem.getAudioInputStream(decodedFormat, in);
			// Store in Clip and return
			Clip clip = AudioSystem.getClip();
			clip.open(decodedInputStream);
			if (clip.getFrameLength() == 0) {
				System.err.println("Clip " + name + " has no loaded frames. There is an unknown issue decoding .ogg files of sizes less than or equal to around 6K. Please add inaudible noise to sound file to increase size");
			}

			return clip;
		}
	}
	
	/**
	 * 
	 * The minimum allowed sound before cutting into nothing. Note that this does not mean that
	 * gains less than this mean nothing. It is up to clients to detect 0 sound percent and handle
	 * muting or not playing the clips accordingly.
	 * 
	 */
	private static final double MIN_SOUND = -16.2;
	
	/**
	 * 
	 * Resolves a percentage from 0-100 to a decibel level offset for setting clip volume. By default, all clips have
	 * a master gain of 0.0 decibels, which is already pretty loud. Based on constants defined in {@code GameConstants},
	 * the percentage is transformed into either a positive or more likely negative offset used to change the 'gain' on
	 * associated clips and either make them louder or softer.
	 * 
	 * @param value
	 * 		the percentage, from 0-100, of volume. 0 is always no volume and optionally can be handled separately by not
	 * 		playing the clip at all
	 * 
	 * @return
	 * 		decibel offset level. Returns {@code Float.MIN_VALUE} for volumes of 0%
	 * 
	 */
	public static float resolveDecibelOffsetFromPercentage(int value) {
		// special case (since 0 will be -Infinity)
		if (value == 0)  return (float)MIN_SOUND;
		
		// Default decibel gain is +0, which is already very loud. as a result, all calculations
		// currently return negative values. Since decibel values are not linear by nature.
		double gain = (Math.log10(value) ) * 6.0;
		// Larger values mean MORE sound, not less, so converting to negative directly won't work. We pick a 'MIN'
		// sound on offset from that.
		gain = MIN_SOUND + gain;
		System.out.println("Calculation of " + value + " got a gain of " + gain);
		return (float) gain;
	}
}
