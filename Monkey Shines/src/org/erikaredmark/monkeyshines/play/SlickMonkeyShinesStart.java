package org.erikaredmark.monkeyshines.play;

import org.erikaredmark.monkeyshines.GameConstants;
import org.erikaredmark.monkeyshines.KeyBindingsSlick;
import org.erikaredmark.monkeyshines.global.VideoSettings;
import org.erikaredmark.monkeyshines.video.ScreenSize;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.ScalableGame;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.opengl.InternalTextureLoader;

/**
 * Static utility class to manage {@code SlickMonkeyShines}.
 * Only exists because otherwise SlickMonkeyShines is already a complex class.
 */
public class SlickMonkeyShinesStart {
	/**
	 * Starts the actual game either in fullscreen or not. Game always runs in 640x480 resolution
	 * internally, whether or not it is scaled visually.
	 * <p/>
	 * Returning false here indicates the game is already running and another instance won't start
	 * (so a user couldn't alt-tab to the other JFrame for the main menu and open another world).
	 * If the game is not running, this will return true indicating that <strong> it already ran</strong>.
	 * This is a blocking method... once called, control will not return until the user exits the game.
	 * <p/>
	 * More critical failures to run the game are found in the generated SlickException
	 * @param world
	 * 		the actual world to play
	 * @param fullScreen
	 * 		{@code true} to use fullscreen, {@code false} not to.
	 * @return
	 * @throws SlickException
	 */
	public static boolean startMonkeyShines(FrozenWorld world, KeyBindingsSlick keyBindings, boolean fullScreen) 
		throws SlickException
	{
		if (SlickMonkeyShines.running)
		{
			return false;
		}
		
		SlickMonkeyShines.running = true;
		SlickMonkeyShines monkeyShines = new SlickMonkeyShines(world,  keyBindings);
		
		AppGameContainer bonzoContainer = new AppGameContainer(
			new ScalableGame(
				monkeyShines,
				GameConstants.SCREEN_WIDTH, 
				GameConstants.SCREEN_HEIGHT + GameConstants.UI_HEIGHT));
		monkeyShines.setQuitAction(() -> {
			monkeyShines.closeRequested();
			bonzoContainer.exit();
		});
		
		// TODO separate video settings so that both full/non full screen can use variety of size settings.
		ScreenSize resolution = fullScreen 
			? ScreenSize.getLargestResolution()
			: VideoSettings.getResolution();
			
		bonzoContainer.setDisplayMode(
			resolution.getWidth(),
			resolution.getHeight(),
			fullScreen);
		
		bonzoContainer.setIcon("resources/graphics/ms_launch.png");
		
		// This game was never set up with the ability to calculate things using a delta of time between
		// updating game logic. Easiest solution currently is to just clamp the speed to the exact speed it
		// should run, which shouldn't have a problem on modern systems given how simple the game is.
		bonzoContainer.setMinimumLogicUpdateInterval(GameConstants.GAME_SPEED);
		bonzoContainer.setTargetFrameRate(GameConstants.FRAMES_PER_SECOND);
		bonzoContainer.setForceExit(false);
	
		try {
			bonzoContainer.start();
		// This code doesn't continue until game ends.
		} finally {
			monkeyShines.destroySounds();
			SlickMonkeyShines.running = false;
		}
		
		bonzoContainer.destroy();
		
		// This is VERY important! If the texture cache is not cleared, then if the user
		// starts another game the textures from the previous game will collide with the textures
		// from the... it's basically a fucking mess. Comment this out to see something cool when
		// choosing another world but otherwise keep this in.
		InternalTextureLoader.get().clear();
		
		return true;
	}
}
