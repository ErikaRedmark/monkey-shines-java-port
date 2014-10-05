package org.erikaredmark.monkeyshines;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.erikaredmark.monkeyshines.global.MonkeyShinesPreferences;
import org.erikaredmark.util.StringToNumber;

import com.google.common.base.Optional;

/**
 * 
 * Represents an underlying concept of the high scores for the session. This object keeps high score records
 * ordered from highest to lowest and stores the names of the achievers. A single instance is created when loading 
 * a highscores file and typically exist until the game is closed (making an instance of this object
 * global in a sense). The high scores object can be added to and persisted to a file (typically the
 * preferences file given the simplicity of the object.
 * <p/>
 * Only ten scores are stored. Adding a new score may either not work if the score is too low, or bump another
 * score out.
 * <p/>
 * Since this object 
 * 
 * @author Erika Redmark
 *
 */
public final class HighScores {
	private static final String CLASS_NAME = "org.erikaredmark.monkeyshines.HighScores";
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
	
	// primitive array since size must be enforced
	private HighScore[] scores = new HighScore[MAX_SCORES];
	private int scoreSize = 0;
	
	private static final int MAX_SCORES = 10;
	
	private static final HighScores EMPTY = new HighScores();
	
	/**
	 * 
	 * Checks if the score is higher than the lowest score. If not, the score cannot be added.
	 * 
	 * @param score
	 * 
	 * @return
	 * 		{@code true} if the score can be entered, {@code false} if otherwise.
	 * 
	 */
	public boolean isScoreHigh(int score) {
		if (scoreSize < 10)  return true;
		// We can assume ordered array, so look at last element
		return scores[9].score < score;
	}
	
	/**
	 * 
	 * Adds the given score to the high scores list for the given person. This does not automatically persist
	 * the scores to a preferences file.
	 * <p/>
	 * If the score is too low and can't be added an exception is thrown. Only call after confirming with
	 * {@code isScoreHigh(int)} first.
	 * 
	 * @param name
	 * 		name of the person who got the score
	 * 
	 * @param score
	 * 
	 * @throws IllegalArgumentException
	 * 		if score is too low to be added. Call {@code isScoreHigh(int) } first
	 * 
	 */
	public void addScore(String name, int score) {
		if (!(isScoreHigh(score) ) ) {
			throw new IllegalArgumentException("Score " + score + " is too low to be entered into the high scores. Check with isScoreHigh first");
		}
		
		// Condition 1: Bump out the lowest score since there isn't space left. We will resort array later.
		if (scoreSize == 10) {
			scores[9] = new HighScore(name, score);
		// Condition 2: Enough room
		} else {
			scores[scoreSize] = new HighScore(name, score);
			++scoreSize;
		}
		
		Arrays.sort(scores, 0, scoreSize);
	}
	
	/**
	 * 
	 * Returns the name/score pairs in this object. The list is only as long as the number of actual
	 * entries and therefore may be less than 10.
	 * <p/>
	 * Changes to the returned list will not affect this object
	 * 
	 * @return
	 * 		copy of the high scores list in this object
	 * 
	 */
	public List<HighScore> getHighScores() {
		List<HighScore> highs = new ArrayList<>(scoreSize);
		for (int i = 0; i < scoreSize; ++i) {
			highs.add(scores[i]);
		}
		return highs;
	}
	
	/**
	 * 
	 * Persists the high scores data into the Java preferences form. Each key is the person's name prefixed with
	 * 'high_', for example 'high_Cordelia Chase' and the value is the score as an integer.
	 * <p/>
	 * As long as the preferences file does not use a naming scheme for keys that will conflict with this persistence form
	 * it is save to combine this with a more global preferences file.
	 * <p/>
	 * If this method fails, it will log and return false
	 * 
	 * @param preferences
	 * 		path to preferences file. If one does not exist it will be created
	 * 
	 * @return
	 * 		{@code true} if the high scores could be saved, {@code false} if otherwise
	 * 
	 */
	public boolean persist(Path preferences) {
		Properties props = new Properties();
		if (!(Files.exists(preferences) ) ) {
			try {
				Files.createFile(preferences);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE,
						   "Could not create high scores: " + e.getMessage(),
						   e);
				return false;
			}
		}
		
		try (InputStream is = Files.newInputStream(preferences) ) {
			props.load(is);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE,
					   "Could not load high scores for saving: " + e.getMessage(),
					   e);
			return false;
		}
		
		// We have loaded the properties file for the given preferences file, and it is valid.
		// Generate the keys for these high scores. If they exist, update them, else create new ones.
		for (int i = 0; i < scoreSize; ++i) {
			HighScore s = scores[i];
			assert s != null;
			String key = "high_" + s.name;
			String value = String.valueOf(s.score);
			props.put(key, value);
		}
		
		try (OutputStream os = Files.newOutputStream(preferences) ) {
			props.store(os, MonkeyShinesPreferences.getPreferencesComments() );
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE,
					   "Could not save high scores: " + e.getMessage(),
					   e);
			
			return false;
		}
		
		return true;
		
	}
	
	/**
	 * 
	 * Constructs an instance of this object from a preferences file. The object will be looking for all preference
	 * keys that are prefixed with 'high_'. Keys are assumed to be names, and more critically values are assumed to
	 * be actual integers.
	 * <p/>
	 * If the file does not exist, or does not contain any keys, this method does NOT throw an exception. It simply
	 * returns an empty high scores object.
	 * 
	 * @return
	 * 		instance of this object
	 * 
	 */
	public static HighScores fromPreferences(Path preferences) {
		Properties props = new Properties();
		try (InputStream is = Files.newInputStream(preferences) ) {
			props.load(is);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE,
					   "Could not load high scores, returning empty object: " + e.getMessage(),
					   e);
			return EMPTY;
		}
		
		HighScores returnScores = new HighScores();
		for (Object o : props.keySet() ) {
			String possibleName = (String) o;
			// Length check ensures it is not JUST 'high_'
			if (possibleName.contains("high_") && possibleName.length() > 5) {
				String name = possibleName.substring(5);
				Optional<Integer> score = StringToNumber.string2Int(props.getProperty(possibleName) );
				if (score.isPresent() ) {
					returnScores.addScore(name, score.get() );
				}
			}
		}
		
		return returnScores;
	}
	
	public static final class HighScore implements Comparable<HighScore> {
		private final String name;
		private final int score;
		
		private HighScore(final String name, final int score) { this.name = name; this.score = score; }
		
		public String getName() { return name; }
		public int getScore() { return score; }

		/** Comparison is on score only. */
		@Override public int compareTo(HighScore o) {
			// We invert the logic and return -1 if we are greater to force descending order sort.
			return o.score - this.score;
		}
	}
}
