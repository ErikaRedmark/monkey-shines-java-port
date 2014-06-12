package org.erikaredmark.monkeyshines;

import javax.swing.JFrame;

public class MonkeyShines extends JFrame {
	private static final long serialVersionUID = 4831422227222747908L;
	
	
	GameWindow currentWorld;
	KeyboardInput keys;
	
	
	private final Runnable resetCallback = new Runnable() {
		@Override public void run() {
			reset();
		}
	};
	
	/**
	 * Initialises the game and the GUI.
	 */
	public MonkeyShines() {
		keys = new KeyboardInput();
		currentWorld = new GameWindow(keys, resetCallback);
		// Must add to both.
		this.addKeyListener(keys);
		add(currentWorld);
		setTitle("Monkey Shines");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
	}
	
	// Currently the callback for game overs. This is a temporary measure
	// until the main menu is implemented.
	private void reset() {
		currentWorld.setVisible(false);
		currentWorld = new GameWindow(keys, resetCallback);
	}

	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new MonkeyShines();
	}

}
