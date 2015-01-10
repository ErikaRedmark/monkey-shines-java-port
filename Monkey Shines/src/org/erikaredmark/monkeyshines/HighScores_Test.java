package org.erikaredmark.monkeyshines;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.erikaredmark.monkeyshines.HighScores.HighScore;
import org.junit.Test;

public final class HighScores_Test {
	
	/**
	 * 
	 * Tests adding scores to a high scores list and ensures they stay ordered (also 
	 * tests the return-type for accessing the list)
	 * 
	 */
	@Test public void addScoresEnsureSorted() {
		HighScores scores = new HighScores();
		scores.addScore("Test 1", 451);
		scores.addScore("Test 2", 200);
		scores.addScore("Test 3", 797);
		scores.addScore("Test 4", 900);
		scores.addScore("Test 5", 100);
		scores.addScore("Test 6", 40);
		
		List<HighScore> actual = scores.getHighScores();
		assertEquals(6, actual.size() );
		HighScore first = actual.get(0);
		HighScore second = actual.get(1);
		HighScore third = actual.get(2);
		HighScore fourth = actual.get(3);
		HighScore fifth = actual.get(4);
		HighScore sixth = actual.get(5);
		
		assertEquals("Test 4", first.getName() );
		assertEquals(900, first.getScore() );
		
		assertEquals("Test 3", second.getName() );
		assertEquals(797, second.getScore() );
		
		assertEquals("Test 1", third.getName() );
		assertEquals(451, third.getScore() );
		
		assertEquals("Test 2", fourth.getName() );
		assertEquals(200, fourth.getScore() );
		
		assertEquals("Test 5", fifth.getName() );
		assertEquals(100, fifth.getScore() );
		
		assertEquals("Test 6", sixth.getName() );
		assertEquals(40, sixth.getScore() );
	}
	
	/**
	 * 
	 * Adds a high score to a high scores list that displaces a lower one, bumping it out.
	 * 
	 */
	@Test public void addScoreBumpOut() {
		HighScores scores = new HighScores();
		scores.addScore("Test 1", 8000);
		scores.addScore("Test 2", 8100);
		scores.addScore("Test 3", 8200);
		scores.addScore("Test 4", 8300);
		scores.addScore("Test 5", 8400);
		scores.addScore("Test 6", 8500);
		scores.addScore("Test 7", 8600);
		scores.addScore("Test 8", 8700);
		scores.addScore("Test 9", 8800);
		scores.addScore("Test 10", 8900);
		
		assertTrue(scores.isScoreHigh(8555) );
		scores.addScore("Newer", 8555);
		
		List<HighScore> actual = scores.getHighScores();
		assertEquals(10, actual.size() );
		
		HighScore first = actual.get(0);
		HighScore second = actual.get(1);
		HighScore third = actual.get(2);
		HighScore fourth = actual.get(3);
		HighScore fifth = actual.get(4);
		HighScore sixth = actual.get(5);
		HighScore seventh = actual.get(6);
		HighScore eighth = actual.get(7);
		HighScore ninth = actual.get(8);
		HighScore tenth = actual.get(9);
		
		assertEquals("Test 10", first.getName() );
		assertEquals(8900, first.getScore() );
		
		assertEquals("Test 9", second.getName() );
		assertEquals(8800, second.getScore() );
		
		assertEquals("Test 8", third.getName() );
		assertEquals(8700, third.getScore() );
		
		assertEquals("Test 7", fourth.getName() );
		assertEquals(8600, fourth.getScore() );
		
		assertEquals("Newer", fifth.getName() );
		assertEquals(8555, fifth.getScore() );
		
		assertEquals("Test 6", sixth.getName() );
		assertEquals(8500, sixth.getScore() );
		
		assertEquals("Test 5", seventh.getName() );
		assertEquals(8400, seventh.getScore() );
		
		assertEquals("Test 4", eighth.getName() );
		assertEquals(8300, eighth.getScore() );
		
		assertEquals("Test 3", ninth.getName() );
		assertEquals(8200, ninth.getScore() );
		
		assertEquals("Test 2", tenth.getName() );
		assertEquals(8100, tenth.getScore() );
	}
	
	/**
	 * 
	 * An exception should occur if a score too low is added to the list, and ensures that the
	 * state checking method properly reflects if the given score should not be added.
	 * 
	 */
	@Test(expected=IllegalArgumentException.class) public void addScoreNotAllowed() {
		HighScores scores = new HighScores();
		scores.addScore("Test 1", 8000);
		scores.addScore("Test 2", 8100);
		scores.addScore("Test 3", 8200);
		scores.addScore("Test 4", 8300);
		scores.addScore("Test 5", 8400);
		scores.addScore("Test 6", 8500);
		scores.addScore("Test 7", 8600);
		scores.addScore("Test 8", 8700);
		scores.addScore("Test 9", 8800);
		scores.addScore("Test 10", 8900);
		
		assertFalse(scores.isScoreHigh(500) );
		scores.addScore("Throws Exception", 500);
	}
	
