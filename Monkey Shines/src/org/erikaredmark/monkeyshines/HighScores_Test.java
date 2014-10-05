package org.erikaredmark.monkeyshines;

import static org.junit.Assert.*;

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
	
}
