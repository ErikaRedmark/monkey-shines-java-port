package org.erikaredmark.monkeyshines;

/**
 * PLEASE NOTE!
 * All this code was taken directly from http://www.gamedev.net/reference/programming/features/javainput/page2.asp
 * I looked through it and understand how it works, but there was no point in re-writing it all from scratch,
 * so I will be using this class as if I was using a Java library function, because it fulfills a very basic
 * role.
 */

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * 
 * The KeyboardInput class handles making sure the key presses respond quickly
 * and intuitively.
 *
 */
public class KeyboardInput implements KeyListener {

	private static final int KEY_COUNT = 256;

	private enum KeyState {
		RELEASED, // Not down
		PRESSED,  // Down, but not the first time
		ONCE      // Down for the first time
	}

	// Current state of the keyboard
	private boolean[] currentKeys = null;

	// Polled keyboard state
	private KeyState[] keys = null;

	public KeyboardInput() {
		currentKeys = new boolean[ KEY_COUNT ];
		keys = new KeyState[ KEY_COUNT ];
		for( int i = 0; i < KEY_COUNT; ++i ) {
			keys[ i ] = KeyState.RELEASED;
		}
	}

	public synchronized void poll() {
		for( int i = 0; i < KEY_COUNT; ++i ) {
			// Set the key state 
			if( currentKeys[ i ] ) {
				// If the key is down now, but was not
				// down last frame, set it to ONCE,
				// otherwise, set it to PRESSED
				if( keys[ i ] == KeyState.RELEASED )
					keys[ i ] = KeyState.ONCE;
				else
					keys[ i ] = KeyState.PRESSED;
			} else {
				keys[ i ] = KeyState.RELEASED;
			}
		}
	}

	public boolean keyDown( int keyCode ) {
		return keys[ keyCode ] == KeyState.ONCE ||
				keys[ keyCode ] == KeyState.PRESSED;
	}

	public boolean keyDownOnce( int keyCode ) {
		return keys[ keyCode ] == KeyState.ONCE;
	}

	@Override public synchronized void keyPressed( KeyEvent e ) {
		int keyCode = e.getKeyCode();
		if( keyCode >= 0 && keyCode < KEY_COUNT ) {
			currentKeys[ keyCode ] = true;
		}
	}

	@Override public synchronized void keyReleased( KeyEvent e ) {
		int keyCode = e.getKeyCode();
		if( keyCode >= 0 && keyCode < KEY_COUNT ) {
			currentKeys[ keyCode ] = false;
		}
	}

	@Override public void keyTyped( KeyEvent e ) {
		// Not needed
	}
}