	/**
	 * 
	 * Ensures high scores can be properly saved and read back.
	 * 
	 */
	@Test public void saveAndRestore() throws IOException {
		HighScores scores = new HighScores();
		scores.addScore("Test 1", 8000);
		scores.addScore("Test 2", 8100);
		scores.addScore("Test 3", 8200);
		scores.addScore("Test 4", 8300);
		scores.addScore("Test 5", 8400);
		scores.addScore("Test 6", 8500);
		scores.addScore("Test 7", 8600);
		scores.addScore("Test 8", 8700);
		scores.addScore("Test 9", 8800);
		scores.addScore("Test 10", 8900);
		
		Path temp = Files.createTempFile("score", "ms");
		scores.persistScores(temp);
		
		HighScores other = HighScores.fromFile(temp);
		
		List<HighScore> result = other.getHighScores();
		
		assertEquals("Test 10", result.get(0).getName() );
		assertEquals(8900, result.get(0).getScore() );
		
		assertEquals("Test 9", result.get(1).getName() );
		assertEquals(8800, result.get(1).getScore() );
		
		assertEquals("Test 8", result.get(2).getName() );
		assertEquals(8700, result.get(2).getScore() );
		
		assertEquals("Test 7", result.get(3).getName() );
		assertEquals(8600, result.get(3).getScore() );
		
		assertEquals("Test 6", result.get(4).getName() );
		assertEquals(8500, result.get(4).getScore() );
		
		assertEquals("Test 5", result.get(5).getName() );
		assertEquals(8400, result.get(5).getScore() );
		
		assertEquals("Test 4", result.get(6).getName() );
		assertEquals(8300, result.get(6).getScore() );
		
		assertEquals("Test 3", result.get(7).getName() );
		assertEquals(8200, result.get(7).getScore() );
		
		assertEquals("Test 2", result.get(8).getName() );
		assertEquals(8100, result.get(8).getScore() );
		
		assertEquals("Test 1", result.get(9).getName() );
		assertEquals(8000, result.get(9).getScore() );
	}
	
	/**
	 * 
	 * Writes a file with too many scores. Ensures result contains only the first ten.
	 * 
	 */
	@Test public void tooManyScoresRestore() throws IOException {
		Path temp = Files.createTempFile("score", "ms");
		try (BufferedWriter writer = Files.newBufferedWriter(temp, Charset.forName("UTF-8") ) ) {
			writer.write("Test1,800\n");
			writer.write("Test2,790\n");
			writer.write("Test3,780\n");
			writer.write("Test4,770\n");
			// Intentional; ensures duplicates are allowed.
			writer.write("Test5,760\n");
			writer.write("Test5,750\n");
			writer.write("Test5,740\n");
			writer.write("Test5,730\n");
			writer.write("Test9,720\n");
			writer.write("Test10,710\n");
			// These two don't make the cut even though they are higher.
			writer.write("Test11,1700\n");
			writer.write("Test12,4690\n");
		}
		
		HighScores scores = HighScores.fromFile(temp);
		List<HighScore> result = scores.getHighScores();
		
		assertEquals("Test1", result.get(0).getName() );
		assertEquals(800, result.get(0).getScore() );
		
		assertEquals("Test2", result.get(1).getName() );
		assertEquals(790, result.get(1).getScore() );
		
		assertEquals("Test3", result.get(2).getName() );
		assertEquals(780, result.get(2).getScore() );
		
		assertEquals("Test4", result.get(3).getName() );
		assertEquals(770, result.get(3).getScore() );
		
		assertEquals("Test5", result.get(4).getName() );
		assertEquals(760, result.get(4).getScore() );
		
		assertEquals("Test5", result.get(5).getName() );
		assertEquals(750, result.get(5).getScore() );
		
		assertEquals("Test5", result.get(6).getName() );
		assertEquals(740, result.get(6).getScore() );
		
		assertEquals("Test5", result.get(7).getName() );
		assertEquals(730, result.get(7).getScore() );
		
		assertEquals("Test9", result.get(8).getName() );
		assertEquals(720, result.get(8).getScore() );
		
		assertEquals("Test10", result.get(9).getName() );
		assertEquals(710, result.get(9).getScore() );
	}
	
	/**
	 * 
	 * Rights scores to file out of order. Loading should not be contingent on order.
	 * 
	 */
	@Test public void outOfOrderRestore() throws IOException {
		Path temp = Files.createTempFile("score", "ms");
		try (BufferedWriter writer = Files.newBufferedWriter(temp, Charset.forName("UTF-8") ) ) {
			writer.write("Test1,400\n");
			writer.write("Test2,790\n");
			writer.write("Test3,180\n");
		}
		
		HighScores scores = HighScores.fromFile(temp);
		List<HighScore> result = scores.getHighScores();
		
		assertEquals("Test2", result.get(0).getName() );
		assertEquals(790, result.get(0).getScore() );
		
		assertEquals("Test1", result.get(1).getName() );
		assertEquals(400, result.get(1).getScore() );
		
		assertEquals("Test3", result.get(2).getName() );
		assertEquals(180, result.get(2).getScore() );
	}
	
}
