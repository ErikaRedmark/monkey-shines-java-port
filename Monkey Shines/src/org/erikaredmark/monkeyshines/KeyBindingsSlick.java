package org.erikaredmark.monkeyshines;

import java.awt.event.KeyEvent;

import org.newdawn.slick.Input;

import com.google.common.collect.ImmutableMap;

/**
 * Slick-based key bindings. in order to preserve user options, key bindings are still saved as 
 * AWT KeyEvent keycodes, but upon starting the game, that keybinding must be converted to this form.
 * <p/>
 * It is the responsibility of the UI to not allow AWT key codes that are not mappable to Slick ones.
 * <p/>
 * Instances of this class are immutable.
 */
public class KeyBindingsSlick {
	public final int left;
	public final int right;
	public final int jump;
	
	private KeyBindingsSlick(int left, int right, int jump) {
		this.left = left;
		this.right = right;
		this.jump = jump;
	}
	
	/**
	 * Translates awt key codes to Slick ones when constructing this object. Note that some keys may not convert
	 * properly; check any new key codes against keyMappingExistsFor first.
	 * 
	 * If a key code cannot be converted, it assumes the defaults (arrow keys)
	 * @param code
	 * @return
	 */
	public static KeyBindingsSlick fromKeyBindingsAwt(KeyBindingsAwt awtKeys) {
		return new KeyBindingsSlick(
			awtToSlick.getOrDefault(awtKeys.left, Input.KEY_LEFT),
			awtToSlick.getOrDefault(awtKeys.right, Input.KEY_RIGHT),
			awtToSlick.getOrDefault(awtKeys.jump, Input.KEY_UP));
	}
	
	
	/**
	 * Checks if the given key code from Java awt can map to something from Slick.
	 * GUI should check this before allowing the user to commit the change.
	 */
	public static boolean keyMappingExistsFor(int awtCode) {
		return awtToSlick.containsKey(awtCode);
	}
	
	// There are probably some missing keys here. Hopefully this covers most of them.
	private static ImmutableMap<Integer, Integer> awtToSlick = ImmutableMap.<Integer, Integer>builder().
		put(KeyEvent.VK_0, Input.KEY_0).
		put(KeyEvent.VK_1, Input.KEY_1).
		put(KeyEvent.VK_2, Input.KEY_2).
		put(KeyEvent.VK_3, Input.KEY_3).
		put(KeyEvent.VK_4, Input.KEY_4).
		put(KeyEvent.VK_5, Input.KEY_5).
		put(KeyEvent.VK_6, Input.KEY_6).
		put(KeyEvent.VK_7, Input.KEY_7).
		put(KeyEvent.VK_8, Input.KEY_8).
		put(KeyEvent.VK_9, Input.KEY_9).
		put(KeyEvent.VK_A, Input.KEY_A).
		put(KeyEvent.VK_B, Input.KEY_B).
		put(KeyEvent.VK_C, Input.KEY_C).
		put(KeyEvent.VK_D, Input.KEY_D).
		put(KeyEvent.VK_E, Input.KEY_E).
		put(KeyEvent.VK_F, Input.KEY_F).
		put(KeyEvent.VK_G, Input.KEY_G).
		put(KeyEvent.VK_H, Input.KEY_H).
		put(KeyEvent.VK_I, Input.KEY_I).
		put(KeyEvent.VK_J, Input.KEY_J).
		put(KeyEvent.VK_K, Input.KEY_K).
		put(KeyEvent.VK_L, Input.KEY_L).
		put(KeyEvent.VK_M, Input.KEY_M).
		put(KeyEvent.VK_N, Input.KEY_N).
		put(KeyEvent.VK_O, Input.KEY_O).
		put(KeyEvent.VK_P, Input.KEY_P).
		put(KeyEvent.VK_Q, Input.KEY_Q).
		put(KeyEvent.VK_R, Input.KEY_R).
		put(KeyEvent.VK_S, Input.KEY_S).
		put(KeyEvent.VK_T, Input.KEY_T).
		put(KeyEvent.VK_U, Input.KEY_U).
		put(KeyEvent.VK_V, Input.KEY_V).
		put(KeyEvent.VK_W, Input.KEY_W).
		put(KeyEvent.VK_X, Input.KEY_X).
		put(KeyEvent.VK_Y, Input.KEY_Y).
		put(KeyEvent.VK_Z, Input.KEY_Z).
		put(KeyEvent.VK_ESCAPE, Input.KEY_ESCAPE).
		put(KeyEvent.VK_ENTER, Input.KEY_ENTER).
		put(KeyEvent.VK_SPACE, Input.KEY_SPACE).
		put(KeyEvent.VK_ADD, Input.KEY_ADD).
		put(KeyEvent.VK_MINUS, Input.KEY_MINUS).
		put(KeyEvent.VK_SUBTRACT, Input.KEY_SUBTRACT).
		put(KeyEvent.VK_MULTIPLY, Input.KEY_MULTIPLY).
		put(KeyEvent.VK_DIVIDE, Input.KEY_DIVIDE).
		put(KeyEvent.VK_DECIMAL, Input.KEY_DECIMAL).
		put(KeyEvent.VK_COMMA, Input.KEY_COMMA).
		put(KeyEvent.VK_PERIOD, Input.KEY_PERIOD).
		put(KeyEvent.VK_NUMPAD0, Input.KEY_NUMPAD0).
		put(KeyEvent.VK_NUMPAD1, Input.KEY_NUMPAD1).
		put(KeyEvent.VK_NUMPAD2, Input.KEY_NUMPAD2).
		put(KeyEvent.VK_NUMPAD3, Input.KEY_NUMPAD3).
		put(KeyEvent.VK_NUMPAD4, Input.KEY_NUMPAD4).
		put(KeyEvent.VK_NUMPAD5, Input.KEY_NUMPAD5).
		put(KeyEvent.VK_NUMPAD6, Input.KEY_NUMPAD6).
		put(KeyEvent.VK_NUMPAD7, Input.KEY_NUMPAD7).
		put(KeyEvent.VK_NUMPAD8, Input.KEY_NUMPAD8).
		put(KeyEvent.VK_NUMPAD9, Input.KEY_NUMPAD9).
		put(KeyEvent.VK_CLOSE_BRACKET, Input.KEY_LBRACKET).
		put(KeyEvent.VK_OPEN_BRACKET, Input.KEY_RBRACKET).
		put(KeyEvent.VK_COLON, Input.KEY_COLON).
		put(KeyEvent.VK_SLASH, Input.KEY_SLASH).
		put(KeyEvent.VK_SEMICOLON, Input.KEY_SEMICOLON).
		put(KeyEvent.VK_BACK_SLASH, Input.KEY_BACKSLASH).
		put(KeyEvent.VK_LEFT, Input.KEY_LEFT).
		put(KeyEvent.VK_RIGHT, Input.KEY_RIGHT).
		put(KeyEvent.VK_UP, Input.KEY_UP).
		put(KeyEvent.VK_DOWN, Input.KEY_DOWN).
		build();
}
