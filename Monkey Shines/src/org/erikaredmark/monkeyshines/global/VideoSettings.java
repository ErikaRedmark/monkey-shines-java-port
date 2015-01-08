package org.erikaredmark.monkeyshines.global;

import org.erikaredmark.monkeyshines.global.PreferencePersistException;

/**
 * 
 * Global settings object for information on how the graphics should be displayed
 * 
 * @author Erika Redmark
 *
 */
public class VideoSettings {
	
	private static boolean fullScreen = MonkeyShinesPreferences.defaultFullscreen();
	
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
	
	/**
	 * 
	 * Sets whether the game should run in fullscreen mode. This is only a request; if the request
	 * cannot be satisfied clients should fallback to windowed mode.
	 * 
	 * @param full
	 * 
	 */
	public static void setFullscreen(boolean full) { fullScreen = full; } 
	
	/**
	 *
	 * Updates preferences file (if possible) with changes. This is called manually so that playing around with
	 * preferences doesn't cause excessive disk usage. Only call when the preference is okayed or saved by the user.
	 * 
	 */
	public static void persist() throws PreferencePersistException { MonkeyShinesPreferences.persistVideo(); }
}
