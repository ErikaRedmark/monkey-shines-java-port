package org.erikaredmark.monkeyshines;

import java.util.Collection;
import java.util.logging.Logger;

/**
 * 
 * Used for statistics and point calculations. Generated after a world is finished.
 * 
 * @author Erika Redmark
 *
 */
public final class WorldStatistics {
	private static final String CLASS_NAME = "org.erikaredmark.monkeyshines.WorldStatistics";
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

	
	/**
	 * 
	 * Constructs the statistics object with the given raw data.
	 * 
	 * @param allGoodies
	 * 		a collection of all the goodies in the world. All non-essential goodies will be totaled and compared with
	 * 		the number of collected goodies for a percentage. Only goodies that grant a non-zero score are under
	 * 		consideration
	 * 
	 * @param collectedGoodies
	 * 		the number of goodies collected in the world (tally is based on goodies granting non-zero score)
	 * 
	 * @param score
	 * 		the raw score at the end of the game. This score will be modified based on other calculations for an eventual
	 * 		total
	 * 	
	 * @param bonusTimer
	 * 		countdown timer state before game end
	 * 
	 * @param playtesting
	 * 		if the game was played in playtesting mode or not. Scoring is inverted (negatives) in playtesting.
	 * 
	 */
	WorldStatistics(final Collection<Goodie> allGoodies, final int collectedGoodies, final int score, final int bonusTimer, final boolean playtesting) {
		int totalFruit = 0;
 		for (Goodie g : allGoodies) {
			if (g.getGoodieType().score > 0)  ++totalFruit;
		}
		
		// Note: Whilst the names are different, the algorithms for calculating the score are mostly identical to the original game
		// as these calculations were taken directly from the original source code. Small deviations were made in only a few
		// cases.
		
		// Fruit Bonus
		// Calculate percentage without conversion to double. This calculates how much fruit were REMAINING, not TAKEN.
		int fruitPercent =   (100 * (totalFruit - collectedGoodies) ) 
					       / (totalFruit);
		
		// Change into percent taken.
		fruitPercent = 100 - fruitPercent;

		boolean badCalculation = false;
		if (fruitPercent > 100) {
			LOGGER.warning(CLASS_NAME + ": Possible calculation errors: ended up with above 100% fruit collection " + fruitPercent + "%. Further calculations will be handled assuming 100% collection. Outputting raw stat input: ");
			badCalculation = true;
			fruitPercent = 100;
		} else if (fruitPercent < 0) {
			LOGGER.warning(CLASS_NAME + ": Possible calculation errors: ended up with negative % fruit collection " + fruitPercent + "%. Further calculations will be handled assuming 0% collection. Outputting raw stat input: ");
			badCalculation = true;
			fruitPercent = 0;
		}
		
		if (badCalculation) {
			LOGGER.warning("Total Fruit: " + totalFruit);
			LOGGER.warning("Collected Fruit: " + collectedGoodies);
			LOGGER.warning("Raw Score: " + score);
			LOGGER.warning("Bonus Timer: " + bonusTimer);
		}
		
		fruitCollectedPercent = fruitPercent;
		
		if (fruitCollectedPercent == 100)    this.fruitBonus = 10000;
		else if (fruitCollectedPercent > 95) this.fruitBonus = 7500;
		else if (fruitCollectedPercent > 90) this.fruitBonus = 5000;
		else if (fruitCollectedPercent > 80) this.fruitBonus = 2500;
		else if (fruitCollectedPercent > 50) this.fruitBonus = 1000;
		else                       			 this.fruitBonus = 0;
		
		// Time Bonus
		
		// Must be multiple of ten. It probably is unless the last red key was exactly where the exit door
		// would appear thus making the level end before the counter even ticked down. To handle that
		// case, 9999 is promoted to 10000. All other values are truncated to the nearest ten multiple.
		int tempBonus;
		if (bonusTimer == 9999) {
			tempBonus = 10000;
		} else {
			tempBonus = bonusTimer - (bonusTimer % 10);
		}
		
		timeBonus = tempBonus;
		
		// Raw Score
		rawScore = score;
		
		// Total!!!
		int tempTotalScore = rawScore + fruitBonus + timeBonus;
		
		// Wait, are we in playtesting mode? The best solution to a problem is usually the easiest one.
		if (playtesting)  tempTotalScore = -tempTotalScore;
		
		totalScore = tempTotalScore;
	}
	
	public int getFuritCollectedPercent() { return fruitCollectedPercent; }
	public int getFruitBonus() { return fruitBonus; }
	public int getTimeBonus() { return timeBonus; }
	public int getRawScore() { return rawScore; }
	public int getTotalScore() { return totalScore; }
	
	private final int fruitCollectedPercent;
	private final int fruitBonus;
	private final int timeBonus;
	// Raw score is from before all modifiers are applied
	private final int rawScore;
	// Total is after all modification
	private final int totalScore;
	
}
