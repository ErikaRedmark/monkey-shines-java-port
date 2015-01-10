package org.erikaredmark.monkeyshines;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.Lists;

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

	public HighScores() { }
	
	public HighScores(List<HighScore> initialScores) {
		if (initialScores.size() > MAX_SCORES) {
			LOGGER.warning("More scores in file than maximum (10). Only first " + MAX_SCORES + " scores from the top of the file will be used");
			initialScores = Lists.partition(initialScores, MAX_SCORES).get(0);
		}
		// Do not copy or order may not be preserved.
		// Score size will be handled automatically by addScore, do NOT set it here.
		for (HighScore s : initialScores) {
			addScore(s.name, s.score);
		}

	}
	
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
	 * 		name of the person who got the score. Commas will be removed (as they are internal delimiters)
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
		
		name = name.replace(',', ' ');
		
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
	 * Persists the high scores data as a basic list written out to a text file. The path should NOT be
	 * the preferences file.
	 * 
	 * @param scoresList
	 * 		path to high scores list. File will be created if one does not exist
	 * 
	 * @return
	 * 		{@code true} if the high scores could be saved, {@code false} if otherwise
	 * 
	 */
	public boolean persistScores(Path highScores) {
		try {
			writeScores(highScores, scores, scoreSize);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	/** Reads the high score list from a basic plain-text input. Each line is a name, delimiter, and score.
	 * Delimiter is defined as a comma.
	 * @param scoreFile the input file
	 * @return list of high scores from file.
	 * @throws IOException if the file could not be read. Ensure it exists.
	 */
	private static List<HighScore> readScores(Path scoreFile) throws IOException {
		List<HighScore> highReturns = new ArrayList<>();
		try (BufferedReader reader = Files.newBufferedReader(scoreFile, Charset.forName("UTF-8") ) ) {
			String scoreLine = null;
			while ( (scoreLine = reader.readLine() ) != null) {
				String[] parts = scoreLine.split("\\,");
				
				// Ignore bad lines; the user can modify the file so let's try to be as liberal as possible.
				if (parts.length != 2)  continue;
				highReturns.add(new HighScore(parts[0], Integer.parseInt(parts[1]) ) );
			}
		}
		
		return highReturns;
	}
	
	private static void writeScores(Path scoreFile, HighScore[] scores, int scoreSize) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(scoreFile, Charset.forName("UTF-8") ) ) {
			for (int i = 0; i < scoreSize; ++i) {
				String writeoutLine = scores[i].getName() + "," + scores[i].getScore() + "\n";
				writer.write(writeoutLine);
			}
		}
	}
	
	/**
	 * 
	 * Constructs an instance of this object from a high score .txt, If this file cannot be
	 * read, the generated high scores will be empty. If the file does not exist, a new file
	 * will be generated and an empty high scores object will be returned.
	 * 
	 * @return
	 * 		instance of this object
	 * 
	 */
	public static HighScores fromFile(Path highScoreList) {
		try {
			if (!(Files.exists(highScoreList) ) ) {
				Files.createFile(highScoreList);
			}
			List<HighScore> tempScores = readScores(highScoreList);
			return new HighScores(tempScores);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING,
					   "Could not read high scores: " + e.getMessage(),
					   e);
			
			return new HighScores();
		}
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
