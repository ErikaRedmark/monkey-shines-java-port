package org.erikaredmark.monkeyshines.video;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import org.erikaredmark.monkeyshines.GameConstants;

/**
 * Encodes basic screen size information, and static utility methods to list available resolutions.
 * <p/>
 * Intended to be used for the user to pick a resolution which the game will ultimately run at.
 */
public class ScreenSize {

	private final int width;
	private final int height;
	
	public ScreenSize(int w, int h) {
		this.width = w;
		this.height = h;
	}
	
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	
	/**
	 * Returns the default screen resolution when not user-defined preference is overriding it. Essentially, this
	 * resolution is pixel by pixel the exact size to fit the 
	 * @return
	 */
	public static final ScreenSize getDefaultResolution() {
		return DEFAULT_RESOLUTION;
	}
	
	/**
	 * Returns the largest available resolution for the <strong>default, main</strong> monitor.
	 */
	public static final ScreenSize getLargestResolution() {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		return new ScreenSize(gd.getDisplayMode().getWidth(), gd.getDisplayMode().getHeight());
	}
	
//	/**
//	 * Returns the largest available resolution for the <strong>default, main</strong> monitor, but scales
//	 * downwards the longest side so that the ratio between Width and height is the same as the ratio for the
//	 * default resultion.
//	 */
//	public static final ScreenSize getLargestResolutionKeepRatio() {
//		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
//		int width = gd.getDisplayMode().getWidth();
//		int height = gd.getDisplayMode().getHeight();
//		
//		// bring down the biggest so it fits at least one side of the screen.
//		if (width > height) {
//			height = (int)((double)width * RESOLUTION_RATIO_HEIGHT_TO_WIDTH);
//		} else {
//			width = (int)((double)height * RESOLUTION_RATIO_WIDTH_TO_HEIGHT);
//		}
//		
//		return new ScreenSize(width, height);
//	}
	
	public static final int getDefaultResolutionWidth() {
		return DEFAULT_RESOLUTION.getWidth();
	}
	
	public static final int getDefaultResolutionHeight() {
		return DEFAULT_RESOLUTION.getHeight();
	}
	
	private static final ScreenSize DEFAULT_RESOLUTION = new ScreenSize(
		GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT + GameConstants.UI_HEIGHT);
	
//	private static final double RESOLUTION_RATIO_WIDTH_TO_HEIGHT = (double)DEFAULT_RESOLUTION.getWidth() / (double)DEFAULT_RESOLUTION.getHeight();
//	private static final double RESOLUTION_RATIO_HEIGHT_TO_WIDTH = (double)DEFAULT_RESOLUTION.getHeight() / (double)DEFAULT_RESOLUTION.getWidth();
	
}
