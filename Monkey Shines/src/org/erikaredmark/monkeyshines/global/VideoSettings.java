package org.erikaredmark.monkeyshines.global;

/**
 * 
 * Global settings object for information on how the graphics should be displayed
 * 
 * @author Erika Redmark
 *
 */
public class VideoSettings {
	
	// Debug: testing fullscreen
	private static boolean fullScreen = false;
	
	/**
	 * 
	 * Determines if the user asked for a fullscreen mode. Fullscreen mode affects ONLY the actual
	 * gameplay, not the level editor nor menus.
	 * <p/>
	 * This is merely a request (albeit one that probably can be fulfilled in almost all cases), if
	 * the hardware can't support it the game should play in windowed mode.
	 * 
	 * @return
	 * 		{@code true} if the game should be played in fullscreen, {@code false} if otherwise.
	 * 
	 */
	public static boolean isFullscreen() { return fullScreen; }
}
