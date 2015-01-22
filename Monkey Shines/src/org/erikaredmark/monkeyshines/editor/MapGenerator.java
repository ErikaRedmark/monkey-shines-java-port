package org.erikaredmark.monkeyshines.editor;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.LevelScreen;
import org.erikaredmark.monkeyshines.World;

/**
 * 
 * static utility class that, given a world, outputs a rasterised .png file, at full size, of every screen in the world
 * in correct orientation. This effectively makes it easy to view a map in its entirety.
 * <p/>
 * Rasterisation starts at some screen id, and a 640x400 image is rendered for each screen, connected at the expected locations. However, due
 * to bonus worlds, other unconnected screens may exists. Multiple rasterisations are needed for each unconnected set (client sends in
 * the starting screen id, and only connected screens from there are rendered). Under typical circumstances, rendering starting at
 * screen 1000 and the bonus screen should be enough to render every screen in the level. Any other screens, if they exist, would be
 * otherwise unaccessible anyway.
 * 
 * @author Erika Redmark
 *
 */
public final class MapGenerator {

	private MapGenerator() { }
	
	public static BufferedImage generateMap(World world, int screenStart) { 
		
		// Take a listing of all screens. Starting at screenStart, look up, right, down, and left. We
		// only capture all connecting screens.
		Deque<LevelScreen> walkthrough = new ArrayDeque<>();
		List<LevelScreen> drawThese = new ArrayList<>();
		Set<Integer> alreadyLooked = new HashSet<>();
		walkthrough.push(world.getScreenByID(screenStart) );
		
		while (!(walkthrough.isEmpty() ) ) {
			LevelScreen next = walkthrough.pop();
			int nextId = next.getId();
			alreadyLooked.add(nextId);
			drawThese.add(next);
			
			// Add to deque all four directions, checking the set to make sure we don't
			// backtrack
			int[] directions = new int[] {
				nextId + 100, 
				nextId + 1, 
				nextId - 100, 
				nextId - 1
			};
			
			for (int dir : directions) {
				if (   !(alreadyLooked.contains(dir) ) 
				    && world.screenIdExists(dir) ) {
					
					LevelScreen dirScreen = world.getScreenByID(dir);
					walkthrough.push(dirScreen);
				}
			}
		}
		
		// First, we need to resolve the ids to logical width/height indexes. Screen ids have an ambiguity: for example,
		// 1158 may be left of 1200 or right of 1100. We assume the last two digits of 50 as a cutaway point (similar to
		// our original-level import functionality) for image exporting. This should handle MOST cases.
		List<IdResolved> lvlsFormed = new ArrayList<>();
		int maxWidthIndex = Integer.MIN_VALUE;
		int maxHeightIndex = Integer.MIN_VALUE;
		int minWidthIndex = Integer.MAX_VALUE;
		int minHeightIndex = Integer.MAX_VALUE;
		
		for (LevelScreen lvl : drawThese ) {
			int id = lvl.getId();
			// Abs important: - indexed screens will mess up the baseline 50 size usage.
			int lastTwoDigits = Math.abs(id % 100);
			int exceptLastTwoDigits = id / 100;
			
			int widthIndex;
			int heightIndex;
			if (lastTwoDigits > 50) {
				// Negative intended
			    widthIndex = lastTwoDigits - 100;
				heightIndex = exceptLastTwoDigits + 1;
			} else {
				widthIndex = lastTwoDigits;
				heightIndex = exceptLastTwoDigits;
			}
			
			lvlsFormed.add(new IdResolved(id, widthIndex, heightIndex) );
			if (widthIndex > maxWidthIndex) {
				maxWidthIndex = widthIndex;
			}
			
			if (widthIndex < minWidthIndex) {
				minWidthIndex = widthIndex;
			}
			
			if (heightIndex > maxHeightIndex) {
				maxHeightIndex = heightIndex;
			}
			
			if (heightIndex < minHeightIndex) {
				minHeightIndex = heightIndex;
			}
		}
		
		// Postcondition: We have each level paired with a width/height index that unambigiously can be used to place it into a grid. We
		// have calculated the min/max of these indexes and can now find the size of the image and draw onto it.
		
		// +1 to correct for length since these are indexes.
		int unitWidth = Math.abs(maxWidthIndex - minWidthIndex) + 1;
		int unitHeight = Math.abs(maxHeightIndex - minHeightIndex) + 1;
		
		Map<Integer, LevelScreen> allLvlScreens = world.getLevelScreens();
		
		BufferedImage map = new BufferedImage(unitWidth * GameConstants.SCREEN_WIDTH, unitHeight * GameConstants.SCREEN_HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = map.createGraphics();
		try {
			for (IdResolved nextId : lvlsFormed) {
				// resolve non-zero based indexes to 0 based
				int drawX = (nextId.widthIndex + (-minWidthIndex) ) * GameConstants.SCREEN_WIDTH;
				// Height index needs to be inverted
				int normalisedHeight = nextId.heightIndex + (-minHeightIndex);
				int drawY = ( (unitHeight - normalisedHeight) * GameConstants.SCREEN_HEIGHT);
				
				LevelScreen actualScreen = allLvlScreens.get(nextId.id);
				g2d.translate(drawX, drawY);
				actualScreen.paint(g2d);
				g2d.translate(-drawX, -drawY);
			}
		} finally {
			g2d.dispose();
		}
		
		return map;
	}
	
	// Represents a level id with it's width/height index set to a non-ambigious value for ease in creating the maps.
	private static final class IdResolved {
		public final int id, widthIndex, heightIndex;
		
		IdResolved(int id, int widthIndex, int heightIndex) {
			this.id = id;
			this.widthIndex = widthIndex;
			this.heightIndex = heightIndex;
		}
	}
	
	
	
}
